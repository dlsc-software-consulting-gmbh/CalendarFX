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

package com.calendarfx.demo.performance;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import impl.com.calendarfx.view.CalendarPropertySheet;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;

public class HelloPerformance extends CalendarFXDateControlSample {

    private CalendarView calendarView;
    private ComboBox<Integer> comboBox;
    private Label label;
    private HelloCalendar calendar;
    private int style;

    @Override
    public String getSampleName() {
        return "Performance";
    }

    @Override
    protected DateControl createControl() {
        CalendarSource calendarSource = new CalendarSource("My Calendars");
        calendarSource.getCalendars().add(calendar = new HelloCalendar());

        calendarView = new CalendarView();
        calendarView.getCalendarSources().add(calendarSource);

        return calendarView;
    }

    @Override
    public Node getControlPanel() {
        VBox vBox = new VBox();
        vBox.setFillWidth(true);
        vBox.setSpacing(10);

        // button
        Button button = new Button("Create Entries");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(evt -> createEntries());
        VBox.setVgrow(button, Priority.NEVER);
        vBox.getChildren().add(button);

        // box
        comboBox = new ComboBox<>();
        comboBox.getItems().addAll(100, 1000, 2000, 3000, 10000, 100000, 1000000);
        comboBox.getSelectionModel().select(0);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        vBox.getChildren().add(comboBox);

        // label
        label = new Label("Time: ");
        vBox.getChildren().add(label);

        // Separator
        Separator separator = new Separator(Orientation.HORIZONTAL);
        vBox.getChildren().add(separator);

        // sheet
        CalendarPropertySheet sheet = new CalendarPropertySheet(calendarView.getPropertySheetItems());
        VBox.setVgrow(sheet, Priority.ALWAYS);
        vBox.getChildren().add(sheet);

        return vBox;
    }

    public void createEntries() {
        calendar.setStyle(Calendar.Style.getStyle(style++));
        calendar.clear();

        LocalTime dailyStartTime = LocalTime.of(8, 0);
        LocalTime dailyEndTime = LocalTime.of(20, 0);

        LocalDate entryDate = LocalDate.now();
        LocalTime entryTime = dailyStartTime;

        long startTime = System.currentTimeMillis();
        int count = comboBox.getValue();

        calendar.startBatchUpdates();

        for (int i = 0; i < count; i++) {
            Entry<String> entry = new Entry<>("Entry " + i);
            entry.setInterval(new Interval(entryDate, entryTime, entryDate, entryTime.plusMinutes(30)));
            entryTime = entryTime.plusHours(1);
            if (entryTime.isAfter(dailyEndTime)) {
                entryDate = entryDate.plusDays(1);
                entryTime = dailyStartTime;
            }

            entry.setCalendar(calendar);
        }

        calendar.stopBatchUpdates();

        label.setText("Time: " + (System.currentTimeMillis() - startTime));
    }

    @Override
    public String getSampleDescription() {
        return "A test sample to confirm high performance even when dealing with a lot of entries.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return Calendar.class;
    }

    class HelloCalendar extends Calendar {

        public HelloCalendar() {
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
