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

package impl.com.calendarfx.google.view.log;

import com.calendarfx.google.view.log.ActionType;
import com.calendarfx.google.view.log.LogItem;
import com.calendarfx.google.view.log.LogPane;
import com.calendarfx.google.view.log.StatusType;
import com.google.api.client.util.Lists;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.Event;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.MasterDetailPane;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Skin for the log pane.
 *
 * Created by gdiaz on 22/02/2017.
 */
public class LogPaneSkin extends SkinBase<LogPane> {

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public LogPaneSkin(LogPane control, TableView<LogItem> table) {
        super(control);

        TextArea textArea = new TextArea();
        textArea.addEventFilter(KeyEvent.ANY, Event::consume);

        BorderPane tablePane = new BorderPane();
        tablePane.setTop(createToolBar());
        tablePane.setCenter(table);

        MasterDetailPane container = new MasterDetailPane();
        container.setMasterNode(tablePane);
        container.setDetailNode(textArea);
        container.setDetailSide(Side.RIGHT);
        container.setDividerPosition(0.65);
        container.setShowDetailNode(false);

        control.getSelectedItems().addListener((Observable obs) -> {
            List<LogItem> item = control.getSelectedItems();
            if (item == null || item.isEmpty() || item.size() > 1 || item.get(0).getException() == null) {
                textArea.setText(null);
                container.setShowDetailNode(false);
            } else {
                StringWriter stackTraceWriter = new StringWriter();
                item.get(0).getException().printStackTrace(new PrintWriter(stackTraceWriter));
                textArea.setText(stackTraceWriter.toString());
                container.setShowDetailNode(true);
            }
        });

        getChildren().add(container);
    }

    private ToolBar createToolBar() {
        Button clearAllBtn = new Button();
        clearAllBtn.setText("Clear All");
        clearAllBtn.setGraphic(new FontIcon(FontAwesome.TRASH));
        clearAllBtn.setOnAction(evt -> getSkinnable().clearItems());

        Button clearSelectionBtn = new Button();
        clearSelectionBtn.setText("Clear Selected");
        clearSelectionBtn.setGraphic(new FontIcon(FontAwesome.TRASH_O));
        clearSelectionBtn.setOnAction(evt -> getSkinnable().removeItems(Lists.newArrayList(getSkinnable().getSelectedItems())));

        List<ToggleButton> statusTypeButtons = new ArrayList<>();

        InvalidationListener statusListener = obs -> {
            List<StatusType> statuses = Lists.newArrayList();
            for (ToggleButton btn : statusTypeButtons) {
                if (btn.isSelected()) {
                    statuses.add((StatusType) btn.getUserData());
                }
            }
            getSkinnable().filter(statuses);
        };


        for (StatusType type : StatusType.values()) {
            ToggleButton btn = new ToggleButton();
            btn.setSelected(true);
            btn.setGraphic(type.createView());
            btn.setTooltip(new Tooltip(type.getDisplayName()));
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setUserData(type);
            btn.selectedProperty().addListener(statusListener);
            statusTypeButtons.add(btn);
        }

        TextField textField = new TextField();
        textField.setPrefColumnCount(20);
        textField.setPromptText("Search: Calendar or Description");
        textField.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode() == KeyCode.ENTER) {
                getSkinnable().filter(textField.getText());
            }
        });
        Button searchBtn = new Button();
        searchBtn.setGraphic(new FontIcon(FontAwesome.SEARCH));
        searchBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        searchBtn.setOnAction(evt -> getSkinnable().filter(textField.getText()));

        ComboBox<ActionTypeWrapper> actionTypeComboBox = new ComboBox<>();
        actionTypeComboBox.getItems().addAll(ActionTypeWrapper.values());
        actionTypeComboBox.setEditable(false);
        actionTypeComboBox.valueProperty().addListener(obs -> getSkinnable().filter(actionTypeComboBox.getValue().getActionType()));

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(clearAllBtn);
        toolBar.getItems().add(clearSelectionBtn);
        toolBar.getItems().add(new Separator());
        toolBar.getItems().addAll(statusTypeButtons);
        toolBar.getItems().add(new Separator());
        toolBar.getItems().add(new Label("Action"));
        toolBar.getItems().add(actionTypeComboBox);
        toolBar.getItems().add(new Separator());
        toolBar.getItems().add(textField);
        toolBar.getItems().add(searchBtn);

        return toolBar;
    }

    private static class ActionTypeWrapper {

        private static final ActionTypeWrapper NULL_ITEM = new ActionTypeWrapper();

        private ActionType actionType;

        ActionTypeWrapper() {
            super();
        }

        ActionTypeWrapper(ActionType actionType) {
            this();
            this.actionType = actionType;
        }

        ActionType getActionType() {
            return actionType;
        }

        static List<ActionTypeWrapper> values() {
            List<ActionTypeWrapper> values = Lists.newArrayList();
            values.add(NULL_ITEM);
            for (ActionType actionType : ActionType.values()) {
                values.add(new ActionTypeWrapper(actionType));
            }
            return values;
        }

        @Override
        public String toString() {
            return actionType == null ? "" : actionType.getDisplayName();
        }
    }

}
