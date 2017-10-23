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

package com.calendarfx.google.model;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import javafx.collections.ListChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Google account of a determinate user, which is owner of
 * the Calendars information.
 *
 * @author Gabriel Diaz, 03.03.2015.
 */
public class GoogleAccount extends CalendarSource {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Creates one single calendar with the given name and style.
     *
     * @param name  The name of the calendar.
     * @param style The style of the calendar.
     * @return The new google calendar.
     */
    public final GoogleCalendar createCalendar(String name, Calendar.Style style) {
        GoogleCalendar calendar = new GoogleCalendar();
        calendar.setName(name);
        calendar.setStyle(style);
        return calendar;
    }

    /**
     * Gets the calendar marked as primary calendar for the google account.
     *
     * @return The primary calendar, {@code null} if not loaded.
     */
    public GoogleCalendar getPrimaryCalendar() {
        return (GoogleCalendar) getCalendars().stream()
                .filter(calendar -> ((GoogleCalendar) calendar).isPrimary())
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all the google calendars hold by this source.
     *
     * @return The list of google calendars, always a new list.
     */
    public List<GoogleCalendar> getGoogleCalendars() {
        List<GoogleCalendar> googleCalendars = new ArrayList<>();
        for (Calendar calendar : getCalendars()) {
            if (!(calendar instanceof GoogleCalendar)) {
                continue;
            }
            googleCalendars.add((GoogleCalendar) calendar);
        }
        return googleCalendars;
    }

    /**
     * Adds the given calendar listener in order to get notified when any calendar/entry changes.
     *
     * @param listeners The listener.
     */
    @SafeVarargs
    public final void addCalendarListeners(ListChangeListener<Calendar>... listeners) {
        if (listeners != null) {
            for (ListChangeListener<Calendar> listener : listeners) {
                getCalendars().addListener(listener);
            }
        }
    }

    /**
     * Removes the listener from the ones being notified.
     *
     * @param listeners The listener
     */
    @SafeVarargs
    public final void removeCalendarListeners(ListChangeListener<Calendar>... listeners) {
        if (listeners != null) {
            for (ListChangeListener<Calendar> listener : listeners) {
                getCalendars().removeListener(listener);
            }
        }
    }
}
