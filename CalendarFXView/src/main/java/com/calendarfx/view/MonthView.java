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

import com.calendarfx.model.Entry;
import impl.com.calendarfx.view.MonthViewSkin;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet.Item;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Visualizes an entire month including the last days of the previous month and
 * the first days of the next month. Each day is shown in its own box with its
 * entries. These entries are of type {@link MonthEntryView}.
 *
 *
 * <img width="823" src="doc-files/month-view.png" alt="Month View">
 *
 */
public class MonthView extends MonthViewBase implements ZonedDateTimeProvider {

    private static final String DEFAULT_STYLE_CLASS = "month-view";

    /**
     * Constructs a new month view.
     */
    public MonthView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setEntryViewFactory(MonthEntryView::new);

        new CreateAndDeleteHandler(this);

        getSelectedDates().addListener((Observable it) -> {
            if (getSelectedDates().size() == 1) {
                LocalDate date = getSelectedDates().iterator().next();
                setDate(date);
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MonthViewSkin(this);
    }

    public MonthViewSkin skin;

    @Override
    public final ZonedDateTime getZonedDateTimeAt(double x, double y, ZoneId zoneId) {
        MonthViewSkin skin = (MonthViewSkin) getSkin();
        return skin.getZonedDateTimeAt(x, y, zoneId);
    }

    private final BooleanProperty showCurrentWeek = new SimpleBooleanProperty(
            this, "showCurrentWeek", true);

    /**
     * Controls whether the view will highlight the current week. The image
     * below shows an example:
     *
     * <img src="doc-files/current-week.png" alt="Current Week">
     *
     * @see DateControl#todayProperty()
     *
     * @return true if the current week will be highlighted
     */
    public final BooleanProperty showCurrentWeekProperty() {
        return showCurrentWeek;
    }

    /**
     * Sets the value of {@link #showCurrentWeekProperty()}.
     *
     * @param show
     *            if true will highlight the current week
     */
    public final void setShowCurrentWeek(boolean show) {
        showCurrentWeekProperty().set(show);
    }

    /**
     * Returns the value of {@link #showCurrentWeekProperty()}.
     *
     * @return true if the current week will be highlighted
     */
    public final boolean isShowCurrentWeek() {
        return showCurrentWeekProperty().get();
    }

    private final BooleanProperty showWeekends = new SimpleBooleanProperty(
            this, "showWeekends", true);

    /**
     * Controls whether the view will show weekend days differently than regular
     * week days. The image below shows an example:
     *
     * <img src="doc-files/weekend.png" alt="Weekend">
     *
     * @return true if the weekend days will be shown differently
     */
    public final BooleanProperty showWeekendsProperty() {
        return showWeekends;
    }

    /**
     * Sets the value of {@link #showWeekendsProperty()}.
     *
     * @param show
     *            if true will show weekend days differently
     */
    public final void setShowWeekends(boolean show) {
        showWeekendsProperty().set(show);
    }

    /**
     * Returns the value of {@link #showWeekendsProperty()}.
     *
     * @return true if weekend days will be shown differently
     */
    public final boolean isShowWeekends() {
        return showWeekendsProperty().get();
    }

    private final BooleanProperty showWeekdays = new SimpleBooleanProperty(
            this, "showWeekdays", true);

    /**
     * Controls whether the view will show the names of the week days ("Mo",
     * "Tu", "We", ...). The image below shows an example:
     *
     * <img src="doc-files/weekdays.png" alt="Weekdays">
     *
     * @return true if the week day names will be shown
     */
    public final BooleanProperty showWeekdaysProperty() {
        return showWeekdays;
    }

    /**
     * Sets the value of {@link #showWeekdaysProperty()}.
     *
     * @param show
     *            if true will show the names of the week days
     */
    public final void setShowWeekdays(boolean show) {
        showWeekdaysProperty().set(show);
    }

    /**
     * Returns the value of {@link #showWeekdaysProperty()}.
     *
     * @return true if week day names will be shown
     */
    public final boolean isShowWeekdays() {
        return showWeekdaysProperty().get();
    }

    private final BooleanProperty showTimedEntries = new SimpleBooleanProperty(this,
            "showTimedEntries", true);

    /**
     * Controls whether the view will show calendar entries that are not "full-day" entries
     * (e.g. "soccer training from 6pm till 8pm").
     *
     * @return true if timed entries will be shown
     */
    public final BooleanProperty showTimedEntriesProperty() {
        return showTimedEntries;
    }

    /**
     * Sets the value of {@link #showTimedEntriesProperty()}.
     *
     * @param show
     *            if true timed entries will be shown
     */
    public final void setShowTimedEntries(boolean show) {
        showTimedEntriesProperty().set(show);
    }

    /**
     * Returns the value of {@link #showTimedEntriesProperty()}.
     *
     * @return true if timed entries will be shown
     */
    public final boolean isShowTimedEntries() {
        return showTimedEntriesProperty().get();
    }


    private final BooleanProperty showFullDayEntries = new SimpleBooleanProperty(this,
            "showFullDayEntries", true);

    /**
     * Controls whether the view will show calendar entries that are "full-day" entries
     * (e.g. "Birthday Dirk").
     *
     * @return true if full day entries will be shown
     */
    public final BooleanProperty showFullDayEntriesProperty() {
        return showFullDayEntries;
    }

    /**
     * Sets the value of {@link #showFullDayEntriesProperty()}.
     *
     * @param show
     *            if true full-day entries will be shown
     */
    public final void setShowFullDayEntries(boolean show) {
        showFullDayEntriesProperty().set(show);
    }

    /**
     * Returns the value of {@link #showFullDayEntriesProperty()}.
     *
     * @return true if full-day entries will be shown
     */
    public final boolean isShowFullDayEntries() {
        return showFullDayEntriesProperty().get();
    }

    private final ObjectProperty<Callback<Entry<?>, MonthEntryView>> entryViewFactory = new SimpleObjectProperty<>(
            this, "entryViewFactory");

    /**
     * A factory used for creating instances of type {@link MonthEntryView}.
     * These views will be shown within each day cell. <h2>Code Example</h2>
     *
     * <pre>
     * setEntryViewFactory(entry -&gt; {
     * 	return new MonthEntryView(entry);
     * });
     * </pre>
     *
     * @return the entry view factory
     */
    public final ObjectProperty<Callback<Entry<?>, MonthEntryView>> entryViewFactoryProperty() {
        return entryViewFactory;
    }

    /**
     * Returns the value of {@link #entryViewFactoryProperty()}.
     *
     * @return the entry view factory
     */
    public final Callback<Entry<?>, MonthEntryView> getEntryViewFactory() {
        return entryViewFactoryProperty().get();
    }

    /**
     * Sets the value of {@link #entryViewFactoryProperty()}.
     *
     * @param factory the entry view factory
     */
    public final void setEntryViewFactory(
            Callback<Entry<?>, MonthEntryView> factory) {
        requireNonNull(factory);
        entryViewFactoryProperty().set(factory);
    }

    @Override
    public void goBack() {
        setDate(getDate().minusMonths(1));
    }

    @Override
    public void goForward() {
        setDate(getDate().plusMonths(1));
    }

    private static final String MONTH_PAGE_CATEGORY = "Month View";

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showWeekdaysProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowWeekdays((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowWeekdays();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Weekdays";
            }

            @Override
            public String getDescription() {
                return "Show or hide the weekdays";
            }

            @Override
            public String getCategory() {
                return MONTH_PAGE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showWeekendsProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowWeekends((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowWeekends();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Weekends";
            }

            @Override
            public String getDescription() {
                return "Mark the weekends";
            }

            @Override
            public String getCategory() {
                return MONTH_PAGE_CATEGORY;
            }
        });

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
                return "Show Week Numbers";
            }

            @Override
            public String getDescription() {
                return "Show or hide the week numbers";
            }

            @Override
            public String getCategory() {
                return MONTH_PAGE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showCurrentWeekProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowCurrentWeek((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowCurrentWeek();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Current Week";
            }

            @Override
            public String getDescription() {
                return "Highlight the current week";
            }

            @Override
            public String getCategory() {
                return MONTH_PAGE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showFullDayEntriesProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowFullDayEntries((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowFullDayEntries();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Full Day Entries";
            }

            @Override
            public String getDescription() {
                return "Show full day entries";
            }

            @Override
            public String getCategory() {
                return MONTH_PAGE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showTimedEntriesProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowTimedEntries((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowTimedEntries();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Timed Entries";
            }

            @Override
            public String getDescription() {
                return "Show timed entries";
            }

            @Override
            public String getCategory() {
                return MONTH_PAGE_CATEGORY;
            }
        });

        return items;
    }
}
