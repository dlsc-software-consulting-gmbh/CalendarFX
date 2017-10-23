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

import com.calendarfx.util.Util;
import impl.com.calendarfx.view.WeekDayHeaderViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet.Item;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A view for displaying the names of the week days in the {@link DetailedWeekView}
 * control. A cell factory can be used to customize the appearance of the names.
 * <p/>
 * <center><img src="doc-files/week-weekdays.png"></center>
 * <p/>
 */
public class WeekDayHeaderView extends DateControl {

    private static final String DEFAULT_STYLE_CLASS = "weekday-header-view";

    /**
     * Constructs a new week day header view.
     */
    public WeekDayHeaderView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        dateProperty().addListener(it -> {
            LocalDate date = getDate();
            if (isAdjustToFirstDayOfWeek()) {
                date = Util.adjustToFirstDayOfWeek(date, getFirstDayOfWeek());
            }

            startDate.set(date);
            endDate.set(date.plusDays(getNumberOfDays() - 1));
        });

        startDate.set(getDate());
        endDate.set(getDate().plusDays(getNumberOfDays() - 1));

        setCellFactory(date -> new WeekDayCell(this));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WeekDayHeaderViewSkin(this);
    }

    private final ObjectProperty<Callback<WeekDayHeaderView, WeekDayCell>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory"); //$NON-NLS-1$

    /**
     * A cell factory used for creating instances of {@link WeekDayCell} that will
     * be used to display the weekend day names.
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<WeekDayHeaderView, WeekDayCell>> cellFactoryProperty() {
        return cellFactory;
    }

    /**
     * Returns the value of {@link #cellFactoryProperty()}.
     *
     * @return the cell factory
     */
    public final Callback<WeekDayHeaderView, WeekDayCell> getCellFactory() {
        return cellFactoryProperty().get();
    }

    /**
     * Sets the value of {@link #cellFactoryProperty()}.
     *
     * @param factory
     *            the cell factory
     */
    public final void setCellFactory(
            Callback<WeekDayHeaderView, WeekDayCell> factory) {
        requireNonNull(factory);
        cellFactoryProperty().set(factory);
    }

    private final IntegerProperty numberOfDays = new SimpleIntegerProperty(
            this, "numberOfDays", 7); //$NON-NLS-1$

    /**
     * Stores the number of days that will be shown by this view. This value
     * needs to be equivalent to the number of days shown by the
     * {@link DetailedWeekView}.
     *
     * @return the number of days shown by the view
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
            throw new IllegalArgumentException(
                    "invalid number of days, must be larger than 0 but was " //$NON-NLS-1$
                            + number);
        }

        numberOfDaysProperty().set(number);
    }

    private final BooleanProperty adjustToFirstDayOfWeek = new SimpleBooleanProperty(
            this, "adjustToFirstDayOfWeek", true); //$NON-NLS-1$

    /**
     * A flag used to indicate that the view should always show the first day of
     * the week (e.g. "Monday") at its beginning even if the
     * {@link #dateProperty()} is set to another day (e.g. "Thursday").
     *
     * @return true if the view always shows the first day of the week
     */
    public final BooleanProperty adjustToFirstDayOfWeekProperty() {
        return adjustToFirstDayOfWeek;
    }

    /**
     * Returns the value of {@link #adjustToFirstDayOfWeekProperty()}.
     *
     * @return true if the view always shows the first day of the week
     */
    public final boolean isAdjustToFirstDayOfWeek() {
        return adjustToFirstDayOfWeekProperty().get();
    }

    /**
     * Sets the value of {@link #adjustToFirstDayOfWeekProperty()}.
     *
     * @param adjust
     *            if true the view will always show the first day of the week
     */
    public final void setAdjustToFirstDayOfWeek(boolean adjust) {
        adjustToFirstDayOfWeekProperty().set(adjust);
    }

    private final ReadOnlyObjectWrapper<LocalDate> startDate = new ReadOnlyObjectWrapper<>(
            this, "startDate"); //$NON-NLS-1$

    /**
     * The first date shown by the view.
     *
     * @return the first date shown by the view
     */
    public final ReadOnlyObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    /**
     * Returns the value of {@link #startDateProperty()}.
     *
     * @return the start date
     */
    public final LocalDate getStartDate() {
        return startDate.get();
    }

    private final ReadOnlyObjectWrapper<LocalDate> endDate = new ReadOnlyObjectWrapper<>(
            this, "endDate"); //$NON-NLS-1$

    /**
     * The last date shown by the view.
     *
     * @return the last date shown by the view
     */
    public final ReadOnlyObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    /**
     * A cell used by the {@link WeekDayHeaderView} to display the names of the
     * weekdays (Mo. 5th, Tue. 6th, ...).
     *
     * @see WeekDayHeaderView#cellFactoryProperty()
     */
    public static class WeekDayCell extends Label {

        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Messages.getString("WeekDayHeaderView.CELL_DATE_FORMAT")); //$NON-NLS-1$

        /**
         * Constructs a new date cell.
         */
        public WeekDayCell(WeekDayHeaderView view) {
            Objects.requireNonNull(view);
            getStyleClass().add("cell"); //$NON-NLS-1$
            setMaxWidth(Double.MAX_VALUE);
            dateProperty().addListener(it -> setText(formatter.format(getDate())));
            if (view.isEnableHyperlinks()) {
                getStyleClass().add("date-hyperlink");
                setOnMouseClicked(evt -> {
                    if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 1) {
                        fireEvent(new RequestEvent(this, this, getDate()));
                    }
                });
            }
        }

        private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date", LocalDate.now()); //$NON-NLS-1$

        /**
         * The date shown by this cell.
         *
         * @return the date
         */
        public final ObjectProperty<LocalDate> dateProperty() {
            return date;
        }

        /**
         * Sets the value of {@link #dateProperty()}.
         *
         * @param date
         *            the new date to show
         */
        public final void setDate(LocalDate date) {
            dateProperty().set(date);
        }

        /**
         * Returns the value of {@link #dateProperty()}.
         *
         * @return the date shown by the cell
         */
        public final LocalDate getDate() {
            return dateProperty().get();
        }
    }

    private static final String WEEK_DAY_HEADER_VIEW_CATEGORY = "Week Day Header View"; //$NON-NLS-1$

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(numberOfDaysProperty());
            }

            @Override
            public void setValue(Object value) {
                setNumberOfDays((Integer) value);
            }

            @Override
            public Object getValue() {
                return getNumberOfDays();
            }

            @Override
            public Class<?> getType() {
                return Integer.class;
            }

            @Override
            public String getName() {
                return "Number of Days"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Number of Days"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return WEEK_DAY_HEADER_VIEW_CATEGORY;
            }
        });

        return items;
    }
}
