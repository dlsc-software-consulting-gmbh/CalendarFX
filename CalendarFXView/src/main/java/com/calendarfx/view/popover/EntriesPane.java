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
import com.calendarfx.view.Messages;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class EntriesPane extends VBox {

    public EntriesPane() {
        setMinSize(0, 0);
        entries.addListener((Observable evt) -> update());
        getStyleClass().add("entries-pane");
        setAlignment(Pos.CENTER);
    }

    private final ObservableList<Entry<?>> entries = FXCollections.observableArrayList();

    public final ObservableList<Entry<?>> getEntries() {
        return entries;
    }

    private void update() {

        if (!entries.isEmpty()) {

            List<Entry<?>> workingList = new ArrayList<>(entries);

            /*
             * Individual calendars are already sorted, but now we are
             * displaying entries from several calendars, so let's resort.
             */
            Collections.sort(workingList);

            for (Entry<?> entry : workingList) {
                Calendar calendar = entry.getCalendar();

                BorderPane borderPane = new BorderPane();
                borderPane.getStyleClass().add("entry");

                Label titleLabel = new Label(entry.getTitle());
                BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
                titleLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                titleLabel.getStyleClass().add("title"); //$NON-NLS-1$
                borderPane.setCenter(titleLabel);

                Circle colorDot = new Circle();
                colorDot.setRadius(2.5);
                colorDot.getStyleClass().add(
                        calendar.getStyle() + "-icon-small"); //$NON-NLS-1$
                titleLabel.setGraphic(colorDot);

                Label timeLabel = new Label();
                if (entry.isFullDay()) {
                    timeLabel.setText(Messages.getString("EntriesPane.FULL_DAY")); //$NON-NLS-1$
                } else {
                    timeLabel.setText(DateTimeFormatter.ofLocalizedTime(
                            FormatStyle.SHORT).format(entry.getStartTime()));
                }

                borderPane.setRight(timeLabel);

                timeLabel.getStyleClass().add("time"); //$NON-NLS-1$
                BorderPane.setAlignment(timeLabel, Pos.CENTER_RIGHT);

                getChildren().add(borderPane);
            }
        }
    }
}