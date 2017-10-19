The misc folder contains property files used for i18n (translations) and logging.

i18n
----

messages.properties -- the default resource bundle (English)
messages_de.properties -- the resource bundle for German

If you want to provide your own resource bundle then create a package called 
com.calendarfx.view and place your bundle inside of it.

Logging 
-------

logging.properties -- the settings for the standard java logging API

CalendarFX uses a domain approach to logging (instead of a class or package-based approach).
The following domains are available:

-- config: reports info related to the configuration of the framework
-- model: enables logging output related to the model classes, e.g. Entry and Calendar
-- events: when events are fired by the framework then this logger will report it
-- view: anything visible / controls use this logger
-- search: logging related to the built-in search feature
-- editing: used when the user edits entries (add, remove, changed)
-- recurrence: logging for the recurrence support


