# Cross-Resource Entry Dragging in `ResourcesView`

Date: 2026-09-28
Status: approved, ready for implementation planning

## Problem

`ResourcesView` does not let the user drag a calendar entry horizontally from one
resource to another. An entry can only be moved in time inside the resource column it
already belongs to.

The cause is structural. `ResourcesViewContainerSkin` builds a **separate
`DayViewBase` instance per column**:

- `updateViewDatesOverResources()` creates one `DayView` per (day x resource) cell.
- `updateViewResourcesOverDates()` creates one `WeekView` per resource, spanning
  `numberOfDays` days.

Each of those views gets its own `DayViewEditController` (installed in
`DayViewSkin:174` and `WeekViewSkin:56`), and every controller operates exclusively on
the single view it was constructed with:

- The drag preview is published through `view.setDraggedEntry(...)`, and
  `draggedEntryProperty` is explicitly **un**bound between the container and each child
  view, so the preview cannot appear in a sibling column.
- `mouseReleasedEditEntry()` only calls `entry.setInterval(...)`. It never changes the
  entry's calendar, so reassignment is impossible by construction.
- `fixTimeIfOutsideView()` actively clamps the date back to `entry.getStartDate()` as
  soon as `evt.getX()` leaves the view horizontally.

A consequence of the same structure: in the `DATES_OVER_RESOURCES` layout each day is
its own `DayView`, so even **day-to-day** dragging does not work there. In
`RESOURCES_OVER_DATES` it does, because one `WeekView` spans all days of one resource.

## Goal

Let the user drag an entry across column boundaries in `ResourcesView`, with a live
preview that follows the cursor into the target column, and have the entry reassigned
to a calendar of the target resource on release.

Cross-day dragging in `DATES_OVER_RESOURCES` is an accepted and desirable side effect
of the same mechanism.

## Non-Goals

- **Full-day entries.** `AllDayView` has no drag editing at all today (its skin
  registers no `MOUSE_DRAGGED` or `MOUSE_PRESSED` handlers), so there is nothing to
  extend.
- **Cross-view resizing.** Hand-over applies only to `DragMode.START_AND_END_TIME`.
  Dragging an entry's start or end handle into another resource has no sensible
  meaning; `changeStartTime()` and `changeEndTime()` keep operating on the source view
  exactly as today.
- **Entry creation across views.** The `CREATE_ENTRY` drag path is unchanged.
- **A new `EditOperation` constant.** The existing `MOVE` permission governs the whole
  gesture.

## Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Scope of the mechanism | Generic in `DayViewEditController`, gated by an opt-in flag | The hard part is identical for any composite control; the flag keeps every existing control bit-for-bit unchanged. |
| Target calendar resolution | New `DateControl` callback, defaulting to the target view's `defaultCalendarProvider` | Follows the repo's existing customization pattern; the default is exactly the "always calendar #0" behaviour, so nothing is lost. |
| Veto mechanism | Reuse `EditOperation.MOVE` + a read-only check on the resolved target calendar | Avoids extending a public enum that applications may `switch` over exhaustively. |
| Preview behaviour | Live hand-over of the `DraggedEntry` into the target view | Expected scheduler behaviour, and it falls out of the existing per-view `draggedEntryProperty` listener. |
| Target lookup | Candidate list snapshotted once per gesture | No new public interface and no registry lifecycle; inherently robust against the skin rebuilds that tear down and recreate all child views. |
| Testing | Toolkit-free unit tests of a UI-free `EntryDropSupport` helper, plus manual verification | `DayViewBase` cannot be class-initialized without a JavaFX toolkit, and CI has no display — so automated tests may touch model classes only. |

## Public API

Both additions live in `com.calendarfx.view`, which `module-info.java` already
`exports` and `opens`. **No `module-info.java` change is required.**

### `DayViewBase.crossViewDragEnabled`

```java
private final BooleanProperty crossViewDragEnabled =
        new SimpleBooleanProperty(this, "crossViewDragEnabled", false);

public final BooleanProperty crossViewDragEnabledProperty();
public final boolean isCrossViewDragEnabled();
public final void setCrossViewDragEnabled(boolean enabled);
```

