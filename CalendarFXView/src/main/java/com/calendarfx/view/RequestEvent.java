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
import com.calendarfx.view.page.DayPage;
import com.calendarfx.view.page.YearPage;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;

import static java.util.Objects.requireNonNull;

/**
 * A specialized event class used by date controls to request that the UI shows
 * a given date, dateTime, month, or year. This event gets, for example, fired
 * when the user double clicks on a day in the {@link YearPage} of the
 * {@link CalendarView}. The {@link CalendarView} will then automatically switch
 * to the {@link DayPage}.
 */
public final class RequestEvent extends Event {

    private static final long serialVersionUID = -5343700719205046646L;

    /**
     * An event type used to request that the UI shows a certain granularity.
     */
    public static final EventType<RequestEvent> REQUEST = new EventType<>(
            RequestEvent.ANY, "REQUEST"); //$NON-NLS-1$

    /**
     * An event type used to request that the UI shows a certain granularity.
     */
    public static final EventType<RequestEvent> REQUEST_DATE = new EventType<>(
            RequestEvent.REQUEST, "REQUEST_DATE"); //$NON-NLS-1$

    /**
     * An event type used to request that the UI shows a certain granularity.
     */
    public static final EventType<RequestEvent> REQUEST_DATE_TIME = new EventType<>(
            RequestEvent.REQUEST, "REQUEST_DATE_TIME"); //$NON-NLS-1$

    /**
     * An event type used to request that the UI shows a certain granularity.
     */
    public static final EventType<RequestEvent> REQUEST_YEAR_MONTH = new EventType<>(
            RequestEvent.REQUEST, "REQUEST_YEAR_MONTH"); //$NON-NLS-1$

    /**
     * An event type used to request that the UI shows a certain granularity.
     */
    public static final EventType<RequestEvent> REQUEST_YEAR = new EventType<>(
            RequestEvent.REQUEST, "REQUEST_YEAR"); //$NON-NLS-1$

    /**
     * An event type used to request that the UI shows a certain week.
     */
    public static final EventType<RequestEvent> REQUEST_WEEK = new EventType<>(
            RequestEvent.REQUEST, "REQUEST_WEEK"); //$NON-NLS-1$

    /**
     * An event type used to request that the UI shows a certain entry.
     */
    public static final EventType<RequestEvent> REQUEST_ENTRY = new EventType<>(
            RequestEvent.REQUEST, "REQUEST_ENTRY"); //$NON-NLS-1$

    private LocalDate date;

    private LocalDateTime dateTime;

    private YearMonth yearMonth;

    private Year year;

    private Entry<?> entry;

    private int weekOfYear;

    /**
     * Constructs a new request event.
     *
     * @param source
     *            the event source
     * @param target
     *            the event target
     * @param date
     *            the day to show
     */
    public RequestEvent(Object source, EventTarget target, LocalDate date) {
        super(source, target, REQUEST_DATE);

        this.date = requireNonNull(date);
    }

    /**
     * Constructs a new request event.
     *
     * @param source
     *            the event source
     * @param target
     *            the event target
     * @param dateTime
     *            the day and dateTime to show
     */
    public RequestEvent(Object source, EventTarget target, LocalDateTime dateTime) {
        super(source, target, REQUEST_DATE_TIME);

        this.dateTime = requireNonNull(dateTime);
    }

    /**
     * Constructs a new request event.
     *
     * @param source
     *            the event source
     * @param target
     *            the event target
     * @param year
     *            the year
     * @param weekOfYear
     *            the week of year number
     */
    public RequestEvent(Object source, EventTarget target, Year year, int weekOfYear) {
        super(source, target, REQUEST_WEEK);

        this.year = requireNonNull(year);
        this.weekOfYear = weekOfYear;
    }

    /**
     * Constructs a new request event.
     *
     * @param source
     *            the event source
     * @param target
     *            the event target
     * @param yearMonth
     *            the year month to show
     */
    public RequestEvent(Object source, EventTarget target, YearMonth yearMonth) {
        super(source, target, REQUEST_YEAR_MONTH);

        this.yearMonth = requireNonNull(yearMonth);
    }

    /**
     * Constructs a new request event.
     *
     * @param source
     *            the event source
     * @param target
     *            the event target
     * @param year
     *            the year to show
     */
    public RequestEvent(Object source, EventTarget target, Year year) {
        super(source, target, REQUEST_YEAR);

        this.year = requireNonNull(year);
    }

    /**
     * Constructs a new request event.
     *
     * @param source
     *            the event source
     * @param target
     *            the event target
     * @param entry
     *            the entry to show
     */
    public RequestEvent(Object source, EventTarget target, Entry<?> entry) {
        super(source, target, REQUEST_ENTRY);

        this.entry = requireNonNull(entry);
    }

    /**
     * Returns the requested date.
     *
     * @return the date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the requested date and time.
     *
     * @return the date and time
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Returns the requested year and month.
     *
     * @return the requested year and month
     */
    public YearMonth getYearMonth() {
        return yearMonth;
    }

    /**
     * Returns the requested year.
     *
     * @return the requested year
     */
    public Year getYear() {
        return year;
    }

    /**
     * Returns the requested entry.
     *
     * @return the requested entry
     */
    public Entry<?> getEntry() {
        return entry;
    }

    /**
     * Returns the requested week of year.
     *
     * @return the requested week of year
     */
    public int getWeekOfYear() {
        return weekOfYear;
    }

    @Override
    public String toString() {
        return "RequestEvent [date=" + date + ", dateTime=" + dateTime //$NON-NLS-1$ //$NON-NLS-2$
                + ", yearMonth=" + yearMonth + ", year=" + year + ", entry=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + entry + ", weekOfYear" + weekOfYear + ", eventType=" + eventType + ", target=" + target //$NON-NLS-1$ //$NON-NLS-2$
                + ", consumed=" + consumed + ", source=" + source + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
