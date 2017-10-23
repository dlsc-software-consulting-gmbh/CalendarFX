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

package com.calendarfx.model;

import javafx.event.Event;
import javafx.event.EventType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * An event fired by the framework to inform the application that data will be
 * required for the time interval defined by the given start and end date. This
 * type of event can be used to implement a lazy loading strategy.
 *
 * <h2>Code Example</h2>
 * The following code snippet was taken from the Google calendar application included
 * in the distribution.
 * <br/>
 * <pre>
 * CalendarView calendarView = ...
 * calendarView.addEventFilter(LoadEvent.LOAD, evt -&gt; {
 *    for (CalendarSource source : evt.getCalendarSources()) {
 *      if (source instanceof GoogleAccount) {
 *         GoogleAccount account = (GoogleAccount) source;
 *
 *         Thread loadThread = new Thread() {
 *            public void run() {
 *               for (Calendar calendar : account.getCalendars()) {
 *                  GoogleCalendar googleCalendar = (GoogleCalendar) calendar;
 *                  googleCalendar.load(evt.getStartDate(), evt.getEndDate(), evt.getZoneId());
 *               }
 *            }
 *         };
 *
 *         loadThread.setDaemon(true);
 *         loadThread.start();
 *
 *         break;
 *      }
 *   }
 * });
 * </pre>
 */
public final class LoadEvent extends Event {

    private static final long serialVersionUID = -2691268182059394731L;

    /**
     * Gets fired frequently by the framework to indicate that data for the
     * given date range is required to be present in the calendars.
     */
    public static final EventType<LoadEvent> LOAD = new EventType<>(Event.ANY,
            "LOAD"); //$NON-NLS-1$

    private List<CalendarSource> calendarSources;

    private LocalDate startDate;

    private LocalDate endDate;

    private ZoneId zoneId;

    private String sourceName;

    /**
     * Constructs a new load event.
     *
     * @param eventType
     *            the type of the load event
     * @param sourceName
     *            the name of the source where the event originated, e.g.
     *            "DayView"
     * @param calendarSources
     *            the affected calendar sources
     * @param startDate
     *            the start date of the time interval
     * @param endDate
     *            the end date of the time interval
     * @param zoneId
     *            the time zone
     */
    public LoadEvent(EventType<LoadEvent> eventType, String sourceName,
                     List<CalendarSource> calendarSources, LocalDate startDate,
                     LocalDate endDate, ZoneId zoneId) {
        super(eventType);

        this.sourceName = requireNonNull(sourceName);
        this.calendarSources = requireNonNull(calendarSources);
        this.startDate = requireNonNull(startDate);
        this.endDate = requireNonNull(endDate);
        this.zoneId = requireNonNull(zoneId);
    }

    /**
     * A human readable name of the control that triggered the load event, e.g.
     * "Day View" or "Month View". Mainly used for debugging purposes.
     *
     * @return the name of the control requesting the data
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * The calendar sources that are affected by the load event.
     *
     * @return the calendar sources
     */
    public List<CalendarSource> getCalendarSources() {
        return calendarSources;
    }

    /**
     * The start of the loaded time interval.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * The end of the loaded time interval.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * The time zone used for the load.
     *
     * @return the time zone
     */
    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Convenience method to return a zoned date time based on the given start
     * date and time zone. Uses {@link LocalTime#MIN} as time.
     *
     * @return the start time defined by this event
     */
    public ZonedDateTime getStartTime() {
        return ZonedDateTime.of(startDate, LocalTime.MIN, zoneId);
    }

    /**
     * Convenience method to return a zoned date time based on the given end
     * date and time zone. Uses {@link LocalTime#MAX} as time.
     *
     * @return the start time defined by this event
     */
    public ZonedDateTime getEndTime() {
        return ZonedDateTime.of(endDate, LocalTime.MAX, zoneId);
    }

    @Override
    public String toString() {
        return "LoadEvent [sourceName=" + sourceName + ", startDate=" //$NON-NLS-1$ //$NON-NLS-2$
                + startDate + ", endDate=" + endDate + ", zoneId=" + zoneId //$NON-NLS-1$ //$NON-NLS-2$
                + ", calendarSources=" + calendarSources + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
