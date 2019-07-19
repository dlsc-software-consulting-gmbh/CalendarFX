module com.calendarfx.view {

    requires org.controlsfx.controls;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires de.jensd.fx.glyphs.fontawesome;
    requires transitive com.calendarfx.recurrence;

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