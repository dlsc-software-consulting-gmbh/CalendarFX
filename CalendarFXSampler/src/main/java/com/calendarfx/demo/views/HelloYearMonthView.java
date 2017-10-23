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
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.YearMonthView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

public class HelloYearMonthView extends CalendarFXDateControlSample {

    private YearMonthView yearMonthView;

    @Override
    public String getSampleName() {
        return "Year Month View";
    }

    @Override
    protected DateControl createControl() {
        yearMonthView = new YearMonthView();

        CalendarSource calendarSource = new CalendarSource();
        calendarSource.getCalendars().add(new HelloCalendar(yearMonthView.getYearMonth()));

        yearMonthView.getCalendarSources().add(calendarSource);

        return yearMonthView;
    }

    @Override
    public String getSampleDescription() {
        return "The month view displays the month of a given date.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return YearMonthView.class;
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
                    entry.changeStartDate(date);
                    entry.changeEndDate(date);

                    entry.setTitle("Entry " + (j + 1));

                    int hour = (int) (Math.random() * 23);
                    int durationInHours = Math.min(24 - hour,
                            (int) (Math.random() * 4));

                    LocalTime startTime = LocalTime.of(hour, 0);
                    LocalTime endTime = startTime.plusHours(durationInHours);

                    entry.changeStartTime(startTime);
                    entry.changeEndTime(endTime);

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
