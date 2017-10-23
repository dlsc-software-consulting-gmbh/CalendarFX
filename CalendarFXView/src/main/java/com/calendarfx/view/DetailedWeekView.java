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
import impl.com.calendarfx.view.DetailedWeekViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import org.controlsfx.control.PropertySheet.Item;

import java.time.LocalDate;
import java.util.Optional;

/**
 * A view for showing several week days in a row, normally seven. However the
 * view can be configured to show any number of days. The view consists of several
 * larger sub controls: a week view, an all day view, a calendar header view, a
 * week day header view, and a time scale view. The image below shows the appearance
 * of this view.
 * <p/>
 * <center><img src="doc-files/detailed-week-view.png"></center>
 * <p/>
 * @see WeekView
 */
public class DetailedWeekView extends DayViewBase {

    private static final String DEFAULT_STYLE_CLASS = "detailed-week-view";

    private final WeekDayHeaderView weekDayHeaderView;

    private final WeekView weekView;

    private final AllDayView allDayView;

    private final WeekTimeScaleView timeScaleView;

    private final CalendarHeaderView calendarHeaderView;

    /**
     * Constructs a new view with seven days.
     */
    public DetailedWeekView() {
        this(7);
    }

    /**
     * Constructs a new view with the given number of days.
     *
     * @param numberOfDays the number of days to show in the view
     */
    public DetailedWeekView(int numberOfDays) {
        setNumberOfDays(numberOfDays);

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        calendarHeaderView = new CalendarHeaderView();
        calendarHeaderView.numberOfDaysProperty().bind(numberOfDaysProperty());
        calendarHeaderView.bind(this);

        weekDayHeaderView = new WeekDayHeaderView();
        bind(weekDayHeaderView, true);
        Bindings.bindBidirectional(weekDayHeaderView.numberOfDaysProperty(), numberOfDaysProperty());

        allDayView = new AllDayView(getNumberOfDays());
        bind(allDayView, true);
        Bindings.bindBidirectional(allDayView.numberOfDaysProperty(), numberOfDaysProperty());

        weekView = new WeekView();
        bind(weekView, true);
        Bindings.bindBidirectional(weekView.numberOfDaysProperty(), numberOfDaysProperty());

        timeScaleView = new WeekTimeScaleView();
        bind(timeScaleView, true);

        startDate.bind(weekView.startDateProperty());
        endDate.bind(weekView.endDateProperty());

    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DetailedWeekViewSkin(this);
    }

    /**
     * Returns the header view control that gets shown when the layout of the
     * control is set to {@link com.calendarfx.view.DateControl.Layout#SWIMLANE}
     * . The header view displays the short name of each calendar that is
     * currently visible.
     *
     * @return the calendar header view control
     * @see Calendar#getShortName()
     */
    public final CalendarHeaderView getCalendarHeaderView() {
        return calendarHeaderView;
    }

    /**
     * Returns the header view that is used to display the names of the
     * week days (Mon, Tue, Wed, ...).
     *
     * @return the week day header view
     */
    public final WeekDayHeaderView getWeekDayHeaderView() {
        return weekDayHeaderView;
    }

    /**
     * Returns the all day view child control.
     *
     * @return the all day view
     */
    public final AllDayView getAllDayView() {
        return allDayView;
    }

    /**
     * Returns the time scale child control.
     *
     * @return the time scale view
     */
    public final WeekTimeScaleView getTimeScaleView() {
        return timeScaleView;
    }

