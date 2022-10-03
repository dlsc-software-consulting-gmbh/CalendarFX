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
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase.HoursLayoutStrategy;
import com.calendarfx.view.VirtualGrid;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.temporal.ChronoUnit;

public class HelloAvailabilityCalendar extends CalendarFXSample {

    private final DayView dayView = new DayView();

    @Override
    public String getSampleName() {
        return "Availability Calendar";
    }

    protected Node createControl() {
        dayView.setAvailabilityCalendar(new HelloDayViewCalendar());
        dayView.setHoursLayoutStrategy(HoursLayoutStrategy.FIXED_HOUR_HEIGHT);
        dayView.setHourHeight(20);
        dayView.setPrefWidth(200);
        return dayView;
    }

    @Override
    public Node getControlPanel() {
        CheckBox editMode = new CheckBox("Edit Availability");
        editMode.selectedProperty().bindBidirectional(dayView.editAvailabilityProperty());

        ChoiceBox<VirtualGrid> gridBox = new ChoiceBox<>();
        gridBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(VirtualGrid virtualGrid) {
                return virtualGrid.getName();
            }

            @Override
            public VirtualGrid fromString(String s) {
                return null;
            }
        });
        gridBox.getItems().add(dayView.getAvailabilityGrid());
        gridBox.getItems().add(new VirtualGrid("10 Minutes", "10m", ChronoUnit.MINUTES, 10));
        gridBox.getItems().add(new VirtualGrid("15 Minutes", "15m", ChronoUnit.MINUTES, 15));
        gridBox.getItems().add(new VirtualGrid("1 Hour", "1h", ChronoUnit.HOURS, 1));
        gridBox.valueProperty().bindBidirectional(dayView.availabilityGridProperty());

        VBox box = new VBox(10, editMode, gridBox);
        return box;
    }

    @Override
    public String getSampleDescription() {
        return "The availability calendar can be used to visualize in a read-only way when a person or a resource is available or not.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DayView.class;
    }

    class HelloDayViewCalendar extends Calendar {

        public HelloDayViewCalendar() {
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
