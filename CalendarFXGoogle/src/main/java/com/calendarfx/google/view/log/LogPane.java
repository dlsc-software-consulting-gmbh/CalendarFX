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

package com.calendarfx.google.view.log;

import impl.com.calendarfx.google.view.log.LogPaneSkin;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Pane that displays a table with all log items registered by the app.
 *
 * Created by gdiaz on 22/02/2017.
 */
public class LogPane extends Control {

    private final ObservableList<LogItem> items = FXCollections.observableArrayList();
    private final FilteredList filteredData;
    private final TableView<LogItem> table;
    private LogTableFilter filter;

    public LogPane() {
        super();
        table = new TableView<>();

        TableColumn<LogItem, StatusType> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.prefWidthProperty().bind(Bindings.multiply(0.1, table.widthProperty()));
        statusColumn.setCellFactory(col -> new StatusTypeCell());

        TableColumn<LogItem, ActionType> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionColumn.prefWidthProperty().bind(Bindings.multiply(0.1, table.widthProperty()));
        actionColumn.setCellFactory(col -> new ActionTypeCell());

        TableColumn<LogItem, LocalDateTime> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.prefWidthProperty().bind(Bindings.multiply(0.2, table.widthProperty()));
        timeColumn.setCellFactory(col -> new TimeCell());

        TableColumn<LogItem, String> calendarColumn = new TableColumn<>("Calendar");
        calendarColumn.setCellValueFactory(new PropertyValueFactory<>("calendar"));
        calendarColumn.prefWidthProperty().bind(Bindings.multiply(0.2, table.widthProperty()));

        TableColumn<LogItem, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.prefWidthProperty().bind(Bindings.multiply(0.4, table.widthProperty()));

        filteredData = new FilteredList<>(items);
        SortedList<LogItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().add(statusColumn);
        table.getColumns().add(actionColumn);
        table.getColumns().add(timeColumn);
        table.getColumns().add(calendarColumn);
        table.getColumns().add(descriptionColumn);
        table.setTableMenuButtonVisible(true);
        table.setItems(sortedData);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new LogPaneSkin(this, table);
    }

    public final ObservableList<LogItem> getItems() {
        return items;
    }

    public final void clearItems() {
        items.clear();
        table.getSelectionModel().clearSelection();
    }

    public final void removeItems(Collection<LogItem> items) {
        this.items.removeAll(items);
        table.getSelectionModel().clearSelection();
    }

    public final ObservableList<LogItem> getSelectedItems() {
        return table.getSelectionModel().getSelectedItems();
    }

    public final void filter(Collection<StatusType> statuses) {
        filter = new LogTableFilter(filter);
        filter.setStatusTypes(statuses);
        filteredData.setPredicate(filter);
    }

    public final void filter(String text) {
        filter = new LogTableFilter(filter);
        filter.setText(text);
        filteredData.setPredicate(filter);
    }

    public final void filter(ActionType actionType) {
        filter = new LogTableFilter(filter);
        filter.setActionType(actionType);
        filteredData.setPredicate(filter);
    }

    /**
     * Filter used for the table.
     */
    private static class LogTableFilter implements Predicate<LogItem> {

        private Collection<StatusType> statuses;
        private String text;
        private ActionType actionType;

        LogTableFilter(LogTableFilter oldFilter) {
            if (oldFilter != null) {
                this.statuses = oldFilter.statuses;
                this.text = oldFilter.text;
                this.actionType = oldFilter.actionType;
            }
        }

        void setStatusTypes(Collection<StatusType> statuses) {
            this.statuses = statuses;
        }

        void setText(String text) {
            this.text = text;
        }

        void setActionType(ActionType actionType) {
            this.actionType = actionType;
        }

        @Override
        public boolean test(LogItem logItem) {
            if (statuses != null && !statuses.contains(logItem.getStatus())) {
                return false;
            }

            if (actionType != null && !actionType.equals(logItem.getAction())) {
                return false;
            }

            if (text != null && !text.isEmpty()) {
                String textLower = text.toLowerCase();

                if (logItem.getDescription() != null) {
                    String descriptionLower = logItem.getDescription().toLowerCase();
                    if (descriptionLower.contains(textLower)) {
                        return true;
                    }
                }

                if (logItem.getCalendar() != null) {
                    String calendarLower = logItem.getCalendar().toLowerCase();
                    return calendarLower.contains(textLower);
                }

                return false;
            }

            return true;
        }
    }

    /**
     * Cell for the status column.
     */
    private static class StatusTypeCell extends TableCell<LogItem, StatusType> {
        @Override
        protected void updateItem(StatusType item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setTooltip(null);
            setAlignment(Pos.CENTER);
            setGraphic(null);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            if (item != null && !empty) {
                setGraphic(item.createView());
                setTooltip(new Tooltip(item.getDisplayName()));
            }
        }
    }

    /**
     * Cell for the action type column.
     */
    private static class ActionTypeCell extends TableCell<LogItem, ActionType> {
        @Override
        protected void updateItem(ActionType item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setTextFill(Color.BLACK);
            if (item != null && !empty) {
                setText(item.getDisplayName());
            }
        }
    }

    /**
     * Cell for the time column.
     */
    private static class TimeCell extends TableCell<LogItem, LocalDateTime> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

        @Override
        protected void updateItem(LocalDateTime item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (item != null && !empty) {
                setText(FORMATTER.format(item));
            }
        }
    }

}
