# CalendarFX
A Java framework for creating sophisticated calendar views based on JavaFX. A detailed developer manual can be found online: [CalendarFX 8 Developer Manual](https://dlsc.com/wp-content/html/calendarfx/manual.html)

[![Apache-2 license](https://img.shields.io/badge/license-Apache--2-%230778B9.svg)](https://opensource.org/licenses/Apache-2.0) 
[![Build Status](https://travis-ci.com/dlsc-software-consulting-gmbh/CalendarFX.svg?branch=master-11)](https://travis-ci.com/dlsc-software-consulting-gmbh/CalendarFX)
[![Maven Central](https://img.shields.io/maven-central/v/com.calendarfx/view)](https://search.maven.org/search?q=g:com.calendarfx+AND+a:view) 
[![Download](https://api.bintray.com/packages/dlsc-oss/repository/CalendarFX/images/download.svg)](https://bintray.com/dlsc-oss/repository/CalendarFX)
[![LGTM Alerts](https://img.shields.io/lgtm/alerts/github/dlsc-software-consulting-gmbh/CalendarFX)](https://lgtm.com/projects/g/dlsc-software-consulting-gmbh/CalendarFX/alerts)
[![LGTM Grade](https://img.shields.io/lgtm/grade/java/github/dlsc-software-consulting-gmbh/CalendarFX)](https://lgtm.com/projects/g/dlsc-software-consulting-gmbh/CalendrFX/context:java)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=bugs)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=code_smells)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=ncloc)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=alert_status)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=security_rating)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=sqale_index)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=dlsc-software-consulting-gmbh_CalendarFX2&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=dlsc-software-consulting-gmbh_afterburner.fx)

For a quick online demo please checkout [JPro](https://jpro.one) and their [CalendarFX demo](https://demos.jpro.one/calendar.html).

![Screenshot](screenshot.png "Screenshot")

# Repository Coordinates
CalendarFX can be found on [Bintray](https://bintray.com/dlsc-oss/repository/CalendarFX) and [The Central Repository](https://search.maven.org/search?q=g:com.calendarfx+AND+a:view) as `com.calendarfx:view`.

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
