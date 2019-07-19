module com.calendarfx.google {

    requires com.calendarfx.view;
    requires javafx.base;
    requires com.google.api.services.calendar;
    requires javafx.graphics;
    requires de.jensd.fx.glyphs.fontawesome;
    requires guava.jdk5;
    requires org.controlsfx.controls;
    requires google.http.client;
    requires javafx.controls;
    requires javafx.web;
    requires com.google.api.services.oauth2;
    requires google.oauth.client;
    requires google.api.client;
    requires google.http.client.jackson2;
    requires geocoder.java;
    requires GMapsFX;

    exports com.calendarfx.google;
}