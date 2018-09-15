module com.calendarfx.recurrence {
    requires joda.time;
    requires java.logging;

    exports com.google.ical.compat.javatime;
    exports com.google.ical.compat.javautil;
    exports com.google.ical.compat.jodatime;
    exports com.google.ical.iter;
    exports com.google.ical.util;
    exports com.google.ical.values;
}