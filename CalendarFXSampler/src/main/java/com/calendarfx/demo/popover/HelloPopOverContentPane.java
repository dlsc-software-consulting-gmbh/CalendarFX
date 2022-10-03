/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
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

package com.calendarfx.demo.popover;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.popover.EntryPropertiesView;
import com.calendarfx.view.popover.PopOverContentPane;
import com.calendarfx.view.popover.PopOverTitledPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloPopOverContentPane extends Application {

    @Override
    public void start(Stage primaryStage) {
        Calendar calendar = new Calendar();
        calendar.setName("Calendar");
        calendar.setStyle(Calendar.Style.STYLE2);

        Entry entry = new Entry<>();
        entry.setTitle("Google Entry");
        entry.setCalendar(calendar);
        entry.setLocation("Bogota");

        PopOverContentPane pane = new PopOverContentPane();

        EntryPropertiesView entryPropertiesView = new EntryPropertiesView(entry);
        PopOverTitledPane titledPane = new PopOverTitledPane("Properties", entryPropertiesView);
        titledPane.getStyleClass().add("no-padding");

        titledPane.setExpanded(true);

        pane.getPanes().add(titledPane);

        primaryStage.setTitle("Entry Properties");
        Scene scene = new Scene(pane, 400, 600);
        scene.getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}
