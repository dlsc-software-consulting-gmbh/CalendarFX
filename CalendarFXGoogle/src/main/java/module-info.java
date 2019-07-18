module com.calendarfx.google {

    requires com.calendarfx.view;
    requires javafx.base;
    requires google.api.services.calendar.v3.rev342;
    requires javafx.graphics;
    requires de.jensd.fx.glyphs.fontawesome;
    requires guava.jdk5;
    requires org.controlsfx.controls;
    requires google.http.client;
    requires javafx.controls;
    requires javafx.web;
    requires google.api.services.oauth2.v2.rev141;
    requires google.oauth.client;
    requires google.api.client;
    requires google.http.client.jackson2;
    requires geocoder.java;
    requires GMapsFX;

    exports com.calendarfx.google;
}