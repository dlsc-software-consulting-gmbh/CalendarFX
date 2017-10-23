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

package com.calendarfx.google.service;

import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntry;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulation of the google calendar service that provides some methods for
 * CRUD operations on calendars and events.
 *
 * @author Gabriel Diaz, 14.03.2015.
 */
public class GoogleCalendarService {

    private final Calendar dao;
    private final BeanConverterService converter;

    GoogleCalendarService(Calendar dao) {
        this.dao = Objects.requireNonNull(dao);
        this.converter = BeanConverterService.getInstance();
    }

    /**
     * Inserts a calendar into the google calendar.
     *
     * @param calendar The calendar to be inserted.
     * @throws IOException For unexpected errors.
     */
    public void insertCalendar(GoogleCalendar calendar) throws IOException {
        com.google.api.services.calendar.model.Calendar cal;
        cal = converter.convert(calendar, com.google.api.services.calendar.model.Calendar.class);
        cal = dao.calendars().insert(cal).execute();
        calendar.setId(cal.getId());
    }

    /**
     * Saves the updates done on the calendar into google calendar api.
     *
     * @param calendar The calendar to be updated.
     * @throws IOException For unexpected errors.
     */
    public void updateCalendar(GoogleCalendar calendar) throws IOException {
        CalendarListEntry calendarListEntry = converter.convert(calendar, CalendarListEntry.class);
        dao.calendarList().update(calendarListEntry.getId(), calendarListEntry).execute();
    }

    /**
     * Performs an immediate delete request on the google calendar api.
     *
     * @param calendar The calendar to be removed.
     * @throws IOException For unexpected errors
     */
    public void deleteCalendar(GoogleCalendar calendar) throws IOException {
        dao.calendars().delete(calendar.getId()).execute();
    }

    /**
     * Performs an immediate insert operation on google server by sending the
     * information provided by the given google entry. The entry is associated
     * to this calendar.
     *
     * @param entry The entry to be inserted in a backend google calendar.
     * @param calendar The calendar in which the entry will be inserted.
     * @return The same instance received.
     * @throws IOException For unexpected errors
     */
    public GoogleEntry insertEntry(GoogleEntry entry, GoogleCalendar calendar) throws IOException {
        Event event = converter.convert(entry, Event.class);
        event = dao.events().insert(calendar.getId(), event).execute();
        entry.setId(event.getId());
        entry.setUserObject(event);
        return entry;
    }

    /**
     * Performs an immediate update operation on google server by sending the
     * information stored by the given google entry.
     *
     * @param entry The entry to be updated in a backend google calendar.
     * @return The same instance received.
     * @throws IOException For unexpected errors
     */
    public GoogleEntry updateEntry(GoogleEntry entry) throws IOException {
        GoogleCalendar calendar = (GoogleCalendar) entry.getCalendar();
        Event event = converter.convert(entry, Event.class);
        dao.events().update(calendar.getId(), event.getId(), event).execute();
        return entry;
    }

    /**
     * Sends a delete request to the google server for the given entry.
     *
     * @param entry The entry to be deleted from the backend google calendar.
     * @param calendar The calendar from the entry was deleted.
     * @throws IOException For unexpected errors.
     */
    public void deleteEntry(GoogleEntry entry, GoogleCalendar calendar) throws IOException {
        dao.events().delete(calendar.getId(), entry.getId()).execute();
    }

    /**
     * Moves an entry from one calendar to another.
     *
     * @param entry The entry to be moved.
     * @param from The current calendar.
     * @param to The future calendar.
     * @return The entry updated.
     * @throws IOException For unexpected errors.
     */
    public GoogleEntry moveEntry(GoogleEntry entry, GoogleCalendar from, GoogleCalendar to) throws IOException {
        dao.events().move(from.getId(), entry.getId(), to.getId()).execute();
        return entry;
    }

    /**
     * Gets the list of all calendars available in the account.
     *
     * @return A non-null list of all calendars.
     * @throws IOException For unexpected errors.
     */
    public List<GoogleCalendar> getCalendars() throws IOException {
        List<CalendarListEntry> calendarListEntries = dao.calendarList().list().execute().getItems();
        List<GoogleCalendar> calendars = new ArrayList<>();

        if (calendarListEntries != null && !calendarListEntries.isEmpty()) {
            for (int i = 0; i < calendarListEntries.size(); i++) {
                CalendarListEntry calendarListEntry = calendarListEntries.get(i);
                GoogleCalendar calendar = converter.convert(calendarListEntry, GoogleCalendar.class);
                calendar.setStyle(com.calendarfx.model.Calendar.Style.getStyle(i));
                calendars.add(calendar);
            }
        }

        return calendars;
    }

    /**
     * Gets a list of entries belonging to the given calendar defined between the given range of time. Recurring events
     * are not expanded, always recurrence is handled manually within the framework.
     *
     * @param calendar The calendar owner of the entries.
     * @param startDate The start date, not nullable.
     * @param endDate The end date, not nullable
     * @param zoneId The timezone in which the dates are represented.
     * @return A non-null list of entries.
     * @throws IOException For unexpected errors
     */
    public List<GoogleEntry> getEntries(GoogleCalendar calendar, LocalDate startDate, LocalDate endDate, ZoneId zoneId) throws IOException {
        if (!calendar.existsInGoogle()) {
            return new ArrayList<>(0);
        }

        ZonedDateTime st = ZonedDateTime.of(startDate, LocalTime.MIN, zoneId);
        ZonedDateTime et = ZonedDateTime.of(endDate, LocalTime.MAX, zoneId);
        String calendarId = URLDecoder.decode(calendar.getId(), "UTF-8");

        List<Event> events = dao.events()
                .list(calendarId)
                .setTimeMin(new DateTime(Date.from(st.toInstant())))
                .setTimeMax(new DateTime(Date.from(et.toInstant())))
                .setSingleEvents(false)
                .setShowDeleted(false)
                .execute()
                .getItems();

        return toGoogleEntries(events);
    }

    /**
     * Gets a list of entries that matches the given text. Recurring events
     * are not expanded, always recurrence is handled manually within the framework.
     *
     * @param calendar The calendar owner of the entries.
     * @param searchText The search text
     * @return A non-null list of entries.
     * @throws IOException For unexpected errors
     */
    public List<GoogleEntry> getEntries(GoogleCalendar calendar, String searchText) throws IOException {
        if (!calendar.existsInGoogle()) {
            return new ArrayList<>(0);
        }

        String calendarId = URLDecoder.decode(calendar.getId(), "UTF-8");

        List<Event> events = dao.events()
                .list(calendarId)
                .setQ(searchText)
                .setSingleEvents(false)
                .setShowDeleted(false)
                .execute()
                .getItems();

        return toGoogleEntries(events);
    }

    private List<GoogleEntry> toGoogleEntries(List<Event> events) {
        List<GoogleEntry> entries = new ArrayList<>();
        if (events != null && !events.isEmpty()) {
            for (Event event : events) {
                entries.add(converter.convert(event, GoogleEntry.class));
            }
        }
        return entries;
    }

}
