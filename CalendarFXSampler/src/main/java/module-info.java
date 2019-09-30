open module com.calendarfx.sampler {

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.web;

    requires fxsampler;

    requires org.controlsfx.controls;

    requires com.calendarfx.view;

    exports com.calendarfx.demo to org.controlsfx.fxsampler;
    exports com.calendarfx.demo.entries to org.controlsfx.fxsampler;
    exports com.calendarfx.demo.pages to org.controlsfx.fxsampler;
    exports com.calendarfx.demo.performance to org.controlsfx.fxsampler;
    exports com.calendarfx.demo.popover to org.controlsfx.fxsampler;
    exports com.calendarfx.demo.print to org.controlsfx.fxsampler;
    exports com.calendarfx.demo.views to org.controlsfx.fxsampler;

    provides fxsampler.FXSamplerProject with com.calendarfx.demo.CalendarFXSamplerProject;
}