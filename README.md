

# CalendarFX
A Java framework for creating sophisticated calendar views based on JavaFX. A detailed developer manual can be found online: http://dlsc.com/wp-content/html/calendarfx/manual.html

[![Apache-2 license](https://img.shields.io/badge/license-Apache--2-%230778B9.svg)](https://opensource.org/licenses/Apache-2.0) 
[![travis-ci Build Status](https://img.shields.io/travis/dlsc-software-consulting-gmbh/CalendarFX)](https://travis-ci.org/dlsc-software-consulting-gmbh/CalendarFX/) 
[![Maven Central](https://img.shields.io/maven-central/v/com.calendarfx/view)](https://search.maven.org/search?q=g:com.calendarfx+AND+a:view) 
[![Download](https://api.bintray.com/packages/dlsc-oss/repository/CalendarFX/images/download.svg) ](https://bintray.com/dlsc-oss/repository/CalendarFX/_latestVersion)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/dlsc-software-consulting-gmbh/CalendarFX.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/dlsc-software-consulting-gmbh/CalendarFX/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/dlsc-software-consulting-gmbh/CalendarFX.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/dlsc-software-consulting-gmbh/CalendrFX/context:java)


For a quick online demo please checkout the [JPRO website](https://jpro.one) and there the [CalendarFX demo](https://demos.jpro.one/calendar.html).

![alt text](screenshot.png "Screenshot")

# Repository Coordinates
CalendarFX can be found on jcenter / bintray and also on maven central. For maven central you simply have to add the dependency to the CalendarFX "view" artifact. For jcenter you also need to add the jcenter repository to your pom file.

```XML
<repositories>
	<repository>
		<id>jcenter</id>
		<url>http://jcenter.bintray.com</url>
	</repository>
</repositories>
    
<dependencies>
	<dependency>
  		<groupId>com.calendarfx</groupId>
  		<artifactId>view</artifactId>
  		<version>11.5.0</version>
	</dependency>
</dependencies>
```

# Modules

1. CalendarFXApp - contains a standalone application (day, week, month, year views).
2. CalendarFXAssembly - a module used for creating distributions / assemblies
3. CalendarFXExperimental - a playground for new controls
4. CalendarFXGoogle - code for working with Google calendars
5. CalendarFXiCal - code for dealing with iCal data
6. CalendarFXSampler - a demo app based on FXSampler to test controls individually
7. CalendarFXView - the main module containing the various calendar views
8. CalendarFXWeather - a standalone demo for the month sheet view

# Building It
Simply run the 'install' target inside the top level project. Once completed you will find the installation inside the target folder of the assembly module.

# Assemblies

There are currently two assemblies being created inside the target folder of the assembly module.
The "bin" assembly contains almost everything except for the source files.

## Binary Assembly

The following are the directories found inside the binary assembly.

### css

Contains the calendar.css file.

### demos

Contains several standalone / runnable JAR files. Demos can be started by either double clicking on them or by calling java -jar demo.jar. Make sure to use at least Java 8u60+.
   
### docs

Contains the generated JavaDocs / API HTML files.
   
### ext

Contains third party libraries that are needed for running CalendarFX (controlsfx, license4j, fontawesomefx).

### i18n

The resource bundles used by CalendarFX.
	
### lib

The actual CalendarFX libraries.
	
### misc

Property files used for logging.
	
### tutorial

Getting started files.


