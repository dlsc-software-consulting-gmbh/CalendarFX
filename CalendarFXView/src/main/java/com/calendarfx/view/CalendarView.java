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

import com.calendarfx.view.page.DayPage;
import com.calendarfx.view.page.MonthPage;
import com.calendarfx.view.page.PageBase;
import com.calendarfx.view.page.WeekPage;
import com.calendarfx.view.page.YearPage;
import com.calendarfx.view.print.PrintView;
import impl.com.calendarfx.view.CalendarViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;

import static com.calendarfx.view.CalendarView.Page.DAY;
import static com.calendarfx.view.CalendarView.Page.MONTH;
import static com.calendarfx.view.CalendarView.Page.WEEK;
import static com.calendarfx.view.CalendarView.Page.YEAR;
import static java.util.Objects.requireNonNull;

/**
 * A full calendar view with multiple pages for displaying a single day, a week,
 * a month, and an entire year. The view also includes two trays, one for seeing
 * all calendar sources, and one for seeing the results of the current search.
 * The trays can be shown or hidden. Another nice feature is direct support for
 * printing (see {@link #getPrintView()}).
 *
 * <img width="100%" src="doc-files/calendar-view.png" alt="Calendar View">
 */
public class CalendarView extends DateControl {

    private static final String DEFAULT_STYLE_CLASS = "calendar-view";

    private static final String SELECTED_PAGE = "com.calendarfx.selectedPage";

    private final SourceView sourceView;

    private final SearchResultView searchResultView;

    private final YearMonthView yearMonthView;

    private DayPage dayPage;

    private WeekPage weekPage;

    private MonthPage monthPage;

    private YearPage yearPage;

    private DeveloperConsole developerConsole;

    private final CustomTextField searchField;

    private PrintView printView;

    public CalendarView() {
        this(Page.values());
    }

    /**
     * Constructs a new calendar view.
     */
    public CalendarView(Page... availablePages) {
        Objects.requireNonNull(availablePages, "available pages can not be null");

        if (availablePages.length == 0) {
            throw new IllegalArgumentException("no available page passed to constructor");
        }

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        getAvailablePages().setAll(availablePages);

        this.searchField = (CustomTextField) TextFields.createClearableTextField();
        this.sourceView = new SourceView();
        this.searchResultView = new SearchResultView();
        this.yearMonthView = new YearMonthView();

        if (Boolean.getBoolean("calendarfx.developer")) {
            this.developerConsole = new DeveloperConsole();
            this.developerConsole.setDateControl(this);
        }

        selectedPage.set(availablePages[0]);

        Bindings.bindBidirectional(searchField.visibleProperty(), showSearchFieldProperty());

        /*
         * We do have a user agent stylesheet, but it doesn't seem to work
         * properly when run as a standalone jar file.
         */
        getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());

