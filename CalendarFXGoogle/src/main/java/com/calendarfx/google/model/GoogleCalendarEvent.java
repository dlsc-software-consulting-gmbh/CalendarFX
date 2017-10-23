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

import com.calendarfx.model.CalendarEvent;
import javafx.event.EventType;

/**
 * Extension of the {@link CalendarEvent} which allows to know externally about
 * changes in {@link GoogleCalendar calendars}.
 *
 * @author Gabriel Diaz, 07.03.2015.
 */
public class GoogleCalendarEvent extends CalendarEvent {

    private static final long serialVersionUID = 8064360122175452019L;

    /**
     * Event type saying that attendees of the google entry have been changed.
     */
    public static final EventType<GoogleCalendarEvent> ENTRY_ATTENDEES_CHANGED = new EventType<>(
            ENTRY_CHANGED, "ENTRY_ATTENDEES_CHANGED");

    /**
     * Event type saying whether attendees can see others or not.
     */
    public static final EventType<GoogleCalendarEvent> ENTRY_ATTENDEES_CAN_SEE_OTHERS_CHANGED = new EventType<>(
            ENTRY_CHANGED, "ENTRY_ATTENDEES_CAN_SEE_OTHERS_CHANGED");

    /**
     * Event type saying whether attendees can invite others or not.
     */
    public static final EventType<GoogleCalendarEvent> ENTRY_ATTENDEES_CAN_INVITE_CHANGED = new EventType<>(
            ENTRY_CHANGED, "ENTRY_ATTENDEES_CAN_INVITE_CHANGED");

    /**
     * Event type saying whether attendees can edit the google entry others or
     * not.
     */
    public static final EventType<GoogleCalendarEvent> ENTRY_ATTENDEES_CAN_MODIFY_CHANGED = new EventType<>(
            ENTRY_CHANGED, "ENTRY_ATTENDEES_CAN_MODIFY_CHANGED");

    /**
     * Event type saying that reminders of the google entry have been changed.
     */
    public static final EventType<GoogleCalendarEvent> ENTRY_REMINDERS_CHANGED = new EventType<>(
            ENTRY_CHANGED, "ENTRY_REMINDERS_CHANGED");

    /**
     * Creates a google calendar event to notify some changes in the given
     * google entry.
     *
     * @param eventType
     *            The type of event.
     * @param calendar
     *            The calendar which the entry belongs to.
     * @param entry
     *            The entry affected.
     */
    GoogleCalendarEvent(EventType<GoogleCalendarEvent> eventType, GoogleCalendar calendar, GoogleEntry entry) {
        super(eventType, calendar, entry);
    }

}
