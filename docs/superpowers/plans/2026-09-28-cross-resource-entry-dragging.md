# Cross-Resource Entry Dragging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the user drag a calendar entry horizontally from one resource column to another in `ResourcesView`, with a live preview, reassigning the entry to a calendar of the target resource on release.

**Architecture:** `DayViewEditController` stops assuming that the view it was constructed with hosts the whole drag. It snapshots a list of opt-in `DayViewBase` drag candidates once per gesture, hit-tests the cursor against them on every mouse-drag, and hands the `DraggedEntry` over to whichever view the cursor is in. On release the target calendar is resolved through a new `DateControl` callback and the mutation is delegated to a UI-free helper. Everything is gated behind a new `DayViewBase.crossViewDragEnabled` flag that only `ResourcesView`'s skin switches on, so no existing control changes behaviour.

**Tech Stack:** Java 21, JavaFX 23, Maven (wrapper `./mvnw`), JUnit 4 + Hamcrest.

**Spec:** `docs/superpowers/specs/2026-09-28-cross-resource-entry-dragging-design.md`

## Global Constraints

- **Never run `git commit`.** The repository owner commits. Every task ends with `git add` only, then stop.
- **Never run `git push`** and never rewrite history.
- Work on a dedicated branch created off `master`, not on `master` itself.
- Every new source file carries the Apache-2.0 DLSC copyright header, copied verbatim from an existing file such as `CalendarFXView/src/main/java/com/calendarfx/model/Entry.java`.
- JavaFX property methods are `final`: `fooProperty()`, `getFoo()`/`isFoo()`, `setFoo(...)`.
- Property Javadoc goes on the property accessor `fooProperty()` **only** — not on the field, getter or setter.
- Properties are created with the owner/name constructor: `new SimpleObjectProperty<>(this, "foo", defaultValue)`.
- **Tests must not touch any `DateControl` or `DayViewBase` instance.** Verified empirically: `new DayView()` in a surefire test fails with `NoClassDefFoundError: Could not initialize class com.calendarfx.view.DayViewBase`, and CI (`./mvnw -B verify` on `ubuntu-latest`) has no display. Tests may reference model classes only (`Entry`, `Calendar`, `Interval`).
- Tests are JUnit 4 with `org.hamcrest.MatcherAssert.assertThat` and a `// given / when / then` comment structure, following `CalendarFXView/src/test/java/com/calendarfx/model/EntryTest.java`.
- Build output may be localised. Prefix Maven commands with `MAVEN_OPTS="-Duser.language=en -Duser.country=US"` when grepping output.
- No `module-info.java` change is needed: `com.calendarfx.view` and `impl.com.calendarfx.view` are already both `exports`ed and `opens`ed.
- No CSS change is needed: the drag preview reuses the existing `dragged-entry` style class.

## File Structure

| File | Responsibility |
|---|---|
| `CalendarFXView/src/main/java/impl/com/calendarfx/view/EntryDropSupport.java` (new) | UI-free decision + mutation for a cross-view drop. The only automated-testable unit. |
| `CalendarFXView/src/test/java/impl/com/calendarfx/view/EntryDropSupportTest.java` (new) | Toolkit-free tests for the above. |
| `CalendarFXView/src/main/java/com/calendarfx/view/DateControl.java` | Adds `EntryDropParameter` + `entryDropCalendarProvider`, and binds it. |
| `CalendarFXView/src/main/java/com/calendarfx/view/DayViewBase.java` | Adds the `crossViewDragEnabled` opt-in flag + property sheet item. |
| `CalendarFXView/src/main/java/impl/com/calendarfx/view/DayViewEditController.java` | Drag hand-over between views. The heart of the feature. |
| `CalendarFXView/src/main/java/com/calendarfx/view/ResourcesView.java` | Adds the user-facing `enableCrossResourceDragging` switch. |
| `CalendarFXView/src/main/java/impl/com/calendarfx/view/ResourcesViewContainerSkin.java` | Turns the flag on for each per-resource view; rebuilds on toggle. |
| `CHANGES.txt`, `CalendarFXView/src/main/asciidoc/manual.adoc` | Documentation. |

---

### Task 0: Branch

**Files:** none

- [ ] **Step 1: Create the branch**

```bash
cd /Users/lemmi/git/CalendarFX
git checkout master
git checkout -b feature/cross-resource-dragging
```

- [ ] **Step 2: Confirm a clean baseline build**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -q -pl CalendarFXView test`
Expected: BUILD SUCCESS, 4 existing test classes pass.

---

### Task 1: `EntryDropSupport`

The drop decision, extracted so it can be tested without a JavaFX toolkit.

**Files:**
- Create: `CalendarFXView/src/main/java/impl/com/calendarfx/view/EntryDropSupport.java`
- Test: `CalendarFXView/src/test/java/impl/com/calendarfx/view/EntryDropSupportTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: `impl.com.calendarfx.view.EntryDropSupport.applyDrop(Entry<?> entry, Interval newInterval, Calendar targetCalendar)` returning `EntryDropSupport.DropResult`, an enum with constants `APPLIED`, `REJECTED_NO_CALENDAR`, `REJECTED_READ_ONLY`. Task 4 calls this.

- [ ] **Step 1: Write the failing tests**

