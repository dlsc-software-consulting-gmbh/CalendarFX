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
import com.calendarfx.view.CalendarHeaderView;
import javafx.scene.Node;

public class HelloCalendarHeaderView extends CalendarFXSample {

    @Override
    public String getSampleName() {
        return "Calendar Header View";
    }

    @Override
    protected Node createControl() {
        CalendarHeaderView calendarHeaderView = new CalendarHeaderView();
        calendarHeaderView.setNumberOfDays(5);
        calendarHeaderView.setMaxHeight(30);

        Calendar dirk = new Calendar("Dirk");
        Calendar katja = new Calendar("Katja");
        Calendar philip = new Calendar("Philip");
        Calendar jule = new Calendar("Jule");
        Calendar armin = new Calendar("Armin");

        dirk.setStyle(Style.STYLE1);
        katja.setStyle(Style.STYLE1);
        philip.setStyle(Style.STYLE2);
        jule.setStyle(Style.STYLE1);
        armin.setStyle(Style.STYLE3);

        calendarHeaderView.getCalendars().add(dirk);
        calendarHeaderView.getCalendars().add(katja);
        calendarHeaderView.getCalendars().add(philip);
        calendarHeaderView.getCalendars().add(jule);
        calendarHeaderView.getCalendars().add(armin);

        return calendarHeaderView;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return CalendarHeaderView.class;
    }

    @Override
    public String getSampleDescription() {
        return "The all-day view displays entries that last all day / span multiple days.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