    /**
     * Returns the week view child control.
     *
     * @return the week view
     */
    public final WeekView getWeekView() {
        return weekView;
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

    // all day view support

    private final BooleanProperty showAllDayView = new SimpleBooleanProperty(this, "showAllDayView", true);

    /**
     * A property used to control the visibility of the all day view. The
     * all day view displays those calendar entries that do not have a specific
     * start and end time but that are valid for the entire day or day range.
     *
     * @return true if the all day view will be shown to the user
     */
    public final BooleanProperty showAllDayViewProperty() {
        return showAllDayView;
    }

    /**
     * Returns the value of {@link #showAllDayViewProperty()}.
     *
     * @return true if the all day view will be visible
     */
    public final boolean isShowAllDayView() {
        return showAllDayViewProperty().get();
    }

    /**
     * Sets the value of {@link #showAllDayViewProperty()}.
     *
     * @param show if true the the all day view will be visible
     */
    public final void setShowAllDayView(boolean show) {
        showAllDayViewProperty().set(show);
    }

    // time scale support

    private final BooleanProperty showTimeScaleView = new SimpleBooleanProperty(this, "showTimeScaleView", true);

    /**
     * A property used to control the visibility of the time scale on the left-hand side.
     * The time scale displays the time of day.
     *
     * @return true if the scale will be visible
     */
    public final BooleanProperty showTimeScaleViewProperty() {
        return showTimeScaleView;
    }

    /**
     * Returns the value of {@link #showTimeScaleViewProperty()}.
     *
     * @return true if the time scale will be shown to the user
     */
    public final boolean isShowTimeScaleView() {
        return showTimeScaleViewProperty().get();
    }

    /**
     * Sets the value of {@link #showTimeScaleViewProperty()}.
     *
     * @param show if true the time scale will be shown to the user
     */
    public final void setShowTimeScaleView(boolean show) {
        showTimeScaleViewProperty().set(show);
    }

    // week day view support

    private final BooleanProperty showWeekDayHeaderView = new SimpleBooleanProperty(this, "showWeekDayHeaderView", true);

    /**
     * A property used to control the visibility of the week day header. The
     * header displays the weekday names (Mo., Tu., ....).
     *
     * @return true if the week day header should be visible
     */
    public final BooleanProperty showWeekDayHeaderViewProperty() {
        return showWeekDayHeaderView;
    }

    /**
     * Returns the value of {@link #showWeekDayHeaderViewProperty()}.
     *
     * @return true if the week day header should be visible
     */
    public final boolean isShowWeekDayHeaderView() {
        return showWeekDayHeaderViewProperty().get();
    }

    /**
     * Sets the value of {@link #showWeekDayHeaderViewProperty()}.
     *
     * @param show if true the week day header will be visible
     */
    public final void setShowWeekDayHeaderView(boolean show) {
        showWeekDayHeaderViewProperty().set(show);
    }

    // show scrollbar support

    private final BooleanProperty showScrollBar = new SimpleBooleanProperty(this, "showScrollBar", true);

    /**
     * A property used to control the visibility of the vertial scrollbar.
     *
     * @return true if the scrollbar should be shown to the user
     */
    public final BooleanProperty showScrollBarProperty() {
        return showScrollBar;
    }

    /**
     * Sets the value of {@link #showScrollBarProperty()}.
     *
     * @param show if true the scrollbar will be visible
     */
    public final void setShowScrollBar(boolean show) {
        this.showScrollBar.set(show);
    }

    /**
     * Returns the value of {@link #showScrollBarProperty()}.
     *
     * @return true if the scrollbar will be visible
     */
    public final boolean isShowScrollBar() {
        return showScrollBar.get();
    }

    // start date support

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

    // end date support

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

    @Override
    public void goForward() {
        setDate(getDate().plusDays(getNumberOfDays()));
    }

    @Override
    public void goBack() {
        setDate(getDate().minusDays(getNumberOfDays()));
    }

    private static final String WEEK_VIEW_CATEGORY = "Week View"; //$NON-NLS-1$

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
                return WEEK_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {
            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getCategory() {
                return WEEK_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Show All Day View";
            }

            @Override
            public String getDescription() {
                return "Show All Day View";
            }

            @Override
            public Object getValue() {
                return isShowAllDayView();
            }

            @Override
            public void setValue(Object value) {
                setShowAllDayView((boolean) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showAllDayViewProperty());
            }
        });

        items.add(new Item() {
            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getCategory() {
                return WEEK_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Show Time Scale View";
            }

            @Override
            public String getDescription() {
                return "Show Time Scale View";
            }

            @Override
            public Object getValue() {
                return isShowTimeScaleView();
            }

            @Override
            public void setValue(Object value) {
                setShowTimeScaleView((boolean) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showTimeScaleViewProperty());
            }
        });

        items.add(new Item() {
            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getCategory() {
                return WEEK_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Show Weekday Header View";
            }

            @Override
            public String getDescription() {
                return "Show Weekday Header View";
            }

            @Override
            public Object getValue() {
                return isShowWeekDayHeaderView();
            }

            @Override
            public void setValue(Object value) {
                setShowWeekDayHeaderView((boolean) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showWeekDayHeaderViewProperty());
            }
        });

        items.add(new Item() {
            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getCategory() {
                return WEEK_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Show ScrollBar";
            }

            @Override
            public String getDescription() {
                return "Show ScrollBar";
            }

            @Override
            public Object getValue() {
                return isShowScrollBar();
            }

            @Override
            public void setValue(Object value) {
                setShowScrollBar((boolean) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showScrollBarProperty());
            }
        });

        items.add(new Item() {

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

        return items;
    }
}
