module com.calendarfx.sampler {
    requires com.calendarfx.view;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires fxsampler;

    exports com.calendarfx.demo;
    exports com.calendarfx.demo.entries;
    exports com.calendarfx.demo.pages;
    exports com.calendarfx.demo.performance;
    exports com.calendarfx.demo.popover;
    exports com.calendarfx.demo.print;
    exports com.calendarfx.demo.views;

    provides fxsampler.FXSamplerProject with com.calendarfx.demo.CalendarFXSamplerProject;
}