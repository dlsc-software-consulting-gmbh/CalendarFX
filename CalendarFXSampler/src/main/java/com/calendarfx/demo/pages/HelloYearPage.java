/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *  Copyright (C) 2006 Google Inc.
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

package com.calendarfx.demo.pages;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.page.YearPage;

public class HelloYearPage extends CalendarFXDateControlSample {

    private YearPage yearPage;

    @Override
    protected DateControl createControl() {
        CalendarSource calendarSource = new CalendarSource("My Calendars");
        calendarSource.getCalendars().add(new Calendar("Test"));

        yearPage = new YearPage();
        yearPage.getCalendarSources().add(calendarSource);

        return yearPage;
    }

    @Override
    public String getSampleName() {
        return "Year Page";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return YearPage.class;
    }

    @Override
    public String getSampleDescription() {
        return "The year page displays the calendar information for a full year.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