Javadoc goes on `crossViewDragEnabledProperty()` only, per repo convention. A
`getPropertySheetItems()` entry is added under the existing `DAY_VIEW_BASE_CATEGORY`
so the property appears in the sampler's property sheet.

**This property is deliberately excluded from `DayViewBase.bind()` and
`DayViewBase.unbind()`.** `WeekViewSkin.buildDays()` calls
`getSkinnable().bind(weekDayView, false)`, so anything added to `bind()` propagates
into every `WeekDayView`. A `WeekDayView` must never be a drag target, because
`DayViewSkin:169` deliberately skips installing a `DayViewEditController` on it — the
owning `WeekView` handles the gesture. Keeping the flag out of `bind()` makes "has the
flag" and "owns an edit controller" the same set, with no `instanceof WeekDayView`
special-casing anywhere in the implementation.

### `DateControl.entryDropCalendarProvider`

```java
public static final class EntryDropParameter {
    private final Entry<?> entry;
    private final Calendar sourceCalendar;
    private final DateControl sourceDateControl;
    private final DateControl targetDateControl;
    // constructor + getters, shaped like the existing EntryEditParameter
}

private final ObjectProperty<Callback<EntryDropParameter, Calendar>> entryDropCalendarProvider =
        new SimpleObjectProperty<>(this, "entryDropCalendarProvider", param ->
                param.getTargetDateControl().getDefaultCalendarProvider()
                        .call(param.getTargetDateControl()));

public final ObjectProperty<Callback<EntryDropParameter, Calendar>> entryDropCalendarProviderProperty();
public final Callback<EntryDropParameter, Calendar> getEntryDropCalendarProvider();
public final void setEntryDropCalendarProvider(Callback<EntryDropParameter, Calendar> provider);
```

The name mirrors the existing `defaultCalendarProvider`. The default implementation
delegates to the target view's `defaultCalendarProvider`, and
`ResourcesViewContainerSkin` already sets
`dayView.setDefaultCalendarProvider(dateControl -> resource.getCalendars().get(0))` on
every per-resource view — so out of the box a drop lands in the target resource's first
calendar.

The default provider must tolerate a `null` `defaultCalendarProvider` on the target and
return `null` in that case, which the drop logic treats as a rejection.

The property is bound in `DateControl.bind()` and unbound in `DateControl.unbind()`
alongside `defaultCalendarProvider`.

Unlike `defaultCalendarProvider`, this property is **kept bound** in
`ResourcesViewContainerSkin`, so all per-resource views share one instance and an
application configures it once on the `ResourcesView`. That is safe — and necessary —
because the callback is resource-agnostic by construction: it dispatches on
`param.getTargetDateControl()`, which is the per-resource view the entry was dropped on.
`defaultCalendarProvider` must be un-bound precisely because it carries a different
captured `resource` per view; `entryDropCalendarProvider` does not.

### `ResourcesView.enableCrossResourceDragging`

```java
private final BooleanProperty enableCrossResourceDragging =
        new SimpleBooleanProperty(this, "enableCrossResourceDragging", true);
```

Defaults to `true`. `ResourcesViewContainerSkin` reads it when setting
`crossViewDragEnabled` on the child views, and adds it to its existing
`updateViewListener` chain so toggling it rebuilds the views. (`ResourcesViewSkin` does
not need the listener — it builds only the header row, not the day views.) An
application cannot set the child flag directly and have it survive, because the skin
rebuilds all child views whenever resources, type or `numberOfDays` change.

## Implementation

### `DayViewEditController`

Two new fields replace the implicit assumption that `view` hosts the drag:

```java
private DayViewBase dragHostView;              // view currently holding the DraggedEntry
private List<DayViewBase> dragCandidates;      // snapshot, taken once per gesture
```

**Candidate collection (at mouse-press, `mousePressedEditEntry`).** If
`view.isCrossViewDragEnabled()` is `false`, `dragCandidates` is set to an empty list and
the entire feature short-circuits for every existing control. Otherwise: walk up from
`view` to `view.getScene().getRoot()` (falling back to the top-most `Parent` when the
view is not in a scene), then depth-first collect every `DayViewBase` whose
`crossViewDragEnabled` is `true`. One tree walk per gesture, none per mouse event.

