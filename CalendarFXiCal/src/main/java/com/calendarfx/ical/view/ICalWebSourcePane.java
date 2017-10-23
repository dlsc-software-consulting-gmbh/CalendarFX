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

import com.calendarfx.model.Calendar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Pane that allows to enter a web URL of an iCal.
 *
 * Created by gdiaz on 5/01/2017.
 */
public class ICalWebSourcePane extends BorderPane {

    private final TextField urlField;

    private final TextField nameField;

    private final ComboBox<Calendar.Style> styleComboBox;

    private final Button acceptButton;

    private final Button cancelButton;

    public ICalWebSourcePane() {
        urlField = new TextField();
        urlField.setPrefWidth(300);
        nameField = new TextField();
        styleComboBox = new ComboBox<>();
        styleComboBox.getItems().setAll(Calendar.Style.values());
        styleComboBox.setButtonCell(new StyleCell());
        styleComboBox.setCellFactory(listView -> new StyleCell());

        acceptButton = new Button("Accept");
        cancelButton = new Button("Cancel");

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("URL"), 0, 0);
        gridPane.add(urlField, 1, 0);
        gridPane.add(new Label("Name"), 0, 1);
        gridPane.add(nameField, 1, 1);
        gridPane.add(new Label("Color"), 0, 2);
        gridPane.add(styleComboBox, 1, 2);
        gridPane.getStyleClass().add("center");
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setPadding(new Insets(10));

        GridPane.setHgrow(urlField, Priority.ALWAYS);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(styleComboBox, Priority.ALWAYS);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(acceptButton, cancelButton);

        VBox bottomPane = new VBox();
        bottomPane.getChildren().addAll(new Separator(), buttonBar);
        bottomPane.getStyleClass().add("bottom");
        bottomPane.setFillWidth(true);
        bottomPane.setSpacing(10);

        setCenter(gridPane);
        setBottom(bottomPane);
        getStyleClass().add("ical-web-source-pane");
        setPadding(new Insets(15));
    }

    public final void setOnCancelClicked(EventHandler<ActionEvent> onCancelClicked) {
        cancelButton.setOnAction(onCancelClicked);
    }

    public final void setOnAcceptClicked(EventHandler<ActionEvent> onAcceptClicked) {
        acceptButton.setOnAction(onAcceptClicked);
    }

    public final String getUrl() {
        return urlField.getText();
    }

    public final String getName() {
        return nameField.getText();
    }

    public final Calendar.Style getCalendarStyle() {
        return styleComboBox.getValue();
    }

    public final void clear() {
        urlField.setText(null);
        nameField.setText(null);
        styleComboBox.setValue(null);
    }

    private static class StyleCell extends ListCell<Calendar.Style> {
        @Override
        protected void updateItem(Calendar.Style item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                Rectangle icon = new Rectangle(12, 12);
                icon.getStyleClass().add(item.name().toLowerCase() + "-icon");
                setGraphic(icon);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }

    }

}
