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

import impl.com.calendarfx.view.ViewHelper;
import impl.com.calendarfx.view.WeekViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.util.Objects.requireNonNull;

/**
 * A view for showing several week days in a row, normally seven. However the
 * view can be configured to show any number of days. A factory is used to
 * create instances of {@link WeekDayView} on the fly as needed. Another factory
 * is required for creating the child control {@link AllDayView}. The image
 * below shows the appearance of this view when it is embedded inside the
 * {@link DetailedWeekView}.
 * <p>
 * <p/>
 * <center><img src="doc-files/week-view.png"></center>
 * <p/>
 */
public class WeekView extends DayViewBase {

    private static final String DEFAULT_STYLE_CLASS = "week-view";

    /**
     * Constructs a new week view with seven days.
     */
    public WeekView() {
        this(7);
    }

    /**
     * Constructs a new week view with the given number of days.
     *
     * @param numberOfDays the number of days (day views)
     */
    public WeekView(int numberOfDays) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setWeekDayViewFactory(param -> new WeekDayView());
        setNumberOfDays(numberOfDays);

        dateProperty().addListener(it -> updateStartAndEndDates());

        updateStartAndEndDates();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WeekViewSkin(this);
    }

    @Override
    public ZonedDateTime getZonedDateTimeAt(double x, double y) {
        final WeekDayView view = getWeekDayViewAt(x);
        if (view != null) {
            return ZonedDateTime.of(ViewHelper.getLocationTime(view, y, false, true), getZoneId());
        }

        return super.getZonedDateTimeAt(x, y);
    }

    private WeekDayView getWeekDayViewAt(double x) {
        for (WeekDayView view : getWeekDayViews()) {
            final Bounds bounds = view.getBoundsInParent();
            if (bounds.getMinX() <= x && bounds.getMaxX() >= x) {
                return view;
            }
        }

        return null;
    }

    private final ObservableList<WeekDayView> weekDayViews = FXCollections.observableArrayList();

    /**
     * A list of the {@link WeekDayView} instances that the view created via the factory.
     *
     * @see #setWeekDayViewFactory(Callback)
     *
     * @return the currently used list of week day views (please note that this list is very volatile
     * and will be updated very often.
     */
    public final ObservableList<WeekDayView> getWeekDayViews() {
        return weekDayViews;
    }

    private final IntegerProperty numberOfDays = new SimpleIntegerProperty(this, "numberOfDays", 7); //$NON-NLS-1$

    /**
     * Stores the number of days that will be shown by this view. This value
     * will be 1 if the view is used in combination with the {@link DayView} and
     * 7 if used together with the {@link DetailedWeekView}.
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
     * @param number the new number of days shown by the view
     */
    public final void setNumberOfDays(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("invalid number of days, must be larger than 0 but was " //$NON-NLS-1$
                    + number);
        }

        numberOfDaysProperty().set(number);
    }

    private final BooleanProperty adjustToFirstDayOfWeek = new SimpleBooleanProperty(this, "adjustToFirstDayOfWeek", true); //$NON-NLS-1$

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
     * @param adjust if true the view will always show the first day of the week
     */
    public final void setAdjustToFirstDayOfWeek(boolean adjust) {
        adjustToFirstDayOfWeekProperty().set(adjust);
    }

    /**
     * The parameter object for the week day view factory.
     *
     * @see #weekDayViewFactoryProperty()
     */
    public static final class WeekDayParameter {

        private WeekView weekView;

        /**
         * Constructs a new parameter object.
         *
         * @param weekView the week view for which the week day view will be used
         */
        public WeekDayParameter(WeekView weekView) {
            this.weekView = Objects.requireNonNull(weekView);
        }

        /**
         * Returns the week view where the day view will be used.
         *
         * @return the week view
         */
        public WeekView getWeekView() {
            return weekView;
        }
    }

    private final ObjectProperty<Callback<WeekDayParameter, WeekDayView>> weekDayViewFactory = new SimpleObjectProperty<>(this, "weekDayViewFactory"); //$NON-NLS-1$

    /**
     * A factory used for creating instances of {@link WeekDayView} on the fly
     * as required.
     *
     * @return the week day view factory
     */
    public final ObjectProperty<Callback<WeekDayParameter, WeekDayView>> weekDayViewFactoryProperty() {
        return weekDayViewFactory;
    }

    /**
     * Returns the value of {@link #weekDayViewFactoryProperty()}.
     *
     * @return the week day view factory
     */
    public final Callback<WeekDayParameter, WeekDayView> getWeekDayViewFactory() {
        return weekDayViewFactoryProperty().get();
    }

    /**
     * Sets the value of {@link #weekDayViewFactoryProperty()}.
     *
     * @param factory the new factory
     */
    public final void setWeekDayViewFactory(Callback<WeekDayParameter, WeekDayView> factory) {
        requireNonNull(factory);
        weekDayViewFactoryProperty().set(factory);
    }

    private void updateStartAndEndDates() {
        LocalDate date = calculateStartDate();
        startDate.set(date);
        endDate.set(date.plusDays(getNumberOfDays() - 1));
    }

    private LocalDate calculateStartDate() {
        LocalDate startDate = getDate();

        if (isAdjustToFirstDayOfWeek()) {
            LocalDate newStartDate = startDate.with(DAY_OF_WEEK, getFirstDayOfWeek().getValue());
            if (newStartDate.isAfter(startDate)) {
                startDate = newStartDate.minusWeeks(1);
            } else {
                startDate = newStartDate;
            }
        }

        return startDate;
    }

    private final ReadOnlyObjectWrapper<LocalDate> startDate = new ReadOnlyObjectWrapper<>(this, "startDate"); //$NON-NLS-1$

    /**
     * The earliest date shown by the view.
     *
     * @return the earliest date shown
     */
    public final ReadOnlyObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    /**
     * Returns the value of {@link #startDateProperty()}.
     *
     * @return the earliest date shown
     */
    public final LocalDate getStartDate() {
        return startDate.get();
    }

    private final ReadOnlyObjectWrapper<LocalDate> endDate = new ReadOnlyObjectWrapper<>(this, "endDate"); //$NON-NLS-1$

    /**
     * The latest date shown by the view.
     *
     * @return the latest date shown
     */
    public final ReadOnlyObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    /**
     * Returns the value of {@link #endDateProperty()}.
     *
     * @return the latest date shown
     */
    public final LocalDate getEndDate() {
        return endDate.get();
    }

    private static final String WEEK_VIEW_CATEGORY = "Week View"; //$NON-NLS-1$

    @Override
    public ObservableList<PropertySheet.Item> getPropertySheetItems() {
        ObservableList<PropertySheet.Item> items = super.getPropertySheetItems();

        items.add(new PropertySheet.Item() {

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
                return WEEK_VIEW_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(adjustToFirstDayOfWeekProperty());
            }

            @Override
            public void setValue(Object value) {
                setAdjustToFirstDayOfWeek((Boolean) value);
            }

            @Override
            public Object getValue() {
                return isAdjustToFirstDayOfWeek();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Adjust to first day of week"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Adjust to first day of week"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return WEEK_VIEW_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(startDateProperty());
            }

            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return getStartDate();
            }

            @Override
            public Class<?> getType() {
                return LocalDate.class;
            }

            @Override
            public String getName() {
                return "Start date (read-only)"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Start date (read-only)"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return WEEK_VIEW_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(endDateProperty());
            }

            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return getEndDate();
            }

            @Override
            public Class<?> getType() {
                return LocalDate.class;
            }

            @Override
            public String getName() {
                return "End date (read-only)"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "End date (read-only)"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return WEEK_VIEW_CATEGORY;
            }
        });

        return items;
    }
}
