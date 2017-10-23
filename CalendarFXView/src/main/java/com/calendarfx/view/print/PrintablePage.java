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

import com.calendarfx.view.DateControl;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.DetailedDayView;
import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.MonthView;
import com.calendarfx.view.print.PaperView.MarginType;
import impl.com.calendarfx.view.print.PrintablePageSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.WeakEventHandler;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A control that is designed and laid out in such a way that it can be nicely printed.
 * The control is capable of switching between a day view, a week view, or a month view.
 */
public class PrintablePage extends DateControl {

    public static final String DEFAULT_STYLE = "print-page";

    private final PrintPeriodSplitter periodSplitter;

    private final DetailedDayView detailedDayView;
    private final DetailedWeekView detailedWeekView;
    private final MonthView monthView;

    private final WeakEventHandler<MouseEvent> weakMouseHandler = new WeakEventHandler<>(Event::consume);

    public PrintablePage() {
        getStyleClass().add(DEFAULT_STYLE);

        addEventFilter(KeyEvent.ANY, Event::consume);

        setFocusTraversable(false);

        // day view
        detailedDayView = new DetailedDayView();
        detailedDayView.setShowScrollBar(false);
        detailedDayView.setShowToday(false);
        detailedDayView.setEnableCurrentTimeMarker(false);
        detailedDayView.weekFieldsProperty().bind(weekFieldsProperty());
        detailedDayView.showAllDayViewProperty().bind(showAllDayEntriesProperty());
        detailedDayView.showAgendaViewProperty().bind(showEntryDetailsProperty());
        detailedDayView.layoutProperty().bind(layoutProperty());
        detailedDayView.getDayView().setStartTime(LocalTime.MIN);
        detailedDayView.getDayView().setEndTime(LocalTime.MAX);
        detailedDayView.getDayView().setEarlyLateHoursStrategy(DayViewBase.EarlyLateHoursStrategy.HIDE);
        detailedDayView.getDayView().setHoursLayoutStrategy(DayViewBase.HoursLayoutStrategy.FIXED_HOUR_COUNT);
        detailedDayView.getDayView().setVisibleHours(24);
        detailedDayView.dateProperty().bind(pageStartDateProperty());
        detailedDayView.addEventFilter(MouseEvent.ANY, weakMouseHandler);
        detailedDayView.getDayView().setTrimTimeBounds(true);

        Bindings.bindContent(detailedDayView.getCalendarSources(), getCalendarSources());
        Bindings.bindContent(detailedDayView.getCalendarVisibilityMap(), getCalendarVisibilityMap());

        // week view
        detailedWeekView = new DetailedWeekView();
        detailedWeekView.setShowScrollBar(false);
        detailedWeekView.getWeekView().setShowToday(false);
        detailedWeekView.layoutProperty().bind(layoutProperty());
        detailedWeekView.setEnableCurrentTimeMarker(false);
        detailedWeekView.showAllDayViewProperty().bind(showAllDayEntriesProperty());
        detailedWeekView.weekFieldsProperty().bind(weekFieldsProperty());
        detailedWeekView.setStartTime(LocalTime.MIN);
        detailedWeekView.setEndTime(LocalTime.MAX);
        detailedWeekView.setEarlyLateHoursStrategy(DayViewBase.EarlyLateHoursStrategy.HIDE);
        detailedWeekView.setHoursLayoutStrategy(DayViewBase.HoursLayoutStrategy.FIXED_HOUR_COUNT);
        detailedWeekView.setVisibleHours(24);
        detailedWeekView.addEventFilter(MouseEvent.ANY, weakMouseHandler);
        detailedWeekView.dateProperty().bind(pageStartDateProperty());
        detailedWeekView.getWeekView().setTrimTimeBounds(true);

        Bindings.bindContent(detailedWeekView.getCalendarSources(), getCalendarSources());
        Bindings.bindContent(detailedWeekView.getCalendarVisibilityMap(), getCalendarVisibilityMap());

        // month view
        monthView = new MonthView();
        monthView.setShowToday(false);
        monthView.setShowCurrentWeek(false);
        monthView.weekFieldsProperty().bind(weekFieldsProperty());
        monthView.showFullDayEntriesProperty().bind(showAllDayEntriesProperty());
        monthView.showTimedEntriesProperty().bind(showTimedEntriesProperty());
        monthView.addEventFilter(MouseEvent.ANY, weakMouseHandler);
        monthView.dateProperty().bind(pageStartDateProperty());

        Bindings.bindContent(monthView.getCalendarSources(), getCalendarSources());
        Bindings.bindContentBidirectional(monthView.getCalendarVisibilityMap(), getCalendarVisibilityMap());

        updateView();
        updateDimension();

        paperProperty().addListener(obs -> updateDimension());
        viewTypeProperty().addListener(obs -> updateView());

        periodSplitter = new PrintPeriodSplitter(this);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PrintablePageSkin(this);
    }

