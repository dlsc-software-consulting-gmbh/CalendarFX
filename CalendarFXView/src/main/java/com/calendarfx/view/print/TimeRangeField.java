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

import impl.com.calendarfx.view.print.TimeRangeFieldSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import org.controlsfx.control.PropertySheet.Item;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A control for specifying the start or end time of a time range. This control is used
 * by the {@link TimeRangeView} as part of the print preview functionality. It allows the
 * user to specify the time range that has to be printed. The control supports defining
 * time points for day ranges, week rangers, or month ranges. The default style class used
 * by this control is "time-range-field".
 *
 * <center><img src="doc-files/time-range-field.png"></center>
 */
public class TimeRangeField extends ViewTypeControl {

    public static final String DEFAULT_STYLE = "time-range-field";

    public TimeRangeField() {
        this(ViewType.DAY_VIEW);
    }

    public TimeRangeField(ViewType viewType) {
        this(viewType, false);
    }

    public TimeRangeField(boolean endField) {
        this(ViewType.DAY_VIEW, endField);
    }

    public TimeRangeField(ViewType viewType, boolean endField) {
        getStyleClass().add(DEFAULT_STYLE);

        InvalidationListener updateValuesListener = obs -> updateValues();
        viewTypeProperty().addListener(updateValuesListener);
        endFieldProperty().addListener(updateValuesListener);

        valueProperty().addListener(obs -> updateFields());

        setViewType(viewType);
        setEndField(endField);

        updateValues();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimeRangeFieldSkin(this);
    }

    private final ObjectProperty<LocalDate> today = new SimpleObjectProperty<>(this, "today", LocalDate.now()); //$NON-NLS-1$

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
     * @param date the date representing "today"
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

    private final ObjectProperty<WeekFields> weekFields = new SimpleObjectProperty<>(this, "weekFields", WeekFields.ISO); //$NON-NLS-1$

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
     * @param fields the new week fields
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

    private void updateValues() {
        values.setAll(TimeRangeFieldValue.getSelectablesForView(getViewType(), isEndField()));
        setValue(values.get(0));
    }

    private void updateFields() {
        if (getValue() == TimeRangeFieldValue.ON_DATE) {
            if (getOnDate() == null) {
                setOnDate(getToday());
            }
            setOnWeekNumber(null);
            setMonthYear(null);
            setAfterUnits(null);
        } else if (getValue() == TimeRangeFieldValue.ON_WEEK_NUMBER) {
            if (getOnWeekNumber() == null) {
                setOnWeekNumber(getToday().get(getWeekFields().weekOfWeekBasedYear()));
            }
            setOnDate(null);
            setMonthYear(null);
            setAfterUnits(null);
        } else if (getValue() == TimeRangeFieldValue.AFTER) {
            if (getAfterUnits() == null) {
                setAfterUnits(1);
            }
            setOnDate(null);
            setOnWeekNumber(null);
            setMonthYear(null);
        } else if (getValue().isMonthValue()) {
            if (getMonthYear() == null) {
                setMonthYear(getToday().getYear());
            }
            setOnDate(null);
            setOnWeekNumber(null);
            setAfterUnits(null);
        } else {
            setOnDate(null);
            setOnWeekNumber(null);
            setMonthYear(null);
            setAfterUnits(null);
        }
    }

    // end field support

    private final BooleanProperty endField = new SimpleBooleanProperty(this, "endField");

    public final BooleanProperty endFieldProperty() {
        return endField;
    }

    public final boolean isEndField() {
        return endFieldProperty().get();
    }

    public final void setEndField(boolean endField) {
        endFieldProperty().set(endField);
    }

    private final ObservableList<TimeRangeFieldValue> values = FXCollections.observableArrayList();

    private final ObservableList<TimeRangeFieldValue> valuesUnmodifiable = FXCollections.unmodifiableObservableList(values);

    public ObservableList<TimeRangeFieldValue> getValues() {
        return valuesUnmodifiable;
    }

    // value support

    private final ObjectProperty<TimeRangeFieldValue> value = new SimpleObjectProperty<TimeRangeFieldValue>(this, "value") {
        @Override
        public void set(TimeRangeFieldValue newValue) {
            super.set(requireNonNull(newValue));
        }
    };

    public final ObjectProperty<TimeRangeFieldValue> valueProperty() {
        return value;
    }

    public final TimeRangeFieldValue getValue() {
        return valueProperty().get();
    }

    public final void setValue(TimeRangeFieldValue value) {
        valueProperty().set(value);
    }

    // on date support

    private final ObjectProperty<LocalDate> onDate = new SimpleObjectProperty<>(this, "onDate");

    public final ObjectProperty<LocalDate> onDateProperty() {
        return onDate;
    }

    public final LocalDate getOnDate() {
        return onDateProperty().get();
    }

    public final void setOnDate(LocalDate onDate) {
        onDateProperty().set(onDate);
    }

    // on week support

    private final ObjectProperty<Integer> onWeekNumber = new SimpleObjectProperty<>(this, "onWeekNumber");

    public final ObjectProperty<Integer> onWeekNumberProperty() {
        return onWeekNumber;
    }

    public final Integer getOnWeekNumber() {
        return onWeekNumberProperty().get();
    }

    public final void setOnWeekNumber(Integer onWeekNumber) {
        onWeekNumberProperty().set(onWeekNumber);
    }

    // month year support

    private final ObjectProperty<Integer> monthYear = new SimpleObjectProperty<>(this, "monthYear");

    public final ObjectProperty<Integer> monthYearProperty() {
        return monthYear;
    }

