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
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.time.ZonedDateTime;
import java.util.Optional;

class CreateAndDeleteHandler extends DeleteHandler {


    public CreateAndDeleteHandler(DateControl control) {
        super(control);
        dateControl.addEventHandler(MouseEvent.MOUSE_CLICKED, this::createEntry);
    }

    private void createEntry(MouseEvent evt) {
        if (evt.getButton().equals(MouseButton.PRIMARY) && evt.getClickCount() == dateControl.getCreateEntryClickCount()) {

            if (!evt.isStillSincePress()) {
                return;
            }

            if (dateControl instanceof DayViewBase) {
                DayViewBase dayViewBase = (DayViewBase) dateControl;
                if (dayViewBase.isEditAvailability()) {
                    LoggingDomain.VIEW.fine("no new entry created because day view is currently editing availability");
                    return;
                }
            }

            LoggingDomain.VIEW.fine("create entry mouse event received inside control: " + dateControl.getClass().getSimpleName());

            ZonedDateTime time = ZonedDateTime.now().withZoneSameInstant(dateControl.getZoneId());
            if (dateControl instanceof ZonedDateTimeProvider) {
                ZonedDateTimeProvider provider = (ZonedDateTimeProvider) dateControl;
                time = provider.getZonedDateTimeAt(evt.getX(), evt.getY(), dateControl.getZoneId());
            }

            if (dateControl.getCalendars().isEmpty()) {

                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle(Messages.getString("DateControl.TITLE_CALENDAR_PROBLEM"));
                alert.setHeaderText(Messages.getString("DateControl.HEADER_TEXT_NO_CALENDARS_DEFINED"));
                alert.setContentText(Messages.getString("DateControl.CONTENT_TEXT_NO_CALENDARS_DEFINED"));
                alert.show();

            } else {

                Optional<Calendar> calendar = dateControl.getCalendarAt(evt.getX(), evt.getY());
                if (time != null) {
                    Entry<?> entry = dateControl.createEntryAt(time, calendar.orElse(null));
                    if (dateControl.isShowDetailsUponEntryCreation()) {
                        Platform.runLater(() -> dateControl.fireEvent(new RequestEvent(dateControl, dateControl, entry)));
                    }
                }
            }

            evt.consume();
        }
    }
}