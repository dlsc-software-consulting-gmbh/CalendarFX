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

import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.Messages;
import com.calendarfx.view.RecurrenceView;
import com.calendarfx.view.TimeField;
import impl.com.calendarfx.view.ZoneIdStringConverter;
import impl.com.calendarfx.view.util.Util;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.time.ZoneId;

public class EntryDetailsView extends EntryPopOverPane {

    private final Label summaryLabel;
    private final MenuButton recurrenceButton;
    private final TimeField startTimeField = new TimeField();
    private final TimeField endTimeField = new TimeField();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final ComboBox<ZoneId> zoneBox = new ComboBox<>();
    private final TextArea noteField = new TextArea();
    private final Button submitNote = new Button();
    private final Entry<?> entry;

    private boolean updatingFields;

    private final InvalidationListener entryIntervalListener = it -> {
        updatingFields = true;
        try {
            Entry<?> entry = getEntry();
            startTimeField.setValue(entry.getStartTime());
            endTimeField.setValue(entry.getEndTime());
            startDatePicker.setValue(entry.getStartDate());
            endDatePicker.setValue(entry.getEndDate());
            zoneBox.setValue(entry.getZoneId());
            //noteField.setText(entry.getEntryNotes());
            //noteField.setText("MEMES");
        } finally {
            updatingFields = false;
        }
    };
// search
    private final WeakInvalidationListener weakEntryIntervalListener = new WeakInvalidationListener(entryIntervalListener);

    private final InvalidationListener recurrenceRuleListener = it -> updateRecurrenceRuleButton(getEntry());

    private final WeakInvalidationListener weakRecurrenceRuleListener = new WeakInvalidationListener(recurrenceRuleListener);

    private final InvalidationListener updateSummaryLabelListener = it -> updateSummaryLabel(getEntry());

    private final WeakInvalidationListener weakUpdateSummaryLabelListener = new WeakInvalidationListener(updateSummaryLabelListener);

