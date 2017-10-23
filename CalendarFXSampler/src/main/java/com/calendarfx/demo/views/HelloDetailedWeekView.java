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
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DetailedWeekView;

public class HelloDetailedWeekView extends CalendarFXDateControlSample {

    private DetailedWeekView detailedWeekView;

    @Override
    public String getSampleName() {
        return "Detailed Week View";
    }

    @Override
    protected DateControl createControl() {
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
        calendarSource.getCalendars().setAll(dirk, katja, philip, jule, armin);

        detailedWeekView = new DetailedWeekView();
        detailedWeekView.getCalendarSources().setAll(calendarSource);

        return detailedWeekView;
    }

    @Override
    public String getSampleDescription() {
        return "The detailed week view displays several days inside a week view. " +
                "Additionally it shows an all day view at the top and a time scale " +
                "on its left-hand side";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DetailedWeekView.class;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
