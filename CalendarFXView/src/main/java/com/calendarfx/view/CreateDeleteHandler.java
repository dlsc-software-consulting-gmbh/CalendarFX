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

package com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.util.LoggingDomain;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

class CreateDeleteHandler {

    private DateControl dateControl;

    public CreateDeleteHandler(DateControl control) {
        this.dateControl = requireNonNull(control);

        dateControl.addEventHandler(MouseEvent.MOUSE_CLICKED, this::createEntry);
        dateControl.addEventHandler(KeyEvent.KEY_PRESSED, this::deleteEntries);
    }

    private void createEntry(MouseEvent evt) {
        if (evt.getButton().equals(MouseButton.PRIMARY) && evt.getClickCount() == 2) {
            LoggingDomain.VIEW.fine("create entry mouse event received inside control: " + dateControl.getClass().getSimpleName());

            ZonedDateTime time = ZonedDateTime.now();
            if (dateControl instanceof ZonedDateTimeProvider) {
                ZonedDateTimeProvider provider = (ZonedDateTimeProvider) dateControl;
                time = provider.getZonedDateTimeAt(evt.getX(), evt.getY());
            }

            Optional<Calendar> calendar = dateControl.getCalendarAt(evt.getX(), evt.getY());

            if (time != null) {
                dateControl.createEntryAt(time, calendar.orElse(null));
            }
        }
    }

    private void deleteEntries(KeyEvent evt) {
        switch (evt.getCode()) {
            case DELETE:
            case BACK_SPACE:
                for (Entry<?> entry : dateControl.getSelections()) {
                    if (!dateControl.getEntryEditPolicy().call(new DateControl.EntryEditParameter(dateControl, entry, DateControl.EditOperation.DELETE))) {
                        continue;
                    }
                    if (entry.isRecurrence()) {
                        entry = entry.getRecurrenceSourceEntry();
                    }
                    if (!dateControl.getEntryEditPolicy().call(new DateControl.EntryEditParameter(dateControl, entry, DateControl.EditOperation.DELETE))) {
                        continue;
                    }

                    Calendar calendar = entry.getCalendar();
                    if (calendar != null && !calendar.isReadOnly()) {
                        entry.removeFromCalendar();
                    }
                }
                dateControl.clearSelection();
                break;
            case F5:
                dateControl.refreshData();
            default:
                break;
        }
    }
}