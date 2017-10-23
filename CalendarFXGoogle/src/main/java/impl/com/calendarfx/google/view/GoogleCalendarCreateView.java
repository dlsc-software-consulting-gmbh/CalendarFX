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

package impl.com.calendarfx.google.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.view.CalendarView;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

/**
 * Pane that allows to enter a web URL of an iCal.
 *
 * Created by gdiaz on 5/01/2017.
 */
final class GoogleCalendarCreateView extends BorderPane {

    private final TextField nameField;

    private final ComboBox<Calendar.Style> styleComboBox;

    private Stage dialog;

    GoogleCalendarCreateView(Consumer<CalendarViewBean> onAccept) {
        nameField = new TextField();
        styleComboBox = new ComboBox<>();
        styleComboBox.getItems().setAll(Calendar.Style.values());
        styleComboBox.setButtonCell(new StyleCell());
        styleComboBox.setCellFactory(listView -> new StyleCell());

        Button acceptButton = new Button("Accept");
        acceptButton.disableProperty().bind(Bindings.or(Bindings.isEmpty(nameField.textProperty()), Bindings.isNull(styleComboBox.valueProperty())));
        acceptButton.setOnAction(evt -> {
            if (onAccept != null) {
                CalendarViewBean bean = new CalendarViewBean();
                bean.setName(nameField.getText());
                bean.setStyle(styleComboBox.getValue());
                onAccept.accept(bean);
            }
            close();
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(evt -> close());

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("Name"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("Color"), 0, 1);
        gridPane.add(styleComboBox, 1, 1);
        gridPane.getStyleClass().add("center");
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setPadding(new Insets(10));

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
        setPadding(new Insets(15));
        setPrefWidth(300);
        getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());
    }

    final void show(Window owner) {
        if (dialog == null) {
            dialog = new Stage();
            dialog.initOwner(owner);
            dialog.setScene(new Scene(this));
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.setTitle("Add Calendar");
            dialog.initModality(Modality.APPLICATION_MODAL);
        }
        dialog.showAndWait();
    }

    private void close() {
        nameField.setText(null);
        styleComboBox.setValue(null);
        if (dialog != null) {
            dialog.hide();
        }
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

    static class CalendarViewBean {

        private String name;
        private Calendar.Style style;

        Calendar.Style getStyle() {
            return style;
        }

        void setStyle(Calendar.Style style) {
            this.style = style;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }
    }

}
