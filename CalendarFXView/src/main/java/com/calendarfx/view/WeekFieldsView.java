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

import impl.com.calendarfx.view.WeekFieldsViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import org.controlsfx.control.PropertySheet;

import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.Optional;

/**
 * A control used for editing the values of a {@link WeekFields} instance. These fields
 * are used for determine which weekday is considered the first day of the week. They also
 * store the minimum number of days that are required by the first week of the year in order
 * to be actually called a full week.
 */
public class WeekFieldsView extends CalendarFXControl {

    private static final String DEFAULT_STYLE_CLASS = "week-fields";

    /**
     * Constructs a new view.
     */
    public WeekFieldsView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        weekFieldsProperty.addListener(it -> updateReadOnyProperties());
        updateReadOnyProperties();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WeekFieldsViewSkin(this);
    }

    private final ObjectProperty<WeekFields> weekFieldsProperty = new SimpleObjectProperty<WeekFields>(this, "weekFields", WeekFields.ISO) {
        @Override
        public void setValue(WeekFields v) {
            if (v == null) {
                throw new IllegalArgumentException("week fields value can not be null");
            }
            super.setValue(v);
        }
    };

    /**
     * The property used to store the current value of this view.
     *
     * @return the view's value / a week fields instance
     */
    public final ObjectProperty<WeekFields> weekFieldsProperty() {
        return weekFieldsProperty;
    }

    /**
     * Returns the value of {@link #weekFieldsProperty()}.
     *
     * @return the view's value / a week fields instance
     */
    public final WeekFields getWeekFields() {
        return weekFieldsProperty.get();
    }

    /**
     * Sets the value of {@link #weekFieldsProperty()}.
     *
     * @param fields the view's value / a week fields instance
     */
    public final void setWeekFields(WeekFields fields) {
        this.weekFieldsProperty.set(fields);
    }

    private void updateReadOnyProperties() {
        WeekFields fields = getWeekFields();
        firstDayOfWeek.set(fields.getFirstDayOfWeek());
        minimalDaysInFirstWeek.set(fields.getMinimalDaysInFirstWeek());
    }

    // day of week support

    private final ReadOnlyObjectWrapper<DayOfWeek> firstDayOfWeek = new ReadOnlyObjectWrapper<>(this, "firstDayOfWeek");

    /**
     * A read-only property storing the "first day of week" as specified by {@link #weekFieldsProperty()}.
     *
     * @return the first day of the week
     */
    public final ReadOnlyObjectProperty<DayOfWeek> firstDayOfWeekProperty() {
        return firstDayOfWeek.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #firstDayOfWeekProperty()}.
     *
     * @return the first day of the week
     */
    public final DayOfWeek getFirstDayOfWeek() {
        return firstDayOfWeek.get();
    }

    // minimal days in first week support

    private final ReadOnlyIntegerWrapper minimalDaysInFirstWeek = new ReadOnlyIntegerWrapper(this, "minimalDaysInFirstWeek");

    /**
     * A read-only property storing the "minimal days in first week" as specified by {@link #weekFieldsProperty()}.
     *
     * @return the minimal days in first week value
     */
    public final ReadOnlyIntegerProperty minimalDaysInFirstWeekProperty() {
        return minimalDaysInFirstWeek.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #minimalDaysInFirstWeekProperty()}.
     *
     * @return the minimal days in first week value
     */
    public int getMinimalDaysInFirstWeek() {
        return minimalDaysInFirstWeek.get();
    }

    @Override
    public ObservableList<PropertySheet.Item> getPropertySheetItems() {
        ObservableList<PropertySheet.Item> items = super.getPropertySheetItems();

        items.add(new PropertySheet.Item() {

            @Override
            public Class<?> getType() {
                return WeekFields.class;
            }

            @Override
            public String getCategory() {
                return "Week Fields View";
            }

            @Override
            public String getName() {
                return "Week Fields";
            }

            @Override
            public String getDescription() {
                return "Week Fields";
            }

            @Override
            public Object getValue() {
                return WeekFieldsView.this.getWeekFields();
            }

            @Override
            public void setValue(Object o) {
                WeekFieldsView.this.setWeekFields((WeekFields) o);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(weekFieldsProperty);
            }
        });

        return items;
    }
}
