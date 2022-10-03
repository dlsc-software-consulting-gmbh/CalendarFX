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
import com.calendarfx.view.DateControl.DateDetailsParameter;
import com.calendarfx.view.DateSelectionModel;
import com.calendarfx.view.MonthSheetView;
import com.calendarfx.view.MonthSheetView.DateCell;
import com.calendarfx.view.MonthSheetView.WeekDayLayoutStrategy;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.util.Callback;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MonthSheetViewSkin extends DateControlSkin<MonthSheetView> implements LoadDataSettingsProvider {

    private final GridPane grid = new GridPane();
    private final DataLoader dataLoader = new DataLoader(this);
    private final Map<LocalDate, List<Entry<?>>> dataMap = new HashMap<>();
    private final Map<LocalDate, DateCell> cellMap = new HashMap<>();
    private final Map<Position, DateCell> positionToDateCellMap = new HashMap<>();
    private final Map<LocalDate, Position> dateToPositionMap = new HashMap<>();

    public MonthSheetViewSkin(MonthSheetView control) {
        super(control);

        grid.getStyleClass().add("container");
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        updateRowConstraints();
        control.weekDayLayoutProperty().addListener(it -> updateRowConstraints());

        InvalidationListener builder = obs -> buildCells();
        control.dateProperty().addListener(builder);
        control.viewUnitProperty().addListener(builder);
        control.extendedViewUnitProperty().addListener(builder);
        control.extendedUnitsForwardProperty().addListener(builder);
        control.extendedUnitsBackwardProperty().addListener(builder);
        control.weekDayLayoutProperty().addListener(builder);
        control.weekFieldsProperty().addListener(builder);
        control.cellFactoryProperty().addListener(builder);
        control.headerCellFactoryProperty().addListener(builder);
        control.enableHyperlinksProperty().addListener(builder);
        control.getCalendars().addListener((Observable obs) -> updateEntries("list of calendars changed"));
        control.clickBehaviourProperty().addListener(it -> control.getDateSelectionModel().clear());
        control.getDateSelectionModel().getSelectedDates().addListener((Observable obs) -> updateSelected());

        // important to use change listener
        ChangeListener todayUpdater = (obs, oldValue, newValue) -> updateToday();
        control.todayProperty().addListener(todayUpdater);
        control.showTodayProperty().addListener(todayUpdater);

        EventHandler<KeyEvent> keyPressedHandler = evt -> {
            DateSelectionModel selectionModel = getSkinnable().getDateSelectionModel();
            LocalDate lastSelected = selectionModel.getLastSelected();

            if (lastSelected != null) {

                Position lastPosition = dateToPositionMap.get(lastSelected);

                LocalDate newSelection = null;
                boolean isNavigationKey = true;

                switch (evt.getCode()) {
                    case UP:
                        newSelection = lastSelected.plusDays(-1);
                        break;

                    case DOWN:
                        newSelection = lastSelected.plusDays(1);
                        break;

                    case LEFT:
                        Position newPosition = new Position(Math.max(0, lastPosition.getColumn() - 1), lastPosition.getRow());
                        DateCell newCell = positionToDateCellMap.get(newPosition);
                        if (newCell != null) {
                            newSelection = newCell.getDate();
                        }
                        break;

                    case RIGHT:
                        newPosition = new Position(lastPosition.getColumn() + 1, lastPosition.getRow());
                        newCell = positionToDateCellMap.get(newPosition);
                        if (newCell != null) {
                            newSelection = newCell.getDate();
                        }
                        break;

                    default:
                        isNavigationKey = false;
                        break;
                }

                if (getSkinnable().isVisibleDate(newSelection)) {
                    if (evt.isShiftDown()) {
                        selectionModel.selectUntil(newSelection);
                    } else if (evt.isShortcutDown()) {
                        selectionModel.select(newSelection);
                    } else {
                        selectionModel.clearAndSelect(newSelection);
                    }
                }

                if (isNavigationKey) {
                    evt.consume();
                }
            }
        };
        control.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.setFocusTraversable(true);

        buildCells();
        getChildren().add(grid);

        updateEntries("initial load");
        updateToday();
    }

    private void updateRowConstraints() {
        int rowCount = 32; // header + 31 days
        if (getSkinnable().getWeekDayLayout() == WeekDayLayoutStrategy.ALIGNED) {
            rowCount += 6; // 6 = max number of empty slots / cells at the top
        }

        List<RowConstraints> rowConstraints = new ArrayList<>();
        for (int i = 0; i <= rowCount; i++) {
            RowConstraints con = new RowConstraints();
            con.setFillHeight(true);
            con.setPrefHeight(Region.USE_COMPUTED_SIZE);
            con.setMinHeight(Region.USE_PREF_SIZE);
            con.setMaxHeight(Double.MAX_VALUE);
            con.setVgrow(i == 0 ? Priority.NEVER : Priority.ALWAYS);
            rowConstraints.add(con);
        }

        grid.getRowConstraints().setAll(rowConstraints);
    }

    @Override
    protected void calendarVisibilityChanged() {
        updateEntries("calendar visibility changed");
    }

    private void buildCells() {
        positionToDateCellMap.clear();
        dateToPositionMap.clear();
        cellMap.clear();

        YearMonth start = getSkinnable().getExtendedStartMonth();
        YearMonth end = getSkinnable().getExtendedEndMonth();

        int colIndex = 0;

        grid.getColumnConstraints().clear();
        grid.getChildren().clear();

        while (!start.isAfter(end)) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setFillWidth(true);
            columnConstraints.setMinWidth(Region.USE_PREF_SIZE);
            columnConstraints.setMaxWidth(Double.MAX_VALUE);
            grid.getColumnConstraints().add(columnConstraints);

            buildCells(start, colIndex);

            start = start.plusMonths(1);
            colIndex++;
        }

        grid.getColumnConstraints().forEach(con -> con.setPercentWidth(100d / (double) grid.getColumnConstraints().size()));

        updateEntries("cells were rebuild");
        updateToday();
        updateSelected();
    }

    private void buildCells(YearMonth yearMonth, int colIndex) {
        List<Node> cells = new ArrayList<>();
        Node header = buildHeaderCell(yearMonth);
        header.getStyleClass().add("month-header");

        cells.add(header);

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        if (getSkinnable().getWeekDayLayout() == WeekDayLayoutStrategy.ALIGNED) {
            DayOfWeek firstDayOfWeek = getSkinnable().getFirstDayOfWeek();
            DayOfWeek startDayOfWeek = start.getDayOfWeek();
            int distanceDays = Math.abs(firstDayOfWeek.getValue() - startDayOfWeek.getValue());

            while (distanceDays-- > 0) {
                cells.add(buildCell(null));
            }
        }

        while (start.isBefore(end) || start.isEqual(end)) {
            cells.add(buildCell(start));
            start = start.plusDays(1);
        }

        buildEmptyCellBottom(cells);

        final YearMonth extendedStart = getSkinnable().getExtendedStartMonth();
        final YearMonth extendedEnd = getSkinnable().getExtendedEndMonth();

        cells.forEach(cell -> {
            if (extendedStart.equals(yearMonth)) {
                cell.getStyleClass().add("first-month");
            } else if (extendedEnd.equals(yearMonth)) {
                cell.getStyleClass().add("last-month");
            } else {
                cell.getStyleClass().add("middle-month");
            }
        });

        for (int i = 0; i < cells.size(); i++) {
            Node node = cells.get(i);
            grid.add(node, colIndex, i + 1);

            if (node instanceof DateCell) {

                final Position position = new Position(colIndex, i);
                final DateCell dateCell = (DateCell) node;
                final LocalDate date = dateCell.getDate();

                cellMap.put(date, dateCell);
                positionToDateCellMap.put(position, dateCell);
                dateToPositionMap.put(date, position);
            }
        }
    }

    private DateCell buildCell(LocalDate date) {
        DateCell cell = getSkinnable().getCellFactory().call(new MonthSheetView.DateParameter(getSkinnable(), date));
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cell.setOnMouseClicked(weakCellClickedHandler);
        return cell;
    }

    private Node buildHeaderCell(YearMonth yearMonth) {
        return getSkinnable().getHeaderCellFactory().
                call(new MonthSheetView.HeaderParameter(getSkinnable(), yearMonth));
    }

    private void buildEmptyCellBottom(List<Node> cells) {
        int maximumCells = 31;
        if (getSkinnable().getWeekDayLayout().equals(WeekDayLayoutStrategy.ALIGNED)) {
            maximumCells = 37;
        }

        int cellsNumber = cells.size() - 1;

        if (cellsNumber < maximumCells) {
            while (cellsNumber < maximumCells) {
                cells.add(buildCell(null));
                cellsNumber++;
            }
        }
    }

    private void updateSelected() {
        List<LocalDate> selectedDates = getSkinnable().getDateSelectionModel().getSelectedDates();
        grid.getChildren().stream()
                .filter(child -> child instanceof DateCell)
                .map(child -> (DateCell) child)
                .forEach(cell -> cell.setSelected(selectedDates.contains(cell.getDate())));
    }

    private void updateToday() {
        LocalDate today = getSkinnable().getToday();
        grid.getChildren().stream()
                .filter(child -> child instanceof DateCell)
                .map(child -> (DateCell) child)
                .forEach(cell -> cell.setToday(getSkinnable().isShowToday() && today.equals(cell.getDate())));
    }

    private static final class Position {

        int column;
        int row;

        private Position(int column, int row) {
            this.column = column;
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Position position = (Position) o;

            if (column != position.column) {
                return false;
            }

            return row == position.row;
        }

        @Override
        public int hashCode() {
            int result = column;
            result = 31 * result + row;
            return result;
        }
    }

    private final EventHandler<MouseEvent> cellClickedHandler = evt -> {
        if (!(evt.getSource() instanceof DateCell)) {
            return;
        }

        DateCell cell = (DateCell) evt.getSource();
        cell.requestFocus();
        LocalDate date = cell.getDate();

        if (date != null) {
            switch (getSkinnable().getClickBehaviour()) {
                case NONE:
                    break;
                case PERFORM_SELECTION:
                    DateSelectionModel selectionModel = getSkinnable().getDateSelectionModel();

                    if (selectionModel.isSelected(date)) {
                        /*
                         * Only deselect if the user uses the left / the primary button.
                         * The right button for the context menu will not deselect the cell.
                         */
                        if (evt.getButton() == MouseButton.PRIMARY) {
                            if (evt.isShortcutDown()) {
                                selectionModel.deselect(date);
                            } else {
                                selectionModel.clear();
                            }
                        }
                    } else {
                        if (evt.isShiftDown() && evt.getButton() == MouseButton.PRIMARY) {
                            selectionModel.selectUntil(date);
                        } else if (evt.isShortcutDown() && evt.getButton() == MouseButton.PRIMARY) {
                            selectionModel.select(date);
                        } else {
                            selectionModel.clearAndSelect(date);
                        }
                    }
                    break;
                case SHOW_DETAILS:
                    showDateDetails(date);
                    break;
            }
        }
    };

    private void showDateDetails(LocalDate date) {
        DateCell cell = cellMap.get(date);
        Bounds bounds = cell.localToScreen(cell.getLayoutBounds());
        Callback<DateDetailsParameter, Boolean> callback = getSkinnable().getDateDetailsCallback();
        if (callback != null) {
            callback.call(new DateDetailsParameter(null, getSkinnable(), cell, cell.getScene().getRoot(), date, bounds.getMinX(), bounds.getMinY()));
        }
    }

    private final WeakEventHandler<MouseEvent> weakCellClickedHandler = new WeakEventHandler<>(cellClickedHandler);

    @Override
    protected void calendarChanged(Calendar calendar) {
        updateEntries("calendar changed");
    }

    @Override
    protected void entryCalendarChanged(CalendarEvent evt) {
        updateEntries("entry calendar changed");
    }

    @Override
    protected void entryIntervalChanged(CalendarEvent evt) {
        updateEntries("entry interval changed");
    }

    @Override
    protected void entryFullDayChanged(CalendarEvent evt) {
        updateEntries("entry full day flag changed");
    }

    @Override
    protected void entryRecurrenceRuleChanged(CalendarEvent evt) {
        updateEntries("entry recurrence rule changed");
    }

    private void updateEntries(String reason) {
        if (LoggingDomain.VIEW.isLoggable(Level.FINE)) {
            LoggingDomain.VIEW.fine("updating entries because: " + reason);
        }

        dataMap.clear();
        dataLoader.loadEntries(dataMap);

        for (LocalDate date : cellMap.keySet()) {
            List<Entry<?>> entries = dataMap.get(date);
            DateCell cell = cellMap.get(date);
            cell.updateEntries(entries == null ? Collections.emptyList() : entries);
        }
    }

    @Override
    public String getLoaderName() {
        return "Month Sheet View";
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
    public LocalDate getLoadStartDate() {
        return getSkinnable().getExtendedStartMonth().atDay(1);
    }

    @Override
    public LocalDate getLoadEndDate() {
        return getSkinnable().getExtendedEndMonth().atEndOfMonth();
    }

    @Override
    public ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    @Override
    public boolean isCalendarVisible(Calendar calendar) {
        return getSkinnable().isCalendarVisible(calendar);
    }
}
