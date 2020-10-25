module com.calendarfx.weather {
    requires transitive javafx.graphics;

    requires javafx.controls;
    requires com.calendarfx.view;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.weathericons;

    exports com.calendarfx.weather;
}