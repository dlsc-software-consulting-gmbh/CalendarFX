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

package com.calendarfx.view.print;

import com.calendarfx.util.Util;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.print.TimeRangeField.TimeRangeFieldValue;
import impl.com.calendarfx.view.print.TimeRangeViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A control for specifying the start and end time of a time range. This control
 * is used as part of the print preview functionality. It allows the user to
 * specify the time range that has to be printed. The control supports day
 * ranges, week rangers, or month ranges. The default style class used by this
 * control is "time-range-view".
 *
 * <img src="doc-files/time-range-view.png" alt="Time Range View">
 */
public class TimeRangeView extends ViewTypeControl {

    private static final String DEFAULT_STYLE = "time-range-view";

    private final TimeRangeField startField = new TimeRangeField();

    private final TimeRangeField endField = new TimeRangeField(true);

    public final Map<ViewType, TimeRangeOldValues> oldValuesMap = new EnumMap<>(
            ViewType.class);

    public final InvalidationListener OldValuesListener = obs -> catchOldValues();

    /**
     * Constructs a new time range view.
     */
    public TimeRangeView() {

        viewTypeProperty().addListener(obse -> {
            startDate.removeListener(OldValuesListener);
            endDate.removeListener(OldValuesListener);
        });

        startField.viewTypeProperty().bind(viewTypeProperty());
        startField.todayProperty().bind(todayProperty());
        startField.weekFieldsProperty().bind(weekFieldsProperty());

        InvalidationListener startDateUpdater = obs -> updateStartDate();
        startField.valueProperty().addListener(startDateUpdater);
        startField.onDateProperty().addListener(startDateUpdater);
        startField.onWeekNumberProperty().addListener(startDateUpdater);
        startField.monthYearProperty().addListener(startDateUpdater);
        startField.afterUnitsProperty().addListener(startDateUpdater);
        startField.todayProperty().addListener(startDateUpdater);

        endField.viewTypeProperty().bind(viewTypeProperty());
        endField.todayProperty().bind(todayProperty());
        endField.weekFieldsProperty().bind(weekFieldsProperty());

        InvalidationListener endDateUpdater = obs -> updateEndDate();
        endField.valueProperty().addListener(endDateUpdater);
        endField.onDateProperty().addListener(endDateUpdater);
        endField.onWeekNumberProperty().addListener(endDateUpdater);
        endField.monthYearProperty().addListener(endDateUpdater);
        endField.afterUnitsProperty().addListener(endDateUpdater);
        endField.todayProperty().addListener(endDateUpdater);

        startDate.addListener(obs -> {
            fixEndField();
            updateEndDate();
            updateUnitsToPrint();
        });

        endDate.addListener(obs -> {
            fixStartField();
            updateUnitsToPrint();
        });

        oldValuesMap.put(ViewType.DAY_VIEW, null);
        oldValuesMap.put(ViewType.WEEK_VIEW, null);
        oldValuesMap.put(ViewType.MONTH_VIEW, null);

        getStyleClass().add(DEFAULT_STYLE);
        updateStartDate();
        updateEndDate();
        updateUnitsToPrint();
    }

    private final ObjectProperty<LocalDate> today = new SimpleObjectProperty<>(
            this, "today", LocalDate.now());

    /**
     * Stores the date that is considered to represent "today". This property is
     * initialized with {@link LocalDate#now()} but can be any date.
     *
     * @return the date representing "today"
     */
    public final ObjectProperty<LocalDate> todayProperty() {
        return today;
    }

    /**
     * Sets the value of {@link #todayProperty()}.
     *
     * @param date
     *            the date representing "today"
     */
    public final void setToday(LocalDate date) {
        requireNonNull(date);
        todayProperty().set(date);
    }

    /**
     * Returns the value of {@link #todayProperty()}.
     *
     * @return the date representing "today"
     */
    public final LocalDate getToday() {
        return todayProperty().get();
    }