    public EntryDetailsView(Entry<?> entry, DateControl dateControl) {
        super();

        this.entry = entry;



        getStyleClass().add("entry-details-view");

        Label fullDayLabel = new Label(Messages.getString("EntryDetailsView.FULL_DAY"));
        Label startDateLabel = new Label(Messages.getString("EntryDetailsView.FROM"));
        Label endDateLabel = new Label(Messages.getString("EntryDetailsView.TO"));
        Label recurrentLabel = new Label(Messages.getString("EntryDetailsView.REPEAT"));

        summaryLabel = new Label();
        summaryLabel.getStyleClass().add("recurrence-summary-label");
        summaryLabel.setWrapText(true);
        summaryLabel.setMaxWidth(300);

        CheckBox fullDay = new CheckBox();
        fullDay.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        startTimeField.setValue(entry.getStartTime());
        startTimeField.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        endTimeField.setValue(entry.getEndTime());
        endTimeField.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        startDatePicker.setValue(entry.getStartDate());
        startDatePicker.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        endDatePicker.setValue(entry.getEndDate());
        endDatePicker.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        entry.intervalProperty().addListener(weakEntryIntervalListener);

        HBox startDateBox = new HBox(10);
        HBox endDateBox = new HBox(10);

        startDateBox.setAlignment(Pos.CENTER_LEFT);
        endDateBox.setAlignment(Pos.CENTER_LEFT);

        startDateBox.getChildren().addAll(startDateLabel, startDatePicker, startTimeField);
        endDateBox.getChildren().addAll(endDateLabel, endDatePicker, endTimeField);

        fullDay.setSelected(entry.isFullDay());
        startDatePicker.setValue(entry.getStartDate());
        endDatePicker.setValue(entry.getEndDate());

        Label zoneLabel = new Label(Messages.getString("EntryDetailsView.TIMEZONE"));
        zoneLabel.visibleProperty().bind(dateControl.enableTimeZoneSupportProperty());
        zoneLabel.managedProperty().bind(dateControl.enableTimeZoneSupportProperty());

        SortedList<ZoneId> sortedZones = new SortedList<>(dateControl.getAvailableZoneIds());
        sortedZones.setComparator(new ZoneIdComparator());

        zoneBox.setItems(sortedZones);
        zoneBox.disableProperty().bind(entry.getCalendar().readOnlyProperty());
        zoneBox.setConverter(new ZoneIdStringConverter());
        zoneBox.setValue(entry.getZoneId());
        zoneBox.visibleProperty().bind(dateControl.enableTimeZoneSupportProperty());
        zoneBox.managedProperty().bind(dateControl.enableTimeZoneSupportProperty());

        recurrenceButton = new MenuButton(Messages.getString("EntryDetailsView.MENU_BUTTON_NONE"));

        MenuItem none = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_NONE"));
        MenuItem everyDay = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_EVERY_DAY"));
        MenuItem everyWeek = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_EVERY_WEEK"));
        MenuItem everyMonth = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_EVERY_MONTH"));
        MenuItem everyYear = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_EVERY_YEAR"));
        MenuItem custom = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_CUSTOM"));

        none.setOnAction(evt -> updateRecurrenceRule(entry, null));
        everyDay.setOnAction(evt -> updateRecurrenceRule(entry, "RRULE:FREQ=DAILY"));
        everyWeek.setOnAction(evt -> updateRecurrenceRule(entry, "RRULE:FREQ=WEEKLY"));
        everyMonth.setOnAction(evt -> updateRecurrenceRule(entry, "RRULE:FREQ=MONTHLY"));
        everyYear.setOnAction(evt -> updateRecurrenceRule(entry, "RRULE:FREQ=YEARLY"));
        custom.setOnAction(evt -> showRecurrenceEditor(entry));

        recurrenceButton.getItems().setAll(none, everyDay, everyWeek, everyMonth, everyYear, new SeparatorMenuItem(), custom);
        recurrenceButton.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        EntryMapView mapView = new EntryMapView(entry);
        noteField.setText(entry.getEntryNotes());

        if(noteField.getText() == null){
            noteField.setPromptText("Enter notes...");
        }


        submitNote.setText("Save Note");

        GridPane box = new GridPane();
        box.getStyleClass().add("content");
        //.add(fullDayLabel, 0, 0);
        //box.add(fullDay, 1, 0);
        //box.add(startDateLabel, 0, 1);
        //box.add(startDateBox, 1, 1);
      //  box.add(endDateLabel, 0, 2);
       // box.add(endDateBox, 1, 2);
       // box.add(zoneLabel, 0, 3);
        //box.add(zoneBox, 1, 3);
        box.add(noteField, 0, 0);
        box.add(submitNote,1,0);
        //box.add(recurrentLabel, 0, 4);
        //box.add(recurrenceButton, 1, 4);
        //box.add(summaryLabel, 1, 5);
        //box.add(mapView, 1, 6);

        GridPane.setFillWidth(zoneBox, true);
        GridPane.setHgrow(zoneBox, Priority.ALWAYS);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();

        col1.setHalignment(HPos.RIGHT);
        col2.setHalignment(HPos.LEFT);

        box.getColumnConstraints().addAll(col1, col2);

        getChildren().add(box);

        startTimeField.visibleProperty().bind(Bindings.not(entry.fullDayProperty()));
        endTimeField.visibleProperty().bind(Bindings.not(entry.fullDayProperty()));

        EventHandler<ActionEvent> submission = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                entry.setNotes(noteField.getText());
                System.out.println(entry.getEntryNotes());
            }
        };

        submitNote.setOnAction(submission);

        // start date and time
        startDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingFields) {
                // Work-Around for DatePicker bug introduced with 18+9 ("commit on focus lost").
                startDatePicker.getEditor().setText(startDatePicker.getConverter().toString(newValue));
                entry.changeStartDate(newValue, true);
            }
        });

        startTimeField.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingFields) {
                entry.changeStartTime(newValue, true);
            }
        });

        // end date and time
        endDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingFields) {
                // Work-Around for DatePicker bug introduced with 18+9 ("commit on focus lost").
                endDatePicker.getEditor().setText(endDatePicker.getConverter().toString(newValue));
                entry.changeEndDate(newValue, false);
            }
        });

        endTimeField.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingFields) {
                entry.changeEndTime(newValue, false);
            }
        });

        zoneBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingFields && zoneBox.getValue() != null) {
                entry.changeZoneId(newValue);
            }
        });

        // full day
        fullDay.setOnAction(evt -> entry.setFullDay(fullDay.isSelected()));

        entry.recurrenceRuleProperty().addListener(weakRecurrenceRuleListener);

        updateRecurrenceRuleButton(entry);
        updateSummaryLabel(entry);

        entry.recurrenceRuleProperty().addListener(weakUpdateSummaryLabelListener);

        //entry.setNotes(noteField.getText().replaceAll("\n", System.getProperty("line.separator")));

    }

    public final Entry<?> getEntry() {
        return entry;
    }

    private void updateSummaryLabel(Entry<?> entry) {
        String rule = entry.getRecurrenceRule();
        if (rule != null && !rule.trim().equals("")) {
            String text = Util.convertRFC2445ToText(rule, entry.getStartDate());
            summaryLabel.setText(text);
            summaryLabel.setVisible(true);
            summaryLabel.setManaged(true);
        } else {
            summaryLabel.setText("");
            summaryLabel.setVisible(false);
            summaryLabel.setManaged(false);
        }
    }

    private void showRecurrenceEditor(Entry<?> entry) {
        RecurrencePopup popup = new RecurrencePopup();
        RecurrenceView recurrenceView = popup.getRecurrenceView();
        String recurrenceRule = entry.getRecurrenceRule();
        if (recurrenceRule == null || recurrenceRule.trim().equals("")) {
            recurrenceRule = "RRULE:FREQ=DAILY;";
        }
        recurrenceView.setRecurrenceRule(recurrenceRule);
        popup.setOnOkPressed(evt -> {
            String rrule = recurrenceView.getRecurrenceRule();
            entry.setRecurrenceRule(rrule);
        });

        Point2D anchor = recurrenceButton.localToScreen(0, recurrenceButton.getHeight());
        popup.show(recurrenceButton, anchor.getX(), anchor.getY());
    }

    private void updateRecurrenceRule(Entry<?> entry, String rule) {
        entry.setRecurrenceRule(rule);
    }

    private void updateRecurrenceRuleButton(Entry<?> entry) {
        String rule = entry.getRecurrenceRule();
        if (rule == null) {
            recurrenceButton.setText(Messages.getString("EntryDetailsView.NONE"));
        } else {
            switch (rule.trim().toUpperCase()) {
                case "RRULE:FREQ=DAILY":
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.DAILY"));
                    break;
                case "RRULE:FREQ=WEEKLY":
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.WEEKLY"));
                    break;
                case "RRULE:FREQ=MONTHLY":
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.MONTHLY"));
                    break;
                case "RRULE:FREQ=YEARLY":
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.YEARLY"));
                    break;
                default:
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.CUSTOM"));
                    break;
            }
        }
    }
}
