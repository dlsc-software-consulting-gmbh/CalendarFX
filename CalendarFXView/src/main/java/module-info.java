module com.calendarfx.view {

    requires java.logging;

    requires transitive org.controlsfx.controls;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    requires transitive org.kordamp.ikonli.javafx;
    requires transitive org.kordamp.ikonli.fontawesome;

    requires ical4j;

    exports com.calendarfx.model;
    exports com.calendarfx.util;
    exports com.calendarfx.view;
    exports com.calendarfx.view.page;
    exports com.calendarfx.view.popover;
    exports com.calendarfx.view.print;

    exports impl.com.calendarfx.view;
    exports impl.com.calendarfx.view.page;
    exports impl.com.calendarfx.view.popover;
    exports impl.com.calendarfx.view.print;
    exports impl.com.calendarfx.view.util;

    opens com.calendarfx.view;
}