Create `CalendarFXView/src/test/java/impl/com/calendarfx/view/EntryDropSupportTest.java`:

```java
/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import impl.com.calendarfx.view.EntryDropSupport.DropResult;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class EntryDropSupportTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 28);

    private final Calendar<String> sourceCalendar = new Calendar<>("Source");

    private final Calendar<String> targetCalendar = new Calendar<>("Target");

    private final Entry<String> entry = new Entry<>();

    private Interval originalInterval;

    private final Interval newInterval = new Interval(DATE, LocalTime.of(14, 0), DATE, LocalTime.of(15, 0));

    @Before
    public void setup() {
        entry.setZoneId(ZoneId.of("UTC"));
        entry.setInterval(new Interval(DATE, LocalTime.of(9, 0), DATE, LocalTime.of(10, 0)));
        entry.setCalendar(sourceCalendar);
        originalInterval = entry.getInterval();
    }

    @Test
    public void shouldRejectDropWhenTargetCalendarIsNull() {

        // when
        DropResult result = EntryDropSupport.applyDrop(entry, newInterval, null);

        // then
        assertThat(result, is(DropResult.REJECTED_NO_CALENDAR));
        assertThat(entry.getInterval(), is(equalTo(originalInterval)));
        assertThat(entry.getCalendar(), is(sameInstance(sourceCalendar)));
    }

    @Test
    public void shouldRejectDropWhenTargetCalendarIsReadOnly() {

        // given
        targetCalendar.setReadOnly(true);

        // when
        DropResult result = EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then
        assertThat(result, is(DropResult.REJECTED_READ_ONLY));
        assertThat(entry.getInterval(), is(equalTo(originalInterval)));
        assertThat(entry.getCalendar(), is(sameInstance(sourceCalendar)));
    }

    @Test
    public void shouldApplyIntervalAndCalendarWhenTargetCalendarIsWritable() {

        // when
        DropResult result = EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then
        assertThat(result, is(DropResult.APPLIED));
        assertThat(entry.getInterval(), is(equalTo(newInterval)));
        assertThat(entry.getCalendar(), is(sameInstance(targetCalendar)));
    }

    @Test
    public void shouldApplyIntervalBeforeCalendar() {

        // given
        AtomicReference<Interval> intervalSeenByCalendarChange = new AtomicReference<>();
        targetCalendar.addEventHandler(evt -> {
            if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED)) {
                intervalSeenByCalendarChange.set(evt.getEntry().getInterval());
            }
        });

        // when
        EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then
        assertThat(intervalSeenByCalendarChange.get(), is(equalTo(newInterval)));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -pl CalendarFXView test -Dtest=EntryDropSupportTest`
Expected: COMPILATION ERROR — `cannot find symbol: class EntryDropSupport`.

- [ ] **Step 3: Write the implementation**

Create `CalendarFXView/src/main/java/impl/com/calendarfx/view/EntryDropSupport.java`:

```java
/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;

import java.util.Objects;

/**
 * Applies the result of a drag gesture that ended in a different view than the one it
 * started in. Deliberately free of any UI types so that it can be unit tested without
 * an initialised JavaFX toolkit.
 */
public final class EntryDropSupport {

    /**
     * The outcome of a drop.
     */
    public enum DropResult {

        /**
         * The new interval and the target calendar were applied to the entry.
         */
        APPLIED,

        /**
         * No target calendar could be determined. The entry was left untouched.
         */
        REJECTED_NO_CALENDAR,

        /**
         * The target calendar is read-only. The entry was left untouched.
         */
        REJECTED_READ_ONLY
    }

    private EntryDropSupport() {
    }

    /**
     * Moves the given entry to the given calendar and interval. The entry is left
     * completely untouched when the drop is rejected: applying the new interval while
     * refusing the calendar change would leave the user with a confusing half-outcome.
     *
     * @param entry          the entry that was dragged
     * @param newInterval    the interval determined by the drag gesture
     * @param targetCalendar the calendar resolved for the drop target, may be null
     * @return the outcome of the drop
     */
    public static DropResult applyDrop(Entry<?> entry, Interval newInterval, Calendar targetCalendar) {
        Objects.requireNonNull(entry, "entry can not be null");
        Objects.requireNonNull(newInterval, "new interval can not be null");

        if (targetCalendar == null) {
            return DropResult.REJECTED_NO_CALENDAR;
        }

        if (targetCalendar.isReadOnly()) {
            return DropResult.REJECTED_READ_ONLY;
        }

        /*
         * Order matters. Entry.setCalendar fires ENTRY_CALENDAR_CHANGED on the old and
         * the new calendar, which makes both views reload. The interval has to be final
         * by then.
         */
        entry.setInterval(newInterval);
        entry.setCalendar(targetCalendar);

        return DropResult.APPLIED;
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -pl CalendarFXView test -Dtest=EntryDropSupportTest`
Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 5: Stage (do not commit)**

```bash
git add CalendarFXView/src/main/java/impl/com/calendarfx/view/EntryDropSupport.java \
        CalendarFXView/src/test/java/impl/com/calendarfx/view/EntryDropSupportTest.java
```

---

### Task 2: `DateControl.entryDropCalendarProvider`

