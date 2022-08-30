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

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl.Layout;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import impl.com.calendarfx.view.CalendarPropertySheet;
import javafx.scene.Node;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class HelloTimezones extends CalendarFXSample {

    private final DayView dayView = new DayView();

    @Override
    public String getSampleName() {
        return "Time Zones";
    }

    @Override
    public Node getControlPanel() {
        return new CalendarPropertySheet(dayView.getPropertySheetItems());
    }

    protected Node createControl() {
        CalendarSource calendarSource = new CalendarSource();

        ParisCalendar calendar = new ParisCalendar();
        calendar.setStyle(Style.STYLE1);
        calendarSource.getCalendars().add(calendar);

        LondonCalendar timeZoneCalendar = new LondonCalendar(calendar);
        timeZoneCalendar.setStyle(Style.STYLE2);
        calendarSource.getCalendars().add(timeZoneCalendar);

        dayView.setZoneId(ZoneId.of("Europe/Paris"));
        dayView.setEarlyLateHoursStrategy(EarlyLateHoursStrategy.SHOW_COMPRESSED);
        dayView.setStartTime(LocalTime.of(6, 0));
        dayView.setEndTime(LocalTime.of(20, 0));
        dayView.setLayout(Layout.SWIMLANE);
        dayView.getCalendarSources().setAll(calendarSource);
        dayView.setHoursLayoutStrategy(DayViewBase.HoursLayoutStrategy.FIXED_HOUR_HEIGHT);
        dayView.setHourHeight(20);
        dayView.setPrefWidth(200);
        return dayView;
    }

    @Override
    public String getSampleDescription() {
        return "The day view displays a single day.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DayView.class;
    }

    class LondonCalendar extends Calendar {
        public LondonCalendar(ParisCalendar input) {
            Instant earliestTimeUsed = input.getEarliestTimeUsed();
            Instant latestTimeUsed = input.getLatestTimeUsed();
            Map<LocalDate, List<Entry<?>>> entries = input.findEntries(LocalDate.ofInstant(earliestTimeUsed, ZoneId.systemDefault()), LocalDate.ofInstant(latestTimeUsed, ZoneId.systemDefault()), ZoneId.systemDefault());
            entries.values().forEach(entryList -> {
                entryList.forEach(e -> {
                    Entry<?> copiedEntry = new Entry<>();
                    copiedEntry.setTitle(e.getTitle().replace("Paris", "Chicago"));
                    copiedEntry.setInterval(e.getStartDate(), e.getStartTime(), e.getEndDate(), e.getEndTime(), ZoneId.of("America/Chicago"));
                    addEntries(copiedEntry);
                });
            });
        }
    }

    class ParisCalendar extends Calendar {

        public ParisCalendar() {
            LocalDate date = LocalDate.now();

            ZoneId zoneId = ZoneId.of("Europe/Paris");

            Entry<?> entry = new Entry<>();
            entry.setTitle("Paris");

            int hour = 2;
            int durationInHours = 4;

            LocalTime startTime = LocalTime.of(hour, 0);
            LocalTime endTime = startTime.plusHours(durationInHours);

            entry.setInterval(date, startTime, date, endTime, zoneId);

            addEntry(entry);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
