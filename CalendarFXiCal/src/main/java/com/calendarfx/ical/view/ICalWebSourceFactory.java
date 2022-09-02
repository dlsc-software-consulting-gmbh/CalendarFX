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

package com.calendarfx.ical.view;

import com.calendarfx.ical.ICalRepository;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * Calendar Source Factory that shows up a dialog to enter a web based URL of an iCal.
 * <p>
 * Created by gdiaz on 5/01/2017.
 */
public final class ICalWebSourceFactory implements Callback<DateControl.CreateCalendarSourceParameter, CalendarSource> {

    private final Window owner;
    private ICalWebSourcePane pane;
    private Stage dialog;

    public ICalWebSourceFactory(Window owner) {
        this.owner = owner;
    }

    @Override
    public CalendarSource call(DateControl.CreateCalendarSourceParameter param) {
        if (dialog == null) {
            pane = new ICalWebSourcePane();
            pane.setOnCancelClicked(this::cancel);
            pane.setOnAcceptClicked(this::accept);
            pane.getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());

            dialog = new Stage();
            dialog.initOwner(owner);
            dialog.setScene(new Scene(pane));
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.setTitle("Add Web iCal");
            dialog.initModality(Modality.APPLICATION_MODAL);
        }

        dialog.showAndWait();

        return ICalRepository.getCommunityCalendarSource();
    }

    private void cancel(ActionEvent evt) {
        pane.clear();
        dialog.hide();
    }

    private void accept(ActionEvent evt) {
        String url = pane.getUrl();
        String name = pane.getName();
        Calendar.Style style = pane.getCalendarStyle();

        if (ICalRepository.existsWebCalendar(url)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("This calendar was already added!");
            alert.show();
            return;
        }

        ICalRepository.createWebCalendar(url, name, style, ICalRepository.getCommunityCalendarSource());
        pane.clear();
        dialog.hide();
    }
}