**Files:**
- Modify: `CalendarFXView/src/main/java/com/calendarfx/view/DateControl.java`

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `DateControl.EntryDropParameter` with constructor `EntryDropParameter(Entry<?> entry, Calendar sourceCalendar, DateControl sourceDateControl, DateControl targetDateControl)` and getters `getEntry()`, `getSourceCalendar()`, `getSourceDateControl()`, `getTargetDateControl()`.
  - `DateControl.entryDropCalendarProviderProperty()` / `getEntryDropCalendarProvider()` / `setEntryDropCalendarProvider(Callback<EntryDropParameter, Calendar>)`. Task 4 calls the getter.

- [ ] **Step 1: Add the parameter class and the property**

In `DateControl.java`, insert the following directly **after** the closing brace of the `setEntryEditPolicy(...)` method (which currently ends just before the `/*\n     * Alert callback.\n     */` comment block, around line 1250):

```java
    /*
     * Entry drop calendar provider callback.
     */

    /**
     * The parameter object passed to the entry drop calendar provider.
     *
     * @see DateControl#entryDropCalendarProviderProperty()
     */
    public static final class EntryDropParameter {

        private final Entry<?> entry;

        private final Calendar sourceCalendar;

        private final DateControl sourceDateControl;

        private final DateControl targetDateControl;

        public EntryDropParameter(Entry<?> entry, Calendar sourceCalendar, DateControl sourceDateControl, DateControl targetDateControl) {
            this.entry = Objects.requireNonNull(entry);
            this.sourceCalendar = sourceCalendar;
            this.sourceDateControl = Objects.requireNonNull(sourceDateControl);
            this.targetDateControl = Objects.requireNonNull(targetDateControl);
        }

        /**
         * The entry that was dragged.
         *
         * @return the dragged entry
         */
        public Entry<?> getEntry() {
            return entry;
        }

        /**
         * The calendar the entry belonged to when the drag started. May be null.
         *
         * @return the original calendar of the entry
         */
        public Calendar getSourceCalendar() {
            return sourceCalendar;
        }

        /**
         * The date control where the drag gesture started.
         *
         * @return the source date control
         */
        public DateControl getSourceDateControl() {
            return sourceDateControl;
        }

        /**
         * The date control where the entry was dropped.
         *
         * @return the target date control
         */
        public DateControl getTargetDateControl() {
            return targetDateControl;
        }

        @Override
        public String toString() {
            return "EntryDropParameter{" +
                    "entry=" + entry +
                    ", sourceCalendar=" + sourceCalendar +
                    ", sourceDateControl=" + sourceDateControl +
                    ", targetDateControl=" + targetDateControl +
                    '}';
        }
    }

    private final ObjectProperty<Callback<EntryDropParameter, Calendar>> entryDropCalendarProvider = new SimpleObjectProperty<>(this, "entryDropCalendarProvider", param -> {
        Callback<DateControl, Calendar> defaultCalendarProvider = param.getTargetDateControl().getDefaultCalendarProvider();
        if (defaultCalendarProvider == null) {
            return null;
        }
        return defaultCalendarProvider.call(param.getTargetDateControl());
    });

    /**
     * A callback used to determine the calendar that an entry shall be moved to when the
     * user drags it from one date control into another one, for example from one resource
     * to another inside a {@link ResourcesView}. Returning null rejects the drop, in which
     * case the entry is left completely untouched.
     *
     * <p>
     * The default implementation delegates to the {@link #defaultCalendarProviderProperty()}
     * of the control the entry was dropped on. Because the callback dispatches on
     * {@link EntryDropParameter#getTargetDateControl()} it is resource-agnostic, so a single
     * instance can be shared by all views of a composite control.
     *
     * @return the entry drop calendar provider callback
     */
    public final ObjectProperty<Callback<EntryDropParameter, Calendar>> entryDropCalendarProviderProperty() {
        return entryDropCalendarProvider;
    }

    /**
     * Returns the value of {@link #entryDropCalendarProviderProperty()}.
     *
     * @return the entry drop calendar provider
     */
    public final Callback<EntryDropParameter, Calendar> getEntryDropCalendarProvider() {
        return entryDropCalendarProviderProperty().get();
    }

    /**
     * Sets the value of {@link #entryDropCalendarProviderProperty()}.
     *
     * @param provider the entry drop calendar provider
     */
    public final void setEntryDropCalendarProvider(Callback<EntryDropParameter, Calendar> provider) {
        entryDropCalendarProviderProperty().set(provider);
    }
```

- [ ] **Step 2: Bind and unbind the new property**

In `DateControl.bind(DateControl otherControl, boolean bindDate)`, add a line directly after the existing `defaultCalendarProviderProperty()` binding (around line 2909):

```java
        Bindings.bindBidirectional(otherControl.entryDropCalendarProviderProperty(), entryDropCalendarProviderProperty());
```

In `DateControl.unbind(DateControl otherControl)`, add the matching line directly after the existing `defaultCalendarProviderProperty()` un-binding (around line 2972):

```java
        Bindings.unbindBidirectional(otherControl.entryDropCalendarProviderProperty(), entryDropCalendarProviderProperty());
```

- [ ] **Step 3: Compile**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -q -pl CalendarFXView compile`
Expected: BUILD SUCCESS. If `Objects` or `ResourcesView` cannot be resolved, note that `DateControl.java` already imports `java.util.Objects` (used by `EntryEditParameter`) and `ResourcesView` is in the same package — no new imports should be required.

- [ ] **Step 4: Run the full module test suite**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -pl CalendarFXView test`
Expected: all tests pass, no new failures.

