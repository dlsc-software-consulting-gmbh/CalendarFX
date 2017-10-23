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

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.MonthView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

public class HelloMonthView extends CalendarFXDateControlSample {

    private MonthView monthView;

    @Override
    public String getSampleName() {
        return "Month View";
    }

    @Override
    protected DateControl createControl() {
        monthView = new MonthView();

        CalendarSource calendarSource = new CalendarSource();
        HelloCalendar calendar1 = new HelloCalendar(monthView.getYearMonth());
        HelloCalendar calendar2 = new HelloCalendar(monthView.getYearMonth());
        HelloCalendar calendar3 = new HelloCalendar(monthView.getYearMonth());
        HelloCalendar calendar4 = new HelloCalendar(monthView.getYearMonth());

        calendar1.setName("Calendar 1");
        calendar2.setName("Calendar 2");
        calendar3.setName("Calendar 3");
        calendar4.setName("Calendar 4");

        calendar1.setStyle(Style.STYLE1);
        calendar2.setStyle(Style.STYLE2);
        calendar3.setStyle(Style.STYLE3);
        calendar4.setStyle(Style.STYLE4);

        calendarSource.getCalendars().setAll(calendar1, calendar2, calendar3, calendar4);

        monthView.getCalendarSources().setAll(calendarSource);

        return monthView;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return MonthView.class;
    }

    @Override
    public String getSampleDescription() {
        return "The month view displays the month of a given date.";
    }

    class HelloCalendar extends Calendar {

        public HelloCalendar(YearMonth month) {
            createEntries(month);
        }

        private void createEntries(YearMonth month) {
            for (int i = 1; i < 28; i++) {

                LocalDate date = month.atDay(i);

                for (int j = 0; j < (int) (Math.random() * 2); j++) {
                    Entry<?> entry = new Entry<>();

                    entry.setTitle("Entry " + (j + 1));

                    LocalDate startDate = date;
                    LocalDate endDate = startDate.plusDays((int) (Math.random() * 4));

                    int hour = (int) (Math.random() * 23);
                    int durationInHours = Math.min(23 - hour, (int) (Math.random() * 4));

                    LocalTime startTime = LocalTime.of(hour, 0);
                    LocalTime endTime = startTime.plusHours(durationInHours);

                    entry.setInterval(startDate, startTime, endDate, endTime);

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