    public final DetailedDayView getDayView() {
        return detailedDayView;
    }

    public final DetailedWeekView getWeekView() {
        return detailedWeekView;
    }

    public final MonthView getMonthView() {
        return monthView;
    }

    // view type support

    private final ObjectProperty<ViewType> viewType = new SimpleObjectProperty<ViewType>(this, "viewType", ViewType.DAY_VIEW) {
        @Override
        public void set(ViewType newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    public final ObjectProperty<ViewType> viewTypeProperty() {
        return viewType;
    }

    public final ViewType getViewType() {
        return viewTypeProperty().get();
    }

    public final void setViewType(ViewType viewType) {
        viewTypeProperty().set(viewType);
    }

    // current view support (read-only)

    private final ReadOnlyObjectWrapper<DateControl> view = new ReadOnlyObjectWrapper<DateControl>(this, "view") {
        @Override
        public void set(DateControl newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    public final ReadOnlyObjectProperty<DateControl> viewProperty() {
        return view.getReadOnlyProperty();
    }

    public final DateControl getView() {
        return view.get();
    }

    // private because read-only property
    private void setView(DateControl view) {
        this.view.set(view);
    }


    // margin type support

    private final ObjectProperty<MarginType> marginType = new SimpleObjectProperty<MarginType>(this, "marginType", MarginType.DEFAULT) {
        @Override
        public void set(MarginType newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    /**
     * A property used to store the currently requested margin type (custom, minimum, or default).
     *
     * @return the requested margin types
     */
    public final ObjectProperty<MarginType> marginTypeProperty() {
        return marginType;
    }

    /**
     * Returns the value of {@link #marginTypeProperty()}.
     *
     * @return the margin type (custom, default, minimum)
     */
    public final MarginType getMarginType() {
        return marginTypeProperty().get();
    }

    /**
     * Sets the value of {@link #marginTypeProperty()}.
     *
     * @param type the margin type (custom, default, minimum)
     */
    public final void setMarginType(MarginType type) {
        marginTypeProperty().set(type);
    }

    // top margin support

    private final DoubleProperty topMargin = new SimpleDoubleProperty(this, "topMargin") {
        @Override
        public void set(double newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("The margin is invalid: " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * Stores the top print margin value.
     *
     * @return the top margin
     */
    public final DoubleProperty topMarginProperty() {
        return topMargin;
    }

    /**
     * Returns the value of the {@link #topMarginProperty()}.
     *
     * @return the top margin
     */
    public final double getTopMargin() {
        return topMarginProperty().get();
    }

    /**
     * Sets the value of the {@link #topMarginProperty()}.
     *
     * @param margin the top margin
     */
    public final void setTopMargin(double margin) {
        topMarginProperty().set(margin);
    }

    // right margin support

    private final DoubleProperty rightMargin = new SimpleDoubleProperty(this, "rightMargin") {
        @Override
        public void set(double newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("The margin is invalid: " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * Stores the right print margin value.
     *
     * @return the right margin
     */
    public final DoubleProperty rightMarginProperty() {
        return rightMargin;
    }

    /**
     * Returns the value of the {@link #rightMarginProperty()}.
     *
     * @return the right margin
     */
    public final double getRightMargin() {
        return rightMarginProperty().get();
    }

    /**
     * Sets the value of the {@link #rightMarginProperty()}.
     *
     * @param margin the right margin
     */
    public final void setRightMargin(double margin) {
        rightMarginProperty().set(margin);
    }

    // bottom margin support

    private final DoubleProperty bottomMargin = new SimpleDoubleProperty(this, "bottomMargin") {
        @Override
        public void set(double newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("The margin is invalid: " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * Stores the bottom print margin value.
     *
     * @return the bottom margin
     */
    public final DoubleProperty bottomMarginProperty() {
        return bottomMargin;
    }

    /**
     * Returns the value of the {@link #bottomMarginProperty()}.
     *
     * @return the bottom margin
     */
    public final double getBottomMargin() {
        return bottomMarginProperty().get();
    }

    /**
     * Sets the value of the {@link #bottomMarginProperty()}.
     *
     * @param margin the bottom margin
     */
    public final void setBottomMargin(double margin) {
        bottomMarginProperty().set(margin);
    }

    // left margin support

    private final DoubleProperty leftMargin = new SimpleDoubleProperty(this, "leftMargin") {
        @Override
        public void set(double newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("The margin is invalid: " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * Stores the left print margin value.
     *
     * @return the left margin
     */
    public final DoubleProperty leftMarginProperty() {
        return leftMargin;
    }

    /**
     * Returns the value of the {@link #leftMarginProperty()}.
     *
     * @return the left margin
     */
    public final double getLeftMargin() {
        return leftMarginProperty().get();
    }

    /**
     * Sets the value of the {@link #leftMarginProperty()}.
     *
     * @param margin the left margin
     */
    public final void setLeftMargin(double margin) {
        leftMarginProperty().set(margin);
    }

    // print start date support

    private final ObjectProperty<LocalDate> printStartDate = new SimpleObjectProperty<LocalDate>(this, "printStartDate", getToday()) {
        @Override
        public void set(LocalDate newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    public final ObjectProperty<LocalDate> printStartDateProperty() {
        return printStartDate;
    }

    public final LocalDate getPrintStartDate() {
        return printStartDateProperty().get();
    }

    public final void setPrintStartDate(LocalDate date) {
        printStartDateProperty().set(date);
    }

    // print end date support
    private final ObjectProperty<LocalDate> printEndDate = new SimpleObjectProperty<LocalDate>(this, "printEndDate", getToday()) {
        @Override
        public void set(LocalDate newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    public final ObjectProperty<LocalDate> printEndDateProperty() {
        return printEndDate;
    }

    public final LocalDate getPrintEndDate() {
        return printEndDateProperty().get();
    }

    public final void setPrintEndDate(LocalDate date) {
        printEndDateProperty().set(date);
    }

    // page start date support

    private final ReadOnlyObjectWrapper<LocalDate> pageStartDate = new ReadOnlyObjectWrapper<>(this, "pageStartDate", getToday());

    public final ReadOnlyObjectProperty<LocalDate> pageStartDateProperty() {
        return pageStartDate.getReadOnlyProperty();
    }

    public final LocalDate getPageStartDate() {
        return pageStartDate.get();
    }

    private void setPageStartDate(LocalDate date) {
        this.pageStartDate.set(date);
    }

    // page end date support

    private final ReadOnlyObjectWrapper<LocalDate> pageEndDate = new ReadOnlyObjectWrapper<>(this, "pageEndDate", getToday());

    public final ReadOnlyObjectProperty<LocalDate> pageEndDateProperty() {
        return pageEndDate.getReadOnlyProperty();
    }

    public final LocalDate getPageEndDate() {
        return pageEndDate.get();
    }

    private void setPageEndDate(LocalDate date) {
        this.pageEndDate.set(date);
    }

    // paper support

    private final ObjectProperty<Paper> paper = new SimpleObjectProperty<Paper>(this, "paper", Paper.A4) {
        @Override
        public void set(Paper newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    public final ObjectProperty<Paper> paperProperty() {
        return paper;
    }

    public final Paper getPaper() {
        return paperProperty().get();
    }

    public final void setPaper(Paper paper) {
        paperProperty().set(paper);
    }

    // show all day entries support

    private final BooleanProperty showAllDayEntries = new SimpleBooleanProperty(this, "showAllDayEntries", true);

    public final BooleanProperty showAllDayEntriesProperty() {
        return showAllDayEntries;
    }

    public final boolean isShowAllDayEntries() {
        return showAllDayEntriesProperty().get();
    }

    public final void setShowAllDayEntries(boolean show) {
        showAllDayEntriesProperty().set(show);
    }

    // show mini calendars support

    private final BooleanProperty showMiniCalendars = new SimpleBooleanProperty(this, "showMiniCalendars", true);

    public final BooleanProperty showMiniCalendarsProperty() {
        return showMiniCalendars;
    }

    public final boolean isShowMiniCalendars() {
        return showMiniCalendarsProperty().get();
    }

    public final void setShowMiniCalendars(boolean show) {
        showMiniCalendarsProperty().set(show);
    }

    // show calendar keys support

    private final BooleanProperty showCalendarKeys = new SimpleBooleanProperty(this, "showCalendarKeys", true);

    public final BooleanProperty showCalendarKeysProperty() {
        return showCalendarKeys;
    }

    public final boolean isShowCalendarKeys() {
        return showCalendarKeysProperty().get();
    }

    public final void setShowCalendarKeys(boolean show) {
        showCalendarKeysProperty().set(show);
    }

    private final BooleanProperty showTimedEntries = new SimpleBooleanProperty(this, "showTimedEntries", true);

    public final BooleanProperty showTimedEntriesProperty() {
        return showTimedEntries;
    }

    public final boolean isShowTimedEntries() {
        return showTimedEntriesProperty().get();
    }

    public final void setShowTimedEntries(boolean show) {
        showTimedEntriesProperty().set(show);
    }

    private final BooleanProperty showEntryDetails = new SimpleBooleanProperty(this, "showEntryDetails", true);

    public final BooleanProperty showEntryDetailsProperty() {
        return showEntryDetails;
    }

    public final boolean isShowEntryDetails() {
        return showEntryDetailsProperty().get();
    }

    public final void setShowEntryDetails(boolean show) {
        showEntryDetailsProperty().set(show);
    }

    private final ReadOnlyIntegerWrapper pageNumber = new ReadOnlyIntegerWrapper(this, "pageNumber");

    public final ReadOnlyIntegerProperty pageNumberProperty() {
        return pageNumber.getReadOnlyProperty();
    }

    public final int getPageNumber() {
        return pageNumber.get();
    }

    private void setPageNumber(int number) {
        this.pageNumber.set(number);
    }

    private final ReadOnlyIntegerWrapper totalPages = new ReadOnlyIntegerWrapper(this, "totalPages");

    public final ReadOnlyIntegerProperty totalPagesProperty() {
        return totalPages.getReadOnlyProperty();
    }

    public final int getTotalPages() {
        return totalPages.get();
    }

    private void setTotalPages(int total) {
        this.totalPages.set(total);
    }

    public final boolean next() {
        return periodSplitter.next();
    }

    public final boolean back() {
        return periodSplitter.back();
    }

    public final void bindPage(PrintablePage otherPage) {
        super.bind(otherPage, true);

        Bindings.bindBidirectional(otherPage.viewTypeProperty(), viewTypeProperty());
        Bindings.bindBidirectional(otherPage.paperProperty(), paperProperty());
        Bindings.bindBidirectional(otherPage.showAllDayEntriesProperty(), showAllDayEntriesProperty());
        Bindings.bindBidirectional(otherPage.showCalendarKeysProperty(), showCalendarKeysProperty());
        Bindings.bindBidirectional(otherPage.showMiniCalendarsProperty(), showMiniCalendarsProperty());
        Bindings.bindBidirectional(otherPage.showTimedEntriesProperty(), showTimedEntriesProperty());
        Bindings.bindBidirectional(otherPage.printStartDateProperty(), printStartDateProperty());
        Bindings.bindBidirectional(otherPage.printEndDateProperty(), printEndDateProperty());
    }

    public final void unbindPage(PrintablePage otherPage) {
        super.unbind(otherPage);

        Bindings.unbindBidirectional(otherPage.viewTypeProperty(), viewTypeProperty());
        Bindings.unbindBidirectional(otherPage.paperProperty(), paperProperty());
        Bindings.unbindBidirectional(otherPage.showAllDayEntriesProperty(), showAllDayEntriesProperty());
        Bindings.unbindBidirectional(otherPage.showCalendarKeysProperty(), showCalendarKeysProperty());
        Bindings.unbindBidirectional(otherPage.showMiniCalendarsProperty(), showMiniCalendarsProperty());
        Bindings.unbindBidirectional(otherPage.showTimedEntriesProperty(), showTimedEntriesProperty());
        Bindings.unbindBidirectional(otherPage.printStartDateProperty(), printStartDateProperty());
        Bindings.unbindBidirectional(otherPage.printEndDateProperty(), printEndDateProperty());
    }

    private void updateView() {
        DateControl newView;

        switch (getViewType()) {
            case DAY_VIEW:
                newView = detailedDayView;
                break;
            case WEEK_VIEW:
                newView = detailedWeekView;
                break;
            case MONTH_VIEW:
                newView = monthView;
                break;
            default:
                throw new UnsupportedOperationException("unsupported view type: " + getViewType());
        }

        setView(newView);
        updateDimension();
    }

    private void updateDimension() {
        final double SIZE_MULTIPLIER = 1.5;
        if (getViewType().getPageOrientation() == PageOrientation.PORTRAIT) {
            setPrefHeight(getPaper().getHeight() * SIZE_MULTIPLIER);
            setPrefWidth(getPaper().getWidth() * SIZE_MULTIPLIER);
        } else {
            setPrefHeight(getPaper().getWidth() * SIZE_MULTIPLIER);
            setPrefWidth(getPaper().getHeight() * SIZE_MULTIPLIER);
        }
    }

    private static final class PrintPeriodSplitter implements InvalidationListener {

        private PrintablePage page;
        private PageSlice slice;

        public PrintPeriodSplitter(PrintablePage page) {
            this.page = Objects.requireNonNull(page);
            page.viewTypeProperty().addListener(this);
            page.printStartDateProperty().addListener(this);
            page.printEndDateProperty().addListener(this);
            split();
        }

        @Override
        public void invalidated(Observable observable) {
            split();
        }

        private void split() {
            LocalDate printStart = page.getPrintStartDate();
            LocalDate printEnd = page.getPrintEndDate();

            if (printStart == null || printEnd == null || printStart.isAfter(printEnd)) {
                // Just in case, should never happen!
                return;
            }

            PageSlice first = null;
            PageSlice pivot = null;
            LocalDate pageStartDate = printStart;
            int count = 0;

            do {
                LocalDate pageEndDate = pageStartDate.plus(1, page.getViewType().getChronoUnit()).minusDays(1);

                PageSlice next = new PageSlice(++count, pageStartDate, pageEndDate);

                if (first == null) {
                    first = next;
                }

                if (pivot != null) {
                    pivot.setNext(next);
                }

                pivot = next;
                pageStartDate = pageEndDate.plusDays(1);
            }
            while (pageStartDate.isBefore(printEnd) || pageStartDate.isEqual(printEnd));

            setSlice(first);
            page.setTotalPages(count);
        }

        public boolean next() {
            if (slice != null && slice.hasNext()) {
                setSlice(slice.getNext());
                return true;
            }
            return false;
        }

        public boolean back() {
            if (slice != null && slice.hasBack()) {
                setSlice(slice.getBack());
            }
            return false;
        }

        private void setSlice(PageSlice slice) {
            this.slice = slice;
            this.page.setPageStartDate(slice.getStart());
            this.page.setPageEndDate(slice.getEnd());
            this.page.setPageNumber(slice.getNumber());
        }

    }

    private static final class PageSlice {

        private int number;
        private LocalDate start;
        private LocalDate end;
        private PageSlice next;
        private PageSlice back;

        public PageSlice(int number, LocalDate start, LocalDate end) {
            this.number = number;
            this.start = start;
            this.end = end;
        }

        public int getNumber() {
            return number;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }

        public PageSlice getNext() {
            return next;
        }

        public void setNext(PageSlice next) {
            if (this.next != next) {
                this.next = next;
                next.setBack(this);
            }
        }

        public PageSlice getBack() {
            return back;
        }

        public void setBack(PageSlice back) {
            if (this.back != back) {
                this.back = back;
                back.setNext(this);
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public boolean hasBack() {
            return back != null;
        }

    }
}

