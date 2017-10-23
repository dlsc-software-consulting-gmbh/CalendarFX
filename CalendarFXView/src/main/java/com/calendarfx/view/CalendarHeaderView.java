/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.view.DateControl.Layout;
import impl.com.calendarfx.view.CalendarHeaderViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import static java.util.Objects.requireNonNull;

/**
 * A view used for showing the short names of calendars on top of those
 * {@link DayView} instances that are using the {@link Layout#SWIMLANE} layout
 * strategy.
 * <p/>
 * <center><img width="100%" src="doc-files/calendar-header-view.png"></center>
 *
 * @see DetailedWeekView#getCalendarHeaderView()
 */
public class CalendarHeaderView extends CalendarFXControl {

    private static final String DEFAULT_STYLE_CLASS = "calendar-header"; //$NON-NLS-1$

    /**
     * Constructs a new calendar header view.
     */
    public CalendarHeaderView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setCellFactory(calendar -> {
            Label label = new Label();
            label.textProperty().bind(calendar.shortNameProperty());
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            return label;
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CalendarHeaderViewSkin(this);
    }

    public final void bind(DateControl dateControl) {
        Bindings.bindContentBidirectional(calendars, dateControl.getCalendars());
        Bindings.bindContentBidirectional(calendarVisibilityMap, dateControl.getCalendarVisibilityMap());
    }

    public final void unbind(DateControl dateControl) {
        Bindings.unbindContentBidirectional(calendars, dateControl.getCalendars());
        Bindings.unbindContentBidirectional(calendarVisibilityMap, dateControl.getCalendarVisibilityMap());
    }

    private final ObservableMap<Calendar, BooleanProperty> calendarVisibilityMap = FXCollections.observableHashMap();

    public final ObservableMap<Calendar, BooleanProperty> getCalendarVisibilityMap() {
        return calendarVisibilityMap;
    }

    public final BooleanProperty getCalendarVisibilityProperty(Calendar calendar) {
        return calendarVisibilityMap.computeIfAbsent(calendar, cal -> new SimpleBooleanProperty(CalendarHeaderView.this, "visible", true));
    }

    public final boolean isCalendarVisible(Calendar calendar) {
        BooleanProperty prop = getCalendarVisibilityProperty(calendar);
        return prop.get();
    }

    public final void setCalendarVisibility(Calendar calendar, boolean visible) {
        BooleanProperty prop = getCalendarVisibilityProperty(calendar);
        prop.set(visible);
    }

    private final ObjectProperty<Callback<Calendar, Node>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory"); //$NON-NLS-1$

    /**
     * Returns the property used for storing a reference to a cell factory that
     * will be used to create the nodes that will serve as column headers.
     *
     * @return the cell factory property
     */
    public final ObjectProperty<Callback<Calendar, Node>> cellFactoryProperty() {
        return cellFactory;
    }

    /**
     * Sets the value of {@link #cellFactoryProperty()}.
     *
     * @param factory
     *            the factory
     */
    public final void setCellFactory(Callback<Calendar, Node> factory) {
        requireNonNull(factory);
        cellFactoryProperty().set(factory);
    }

    /**
     * Returns the value of {@link #cellFactoryProperty()}.
     *
     * @return the cell factory
     */
    public final Callback<Calendar, Node> getCellFactory() {
        return cellFactory.get();
    }

    private final ObservableList<Calendar> calendars = FXCollections.observableArrayList();

    /**
     * The list of calendars for which the view will display a header.
     *
     * @return the calendars
     */
    public final ObservableList<Calendar> getCalendars() {
        return calendars;
    }

    private final IntegerProperty numberOfDays = new SimpleIntegerProperty(this, "numberOfDays", 1); //$NON-NLS-1$

    /**
     * Stores the number of days that will be shown by this view. This value
     * will be 1 if the view is used in combination with the {@link DayView} and
     * 7 if used together with the {@link DetailedWeekView}.
     *
     * @return the number of days shown by the view
     *
     * @see DetailedWeekView#numberOfDaysProperty()
     */
    public final IntegerProperty numberOfDaysProperty() {
        return numberOfDays;
    }

    /**
     * Returns the value of {@link #numberOfDaysProperty()}.
     *
     * @return the number of days shown by the view
     */
    public final int getNumberOfDays() {
        return numberOfDaysProperty().get();
    }

    /**
     * Sets the value of {@link #numberOfDaysProperty()}.
     *
     * @param number
     *            the new number of days shown by the view
     */
    public final void setNumberOfDays(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("invalid number of days, must be larger than 0 but was " //$NON-NLS-1$
                    + number);
        }

        numberOfDaysProperty().set(number);
    }
}
