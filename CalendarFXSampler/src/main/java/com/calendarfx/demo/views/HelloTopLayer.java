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
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.EntryViewBase.AlignmentStrategy;
import impl.com.calendarfx.view.CalendarPropertySheet;
import javafx.scene.Node;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class HelloTopLayer extends CalendarFXSample {

    private final DayView dayView = new DayView();

    @Override
    public String getSampleName() {
        return "Day View with Top Layer";
    }

    @Override
    public Node getControlPanel() {
        return new CalendarPropertySheet(dayView.getPropertySheetItems());
    }

    protected Node createControl() {
        CalendarSource calendarSource = new CalendarSource();
        calendarSource.getCalendars().add(new BaseLayerCalendar());
        calendarSource.getCalendars().add(new TopLayerCalendar());
        dayView.getCalendarSources().setAll(calendarSource);
        dayView.setHoursLayoutStrategy(DayViewBase.HoursLayoutStrategy.FIXED_HOUR_HEIGHT);
        dayView.setHourHeight(20);
        dayView.setPrefWidth(200);

        dayView.visibleLayersProperty().addAll(List.of(DateControl.Layer.BASE, DateControl.Layer.TOP));
        dayView.setEntryViewFactory(this::createEntryView);

        return dayView;
    }

    private DayEntryView createEntryView(Entry<?> entry) {
        DayEntryView entryView = new DayEntryView(entry);
        if (entry.getCalendar() instanceof TopLayerCalendar) {
            entryView.setLayer(DateControl.Layer.TOP);
            entryView.setWidthPercentage(20.0);
            entryView.setAlignmentStrategy(AlignmentStrategy.ALIGN_RIGHT);
        }
        return entryView;
    }

    @Override
    public String getSampleDescription() {
        return "The day view supports base and top layers functionality.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DayView.class;
    }

    static class BaseLayerCalendar extends Calendar {

        public BaseLayerCalendar() {
            createCalendarEntries(this, "Entry", LocalDate.now());
            setStyle(Style.STYLE1);
        }

    }

    static class TopLayerCalendar extends Calendar {

        public TopLayerCalendar() {
            createCalendarEntries(this, "Note", LocalDate.now());
            setStyle(Style.STYLE2);
        }

    }

    private static void createCalendarEntries(Calendar calendar, String titlePrefix, LocalDate startDate) {
        for (int j = 0; j < 5 + (int) (Math.random() * 7); j++) {
            Entry<?> entry = new Entry<>();
            entry.changeStartDate(startDate);
            entry.changeEndDate(startDate);

            entry.setTitle(titlePrefix + " " + (j + 1));

            int hour = (int) (Math.random() * 23);
            int durationInHours = Math.max(1, Math.min(24 - hour, (int) (Math.random() * 4)));

            LocalTime startTime = LocalTime.of(hour, 0);
            LocalTime endTime = startTime.plusHours(durationInHours);

            entry.changeStartTime(startTime);
            entry.changeEndTime(endTime);

            entry.setCalendar(calendar);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
