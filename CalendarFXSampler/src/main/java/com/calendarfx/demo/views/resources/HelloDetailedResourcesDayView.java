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
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.resources.DetailedResourcesDayView;
import com.calendarfx.view.resources.Resource;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

public class HelloDetailedResourcesDayView extends CalendarFXDateControlSample {

    private DetailedResourcesDayView detailedResouresDayView;

    @Override
    public String getSampleName() {
        return "Detailed Resources Day View";
    }

    @Override
    public String getSampleDescription() {
        return "The detailed day view aggregates a day view, an all day view, a calendar header (for swimlane layout), and a time scale.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DetailedResourcesDayView.class;
    }

    @Override
    public Node getControlPanel() {
        ToggleButton availabilityButton = new ToggleButton("Edit Schedule");
        availabilityButton.selectedProperty().bindBidirectional(detailedResouresDayView.editAvailabilityProperty());
        return availabilityButton;
    }

    @Override
    protected DateControl createControl() {
        detailedResouresDayView = new DetailedResourcesDayView();
        detailedResouresDayView.setPrefHeight(800);
        detailedResouresDayView.setPrefWidth(700);
        detailedResouresDayView.getResources().addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2), create("Philip", Style.STYLE3), create("Jule", Style.STYLE4), create("Armin", Style.STYLE5));
        return detailedResouresDayView;
    }

    private Resource<String> create(String name, Style style) {
        Resource<String> resource = new Resource(name);
        resource.getAvailabilityCalendar().setName("Availability of " + name);
        resource.getCalendar().setStyle(style);
        resource.getCalendar().addEventHandler(evt -> System.out.println(evt));
        resource.getAvailabilityCalendar().addEventHandler(evt -> System.out.println(evt));
        return resource;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
