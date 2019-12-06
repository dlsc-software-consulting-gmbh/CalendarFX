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

package com.calendarfx.google.view.data;

import com.calendarfx.google.model.GoogleCalendar;

/**
 * Provider of the google calendar data.
 *
 * Created by gdiaz on 5/05/2017.
 */
public interface IGoogleCalendarDataProvider {

    default GoogleCalendarData getCalendarData(GoogleCalendar calendar) {
        return getCalendarData(calendar, false);
    }

    GoogleCalendarData getCalendarData(GoogleCalendar calendar, boolean create);

    void removeCalendarData(GoogleCalendar calendar);

    void clearData();
}
