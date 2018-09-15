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

package com.calendarfx.google;

import com.calendarfx.google.view.GoogleCalendarAppView;
import com.calendarfx.view.CalendarView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;

public class GoogleCalendarApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        CalendarView calendarView = new CalendarView();
        calendarView.setToday(LocalDate.now());
        calendarView.setTime(LocalTime.now());
        calendarView.setShowDeveloperConsole(Boolean.getBoolean("calendarfx.developer"));

        GoogleCalendarAppView appView = new GoogleCalendarAppView(calendarView);
        appView.getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());

        primaryStage.setTitle("Google Calendar");
        primaryStage.setScene(new Scene(appView));
        primaryStage.setWidth(1400);
        primaryStage.setHeight(950);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
