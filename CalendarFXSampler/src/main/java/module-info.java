open module com.calendarfx.sampler {
    requires transitive org.controlsfx.fxsampler;
    requires transitive javafx.graphics;

    requires javafx.web;

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