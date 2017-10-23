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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import org.controlsfx.control.PropertySheet.Item;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

/**
 * Common superclass for views showing a month (e.g. {@link MonthView},
 * {@link YearMonthView}).
 */
public abstract class MonthViewBase extends DateControl {

    /**
     * Constructs a new month view.
     */
    protected MonthViewBase() {
        dateProperty().addListener(evt -> yearMonth.set(YearMonth.from(getDate())));
        selectionModeProperty().addListener(evt -> getSelectedDates().clear());
    }

    private final ReadOnlyObjectWrapper<YearMonth> yearMonth = new ReadOnlyObjectWrapper<>(
            this, "yearMonth", YearMonth.from(getToday())); //$NON-NLS-1$

    /**
     * Stores the year and month shown by the control.
     *
     * @return the year and month
     */
    public final ReadOnlyObjectProperty<YearMonth> yearMonthProperty() {
        return yearMonth.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #yearMonthProperty()}.
     *
     * @return the year and month
     */
    public final YearMonth getYearMonth() {
        return yearMonthProperty().get();
    }

    private final BooleanProperty showWeeks = new SimpleBooleanProperty(this,
            "showWeeks", true); //$NON-NLS-1$

    /**
     * Controls whether the view will show week numbers. The image below shows
     * an example (weeks 10, 11, 12 in 2015):
     * <p/>
     * <center><img src="doc-files/week-numbers.png"></center>
     * <p/>
     *
     * @return true if week numbers are shown
     */
    public final BooleanProperty showWeekNumbersProperty() {
        return showWeeks;
    }

    /**
     * Sets the value of {@link #showWeekNumbersProperty()}.
     *
     * @param show
     *            if true will show week numbers
     */
    public final void setShowWeekNumbers(boolean show) {
        showWeekNumbersProperty().set(show);
    }

    /**
     * Returns the value of {@link #showWeekNumbersProperty()}.
     *
     * @return true if week numbers will be shown
     */
    public final boolean isShowWeekNumbers() {
        return showWeekNumbersProperty().get();
    }

    private final ObservableSet<LocalDate> selectedDates = FXCollections
            .observableSet();

    /**
     * The selected dates.
     *
     * @return the selected dates
     */
    public final ObservableSet<LocalDate> getSelectedDates() {
        return selectedDates;
    }

    private static final String MONTH_VIEW_CATEGORY = "Month View Base"; //$NON-NLS-1$

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {
            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showWeekNumbersProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowWeekNumbers((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowWeekNumbers();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Week Numbers"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Show or hide the week numbers"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_VIEW_CATEGORY;
            }
        });

        return items;
    }
}
