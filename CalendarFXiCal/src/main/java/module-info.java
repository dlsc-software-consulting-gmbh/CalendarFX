module com.calendarfx.ical {
    requires java.logging;
    requires com.calendarfx.view;
    requires org.mnode.ical4j.core;

    exports com.calendarfx.ical;
    exports com.calendarfx.ical.model;
    exports com.calendarfx.ical.view;
}