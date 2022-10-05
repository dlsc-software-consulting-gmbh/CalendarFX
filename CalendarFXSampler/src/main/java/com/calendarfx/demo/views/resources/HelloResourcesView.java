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

package com.calendarfx.demo.views.resources;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DayViewBase.AvailabilityEditingEntryBehaviour;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import com.calendarfx.view.DayViewBase.GridType;
import com.calendarfx.model.Resource;
import com.calendarfx.view.ResourcesView;
import com.calendarfx.view.ResourcesView.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class HelloResourcesView extends CalendarFXDateControlSample {

    private ResourcesView resourcesView;

    @Override
    public String getSampleName() {
        return "ResourcesView";
    }

    @Override
    public String getSampleDescription() {
        return "The resources view is used to display allocations of resources, e.g. people working at a hairdresser and their customer appointments.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return ResourcesView.class;
    }

    @Override
    protected boolean isSupportingDeveloperConsole() {
        return false;
    }

    @Override
    public Node getControlPanel() {
        ToggleButton availabilityButton = new ToggleButton("Edit Schedule");
        availabilityButton.selectedProperty().bindBidirectional(resourcesView.editAvailabilityProperty());

        DatePicker datePicker = new DatePicker();
        datePicker.valueProperty().bindBidirectional(resourcesView.dateProperty());

        ChoiceBox<Integer> daysBox = new ChoiceBox<>();
        daysBox.getItems().setAll(1, 2, 3, 4, 5, 7, 10, 14);
        daysBox.setValue(resourcesView.getNumberOfDays());
        daysBox.valueProperty().addListener(it -> resourcesView.setNumberOfDays(daysBox.getValue()));

        ChoiceBox<Integer> numberOfResourcesBox = new ChoiceBox<>();
        numberOfResourcesBox.getItems().setAll(1, 2, 3, 4, 5);
        numberOfResourcesBox.setValue(resourcesView.getResources().size());
        numberOfResourcesBox.valueProperty().addListener(it -> resourcesView.getResources().setAll(createResources(numberOfResourcesBox.getValue())));

        ChoiceBox<Integer> clicksBox = new ChoiceBox<>();
        clicksBox.getItems().setAll(1, 2, 3);
        clicksBox.setValue(resourcesView.getCreateEntryClickCount());
        clicksBox.valueProperty().addListener(it -> resourcesView.setCreateEntryClickCount(clicksBox.getValue()));

        ChoiceBox<AvailabilityEditingEntryBehaviour> behaviourBox = new ChoiceBox<>();
        behaviourBox.getItems().setAll(AvailabilityEditingEntryBehaviour.values());
        behaviourBox.valueProperty().bindBidirectional(resourcesView.entryViewAvailabilityEditingBehaviourProperty());

        ChoiceBox<Type> typeBox = new ChoiceBox<>();
        typeBox.getItems().setAll(Type.values());
        typeBox.valueProperty().bindBidirectional(resourcesView.typeProperty());
        typeBox.setConverter(new StringConverter<Type>() {
            @Override
            public String toString(Type object) {
                if (object != null) {
                    if (object.equals(Type.RESOURCES_OVER_DATE)) {
                        return "Resources over date";
                    } else if (object.equals(Type.DATE_OVER_RESOURCES)) {
                        return "Date over resources";
                    } else {
                        return "unknown view type: " + object.name();
                    }
                }
                return "";
            }

            @Override
            public Type fromString(String string) {
                return null;
            }
        });

        ChoiceBox<GridType> gridTypeBox = new ChoiceBox<>();
        gridTypeBox.getItems().setAll(GridType.values());
        gridTypeBox.valueProperty().bindBidirectional(resourcesView.gridTypeProperty());

        CheckBox adjustBox = new CheckBox("Adjust first day of week");
        adjustBox.selectedProperty().bindBidirectional(resourcesView.adjustToFirstDayOfWeekProperty());

        CheckBox scrollbarBox = new CheckBox("Show scrollbar");
        scrollbarBox.selectedProperty().bindBidirectional(resourcesView.showScrollBarProperty());

        CheckBox timescaleBox = new CheckBox("Show timescale");
        timescaleBox.selectedProperty().bindBidirectional(resourcesView.showTimeScaleViewProperty());

        CheckBox allDayBox = new CheckBox("Show all day events");
        allDayBox.selectedProperty().bindBidirectional(resourcesView.showAllDayViewProperty());

        CheckBox detailsBox = new CheckBox("Show details upon creation");
        detailsBox.selectedProperty().bindBidirectional(resourcesView.showDetailsUponEntryCreationProperty());

        CheckBox flipBox = new CheckBox("Enable start / end flip over");
        flipBox.selectedProperty().bindBidirectional(resourcesView.enableStartAndEndTimesFlipProperty());

        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.valueProperty().bindBidirectional(resourcesView.entryViewAvailabilityEditingOpacityProperty());

        return new VBox(10, availabilityButton, new Label("View type"), typeBox, datePicker, adjustBox, new Label("Number of resources"), numberOfResourcesBox, new Label("Number of days"), daysBox, new Label("Clicks to create"), clicksBox,
                new Label("Availability Behaviour"), behaviourBox, new Label("Availability Opacity"), slider, new Label("Grid Type"), gridTypeBox, scrollbarBox, timescaleBox, allDayBox, detailsBox, flipBox);
    }

    @Override
    protected DateControl createControl() {
        resourcesView = new ResourcesView();
        resourcesView.setType(Type.DATE_OVER_RESOURCES);
        resourcesView.setNumberOfDays(5);
        resourcesView.setCreateEntryClickCount(1);
        resourcesView.setGridType(GridType.CUSTOM);
        resourcesView.setEarlyLateHoursStrategy(EarlyLateHoursStrategy.HIDE);
        resourcesView.getResources().setAll(createResources(3));
        resourcesView.setShowDetailsUponEntryCreation(false);

        return resourcesView;
    }

    private List<Resource<String>> createResources(int count) {
        ObservableList<Resource<String>> result = FXCollections.observableArrayList();
        switch (count) {
            case 1:
                result.addAll(create("Dirk", Style.STYLE1));
                break;
            case 2:
                result.addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2));
                break;
            case 3:
                result.addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2), create("Philip", Style.STYLE3));
                break;
            case 4:
                result.addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2), create("Philip", Style.STYLE3), create("Jule", Style.STYLE4));
                break;
            case 5:
                result.addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2), create("Philip", Style.STYLE3), create("Jule", Style.STYLE4), create("Armin", Style.STYLE5));
                break;
        }

        return result;
    }

    private Resource<String> create(String name, Style style) {
        Resource<String> resource = new Resource(name);
        resource.getAvailabilityCalendar().setName("Availability of " + name);
        resource.getCalendar().setStyle(style);
        fillAvailabilities(resource.getAvailabilityCalendar());
        return resource;
    }

    private void fillAvailabilities(Calendar calendar) {
        LocalDate date = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int i = 0; i < 14; i++) {
            // fourteen days is enough for this demo
            Entry morning = new Entry("Morning");
            morning.setInterval(date, LocalTime.MIN, date, LocalTime.of(8, 0));
            calendar.addEntry(morning);
            Entry noon = new Entry("Noon");
            noon.setInterval(date, LocalTime.of(12, 0), date, LocalTime.of(13, 0));
            calendar.addEntry(noon);
            Entry evening = new Entry("Evening");
            evening.setInterval(date, LocalTime.of(18, 0), date, LocalTime.MAX);
            calendar.addEntry(evening);
            date = date.plusDays(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
