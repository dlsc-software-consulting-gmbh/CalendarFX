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
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.EntryViewBase.Position;
import com.calendarfx.view.Messages;
import com.calendarfx.view.MonthEntryView;
import com.calendarfx.view.MonthView;
import com.calendarfx.view.RequestEvent;
import impl.com.calendarfx.view.util.Util;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.util.Callback;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Double.MAX_VALUE;
import static javafx.scene.layout.Priority.ALWAYS;

public class MonthViewSkin extends DateControlSkin<MonthView> implements LoadDataSettingsProvider {

    private static final String DAY_OF_WEEK_LABEL = "day-of-week-label";

    private static final String DAY_OF_WEEKEND_LABEL = "day-of-weekend-label";

    private final GridPane gridPane;

    private final Map<LocalDate, MonthDayView> controlsMap = new HashMap<>();

    private final DataLoader dataLoader;

    private YearMonth displayedYearMonth;

    public MonthViewSkin(MonthView view) {
        super(view);

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setMinSize(0, 0);
        gridPane.setMaxSize(MAX_VALUE, MAX_VALUE);

        ColumnConstraints colCon = new ColumnConstraints();
        colCon.setPercentWidth(100d / 7d);

        gridPane.getColumnConstraints().add(colCon);
        gridPane.getColumnConstraints().add(colCon);
        gridPane.getColumnConstraints().add(colCon);
        gridPane.getColumnConstraints().add(colCon);
        gridPane.getColumnConstraints().add(colCon);
        gridPane.getColumnConstraints().add(colCon);
        gridPane.getColumnConstraints().add(colCon);

        RowConstraints rowHeaderCon = new RowConstraints();
        rowHeaderCon.setMinHeight(Region.USE_PREF_SIZE);
        gridPane.getRowConstraints().add(rowHeaderCon);

        for (int i = 0; i < 6; i++) {
            RowConstraints rowCon = new RowConstraints();
            gridPane.getRowConstraints().add(rowCon);
        }

        gridPane.getStyleClass().add("container");

        ChangeListener updateViewListener = (obs, oldV, newV) -> {
            System.out.println("property: " + obs.toString());
            updateView();
        };

        view.yearMonthProperty().addListener(it -> {
            if (displayedYearMonth == null || !(displayedYearMonth.equals(view.getYearMonth()))) {
                updateView();
            }
        });

        view.todayProperty().addListener(updateViewListener);
        view.showWeekNumbersProperty().addListener(updateViewListener);
        view.showWeekendsProperty().addListener(updateViewListener);
        view.showWeekdaysProperty().addListener(updateViewListener);
        view.showTodayProperty().addListener(updateViewListener);
        view.showCurrentWeekProperty().addListener(updateViewListener);
        view.weekFieldsProperty().addListener(updateViewListener);
        view.showTimedEntriesProperty().addListener(updateViewListener);
        view.showFullDayEntriesProperty().addListener(updateViewListener);
        view.enableHyperlinksProperty().addListener(updateViewListener);

        dataLoader = new DataLoader(this);

        updateView();

        view.getSelections().addListener((Observable it) -> updateSelection());

        getChildren().add(gridPane);

        view.getSelectedDates().addListener((Observable observable) -> updateDaySelection());

        view.getCalendars().addListener((Observable obs) -> updateEntries("list of calendars changed"));
        view.suspendUpdatesProperty().addListener(it -> updateEntries("suspend updates set to false"));
    }

    @Override
    protected void calendarVisibilityChanged() {
        updateEntries("calendar visibility changed");
    }

    @Override
    protected void refreshData() {
        updateView();
    }

    @Override
    protected void zoneIdChanged() {
        updateEntries("time zone changed");
    }

    @Override
    protected void calendarChanged(Calendar calendar) {
        updateEntries("changes in calendar " + calendar.getName());
    }

    @Override
    protected void entryCalendarChanged(CalendarEvent evt) {
        if (evt.isEntryAdded()) {
            updateEntries("entry added");
        } else if (evt.isEntryRemoved()) {
            updateEntries("entry removed");
        }
    }

