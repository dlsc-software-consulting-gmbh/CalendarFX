# Contributing to CalendarFX

Thanks for taking the time to contribute! CalendarFX is developed and maintained by
[DLSC Software & Consulting GmbH](https://www.dlsc.com) and is licensed under the
[Apache License 2.0](LICENSE).

## Code of Conduct

This project and everyone participating in it is governed by our
[Code of Conduct](CODE_OF_CONDUCT.md). By participating you are expected to uphold it.

## Getting Started

CalendarFX is a multi-module Maven project requiring **Java 21** and **JavaFX 23**.
The Maven wrapper is committed to the repository, so no local Maven installation is needed.

```bash
git clone https://github.com/dlsc-software-consulting-gmbh/CalendarFX.git
cd CalendarFX
./mvnw verify
```

To install the artifacts into your local repository (required by the demo applications):

```bash
./mvnw install
```

Running a demo application, from inside the module folder:

```bash
cd CalendarFXApp && mvn javafx:run
```

### Module Layout

The root POM only builds `CalendarFXView`, the library itself. All demo modules
(`CalendarFXApp`, `CalendarFXSampler`, `CalendarFXGoogle`, `CalendarFXiCal`,
`CalendarFXResourceApp`, `CalendarFXSchedulerApp`, `CalendarFXWeather`) live in the
`all-modules` profile, which is active unless `-Drelease=true` is passed.

## Reporting Bugs

Before opening a new issue, please search the
[existing issues](https://github.com/dlsc-software-consulting-gmbh/CalendarFX/issues).
When filing a bug report, use the bug report template and include:

* the CalendarFX, Java and JavaFX versions you are using,
* your operating system,
* a **minimal, self-contained, runnable example** that reproduces the problem,
* a screenshot or short screen recording if the issue is visual.

Security vulnerabilities must **not** be reported as issues. Please follow the process
described in [SECURITY.md](SECURITY.md).

## Suggesting Features

Feature requests are welcome. Please open an issue using the feature request template and
describe the use case behind the request, not only the proposed solution. For larger ideas,
consider starting a [discussion](https://github.com/dlsc-software-consulting-gmbh/CalendarFX/discussions)
first, so that the design can be agreed upon before any code is written.

## Pull Requests

1. Fork the repository and create a branch off `master`.
2. Keep the change focused — one logical change per pull request.
3. Make sure `./mvnw verify` passes.
4. Add or update tests for behavioural changes.
5. Update the documentation (`CalendarFXView/src/main/asciidoc/manual.adoc`) and `CHANGES.txt`
   when the change is user visible.
6. Fill out the pull request template and reference the issue the change relates to.

By submitting a pull request you agree that your contribution is licensed under the
Apache License 2.0.

## Coding Conventions

There is no automated linter. Code formatting follows `formatter-settings.xml`, which can be
imported into IntelliJ IDEA and Eclipse. Beyond formatting, the project follows a number of
conventions:

* **JavaFX properties are `final`.** `fooProperty()`, `getFoo()`/`isFoo()` and `setFoo(...)`
  are all declared `final` on controls and observable model classes.
* **Property Javadoc goes on the property accessor only** (`fooProperty()`), never on the
  getter, setter or field.
* Properties are created with the owner/name constructor:
  `new SimpleObjectProperty<>(this, "foo", defaultValue)`.
* CSS style classes are declared as `private static final String` constants and added in the
  constructor via `getStyleClass().add(DEFAULT_STYLE_CLASS)`. Styling lives in
  `CalendarFXView/src/main/resources/com/calendarfx/view/calendar.css` and, for the AtlantaFX
  variant, in `atlantafx.css` — **both files must be kept in sync**.
* All user-visible strings go through `Messages.getString(key, args...)` and
  `messages.properties`. Translations exist for `_de`, `_es`, `_fr`, `_it`, `_pt_BR` and `_sk`.
* New public API must be added to `module-info.java` exports and, when it is reflected upon,
  to `opens`.
* Never instantiate `Alert` directly — go through the `alertCallback` of `DateControl`.
* Use the loggers in `com.calendarfx.util.LoggingDomain` instead of creating new ones.
* Every source file carries the Apache-2.0 DLSC copyright header.

## Testing

Tests are JUnit 4 with Hamcrest `assertThat` and follow a `// given / when / then` style.

```bash
./mvnw -pl CalendarFXView test                                    # all library tests
./mvnw -pl CalendarFXView test -Dtest=EntryTest                   # a single test class
./mvnw -pl CalendarFXView test -Dtest=EntryTest#shouldNotHaveStyles  # a single test method
```

## Questions

For usage questions, please use
[GitHub Discussions](https://github.com/dlsc-software-consulting-gmbh/CalendarFX/discussions)
or Stack Overflow with the `calendarfx` tag rather than the issue tracker.