`dragHostView` is initialised to `view`.

**Target lookup (per `mouseDragged`, only when `dragMode == START_AND_END_TIME`).**
Hit-test `evt.getScreenX()` / `evt.getScreenY()` against each candidate's
`localToScreen(getBoundsInLocal())`, skipping candidates that are not visible. If no
candidate matches — the cursor is over a separator, the time scale, or outside the
window — the **current host is kept**, not reverted to the source. Reverting would make
the preview flicker back and forth as the cursor crosses the 1px separator regions
between columns.

**Hand-over** when the resolved target differs from the current host:

```java
DraggedEntry draggedEntry = dragHostView.getDraggedEntry();
dragHostView.setDraggedEntry(null);
dragHostView = target;
dragHostView.setDraggedEntry(draggedEntry);
```

`DayViewSkin.addOrRemoveDraggedEntryView()` listens to `draggedEntryProperty` in each
view independently, so the preview relocates with no further work. Note that
`addOrRemoveDraggedEntryView()` attaches a `WeakInvalidationListener` to
`draggedEntry.intervalProperty()` on add and calls `removeEntryView` on remove; the
implementation must confirm that re-adding the same `DraggedEntry` instance to a second
view leaves no stale entry view behind in the first.

**Time computation** inside `changeStartAndEndTime()` moves from `view` to the host, in
host-local coordinates:

```java
Point2D p = dragHostView.screenToLocal(evt.getScreenX(), evt.getScreenY());
Instant locationTime = dragHostView.getInstantAt(p.getX(), p.getY());
```

The same substitution applies to the `getVirtualGrid()`, `getZoneId()` and
`getFirstDayOfWeek()` reads used by `snapToGrid()` along that path. Because
`WeekView.getZonedDateTimeAt()` (WeekView.java:93) resolves the day from `x`, feeding it
host-local coordinates is what picks the correct day *within the target resource's
week*. It is also exactly what produces cross-day dragging in `DATES_OVER_RESOURCES`.

**`fixTimeIfOutsideView()`** is narrowed: it clamps the date only when the cursor lies
outside `dragHostView`'s bounds, and clamps relative to `dragHostView`. While the cursor
is inside a legitimate host, no clamping occurs. This method's current unconditional
horizontal clamp is the check that blocks cross-resource dragging today.

**Release (`mouseReleasedEditEntry`).** The preview is read from `dragHostView` rather
than `view`, and `dragHostView.setDraggedEntry(null)` clears it.

When `dragHostView == view`, behaviour is unchanged: `entry.setInterval(newInterval)`.

When `dragHostView != view`:

1. Resolve the target calendar via the **source** view's provider:
   `view.getEntryDropCalendarProvider().call(new EntryDropParameter(entry, entry.getCalendar(), view, dragHostView))`.
   Source and host share the same provider instance anyway, since the property
   propagates through `bind()`.
2. Delegate the decision and the mutation to
   `EntryDropSupport.applyDrop(entry, newInterval, targetCalendar)` (see Testing). It
   rejects a `null` or read-only target, leaving the entry completely untouched —
   **neither** the calendar **nor** the interval changes. Keeping the time change while
   dropping the reassignment would be a confusing half-outcome.
3. On acceptance `applyDrop` sets the interval **first**, then the calendar. Order
   matters: `Entry.setCalendar()` fires `ENTRY_CALENDAR_CHANGED` on both the old and the
   new calendar (Entry.java:880-885), triggering reloads in both views, so the interval
   must already be final.

Finally `dragHostView` and `dragCandidates` are reset.

### `ResourcesViewContainerSkin`

In `updateViewDatesOverResources()` (on each `DayView`) and
`updateViewResourcesOverDates()` (on each `WeekView`):

- `childView.setCrossViewDragEnabled(resourcesView.isEnableCrossResourceDragging())`.
- `entryDropCalendarProvider` is **not** added to the un-binding list — see the API
  section above. It stays bound so the application configures one resolver on the
  `ResourcesView`.

