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
import com.calendarfx.model.Marker;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase.OverlapResolutionStrategy;
import com.calendarfx.view.ResourceCalendarView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class HelloResourcesCalendarView extends CalendarFXSample {

    @Override
    public String getSampleName() {
        return "Resources Calendar View";
    }

    ResourceCalendarView<String> resourceCalendarView = new ResourceCalendarView<>();

    @Override
    public Node getPanel(Stage stage) {

        resourceCalendarView.setOverlapResolutionStrategy(OverlapResolutionStrategy.VISUAL_BOUNDS);
        resourceCalendarView.setHeaderFactory(resource -> {
            Label label1 = new Label("IG-TR");
            Label label2 = new Label("29.000");
            Label label3 = new Label("13.200");
            Label label4 = new Label("92 (CPC)");

            label1.setStyle("-fx-font-family: Monospaced; -fx-font-style: bold;");
            label2.setStyle("-fx-font-family: Monospaced; -fx-text-fill: blue; -fx-font-style: bold;");
            label3.setStyle("-fx-font-family: Monospaced; -fx-text-fill: blue; -fx-font-style: bold;");
            label4.setStyle("-fx-font-family: Monospaced; -fx-text-fill: blue; -fx-font-style: bold;");

            label1.setAlignment(Pos.CENTER);
            label2.setAlignment(Pos.CENTER);
            label3.setAlignment(Pos.CENTER);
            label4.setAlignment(Pos.CENTER);

            VBox box = new VBox(5, label1, label2, label3, label4);
            box.setFillWidth(true);

            if (resource.equals("Resource 1")) {
                box.setPrefWidth(400);
            }

            return box;
        });

        for (int i = 0; i < 5; i++) {
            CalendarSource source = new CalendarSource("Default");

            Calendar calendar1 = new HelloDayViewCalendar("cal1");
            calendar1.setStyle(Style.STYLE1);
            source.getCalendars().add(calendar1);

            Calendar calendar2 = new HelloDayViewCalendar("cal2");
            calendar2.setStyle(Style.STYLE2);
            source.getCalendars().add(calendar2);

            Calendar calendar3 = new HelloDayViewCalendar("cal3");
            calendar3.setStyle(Style.STYLE3);
            source.getCalendars().add(calendar3);

            String resource = "Resource " + (i + 1);
            resourceCalendarView.getResources().add(resource);

            DayView dayView = resourceCalendarView.getDayView(resource);
            dayView.setEnableCurrentTimeMarker(true);
            dayView.setEnableCurrentTimeCircle(i == 0);
            dayView.getCalendarSources().setAll(source);
            dayView.setEntryViewFactory(entry -> new DayEntryView(entry) {
                {
                    setPrefHeight(25);
                    setHeightLayoutStrategy(HeightLayoutStrategy.COMPUTE_PREF_SIZE);
                }
            });

            if (i % 2 == 1) {
                dayView.setStyle("-fx-background-color: #e9e9e9;");
            }
        }

        Marker marker1 = new Marker();
        marker1.setTitle("My Marker 1");
        marker1.setTime(ZonedDateTime.now().minusHours(1));
        resourceCalendarView.getMarkers().add(marker1);

        Marker marker2 = new Marker();
        marker2.setTitle("My Marker 2");
        marker2.setTime(ZonedDateTime.now().plusHours(1));
        marker2.getStyleClass().add("marker2");
        resourceCalendarView.getMarkers().add(marker2);

        return resourceCalendarView;
    }

    @Override
    protected Node createControl() {
        return null;
    }

    @Override
    public String getSampleDescription() {
        return "The resource calendar view can scroll vertically up and down with infinite scrolling enabled.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DayView.class;
    }

    class HelloDayViewCalendar extends Calendar {

        public HelloDayViewCalendar(String name) {
            setName(name);
            createEntries(LocalDate.now().minusDays(2));
            createEntries(LocalDate.now().minusDays(1));
            createEntries(LocalDate.now());
            createEntries(LocalDate.now().plusDays(1));
            createEntries(LocalDate.now().plusDays(2));
        }

        private void createEntries(LocalDate startDate) {
            for (int j = 0; j < 5 + (int) (Math.random() * 7); j++) {
                Entry<?> entry = new Entry<>();
                entry.changeStartDate(startDate);
                entry.changeEndDate(startDate);

                entry.setTitle("Entry " + (j + 1));

                int hour = (int) (Math.random() * 23);
                int durationInHours = Math.max(1, Math.min(24 - hour, (int) (Math.random() * 4)));

                LocalTime startTime = LocalTime.of(hour, 0);
                LocalTime endTime = startTime.plusHours(durationInHours);

                entry.changeStartTime(startTime);
                entry.changeEndTime(endTime);

                entry.setCalendar(this);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