- [ ] **Step 5: Stage (do not commit)**

```bash
git add CalendarFXView/src/main/java/com/calendarfx/view/DateControl.java
```

---

### Task 3: `DayViewBase.crossViewDragEnabled`

**Files:**
- Modify: `CalendarFXView/src/main/java/com/calendarfx/view/DayViewBase.java`

**Interfaces:**
- Consumes: nothing.
- Produces: `DayViewBase.crossViewDragEnabledProperty()` / `isCrossViewDragEnabled()` / `setCrossViewDragEnabled(boolean)`, default `false`. Tasks 4 and 5 use these.

- [ ] **Step 1: Add the property**

In `DayViewBase.java`, insert directly **after** the `setEnableStartAndEndTimesFlip(...)` method (which ends around line 1093, immediately before the `/**\n     * Invokes {@link DateControl#bind(DateControl, boolean)}` Javadoc):

```java
    private final BooleanProperty crossViewDragEnabled = new SimpleBooleanProperty(this, "crossViewDragEnabled", false);

    public final boolean isCrossViewDragEnabled() {
        return crossViewDragEnabled.get();
    }

    /**
     * Determines whether the user can drag an entry out of this view and into a sibling
     * view that also has this flag set, for example from one resource to another inside
     * a {@link ResourcesView}. The entry is then moved to the calendar returned by
     * {@link DateControl#entryDropCalendarProviderProperty()}.
     *
     * <p>
     * This property is deliberately not propagated by {@link #bind(DayViewBase, boolean)}.
     * A {@link WeekDayView} must never become a drag target, because the drag gesture
     * inside a {@link WeekView} is owned by the week view itself and not by the
     * individual day views.
     *
     * @return whether entries can be dragged from this view into a sibling view
     */
    public final BooleanProperty crossViewDragEnabledProperty() {
        return crossViewDragEnabled;
    }

    public final void setCrossViewDragEnabled(boolean crossViewDragEnabled) {
        this.crossViewDragEnabled.set(crossViewDragEnabled);
    }
```

**Do not** add this property to `bind(DayViewBase, boolean)` or `unbind(DayViewBase)`. That exclusion is load-bearing: `WeekViewSkin.buildDays()` calls `getSkinnable().bind(weekDayView, false)`, so anything added there would propagate into every `WeekDayView`, and `DayViewSkin:169` deliberately skips installing a `DayViewEditController` on those.

- [ ] **Step 2: Add the property sheet item**

In `DayViewBase.getPropertySheetItems()`, add this block directly after the closing `});` of the first item (the "Enable Scrolling" item, around line 1197):

```java
        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(crossViewDragEnabledProperty());
            }

            @Override
            public void setValue(Object value) {
                setCrossViewDragEnabled((boolean) value);
            }

            @Override
            public Object getValue() {
                return isCrossViewDragEnabled();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Cross view dragging";
            }

            @Override
            public String getDescription() {
                return "Allow entries to be dragged into a sibling view.";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });
```