`ResourcesViewContainer` itself must **not** get `crossViewDragEnabled`. It is a
`DayViewBase` whose bounds span every column, so as a candidate it would swallow every
hit-test. Because the flag is excluded from `bind()`, it cannot reach the container or
any `WeekDayView` by accident.

## Testing

The module has no UI test infrastructure: the four existing tests are plain JUnit 4
model tests requiring no JavaFX toolkit, and there is no TestFX or Monocle dependency.

**Verified empirically:** `DayViewBase` cannot even be *class-initialized* without a
JavaFX toolkit — constructing a `DayView` in a surefire test fails with
`NoClassDefFoundError: Could not initialize class com.calendarfx.view.DayViewBase`.
CI runs `./mvnw -B verify` on `ubuntu-latest` with no display and no xvfb step, so any
test touching a `DateControl` or `DayViewBase` instance would break the build.

Consequently the automated tests may reference **model classes only** (`Entry`,
`Interval`, `Calendar`), exactly like the existing `EntryTest` and `CalendarTest`.

To make that possible, the drop decision is extracted out of `DayViewEditController`
into a new UI-free helper `impl.com.calendarfx.view.EntryDropSupport`, which imports
nothing from `javafx.scene` or `com.calendarfx.view`:

```java
public final class EntryDropSupport {

    public enum DropResult { APPLIED, REJECTED_NO_CALENDAR, REJECTED_READ_ONLY }

    private EntryDropSupport() {
    }

    public static DropResult applyDrop(Entry<?> entry, Interval newInterval, Calendar targetCalendar) {
        // null target        -> REJECTED_NO_CALENDAR, entry untouched
        // read-only target   -> REJECTED_READ_ONLY,   entry untouched
        // otherwise          -> setInterval(newInterval), then setCalendar(targetCalendar), APPLIED
    }
}
```

`DayViewEditController` resolves the target calendar (which needs `DateControl` and is
therefore not unit-testable here) and then delegates the decision and mutation to
`EntryDropSupport.applyDrop(...)`.

**Automated (toolkit-free, JUnit 4 + Hamcrest, `// given / when / then` style),
`CalendarFXView/src/test/java/impl/com/calendarfx/view/EntryDropSupportTest.java`:**

- `applyDrop` with a `null` target calendar returns `REJECTED_NO_CALENDAR` and changes
  neither `entry.getInterval()` nor `entry.getCalendar()`.
- `applyDrop` with a read-only target calendar returns `REJECTED_READ_ONLY` and changes
  neither `entry.getInterval()` nor `entry.getCalendar()`.
- `applyDrop` with a writable target calendar returns `APPLIED`, and afterwards
  `entry.getInterval()` is the new interval and `entry.getCalendar()` is the target.
- The interval is applied before the calendar: a `CalendarEvent.ENTRY_CALENDAR_CHANGED`
  listener registered on the target calendar observes the *new* interval on the entry.

The "default `entryDropCalendarProvider` resolves through the target's
`defaultCalendarProvider`" check named in an earlier draft of this spec is **dropped**
from the automated set — it requires two `DateControl` instances and is unreachable
without a toolkit. It moves to the manual checklist.

**Manual:** exercise both layouts in `CalendarFXResourceApp` and the sampler's
`HelloResourcesView`, covering: drag between resources in both `Type` variants;
drag between days in `DATES_OVER_RESOURCES`; the default resolver landing the entry in
the target resource's first calendar; a custom resolver set once on the `ResourcesView`
being consulted for every resource; drag onto a read-only calendar; release over a
separator; release outside the window; resize handles still confined to their source
view; and `enableCrossResourceDragging = false` restoring today's behaviour.

## Documentation

- `CHANGES.txt` entry.
- A subsection on cross-resource dragging in
  `CalendarFXView/src/main/asciidoc/manual.adoc`, in the `ResourcesView` coverage.
- No CSS changes. The preview reuses the existing `dragged-entry` style class, so
  `calendar.css` and `atlantafx.css` are untouched.