    public final Integer getMonthYear() {
        return monthYearProperty().get();
    }

    public final void setMonthYear(Integer monthYear) {
        monthYearProperty().set(monthYear);
    }

    // after units support

    private final ObjectProperty<Integer> afterUnits = new SimpleObjectProperty<>(this, "afterUnits");

    public final ObjectProperty<Integer> afterUnitsProperty() {
        return afterUnits;
    }

    public final Integer getAfterUnits() {
        return afterUnitsProperty().get();
    }

    public final void setAfterUnits(Integer afterUnits) {
        afterUnitsProperty().set(afterUnits);
    }

    public enum TimeRangeFieldValue {

        TODAY(1, "TimeRangeFieldValue.TODAY_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.DAY_VIEW);
            }
        },

        TOMORROW(2, "TimeRangeFieldValue.TOMORROW_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.DAY_VIEW);
            }
        },

        THIS_WEEK(3, "TimeRangeFieldValue.THIS_WEEK_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.WEEK_VIEW);
            }
        },

        NEXT_WEEK(4, "TimeRangeFieldValue.NEXT_WEEK_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.WEEK_VIEW);
            }
        },

        ON_WEEK_NUMBER(5, "TimeRangeFieldValue.ON_WEEK_NUMBER_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.WEEK_VIEW);
            }
        },

        THIS_MONTH(6, "TimeRangeFieldValue.THIS_MONTH_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        NEXT_MONTH(7, "TimeRangeFieldValue.NEXT_MONTH_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        JANUARY(8, "TimeRangeFieldValue.JANUARY_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        FEBRUARY(9, "TimeRangeFieldValue.FEBRUARY_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        MARCH(10, "TimeRangeFieldValue.MARCH_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        APRIL(11, "TimeRangeFieldValue.APRIL_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        MAY(12, "TimeRangeFieldValue.MAY_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        JUNE(13, "TimeRangeFieldValue.JUNE_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        JULY(14, "TimeRangeFieldValue.JULY_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        AUGUST(15, "TimeRangeFieldValue.AUGUST_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },


        SEPTEMBER(16, "TimeRangeFieldValue.SEPTEMBER_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },


        OCTOBER(17, "TimeRangeFieldValue.OCTOBER_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },


        NOVEMBER(18, "TimeRangeFieldValue.NOVEMBER_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },


        DECEMBER(19, "TimeRangeFieldValue.DECEMBER_LABEL") {
            @Override
            public boolean isMonthValue() {
                return true;
            }

            @Override
            public Collection<ViewType> getViewTypes() {
                return Collections.singletonList(ViewType.MONTH_VIEW);
            }
        },

        ON_DATE(20, "TimeRangeFieldValue.ON_DATE_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Arrays.asList(ViewType.DAY_VIEW, ViewType.WEEK_VIEW);
            }
        },

        AFTER(21, "TimeRangeFieldValue.AFTER_LABEL") {
            @Override
            public Collection<ViewType> getViewTypes() {
                return Arrays.asList(ViewType.DAY_VIEW, ViewType.WEEK_VIEW, ViewType.MONTH_VIEW);
            }
        };

        private int order;
        private String messageKey;

        TimeRangeFieldValue(int order, String messageKey) {
            this.order = order;
            this.messageKey = messageKey;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public boolean isMonthValue() {
            return false;
        }

        public boolean isSelectableForView(ViewType viewType) {
            return getViewTypes().contains(viewType);
        }

        public abstract Collection<ViewType> getViewTypes();

        public static List<TimeRangeFieldValue> getSelectablesForView(ViewType viewType, boolean endField) {
            List<TimeRangeFieldValue> allowedValues = new ArrayList<>();

            for (TimeRangeFieldValue value : values()) {
                if (value.isSelectableForView(viewType)) {
                    allowedValues.add(value);
                }
            }

            if (!endField) {
                allowedValues.remove(AFTER);
            }

            allowedValues.sort(Comparator.comparingInt(v -> v.order));

            return allowedValues;
        }

        public static TimeRangeFieldValue getFromMonth(Month month) {
            switch (month) {
                case JANUARY:
                    return TimeRangeFieldValue.JANUARY;

                case FEBRUARY:
                    return TimeRangeFieldValue.FEBRUARY;

                case MARCH:
                    return TimeRangeFieldValue.MARCH;

                case APRIL:
                    return TimeRangeFieldValue.APRIL;

                case MAY:
                    return TimeRangeFieldValue.MAY;

                case JUNE:
                    return TimeRangeFieldValue.JUNE;

                case JULY:
                    return TimeRangeFieldValue.JULY;

                case AUGUST:
                    return TimeRangeFieldValue.AUGUST;

                case SEPTEMBER:
                    return TimeRangeFieldValue.SEPTEMBER;

                case OCTOBER:
                    return TimeRangeFieldValue.OCTOBER;

                case NOVEMBER:
                    return TimeRangeFieldValue.NOVEMBER;

                default:
                    return TimeRangeFieldValue.DECEMBER;
            }
        }
    }

    private static final String TIME_RANGE_FIELD_CATEGORY = "Time Range Field";

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {
            @Override
            public void setValue(Object value) {
                setEndField((boolean) value);
            }

            @Override
            public Object getValue() {
                return isEndField();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(endFieldProperty());
            }

            @Override
            public String getName() {
                return "End Field";
            }

            @Override
            public String getDescription() {
                return "Indicates this field is used for te end value";
            }

            @Override
            public String getCategory() {
                return TIME_RANGE_FIELD_CATEGORY;
            }
        });

        return items;
    }

}
