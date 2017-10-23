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
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.AgendaView;
import com.calendarfx.view.DateControl;

public class HelloAgendaView extends CalendarFXDateControlSample {

    private AgendaView agendaView;

    @Override
    public String getSampleName() {
        return "Agenda View";
    }

    @Override
    protected DateControl createControl() {
        agendaView = new AgendaView();
        agendaView.setPrefWidth(400);

        CalendarSource calendarSource = new CalendarSource();
        HelloCalendar calendar1 = new HelloCalendar();
        HelloCalendar calendar2 = new HelloCalendar();
        HelloCalendar calendar3 = new HelloCalendar();
        HelloCalendar calendar4 = new HelloCalendar();
        calendarSource.getCalendars().addAll(calendar1, calendar2, calendar3, calendar4);

        agendaView.getCalendarSources().add(calendarSource);

        return agendaView;
    }

    @Override
    protected boolean isSupportingDeveloperConsole() {
        return false;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return AgendaView.class;
    }

    @Override
    public String getSampleDescription() {
        return "The agenda view displays a (text) list of calendar entries for today and several days into the future or past.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