        /*
         * We are "abusing" the properties map to pass new values of read-only
         * properties from the skin to the control.
         */
        getProperties().addListener((Change<?, ?> change) -> {
            if (change.getKey().equals(SELECTED_PAGE)) {
                if (change.getValueAdded() != null) {
                    Page page = (Page) change.getValueAdded();
                    selectedPage.set(page);
                    getProperties().remove(SELECTED_PAGE);
                }
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CalendarViewSkin(this);
    }

    public PageBase getPageView(Page page) {
        switch (page) {
            case DAY:
                return getDayPage();
            case WEEK:
                return getWeekPage();
            case MONTH:
                return getMonthPage();
            case YEAR:
                return getYearPage();
            default:
                throw new IllegalArgumentException("unknown page: " + page);
        }
    }

    /**
     * An enumerator listing the available pages that can be shown
     * by the calendar view. By default, the CalendarView contains
     * all possible pages. Use {@link #getAvailablePages()} to change
     * this.
     *
     * @see #getAvailablePages()
     */
    public enum Page {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    private final ObservableList<Page> availablePages = FXCollections.observableArrayList();

    /**
     * Returns the list of pages that will be available within the calendar view (day, week,
     * month, year).
     *
     * @return the available pages
     */
    public final ObservableList<Page> getAvailablePages() {
        return availablePages;
    }

    /**
     * Returns the developer console that can be made visible via a META-D keystroke
     * when the system property "calendarfx.developer" is set to true.
     *
     * @return the developer console or null if the system property
     * "calendarfx.developer" is not set to true
     */
    public final DeveloperConsole getDeveloperConsole() {
        return developerConsole;
    }

    /**
     * Returns the day page.
     *
     * @return the day page
     */
    public final DayPage getDayPage() {
        if (dayPage == null) {
            dayPage = new DayPage();
            bind(dayPage, true);
        }
        return dayPage;
    }

    /**
     * Returns the week page.
     *
     * @return the week page
     */
    public final WeekPage getWeekPage() {
        if (weekPage == null) {
            weekPage = new WeekPage();
            bind(weekPage, true);
        }
        return weekPage;
    }

    /**
     * Returns the month page.
     *
     * @return the month page
     */
    public final MonthPage getMonthPage() {
        if (monthPage == null) {
            monthPage = new MonthPage();
            bind(monthPage, true);
        }
        return monthPage;
    }

    /**
     * Returns the year page.
     *
     * @return the year page
     */
    public final YearPage getYearPage() {
        if (yearPage == null) {
            yearPage = new YearPage();
            bind(yearPage, true);
        }
        return yearPage;
    }

    /**
     * Returns the search text field.
     *
     * @return the search field
     */
    public final CustomTextField getSearchField() {
        return searchField;
    }

    /**
     * Returns the search result view child control.
     *
     * @return the search result view
     */
    public final SearchResultView getSearchResultView() {
        return searchResultView;
    }

    /**
     * Returns the source view child control.
     *
     * @return the source view
     */
    public final SourceView getSourceView() {
        return sourceView;
    }

    /**
     * Returns the year month view child control.
     *
     * @return the year month view
     */
    public final YearMonthView getYearMonthView() {
        return yearMonthView;
    }

    /**
     * Returns the print view associated with this view. The print view shows a print
     * result preview dialog that allows the user to customize several printing options
     * and trigger a print job to be executed.
     *
     * @return the print view
     */
    public final PrintView getPrintView() {
        if (printView == null) {
            printView = new PrintView();
        }

        return printView;
    }

    private final BooleanProperty showDevoloperConsole = new SimpleBooleanProperty(this, "showDevoloperConsole", false);

    /**
     * Controls the visibility of the developer console. The console displays
     * various types of events that are fired by the calendar while the user is
     * using it.
     *
     * @return true if the developer console will be shown to the user
     */
    public final BooleanProperty showDeveloperConsoleProperty() {
        return showDevoloperConsole;
    }

    /**
     * Sets the value of {@link #showDeveloperConsoleProperty()}.
     *
     * @param show if true will show the developer console
     */
    public final void setShowDeveloperConsole(boolean show) {
        showDeveloperConsoleProperty().set(show);
    }

    /**
     * Returns the value of {@link #showSourceTrayProperty()}.
     *
     * @return true if the developer console gets shown to the user
     */
    public final boolean isShowDeveloperConsole() {
        return showDeveloperConsoleProperty().get();
    }

    private final BooleanProperty showSourceTray = new SimpleBooleanProperty(this, "showSourceTray", false);

    /**
     * Controls the visibility of the source tray.
     *
     * @return true if the source tray will be shown to the user
     */
    public final BooleanProperty showSourceTrayProperty() {
        return showSourceTray;
    }

    /**
     * Sets the value of {@link #showSourceTrayProperty()}.
     *
     * @param show if true will show the tray
     */
    public final void setShowSourceTray(boolean show) {
        showSourceTray.set(show);
    }

    /**
     * Returns the value of {@link #showSourceTrayProperty()}.
     *
     * @return true if the calendar source tray gets shown to the user
     */
    public final boolean isShowSourceTray() {
        return showSourceTray.get();
    }

    private final BooleanProperty showSearchResultsTray = new SimpleBooleanProperty(this, "showSearchResultsTray", false);

    /**
     * Controls the visibility of the search results tray.
     *
     * @return the search result tray visibility
     */
    public final BooleanProperty showSearchResultsTrayProperty() {
        return showSearchResultsTray;
    }

    /**
     * Sets the value of {@link #showSearchResultsTrayProperty()}.
     *
     * @param show if true the search result tray will be shown to the user
     */
    public final void setShowSearchResultsTray(boolean show) {
        showSearchResultsTray.set(show);
    }

    /**
     * Returns the value of {@link #showSearchResultsTrayProperty()}.
     *
     * @return true if the search result tray is visible
     */
    public final boolean isShowSearchResultsTray() {
        return showSearchResultsTray.get();
    }

    private final ReadOnlyObjectWrapper<Page> selectedPage = new ReadOnlyObjectWrapper<>(this, "selectedPage");

    /**
     * A read-only property used for storing the currently selected page.
     *
     * @return the selected page view
     */
    public final ReadOnlyObjectProperty<Page> selectedPageProperty() {
        return selectedPage.getReadOnlyProperty();
    }

    /**
     * The value of {@link #selectedPageProperty()}.
     *
     * @return the selected page view
     */
    public final Page getSelectedPage() {
        return selectedPageProperty().get();
    }

    /**
     * Returns the view for the currently selected {@link Page}.
     *
     * @return the selected page view
     * @see #selectedPageProperty()
     */
    public final PageBase getSelectedPageView() {
        return getPageView(getSelectedPage());
    }

    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header", null);

    /**
     * Property used to reference a node that can be used as a header for the
     * calendar view. The skin of this view uses a {@link BorderPane} to lay out
     * its children. The header will be placed in its top location.
     *
     * @return a node added as a header to the view (e.g. a tool bar)
     */
    public final ObjectProperty<Node> headerProperty() {
        return header;
    }

    /**
     * Returns the value of {@link #headerProperty()}.
     *
     * @return the node that will be shown above the actual calendar
     */
    public final Node getHeader() {
        return headerProperty().get();
    }

    /**
     * Sets the value of {@link #headerProperty()}.
     *
     * @param node a node that will be shown above the actual calendar
     */
    public final void setHeader(Node node) {
        headerProperty().set(node);
    }

    private final ObjectProperty<Node> footer = new SimpleObjectProperty<>(this, "footer", null);

    /**
     * Property used to reference a node that can be used as a footer for the
     * calendar view. The skin of this view uses a {@link BorderPane} to lay out
     * its children. The footer will be placed in its bottom location.
     *
     * @return a node added as a footer to the view (e.g. a status bar)
     */
    public final ObjectProperty<Node> footerProperty() {
        return footer;
    }

    /**
     * Returns the value of {@link #footerProperty()}.
     *
     * @return the header node shown below the actual calendar
     */
    public final Node getFooter() {
        return footerProperty().get();
    }

    /**
     * Sets the value of {@link #footerProperty()}.
     *
     * @param node a node that will be shown below the actual calendar
     */
    public final void setFooter(Node node) {
        footerProperty().set(node);
    }

    // tray animation support

    private final BooleanProperty traysAnimated = new SimpleBooleanProperty(this, "traysAnimated", true);

    /**
     * A property used to control whether closing or opening the trays (source view,
     * search result view) will be animated.
     *
     * @return true if animations (eye candy) are enabled
     */
    public final BooleanProperty traysAnimatedProperty() {
        return this.traysAnimated;
    }

    /**
     * Returns the value of {@link #traysAnimatedProperty()}.
     *
     * @return true if tray animations are enabled
     */
    public final boolean isTraysAnimated() {
        return traysAnimatedProperty().get();
    }

    /**
     * Sets the value of {@link #traysAnimatedProperty()}.
     *
     * @param animated if true tray animations will be used when opening / closing
     *                 the source tray or the search result tray
     */
    public final void setTraysAnimated(boolean animated) {
        traysAnimatedProperty().set(animated);
    }

    // show search field support

    private final BooleanProperty showSearchField = new SimpleBooleanProperty(this, "showSearchField", true);

    /**
     * Controls whether the search field (text field) in the upper right corner
     * of the control will be shown to the user or not.
     *
     * @return true if the search field will be accessible by the user
     */
    public final BooleanProperty showSearchFieldProperty() {
        return showSearchField;
    }

    /**
     * Returns the value of {@link #showSearchFieldProperty()}.
     *
     * @return true if the search field will be accessible by the user
     */
    public final boolean isShowSearchField() {
        return showSearchField.get();
    }

    /**
     * Sets the value of {@link #showSearchFieldProperty()}.
     *
     * @param show if true the search field will be accessible by the user
     */
    public final void setShowSearchField(boolean show) {
        showSearchField.set(show);
    }

    private final BooleanProperty showSourceTrayButton = new SimpleBooleanProperty(this, "showSourceTrayButton", true);

    /**
     * Controls whether the source tray button in the upper left corner of the
     * control will be shown to the user or not.
     *
     * @return true if the source tray button will be accessible by the user
     */
    public final BooleanProperty showSourceTrayButtonProperty() {
        return showSourceTrayButton;
    }

    /**
     * Returns the value of {@link #showSourceTrayButtonProperty()}.
     *
     * @return true if the source tray button will be accessible by the user
     */
    public final boolean isShowSourceTrayButton() {
        return showSourceTrayButton.get();
    }

    /**
     * Sets the value of {@link #showSourceTrayButtonProperty()}.
     *
     * @param show if true the source tray button will be accessible by the user
     */
    public final void setShowSourceTrayButton(boolean show) {
        showSourceTrayButton.set(show);
    }

    private final BooleanProperty showAddCalendarButton = new SimpleBooleanProperty(this, "showAddCalendarButton", true);

    /**
     * Controls whether the "add calendar" button in the upper left corner of
     * the control will be shown to the user or not.
     *
     * @return true if the "add calendar" button will be accessible by the user
     */
    public final BooleanProperty showAddCalendarButtonProperty() {
        return showAddCalendarButton;
    }

    /**
     * Returns the value of {@link #showAddCalendarButtonProperty()}.
     *
     * @return true if the "add calendar" button will be accessible by the user
     */
    public final boolean isShowAddCalendarButton() {
        //return showAddCalendarButton.get();
        return false; //TD: We don't need this
    }

    /**
     * Sets the value of {@link #showAddCalendarButtonProperty()}.
     *
     * @param show if true the "add calendar" button will be accessible by the
     *             user
     */
    public final void setShowAddCalendarButton(boolean show) {
        showAddCalendarButton.set(show);
    }

    private final BooleanProperty showPrintButton = new SimpleBooleanProperty(this, "showPrintButton", true);

    /**
     * Controls whether the "print" button in the upper left corner of the
     * control will be shown to the user or not.
     *
     * @return true if the "print" button will be accessible by the user
     */
    public final BooleanProperty showPrintButtonProperty() {
        return showPrintButton;
    }

    /**
     * Returns the value of {@link #showPrintButtonProperty()}.
     *
     * @return true if the "print" button will be accessible by the user
     */
    public final boolean isShowPrintButton() {
        return showPrintButton.get();
    }

    /**
     * Sets the value of {@link #showPrintButtonProperty()}.
     *
     * @param show if true the "print" button will be accessible by the user
     */
    public final void setShowPrintButton(boolean show) {
        showPrintButton.set(show);
    }

    private final BooleanProperty showPageToolBarControls = new SimpleBooleanProperty(this, "showPageToolBarControls", true);

    /**
     * Controls whether the "page-specific" toolbar controls (e.g. DayPage:
     * agenda view, day view, combined view) in the upper left corner of the
     * control will be shown to the user or not.
     *
     * @return true if the "page-specific" toolbar controls will be accessible
     * by the user
     * @see PageBase#getToolBarControls()
     */
    public final BooleanProperty showPageToolBarControlsProperty() {
        return showPageToolBarControls;
    }

    /**
     * Returns the value of {@link #showPageToolBarControlsProperty()}.
     *
     * @return true if the "page-specific" toolbar controls will be accessible
     * by the user
     * @see PageBase#getToolBarControls()
     */
    public final boolean isShowPageToolBarControls() {
        return showPageToolBarControls.get();
    }

    /**
     * Sets the value of {@link #showPageToolBarControlsProperty()}.
     *
     * @param show if true the "page-specific" toolbar controls will be
     *             accessible by the user
     * @see PageBase#getToolBarControls()
     */
    public final void setShowPageToolBarControls(boolean show) {
        showPageToolBarControls.set(show);
    }

    private final BooleanProperty showPageSwitcher = new SimpleBooleanProperty(this, "showPageSwitcher", true);

    /**
     * Controls whether the page switcher (day, week, month, year) will be shown
     * to the user or not.
     *
     * @return true if the toolbar will be shown to the user
     */
    public final BooleanProperty showPageSwitcherProperty() {
        return showPageSwitcher;
    }

    /**
     * Returns the value of {@link #showPageSwitcherProperty()}.
     *
     * @return true if the page switcher will be visible
     */
    public final boolean isShowPageSwitcher() {
        return showPageSwitcher.get();
    }

    /**
     * Sets the value of {@link #showPageSwitcherProperty()}.
     *
     * @param show true if the page switcher will be visible
     */
    public final void setShowPageSwitcher(boolean show) {
        showPageSwitcher.set(show);
    }

    private final BooleanProperty showToolBar = new SimpleBooleanProperty(this, "showToolBar", true);

    /**
     * Controls whether the toolbar will be shown to the user or not.
     *
     * @return true if the toolbar will be shown to the user
     */
    public final BooleanProperty showToolBarProperty() {
        return showToolBar;
    }

    /**
     * Returns the value of {@link #showToolBarProperty()}.
     *
     * @return true if the toolbar will be visible
     */
    public final boolean isShowToolBar() {
        return showToolBar.get();
    }

    /**
     * Sets the value of {@link #showToolBarProperty()}.
     *
     * @param show true if the toolbar will be visible
     */
    public final void setShowToolBar(boolean show) {
        showToolBar.set(show);
    }

    /**
     * Switches the view to the {@link DayPage}.
     */
    public final void showDayPage() {
        if (getAvailablePages().contains(DAY)) {
            selectedPage.set(DAY);
        } else {
            throw new UnsupportedOperationException("calendar does not support day view");
        }
    }

    /**
     * Switches the view to the {@link WeekPage}.
     */
    public final void showWeekPage() {
        if (getAvailablePages().contains(WEEK)) {
            selectedPage.set(WEEK);
        } else {
            throw new UnsupportedOperationException("calendar does not support week view");
        }
    }

    /**
     * Switches the view to the {@link MonthPage}.
     */
    public final void showMonthPage() {
        if (getAvailablePages().contains(MONTH)) {
            selectedPage.set(MONTH);
        } else {
            throw new UnsupportedOperationException("calendar does not support month view");
        }
    }

    /**
     * Switches the view to the {@link YearPage}.
     */
    public final void showYearPage() {
        if (getAvailablePages().contains(YEAR)) {
            selectedPage.set(YEAR);
        } else {
            throw new UnsupportedOperationException("calendar does not support year view");
        }
    }

    /**
     * Sends the request to the calendar view to display the given date. The
     * view will switch to the {@link DayPage} and set the value of
     * {@link #dateProperty()} to the date.
     *
     * @param date the date to show in the view
     */
    public final void showDate(LocalDate date) {
        requireNonNull(date);
        if (getAvailablePages().contains(DAY)) {
            selectedPage.set(DAY);
        } else if (getAvailablePages().contains(WEEK)) {
            selectedPage.set(WEEK);
        } else if (getAvailablePages().contains(MONTH)) {
            selectedPage.set(MONTH);
        } else if (getAvailablePages().contains(YEAR)) {
            selectedPage.set(YEAR);
        }

        setDate(date);
    }

    /**
     * Sends the request to the calendar view to display the given week. The
     * view will try to switch to the {@link WeekPage} and set the value of
     * {@link #dateProperty()} to the date.
     *
     * @param year       the date to show in the view
     * @param weekOfYear the week to show in the view
     */
    public final void showWeek(Year year, int weekOfYear) {
        requireNonNull(year);
        if (weekOfYear < 1) {
            throw new IllegalArgumentException("illegal value for week of year: " + weekOfYear);
        }
        if (getAvailablePages().contains(WEEK)) {
            selectedPage.set(WEEK);
        } else if (getAvailablePages().contains(MONTH)) {
            selectedPage.set(MONTH);
        } else if (getAvailablePages().contains(YEAR)) {
            selectedPage.set(YEAR);
        }

        setDate(LocalDate.of(year.getValue(), 1, 1).plusWeeks(weekOfYear));
    }

    /**
     * Sends the request to the calendar view to display the given date and
     * time. The view will switch to the {@link DayPage} and set the value of
     * {@link #dateProperty()} to the date and {@link #requestedTimeProperty()}
     * to the time.
     *
     * @param dateTime the date and time to show in the view
     */
    public final void showDateTime(LocalDateTime dateTime) {
        requireNonNull(dateTime);

        if (getAvailablePages().contains(DAY)) {
            selectedPage.set(DAY);
        } else if (getAvailablePages().contains(WEEK)) {
            selectedPage.set(WEEK);
        } else if (getAvailablePages().contains(MONTH)) {
            selectedPage.set(MONTH);
        } else if (getAvailablePages().contains(YEAR)) {
            selectedPage.set(YEAR);
        }

        setDate(dateTime.toLocalDate());
        setRequestedTime(dateTime.toLocalTime());
    }

    /**
     * Sends the request to the calendar view to display the given year and
     * month. The view will switch to the {@link MonthPage} and set the value of
     * {@link #dateProperty()} to the first day of the month.
     *
     * @param yearMonth the year and month to show in the view
     */
    public final void showYearMonth(YearMonth yearMonth) {
        requireNonNull(yearMonth);

        if (getAvailablePages().contains(MONTH)) {
            selectedPage.set(MONTH);
        } else if (getAvailablePages().contains(YEAR)) {
            selectedPage.set(YEAR);
        }

        setDate(yearMonth.atDay(1));
    }

    /**
     * Sends the request to the calendar view to display the given year. The
     * view will switch to the {@link YearPage} and set the value of
     * {@link #dateProperty()} to the first day of the year.
     *
     * @param year the year to show in the view
     */
    public final void showYear(Year year) {
        requireNonNull(year);
        if (getAvailablePages().contains(YEAR)) {
            selectedPage.set(YEAR);
            setDate(year.atDay(1));
        }
    }

    private final String CALENDAR_VIEW_CATEGORY = "Calendar View";

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showSourceTrayProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowSourceTray((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowSourceTray();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Calendar Tray";
            }

            @Override
            public String getDescription() {
                return "Show or hide the calendar tray on the left";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showSearchResultsTrayProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowSearchResultsTray((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowSearchResultsTray();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Search Results Tray";
            }

            @Override
            public String getDescription() {
                return "Show or hide the search results tray on the right";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showSearchFieldProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowSearchField((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowSearchField();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Search Field";
            }

            @Override
            public String getDescription() {
                return "Can the user access the search field or not.";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showSourceTrayButtonProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowSourceTrayButton((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowSourceTrayButton();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Source Tray Button";
            }

            @Override
            public String getDescription() {
                return "Can the user access the source tray button or not.";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showAddCalendarButtonProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowAddCalendarButton((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowAddCalendarButton();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Add Calendar Button";
            }

            @Override
            public String getDescription() {
                return "Can the user access the button to add new calendars or not.";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showPrintButtonProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowPrintButton((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowPrintButton();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Print Button";
            }

            @Override
            public String getDescription() {
                return "Can the user access the button to print calendars or not.";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showPageToolBarControlsProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowPageToolBarControls((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowPageToolBarControls();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Page Controls";
            }

            @Override
            public String getDescription() {
                return "Can the user access the page-specific toolbar controls or not.";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showPageSwitcherProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowPageSwitcher((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowPageSwitcher();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Page Switcher";
            }

            @Override
            public String getDescription() {
                return "Visibility of the switcher.";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showToolBarProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowToolBar((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowToolBar();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show ToolBar";
            }

            @Override
            public String getDescription() {
                return "Visibility of the toolbar.";
            }

            @Override
            public String getCategory() {
                return CALENDAR_VIEW_CATEGORY;
            }
        });

        return items;
    }
}
