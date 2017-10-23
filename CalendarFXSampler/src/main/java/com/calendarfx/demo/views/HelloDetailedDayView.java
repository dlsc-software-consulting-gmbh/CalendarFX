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
import com.calendarfx.view.DetailedDayView;

import java.time.LocalDate;
import java.time.LocalTime;

public class HelloDetailedDayView extends CalendarFXDateControlSample {

    private DetailedDayView dayView;

    @Override
    public String getSampleName() {
        return "Detailed Day View";
    }

    @Override
    public String getSampleDescription() {
        return "The detailed day view aggregates a day view, an all day view, a calendar header (for swimlane layout), and a time scale.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DetailedDayView.class;
    }

    @Override
    protected DateControl createControl() {
        dayView = new DetailedDayView();

        CalendarSource calendarSource = new CalendarSource();
        HelloCalendar calendar1 = new HelloCalendar();
        HelloCalendar calendar2 = new HelloCalendar();
        HelloCalendar calendar3 = new HelloCalendar();
        HelloCalendar calendar4 = new HelloCalendar();

        calendar1.setName("Calendar 1");
        calendar1.setShortName("C1");
        calendar2.setName("Calendar 2");
        calendar2.setShortName("C2");
        calendar3.setName("Calendar 3");
        calendar3.setShortName("C3");
        calendar4.setName("Calendar 4");
        calendar4.setShortName("C4");

        calendar1.setStyle(Calendar.Style.STYLE1);
        calendar2.setStyle(Calendar.Style.STYLE2);
        calendar3.setStyle(Calendar.Style.STYLE3);
        calendar4.setStyle(Calendar.Style.STYLE4);

        calendarSource.getCalendars().setAll(calendar1, calendar2, calendar3, calendar4);

        dayView.getCalendarSources().setAll(calendarSource);

        return dayView;
    }

    class HelloCalendar extends Calendar {

        public HelloCalendar() {
            LocalDate date = LocalDate.now();

            for (int i = 1; i < 3; i++) {

                Entry<?> entry = new Entry<>();
                entry.changeStartDate(date);
                entry.changeEndDate(date);

                entry.setTitle("Entry " + i);

                int hour = (int) (Math.random() * 23);
                int durationInHours = Math.min(24 - hour,
                        (int) (Math.random() * 4));

                LocalTime startTime = LocalTime.of(hour, 0);
                LocalTime endTime = startTime.plusHours(durationInHours);

                entry.changeStartTime(startTime);
                entry.changeEndTime(endTime);

                if (Math.random() < .1) {
                    entry.setFullDay(true);
                    entry.setTitle("Full Day Entry");
                }

                entry.setCalendar(this);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
