module com.calendarfx.ical {
    requires transitive ical4j.core;
    requires transitive com.calendarfx.view;

    requires java.logging;

    exports com.calendarfx.ical;
    exports com.calendarfx.ical.model;
    exports com.calendarfx.ical.view;
}