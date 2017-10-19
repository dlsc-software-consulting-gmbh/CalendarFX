/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import impl.com.calendarfx.view.YearViewSkin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import java.time.Month;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

/**
 * A view that shows exactly twelve months of a year via the help of
 * the {@link YearMonthView} control. The image below shows the year view
 * with usage coloring enabled.
 *
 * @see YearMonthView#setShowUsageColors(boolean)
 * @see DateControl#setUsagePolicy(Callback)
 *
 * <h3>Screenshot</h3>
 * <center><img src="doc-files/year-view.png"></center>
 */
public class YearView extends DateControl {

    private static final String DEFAULT_STYLE_CLASS = "year-view";

    private Map<Month, YearMonthView> viewMap = new HashMap<>();

    /**
     * Constructs a new year view.
     */
    public YearView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        dateProperty().addListener(evt -> year.set(Year.from(getDate())));

        for (Month month : Month.values()) {
            YearMonthView view = new YearMonthView();
            viewMap.put(month, view);
            view.setShowYear(false);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearViewSkin(this);
    }

    private final ReadOnlyObjectWrapper<Year> year = new ReadOnlyObjectWrapper<>(this, "year", Year.from(getToday())); //$NON-NLS-1$

    /**
     * Reports the year shown by the control.
     *
     * @return the year and month
     */
    public final ReadOnlyObjectProperty<Year> yearProperty() {
        return year.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #yearProperty()}.
     *
     * @return the year
     */
    public final Year getYear() {
        return yearProperty().get();
    }

    /**
     * Returns the view that is used for displaying the given month of the year.
     *
     * @param month
     *            the month
     * @return a view for the given month
     */
    public final YearMonthView getMonthView(Month month) {
        return viewMap.get(month);
    }
}
