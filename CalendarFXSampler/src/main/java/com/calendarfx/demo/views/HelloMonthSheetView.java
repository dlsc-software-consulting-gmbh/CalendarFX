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
import com.calendarfx.view.MonthSheetView;
import impl.com.calendarfx.view.CalendarPropertySheet;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class HelloMonthSheetView extends CalendarFXDateControlSample {

    public static void main(String[] args) {
        launch(args);
    }

    private MonthSheetView monthView;

    @Override
    public String getSampleName() {
        return "Month Sheet View";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return MonthSheetView.class;
    }

    private enum CellType {
        STANDARD,
        USAGE,
        DETAILED,
        BADGE
    }

    @Override
    public Node getControlPanel() {
        VBox box = new VBox();

        ComboBox<CellType> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(CellType.values());
        comboBox.setValue(CellType.STANDARD);
        comboBox.valueProperty().addListener(it -> {
            switch (comboBox.getValue()) {
                case USAGE:
                    monthView.setCellFactory(param -> new MonthSheetView.UsageDateCell(param.getView(), param.getDate()));
                    break;
                case BADGE:
                    monthView.setCellFactory(param -> new MonthSheetView.BadgeDateCell(param.getView(), param.getDate()));
                    break;
                case DETAILED:
                    monthView.setCellFactory(param -> new MonthSheetView.DetailedDateCell(param.getView(), param.getDate()));
                    break;
                case STANDARD:
                    monthView.setCellFactory(param -> new MonthSheetView.SimpleDateCell(param.getView(), param.getDate()));
                    break;
            }
        });

        box.getChildren().add(comboBox);
        final CalendarPropertySheet propertySheet = new CalendarPropertySheet(monthView.getPropertySheetItems());
        VBox.setVgrow(propertySheet, Priority.ALWAYS);
        box.getChildren().add(propertySheet);

        return box;
    }

    @Override
    protected DateControl createControl() {
        CalendarSource source = new CalendarSource();
        source.setName("Demo Source");

        Calendar[] calendar = new Calendar[7];
        for (int i = 0; i < 7; i++) {
            calendar[i] = new Calendar("Calendar " + i);
            calendar[i].setStyle(Calendar.Style.getStyle(i));
        }

        for (int i = 0; i < 1000; i++) {
            Entry<String> entry = new Entry<>("Entry " + i);
            LocalDate date = LocalDate.now();
            if (Math.random() < .5) {
                date = date.minusDays((long) (Math.random() * 365));
            } else {
                date = date.plusDays((long) (Math.random() * 365));
            }

            LocalTime start = LocalTime.of((int) (Math.random() * 20), (int) (Math.random() * 30));
            Duration duration = Duration.ofHours((int) (Math.random() * 8));
            LocalTime end = start.plus(duration);
            if (end.isBefore(start)) {
                end = LocalTime.MAX;
            }

            entry.changeStartDate(date);
            entry.changeEndDate(date);
            entry.changeStartTime(start);
            entry.changeEndTime(end);

            if (Math.random() > .9) {
                entry.setFullDay(true);
            }

            entry.setCalendar(calendar[(int) (Math.random() * 7)]);
        }

        source.getCalendars().addAll(calendar);

        monthView = new MonthSheetView();
        monthView.getCalendarSources().add(source);
        monthView.setCellFactory(param -> new MonthSheetView.DetailedDateCell(param.getView(), param.getDate()));

        return monthView;
    }

}
