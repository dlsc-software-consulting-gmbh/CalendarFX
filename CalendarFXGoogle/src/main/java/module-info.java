module com.calendarfx.google {
    requires transitive javafx.graphics;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.iconli.core;

    requires com.calendarfx.view;
    requires javafx.base;
    requires com.google.api.services.calendar;
    requires org.controlsfx.controls;
    requires javafx.controls;
    requires javafx.web;
    requires geocoder.java;
    requires GMapsFX;
    requires com.google.common;
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.services.oauth2;
    requires com.google.api.client.json.jackson2;
    requires google.api.client;

    exports com.calendarfx.google;
}