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

package com.calendarfx.view.popover;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarSelector;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.Messages;
import javafx.beans.binding.Bindings;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class EntryHeaderView extends GridPane {

    private final CalendarSelector calendarSelector;

    private Entry<?> entry;

    public EntryHeaderView(Entry<?> entry, List<Calendar> calendars) {
        this.entry = requireNonNull(entry);
        requireNonNull(calendars);

        getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());

        TextField titleField = new TextField(entry.getTitle());
        Bindings.bindBidirectional(titleField.textProperty(), entry.titleProperty());

        titleField.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        TextField locationField = new TextField(entry.getLocation());
        Bindings.bindBidirectional(locationField.textProperty(), entry.locationProperty());
        locationField.getStyleClass().add("location"); //$NON-NLS-1$
        locationField.setEditable(true);
        locationField.setPromptText(Messages.getString("EntryHeaderView.PROMPT_LOCATION")); //$NON-NLS-1$
        locationField.setMaxWidth(500);
        locationField.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        calendarSelector = new CalendarSelector();
        calendarSelector.disableProperty().bind(entry.getCalendar().readOnlyProperty());
        calendarSelector.getCalendars().setAll(calendars);
        calendarSelector.setCalendar(entry.getCalendar());
        Bindings.bindBidirectional(calendarSelector.calendarProperty(), entry.calendarProperty());

        titleField.getStyleClass().add("default-style-entry-popover-title"); //$NON-NLS-1$

        add(titleField, 0, 0);
        add(calendarSelector, 1, 0, 1, 2);
        add(locationField, 0, 1);

        RowConstraints row1 = new RowConstraints();
        row1.setValignment(VPos.TOP);
        row1.setFillHeight(true);

        RowConstraints row2 = new RowConstraints();
        row2.setValignment(VPos.TOP);
        row2.setFillHeight(true);

        getRowConstraints().addAll(row1, row2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setFillWidth(true);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow(Priority.NEVER);

        getColumnConstraints().addAll(col1, col2);

        getStyleClass().add("popover-header"); //$NON-NLS-1$

        titleField.getStyleClass().add("title"); //$NON-NLS-1$
        titleField.setPromptText(Messages.getString("EntryHeaderView.PROMPT_TITLE")); //$NON-NLS-1$
        titleField.setMaxWidth(500);

        Calendar calendar = entry.getCalendar();

        titleField.getStyleClass().add(calendar.getStyle() + "-entry-popover-title"); //$NON-NLS-1$

        entry.calendarProperty()
                .addListener((observable, oldCalendar, newCalendar) -> {
                    if (oldCalendar != null) {
                        titleField.getStyleClass().remove(oldCalendar.getStyle() + "-entry-popover-title"); //$NON-NLS-1$
                    }
                    if (newCalendar != null) {
                        titleField.getStyleClass().add(newCalendar.getStyle() + "-entry-popover-title"); //$NON-NLS-1$
                    }
                });
    }

    /**
     * Returns the currently selected calendar.
     *
     * @return the selected calendar
     */
    public final Calendar getCalendar() {
        Calendar calendar = calendarSelector.getCalendar();
        if (calendar == null) {
            calendar = entry.getCalendar();
        }

        return calendar;
    }
}