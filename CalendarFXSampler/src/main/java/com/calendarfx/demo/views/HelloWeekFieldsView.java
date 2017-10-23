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
import com.calendarfx.view.AgendaView;
import com.calendarfx.view.WeekFieldsView;
import javafx.scene.control.Control;

public class HelloWeekFieldsView extends CalendarFXSample {

    private WeekFieldsView view;

    @Override
    public String getSampleName() {
        return "Week Fields View";
    }

    @Override
    protected Control createControl() {
        view = new WeekFieldsView();
        return view;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return AgendaView.class;
    }

    @Override
    public String getSampleDescription() {
        return "The week fields view lets the user specify the first day of the week " +
                "(e.g. MONDAY in Germany, SUNDAY in the US) and the minimum number of " +
                "days in the first week of the year.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
