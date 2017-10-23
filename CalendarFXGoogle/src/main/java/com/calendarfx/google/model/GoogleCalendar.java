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
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Calendar class that encapsulates the logic of a Google Calendar Entry.
 *
 * @author Gabriel Diaz, 10.02.2015.
 */
public class GoogleCalendar extends Calendar {

    private static final String ACCESS_ROLE_READER = "reader";

    private static final String ACCESS_ROLE_FREE_BUSY_READER = "freeBusyReader";

    /**
     * Static field used to enumerate the entries created in this calendar.
     */
    private static int entryConsecutive = 1;

    /**
     * Constructs a calendar using the given calendar service.
     */
    public GoogleCalendar() {
        super();
    }

    /**
     * Generates a new consecutive number each time is called. This always
     * starts at 1.
     *
     * @return The consecutive generated.
     */
    private static int generateEntryConsecutive() {
        return entryConsecutive++;
    }

    /**
     * Represents the ID of the backend calendar entry, this field is generated
     * only by Google.
     */
    private String id;

    /**
     * Gets the ID of the backend calendar entry, can be null if this calendar
     * has not been saved in Google.
     *
     * @return The ID of the backend calendar entry.
     */
    public final String getId() {
        return id;
    }

    /**
     * Sets the ID for client side use; this is supposed to be called when this
     * calendar is associated with its backend calendar entry.
     *
     * @param id
     *            The new ID.
     */
    public final void setId(String id) {
        this.id = id;
    }

    /**
     * Indicating this calendar is the primary of the google account.
     */
    private boolean primary;

    /**
     * Gets the flag that indicates whether this calendar is primary or not. A
     * primary calendar can be seen as the default calendar of the account.
     *
     * @return {@code true} when this calendar is primary, otherwise
     *         {@code false}.
     */
    public final boolean isPrimary() {
        return primary;
    }

    /**
     * Sets the primary flag, supposed to be called when this calendar is
     * associated with a backend calendar entry.
     *
     * @param primary
     *            The value for the flag.
     */
    public final void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * List storing the default reminders of the calendar, represents the
     * default configuration for all the entries of this calendar and is
     * supposed to be inherited.
     */
    private final ObservableList<GoogleEntryReminder> defaultReminders = FXCollections.observableArrayList();

    /**
     * Gets the list of default reminders configured for this calendar.
     *
     * @return The default remind configuration.
     */
    public final ObservableList<GoogleEntryReminder> getDefaultReminders() {
        return defaultReminders;
    }

    /**
     * Creates a new google entry by using the given parameters, this assigns a
     * default name by using a consecutive number. The entry is of course
     * associated to this calendar, but it is not sent to google for storing.
     *
     * @param start
     *            The start date/time of the new entry.
     * @param fullDay
     *            A flag indicating if the new entry is going to be full day.
     * @return The new google entry created, this is not send to server side.
     * @see #generateEntryConsecutive()
     */
    public final GoogleEntry createEntry(ZonedDateTime start, boolean fullDay) {
        GoogleEntry entry = new GoogleEntry();
        entry.setTitle("New Entry " + generateEntryConsecutive());
        entry.setInterval(new Interval(start.toLocalDate(), start.toLocalTime(), start.toLocalDate(), start.toLocalTime().plusHours(1)));
        entry.setFullDay(fullDay);
        entry.setAttendeesCanInviteOthers(true);
        entry.setAttendeesCanSeeOthers(true);
        return entry;
    }

    /**
     * Indicates whether the calendar exist in google calendar or not.
     *
     * @return a flag saying whether this calendar was already persisted or not.
     */
    public final boolean existsInGoogle() {
        return id != null;
    }

    /**
     * Checks whether the given access role means the calendar is read only.
     *
     * @param accessRole The access role to be analyzed.
     * @return {@code true} if the access role matches {@link #ACCESS_ROLE_READER}
     * or {@link #ACCESS_ROLE_FREE_BUSY_READER}.
     */
    public static boolean isReadOnlyAccessRole(String accessRole) {
        return ACCESS_ROLE_READER.equals(accessRole) || ACCESS_ROLE_FREE_BUSY_READER.equals(accessRole);
    }

    /**
     * Consumer to delegate the loading of entries to an external provider.
     */
    private IGoogleCalendarSearchTextProvider searchTextProvider;

    public void setSearchTextProvider(IGoogleCalendarSearchTextProvider searchTextProvider) {
        this.searchTextProvider = searchTextProvider;
    }

    @Override
    public List<Entry<?>> findEntries(String text) {
        if (searchTextProvider != null) {
            searchTextProvider.search(this, text);
        }
        return super.findEntries(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoogleCalendar that = (GoogleCalendar) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