    @Override
    protected void entryFullDayChanged(CalendarEvent evt) {
        // full day changes will be handled by the entry views
    }

    @Override
    protected void entryRecurrenceRuleChanged(CalendarEvent evt) {
        /*
         * We can hardly figure out whether the rule change has an impact
         * on the month view. Maybe the old rule did ... who knows.
         */
        updateEntries("recurrence rule changed");
    }

    @Override
    protected void entryIntervalChanged(CalendarEvent evt) {
        Entry<?> entry = evt.getEntry();

        if (isRelevant(evt.getOldInterval()) || isRelevant(entry)) {
            updateEntries("entry interval changed");
        }
    }

    private final PseudoClass selectedClass = PseudoClass.getPseudoClass("selected");

    private void updateDaySelection() {
        for (MonthDayView view : controlsMap.values()) {
            view.pseudoClassStateChanged(selectedClass, false);
        }

        for (LocalDate date : getSkinnable().getSelectedDates()) {
            MonthDayView view = controlsMap.get(date);
            if (view != null) {
                view.pseudoClassStateChanged(selectedClass, true);
            }
        }
    }

    private void updateSelection() {
        Set<Object> selectedKeys = getSkinnable().getSelections().stream().map(Entry::getId).collect(Collectors.toSet());

        for (MonthDayView view : controlsMap.values()) {
            MonthDayEntriesPane entriesPane = view.getEntriesPane();
            for (Node node : entriesPane.getChildren()) {
                if (node instanceof MonthEntryView) {
                    MonthEntryView entryView = (MonthEntryView) node;
                    Entry<?> entry = entryView.getEntry();
                    if (entry != null) {
                        Object entryId = entryView.getEntry().getId();
                        entryView.getProperties().put("selected",
                                selectedKeys.contains(entryId));
                    }
                }
            }
        }
    }

    private void updateView() {
        System.out.println("HERE!");
        controlsMap.clear();

        MonthView view = getSkinnable();
        gridPane.getChildren().clear();

        displayedYearMonth = view.getYearMonth();

        WeekFields weekFields = view.getWeekFields();
        DayOfWeek dayOfWeek = weekFields.getFirstDayOfWeek();

        if (view.isShowWeekdays()) {
            for (int i = 0; i < 7; i++) {
                // TODO: provide a factory for these labels
                Label dayOfWeekLabel = new Label(dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()));
                dayOfWeekLabel.setAlignment(Pos.CENTER_RIGHT);
                dayOfWeekLabel.setMaxSize(MAX_VALUE, MAX_VALUE);
                dayOfWeekLabel.getStyleClass().add(DAY_OF_WEEK_LABEL);

                if (view.isShowWeekends() && view.getWeekendDays().contains(dayOfWeek)) {
                    dayOfWeekLabel.getStyleClass().add(DAY_OF_WEEKEND_LABEL);
                }

                GridPane.setHgrow(dayOfWeekLabel, ALWAYS);
                gridPane.add(dayOfWeekLabel, i, 0);
                dayOfWeek = dayOfWeek.plus(1);
            }
        }


        LocalDate date = view.getDate().with(TemporalAdjusters.firstDayOfMonth());

        date = Util.adjustToFirstDayOfWeek(date, getSkinnable().getFirstDayOfWeek());

        final int firstWeek = 0;
        final int lastWeek = 5;
        final int firstDay = 0;
        final int lastDay = 6;

