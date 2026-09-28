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

package com.calendarfx.appointments;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Resource;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;

/**
 * A little debugging utility that prints all {@link CalendarEvent} instances
 * fired by a calendar to the console. Useful for observing what happens in the
 * model while entries get created, edited, or dragged around in the views.
 */
public final class CalendarEventLogger {

    private CalendarEventLogger() {
    }

    /**
     * Attaches a logging handler to the given calendar.
     *
     * @param context a short text that identifies where the calendar is used
     * @param calendar the calendar to observe
     */
    public static void attach(String context, Calendar calendar) {
        if (calendar == null) {
            return;
        }

        EventHandler<CalendarEvent> handler = evt -> System.out.println(format(context, evt));
        calendar.addEventHandler(handler);
    }

    /**
     * Attaches a logging handler to all calendars of the given calendar source,
     * including those that get added later on.
     *
     * @param context a short text that identifies where the source is used
     * @param source the calendar source to observe
     */
    public static void attach(String context, CalendarSource source) {
        if (source == null) {
            return;
        }

        attachToAll(context, source.getCalendars());
    }

    /**
     * Attaches a logging handler to the availability calendar and to all
     * calendars of the given resource, including those that get added later on.
     *
     * @param resource the resource to observe
     */
    public static void attach(Resource<?> resource) {
        if (resource == null) {
            return;
        }

        String context = String.valueOf(resource);

        attach(context + " / availability", resource.getAvailabilityCalendar());
        attachToAll(context, resource.getCalendars());
    }

    private static void attachToAll(String context, ObservableList<Calendar> calendars) {
        calendars.forEach(calendar -> attach(context, calendar));
        calendars.addListener((ListChangeListener<Calendar>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(calendar -> attach(context, calendar));
                }
            }
        });
    }

    private static String format(String context, CalendarEvent evt) {
        StringBuilder sb = new StringBuilder();

        sb.append("[").append(context).append("] ").append(evt.getEventType().getName());

        Calendar calendar = evt.getCalendar();
        sb.append(" | calendar = ").append(calendar != null ? calendar.getName() : "null");

        Entry<?> entry = evt.getEntry();
        if (entry != null) {
            sb.append(" | entry = \"").append(entry.getTitle()).append("\" (").append(entry.getId()).append(")");
            sb.append(" | interval = ").append(entry.getInterval());
        }

        if (evt.getOldCalendar() != null) {
            sb.append(" | old calendar = ").append(evt.getOldCalendar().getName());
        }

        if (evt.getOldInterval() != null) {
            sb.append(" | old interval = ").append(evt.getOldInterval());
        }

        if (evt.isEntryAdded()) {
            sb.append(" | ENTRY ADDED");
        }

        if (evt.isEntryRemoved()) {
            sb.append(" | ENTRY REMOVED");
        }

        return sb.toString();
    }
}