- [ ] **Step 3: Compile and test**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -pl CalendarFXView test`
Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 4: Verify the exclusion from bind/unbind**

Run: `grep -n "crossViewDragEnabledProperty" CalendarFXView/src/main/java/com/calendarfx/view/DayViewBase.java`
Expected: exactly two lines — the property accessor declaration and the `Optional.of(...)` inside the property sheet item. **Neither may sit inside `bind(` or `unbind(`.** If a third line appears inside either of those methods, delete it: propagating this flag would turn every `WeekDayView` into a drag target.

- [ ] **Step 5: Stage (do not commit)**

```bash
git add CalendarFXView/src/main/java/com/calendarfx/view/DayViewBase.java
```

---

### Task 4: Drag hand-over in `DayViewEditController`

The core of the feature.

**Files:**
- Modify: `CalendarFXView/src/main/java/impl/com/calendarfx/view/DayViewEditController.java`

**Interfaces:**
- Consumes: `EntryDropSupport.applyDrop(...)` and `EntryDropSupport.DropResult` from Task 1; `DateControl.EntryDropParameter` and `getEntryDropCalendarProvider()` from Task 2; `DayViewBase.isCrossViewDragEnabled()` from Task 3.
- Produces: no new public API.

- [ ] **Step 1: Add the imports**

Add to the import block of `DayViewEditController.java`:

```java
import com.calendarfx.view.DateControl.EntryDropParameter;
import impl.com.calendarfx.view.EntryDropSupport.DropResult;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
```

`Calendar`, `Entry`, `Interval`, `DayViewBase`, `DraggedEntry`, `Callback` and `ZonedDateTime` are already imported.

- [ ] **Step 2: Add the two new fields and initialise the host**

Directly after the existing field `private Duration entryDuration;` add:

```java
    private DayViewBase dragHostView;
    private List<DayViewBase> dragCandidates = Collections.emptyList();
```

In the constructor, directly after `this.view = Objects.requireNonNull(dayView);` add:

```java
        this.dragHostView = this.view;
```

This initialisation is load-bearing: the `CREATE_ENTRY` drag path reaches `fixTimeIfOutsideView()`, which dereferences `dragHostView`, without ever going through `mousePressedEditEntry()`.

- [ ] **Step 3: Add the candidate collection and hit-testing helpers**

Add these four private methods at the end of the class, directly before the `lassoStart` property declaration:

```java
    private void collectDragCandidates() {
        dragHostView = view;
        dragCandidates = Collections.emptyList();

        if (!view.isCrossViewDragEnabled()) {
            return;
        }

        Parent root = view.getScene() != null ? view.getScene().getRoot() : null;

        if (root == null) {
            Parent parent = view.getParent();
            while (parent != null && parent.getParent() != null) {
                parent = parent.getParent();
            }
            root = parent;
        }

        if (root == null) {
            return;
        }

        List<DayViewBase> candidates = new ArrayList<>();
        collectDragCandidates(root, candidates);
        dragCandidates = candidates;
    }

    private void collectDragCandidates(Node node, List<DayViewBase> candidates) {
        if (node instanceof DayViewBase dayViewBase && dayViewBase.isCrossViewDragEnabled()) {
            candidates.add(dayViewBase);

            /*
             * Never descend into a candidate. If a view opted in then it owns the drag
             * gesture, and any nested day view below it does not.
             */
            return;
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectDragCandidates(child, candidates);
            }
        }
    }

    private DayViewBase findDragTargetAt(double screenX, double screenY) {
        for (DayViewBase candidate : dragCandidates) {
            if (!candidate.isVisible()) {
                continue;
            }

            Bounds bounds = candidate.localToScreen(candidate.getBoundsInLocal());
            if (bounds != null && bounds.contains(screenX, screenY)) {
                return candidate;
            }
        }

        return null;
    }

    private void updateDragHost(MouseEvent evt) {
        if (dragCandidates.isEmpty()) {
            return;
        }

        DayViewBase target = findDragTargetAt(evt.getScreenX(), evt.getScreenY());

        /*
         * Keep the current host when the cursor is over a separator or outside the
         * window. Reverting would make the preview flicker while crossing the gaps
         * between the columns.
         */
        if (target == null || target == dragHostView) {
            return;
        }

        DraggedEntry draggedEntry = dragHostView.getDraggedEntry();
        if (draggedEntry == null) {
            return;
        }

        LOGGER.fine("handing dragged entry over to another view");

        dragHostView.setDraggedEntry(null);
        dragHostView = target;
        dragHostView.setDraggedEntry(draggedEntry);
    }

    private Instant getInstantAtHost(MouseEvent evt) {
        Point2D p = dragHostView.screenToLocal(evt.getScreenX(), evt.getScreenY());
        if (p == null) {
            return view.getInstantAt(evt);
        }
        return dragHostView.getInstantAt(p.getX(), p.getY());
    }
```

- [ ] **Step 4: Collect the candidates when a drag starts**

In `mousePressedEditEntry(MouseEvent evt)`, replace the final statement:

```java
            entryEditingAllowed = true;
```

with:

```java
            entryEditingAllowed = true;

            if (dragMode == DragMode.START_AND_END_TIME) {
                collectDragCandidates();
            } else {
                dragHostView = view;
                dragCandidates = Collections.emptyList();
            }
```

Hand-over is restricted to `START_AND_END_TIME` on purpose: resizing an entry's start or end handle into another resource has no meaning.

- [ ] **Step 5: Use the host when creating and moving the preview**

In `mouseDraggedEditEntry(MouseEvent evt)`, replace:

```java
            if (view.getDraggedEntry() == null) {
                DraggedEntry draggedEntry = new DraggedEntry(entry, dragMode);
                draggedEntry.setOffsetDuration(offsetDuration);
                view.setDraggedEntry(draggedEntry);
```

with:

```java
            if (dragHostView.getDraggedEntry() == null) {
                DraggedEntry draggedEntry = new DraggedEntry(entry, dragMode);
                draggedEntry.setOffsetDuration(offsetDuration);
                dragHostView.setDraggedEntry(draggedEntry);
```

Then, in the same method, replace:

```java
                case START_AND_END_TIME:
                    changeStartAndEndTime(evt);
                    break;
```

with:

```java
                case START_AND_END_TIME:
                    updateDragHost(evt);
                    changeStartAndEndTime(evt);
                    break;
```

- [ ] **Step 6: Compute the time against the host**

Replace the whole `changeStartAndEndTime(MouseEvent evt)` method with:

```java
    private void changeStartAndEndTime(MouseEvent evt) {
        DraggedEntry draggedEntry = dragHostView.getDraggedEntry();

        Instant locationTime = fixTimeIfOutsideView(evt, getInstantAtHost(evt));

        LOGGER.fine("changing start/end time, time = " + locationTime + " offset duration = " + offsetDuration);

        if (locationTime != null && offsetDuration != null) {

            Instant newStartTime = locationTime.minus(offsetDuration);
            LOGGER.fine("new start time = " + newStartTime);

            newStartTime = snapToGrid(newStartTime, dragHostView.getVirtualGrid(), true, dragHostView);
            Instant newEndTime = newStartTime.plus(entryDuration);

            LOGGER.fine("new start time (grid) = " + newStartTime);
            LOGGER.fine("new end time = " + newEndTime);

            ZonedDateTime gridStartZonedTime = ZonedDateTime.ofInstant(newStartTime, draggedEntry.getZoneId());
            ZonedDateTime gridEndZonedTime = ZonedDateTime.ofInstant(newEndTime, draggedEntry.getZoneId());

            LocalDate startDate = gridStartZonedTime.toLocalDate();
            LocalTime startTime = gridStartZonedTime.toLocalTime();

            LocalDate endDate = LocalDateTime.of(startDate, startTime).plus(entryDuration).toLocalDate();
            LocalTime endTime = gridEndZonedTime.toLocalTime();

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);
        }
    }
```

Feeding host-local coordinates into `dragHostView.getInstantAt(...)` is what makes `WeekView.getZonedDateTimeAt()` (WeekView.java:93, which resolves the day from `x`) pick the right day inside the target resource's week, and it is also what produces cross-day dragging in the `DATES_OVER_RESOURCES` layout.

- [ ] **Step 7: Add the `snapToGrid` overload**

Replace the existing `snapToGrid(Instant time, VirtualGrid grid, boolean checkCloser)` method with:

```java
    private Instant snapToGrid(Instant time, VirtualGrid grid,
                               boolean checkCloser) {
        return snapToGrid(time, grid, checkCloser, view);
    }

    private Instant snapToGrid(Instant time, VirtualGrid grid,
                               boolean checkCloser, DayViewBase referenceView) {
        if (grid == null) {
            return time;
        }

        DayOfWeek firstDayOfWeek = referenceView.getFirstDayOfWeek();
        Instant lowerTime = grid.adjustTime(time, referenceView.getZoneId(), false, firstDayOfWeek);

        if (checkCloser) {
            Instant upperTime = grid.adjustTime(time, referenceView.getZoneId(), true, firstDayOfWeek);
            if (Duration.between(time, upperTime).abs().minus(Duration.between(time, lowerTime).abs()).isNegative()) {
                return upperTime;
            }
        }

        return lowerTime;
    }
```

The three-argument version keeps its existing callers (`changeStartTime`, `changeEndTime`, `mousePressedEditAvailability`, `mouseDraggedEditAvailability`, `createEntryAt`) on `view`, exactly as today.

- [ ] **Step 8: Narrow the out-of-view clamp**

Replace the whole `fixTimeIfOutsideView(MouseEvent evt, Instant gridTime)` method with:

```java
    private Instant fixTimeIfOutsideView(MouseEvent evt, Instant gridTime) {
        /*
         * Fix the time calculation if the mouse cursor exits the view area.
         * Note: the view can also be a WeekView as it extends DayViewBase.
         *
         * While the cursor is inside a legitimate drag host we must not clamp,
         * otherwise the entry could never be dragged into another view.
         */
        Point2D p = dragHostView.screenToLocal(evt.getScreenX(), evt.getScreenY());
        boolean insideHost = p != null && p.getX() >= 0 && p.getX() <= dragHostView.getWidth();

        if (!insideHost) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(gridTime, entry.getZoneId());
            gridTime = ZonedDateTime.of(entry.getStartDate(), zdt.toLocalTime(), zdt.getZone()).toInstant();
        }

        return gridTime;
    }
```

For every control that does not opt in, `dragHostView` is always `view`, so this is equivalent to today's `evt.getX() > view.getWidth() || evt.getX() < 0` check.

- [ ] **Step 9: Apply the drop on release**

Replace the whole `mouseReleasedEditEntry()` method with:

```java
    private void mouseReleasedEditEntry() {
        if (dayEntryView != null) {
            dayEntryView.getProperties().put("dragged", false);
            dayEntryView.getProperties().put("dragged-start", false);
            dayEntryView.getProperties().put("dragged-end", false);
        }

        DayViewBase hostView = dragHostView != null ? dragHostView : view;

        DraggedEntry draggedEntry = hostView.getDraggedEntry();

        if (draggedEntry != null) {
            hostView.setDraggedEntry(null);

            Interval newInterval = draggedEntry.getInterval();

            if (hostView == view) {
                entry.setInterval(newInterval);
            } else {
                Calendar targetCalendar = null;

                Callback<EntryDropParameter, Calendar> provider = view.getEntryDropCalendarProvider();
                if (provider != null) {
                    targetCalendar = provider.call(new EntryDropParameter(entry, entry.getCalendar(), view, hostView));
                }

                DropResult result = EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

                if (result != DropResult.APPLIED) {
                    LOGGER.fine("cross view drop was rejected: " + result);
                }
            }

            if (view.isShowDetailsUponEntryCreation() && operation.equals(Operation.CREATE_ENTRY)) {
                view.fireEvent(new RequestEvent(view, view, entry));
            }
        }

        dragHostView = view;
        dragCandidates = Collections.emptyList();
    }
```

Note that this drops the commented-out recurrence block that currently sits inside this method. That block is dead code and is deliberately not carried over.

- [ ] **Step 10: Compile and run the tests**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -pl CalendarFXView test`
Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 11: Verify no stale references to the old single-view assumption**

Run: `grep -n "view.getDraggedEntry()\|view.setDraggedEntry" CalendarFXView/src/main/java/impl/com/calendarfx/view/DayViewEditController.java`
Expected: only occurrences prefixed with `dragHostView.` or `hostView.`, **except** the two inside `mouseDraggedCreateEntry()` and `mouseReleasedCreateEntry()`, which legitimately stay on `view` because entry creation never hands over.

- [ ] **Step 12: Stage (do not commit)**

```bash
git add CalendarFXView/src/main/java/impl/com/calendarfx/view/DayViewEditController.java
```

---

### Task 5: Wire up `ResourcesView`

**Files:**
- Modify: `CalendarFXView/src/main/java/com/calendarfx/view/ResourcesView.java`
- Modify: `CalendarFXView/src/main/java/impl/com/calendarfx/view/ResourcesViewContainerSkin.java`

**Interfaces:**
- Consumes: `DayViewBase.setCrossViewDragEnabled(boolean)` from Task 3.
- Produces: `ResourcesView.enableCrossResourceDraggingProperty()` / `isEnableCrossResourceDragging()` / `setEnableCrossResourceDragging(boolean)`, default `true`.

- [ ] **Step 1: Add the property to `ResourcesView`**

In `ResourcesView.java`, insert directly **after** the `setShowAllDayView(boolean show)` method (which ends just before the `// show timescale view support` comment, around line 308):

```java
    // cross resource dragging support

    private final BooleanProperty enableCrossResourceDragging = new SimpleBooleanProperty(this, "enableCrossResourceDragging", true);

    /**
     * A property used to control whether the user can drag an entry from one resource to
     * another. The calendar that the entry ends up in is determined by the callback stored
     * in {@link DateControl#entryDropCalendarProviderProperty()}, which by default returns
     * the first calendar of the target resource.
     *
     * @return true if entries can be dragged from one resource to another
     */
    public final BooleanProperty enableCrossResourceDraggingProperty() {
        return enableCrossResourceDragging;
    }

    /**
     * Returns the value of {@link #enableCrossResourceDraggingProperty()}.
     *
     * @return true if entries can be dragged from one resource to another
     */
    public final boolean isEnableCrossResourceDragging() {
        return enableCrossResourceDraggingProperty().get();
    }

    /**
     * Sets the value of {@link #enableCrossResourceDraggingProperty()}.
     *
     * @param enable true if entries can be dragged from one resource to another
     */
    public final void setEnableCrossResourceDragging(boolean enable) {
        enableCrossResourceDraggingProperty().set(enable);
    }
```

- [ ] **Step 2: Rebuild the child views when the switch is toggled**

In `ResourcesViewContainerSkin`'s constructor, add one line directly after `resourcesView.typeProperty().addListener(updateViewListener);`:

```java
        resourcesView.enableCrossResourceDraggingProperty().addListener(updateViewListener);
```

- [ ] **Step 3: Turn the flag on for each per-resource view**

In `ResourcesViewContainerSkin.updateViewDatesOverResources()`, add directly after the existing line `dayView.setEnableCurrentTimeCircle(dayIndex == 0 && resourceIndex == 0);`:

```java
                dayView.setCrossViewDragEnabled(resourcesView.isEnableCrossResourceDragging());
```

In `ResourcesViewContainerSkin.updateViewResourcesOverDates()`, add directly after the existing line `weekView.setEnableCurrentTimeMarker(true);`:

```java
            weekView.setCrossViewDragEnabled(resourcesView.isEnableCrossResourceDragging());
```

**Do not** set the flag on `ResourcesViewContainer` itself. It is a `DayViewBase` whose bounds span every column, so as a candidate it would swallow every hit-test.

**Do not** add `entryDropCalendarProvider` to the list of properties that this skin un-binds. Unlike `defaultCalendarProvider`, which captures a different `resource` per view, the drop provider dispatches on `param.getTargetDateControl()` and is therefore resource-agnostic. Keeping it bound is what lets an application configure one resolver on the `ResourcesView` and have every resource view consult it.

- [ ] **Step 4: Compile and run the tests**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -pl CalendarFXView test`
Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 5: Verify the container itself was not opted in**

Run: `grep -n "setCrossViewDragEnabled" CalendarFXView/src/main/java/impl/com/calendarfx/view/ResourcesViewContainerSkin.java`
Expected: exactly two lines — one on `dayView`, one on `weekView`. Neither may be on `resourcesViewContainer`, `container` or `resourcesView`.

- [ ] **Step 6: Stage (do not commit)**

```bash
git add CalendarFXView/src/main/java/com/calendarfx/view/ResourcesView.java \
        CalendarFXView/src/main/java/impl/com/calendarfx/view/ResourcesViewContainerSkin.java
```

---

### Task 6: Manual verification

The drag gesture itself is not reachable by an automated test in this module, so this task is the real acceptance gate.

**Files:** none modified.

- [ ] **Step 1: Install the library so the demo apps resolve it**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -q install -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 2: Launch the resource demo**

```bash
cd CalendarFXResourceApp && mvn javafx:run
```

- [ ] **Step 3: Walk the checklist**

Confirm each of the following, in both `ResourcesView.Type.RESOURCES_OVER_DATES` and `Type.DATES_OVER_RESOURCES`:

1. Grab an entry in the middle and drag it sideways into another resource column. The preview follows the cursor into the target column.
2. While the preview is in the target column, confirm that **no ghost preview is left behind** in the source column. `DayViewSkin.addOrRemoveDraggedEntryView()` removes the preview when `draggedEntryProperty` is set to `null`, and the hand-over relies on that; a leftover entry view here means the removal path does not handle re-adding the same `DraggedEntry` instance to a second view.
3. Release it there. The entry stays in the target column and disappears from the source column.
4. In `DATES_OVER_RESOURCES` with `numberOfDays > 1`, drag an entry from one day to another. This did not work before and must work now.
5. Drag an entry and release while the cursor sits exactly over a separator between two columns. The entry lands in the last column the cursor was actually inside, and nothing is lost.
6. Drag an entry and release with the cursor outside the application window. The entry keeps its original date, as before.
7. Grab an entry by its top or bottom edge (resize) and drag sideways. The resize stays inside the source column and does not hand over.
8. Set `setEnableCrossResourceDragging(false)` in the demo, relaunch, repeat step 1. The entry stays confined to its column, exactly like before this change.
9. Mark one resource's calendar read-only via `calendar.setReadOnly(true)` and drag an entry onto it. Nothing changes — neither the time nor the resource.
10. Set a custom `setEntryDropCalendarProvider(...)` once on the `ResourcesView` and confirm it is invoked for drops onto **every** resource, not just one.

- [ ] **Step 4: Repeat the two core checks in the sampler**

```bash
cd CalendarFXSampler && mvn javafx:run
```

Open the `HelloResourcesView` page and repeat checklist items 1, 2 and 3. The new "Cross view dragging" entry must be visible in the property sheet.

---

### Task 7: Documentation

**Files:**
- Modify: `CHANGES.txt`
- Modify: `CalendarFXView/src/main/asciidoc/manual.adoc`

- [ ] **Step 1: Add the release note**

Insert at the very top of `CHANGES.txt`, above the existing `RELEASE NOTES, VERSION 11.12.x` block:

```
-------------------------------------------------------------------------------
RELEASE NOTES, VERSION 12.1.x
-------------------------------------------------------------------------------

*** NEW FEATURES

ResourcesView
-------------

Calendar entries can now be dragged horizontally from one resource to another.
The preview follows the mouse cursor into the target column and the entry is
moved to a calendar of the target resource upon release. The calendar is
determined by a new callback, DateControl.setEntryDropCalendarProvider, which
by default returns the first calendar of the target resource. The feature can
be switched off via ResourcesView.setEnableCrossResourceDragging(false).

As a side effect entries can now also be dragged from one date to another when
the view type is set to DATES_OVER_RESOURCES, which was not possible before.

DayViewBase
-----------

A new property called "crossViewDragEnabled" was added. It controls whether
entries can be dragged out of a view and into a sibling view that also has this
flag set. The default value is false, so existing controls are unaffected.
```

- [ ] **Step 2: Document the feature in the manual**

In `CalendarFXView/src/main/asciidoc/manual.adoc`, insert the following directly **after** the paragraph ending with `...calendar entries become semi-transparent.` and its `image::resources-view-availability.png[...]` block, and **before** the `== Developer Console` heading:

```asciidoc
==== Dragging Entries Between Resources

By default the user can drag a calendar entry horizontally from one resource to another. While the mouse cursor moves across the
column boundary the drag preview follows it into the target column. Upon release the entry is moved to a calendar of the target
resource.

The calendar that receives the entry is determined by the callback stored in `DateControl.entryDropCalendarProviderProperty()`. The
default implementation returns the first calendar of the target resource. An application that keeps several calendars per resource can
replace it. The callback only has to be set once, on the `ResourcesView` itself, as it receives the target control as part of its
parameter object.

[source,java]
----
resourcesView.setEntryDropCalendarProvider(param -> {
    Calendar sourceCalendar = param.getSourceCalendar();
    DateControl target = param.getTargetDateControl();
    // pick a calendar of the target resource, or return null to reject the drop
    return target.getCalendars().stream()
            .filter(calendar -> calendar.getName().equals(sourceCalendar.getName()))
            .findFirst()
            .orElse(null);
});
----

Returning `null`, or returning a calendar that is read-only, rejects the drop. A rejected drop leaves the entry completely untouched:
neither its time nor its resource changes.

The feature can be switched off entirely:

[source,java]
----
resourcesView.setEnableCrossResourceDragging(false);
----

Note that resizing an entry by dragging its top or bottom edge never crosses a column boundary, as changing the start or end time of an
entry in a different resource has no meaning.
```

- [ ] **Step 3: Verify the manual still renders**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -q -pl CalendarFXView install -DskipTests`
Expected: BUILD SUCCESS, and `CalendarFXView/target/generated-docs/manual.html` contains the new section.

Run: `grep -c "Dragging Entries Between Resources" CalendarFXView/target/generated-docs/manual.html`
Expected: at least `1`.

- [ ] **Step 4: Stage (do not commit)**

```bash
git add CHANGES.txt CalendarFXView/src/main/asciidoc/manual.adoc
```

- [ ] **Step 5: Final full build**

Run: `MAVEN_OPTS="-Duser.language=en -Duser.country=US" ./mvnw -B verify`
Expected: BUILD SUCCESS across all modules.

- [ ] **Step 6: Hand over to the repository owner**

Run: `git status` and `git --no-pager diff --staged --stat`, then report the staged change set. **Do not commit.**
