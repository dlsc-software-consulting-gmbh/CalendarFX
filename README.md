# CalendarFX
A Java framework for creating sophisticated calendar views based on JavaFX. A detailed developer manual can be found online: [CalendarFX Developer Manual](https://dlsc-software-consulting-gmbh.github.io/CalendarFX/)

[![JFXCentral](https://img.shields.io/badge/Find_me_on-JFXCentral-blue?logo=googlechrome&logoColor=white)](https://www.jfx-central.com/libraries/calendarfx)

[![Apache-2 license](https://img.shields.io/badge/license-Apache--2-%230778B9.svg)](https://opensource.org/licenses/Apache-2.0) 
[![Maven Central](https://img.shields.io/maven-central/v/com.calendarfx/view)](https://search.maven.org/search?q=g:com.calendarfx+AND+a:view) 
[![LGTM Alerts](https://img.shields.io/lgtm/alerts/github/dlsc-software-consulting-gmbh/CalendarFX)](https://lgtm.com/projects/g/dlsc-software-consulting-gmbh/CalendarFX/alerts)
[![LGTM Grade](https://img.shields.io/lgtm/grade/java/github/dlsc-software-consulting-gmbh/CalendarFX)](https://lgtm.com/projects/g/dlsc-software-consulting-gmbh/CalendarFX/context:java)

[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=code_smells)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_CalendarFX2.fx)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=ncloc)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_CalendarFX2.fx)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_CalendarFX2.fx)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=security_rating)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_CalendarFX2.fx)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_CalendarFX2.fx)

For a quick online demo please checkout [JPro](https://jpro.one) and their [CalendarFX demo](https://demos.jpro.one/calendar.html).

![Screenshot](screenshot.png "Screenshot")

# Repository Coordinates
CalendarFX can be found on [The Central Repository](https://search.maven.org/search?q=g:com.calendarfx+AND+a:view) as `com.calendarfx:view`.

# Modules

* CalendarFXView — the main module containing the various calendar views
* CalendarFXSampler — a demo app based on FXSampler to test controls individually
* CalendarFXApp — a demo app (day, week, month, year views).
* CalendarFXiCal — a demo app for working with iCalendar data
* CalendarFXGoogle — a demo app for working with Google calendars
* CalendarFXResourceApp — a demo app for the resource calendar view
* CalendarFXWeather — a demo app for the month sheet view

# Running
In the module folder of the corresponding app:
```bash
mvn javafx:run
```

# Building
To install the package into the local repository, for use as a dependency in other projects locally:
```bash
mvn install
```


