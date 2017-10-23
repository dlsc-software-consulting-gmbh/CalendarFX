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

package com.calendarfx.demo.popover;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.Entry;
import com.calendarfx.view.popover.EntryHeaderView;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

public class HelloEntryHeaderView extends CalendarFXSample {

    @Override
    public String getSampleName() {
        return "Entry Header View";
    }

    @Override
    protected Node createControl() {
        Calendar meetings = new Calendar("Meetings");
        Calendar training = new Calendar("Training");
        Calendar customers = new Calendar("Customers");
        Calendar holidays = new Calendar("Holidays");

        meetings.setStyle(Style.STYLE2);
        training.setStyle(Style.STYLE3);
        customers.setStyle(Style.STYLE4);
        holidays.setStyle(Style.STYLE5);

        List<Calendar> calendars = new ArrayList<>();
        calendars.add(meetings);
        calendars.add(training);
        calendars.add(customers);
        calendars.add(holidays);

        Entry<String> entry = new Entry<>("Hello Header View");
        entry.setCalendar(meetings);

        return new EntryHeaderView(entry, calendars);
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return EntryHeaderView.class;
    }

    @Override
    public String getSampleDescription() {
        return "A view used to select a calendar from a list of calendars.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