    private final ObjectProperty<WeekFields> weekFields = new SimpleObjectProperty<>(
            this, "weekFields",
            WeekFields.ISO);

    /**
     * Week fields are used to determine the first day of a week (e.g. "Monday"
     * in Germany or "Sunday" in the US). It is also used to calculate the week
     * number as the week fields determine how many days are needed in the first
     * week of a year. This property is initialized with {@link WeekFields#ISO}.
     *
     * @return the week fields
     */
    public final ObjectProperty<WeekFields> weekFieldsProperty() {
        return weekFields;
    }

    /**
     * Sets the value of {@link #weekFieldsProperty()}.
     *
     * @param fields
     *            the new week fields
     */
    public final void setWeekFields(WeekFields fields) {
        requireNonNull(fields);
        weekFieldsProperty().set(fields);
    }

    /**
     * Returns the value of {@link #weekFieldsProperty()}.
     *
     * @return the week fields
     */
    public final WeekFields getWeekFields() {
        return weekFieldsProperty().get();
    }

    
    /**
     * Set the expected values for TimeRange combos. <br>
     * If dialog is opened for the first time or user hasn't made any change 
     * It will calculate the values based on {@link CalendarView#dateProperty()}.
     * Otherwise, It will search on OldValuesMap in order to mantain in combos 
     * what user does while dialog is opened. <br>
     * 
     * When dialog get closed, old values get cleaned.
     * 
     * @param date Used to calculate values of combos when dialog is 
     * opened for the first time.
     */
    public final void loadDropDownValues(LocalDate date) {
        TimeRangeOldValues oldValue = oldValuesMap.get(getViewType());
        if (date == null) {
            return;
        }

        switch (getViewType()) {
        case DAY_VIEW:

            // Will enter here if user has made some changes in Time range
            // combos for DAY_VIEW type.
            if (oldValue != null) {

                startField.setValue(oldValue.startField.getValue());
                endField.setValue(oldValue.endField.getValue());

                if (startField.getValue() == TimeRangeFieldValue.ON_DATE) {
                    startField.setOnDate(oldValue.startField.getOnDate());
                }

                if (endField.getValue() == TimeRangeFieldValue.ON_DATE) {
                    endField.setOnDate(oldValue.endField.getOnDate());
                } else if (endField.getValue() == TimeRangeFieldValue.AFTER) {
                    endField.setAfterUnits(oldValue.endField.getAfterUnits());
                }
            } else {

                if (date.equals(getToday())) {
                    startField.setValue(TimeRangeField.TimeRangeFieldValue.TODAY);
                } else if (date.equals(getTomorrow())) {
                    startField.setValue(TimeRangeField.TimeRangeFieldValue.TOMORROW);
                } else {
                    startField.setValue(TimeRangeField.TimeRangeFieldValue.ON_DATE);
                    startField.setOnDate(date);
                }
                endField.setValue(TimeRangeField.TimeRangeFieldValue.AFTER);
            }
            break;

        case WEEK_VIEW:
            
            // Will enter here if user has made some changes in Time range
            // combos for WEEK_VIEW type.
            if (oldValue != null){
                
                startField.setValue(oldValue.startField.getValue());
                endField.setValue(oldValue.endField.getValue());
                
                if (startField.getValue() == TimeRangeFieldValue.ON_DATE) {
                    startField.setOnDate(oldValue.startField.getOnDate());
                } else if (startField.getValue() == TimeRangeFieldValue.ON_WEEK_NUMBER) {
                    startField.setOnWeekNumber(oldValue.startField.getOnWeekNumber());
                }
                
                if (endField.getValue() == TimeRangeFieldValue.ON_DATE) {
                    endField.setOnDate(oldValue.endField.getOnDate());
                } else if (endField.getValue() == TimeRangeFieldValue.ON_WEEK_NUMBER) {
                    endField.setOnWeekNumber(oldValue.endField.getOnWeekNumber());
                } else if (endField.getValue() == TimeRangeFieldValue.AFTER) {
                    endField.setAfterUnits(oldValue.endField.getAfterUnits());
                }
            }else {
                if (isCurrentWeek(date)) {
                    startField
                            .setValue(TimeRangeField.TimeRangeFieldValue.THIS_WEEK);
                } else if (isNextWeek(date)) {
                    startField
                            .setValue(TimeRangeField.TimeRangeFieldValue.NEXT_WEEK);
                } else {
                    startField.setValue(TimeRangeField.TimeRangeFieldValue.ON_DATE);
                    startField.setOnDate(date);
                }
                endField.setValue(TimeRangeFieldValue.AFTER);    
            }
            break;

        case MONTH_VIEW:
            
            // Will enter here if user has made some changes in Time range
            // combos for MONTH_VIEW type.
            if(oldValue != null){
                
                startField.setValue(oldValue.startField.getValue());
                endField.setValue(oldValue.endField.getValue());
                
                if (startField.getValue() != TimeRangeField.TimeRangeFieldValue.THIS_MONTH
                        && startField.getValue() != TimeRangeField.TimeRangeFieldValue.NEXT_MONTH) {
                    startField.setMonthYear(oldValue.startField.getMonthYear());
                }
                
                if (endField.getValue() == TimeRangeFieldValue.AFTER) {
                    endField.setAfterUnits(oldValue.endField.getAfterUnits());
                } else if (endField.getValue() != TimeRangeField.TimeRangeFieldValue.THIS_MONTH
                        && startField.getValue() != TimeRangeField.TimeRangeFieldValue.NEXT_MONTH) {
                    endField.setMonthYear(oldValue.endField.getMonthYear());
                }
            }else {
                if (isCurrentMonth(date)) {
                    startField.setValue(TimeRangeField.TimeRangeFieldValue.THIS_MONTH);
                } else if (isNextMonth(date)) {
                    startField.setValue(TimeRangeField.TimeRangeFieldValue.NEXT_MONTH);
                } else {
                    TimeRangeField.TimeRangeFieldValue month = TimeRangeField.TimeRangeFieldValue
                            .getFromMonth(date.getMonth());
                    int year = date.getYear();
                    startField.setValue(month);
                    startField.setMonthYear(year);
                }
                endField.setValue(TimeRangeFieldValue.AFTER);
            }
            break;

        default:
            throw new UnsupportedOperationException(
                    "Not supported yet!: " + getViewType());
        }

        startDate.addListener(OldValuesListener);
        endDate.addListener(OldValuesListener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimeRangeViewSkin(this);
    }

    public final TimeRangeField getStartField() {
        return startField;
    }

    public final TimeRangeField getEndField() {
        return endField;
    }

    private final ReadOnlyObjectWrapper<LocalDate> startDate = new ReadOnlyObjectWrapper<>(
            this, "startDate");

    public final ReadOnlyObjectProperty<LocalDate> startDateProperty() {
        return startDate.getReadOnlyProperty();
    }

    public final LocalDate getStartDate() {
        return startDate.get();
    }

    private void setStartDate(LocalDate startDate) {
        if (!Objects.requireNonNull(startDate).equals(getStartDate())) {
            this.startDate.set(startDate);
        }
    }

    private LocalDate getTomorrow() {
        return getToday().plusDays(1);
    }

    private LocalDate getThisWeekDate() {
        return getStartOfWeek(getToday());
    }

    private LocalDate getNextWeekDate() {
        return getThisWeekDate().plusWeeks(1);
    }

    private LocalDate getStartOfWeek(LocalDate date) {
        return Util.adjustToFirstDayOfWeek(date,
                getWeekFields().getFirstDayOfWeek());
    }

    private LocalDate getEndOfWeek(LocalDate date) {
        return Util.adjustToLastDayOfWeek(date,
                getWeekFields().getFirstDayOfWeek());
    }

    private boolean isCurrentWeek(LocalDate date) {
        return date.compareTo(getStartOfWeek(getToday())) >= 0
                && date.compareTo(getEndOfWeek(getToday())) <= 0;
    }

    private boolean isNextWeek(LocalDate date) {
        return date.compareTo(getStartOfWeek(getNextWeekDate())) >= 0
                && date.compareTo(getEndOfWeek(getNextWeekDate())) <= 0;
    }

    private LocalDate getThisMonthDate() {
        return getStartOfMonth(getToday());
    }

    private LocalDate getNextMonthDate() {
        return getThisMonthDate().plusMonths(1);
    }

    private LocalDate getStartOfMonth(LocalDate date) {
        return YearMonth.of(date.getYear(), date.getMonthValue()).atDay(1);
    }

    private LocalDate getEndOfMonth(LocalDate date) {
        return YearMonth.of(date.getYear(), date.getMonthValue())
                .atEndOfMonth();
    }

    private boolean isCurrentMonth(LocalDate date) {
        return date.getYear() == getToday().getYear()
                && date.getMonth().equals(getToday().getMonth());
    }

    private boolean isNextMonth(LocalDate date) {
        return date.getYear() == getToday().getYear()
                && date.getMonth().equals(getNextMonthDate().getMonth());
    }

    private void updateStartDate() {
        switch (startField.getValue()) {
        case TODAY:
            setStartDate(getToday());
            break;

        case TOMORROW:
            setStartDate(getTomorrow());
            break;

        case ON_DATE:
            if (getViewType() == ViewType.DAY_VIEW) {
                setStartDate(startField.getOnDate());
            } else {
                setStartDate(getStartOfWeek(startField.getOnDate()));
            }
            break;

        case THIS_WEEK:
            setStartDate(getThisWeekDate());
            break;

        case NEXT_WEEK:
            setStartDate(getNextWeekDate());
            break;

        case ON_WEEK_NUMBER:
            int startWeekNumber = getToday()
                    .get(getWeekFields().weekOfWeekBasedYear());
            int diff = startField.getOnWeekNumber() - startWeekNumber;
            setStartDate(
                    getToday().plusWeeks(diff).with(ChronoField.DAY_OF_WEEK,
                            getWeekFields().getFirstDayOfWeek().getValue()));
            break;

        case THIS_MONTH:
            setStartDate(getThisMonthDate());
            break;

        case NEXT_MONTH:
            setStartDate(getNextMonthDate());
            break;

        case JANUARY:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.JANUARY.getValue())
                    .atDay(1));
            break;

        case FEBRUARY:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.FEBRUARY.getValue())
                    .atDay(1));
            break;

        case MARCH:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.MARCH.getValue())
                    .atDay(1));
            break;

        case APRIL:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.APRIL.getValue())
                    .atDay(1));
            break;

        case MAY:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.MAY.getValue())
                    .atDay(1));
            break;

        case JUNE:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.JUNE.getValue())
                    .atDay(1));
            break;

        case JULY:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.JULY.getValue())
                    .atDay(1));
            break;

        case AUGUST:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.AUGUST.getValue())
                    .atDay(1));
            break;

        case SEPTEMBER:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.SEPTEMBER.getValue())
                    .atDay(1));
            break;

        case OCTOBER:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.OCTOBER.getValue())
                    .atDay(1));
            break;

        case NOVEMBER:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.NOVEMBER.getValue())
                    .atDay(1));
            break;

        case DECEMBER:
            setStartDate(YearMonth
                    .of(startField.getMonthYear(), Month.DECEMBER.getValue())
                    .atDay(1));
            break;

        default:
            throw new UnsupportedOperationException("Value not supported!");
        }
    }

    private final ReadOnlyObjectWrapper<LocalDate> endDate = new ReadOnlyObjectWrapper<>(
            this, "endDate");

    public final ReadOnlyObjectProperty<LocalDate> endDateProperty() {
        return endDate.getReadOnlyProperty();
    }

    public final LocalDate getEndDate() {
        return endDate.get();
    }

    private void setEndDate(LocalDate endDate) {
        if (!Objects.requireNonNull(endDate).equals(getEndDate())) {
            this.endDate.set(endDate);
        }
    }

    private void updateEndDate() {
        switch (endField.getValue()) {
        case TODAY:
            setEndDate(getToday());
            break;

        case TOMORROW:
            setEndDate(getTomorrow());
            break;

        case ON_DATE:
            if (getViewType() == ViewType.DAY_VIEW) {
                setEndDate(endField.getOnDate());
            } else {
                setEndDate(getEndOfWeek(endField.getOnDate()));
            }
            break;

        case THIS_WEEK:
            setEndDate(getEndOfWeek(getToday()));
            break;

        case NEXT_WEEK:
            setEndDate(getEndOfWeek(getToday()).plusWeeks(1));
            break;

        case ON_WEEK_NUMBER:
            int startWeekNumber = getToday()
                    .get(getWeekFields().weekOfWeekBasedYear());
            int difference = endField.getOnWeekNumber() - startWeekNumber;
            setEndDate(getToday().plusWeeks(difference).with(
                    ChronoField.DAY_OF_WEEK,
                    getWeekFields().getFirstDayOfWeek().plus(6).getValue()));
            break;

        case THIS_MONTH:
            setEndDate(getEndOfMonth(getToday()));
            break;

        case NEXT_MONTH:
            setEndDate(getEndOfMonth(getToday()).plusMonths(1));
            break;

        case JANUARY:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.JANUARY.getValue())
                    .atEndOfMonth());
            break;

        case FEBRUARY:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.FEBRUARY.getValue())
                    .atEndOfMonth());
            break;

        case MARCH:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.MARCH.getValue())
                    .atEndOfMonth());
            break;

        case APRIL:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.APRIL.getValue())
                    .atEndOfMonth());
            break;

        case MAY:
            setEndDate(
                    YearMonth.of(endField.getMonthYear(), Month.MAY.getValue())
                            .atEndOfMonth());
            break;

        case JUNE:
            setEndDate(
                    YearMonth.of(endField.getMonthYear(), Month.JUNE.getValue())
                            .atEndOfMonth());
            break;

        case JULY:
            setEndDate(
                    YearMonth.of(endField.getMonthYear(), Month.JULY.getValue())
                            .atEndOfMonth());
            break;

        case AUGUST:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.AUGUST.getValue())
                    .atEndOfMonth());
            break;

        case SEPTEMBER:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.SEPTEMBER.getValue())
                    .atEndOfMonth());
            break;

        case OCTOBER:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.OCTOBER.getValue())
                    .atEndOfMonth());
            break;

        case NOVEMBER:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.NOVEMBER.getValue())
                    .atEndOfMonth());
            break;

        case DECEMBER:
            setEndDate(YearMonth
                    .of(endField.getMonthYear(), Month.DECEMBER.getValue())
                    .atEndOfMonth());
            break;

        case AFTER:
            int units = endField.getAfterUnits();
            if (getViewType() == ViewType.DAY_VIEW) {
                setEndDate(getStartDate().plusDays(units - 1L));
            } else if (getViewType() == ViewType.WEEK_VIEW) {
                setEndDate(getEndOfWeek(getStartDate()).plusWeeks(units - 1L));
            } else {
                setEndDate(getEndOfMonth(getStartDate()).plusMonths(units - 1L));
            }
            break;

        default:
            throw new UnsupportedOperationException("Value not supported!");
        }
    }

    private void fixEndField() {
        LocalDate mStartDate = getStartDate();
        LocalDate mEndDate = getEndDate();

        if (mStartDate == null || mEndDate == null) {
            return;
        }

        mEndDate = endField.getValue() == TimeRangeFieldValue.AFTER ? mStartDate
                : mEndDate;

        if (mEndDate.isBefore(mStartDate)) {
            endField.setValue(startField.getValue());

            if (startField.getOnDate() != null) {
                endField.setOnDate(startField.getOnDate());
            } else if (startField.getOnWeekNumber() != null) {
                endField.setOnWeekNumber(startField.getOnWeekNumber());
            } else if (startField.getMonthYear() != null) {
                endField.setMonthYear(startField.getMonthYear());
            } else if (startField.getAfterUnits() != null) {
                endField.setAfterUnits(startField.getAfterUnits());
            }
        }
    }

    private void fixStartField() {
        LocalDate mStartDate = getStartDate();
        LocalDate mEndDate = getEndDate();

        if (mStartDate == null || mEndDate == null) {
            return;
        }

        if (mEndDate.isBefore(mStartDate)) {
            startField.setValue(endField.getValue());

            if (startField.getOnDate() != null) {
                startField.setOnDate(endField.getOnDate());
            } else if (endField.getOnWeekNumber() != null) {
                startField.setOnWeekNumber(endField.getOnWeekNumber());
            } else if (endField.getMonthYear() != null) {
                startField.setMonthYear(endField.getMonthYear());
            } else if (endField.getAfterUnits() != null) {
                startField.setAfterUnits(endField.getAfterUnits());
            }
        }
    }

    private final ReadOnlyIntegerWrapper unitsToPrint = new ReadOnlyIntegerWrapper(
            this, "unitsToPrint");

    public final ReadOnlyIntegerProperty unitsToPrintProperty() {
        return unitsToPrint.getReadOnlyProperty();
    }

    public final int getUnitsToPrint() {
        return unitsToPrint.get();
    }

    private void setUnitsToPrint(int unitsToPrint) {
        this.unitsToPrint.set(unitsToPrint);
    }

    private void updateUnitsToPrint() {
        LocalDate mStartDate = getStartDate();
        LocalDate mEndDate = getEndDate();

        if (mStartDate == null || mEndDate == null) {
            return;
        }

        Duration duration = Duration.between(mStartDate.atTime(LocalTime.MIN),
                mEndDate.atStartOfDay().plusDays(1));
        long days = duration.toDays();

        if (days > 0) {
            switch (getViewType()) {
            case DAY_VIEW:
                setUnitsToPrint((int) days);
                break;

            case WEEK_VIEW:
                setUnitsToPrint((int) Math.round((double) days / 7));
                break;

            case MONTH_VIEW:
                setUnitsToPrint((int) Math.round((double) days / 30));
                break;

            default:
                throw new UnsupportedOperationException(
                        "Not supported yet!: " + getViewType());
            }
        } else {
            setUnitsToPrint(0);
        }
    }

    public void cleanOldValues() {
        oldValuesMap.replace(ViewType.DAY_VIEW, null);
        oldValuesMap.replace(ViewType.WEEK_VIEW, null);
        oldValuesMap.replace(ViewType.MONTH_VIEW, null);
        startDate.removeListener(OldValuesListener);
        endDate.removeListener(OldValuesListener);
    }

    private void catchOldValues() {

        TimeRangeOldValues value = oldValuesMap.get(getViewType());
        if (value == null) {
            value = new TimeRangeOldValues();
        }

        value.startField.setValue(startField.getValue());
        value.startField.setOnDate(startField.getOnDate());
        value.startField.setOnWeekNumber(startField.getOnWeekNumber());
        value.startField.setMonthYear(startField.getMonthYear());

        value.endField.setValue(endField.getValue());
        value.endField.setOnDate(endField.getOnDate());
        value.endField.setOnWeekNumber(endField.getOnWeekNumber());
        value.endField.setMonthYear(endField.getMonthYear());
        value.endField.setAfterUnits(endField.getAfterUnits());

        oldValuesMap.replace(getViewType(), value);
    }

    private class TimeRangeOldValues {

        private TimeRangeField startField;
        private TimeRangeField endField;

        public TimeRangeOldValues() {
            startField = new TimeRangeField();
            endField = new TimeRangeField();
        }
    }

}
