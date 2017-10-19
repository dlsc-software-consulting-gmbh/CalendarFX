/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view.print;

import com.calendarfx.view.CalendarFXControl;
import impl.com.calendarfx.view.print.PreviewPaneSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import java.time.LocalDate;

/**
 * The preview pane wraps around the zoom pane which again wraps around the
 * printable printablePage. The preview pane features a zoom slider and printablePage flipping
 * controls that come in handy when the user requested to print several pages.
 */
public class PreviewPane extends CalendarFXControl {

    public static final String DEFAULT_STYLE = "print-preview";

    private final PrintablePage printablePage = new PrintablePage();

    private final ZoomPane zoomPane = new ZoomPane(printablePage);

    /**
     * Constructs a new preview pane.
     */
    public PreviewPane() {
        getStyleClass().add(DEFAULT_STYLE);

        final InvalidationListener layoutListener = obs -> zoomPane.requestLayout();
        printablePage.viewTypeProperty().addListener(layoutListener);
        printablePage.paperProperty().addListener(layoutListener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PreviewPaneSkin(this);
    }

    public final PrintablePage getPrintablePage() {
        return printablePage;
    }

    public final ZoomPane getZoomPane() {
        return zoomPane;
    }

    // print start date

    private final ObjectProperty<LocalDate> printStartDate = new SimpleObjectProperty<>(this, "printStartDate");

    public final ObjectProperty<LocalDate> printStartDateProperty() {
        return printStartDate;
    }

    public final LocalDate getPrintStartDate() {
        return printStartDateProperty().get();
    }

    public final void setPrintStartDate(LocalDate date) {
        printStartDateProperty().set(date);
    }

    // print end date

    private final ObjectProperty<LocalDate> printEndDate = new SimpleObjectProperty<>(this, "printEndDate");

    public final ObjectProperty<LocalDate> printEndDateProperty() {
        return printEndDate;
    }

    public final LocalDate getPrintEndDate() {
        return printEndDateProperty().get();
    }

    public final void setPrintEndDate(LocalDate date) {
        printEndDateProperty().set(date);
    }
}
