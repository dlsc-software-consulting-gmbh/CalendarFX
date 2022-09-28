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
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DateControl.DateDetailsParameter;
import com.calendarfx.view.Messages;
import com.calendarfx.view.RequestEvent;
import com.calendarfx.view.YearMonthView;
import impl.com.calendarfx.view.util.Util;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.util.Callback;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.Double.MAX_VALUE;
import static java.time.format.TextStyle.SHORT;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.control.SelectionMode.SINGLE;
import static javafx.scene.layout.Priority.ALWAYS;

public class YearMonthViewSkin extends DateControlSkin<YearMonthView> implements LoadDataSettingsProvider {

    private static final String DAY_OF_WEEK_LABEL = "day-of-week-label";
    private static final String CURRENT_DATE_LABEL = "current-date-label";
    private static final String CURRENT_DATE_BORDER = "current-date-border";
    private static final String USAGE_VERY_LOW = "usage-very-low";
    private static final String USAGE_LOW = "usage-low";
    private static final String USAGE_MEDIUM = "usage-medium";
    private static final String USAGE_HIGH = "usage-high";
    private static final String USAGE_VERY_HIGH = "usage-very-high";
    private static final String TODAY = "today";
    private static final String DAY_OF_MONTH_LABEL = "day-of-month-label";
    private static final String DAY_NOT_OF_MONTH_LABEL = "day-not-of-month-label";
    private static final String WEEKEND_DAY = "weekend-day";
    private static final String SELECTED_MONTH_DATE = "selected-month-date";

    private final GridPane gridPane;

    private final Label monthLabel;

    private final Label yearLabel;

    private final Map<String, YearMonthView.DateCell> cellsMap = new HashMap<>();

    private final Label[] dayOfWeekLabels = new Label[7];

    private final Label[] weekNumberLabels = new Label[6];

    private final DataLoader dataLoader;

    private YearMonth displayedYearMonth;

