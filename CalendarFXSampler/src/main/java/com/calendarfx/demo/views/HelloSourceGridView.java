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
import com.calendarfx.view.SourceGridView;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class HelloSourceGridView extends CalendarFXSample {

    private SourceGridView sourceView;

    @Override
    public String getSampleName() {
        return "Source Grid View";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return SourceGridView.class;
    }

    @Override
    protected Node createControl() {
        sourceView = new SourceGridView();

        Calendar meetings = new Calendar("Meetings");
        Calendar training = new Calendar("Training");
        Calendar customers = new Calendar("Customers");
        Calendar holidays = new Calendar("Holidays");

        meetings.setStyle(Style.STYLE2);
        training.setStyle(Style.STYLE3);
        customers.setStyle(Style.STYLE4);
        holidays.setStyle(Style.STYLE5);

        CalendarSource workCalendarSource = new CalendarSource("Work");
        workCalendarSource.getCalendars().addAll(meetings, training, customers, holidays);

        Calendar birthdays = new Calendar("Birthdays");
        Calendar katja = new Calendar("Katja");
        Calendar dirk = new Calendar("Dirk");
        Calendar philip = new Calendar("Philip");
        Calendar jule = new Calendar("Jule");
        Calendar armin = new Calendar("Armin");

        CalendarSource familyCalendarSource = new CalendarSource("Family");
        familyCalendarSource.getCalendars().addAll(birthdays, katja, dirk, philip, jule, armin);

        sourceView.getCalendarSources().addAll(workCalendarSource, familyCalendarSource);

        return sourceView;
    }

    @Override
    public Node getControlPanel() {
        Node controlPanel = super.getControlPanel();

        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.getChildren().add(controlPanel);

        for (CalendarSource calendarSource : sourceView.getCalendarSources()) {
            for (Calendar calendar : calendarSource.getCalendars()) {
                CheckBox checkBox = new CheckBox(calendar.getName());
                checkBox.selectedProperty().bindBidirectional(sourceView.getCalendarVisibilityProperty(calendar));
                vBox.getChildren().add(checkBox);
            }
        }

        return vBox;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
