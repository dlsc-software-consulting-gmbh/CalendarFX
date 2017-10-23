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

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.model.LoadEvent;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DeveloperConsole;
import com.calendarfx.view.RequestEvent;
import com.calendarfx.view.TimeField;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static java.util.Objects.requireNonNull;

/**
 * A control used for showing the internals of CalendarFX at work. Helps
 * detecting problems.
 */
public class DeveloperConsoleSkin extends SkinBase<DeveloperConsole> {

    private TableView<LogEntry> tableView;
    private FilteredList<LogEntry> filteredList;
    private ToggleButton showLoadEvents;
    private ToggleButton showCalendarEvents;
    private ToggleButton showRequestEvents;
    private DatePicker datePicker;
    private DatePicker todayPicker;
    private TimeField timeField;

    private ObservableList<LogEntry> masterData = FXCollections.observableArrayList();
    private EventHandler<CalendarEvent> calendarListener = evt -> addEvent(evt, LogEntryType.CALENDAR_EVENT);

    public DeveloperConsoleSkin(DeveloperConsole view) {
        super(view);

        TabPane tabPane = view.getTabPane();
        tabPane.setSide(Side.TOP);

        getChildren().add(tabPane);

        this.tableView = new TableView<>();

        ToolBar toolbar = new ToolBar();

        this.datePicker = new DatePicker();
        this.todayPicker = new DatePicker();
        this.timeField = new TimeField();

        showCalendarEvents = new ToggleButton("Calendar Events"); //$NON-NLS-1$
        showCalendarEvents.setSelected(true);
        showCalendarEvents.setOnAction(evt -> filter());
        toolbar.getItems().add(showCalendarEvents);

        showLoadEvents = new ToggleButton("Load Events"); //$NON-NLS-1$
        showLoadEvents.setSelected(false);
        showLoadEvents.setOnAction(evt -> filter());
        toolbar.getItems().add(showLoadEvents);

        showRequestEvents = new ToggleButton("Request Events"); //$NON-NLS-1$
        showRequestEvents.setSelected(false);
        showRequestEvents.setOnAction(evt -> filter());
        toolbar.getItems().add(showRequestEvents);

        toolbar.getItems().add(new Separator());

        Button clearLog = new Button("Clear"); //$NON-NLS-1$
        clearLog.setOnAction(evt -> {
            masterData.clear();
            LogEntry.counter = 0;
        });

        toolbar.getItems().add(clearLog);

        toolbar.getItems().add(new Separator());

        toolbar.getItems().add(new Label("Date:")); //$NON-NLS-1$
        toolbar.getItems().add(datePicker);

        toolbar.getItems().add(new Label("Today:")); //$NON-NLS-1$
        toolbar.getItems().add(todayPicker);

        toolbar.getItems().add(new Label("Time:")); //$NON-NLS-1$
        toolbar.getItems().add(timeField);

        BorderPane eventsBorderPane = new BorderPane();
        eventsBorderPane.setBottom(toolbar);
        eventsBorderPane.setCenter(tableView);

        Tab tab = new Tab("Events", eventsBorderPane); //$NON-NLS-1$
        tabPane.getTabs().add(tab);

        TableColumn<LogEntry, Integer> counterColumn = new TableColumn<>("#"); //$NON-NLS-1$
        counterColumn
                .setCellValueFactory(new PropertyValueFactory<>("counter")); //$NON-NLS-1$
        counterColumn.setPrefWidth(50);

        TableColumn<LogEntry, LogEntryType> logEntryTypeColumn = new TableColumn<>(
                "Event"); //$NON-NLS-1$
        logEntryTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("logEntryType")); //$NON-NLS-1$
        logEntryTypeColumn.setPrefWidth(200);

        TableColumn<LogEntry, String> eventTypeColumn = new TableColumn<>(
                "Event Type"); //$NON-NLS-1$
        eventTypeColumn
                .setCellValueFactory(new PropertyValueFactory<>("eventType")); //$NON-NLS-1$
        eventTypeColumn.setPrefWidth(200);