        for (int week = firstWeek; week <= lastWeek; week++) {
            for (int day = firstDay; day <= lastDay; day++) {
                // TODO: this should be done via a factory (cell factory already defined on MonthViewBase)
                MonthDayView dayOfMonthLabel = new MonthDayView(date, week, day);
                if (week == firstWeek) {
                    dayOfMonthLabel.getStyleClass().add("first-week");
                } else if (week == lastWeek) {
                    dayOfMonthLabel.getStyleClass().add("last-week");
                } else {
                    dayOfMonthLabel.getStyleClass().add("middle-week");
                }

                if (day == firstDay) {
                    dayOfMonthLabel.getStyleClass().add("first-day");
                } else if (day == lastDay) {
                    dayOfMonthLabel.getStyleClass().add("last-day");
                } else {
                    dayOfMonthLabel.getStyleClass().add("middle-day");
                }
                controlsMap.put(date, dayOfMonthLabel);
                GridPane.setHgrow(dayOfMonthLabel, ALWAYS);
                GridPane.setVgrow(dayOfMonthLabel, ALWAYS);
                gridPane.add(dayOfMonthLabel, day, week + 1);
                date = date.plusDays(1);
            }
        }

        updateDaySelection();
        updateEntries("view was updated after a view property change");
    }

    private final List<Map<Object, Integer>> positionMaps = new ArrayList<>();

    private int[][] numberOfFullDayEntries;

    private void updateEntries(String reason) {
        System.out.println("UPDATE ENTRIES");
        if (getSkinnable().isSuspendUpdates()) {
            return;
        }

        Map<LocalDate, List<Entry<?>>> dataMap = new HashMap<>();
        dataLoader.loadEntries(dataMap);

        positionMaps.clear();

        LocalDate date = getLoadStartDate();

        int numberOfWeeks = 6;
        int numberOfDays = 7;

        numberOfFullDayEntries = new int[numberOfWeeks][numberOfDays];

        for (int week = 0; week < numberOfWeeks; week++) {
            final int w = week;
            for (int day = 0; day < numberOfDays; day++) {

                final int d = day;

                List<Entry<?>> entries = dataMap.get(date);
                if (entries != null) {
                    entries.forEach(entry -> {
                        if (entry.isFullDay() || entry.isMultiDay()) {
                            numberOfFullDayEntries[w][d]++;
                        }
                    });
                }

                date = date.plusDays(1);
            }
        }

        // reset date
        date = getLoadStartDate();

        for (int week = 0; week < numberOfWeeks; week++) {

            final Map<Object, Integer> keyPositionMap = new HashMap<>();
            positionMaps.add(keyPositionMap);

            for (int day = 0; day < numberOfDays; day++) {
                List<Entry<?>> entries = dataMap.get(date);
                if (entries != null) {
                    entries.forEach(entry -> {
                        if (entry.isFullDay() || entry.isMultiDay()) {
                            Object entryId = entry.getId();
                            if (keyPositionMap.get(entryId) == null) {
                                int position = 0;
                                for (Entry<?> otherEntry : entries) {
                                    if (otherEntry.isFullDay() || otherEntry.isMultiDay()) {
                                        Object otherEntryId = otherEntry.getId();
                                        if (!otherEntryId.equals(entryId)) {
                                            if (keyPositionMap.get(otherEntryId) != null) {
                                                int otherPosition = keyPositionMap.get(otherEntryId);
                                                if (otherPosition == position) {
                                                    position = otherPosition + 1;
                                                }
                                            }
                                        }
                                    }
                                }
                                keyPositionMap.put(entryId, position);
                            }
                        }
                    });
                }

                date = date.plusDays(1);
            }
        }

        // clear all entries
        for (MonthDayView view : controlsMap.values()) {
            MonthDayEntriesPane entriesPane = view.getEntriesPane();
            entriesPane.getEntries().clear();
        }

        // add the new entries
        for (LocalDate localDate : dataMap.keySet()) {

            if (controlsMap.containsKey(localDate)) {
                MonthDayView view = controlsMap.get(localDate);
                MonthDayEntriesPane entriesPane = view.getEntriesPane();
                List<Entry<?>> entries = dataMap.get(localDate);
                if (entries != null) {

                    if (!getSkinnable().isShowFullDayEntries()) {
                        entries.removeIf(Entry::isFullDay);
                    }

                    if (!getSkinnable().isShowTimedEntries()) {
                        entries.removeIf(entry -> !entry.isFullDay());
                    }

                    Collections.sort(entries);
                    entriesPane.getEntries().setAll(entries);

                }
            }

        }



        LoggingDomain.VIEW.fine("updated entries in month view " + getSkinnable().getYearMonth() + ": reason = " + reason);
    }

    @Override
    public String getLoaderName() {
        return "Month View";
    }

    @Override
    public LocalDate getLoadStartDate() {
        /*
         * The month view also shows the last couple of days of the previous
         * month.
         */
        return Util.adjustToFirstDayOfWeek(getSkinnable().getDate().withDayOfMonth(1), getSkinnable().getFirstDayOfWeek());
    }

    @Override
    public LocalDate getLoadEndDate() {
        /*
         * The month view also shows the first couple of days of the next month.
         */
        return getLoadStartDate().plusDays(41); // the view always shows 41 month days
    }

    @Override
    public ZoneId getZoneId() {
        return getSkinnable().getZoneId();
    }

    @Override
    public List<CalendarSource> getCalendarSources() {
        return getSkinnable().getCalendarSources();
    }

    @Override
    public Control getControl() {
        return getSkinnable();
    }

    @Override
    public boolean isCalendarVisible(Calendar calendar) {
        return getSkinnable().isCalendarVisible(calendar);
    }

    class MonthDayView extends VBox {

        private static final String LAST_DAY_OF_WEEK = "last-day-of-week";
        private static final String FIRST_DAY_OF_WEEK = "first-day-of-week";

        private static final String MONTH_DAY = "day";
        private static final String MONTH_DAY_HEADER = "header";
        private static final String MONTH_DAY_ENTRIES_PANE = "entries-pane";
        private static final String MONTH_DAY_TODAY = "today";
        private static final String MONTH_DAY_CURRENT_WEEK = "current-week";

        private static final String WEEKEND_DAY = "weekend-day";
        private static final String DAY_NOT_OF_MONTH_LABEL = "day-not-of-month-label";
        private static final String DAY_OF_MONTH_LABEL = "day-of-month-label";
        private static final String TODAY_LABEL = "today-label";
        private static final String WEEK_OF_YEAR_LABEL = "week-of-year-label";
        private static final String CURRENT_WEEK_OF_YEAR_LABEL = "current-week-of-year-label";

        private final MonthDayEntriesPane entriesPane;

        private final LocalDate date;
        public final Polygon flag = new Polygon(0.0d, 0.0d,
                25.0d, 0.0d,
                0.0d, 25.0d);

        MonthDayView(LocalDate date, int week, int day) {
            this.date = date;

            getStyleClass().add(MONTH_DAY);

            setFillWidth(true);

            if (day == 0) {
                getStyleClass().add(FIRST_DAY_OF_WEEK);
            } else if (day == 6) {
                getStyleClass().add(LAST_DAY_OF_WEEK);
            }

            Label dateLabel = new Label();
            dateLabel.setMaxWidth(MAX_VALUE);

            if (getSkinnable().isEnableHyperlinks()) {
                dateLabel.setOnMouseClicked(evt -> {
                    if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 1) {
                        fireEvent(new RequestEvent(getSkinnable(), getSkinnable(), date));
                    }
                });
                dateLabel.getStyleClass().add("date-hyperlink");
            }

            WeekFields weekFields = getSkinnable().getWeekFields();

            LocalDate firstDay = Util.adjustToFirstDayOfWeek(date, getSkinnable().getFirstDayOfWeek());

            final int weekOfYear = firstDay.get(weekFields.weekOfYear());
            final Year year = Year.of(Util.adjustToFirstDayOfWeek(date, getSkinnable().getFirstDayOfWeek()).getYear());

            Label weekLabel = new Label(Integer.toString(weekOfYear));
            weekLabel.setVisible(firstDay.equals(date));

            if (getSkinnable().isEnableHyperlinks()) {
                weekLabel.getStyleClass().add("date-hyperlink");
                weekLabel.setOnMouseClicked(evt -> {
                    if (evt.getClickCount() == 1) {
                        getSkinnable().fireEvent(new RequestEvent(getSkinnable(), getSkinnable(), year, weekOfYear));
                    }
                });
            }

            MonthView monthView = getSkinnable();

            if (getSkinnable().isShowCurrentWeek() && date.getYear() == monthView.getToday().getYear()
                    && weekOfYear == monthView.getToday().get(weekFields.weekOfYear())) {
                dateLabel.setText(DateTimeFormatter.ofPattern(Messages.getString("MonthViewSkin.TODAY_DATE_FORMAT")).format(date));
                weekLabel.getStyleClass().add(CURRENT_WEEK_OF_YEAR_LABEL);
                getStyleClass().add(MONTH_DAY_CURRENT_WEEK);
            } else {
                dateLabel.setText(Integer.toString(date.getDayOfMonth()));
                weekLabel.getStyleClass().add(WEEK_OF_YEAR_LABEL);
            }

            if (YearMonth.from(date).equals(monthView.getYearMonth())) {
                dateLabel.getStyleClass().add(DAY_OF_MONTH_LABEL);
                if (date.equals(monthView.getToday())) {
                    if (monthView.isShowToday()) {
                        dateLabel.getStyleClass().add(TODAY_LABEL);
                        getStyleClass().add(MONTH_DAY_TODAY);
                    }
                }
            } else {
                dateLabel.getStyleClass().add(DAY_NOT_OF_MONTH_LABEL);
            }

            DayOfWeek dayOfWeek = date.getDayOfWeek();

            if (monthView.isShowWeekends() && monthView.getWeekendDays().contains(dayOfWeek)) {
                getStyleClass().add(WEEKEND_DAY);
                dateLabel.getStyleClass().add(WEEKEND_DAY);
            }
            getStyleClass().add(dayOfWeek.toString().toLowerCase());
            BorderPane flagPane = new BorderPane();

            flag.setFill(Color.LIGHTGREY);
            flagPane.setLeft(flag);
            VBox.setVgrow(flagPane, Priority.NEVER);
            getChildren().add(flagPane);
            BorderPane headerPane = new BorderPane();
            headerPane.getStyleClass().add(MONTH_DAY_HEADER);
            if (monthView.isShowWeekNumbers()) {
                headerPane.setLeft(weekLabel);
            }
            headerPane.setRight(dateLabel);


            VBox.setVgrow(headerPane, Priority.NEVER);
            getChildren().add(headerPane);
            entriesPane = new MonthDayEntriesPane(date, week, day);
            entriesPane.getStyleClass().add(MONTH_DAY_ENTRIES_PANE);
            VBox.setVgrow(entriesPane, Priority.ALWAYS);
            getChildren().add(entriesPane);

            setMaxSize(MAX_VALUE, MAX_VALUE);
            setPrefSize(50, 50);

            addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                if (evt.getClickCount() == 1 && evt.getButton() == MouseButton.PRIMARY ) {
                    // ENTRY selection
                    monthView.getSelections().clear();

                    // DATE selection
                    boolean wasSelected = monthView.getSelectedDates().contains(date);

                    if (!evt.isShiftDown() && !evt.isControlDown()) {
                        monthView.getSelectedDates().clear();
                    }

                    if (!(wasSelected || getSkinnable().getSelectedDates().contains(date))) {
                        monthView.getSelectedDates().add(date);
                    }
                }
            });
        }

        final MonthDayEntriesPane getEntriesPane() {
            return entriesPane;
        }

        final LocalDate getDate() {
            return date;
        }
    }

    class MonthDayEntriesPane extends Pane {

        private static final String MONTH_DAY_MORE_LABEL = "more-label";
        private static final String SPACE = " ";

        private final Label moreLabel;
        private final LocalDate date;
        private final int week;
        private final int day;

        public Polygon entryFlag;

        private VBox outerBox;

        private BorderPane flagPane;

        MonthDayEntriesPane(LocalDate date, int week, int day) {



            getStyleClass().add("entries-pane");

            this.date = date;
            this.week = week;
            this.day = day;

            // since JavaFX 19 this needs to be run later
            entries.addListener((Observable evt) -> Platform.runLater(() -> update()));

            setMinSize(0, 0);
            setPrefSize(0, 0);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(widthProperty());
            clip.heightProperty().bind(heightProperty());
            setClip(clip);

            moreLabel = new Label();
            moreLabel.getStyleClass().add(MONTH_DAY_MORE_LABEL);
            moreLabel.setManaged(false);
            moreLabel.setVisible(false);

            if (getSkinnable().isEnableHyperlinks()) {
                moreLabel.getStyleClass().add("date-hyperlink");
                moreLabel.setOnMouseClicked(evt -> fireEvent(new RequestEvent(this, this, date)));
            }

            /*
            getChildren().add(moreLabel);
            VBox outerBox = (VBox)getParent();
            BorderPane flagPane = (BorderPane) outerBox.getChildren().get(0);
            Polygon flag = (Polygon)(flagPane.getChildren().get(0));
             */

            System.out.println(getParent());
        }


        private final ObservableList<Entry<?>> entries = FXCollections.observableArrayList();

        public final ObservableList<Entry<?>> getEntries() {
            return entries;
        }

        public void updateFlag(){
            VBox parentVBox = (VBox)getParent();
            System.out.println(parentVBox.getChildren().get(0));
            BorderPane flagPane = (BorderPane) parentVBox.getChildren().get(0);
            System.out.println(flagPane.getChildren().get(0));
            Polygon flag = (Polygon)(flagPane.getChildren().get(0));

            int posCount = 0;
            int milCount = 0;
            int negCount = 0;
            int trgCount = 0;

            if(entries.isEmpty()){
                flag.setFill(Color.LIGHTGREY);
            }

            else {
                for (Entry<?> entry : entries) {
                    switch (entry.getCalendar().getName()) {
                        case "Positive":
                            posCount++;
                            break;
                        case "Mild Negative":
                            milCount++;
                            break;
                        case "Negative":
                            negCount++;
                            break;
                        case "Trigger Event":
                            trgCount++;
                            break;
                        default:
                            break;
                    }
                }

                if ((posCount > milCount) && (posCount > negCount) && (posCount > trgCount)) {
                    flag.setFill(Color.LIGHTGREEN);
                } else if ((milCount > posCount) && (milCount > negCount) && (milCount > trgCount)) {
                    flag.setFill(Color.BLUE);
                } else if ((negCount > milCount) && (negCount > posCount) && (negCount > trgCount)) {
                    flag.setFill(Color.YELLOW);
                } else if ((trgCount > milCount) && (trgCount > posCount) && (trgCount > negCount)) {
                    flag.setFill(Color.PURPLE);
                }
            }
        }

        public void update() {




            Util.removeChildren(this, node -> node instanceof MonthEntryView);

            /////


            ///////

            if (!entries.isEmpty()) {

                List<Entry<?>> otherEntries = new ArrayList<>();

                Map<Object, Integer> positionMap = Objects.requireNonNull(positionMaps.get(week), "missing position map for week number " + week);

                int maxPosition = -1;

                for (Entry<?> entry : entries) {
                    Objects.requireNonNull(entry, "found NULL calendar entry in entry list");
                    if (entry.isFullDay() || entry.isMultiDay()) {
                        int position = positionMap.get(Objects.requireNonNull(entry.getId(), "entry ID is missing, entry type = " + entry.getClass().getName() + ", entry title = " + entry.getTitle()));
                        maxPosition = Math.max(maxPosition, position);
                    } else {
                        otherEntries.add(entry);
                    }
                }

                if (maxPosition > -1) {
                    Node[] fullDayNodes = new Node[maxPosition + 1];
                    for (int i = 0; i < maxPosition; i++) {
                        /*
                         * Do not use factory for this. SPACE is important to guarantee that the blank
                         * entries have the same height as the regular entries.
                         */
                        MonthEntryView label = new MonthEntryView(new Entry<>(SPACE));
                        label.setVisible(false);
                        label.getProperties().put("control", getSkinnable());
                        fullDayNodes[i] = label;
                    }

                    for (Entry<?> entry : entries) {
                        if (entry.isFullDay() || entry.isMultiDay()) {
                            int position = positionMap.get(entry.getId());
                            fullDayNodes[position] = createNode(entry);
                        }
                    }

                    for (Node node : fullDayNodes) {
                        getChildren().add(node);
                    }
                }

                /*
                 * Individual calendars are already sorted, but now we are
                 * displaying entries from several calendars, so let's resort.
                 */


                Collections.sort(otherEntries);


                for (Entry<?> entry : otherEntries) {
                    getChildren().add(createNode(entry));
                }
                //flag.setFill(Color.GREEN);

            }
            updateFlag();
        }

        private Node createNode(Entry<?> entry) {
            Callback<Entry<?>, MonthEntryView> factory = getSkinnable().getEntryViewFactory();
            MonthEntryView view = factory.call(entry);
            view.getProperties().put("control", getSkinnable());
            view.getProperties().put("startDate", date);
            view.getProperties().put("endDate", date);

            Position position = Position.ONLY;


            if (entry.isFullDay()) {
                if (date.isBefore(entry.getEndDate()) && (day == 0 || (date.equals(entry.getStartDate())))) {
                    position = Position.FIRST;
                } else if (entry.isMultiDay() && date.equals(entry.getEndDate())) {
                    position = Position.LAST;
                } else if (entry.isMultiDay()) {
                    position = Position.MIDDLE;
                }
            } else if (entry.isMultiDay()) {
                if (day == 0 || (date.equals(entry.getStartDate()) && date.isBefore(entry.getEndDate()))) {
                    position = Position.FIRST;
                } else if (date.equals(entry.getEndDate())) {
                    position = Position.LAST;
                } else {
                    position = Position.MIDDLE;
                }
            }


            view.getProperties().put("position", position);

            return view;
        }

        @Override
        protected void layoutChildren() {
            Insets insets = getInsets();
            //double w = getWidth();

            double h = getHeight();
            double y = insets.getTop();
            double w = 35;

            moreLabel.setVisible(false);

            List<Node> children = getChildren();

            boolean conflictFound = false;
            int childrenAdded = 0;

            for (int i = 0; i < children.size(); i++) {
                Node child = children.get(i);
                if (child == moreLabel) {
                    continue;
                }
                double ph = child.prefHeight(-1);
                if (y + ph < h - insets.getTop() - insets.getBottom()) {
                    child.resizeRelocate(
                            snapPositionX(insets.getLeft()),
                            snapPositionY(y),
                            snapSizeX(w - insets.getRight() - insets.getLeft()),
                            snapSizeY(ph));

                    y += ph + 1; // +1 = gap
                    child.getProperties().put("hidden", false);
                    childrenAdded++;
                } else {
                    if (!conflictFound && i > 0) {
                        conflictFound = true;
                        children.get(i - 1).getProperties().put("hidden", true);
                    }

                    child.getProperties().put("hidden", true);
                }
            }

            if (conflictFound) {
                moreLabel.setVisible(true);
                moreLabel.setText(MessageFormat.format(Messages.getString("MonthViewSkin.MORE_ENTRIES"), (childrenAdded == 0) ? children.size()-1 : children.size() - childrenAdded));
                double ph = moreLabel.prefHeight(-1);

                moreLabel.resizeRelocate(
                        snapPositionX(insets.getLeft()),
                        snapPositionY(h - insets.getTop() - insets.getBottom() - ph),
                        snapSizeX(w - insets.getRight() - insets.getLeft()),
                        snapSizeY(ph));
            }
        }           
    }

    public ZonedDateTime getZonedDateTimeAt(double x, double y, ZoneId zoneId) {
        Point2D location = getSkinnable().localToScreen(x, y);
        for (MonthDayView view : controlsMap.values()) {
            if (view.localToScreen(view.getLayoutBounds()).contains(location)) {
                return ZonedDateTime.of(view.getDate(), LocalTime.NOON, zoneId);
            }
        }

        return null;
    }
}

