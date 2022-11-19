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

package com.calendarfx.view;

import com.calendarfx.model.Entry;
import impl.com.calendarfx.view.AgendaViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet.Item;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The agenda view displays calendar entries in a list. The view can be
 * configured to look back a given number of days and also to look forward a
 * given number of days.
 *
 * <img src="doc-files/agenda-view.png" alt="Agenda View">
 */
public class AgendaView extends DateControl {

    private static final String DEFAULT_STYLE_CLASS = "agenda-view";
    private static final String AGENDA_CATEGORY = "Agenda View";

    private final ListView<AgendaEntry> listView = new ListView<>();

    /**
     * Constructs a new agenda view.
     */
    public AgendaView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        listView.setCellFactory(cbListView -> getCellFactory().call(this));
        setContextMenu(buildContextMenu());
    }

    /**
     * Returns the list view that will be used to display one cell for each day that
     * contains at least one calendar entry.
     *
     * @return the list view used by this control
     */
    public final ListView<AgendaEntry> getListView() {
        return listView;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AgendaViewSkin(this);
    }

    private ContextMenu buildContextMenu() {
        ContextMenu menu = new ContextMenu();
        Menu lookBackMenu = new Menu(Messages.getString("AgendaView.MENU_ITEM_LOOK_BACK"));
        Menu lookAheadMenu = new Menu(Messages.getString("AgendaView.MENU_ITEM_LOOK_AHEAD"));

        String format = Messages.getString("AgendaView.MENU_ITEM_DAYS");

        MenuItem lookBack0 = new MenuItem(MessageFormat.format(format, 0));
        MenuItem lookBack10 = new MenuItem(MessageFormat.format(format, 10));
        MenuItem lookBack20 = new MenuItem(MessageFormat.format(format, 20));
        MenuItem lookBack30 = new MenuItem(MessageFormat.format(format, 30));
        MenuItem lookAhead0 = new MenuItem(MessageFormat.format(format, 0));
        MenuItem lookAhead10 = new MenuItem(MessageFormat.format(format, 10));
        MenuItem lookAhead20 = new MenuItem(MessageFormat.format(format, 20));
        MenuItem lookAhead30 = new MenuItem(MessageFormat.format(format, 30));
        lookBackMenu.getItems().addAll(lookBack0, lookBack10, lookBack20, lookBack30);
        lookAheadMenu.getItems().addAll(lookAhead0, lookAhead10, lookAhead20, lookAhead30);
        menu.getItems().addAll(lookBackMenu, lookAheadMenu);

        lookBack0.setOnAction(evt -> setLookBackPeriodInDays(0));
        lookBack10.setOnAction(evt -> setLookBackPeriodInDays(10));
        lookBack20.setOnAction(evt -> setLookBackPeriodInDays(20));
        lookBack30.setOnAction(evt -> setLookBackPeriodInDays(30));

        lookAhead0.setOnAction(evt -> setLookAheadPeriodInDays(0));
        lookAhead10.setOnAction(evt -> setLookAheadPeriodInDays(10));
        lookAhead20.setOnAction(evt -> setLookAheadPeriodInDays(20));
        lookAhead30.setOnAction(evt -> setLookAheadPeriodInDays(30));

        return menu;
    }

    private final IntegerProperty lookBackPeriodInDays = new SimpleIntegerProperty(this, "lookBackPeriodInDays", 0);

    /**
     * Stores the number of days to "look back" into the past when loading data.
     *
     * @return the number of days to look back
     */
    public final IntegerProperty lookBackPeriodInDaysProperty() {
        return lookBackPeriodInDays;
    }

    /**
     * Gets the value of {@link #lookBackPeriodInDaysProperty()}.
     *
     * @return the number of days to look back
     */
    public final int getLookBackPeriodInDays() {
        return lookBackPeriodInDaysProperty().get();
    }

    /**
     * Sets the value of {@link #lookBackPeriodInDaysProperty()}.
     *
     * @param days the new number of days to look back
     */
    public final void setLookBackPeriodInDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("days must be larger than or equal to 0");
        }
        lookBackPeriodInDaysProperty().set(days);
    }

    private final IntegerProperty lookAheadPeriodInDays = new SimpleIntegerProperty(this, "lookAheadPeriodInDays", 30);

    /**
     * Stores the number of days to "look ahead" into the future when loading
     * its data.
     *
     * @return the number of days to "look ahead"
     */
    public final IntegerProperty lookAheadPeriodInDaysProperty() {
        return lookAheadPeriodInDays;
    }

    /**
     * Returns the value of {@link #lookAheadPeriodInDaysProperty()}.
     *
     * @return the number of days to look ahead
     */
    public final int getLookAheadPeriodInDays() {
        return lookAheadPeriodInDaysProperty().get();
    }

    /**
     * Sets the value of {@link #lookAheadPeriodInDaysProperty()}.
     *
     * @param days the number of days to look ahead
     */
    public final void setLookAheadPeriodInDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("days must be larger than or equal to 0");
        }
        lookAheadPeriodInDaysProperty().set(days);
    }

    private final BooleanProperty showStatusLabel = new SimpleBooleanProperty(this, "showStatusLabel", true);

    public final BooleanProperty showStatusLabelProperty() {
        return showStatusLabel;
    }

    public final boolean isShowStatusLabel() {
        return showStatusLabelProperty().get();
    }

    public final void setShowStatusLabel(boolean showStatusLabel) {
        showStatusLabelProperty().set(showStatusLabel);
    }

    private final ObjectProperty<Callback<AgendaView, ? extends AgendaEntryCell>> cellFactory = new SimpleObjectProperty<Callback<AgendaView, ? extends AgendaEntryCell>>(this, "cellFactory", view -> new AgendaEntryCell(this)) {
        @Override
        public void set(Callback<AgendaView, ? extends AgendaEntryCell> newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    public final ObjectProperty<Callback<AgendaView, ? extends AgendaEntryCell>> cellFactoryProperty() {
        return cellFactory;
    }

    public final Callback<AgendaView, ? extends AgendaEntryCell> getCellFactory() {
        return cellFactoryProperty().get();
    }

    public final void setCellFactory(Callback<AgendaView, ? extends AgendaEntryCell> cellFactory) {
        cellFactoryProperty().set(cellFactory);
    }

    private final ObjectProperty<DateTimeFormatter> formatter = new SimpleObjectProperty<>(this, "formatter", DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));

    /**
     * Gets the DateTimeFormatter property, which is use to provide the format on the TimeScale Labels. By default it
     * has a value of {@link FormatStyle#LONG}
     *
     * @return the date formatter.
     */
    public final ObjectProperty<DateTimeFormatter> dateTimeFormatterProperty() {
        return formatter;
    }

    /**
     * Returns the value of {@link #dateTimeFormatterProperty()}
     *
     * @return a date time formatter
     */
    public final DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatterProperty().get();
    }

    /**
     * Sets the value of {@link #dateTimeFormatterProperty()}
     *
     * @param formatter a date time formatter, not {@code null}
     */
    public final void setDateTimeFormatter(DateTimeFormatter formatter) {
        requireNonNull(formatter);
        dateTimeFormatterProperty().set(formatter);
    }

    /**
     * Agenda entries are model objects that reference a collection of calendar
     * entries for a specific date.
     */
    public static class AgendaEntry implements Comparable<AgendaEntry> {

        private final LocalDate date;

        public AgendaEntry(LocalDate date) {
            this.date = requireNonNull(date);
        }

        public LocalDate getDate() {
            return date;
        }

        private final List<Entry<?>> entries = new ArrayList<>();

        public final List<Entry<?>> getEntries() {
            return entries;
        }

        @Override
        public int compareTo(AgendaEntry o) {
            return getDate().compareTo(o.getDate());
        }
    }

    /**
     * A specialized list cell that is capable of displaying all entries currently assigned
     * to a given day. Each cell features a header that shows the date information and a body
     * that lists all entries. Each entry is visualized with an icon, a title text, and a
     * time text.
     *
     * @see AgendaView#getListView()
     * @see ListView#setCellFactory(javafx.util.Callback)
     */
    public static class AgendaEntryCell extends ListCell<AgendaEntry> {

        private static final String AGENDA_VIEW_LIST_CELL = "agenda-view-list-cell";
        private static final String AGENDA_VIEW_TIME_LABEL = "time-label";
        private static final String AGENDA_VIEW_TITLE_LABEL = "title-label";
        private static final String AGENDA_VIEW_BODY = "body";
        private static final String AGENDA_VIEW_DATE_LABEL = "date-label";
        private static final String AGENDA_VIEW_DATE_LABEL_TODAY = "today";
        private static final String AGENDA_VIEW_WEEKDAY_LABEL = "weekday-label";
        private static final String AGENDA_VIEW_WEEKDAY_LABEL_TODAY = "today";
        private static final String AGENDA_VIEW_HEADER = "header";
        private static final String AGENDA_VIEW_HEADER_TODAY = "today";
        private static final String AGENDA_VIEW_BODY_SEPARATOR = "separator";

        private DateTimeFormatter weekdayFormatter = DateTimeFormatter.ofPattern(Messages.getString("AgendaEntryCell.WEEKDAY_FORMAT"));
        private DateTimeFormatter mediumDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        private DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        private DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        private Label weekdayLabel;
        private Label dateLabel;
        private GridPane gridPane;
        private BorderPane headerPane;
        private final boolean headerPaneVisible;

        private final AgendaView agendaView;

        /**
         * Constructs a new cell that will work with the given agenda view.
         *
         * @param view the parent list view
         */
        public AgendaEntryCell(AgendaView view) {
            this(view, true);
        }

        /**
         * Constructs a new cell that will work with the given agenda view.
         *
         * @param view              the parent list view
         * @param headerPaneVisible flag to control the visibility of the cell's header.
         */
        public AgendaEntryCell(AgendaView view, boolean headerPaneVisible) {
            this.agendaView = Objects.requireNonNull(view);
            this.headerPaneVisible = headerPaneVisible;

            BorderPane borderPane = new BorderPane();
            borderPane.getStyleClass().add("container");
            borderPane.setTop(createHeader());
            borderPane.setCenter(createBody());

            setGraphic(borderPane);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            getStyleClass().add(AGENDA_VIEW_LIST_CELL);
        }

        /**
         * Creates the node used for the body part of each cell.
         * <p>
         * In this default implementation the body consists of a grid pane with
         * three columns. The middle column is used for showing the title of
         * calendar entries. This column will get whatever space is left after
         * the icon and the time column have used what they need. This means
         * that a very long title will automatically be truncated.
         *
         * @return the body node
         */
        protected Node createBody() {
            // icon column
            ColumnConstraints iconColumn = new ColumnConstraints();

            // title column
            ColumnConstraints descriptionColumn = new ColumnConstraints();
            descriptionColumn.setFillWidth(true);
            descriptionColumn.setHgrow(Priority.SOMETIMES);
            descriptionColumn.setMinWidth(0);
            descriptionColumn.setPrefWidth(0);

            // time column
            ColumnConstraints timeColumn = new ColumnConstraints();
            timeColumn.setHalignment(HPos.RIGHT);

            gridPane = new GridPane();
            gridPane.setGridLinesVisible(true);
            gridPane.setMinWidth(0);
            gridPane.setPrefWidth(0);
            gridPane.getStyleClass().add(AGENDA_VIEW_BODY);
            gridPane.getColumnConstraints().addAll(iconColumn, descriptionColumn, timeColumn);

            return gridPane;
        }

        /**
         * Creates the header part for each cell. The header consists of a border pane
         * with the weekday label in the "left" position and the date label in the "right"
         * position.
         *
         * @return the header node
         */
        protected Node createHeader() {
            headerPane = new BorderPane();
            headerPane.getStyleClass().add(AGENDA_VIEW_HEADER);
            headerPane.setVisible(headerPaneVisible);
            headerPane.managedProperty().bind(headerPane.visibleProperty());

            weekdayLabel = new Label();
            weekdayLabel.getStyleClass().add(AGENDA_VIEW_WEEKDAY_LABEL);
            weekdayLabel.setMinWidth(0);

            dateLabel = new Label();
            dateLabel.setMinWidth(0);
            dateLabel.getStyleClass().add(AGENDA_VIEW_DATE_LABEL);

            headerPane.setLeft(weekdayLabel);
            headerPane.setRight(dateLabel);

            return headerPane;
        }

        @Override
        protected void updateItem(AgendaEntry item, boolean empty) {
            super.updateItem(item, empty);
            gridPane.getChildren().clear();

            if (item != null) {
                LocalDate date = item.getDate();
                if (date.equals(agendaView.getToday())) {
                    if (!headerPane.getStyleClass().contains(AGENDA_VIEW_HEADER_TODAY)) {
                        headerPane.getStyleClass().add(AGENDA_VIEW_HEADER_TODAY);
                        dateLabel.getStyleClass().add(AGENDA_VIEW_DATE_LABEL_TODAY);
                        weekdayLabel.getStyleClass().add(AGENDA_VIEW_WEEKDAY_LABEL_TODAY);
                    }
                } else {
                    headerPane.getStyleClass().remove(AGENDA_VIEW_HEADER_TODAY);
                    dateLabel.getStyleClass().remove(AGENDA_VIEW_DATE_LABEL_TODAY);
                    weekdayLabel.getStyleClass().remove(AGENDA_VIEW_WEEKDAY_LABEL_TODAY);
                }

                dateLabel.setText(mediumDateFormatter.format(date));
                weekdayLabel.setText(weekdayFormatter.format(date));

                int count = item.getEntries().size();
                int row = 0;

                for (int i = 0; i < count; i++) {
                    Entry<?> entry = item.getEntries().get(i);

                    Node entryGraphic = createEntryGraphic(entry);
                    gridPane.add(entryGraphic, 0, row);

                    Label titleLabel = createEntryTitleLabel(entry);
                    titleLabel.setMinWidth(0);
                    gridPane.add(titleLabel, 1, row);

                    Label timeLabel = createEntryTimeLabel(entry);
                    timeLabel.setMinWidth(0);
                    //gridPane.add(timeLabel, 2, row);

                    if (count > 1 && i < count - 1) {
                        Region separator = new Region();
                        separator.getStyleClass().add(AGENDA_VIEW_BODY_SEPARATOR);
                        row++;
                        gridPane.add(separator, 0, row);
                        GridPane.setColumnSpan(separator, 3);
                        GridPane.setFillWidth(separator, true);
                    }

                    row++;
                }
                getGraphic().setVisible(true);
            } else {
                getGraphic().setVisible(false);
            }
        }

        /**
         * Creates the label used to display the time of the entry. The default implementation of this
         * method creates a label and sets the text returned by {@link #getTimeText(Entry)}.
         *
         * @param entry the entry for which the time will be displayed
         * @return a label for displaying the time information on the entry
         */
        protected Label createEntryTimeLabel(Entry<?> entry) {
            Label timeLabel = new Label(getTimeText(entry));
            timeLabel.getStyleClass().add(AGENDA_VIEW_TIME_LABEL);
            if (agendaView.isEnableHyperlinks()) {
                timeLabel.setOnMouseClicked(evt -> fireEvent(new RequestEvent(this, this, entry)));
                timeLabel.getStyleClass().add("date-hyperlink");
            }

            return timeLabel;
        }

        /**
         * Creates the label used to display the title of the entry. The default implementation of this
         * method creates a label and sets the text found in {@link Entry#getTitle()}.
         *
         * @param entry the entry for which the title will be displayed
         * @return a label for displaying the title of the entry
         */
        protected Label createEntryTitleLabel(Entry<?> entry) {
            Label titleLabel = new Label(entry.getTitle());
            titleLabel.getStyleClass().add(AGENDA_VIEW_TITLE_LABEL);
            if (agendaView.isEnableHyperlinks()) {
                titleLabel.setOnMouseClicked(evt -> fireEvent(new RequestEvent(this, this, entry)));
                titleLabel.getStyleClass().add("date-hyperlink");
            }
            return titleLabel;
        }

        /**
         * Creates a node used to display an icon for the entry. The default implementation of this method
         * creates a node of type {@link Circle}. The color of the circle will match the color of
         * the calendar to which the entry belongs.
         * <pre>
         * 	  Circle circle = new Circle(4);
         * 	  circle.getStyleClass().add(entry.getCalendar().getStyle() + "-icon");
         * </pre>
         *
         * @param entry the entry for which the icon will be displayed
         * @return a node for displaying a graphic for the entry
         */
        protected Node createEntryGraphic(Entry<?> entry) {
            Circle circle = new Circle(4);
            //circle.getStyleClass().add(entry.getCalendar().getStyle() + "-icon");
            return circle;
        }

        /**
         * Creates a nicely formatted text that contains the start and end time of
         * the given entry. The text can also be something like "full day" if the entry
         * is a full-day entry.
         *
         * @param entry the entry for which the text will be created
         * @return a text showing the start and end times of the entry
         */
        protected String getTimeText(Entry<?> entry) {
            if (entry.isFullDay()) {
                return Messages.getString("AgendaEntryCell.ALL_DAY");
            }

            LocalDate startDate = entry.getStartDate();
            LocalDate endDate = entry.getEndDate();

            String text;

            if (startDate.equals(endDate)) {

                if (Objects.equals(entry.getZoneId(), agendaView.getZoneId())) {
                    text = MessageFormat.format(Messages.getString("AgendaEntryCell.ENTRY_TIME_RANGE"),
                            timeFormatter.format(entry.getStartAsZonedDateTime()), timeFormatter.format(entry.getEndAsZonedDateTime()));
                } else {
                    text = MessageFormat.format(Messages.getString("AgendaEntryCell.ENTRY_TIME_RANGE"),
                            timeFormatter.format(entry.getStartAsZonedDateTime().withZoneSameInstant(agendaView.getZoneId())), timeFormatter.format(entry.getEndAsZonedDateTime().withZoneSameInstant(agendaView.getZoneId())));
                    text = text + " (" + MessageFormat.format(Messages.getString("AgendaEntryCell.ENTRY_TIME_RANGE"),
                            timeFormatter.format(entry.getStartAsZonedDateTime()), timeFormatter.format(entry.getEndAsZonedDateTime())) + " " +
                            entry.getZoneId().getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                            ")";
                }
            } else {

                if (Objects.equals(entry.getZoneId(), agendaView.getZoneId())) {
                    text = MessageFormat.format(Messages.getString("AgendaEntryCell.ENTRY_TIME_RANGE_WITH_DATE"),
                            shortDateFormatter.format(entry.getStartAsZonedDateTime()), timeFormatter.format(entry.getStartAsZonedDateTime()), shortDateFormatter.format(entry.getEndAsZonedDateTime()),
                            timeFormatter.format(entry.getEndAsZonedDateTime()));
                } else {
                    text = MessageFormat.format(Messages.getString("AgendaEntryCell.ENTRY_TIME_RANGE_WITH_DATE"),
                            shortDateFormatter.format(entry.getStartAsZonedDateTime().withZoneSameInstant(agendaView.getZoneId())), timeFormatter.format(entry.getStartAsZonedDateTime().withZoneSameInstant(agendaView.getZoneId())), shortDateFormatter.format(entry.getEndAsZonedDateTime().withZoneSameInstant(agendaView.getZoneId())),
                            timeFormatter.format(entry.getEndAsZonedDateTime().withZoneSameInstant(agendaView.getZoneId())));
                    text = text + "\n" + MessageFormat.format(Messages.getString("AgendaEntryCell.ENTRY_TIME_RANGE_WITH_DATE"),
                            shortDateFormatter.format(entry.getStartAsZonedDateTime()), timeFormatter.format(entry.getStartAsZonedDateTime()), shortDateFormatter.format(entry.getEndAsZonedDateTime()),
                            timeFormatter.format(entry.getEndAsZonedDateTime())) + " " + entry.getZoneId().getDisplayName(TextStyle.SHORT, Locale.getDefault());

                }
            }

            return text;
        }

        /**
         * Sets the Week Formatter, the value by default is 'EEEE' Format.
         *
         * @param weekdayFormatter sets the week date time format.
         */
        public void setWeekdayFormatter(DateTimeFormatter weekdayFormatter) {
            this.weekdayFormatter = weekdayFormatter;
        }

        /**
         * Sets the Medium Date Formatter, the value by default is {@link FormatStyle#MEDIUM}. <br>
         * Is used to set a format text on the Date Label.
         *
         * @param mediumDateFormatter sets medium date time format.
         */
        public void setMediumDateFormatter(DateTimeFormatter mediumDateFormatter) {
            this.mediumDateFormatter = mediumDateFormatter;
        }

        /**
         * Sets the Short Date Formatter, the value by default is {@link FormatStyle#SHORT}. <br>
         * Is be used to set a Date format text in {@link #getTimeText(Entry)}
         *
         * @param shortDateFormatter sets the short date time format.
         */
        public void setShortDateFormatter(DateTimeFormatter shortDateFormatter) {
            this.shortDateFormatter = shortDateFormatter;
        }

        /**
         * Sets the Time Formatter, the value by default is {@link FormatStyle#SHORT}. <br>
         * Is used to set a Time format text in {@link #getTimeText(Entry)}
         *
         * @param timeFormatter sets the time format.
         */
        public void setTimeFormatter(DateTimeFormatter timeFormatter) {
            this.timeFormatter = timeFormatter;
        }
    }

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(lookAheadPeriodInDaysProperty());
            }

            @Override
            public void setValue(Object value) {
                setLookAheadPeriodInDays((int) value);
            }

            @Override
            public Object getValue() {
                return getLookAheadPeriodInDays();
            }

            @Override
            public Class<?> getType() {
                return Integer.class;
            }

            @Override
            public String getName() {
                return "Look Ahead Period";
            }

            @Override
            public String getDescription() {
                return "Look ahead period in days";
            }

            @Override
            public String getCategory() {
                return AGENDA_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(lookBackPeriodInDaysProperty());
            }

            @Override
            public void setValue(Object value) {
                setLookBackPeriodInDays((int) value);
            }

            @Override
            public Object getValue() {
                return getLookBackPeriodInDays();
            }

            @Override
            public Class<?> getType() {
                return Integer.class;
            }

            @Override
            public String getName() {
                return "Look Back Period";
            }

            @Override
            public String getDescription() {
                return "Look back period in days";
            }

            @Override
            public String getCategory() {
                return AGENDA_CATEGORY;
            }
        });

        items.add(new Item() {
            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showStatusLabelProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowStatusLabel((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowStatusLabel();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Status Label";
            }

            @Override
            public String getDescription() {
                return "Show Status Label";
            }

            @Override
            public String getCategory() {
                return AGENDA_CATEGORY;
            }
        });

        return items;
    }
}
