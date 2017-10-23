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
import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.WeekView;
import impl.com.calendarfx.view.CalendarPropertySheet;
import impl.com.calendarfx.view.DayViewScrollPane;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.stage.Stage;

public class HelloWeekView extends CalendarFXSample {

    private WeekView weekView = new WeekView();

    @Override
    public String getSampleName() {
        return "Week View";
    }

    @Override
    protected Node createControl() {
        return null;
    }

    @Override
    public Node getControlPanel() {
        return new CalendarPropertySheet(weekView.getPropertySheetItems());
    }

    @Override
    public Node getPanel(Stage stage) {
        Calendar dirk = new Calendar("Dirk");
        Calendar katja = new Calendar("Katja");
        Calendar philip = new Calendar("Philip");
        Calendar jule = new Calendar("Jule");
        Calendar armin = new Calendar("Armin");

        dirk.setStyle(Style.STYLE1);
        katja.setStyle(Style.STYLE2);
        philip.setStyle(Style.STYLE3);
        jule.setStyle(Style.STYLE4);
        armin.setStyle(Style.STYLE5);

        CalendarSource calendarSource = new CalendarSource();
        calendarSource.getCalendars().add(dirk);
        calendarSource.getCalendars().add(katja);
        calendarSource.getCalendars().add(philip);
        calendarSource.getCalendars().add(jule);
        calendarSource.getCalendars().add(armin);

        weekView.getCalendarSources().setAll(calendarSource);

        DayViewScrollPane scroll = new DayViewScrollPane(weekView, new ScrollBar());
        scroll.setStyle("-fx-background-color: white;");
        return scroll;
    }

    @Override
    public String getSampleDescription() {
        return "The week view displays several days. Most commonly this view " +
                "will be used to show the 5 work days of a week or the 7 standard " +
                "days.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DetailedWeekView.class;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
