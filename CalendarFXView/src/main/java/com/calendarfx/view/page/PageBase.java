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

package com.calendarfx.view.page;

import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.print.ViewType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.controlsfx.control.PropertySheet.Item;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.time.format.FormatStyle.MEDIUM;
import static java.util.Objects.requireNonNull;

/**
 * The common superclass for all page views. It adds a chrome to the pages so
 * that the user can see the current date in the upper right corner. Navigation
 * controls can be found in the upper left corner.
 */
public abstract class PageBase extends DateControl {

    /**
     * Constructs a new page.
     */
    protected PageBase() {

        /*
         * Pages will take whatever space they can get. If they become too small for
         * their content then they should react responsive and hide some of the content.
         */
        setMinSize(0, 0);
        getStyleClass().add("calendar-page"); //$NON-NLS-1$
    }

    private final BooleanProperty hidden = new SimpleBooleanProperty(this, "hidden", false);

    /**
     * A property used to indicate whether the page should be shown to the user or not. Not
     * every application requires all available pages (day, week, month, year) to be accessible
     * to the user.
     *
     * @return true if the page should not be shown to the user
     */
    public final BooleanProperty hiddenProperty() {
        return hidden;
    }

    /**
     * Sets the value of {@link #hiddenProperty()}.
     *
     * @param hidden true if the page should be hidden
     */
    public final void setHidden(boolean hidden) {
        hiddenProperty().set(hidden);
    }

    /**
     * Returns the value of {@link #hiddenProperty()}.
     *
     * @return true if the page will be hidden
     */
    public final boolean isHidden() {
        return hiddenProperty().get();
    }

    /**
     * Returns one or more controls that can be added to the toolbar by the
     * surrounding container, e.g. the {@link CalendarView}.
     *
     * @return extra toolbar controls
     */
    public Node getToolBarControls() {
        return null;
    }

    private final ObjectProperty<DateTimeFormatter> dateTimeFormatter = new SimpleObjectProperty<>(
            this, "datePattern", DateTimeFormatter.ofLocalizedDate(MEDIUM)); //$NON-NLS-1$

    /**
     * A formatter for the date shown in the upper right corner. Each page has
     * its own formatting requirements. The {@link DayPage} displays a full
     * date, while the {@link YearPage} only shows the current year.
     *
     * @return the date formatter
     */
    public final ObjectProperty<DateTimeFormatter> dateTimeFormatterProperty() {
        return dateTimeFormatter;
    }

    /**
     * Returns the value of {@link #dateTimeFormatterProperty()}.
     *
     * @return the date and time formatter
     */
    public final DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatterProperty().get();
    }

    /**
     * Sets the value of {@link #dateTimeFormatterProperty()}.
     *
     * @param formatter
     *            the date and time formatter
     */
    public final void setDateTimeFormatter(DateTimeFormatter formatter) {
        requireNonNull(formatter);
        dateTimeFormatterProperty().set(formatter);
    }

    private final BooleanProperty showDate = new SimpleBooleanProperty(this,
            "showDate", true); //$NON-NLS-1$

    /**
     * Determines whether the date will be shown by the page in the upper right
     * corner.
     *
     * @return true if the date will be shown
     */
    public final BooleanProperty showDateProperty() {
        return showDate;
    }

    /**
     * Sets the value of {@link #showDateProperty()}.
     *
     * @param show
     *            if true the date willl be shown
     */
    public final void setShowDate(boolean show) {
        showDateProperty().set(show);
    }

    /**
     * Returns the value of {@link #showDateProperty()}.
     *
     * @return true if the date will be shown
     */
    public final boolean isShowDateHeader() {
        return showDateProperty().get();
    }

    private final BooleanProperty showNavigation = new SimpleBooleanProperty(
            this, "showNavigation", true); //$NON-NLS-1$

    /**
     * Determines if the navigation controls for going back and forward in time
     * will be shown in the upper right corner.
     *
     * @return true if the navigation controls will be shown
     */
    public final BooleanProperty showNavigationProperty() {
        return showNavigation;
    }

    /**
     * Sets the value of {@link #showNavigationProperty()}.
     *
     * @param show
     *            if true the navigation controls will be shown
     */
    public final void setShowNavigation(boolean show) {
        showNavigationProperty().set(show);
    }

    /**
     * Returns the value of {@link #showNavigationProperty()}.
     *
     * @return true if the navigation controls will be shown
     */
    public final boolean isShowNavigation() {
        return showNavigationProperty().get();
    }

    /**
     * Returns the type of view used when printing this page.
     *
     * @return The print view type.
     */
    public abstract ViewType getPrintViewType();

    private final String PAGE_BASE_CATEGORY = "Page Base"; //$NON-NLS-1$

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showNavigationProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowNavigation((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowNavigation();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Navigation"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Navigation controls (back, forward, today)"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return PAGE_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showDateProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowDate((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowDateHeader();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Date"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Header with current month, day, or year."; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return PAGE_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(dateTimeFormatterProperty());
            }

            @Override
            public void setValue(Object value) {
                setDateTimeFormatter((DateTimeFormatter) value);
            }

            @Override
            public Object getValue() {
                return getDateTimeFormatter();
            }

            @Override
            public Class<?> getType() {
                return DateTimeFormatter.class;
            }

            @Override
            public String getName() {
                return "Date Time Formatter"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Date time formatter"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return PAGE_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(hiddenProperty());
            }

            @Override
            public void setValue(Object value) {
                setHidden((boolean) value);
            }

            @Override
            public Object getValue() {
                return isHidden();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Hidden"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Hides the page from the user."; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return PAGE_BASE_CATEGORY;
            }
        });

        return items;
    }

}