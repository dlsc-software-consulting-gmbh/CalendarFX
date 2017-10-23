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
import com.calendarfx.model.Entry;
import com.calendarfx.view.popover.EntryDetailsView;
import javafx.scene.Node;

public class HelloEntryDetailsView extends CalendarFXSample {

    @Override
    public String getSampleName() {
        return "Entry Details";
    }

    @Override
    protected Node createControl() {
        Entry<String> entry = new Entry<>("Hello Entry");
        entry.setCalendar(new Calendar("Dummy Calendar"));
        return new EntryDetailsView(entry);
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return EntryDetailsView.class;
    }

    @Override
    public String getSampleDescription() {
        return "A view used to edit various properties of a calendar entry.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
