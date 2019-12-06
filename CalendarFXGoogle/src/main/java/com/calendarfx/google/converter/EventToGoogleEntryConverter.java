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
import com.calendarfx.model.Interval;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventReminder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * BeanConverter between event (google api) and entry (calendarfx api).
 *
 * Created by gdiaz on 20/02/2017.
 */
public final class EventToGoogleEntryConverter implements BeanConverter<Event, GoogleEntry> {

    @Override
    public GoogleEntry convert(Event source) {
        Objects.requireNonNull(source.getStart());
        Objects.requireNonNull(source.getEnd());

        GoogleEntry entry = new GoogleEntry();

        /*
         * TimeZone : Although Google allows different Start/End TimeZone, we
         * always assume that start/end are in the TimeZone of the startDate.
         */
        ZoneId zoneId;
        String stTimeZone = source.getStart().getTimeZone();

        if (stTimeZone == null) {
            zoneId = ZoneId.systemDefault();
        } else {
            zoneId = TimeZone.getTimeZone(stTimeZone).toZoneId();
        }

        // Start Time
        DateTime stdt = source.getStart().getDate();
        if (stdt == null) {
            stdt = source.getStart().getDateTime();
        }
        ZoneOffset stOffset = ZoneOffset.ofTotalSeconds(stdt.getTimeZoneShift() * 60);
        OffsetDateTime offsetSt = Instant.ofEpochMilli(stdt.getValue()).atOffset(stOffset);

        // End Time
        DateTime etdt = source.getEnd().getDate();
        if (etdt == null) {
            etdt = source.getEnd().getDateTime();
        }
        ZoneOffset etoffset = ZoneOffset.ofTotalSeconds(etdt.getTimeZoneShift() * 60);
        OffsetDateTime offsetEt = Instant.ofEpochMilli(etdt.getValue()).atOffset(etoffset);

        LocalDateTime startDateTime = offsetSt.toLocalDateTime();
        if (stdt.isDateOnly() || offsetEt.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            offsetEt = offsetEt.minusDays(1);
            entry.setInterval(new Interval(startDateTime.toLocalDate(), startDateTime.toLocalTime(), offsetEt.toLocalDate(), LocalTime.MAX));
        } else {
            entry.setInterval(new Interval(startDateTime, offsetEt.toLocalDateTime()));
        }

        if (source.getRecurrence() != null && !source.getRecurrence().isEmpty()) {
            entry.setRecurrenceRule(source.getRecurrence().get(0));
        }

        if (source.getAttendees() != null) {
            entry.getAttendees().setAll(source.getAttendees());
        }

        if (source.getReminders() != null) {
            Event.Reminders reminders = source.getReminders();

            if (Boolean.TRUE.equals(reminders.getUseDefault())) {
                entry.setUseDefaultReminder(true);
            } else if (reminders.getOverrides() != null) {
                List<GoogleEntryReminder> entryReminders = new ArrayList<>();
                for (EventReminder reminder : reminders.getOverrides()) {
                    entryReminders.add(new GoogleEntryReminder(reminder));
                }
                entry.getReminders().setAll(entryReminders);
            }
        }

        entry.setId(source.getId());
        entry.setTitle(source.getSummary());
        entry.setLocation(source.getLocation());
        entry.setZoneId(zoneId);
        entry.setFullDay(stdt.isDateOnly());
        entry.setAttendeesCanModify(source.isGuestsCanModify());
        entry.setAttendeesCanInviteOthers(source.isGuestsCanInviteOthers());
        entry.setAttendeesCanSeeOthers(source.isGuestsCanSeeOtherGuests());
        entry.setStatus(GoogleEntry.Status.fromName(source.getStatus()));
        entry.setUserObject(source);

        return entry;
    }
}
