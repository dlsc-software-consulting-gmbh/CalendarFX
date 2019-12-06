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

package com.calendarfx.google.converter;

import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.model.GoogleEntryReminder;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * BeanConverter between entry (calendarfx api) and event (google api).
 *
 * Created by gdiaz on 20/02/2017.
 */
public final class GoogleEntryToEventConverter implements BeanConverter<GoogleEntry, Event> {
    @Override
    public Event convert(GoogleEntry source) {
        EventDateTime startTime = new EventDateTime();
        EventDateTime endTime = new EventDateTime();

        ZonedDateTime st = source.getStartAsZonedDateTime();
        ZonedDateTime et = source.getEndAsZonedDateTime();
        TimeZone timeZone = TimeZone.getTimeZone(source.getZoneId());

        if (source.getEndTime().equals(LocalTime.MAX)) {
            et = et.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        }

        if (source.isFullDay()) {
            startTime.setDate(new DateTime(true, st.toInstant().toEpochMilli(), 0));
            startTime.setTimeZone(timeZone.getID());
            endTime.setDate(new DateTime(true, et.toInstant().toEpochMilli(), 0));
            endTime.setTimeZone(timeZone.getID());
        } else {
            startTime.setDateTime(new DateTime(Date.from(st.toInstant())));
            startTime.setTimeZone(timeZone.getID());
            endTime.setDateTime(new DateTime(Date.from(et.toInstant())));
            endTime.setTimeZone(timeZone.getID());
        }

        Event event = new Event();

        if (source.getRecurrenceRule() != null) {
            List<String> recurrence = new ArrayList<>();
            recurrence.add(source.getRecurrenceRule());
            event.setRecurrence(recurrence);
        }

        Event.Reminders reminders = new Event.Reminders();
        reminders.setUseDefault(source.isUseDefaultReminder());
        if (source.getReminders() != null) {
            List<EventReminder> overrides = new ArrayList<>();
            for (GoogleEntryReminder reminder : source.getReminders()) {
                EventReminder override = new EventReminder();
                override.setMethod(reminder.getMethod().getId());
                override.setMinutes(reminder.getMinutes());
                overrides.add(override);
            }
            reminders.setOverrides(overrides);
        }

        event.setId(source.existsInGoogle() ? source.getId() : null);
        event.setSummary(source.getTitle());
        event.setStart(startTime);
        event.setEnd(endTime);
        event.setAttendees(source.getAttendees());
        event.setGuestsCanModify(source.isAttendeesCanModify());
        event.setGuestsCanInviteOthers(source.isAttendeesCanInviteOthers());
        event.setGuestsCanSeeOtherGuests(source.isAttendeesCanSeeOthers());
        event.setLocation(source.getLocation());
        event.setReminders(reminders);

        return event;
    }
}
