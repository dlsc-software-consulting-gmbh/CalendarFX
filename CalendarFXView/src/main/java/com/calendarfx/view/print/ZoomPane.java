/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view.print;

import com.calendarfx.view.CalendarFXControl;
import impl.com.calendarfx.view.print.ZoomPaneSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;

import java.util.Objects;

/**
 * The zoom pane manages a zoom property, which is used to scale the pane's
 * content.
 */
public class ZoomPane extends CalendarFXControl {

    public static final double MIN_ZOOM_VALUE = 1.0;

    public static final double MAX_ZOOM_VALUE = 5.0;

    public static final String DEFAULT_STYLE = "zoom-pane";

    /**
     * Constructs a new zoom pane.
     */
    public ZoomPane() {
        super();
        getStyleClass().add(DEFAULT_STYLE);

        zoomProperty().addListener(obs -> {
            if (getZoom() < MIN_ZOOM_VALUE || getZoom() > MAX_ZOOM_VALUE) {
                throw new IndexOutOfBoundsException("The new value is out of bounds!");
            }
        });

        setPrefSize(700, 700);
    }

    /**
     * Constructs a new zoom pane.
     *
     * @param content the initial content
     */
    public ZoomPane(Region content) {
        this();
        setContent(content);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ZoomPaneSkin(this);
    }

    // content support

    private final ObjectProperty<Region> content = new SimpleObjectProperty<Region>(this, "content") {
        @Override
        public void set(Region newValue) {
            Objects.requireNonNull(newValue);
            setZoom(MIN_ZOOM_VALUE);
            super.set(newValue);
        }
    };

    public final ObjectProperty<Region> contentProperty() {
        return content;
    }

    public final Region getContent() {
        return contentProperty().get();
    }

    public final void setContent(Region content) {
        contentProperty().set(content);
    }

    // zoom support

    private final DoubleProperty zoom = new SimpleDoubleProperty(this, "zoom", MIN_ZOOM_VALUE);

    public final DoubleProperty zoomProperty() {
        return zoom;
    }

    public final double getZoom() {
        return zoomProperty().get();
    }

    public final void setZoom(double zoom) {
        zoomProperty().set(zoom);
    }

}
