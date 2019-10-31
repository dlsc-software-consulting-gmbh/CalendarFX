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

import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntryReminder;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.EventReminder;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts from google api to calendarfx api.
 *
 * Created by gdiaz on 20/02/2017.
 */
public final class CalendarListEntryToGoogleCalendarConverter implements BeanConverter<CalendarListEntry, GoogleCalendar> {
    @Override
    public GoogleCalendar convert(CalendarListEntry source) {
        GoogleCalendar calendar = new GoogleCalendar();
        calendar.setId(source.getId());
        calendar.setName(source.getSummary());
        calendar.setShortName(source.getSummary());
        calendar.setPrimary(source.isPrimary());
        calendar.setReadOnly(GoogleCalendar.isReadOnlyAccessRole(source.getAccessRole()));
        List<GoogleEntryReminder> reminders = new ArrayList<>();
        if (source.getDefaultReminders() != null) {
            for (EventReminder r : source.getDefaultReminders()) {
                reminders.add(new GoogleEntryReminder(r));
            }
        }
        calendar.getDefaultReminders().setAll(reminders);
        return calendar;
    }
}
