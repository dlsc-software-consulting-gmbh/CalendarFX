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

package com.calendarfx.google.view.popover;

import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.model.GoogleEntryReminder;
import com.calendarfx.google.model.GoogleEntryReminder.RemindMethod;
import com.calendarfx.view.popover.EntryDetailsView;
import com.google.common.collect.Lists;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Custom details view which adds some additional controls to edit google entry
 * specific info.
 *
 * @author Gabriel Diaz, 07.03.2015.
 */
public class GoogleEntryDetailsView extends EntryDetailsView {

    private GoogleEntry entry;

    public GoogleEntryDetailsView(GoogleEntry entry) {
        super(requireNonNull(entry));

        this.entry = entry;

        Label notificationLabel = new Label("Notification:");

        Label addButton = new Label("Add a notification");
        addButton.getStyleClass().add("link");
        addButton.setOnMouseClicked(evt -> createReminder());
        addButton.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        VBox center = new VBox();

        BorderPane notificationPane = new BorderPane();
        notificationPane.setCenter(center);
        notificationPane.setBottom(addButton);

        GridPane box = (GridPane) getChildren().get(0);
        box.add(notificationLabel, 0, 5);
        box.add(notificationPane, 1, 5);

        GridPane.setValignment(notificationLabel, VPos.TOP);
        getStyleClass().add("details-view");

        if (entry.isUseDefaultReminder()) {
            GoogleCalendar calendar = (GoogleCalendar) entry.getCalendar();
            reminders.addAll(calendar.getDefaultReminders());
        }

        reminders.addAll(entry.getReminders());
        reminders.addListener((Observable obs) -> buildItems(center));
        buildItems(center);
    }

    private final ObservableList<GoogleEntryReminder> reminders = FXCollections.observableArrayList();

    private void buildItems(VBox parent) {
        List<GoogleEntryReminderItem> attendeesNode = Lists.newArrayList();
        for (GoogleEntryReminder reminder : reminders) {
            attendeesNode.add(new GoogleEntryReminderItem(reminder));
        }
        parent.getChildren().setAll(attendeesNode);
    }

    private void createReminder() {
        GoogleEntryReminder reminder = new GoogleEntryReminder();
        reminder.setMethod(RemindMethod.POPUP);
        reminder.setMinutes(10);
        reminders.add(reminder);
        entry.getReminders().add(reminder);
    }

    private void removeReminder(GoogleEntryReminder reminder) {
        reminders.remove(reminder);
        entry.getReminders().remove(reminder);
    }

    /**
     * Control that represents an event reminder item.
     *
     * @author Gabriel Diaz, 07.03.2015.
     */
    private class GoogleEntryReminderItem extends HBox {

        private GoogleEntryReminder reminder;
        private ComboBox<RemindMethod> methodCombo;
        private ComboBox<TimeUnit> unitCombo;
        private TextField valueTxt;
        private Label removeIcon;

        private GoogleEntryReminderItem(GoogleEntryReminder reminder) {
            this.reminder = requireNonNull(reminder);

            methodCombo = new ComboBox<>();
            methodCombo.getItems().setAll(RemindMethod.values());
            methodCombo.disableProperty().bind(entry.getCalendar().readOnlyProperty());
            methodCombo.valueProperty().bindBidirectional(reminder.methodProperty());
            methodCombo.setConverter(new StringConverter<RemindMethod>() {
                @Override
                public String toString(RemindMethod object) {
                    return object.getName();
                }

                @Override
                public RemindMethod fromString(String string) {
                    for (RemindMethod method : RemindMethod.values()) {
                        if (method.getName().equals(string)) {
                            return method;
                        }
                    }
                    return null;
                }
            });

            Integer minutes = reminder.getMinutes();
            TimeUnit unit = TimeUnit.MINUTES;
            if (minutes != null) {
                if (minutes % 1440 == 0) {
                    unit = TimeUnit.DAYS;
                    minutes = minutes / 1400;
                } else if (minutes % 60 == 0) {
                    unit = TimeUnit.HOURS;
                    minutes = minutes / 60;
                }
            }

            valueTxt = new TextField();
            valueTxt.disableProperty().bind(entry.getCalendar().readOnlyProperty());
            valueTxt.setPrefColumnCount(5);
            valueTxt.setText(minutes == null ? "" : minutes.toString());
            valueTxt.textProperty().addListener(obs -> updateMinutes());

            unitCombo = new ComboBox<>();
            unitCombo.getItems().setAll(TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS);
            unitCombo.disableProperty().bind(entry.getCalendar().readOnlyProperty());
            unitCombo.setValue(unit);
            unitCombo.valueProperty().addListener(obs -> updateMinutes());

            removeIcon = new Label();
            removeIcon.getStyleClass().add("button-icon");
            removeIcon.setGraphic(new FontAwesome().create(FontAwesome.Glyph.TRASH_ALT));
            removeIcon.setOnMouseClicked(evt -> removeReminder(reminder));
            removeIcon.disableProperty().bind(entry.getCalendar().readOnlyProperty());

            HBox.setHgrow(removeIcon, Priority.NEVER);
            setAlignment(Pos.CENTER_LEFT);
            getChildren().addAll(methodCombo, valueTxt, unitCombo, removeIcon);
            getStyleClass().add("notification-item");
        }

        private void updateMinutes() {
            Integer minutes = null;

            try {
                Integer value = Integer.valueOf(valueTxt.getText());

                if (unitCombo.getValue() == TimeUnit.DAYS) {
                    minutes = Math.toIntExact(TimeUnit.DAYS.toMinutes(value));
                } else if (unitCombo.getValue() == TimeUnit.HOURS) {
                    minutes = Math.toIntExact(TimeUnit.HOURS.toMinutes(value));
                } else {
                    minutes = value;
                }
            } catch (NumberFormatException e) {
                // DO nothing
            }

            reminder.setMinutes(minutes);
        }

    }

}
