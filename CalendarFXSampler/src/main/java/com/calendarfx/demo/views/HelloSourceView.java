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
import com.calendarfx.view.SourceView;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

public class HelloSourceView extends CalendarFXSample {

    private SourceView sourceView;

    private CalendarSource workCalendarSource;

    private CalendarSource familyCalendarSource;

    @Override
    public String getSampleName() {
        return "Source View";
    }

    @Override
    protected Node createControl() {
        sourceView = new SourceView();

        Calendar meetings = new Calendar("Meetings");
        Calendar training = new Calendar("Training");
        Calendar customers = new Calendar("Customers");
        Calendar holidays = new Calendar("Holidays");

        meetings.setStyle(Style.STYLE2);
        training.setStyle(Style.STYLE3);
        customers.setStyle(Style.STYLE4);
        holidays.setStyle(Style.STYLE5);

        workCalendarSource = new CalendarSource("Work");
        workCalendarSource.getCalendars().addAll(meetings, training, customers,
                holidays);

        Calendar birthdays = new Calendar("Birthdays");
        Calendar katja = new Calendar("Katja");
        Calendar dirk = new Calendar("Dirk");
        Calendar philip = new Calendar("Philip");
        Calendar jule = new Calendar("Jule");
        Calendar armin = new Calendar("Armin");

        familyCalendarSource = new CalendarSource("Family");
        familyCalendarSource.getCalendars().addAll(birthdays, katja, dirk,
                philip, jule, armin);

        sourceView.getCalendarSources().addAll(workCalendarSource,
                familyCalendarSource);

        return sourceView;
    }

    @Override
    public Node getControlPanel() {
        VBox box = new VBox();
        box.setSpacing(5);
        box.setFillWidth(true);

        Button addWorkCalendar = new Button("Add Work Calendar");
        addWorkCalendar.setOnAction(evt -> addWorkCalendar());

        Button removeWorkCalendar = new Button("Remove Work Calendar");
        removeWorkCalendar.setOnAction(evt -> removeWorkCalendar());

        Button addWorkCalendarSource = new Button("Add Work Calendar Source");
        addWorkCalendarSource.setOnAction(evt -> addWorkCalendarSource());

        Button removeWorkCalendarSource = new Button(
                "Remove Work Calendar Source");
        removeWorkCalendarSource.setOnAction(evt -> removeWorkCalendarSource());

        box.getChildren().addAll(addWorkCalendar, removeWorkCalendar,
                addWorkCalendarSource, removeWorkCalendarSource);

        Button addFamilyCalendar = new Button("Add Family Calendar");
        addFamilyCalendar.setOnAction(evt -> addFamilyCalendar());

        Button removeFamilyCalendar = new Button("Remove Family Calendar");
        removeFamilyCalendar.setOnAction(evt -> removeFamilyCalendar());

        Button addFamilyCalendarSource = new Button(
                "Add Family Calendar Source");
        addFamilyCalendarSource.setOnAction(evt -> addFamilyCalendarSource());

        Button removeFamilyCalendarSource = new Button(
                "Remove Family Calendar Source");
        removeFamilyCalendarSource
                .setOnAction(evt -> removeFamilyCalendarSource());

        box.getChildren().addAll(new Separator(Orientation.HORIZONTAL),
                addFamilyCalendar, removeFamilyCalendar,
                addFamilyCalendarSource, removeFamilyCalendarSource);

        addWorkCalendar.setMaxWidth(Double.MAX_VALUE);
        removeWorkCalendar.setMaxWidth(Double.MAX_VALUE);
        addWorkCalendarSource.setMaxWidth(Double.MAX_VALUE);
        removeWorkCalendarSource.setMaxWidth(Double.MAX_VALUE);

        addFamilyCalendar.setMaxWidth(Double.MAX_VALUE);
        removeFamilyCalendar.setMaxWidth(Double.MAX_VALUE);
        addFamilyCalendarSource.setMaxWidth(Double.MAX_VALUE);
        removeFamilyCalendarSource.setMaxWidth(Double.MAX_VALUE);

        return box;
    }

    private int calendarCounter = 1;

    private void addWorkCalendar() {
        Calendar calendar = new Calendar("Work Calendar " + calendarCounter++);
        calendar.setStyle(Style.getStyle(calendarCounter));
        workCalendarSource.getCalendars().add(calendar);
    }

    private void removeWorkCalendar() {
        workCalendarSource.getCalendars().remove(
                workCalendarSource.getCalendars().size() - 1);
    }

    private void addWorkCalendarSource() {
        sourceView.getCalendarSources().add(workCalendarSource);
    }

    private void removeWorkCalendarSource() {
        sourceView.getCalendarSources().remove(workCalendarSource);
    }

    private void addFamilyCalendar() {
        Calendar calendar = new Calendar("Family Calendar " + calendarCounter++);
        calendar.setStyle(Style.getStyle(calendarCounter));
        familyCalendarSource.getCalendars().add(calendar);
    }

    private void addFamilyCalendarSource() {
        sourceView.getCalendarSources().add(familyCalendarSource);
    }

    private void removeFamilyCalendar() {
        familyCalendarSource.getCalendars().remove(
                familyCalendarSource.getCalendars().size() - 1);
    }

    private void removeFamilyCalendarSource() {
        sourceView.getCalendarSources().remove(familyCalendarSource);
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return SourceView.class;
    }

    @Override
    public String getSampleDescription() {
        return "Shows all calendar sources. Sources are used to group calendars together that all origin from the same "
                + "source, e.g. Google calendar. Sources can be collapsed by clicking on their name.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