    public YearMonthViewSkin(YearMonthView view) {
        super(view);

        dataLoader = new DataLoader(this);

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setMaxSize(MAX_VALUE, MAX_VALUE);
        gridPane.getStyleClass().add("container");

        monthLabel = new Label();
        monthLabel.getStyleClass().add("month-label");
        monthLabel.visibleProperty().bind(view.showMonthProperty());

        yearLabel = new Label();
        yearLabel.getStyleClass().add("year-label");
        yearLabel.visibleProperty().bind(view.showYearProperty());

        final InvalidationListener updateViewListener = evt -> updateView();
        view.yearMonthProperty().addListener(evt -> {
            if (displayedYearMonth == null || !displayedYearMonth.equals(view.getYearMonth())) {
                updateView();
            }
        });

        final InvalidationListener buildViewListener = evt -> buildView();

        view.showTodayProperty().addListener(updateViewListener);
        view.getSelectedDates().addListener(updateViewListener);

        view.showUsageColorsProperty().addListener(it -> updateUsageColors("show usage colors flag changed"));

        view.showWeekNumbersProperty().addListener(buildViewListener);
        view.showMonthArrowsProperty().addListener(buildViewListener);
        view.showMonthProperty().addListener(buildViewListener);
        view.showYearProperty().addListener(buildViewListener);
        view.cellFactoryProperty().addListener(buildViewListener);
        view.weekFieldsProperty().addListener(buildViewListener);
        view.showTodayButtonProperty().addListener(buildViewListener);
        view.showYearArrowsProperty().addListener(buildViewListener);

        view.getCalendars().addListener((Observable it) -> updateUsageColors("list of calendars changed"));

        view.dateProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.getYear() != newValue.getYear()) {
                updateUsageColors("Year has changed.");
            }
        });

        view.suspendUpdatesProperty().addListener(it -> {
            if (!view.isSuspendUpdates()) {
                updateUsageColors("suspend update set to false");
            }
        });

        getChildren().add(gridPane);

        updateHyperlinkSupport();
        view.enableHyperlinksProperty().addListener(it -> updateHyperlinkSupport());

        buildView();

        updateVisibility();

        updateUsageColors("initial creation of usage colors");
    }

    private void updateHyperlinkSupport() {
        final YearMonthView view = getSkinnable();

        if (view.isEnableHyperlinks()) {
            monthLabel.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 1) {
                    view.fireEvent(new RequestEvent(view, view, getSkinnable().getYearMonth()));
                }
            });

            yearLabel.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 1) {
                    view.fireEvent(new RequestEvent(view, view, Year.of(getSkinnable().getYearMonth().getYear())));
                }
            });
        } else {
            monthLabel.setOnMouseClicked(null);
            yearLabel.setOnMouseClicked(null);
        }
    }

    private void updateVisibility() {
        for (int row = 0; row < 6; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setFillHeight(true);
            rowConstraints.setMinHeight(Region.USE_PREF_SIZE);
            rowConstraints.setMaxHeight(Region.USE_COMPUTED_SIZE);
            rowConstraints.setPrefHeight(Region.USE_COMPUTED_SIZE);
            rowConstraints.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(rowConstraints);
        }

        ColumnConstraints weekColumn = new ColumnConstraints();
        weekColumn.setHalignment(HPos.RIGHT);
        weekColumn.setMaxWidth(Region.USE_COMPUTED_SIZE);
        weekColumn.setMinWidth(Region.USE_PREF_SIZE);
        weekColumn.setPrefWidth(Region.USE_COMPUTED_SIZE);
        weekColumn.setFillWidth(true);
        weekColumn.setHgrow(Priority.NEVER);
        gridPane.getColumnConstraints().add(weekColumn);

        for (int col = 0; col < 7; col++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHalignment(HPos.CENTER);
            columnConstraints.setMaxWidth(Region.USE_COMPUTED_SIZE);
            columnConstraints.setMinWidth(Region.USE_PREF_SIZE);
            columnConstraints.setPrefWidth(Region.USE_COMPUTED_SIZE);
            columnConstraints.setFillWidth(true);
            columnConstraints.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
    }

    @Override
    protected void refreshData() {
        updateView();
    }

    @Override
    protected void calendarVisibilityChanged() {
        updateUsageColors("calendar visibility changed");
    }

    @Override
    protected void calendarChanged(Calendar calendar) {
        updateUsageColors("changes in calendar " + calendar.getName());
    }

    @Override
    protected void entryCalendarChanged(CalendarEvent evt) {
        Entry<?> entry = evt.getEntry();

        /*
         * If an entry has been deleted (new calendar == null) then we have no
         * way of finding out
         */
        if (isRelevant(entry)) {
            updateUsageColors("entry calendar changed");
        }
    }

    @Override
    protected void entryIntervalChanged(CalendarEvent evt) {
        if (evt.isDayChange()) {
            Entry<?> entry = evt.getEntry();

            if (isRelevant(entry) || isRelevant(evt.getOldInterval())) {
                updateUsageColors("entry interval changed");
            }
        }
    }

    @Override
    protected void entryFullDayChanged(CalendarEvent evt) {
    }

    @Override
    protected void entryRecurrenceRuleChanged(CalendarEvent evt) {
        Entry<?> entry = evt.getEntry();

        if (isRelevant(entry)) {
            updateUsageColors("entry recurrence rule changed");
        }
    }

    private LocalDate lastSelectedDate;

    private void buildView() {
        gridPane.getChildren().clear();

        YearMonthView view = getSkinnable();

        BorderPane header = new BorderPane();
        header.getStyleClass().add("header");

        BorderPane leftHeader = new BorderPane();
        leftHeader.getStyleClass().add("month-header");

        if (getSkinnable().isShowMonth()) {
            if (getSkinnable().isShowMonthArrows()) {
                // left: previous year button
                Region prevMonthRegion = new Region();
                BorderPane.setAlignment(prevMonthRegion, Pos.CENTER);
                BorderPane.setMargin(prevMonthRegion, new Insets(0, 6, 0, 6));
                prevMonthRegion.getStyleClass().add("previous-button");
                leftHeader.setLeft(prevMonthRegion);
                prevMonthRegion.setOnMouseClicked(evt -> getSkinnable().setDate(getSkinnable().getDate().minusMonths(1)));
            }

            // center: year label
            leftHeader.setCenter(monthLabel);

            if (getSkinnable().isShowMonthArrows()) {
                // left: next year button
                Region nextMonthRegion = new Region();
                BorderPane.setAlignment(nextMonthRegion, Pos.CENTER);
                BorderPane.setMargin(nextMonthRegion, new Insets(0, 6, 0, 6));
                nextMonthRegion.getStyleClass().add("next-button");
                leftHeader.setRight(nextMonthRegion);
                nextMonthRegion.setOnMouseClicked(evt -> getSkinnable().setDate(getSkinnable().getDate().plusMonths(1)));
            }

            header.setLeft(leftHeader);
        }

        if (getSkinnable().isShowYear()) {
            BorderPane rightHeader = new BorderPane();
            rightHeader.getStyleClass().add("year-header");

            if (getSkinnable().isShowYearArrows()) {
                // left: previous year button
                Region prevYearRegion = new Region();
                BorderPane.setAlignment(prevYearRegion, Pos.CENTER);
                BorderPane.setMargin(prevYearRegion, new Insets(0, 6, 0, 6));
                prevYearRegion.getStyleClass().add("previous-button");
                rightHeader.setLeft(prevYearRegion);
                prevYearRegion.setOnMouseClicked(evt -> getSkinnable().setDate(getSkinnable().getDate().minusYears(1)));
            }

            // center: year label
            rightHeader.setCenter(yearLabel);

            if (getSkinnable().isShowYearArrows()) {
                // left: next year button
                Region nextYearRegion = new Region();
                BorderPane.setAlignment(nextYearRegion, Pos.CENTER);
                BorderPane.setMargin(nextYearRegion, new Insets(0, 6, 0, 6));
                nextYearRegion.getStyleClass().add("next-button");
                rightHeader.setRight(nextYearRegion);
                nextYearRegion.setOnMouseClicked(evt -> getSkinnable().setDate(getSkinnable().getDate().plusYears(1)));
            }

            header.setRight(rightHeader);
        }

        GridPane.setColumnSpan(header, 7);

        gridPane.add(header, 1, 0);

        DayOfWeek dayOfWeek = view.getFirstDayOfWeek();
        for (int i = 0; i < 7; i++) {
            dayOfWeekLabels[i] = new Label(dayOfWeek.getDisplayName(SHORT, Locale.getDefault()));
            dayOfWeekLabels[i].setAlignment(CENTER);
            dayOfWeekLabels[i].setMaxSize(MAX_VALUE, MAX_VALUE);
            dayOfWeekLabels[i].getStyleClass().add(DAY_OF_WEEK_LABEL);
            gridPane.add(dayOfWeekLabels[i], i + 1, 1);
            dayOfWeek = dayOfWeek.plus(1);
        }

        final DayOfWeek firstDayOfWeek = getSkinnable().getFirstDayOfWeek();

        LocalDate date = getLoadStartDate();
        date = Util.adjustToFirstDayOfWeek(date, firstDayOfWeek);

        if (getSkinnable().isShowWeekNumbers()) {
            for (int i = 0; i < 6; i++) {
                weekNumberLabels[i] = new Label();
                weekNumberLabels[i].setMaxSize(MAX_VALUE, MAX_VALUE);
                weekNumberLabels[i].setAlignment(Pos.CENTER_RIGHT);
                weekNumberLabels[i].getStyleClass().add("week-label");
                gridPane.add(weekNumberLabels[i], 0, 2 + i);
                date = date.plusWeeks(1);
            }
        }

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                Callback<YearMonthView, YearMonthView.DateCell> cellFactory = view.getCellFactory();
                YearMonthView.DateCell cell = cellFactory.call(getSkinnable());
                GridPane.setHgrow(cell, ALWAYS);
                GridPane.setVgrow(cell, ALWAYS);
                cell.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> handleMouseClick(evt, cell, cell.getDate()));
                cell.getStyleClass().add("month-day");
                cellsMap.put(getKey(row, col), cell);
                gridPane.add(cell, col + 1, row + 2);
                date = date.plusDays(1);
            }
        }

        if (getSkinnable().isShowTodayButton()) {
            Button button = new Button(Messages.getString("YearMonthViewSkin.TODAY"));
            gridPane.add(button, 0, 9);
            GridPane.setColumnSpan(button, 8);
            GridPane.setHalignment(button, HPos.CENTER);
            GridPane.setMargin(button, new Insets(6, 0, 0, 0));
            button.setOnAction(evt -> getSkinnable().setDate(getSkinnable().getToday()));
        }

        // after a build we always have to update the view
        updateView();
    }

    private String getKey(int row, int col) {
        return row + "/" + col;
    }

    private void updateView() {
        lastSelectedDate = null;

        YearMonthView view = getSkinnable();
        YearMonth yearMonth = view.getYearMonth();

        displayedYearMonth = yearMonth;

        boolean currentYearMonth = getSkinnable().getYearMonth().equals(YearMonth.from(getSkinnable().getToday()));

        monthLabel.getStyleClass().remove(CURRENT_DATE_LABEL);
        yearLabel.getStyleClass().remove(CURRENT_DATE_LABEL);

        if (currentYearMonth && view.isShowToday()) {
            monthLabel.getStyleClass().add(CURRENT_DATE_LABEL);
            yearLabel.getStyleClass().add(CURRENT_DATE_LABEL);
        }

        monthLabel.setText(DateTimeFormatter.ofPattern(Messages.getString("YearMonthViewSkin.MONTH_FORMAT")).format(yearMonth));
        yearLabel.setText(DateTimeFormatter.ofPattern(Messages.getString("YearMonthViewSkin.YEAR_FORMAT")).format(yearMonth));

        // update the week days (mon, tues, wed, ....)

        DayOfWeek dayOfWeek = view.getFirstDayOfWeek();
        for (int i = 0; i < 7; i++) {
            if (view.isShowToday()) {
                if (currentYearMonth) {
                    dayOfWeekLabels[i].getStyleClass().add(CURRENT_DATE_BORDER);
                } else {
                    dayOfWeekLabels[i].getStyleClass().removeAll(CURRENT_DATE_BORDER);
                }

                if (currentYearMonth && view.getToday().getDayOfWeek().equals(dayOfWeek)) {
                    dayOfWeekLabels[i].getStyleClass().add(CURRENT_DATE_LABEL);
                }
            }
        }

        // update the days (1 to 31) plus padding days

        final DayOfWeek firstDayOfWeek = getSkinnable().getFirstDayOfWeek();
        LocalDate date = Util.adjustToFirstDayOfWeek(getLoadStartDate(), firstDayOfWeek);

        if (getSkinnable().isShowWeekNumbers()) {
            for (int i = 0; i < 6; i++) {
                int weekOfYear = date.get(getSkinnable().getWeekFields().weekOfYear());
                weekNumberLabels[i].setText(Integer.toString(weekOfYear));
                date = date.plusWeeks(1);
            }
        }

        date = Util.adjustToFirstDayOfWeek(getLoadStartDate(), firstDayOfWeek);

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                LocalDate localDate = LocalDate.from(date);

                YearMonthView.DateCell cell = cellsMap.get(getKey(row, col));
                cell.setDate(localDate);
                cell.getStyleClass().removeAll(TODAY, DAY_OF_MONTH_LABEL, DAY_NOT_OF_MONTH_LABEL, WEEKEND_DAY, SELECTED_MONTH_DATE);

                if (getSkinnable().getSelectedDates().contains(date)) {
                    cell.getStyleClass().add(SELECTED_MONTH_DATE); // $NON-NLS-1$
                }

                if (YearMonth.from(date).equals(YearMonth.from(getSkinnable().getDate()))) {
                    if (getSkinnable().isShowToday() && date.equals(getSkinnable().getToday())) {
                        cell.getStyleClass().addAll(DAY_OF_MONTH_LABEL, TODAY);
                    } else {
                        cell.getStyleClass().add(DAY_OF_MONTH_LABEL); // $NON-NLS-1$
                    }
                } else {
                    cell.getStyleClass().add(DAY_NOT_OF_MONTH_LABEL); // $NON-NLS-1$
                }

                if (view.getWeekendDays().contains(date.getDayOfWeek())) {
                    cell.getStyleClass().add(WEEKEND_DAY); // $NON-NLS-1$
                }

                date = date.plusDays(1);
            }
        }
    }

    private void handleMouseClick(MouseEvent evt, Node node, LocalDate date) {
        switch (evt.getClickCount()) {
        case 1:
            handleSingleClick(evt, node, date);
            break;
        case 2:
            if (getSkinnable().isEnableHyperlinks()) {
                handleDoubleClick(date);
            }
            break;
        default:
            break;
        }
    }

    private void handleSingleClick(MouseEvent evt, Node node, LocalDate date) {
        if (!(evt.getButton() == MouseButton.PRIMARY)) {
            return;
        }

        YearMonthView view = getSkinnable();
        switch (view.getClickBehaviour()) {
        case NONE:
            break;
        case SHOW_DETAILS:
            Callback<DateDetailsParameter, Boolean> callback = view.getDateDetailsCallback();
            if (callback != null) {
                callback.call(new DateDetailsParameter(evt, view, node, node.getScene().getRoot(), date, evt.getScreenX(), evt.getScreenY()));
            }
            break;
        case PERFORM_SELECTION:
            boolean multiSelect = evt.isShiftDown() || evt.isShortcutDown();
            if (!multiSelect || (view.getSelectionMode().equals(SINGLE)
                    && !evt.isControlDown())) {
                view.getSelectedDates().clear();
            }

            if (evt.isShiftDown()) {
                if (lastSelectedDate != null) {
                    LocalDate st = lastSelectedDate;
                    LocalDate et = date;
                    if (date.isBefore(st)) {
                        st = date;
                        et = lastSelectedDate;
                    }

                    do {
                        view.getSelectedDates().add(st);
                        st = st.plusDays(1);
                    } while (!et.isBefore(st));
                } else {
                    view.getSelectedDates().clear();
                    view.getSelectedDates().add(date);
                }
            } else {
                if (view.getSelectedDates().contains(date)) {
                    view.getSelectedDates().remove(date);
                } else {
                    view.getSelectedDates().add(date);
                }
            }

            lastSelectedDate = date;

            if (!date.getMonth().equals(view.getYearMonth().getMonth())) {
                view.setDate(date);
            }

            break;
        default:
            break;
        }
    }

    private void handleDoubleClick(LocalDate date) {
        YearMonthView view = getSkinnable();
        view.fireEvent(new RequestEvent(view, view, date));
    }

    private void updateUsageColors(String reason) {
        cellsMap.values()
                .forEach(control -> control.getStyleClass().removeAll(
                        USAGE_VERY_LOW, USAGE_LOW, USAGE_MEDIUM, USAGE_HIGH,
                        USAGE_VERY_HIGH));

        if (!getSkinnable().isShowUsageColors()) {
            return;
        }

        LoggingDomain.VIEW.fine("updating colors: reason = " + reason
                + ", year month = " + getSkinnable().getYearMonth());

        Map<LocalDate, List<Entry<?>>> dataMap = new HashMap<>();
        dataLoader.loadEntries(dataMap);

        for (String key : cellsMap.keySet()) {
            YearMonthView.DateCell cell = cellsMap.get(key);
            LocalDate date = cell.getDate();

            if (date.isEqual(getSkinnable().getToday())
                    && getSkinnable().isShowToday()) {
                continue;
            }

            int entryCount = 0;
            List<Entry<?>> entries = dataMap.get(date);
            if (entries != null) {
                entryCount = entries.size();
            }

            final Callback<Integer, DateControl.Usage> usagePolicy = getSkinnable()
                    .getUsagePolicy();

            switch (usagePolicy.call(entryCount)) {
            case NONE:
                break;
            case VERY_LOW:
                cell.getStyleClass().add(USAGE_VERY_LOW);
                break;
            case LOW:
                cell.getStyleClass().add(USAGE_LOW);
                break;
            case MEDIUM:
                cell.getStyleClass().add(USAGE_MEDIUM);
                break;
            case HIGH:
                cell.getStyleClass().add(USAGE_HIGH);
                break;
            case VERY_HIGH:
            default:
                cell.getStyleClass().add(USAGE_VERY_HIGH);
                break;
            }
        }
    }

    @Override
    public String getLoaderName() {
        return "Year Month View";
    }

    @Override
    public LocalDate getLoadStartDate() {
        /*
         * The month view also shows the last couple of days of the previous
         * month.
         */
        return Util.adjustToFirstDayOfWeek(
                getSkinnable().getDate().withDayOfMonth(1),
                getSkinnable().getFirstDayOfWeek());
    }

    @Override
    public LocalDate getLoadEndDate() {
        /*
         * The month view also shows the first couple of days of the next month.
         */
        return getLoadStartDate().plusDays(41); // the view always shows 41
                                                // month days
    }

    @Override
    public ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    @Override
    public List<CalendarSource> getCalendarSources() {
        YearMonthView view = getSkinnable();
        return view.getCalendarSources();
    }

    @Override
    public Control getControl() {
        return getSkinnable();
    }

    @Override
    public boolean isCalendarVisible(Calendar calendar) {
        return getSkinnable().isCalendarVisible(calendar);
    }
}
