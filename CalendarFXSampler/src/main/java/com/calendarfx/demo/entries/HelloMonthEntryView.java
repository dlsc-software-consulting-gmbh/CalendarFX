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

package com.calendarfx.demo.entries;

import com.calendarfx.model.Entry;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.MonthEntryView;

import java.time.LocalDate;

public class HelloMonthEntryView extends HelloEntryViewBase {

    public HelloMonthEntryView() {
        super();

        entry.setInterval(LocalDate.now(), LocalDate.now().plusDays(5));
    }

    @Override
    protected EntryViewBase<?> createEntryView(Entry<?> entry) {
        MonthEntryView view = new MonthEntryView(entry);
        view.setPrefSize(400, 20);
        return view;
    }

    @Override
    public String getSampleName() {
        return "Month Entry View";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return MonthEntryView.class;
    }

    @Override
    public String getSampleDescription() {
        return "This view is used to display a single entry in a month view.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