        TableColumn<LogEntry, String> sourceColumn = new TableColumn<>(
                "Source"); //$NON-NLS-1$
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source")); //$NON-NLS-1$
        sourceColumn.setPrefWidth(120);

        TableColumn<LogEntry, String> targetColumn = new TableColumn<>(
                "Target"); //$NON-NLS-1$
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("target")); //$NON-NLS-1$
        targetColumn.setPrefWidth(120);

        TableColumn<LogEntry, LocalDateTime> newStartTimeColumn = new TableColumn<>(
                "Start"); //$NON-NLS-1$
        newStartTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("newStartTime")); //$NON-NLS-1$
        newStartTimeColumn.setPrefWidth(120);

        TableColumn<LogEntry, LocalDateTime> newEndTimeColumn = new TableColumn<>(
                "End"); //$NON-NLS-1$
        newEndTimeColumn
                .setCellValueFactory(new PropertyValueFactory<>("newEndTime")); //$NON-NLS-1$
        newEndTimeColumn.setPrefWidth(120);

        TableColumn<LogEntry, LocalDateTime> oldStartTimeColumn = new TableColumn<>(
                "Old Start"); //$NON-NLS-1$
        oldStartTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("oldStartTime")); //$NON-NLS-1$
        oldStartTimeColumn.setPrefWidth(120);

        TableColumn<LogEntry, LocalDateTime> oldEndTimeColumn = new TableColumn<>(
                "Old End"); //$NON-NLS-1$
        oldEndTimeColumn
                .setCellValueFactory(new PropertyValueFactory<>("oldEndTime")); //$NON-NLS-1$
        oldEndTimeColumn.setPrefWidth(120);

        TableColumn<LogEntry, String> timestampColumn = new TableColumn<>(
                "Timestamp"); //$NON-NLS-1$
        timestampColumn
                .setCellValueFactory(new PropertyValueFactory<>("timestamp")); //$NON-NLS-1$
        timestampColumn.setPrefWidth(120);

        TableColumn<LogEntry, String> descriptionColumn = new TableColumn<>(
                "Description"); //$NON-NLS-1$
        descriptionColumn
                .setCellValueFactory(new PropertyValueFactory<>("description")); //$NON-NLS-1$
        descriptionColumn.setPrefWidth(700);

        tableView.getColumns().setAll(counterColumn, logEntryTypeColumn,
                eventTypeColumn, sourceColumn, targetColumn, newStartTimeColumn,
                newEndTimeColumn, oldStartTimeColumn, oldEndTimeColumn,
                timestampColumn, descriptionColumn);

        filteredList = new FilteredList<>(masterData);

        tableView.setItems(filteredList);

        filter();

        updateSkin();

        view.dateControlProperty().addListener(it -> updateSkin());
    }

    private void updateSkin() {
        DeveloperConsole view = getSkinnable();
        if (view.getDateControl() != null) {
            setDateControl(view.getDateControl());
        }
    }

    /**
     * Sets the control that will be "monitored" by the developer console.
     *
     * @param control
     *            the monitored control
     */
    private void setDateControl(DateControl control) {
        requireNonNull(control);

        control.addEventFilter(RequestEvent.REQUEST,
                evt -> addEvent(evt, LogEntryType.REQUEST_EVENT));
        control.addEventFilter(LoadEvent.LOAD,
                evt -> addEvent(evt, LogEntryType.LOAD_EVENT));

        // listen to calendars

        for (Calendar calendar : control.getCalendars()) {
            calendar.addEventHandler(calendarListener);
        }

        ListChangeListener<? super Calendar> l = change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Calendar c : change.getAddedSubList()) {
                        c.addEventHandler(calendarListener);
                    }
                } else if (change.wasRemoved()) {
                    for (Calendar c : change.getRemoved()) {
                        c.removeEventHandler(calendarListener);
                    }
                }
            }
        };

        control.getCalendars().addListener(l);

        Bindings.bindBidirectional(datePicker.valueProperty(),
                control.dateProperty());
        Bindings.bindBidirectional(todayPicker.valueProperty(),
                control.todayProperty());
        Bindings.bindBidirectional(timeField.valueProperty(),
                control.timeProperty());
        timeField.setDisable(false);
    }

    private void filter() {
        filteredList.setPredicate(item -> {
            switch (item.getLogEntryType()) {
                case CALENDAR_EVENT:
                    return showCalendarEvents.isSelected();
                case INFO:
                    return true;
                case LOAD_EVENT:
                    return showLoadEvents.isSelected();
                case REQUEST_EVENT:
                    return showRequestEvents.isSelected();
                default:
                    return true;
            }
        });
    }

    private void addEvent(Event evt, LogEntryType type) {
        LogEntry entry = new LogEntry(type, evt);
        masterData.add(entry);
        limitListSize();
        tableView.scrollTo(entry);
    }

    private void limitListSize() {
        if (masterData.size() > 100) {
            masterData.remove(0, 10);
        }
    }

    enum LogEntryType {
        INFO, CALENDAR_EVENT, REQUEST_EVENT, LOAD_EVENT
    }

    public static class LogEntry {

        private static int counter = 0;

        private LocalDateTime timestamp = LocalDateTime.now();

        private LogEntryType logEntryType;

        private Event event;

        private int count;

        public LogEntry(LogEntryType type, Event event) {
            this.logEntryType = type;
            this.event = event;

            counter++;
            count = counter;
        }

        public int getCounter() {
            return count;
        }

        public Object getSource() {
            return event.getSource().getClass().getSimpleName();
        }

        public Object getTarget() {
            return event.getTarget().getClass().getSimpleName();
        }

        public String getDescription() {
            return event.toString();
        }

        public String getTimestamp() {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .format(timestamp);
        }

        public String getEventType() {
            return event.getEventType().getName();
        }

        public LogEntryType getLogEntryType() {
            return logEntryType;
        }

        public String getNewStartTime() {
            if (event instanceof CalendarEvent) {
                CalendarEvent evt = (CalendarEvent) event;
                Entry<?> entry = evt.getEntry();
                if (entry != null) {
                    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(entry.getStartAsLocalDateTime());
                } else {
                    return "";
                }
            } else if (event instanceof RequestEvent) {
                RequestEvent evt = (RequestEvent) event;
                if (evt.getDate() != null) {
                    return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                            .format(evt.getDate());
                } else if (evt.getDateTime() != null) {
                    return DateTimeFormatter
                            .ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(evt.getDateTime());
                } else if (evt.getYearMonth() != null) {
                    return evt.getYearMonth().toString();
                } else if (evt.getYearMonth() != null) {
                    return evt.getYear().toString();
                }
            }

            return null;
        }

        public String getNewEndTime() {
            if (event instanceof CalendarEvent) {
                CalendarEvent evt = (CalendarEvent) event;
                Entry<?> entry = evt.getEntry();
                if (entry != null) {
                    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(entry.getEndAsLocalDateTime());
                }
            }

            return null;
        }

        public String getOldStartTime() {
            if (event instanceof CalendarEvent) {
                CalendarEvent evt = (CalendarEvent) event;
                Interval oldInterval = evt.getOldInterval();
                if (oldInterval != null) {
                    return DateTimeFormatter
                            .ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(LocalDateTime.of(oldInterval.getStartDate(),
                                    oldInterval.getStartTime()));
                }
            }

            return null;
        }

        public String getOldEndTime() {
            if (event instanceof CalendarEvent) {
                CalendarEvent evt = (CalendarEvent) event;
                Interval oldInterval = evt.getOldInterval();
                if (oldInterval != null) {
                    return DateTimeFormatter
                            .ofLocalizedDateTime(FormatStyle.SHORT)
                            .format(LocalDateTime.of(oldInterval.getEndDate(),
                                    oldInterval.getEndTime()));
                }
            }

            return null;
        }
    }
}
