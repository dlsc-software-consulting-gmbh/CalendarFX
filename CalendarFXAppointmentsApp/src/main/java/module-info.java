module com.calendarfx.appointments {
    requires transitive javafx.graphics;

    requires javafx.controls;
    requires com.calendarfx.view;

    exports com.calendarfx.appointments;

    opens com.calendarfx.appointments;
}
