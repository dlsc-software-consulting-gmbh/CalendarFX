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
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.page.MonthPage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

public class HelloMonthPage extends CalendarFXDateControlSample {

    private MonthPage monthPage;

    @Override
    protected DateControl createControl() {
        monthPage = new MonthPage();

        CalendarSource calendarSource = new CalendarSource();
        HelloCalendar calendar1 = new HelloCalendar(monthPage.getMonthView().getYearMonth());
        HelloCalendar calendar2 = new HelloCalendar(monthPage.getMonthView().getYearMonth());
        HelloCalendar calendar3 = new HelloCalendar(monthPage.getMonthView().getYearMonth());
        HelloCalendar calendar4 = new HelloCalendar(monthPage.getMonthView().getYearMonth());

        calendar1.setStyle(Style.STYLE1);
        calendar2.setStyle(Style.STYLE2);
        calendar3.setStyle(Style.STYLE3);
        calendar4.setStyle(Style.STYLE4);

        calendarSource.getCalendars().addAll(calendar1, calendar2, calendar3, calendar4);

        monthPage.getCalendarSources().add(calendarSource);

        return monthPage;
    }

    @Override
    public String getSampleName() {
        return "Month Page";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return MonthPage.class;
    }

    @Override
    public String getSampleDescription() {
        return "The month page displays the calendar information for a full month.";
    }

    class HelloCalendar extends Calendar {

        public HelloCalendar(YearMonth month) {
            createEntries(month);
        }

        private void createEntries(YearMonth month) {
            for (int i = 1; i < 28; i++) {

                LocalDate date = month.atDay(i);

                for (int j = 0; j < (int) (Math.random() * 7); j++) {
                    Entry<?> entry = new Entry<>();

                    entry.setTitle("Entry " + (j + 1));

                    int hour = (int) (Math.random() * 22);
                    int durationInHours = Math.min(24 - hour,
                            (int) (Math.random() * 4));

                    LocalTime startTime = LocalTime.of(hour, 0);
                    LocalTime endTime = startTime.plusHours(durationInHours);

                    entry.setInterval(date, startTime, date.plusDays((int) (Math.random() * 4)), endTime);

                    if (Math.random() < .3) {
                        entry.setFullDay(true);
                    }

                    entry.setCalendar(this);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
