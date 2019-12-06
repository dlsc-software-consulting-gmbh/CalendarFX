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
import com.google.api.services.calendar.model.Calendar;

import java.time.ZoneId;

/**
 * BeanConverter between calendarfx and google api.
 *
 * Created by gdiaz on 26/03/2017.
 */
public class GoogleCalendarToCalendarConverter implements BeanConverter<GoogleCalendar, Calendar> {
    @Override
    public Calendar convert(GoogleCalendar source) {
        Calendar target = new Calendar();
        target.setSummary(source.getName());
        target.setTimeZone(ZoneId.systemDefault().getId());
        return target;
    }
}
