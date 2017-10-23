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

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.EntryViewBase;
import impl.com.calendarfx.view.CalendarPropertySheet;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import org.controlsfx.control.PropertySheet;

public abstract class HelloEntryViewBase extends CalendarFXSample {

    private static final String ORANGE_BACKGROUND_STYLE = "orange-background";
    private static final String RED_BORDER_STYLE = "red-border";

    protected EntryViewBase<?> entryView;
    protected Entry<?> entry;

    public HelloEntryViewBase() {
        Calendar calendar = new Calendar("Test Calendar");
        entry = new Entry<>("Test Entry");
        entry.setCalendar(calendar);
    }

    @Override
    protected Node createControl() {
        entryView = createEntryView(entry);
        entryView.getStylesheets().add(HelloDayEntryView.class.getResource("entry.css").toExternalForm());
        control = entryView;

        ContextMenu menu = new ContextMenu();

        // red border
        CheckMenuItem redBorder = new CheckMenuItem("Red Border");
        menu.getItems().add(redBorder);
        redBorder.setOnAction(evt -> {
            if (redBorder.isSelected()) {
                entry.getStyleClass().add(RED_BORDER_STYLE);
            } else {
                entry.getStyleClass().remove(RED_BORDER_STYLE);
            }
        });

        // red background
        CheckMenuItem redBackground = new CheckMenuItem("Orange Background");
        menu.getItems().add(redBackground);
        redBackground.setOnAction(evt -> {
            if (redBackground.isSelected()) {
                entry.getStyleClass().add(ORANGE_BACKGROUND_STYLE);
            } else {
                entry.getStyleClass().remove(ORANGE_BACKGROUND_STYLE);
            }
        });

        entryView.setContextMenu(menu);

        return entryView;
    }

    protected abstract EntryViewBase<?> createEntryView(Entry<?> entry);

    @Override
    public Node getControlPanel() {
        PropertySheet sheet = new CalendarPropertySheet(entryView.getPropertySheetItems());
        sheet.getItems().addAll(entry.getPropertySheetItems());
        return sheet;
    }
}
