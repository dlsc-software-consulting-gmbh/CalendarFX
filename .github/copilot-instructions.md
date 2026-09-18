# CalendarFX — Copilot Instructions

JavaFX calendar control library (`com.calendarfx:view`), Java 21 + JavaFX 23, multi-module Maven build.

## Build & Test

The Maven wrapper is used everywhere (CI runs `./mvnw -B verify`).

```bash
./mvnw verify                 # full build, all modules
./mvnw install                # install into local repo (needed by the demo apps)
./mvnw -pl CalendarFXView test          # tests of the library module only
./mvnw -pl CalendarFXView test -Dtest=EntryTest              # single test class
./mvnw -pl CalendarFXView test -Dtest=EntryTest#shouldNotHaveStyles   # single test method
```

Running a demo app — from the module folder:

```bash
cd CalendarFXApp && mvn javafx:run
```

Every demo module declares its own `mainClass` in its `javafx-maven-plugin` configuration
(e.g. `com.calendarfx.app.CalendarApp`, `com.calendarfx.demo.CalendarFXSampler`).

There is no linter. Code formatting follows `formatter-settings.xml` (IntelliJ/Eclipse import).

### Module layout of the build

The root POM only contains `CalendarFXView`. All demo modules (`CalendarFXApp`, `CalendarFXSampler`,
`CalendarFXGoogle`, `CalendarFXiCal`, `CalendarFXResourceApp`, `CalendarFXSchedulerApp`,
`CalendarFXWeather`) live in the `all-modules` profile, which is active unless `-Drelease=true`
is passed. Releases therefore publish only the library.

## Architecture

**Model (`com.calendarfx.model`)** — `CalendarSource` → `Calendar` → `Entry<T>`.
`Entry` carries an `Interval` (dates, times, zone id) plus a user object of type `T`.
Model changes are propagated as `CalendarEvent`s fired by the `Calendar` (`ENTRY_CHANGED`,
`CALENDAR_CHANGED`, …); views listen to these events instead of polling. `Calendar` can
temporarily suppress events during bulk updates. Recurrence is RFC 2445 based (ical4j `Recur`):
an entry with an RRULE acts as the "source entry" and produces detached recurrence copies
(`isRecurrence()`, `getRecurrenceSourceEntry()`); changes to the source are mirrored onto them.
`IntervalTree` provides the interval-based lookup used for loading entries per date range.

**Controls (`com.calendarfx.view`, plus `.page`, `.print`, `.popover`)** — public API.
All controls extend `CalendarFXControl`, which resolves the user agent stylesheet
(`calendar.css`, or `atlantafx.css` when the system property `atlantafx` is `true`) and exposes
`getPropertySheetItems()` for the ControlsFX `PropertySheet` used by the developer console/sampler.

**`DateControl` is the central abstraction.** Almost every view derives from it. Key concept:
**binding**. Composite controls (e.g. `CalendarView`, `DetailedWeekView`) call
`bind(otherDateControl, boolean)` so nested controls inherit date, today, time zone, week fields,
selection, factories and callbacks from their parent. Consequently an application configures only
the outermost control. Customization points are `Callback`s / factories on `DateControl`:
`entryFactory`, `calendarSourceFactory`, `defaultCalendarProvider`, `contextMenuCallback`,
`entryContextMenuCallback`, `entryDetailsCallback`, `dateDetailsCallback`,
`entryDetailsPopOverContentCallback`, `entryEditPolicy`, `alertCallback`. Never instantiate
`Alert` directly — go through `alertCallback`.

**Skins / internals (`impl.com.calendarfx.view.*`)** — every public control has a matching skin
(`DayView` → `DayViewSkin`) returned from `createDefaultSkin()`. This package is exported but is
considered private API and may change. `DataLoader` + `LoadDataSettingsProvider` implement the
date-range-based loading of entries into the views; `DayViewEditController` implements drag/resize
editing.

**Utilities** — `com.calendarfx.util.LoggingDomain` holds the named `java.util.logging` loggers
(`MODEL`, `VIEW`, `EDITING`, `PRINTING`, `PERFORMANCE`, …); use these instead of creating loggers.
`CalendarFXView/logging.properties` configures them for development.

## Conventions

- **JavaFX properties are `final`.** `fooProperty()`, `getFoo()`/`isFoo()` and `setFoo(...)` are all
  declared `final` on controls and observable model classes.
- **Property Javadoc goes on the property accessor only** (`fooProperty()`), not on the getter,
  setter or the field.
- Properties are created with the owner/name constructor:
  `new SimpleObjectProperty<>(this, "foo", defaultValue)`.
- CSS style classes are declared as `private static final String` constants and added in the
  constructor via `getStyleClass().add(DEFAULT_STYLE_CLASS)`; styling lives in
  `CalendarFXView/src/main/resources/com/calendarfx/view/calendar.css` and, for the AtlantaFX
  variant, `atlantafx.css`. Both files must be kept in sync when adding new style classes.
- All user-visible strings go through `Messages.getString(key, args...)` and
  `messages.properties` (translations: `_de`, `_es`, `_fr`, `_it`, `_pt_BR`, `_sk`).
- New public API must be added to `module-info.java` exports and, when it is reflected upon,
  to `opens`.
- Every source file carries the Apache-2.0 DLSC copyright header.
- Tests are JUnit 4 with Hamcrest `assertThat` and follow a `// given / when / then` comment style.
- The developer manual is AsciiDoc: `CalendarFXView/src/main/asciidoc/manual.adoc`, rendered to HTML
  by the asciidoctor plugin during `install`; the published copy lives in `docs/`. Notable feature
  changes are also recorded in `CHANGES.txt`.
