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

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.Entry;
import impl.com.calendarfx.view.MonthSheetViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A view laying out months and dates in a spreadsheet style. An ideal way of displaying several
 * months or even a year. Each day is represented by a cell (see {@link DateCell}). Cells are
 * created by a cell factory and can be customized to fit an application's needs.
 * <h3>Screenshot (Using DetailedDateCell)</h3>
 * <center><img width="100%" src="doc-files/month-sheet-view.png"></center>
 *
 * <h3>Screenshot (Using "aligned" weekday layout)</h3>
 * <center><img width="100%" src="doc-files/month-sheet-view-aligned.png"></center>
 *
 * @see #setWeekDayLayout(WeekDayLayoutStrategy)
 * @see #setCellFactory(Callback)
 * @see MonthSheetView.DateCell
 * @see MonthSheetView.SimpleDateCell
 * @see MonthSheetView.DetailedDateCell
 * @see MonthSheetView.BadgeDateCell
 * @see MonthSheetView.UsageDateCell
 */
public class MonthSheetView extends DateControl {

    private static final String DEFAULT_STYLE_CLASS = "month-sheet-view";

    private static final String USAGE_VERY_LOW = "usage-very-low";
    private static final String USAGE_LOW = "usage-low";
    private static final String USAGE_MEDIUM = "usage-medium";
    private static final String USAGE_HIGH = "usage-high";
    private static final String USAGE_VERY_HIGH = "usage-very-high";


    private double ctxMenuScreenX;
    private double ctxMenuScreenY;
    private DateCell dateCell;

    /**
     * Constructs a new month sheet view that is using the {@link SimpleDateCell}.
     */
    public MonthSheetView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setHeaderCellFactory(param -> new MonthHeaderCell(param.getView(), param.getYearMonth()));

        /*
         * This view has its own context menu.
         */
        setContextMenu(createContextMenu());
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, evt -> {
            final PickResult pickResult = evt.getPickResult();
            final Node intersectedNode = pickResult.getIntersectedNode();
            if (intersectedNode != null && intersectedNode instanceof DateCell) {
                this.dateCell = (DateCell) intersectedNode;
            } else {
                this.dateCell = null;
            }

            ctxMenuScreenX = evt.getScreenX();
            ctxMenuScreenY = evt.getScreenY();
        });

        // Set the factory AFTER the context menu has been created or the cell factory
        // will be overridden again.
        setCellFactory(param -> new SimpleDateCell(param.getView(), param.getDate()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MonthSheetViewSkin(this);
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem newEntry = new MenuItem(Messages.getString("MonthSheetView.ADD_NEW_EVENT")); //$NON-NLS-1$
        newEntry.setOnAction(evt -> {

            LocalDate date = getDateSelectionModel().getLastSelected();
            Entry<?> entry = createEntryAt(ZonedDateTime.of(date, LocalTime.of(12, 0), getZoneId()));

            Callback<EntryDetailsParameter, Boolean> callback = getEntryDetailsCallback();
            EntryDetailsParameter param = new EntryDetailsParameter(null, this, entry, dateCell, ctxMenuScreenX, ctxMenuScreenY);
            callback.call(param);

        });
        contextMenu.getItems().add(newEntry);

        contextMenu.getItems().add(new SeparatorMenuItem());

        RadioMenuItem standardCellItem = new RadioMenuItem(Messages.getString("MonthSheetView.STANDARD_CELLS"));
        RadioMenuItem detailCellItem = new RadioMenuItem(Messages.getString("MonthSheetView.DETAIL_CELLS"));
        RadioMenuItem usageCellItem = new RadioMenuItem(Messages.getString("MonthSheetView.USAGE_CELLS"));
        RadioMenuItem badgeCellItem = new RadioMenuItem(Messages.getString("MonthSheetView.BADGE_CELLS"));

        standardCellItem.setOnAction(evt -> setCellFactory(param -> new SimpleDateCell(param.getView(), param.getDate())));
        detailCellItem.setOnAction(evt -> setCellFactory(param -> new DetailedDateCell(param.getView(), param.getDate())));
        usageCellItem.setOnAction(evt -> setCellFactory(param -> new UsageDateCell(param.getView(), param.getDate())));
        badgeCellItem.setOnAction(evt -> setCellFactory(param -> new BadgeDateCell(param.getView(), param.getDate())));

        ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(standardCellItem, detailCellItem, usageCellItem, badgeCellItem);

        contextMenu.getItems().addAll(standardCellItem, detailCellItem, usageCellItem, badgeCellItem);

        standardCellItem.setSelected(true);

        return contextMenu;
    }

    // cell factory support

    private final ObjectProperty<Callback<DateParameter, DateCell>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

    /**
     * A property used to store a reference to the cell factory for this view. The default cell factory
     * simply returns an instance of type {@link SimpleDateCell}.
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<DateParameter, DateCell>> cellFactoryProperty() {
        return cellFactory;
    }

    /**
     * Returns the value of {@link #cellFactoryProperty()}.
     *
     * @return the cell factory
     */
    public final Callback<DateParameter, DateCell> getCellFactory() {
        return cellFactoryProperty().get();
    }

    /**
     * Sets the value of {@link #cellFactoryProperty()}.
     *
     * @param factory the cell factory
     */
    public final void setCellFactory(Callback<DateParameter, DateCell> factory) {
        requireNonNull(factory);
        cellFactoryProperty().set(factory);
    }

    // header cell factory support

    private final ObjectProperty<Callback<HeaderParameter, Node>> headerCellFactory = new SimpleObjectProperty<>(this, "headerCellFactory");

    /**
     * A property used to store a reference to the "header" cell factory for this view. The default cell factory
     * simply returns an instance of type {@link MonthHeaderCell}.
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<HeaderParameter, Node>> headerCellFactoryProperty() {
        return headerCellFactory;
    }

    /**
     * Returns the value of {@link #headerCellFactoryProperty()}.
     *
     * @return the "header" cell factory
     */
    public final Callback<HeaderParameter, Node> getHeaderCellFactory() {
        return headerCellFactoryProperty().get();
    }

    /**
     * Sets the value of {@link #headerCellFactoryProperty()}.
     *
     * @param factory the "header" cell factory
     */
    public final void setHeaderCellFactory(Callback<HeaderParameter, Node> factory) {
        requireNonNull(factory);
        headerCellFactoryProperty().set(factory);
    }

    // layout strategy support

    /**
     * The different ways the view can align date cells.
     *
     * @see #setWeekDayLayout(WeekDayLayoutStrategy)
     */
    public enum WeekDayLayoutStrategy {

        /**
         * Aligns the date cells in the traditional ways where
         * the top-most cell shows the 1st day of the month and the
         * bottom-most cell shows the last day of the month.
         */
        STANDARD,

        /**
         * Aligns the date cells in such a way that all cells
         * in a given row are dates on the same weekday, e.g. all
         * cells / dates are located on a "Monday".
         */
        ALIGNED
    }

    private final ObjectProperty<WeekDayLayoutStrategy> weekDayLayout = new SimpleObjectProperty<>(this, "weekDayLayout", WeekDayLayoutStrategy.STANDARD);

    /**
     * A property used to store the layout strategy used by this view. The month sheet
     * view is capable of ensuring that the date cells next to each other always show
     * the same weekday. For this extra empty cells are added at the top of some of the
     * columns. However, the standard layout is to always show the first day of the month
     * in the first cell from the top.
     *
     * @return the weekday layout strategy property
     */
    public final ObjectProperty<WeekDayLayoutStrategy> weekDayLayoutProperty() {
        return weekDayLayout;
    }

    /**
     * Returns the value of {@link #weekDayLayoutProperty()}.
     *
     * @return the weekday layout strategy
     */
    public final WeekDayLayoutStrategy getWeekDayLayout() {
        return weekDayLayoutProperty().get();
    }

    /**
     * Sets the value of {@link #weekDayLayoutProperty()}.
     *
     * @param weekDayLayout the weekday layout strategy
     */
    public final void setWeekDayLayout(WeekDayLayoutStrategy weekDayLayout) {
        weekDayLayoutProperty().set(weekDayLayout);
    }

    // view unit support

    private final ObjectProperty<ViewUnit> viewUnit = new SimpleObjectProperty<>(this, "viewUnit", ViewUnit.YEAR);

    /**
     * A property used to store the unit shown by the view (e.g. "quarters", "year", "month").
     *
     * @return the view unit (e.g. "quarters", "year", "month")
     */
    public final ObjectProperty<ViewUnit> viewUnitProperty() {
        return viewUnit;
    }

    /**
     * Returns the value of {@link #viewUnitProperty()}.
     *
     * @return the view unit (e.g. "quarters", "year", "month")
     */
    public final ViewUnit getViewUnit() {
        return viewUnitProperty().get();
    }

    /**
     * Sets the value of {@link #viewUnitProperty()}.
     *
     * @param unit the view unit  (e.g. "quarters", "year", "month")
     */
    public final void setViewUnit(ViewUnit unit) {
        requireNonNull(unit);
        viewUnitProperty().set(unit);
    }

    // extended view unit support

    private final ObjectProperty<ViewUnit> extendedViewUnit = new SimpleObjectProperty<>(this, "extendedViewUnit", ViewUnit.MONTH);

    /**
     * A property used to store the unit shown by the view in front of or after the
     * main columns (e.g. "quarters", "year", "month"). So a month showing twelve months
     * of a year might decide to add some "padding" and show another month in front of
     * the year and one month after the year.
     *
     * @return the view unit (e.g. "quarters", "year", "month")
     */
    public final ObjectProperty<ViewUnit> extendedViewUnitProperty() {
        return extendedViewUnit;
    }

    /**
     * Returns the value of {@link #extendedViewUnitProperty()}.
     *
     * @return the extended view unit (e.g. "quarters", "year", "month")
     */
    public final ViewUnit getExtendedViewUnit() {
        return extendedViewUnitProperty().get();
    }

    /**
     * Sets the value of {@link #extendedViewUnitProperty()}.
     *
     * @param unit the extended view unit  (e.g. "quarters", "year", "month")
     */
    public final void setExtendedViewUnit(ViewUnit unit) {
        requireNonNull(unit);
        extendedViewUnitProperty().set(unit);
    }

    // extended units forward support

    private final IntegerProperty extendedUnitsForward = new SimpleIntegerProperty(this, "extendedUnitsForward");

    /**
     * An integer property that stores the number of units that will be used
     * to extend the view by. A month showing twelve months of a year might decide
     * to add some "padding" and show another month at the end of the year. In this
     * case the number of extended units will be 1.
     *
     * @return the number of units used to extend the view at the end (e.g. "3 months")
     */
    public final IntegerProperty extendedUnitsForwardProperty() {
        return extendedUnitsForward;
    }

    /**
     * Returns the value of {@link #extendedUnitsForward}.
     *
     * @return the number of units shown at the end of the view
     */
    public final int getExtendedUnitsForward() {
        return extendedUnitsForwardProperty().get();
    }

    /**
     * Sets the value of {@link #extendedUnitsForward}.
     *
     * @param units the number of units shown at the end of the view
     */
    public final void setExtendedUnitsForward(int units) {
        if (units < 0) {
            throw new IllegalArgumentException("number of units can not be negative but was " + units);
        }
        extendedUnitsForwardProperty().set(units);
    }

    // extended units backward support

    private final IntegerProperty extendedUnitsBackward = new SimpleIntegerProperty(this, "extendedUnitsBackward");

    /**
     * An integer property that stores the number of units that will be used
     * to extend the view by. A month showing twelve months of a year might decide
     * to put some "padding" in front of the year and show another month. In this
     * case the number of extended units will be 1.
     *
     * @return the number of units used to extend the view at the beginning (e.g. "3 months")
     */
    public final IntegerProperty extendedUnitsBackwardProperty() {
        return extendedUnitsBackward;
    }

    /**
     * Returns the value of {@link #extendedUnitsBackwardProperty}.
     *
     * @return the number of units shown at the beginning of the view
     */
    public final int getExtendedUnitsBackward() {
        return extendedUnitsBackwardProperty().get();
    }

    /**
     * Sets the value of {@link #extendedUnitsBackwardProperty}.
     *
     * @param units the number of units shown at the beginning of the view
     */
    public final void setExtendedUnitsBackward(final int units) {
        if (units < 0) {
            throw new IllegalArgumentException("number of units can not be negative but was " + units);
        }
        extendedUnitsBackwardProperty().set(units);
    }

    /**
     * Returns the first "regular" month shown by the view. This method
     * does not consider the extended months. If the view shows a whole
     * year then the start month will be January.
     *
     * @return the start month
     */
    public final YearMonth getStartMonth() {
        return getViewUnit().getStartMonth(getDate());
    }

    /**
     * Returns the first "regular" month shown by the view. This method
     * does not consider the extended months. If the view shows a whole
     * year then the end month will be December.
     *
     * @return the start month
     */
    public final YearMonth getEndMonth() {
        return getViewUnit().getEndMonth(getDate());
    }

    /**
     * Returns the first month shown by the view. This method also takes the extended months into account. If the view shows a whole
     * year with an extended unit of "month" and and extended unit count of two, then the start month will be November of the previous year.
     *
     * @return the start month
     * @see #setExtendedViewUnit(ViewUnit)
     * @see #setExtendedUnitsBackward(int)
     * @see #setExtendedUnitsForward(int)
     */
    public final YearMonth getExtendedStartMonth() {
        return getStartMonth().minusMonths(getExtendedViewUnit().toMonths(getExtendedUnitsBackward()));
    }

    /**
     * Returns the last month shown by the view. This method also takes the extended months into account. If the view shows a whole
     * year with an extended unit of "month" and and extended unit count of two, then the end month will be February of the next year.
     *
     * @return the start month
     * @see #setExtendedViewUnit(ViewUnit)
     * @see #setExtendedUnitsBackward(int)
     * @see #setExtendedUnitsForward(int)
     */
    public final YearMonth getExtendedEndMonth() {
        return getEndMonth().plusMonths(getExtendedViewUnit().toMonths(getExtendedUnitsForward()));
    }

    /**
     * A simple check to see if the given month is part of the extended months.
     *
     * @param month the month to check
     * @return true if the given month is part of the extended months
     * @see #setExtendedViewUnit(ViewUnit)
     * @see #setExtendedUnitsBackward(int)
     * @see #setExtendedUnitsForward(int)
     */
    public final boolean isExtendedMonth(YearMonth month) {
        if (month != null) {
            YearMonth extendedStart = getExtendedStartMonth();
            if ((month.equals(extendedStart) || month.isAfter(extendedStart)) && month.isBefore(getStartMonth())) {
                return true;
            }

            YearMonth extendedEnd = getExtendedEndMonth();
            if ((month.equals(extendedEnd) || month.isBefore(extendedEnd)) && month.isAfter(getEndMonth())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given date is currently showing is part of the view. This
     * method uses the extended start and end months.
     *
     * @param date the date to check for visibility
     * @return true if the date is within the time range of the view
     * @see #getExtendedStartMonth()
     * @see #getExtendedEndMonth()
     */
    public final boolean isVisibleDate(LocalDate date) {
        if (date != null) {
            YearMonth extendedStart = getExtendedStartMonth();
            YearMonth extendedEnd = getExtendedEndMonth();

            LocalDate startDate = extendedStart.atDay(1);
            LocalDate endDate = extendedEnd.atEndOfMonth();

            if ((date.equals(startDate) || date.isAfter(startDate)) && (date.equals(endDate) || date.isBefore(endDate))) {
                return true;
            }
        }
        return false;
    }

    // show week number support

    private final BooleanProperty showWeekNumber = new SimpleBooleanProperty(this, "showWeekNumber", true);

    /**
     * A property used to control whether the week numbers should be shown.
     *
     * @return true if the numbers should be shown
     * @see DateControl#weekFieldsProperty()
     */
    public final BooleanProperty showWeekNumberProperty() {
        return showWeekNumber;
    }

    /**
     * Returns the value of {@link #showWeekNumberProperty()}.
     *
     * @return true if the numbers should be shown
     */
    public final boolean isShowWeekNumber() {
        return showWeekNumberProperty().get();
    }

    /**
     * Sets the value of {@link #showWeekNumberProperty()}.
     *
     * @param show true if the numbers should be shown
     */
    public final void setShowWeekNumber(boolean show) {
        showWeekNumberProperty().set(show);
    }

    // date selection model support

    private final ObjectProperty<DateSelectionModel> dateSelectionModel = new SimpleObjectProperty<>(this, "dateSelectionModel", new DateSelectionModel());

    /**
     * A property used to store a selection model for selecting dates.
     *
     * @return the date selection model
     */
    public final ObjectProperty<DateSelectionModel> dateSelectionModelProperty() {
        return dateSelectionModel;
    }

    /**
     * Returns the value of {@link #dateSelectionModelProperty()}.
     *
     * @return the date selection model
     */
    public final DateSelectionModel getDateSelectionModel() {
        return dateSelectionModelProperty().get();
    }

    /**
     * Sets the value of {@link #dateSelectionModelProperty()}.
     *
     * @param model the date selection model
     */
    public final void setDateSelectionModel(DateSelectionModel model) {
        Objects.requireNonNull(model);
        dateSelectionModelProperty().set(model);
    }

    /**
     * An enumerator to control the behaviour of the control when the user
     * clicks on a date. The view supports date selections or the ability to
     * show a popover that lists the entries on the clicked date.
     *
     * @see MonthSheetView#clickBehaviourProperty()
     */
    public enum ClickBehaviour {

        /**
         * A value used to make the control select the date on which the user clicked.
         */
        PERFORM_SELECTION,

        /**
         * A value used to make the control show some kind of dialog or popover to show
         * details about the clicked date.
         */
        SHOW_DETAILS,

        /**
         * Do nothing when the user clicks on a date.
         */
        NONE
    }

    private final ObjectProperty<ClickBehaviour> clickBehaviour = new SimpleObjectProperty<>(this, "clickBehaviour", ClickBehaviour.PERFORM_SELECTION);

    /**
     * The behaviour used when the user clicks on a date.
     *
     * @return the click behaviour
     */
    public final ObjectProperty<ClickBehaviour> clickBehaviourProperty() {
        return clickBehaviour;
    }

    /**
     * Sets the value of {@link #clickBehaviourProperty()}.
     *
     * @param behaviour the click behaviour
     */
    public final void setClickBehaviour(ClickBehaviour behaviour) {
        requireNonNull(behaviour);
        clickBehaviourProperty().set(behaviour);
    }

    /**
     * Returns the value of {@link #clickBehaviourProperty()}.
     *
     * @return the click behaviour
     */
    public final ClickBehaviour getClickBehaviour() {
        return clickBehaviourProperty().get();
    }

    /**
     * A view unit describes how many months the view should show. The view
     * knows about years, semesters (6 months), quarters (3 months), and single
     * months. When the date property changes the view will make sure to show
     * the year, semester, quarter, or month where that date is located.
     *
     * @see MonthSheetView#setViewUnit(ViewUnit)
     * @see MonthSheetView#setExtendedViewUnit(ViewUnit)
     */
    public enum ViewUnit {

        /**
         * A value used to instruct the {@link MonthSheetView} to display a single
         * month.
         *
         * @see MonthSheetView#setViewUnit(ViewUnit)
         */
        MONTH {
            @Override
            public YearMonth getStartMonth(LocalDate date) {
                return YearMonth.from(date);
            }

            @Override
            public YearMonth getEndMonth(LocalDate date) {
                return YearMonth.from(date);
            }

            @Override
            public int getMonthsCount() {
                return 1;
            }
        },

        /**
         * A value used to instruct the {@link MonthSheetView} to display a quarter
         * year (3 months).
         *
         * @see MonthSheetView#setViewUnit(ViewUnit)
         */
        QUARTER {
            @Override
            public YearMonth getStartMonth(LocalDate date) {
                return Year.of(date.getYear()).atMonth(QUARTER_START_MONTH[date.getMonthValue() - 1]);
            }

            @Override
            public YearMonth getEndMonth(LocalDate date) {
                return Year.of(date.getYear()).atMonth(QUARTER_END_MONTH[date.getMonthValue() - 1]);
            }

            @Override
            public int getMonthsCount() {
                return 3;
            }
        },

        /**
         * A value used to instruct the {@link MonthSheetView} to display a semester
         * (6 months).
         *
         * @see MonthSheetView#setViewUnit(ViewUnit)
         */
        SEMESTER {
            @Override
            public YearMonth getStartMonth(LocalDate date) {
                return Year.of(date.getYear()).atMonth(SEMESTER_START_MONTH[date.getMonthValue() - 1]);
            }

            @Override
            public YearMonth getEndMonth(LocalDate date) {
                return Year.of(date.getYear()).atMonth(SEMESTER_END_MONTH[date.getMonthValue() - 1]);
            }

            @Override
            public int getMonthsCount() {
                return 6;
            }
        },

        /**
         * A value used to instruct the {@link MonthSheetView} to display a whole year.
         *
         * @see MonthSheetView#setViewUnit(ViewUnit)
         */
        YEAR {
            @Override
            public YearMonth getStartMonth(LocalDate date) {
                return Year.from(date).atMonth(Month.JANUARY);
            }

            @Override
            public YearMonth getEndMonth(LocalDate date) {
                return Year.from(date).atMonth(Month.DECEMBER);
            }

            @Override
            public int getMonthsCount() {
                return 12;
            }
        };

        private static final int[] QUARTER_START_MONTH = {1, 1, 1, 4, 4, 4, 7, 7, 7, 10, 10, 10};
        private static final int[] QUARTER_END_MONTH = {3, 3, 3, 6, 6, 6, 9, 9, 9, 12, 12, 12};
        private static final int[] SEMESTER_START_MONTH = {1, 1, 1, 1, 1, 1, 7, 7, 7, 7, 7, 7};
        private static final int[] SEMESTER_END_MONTH = {6, 6, 6, 6, 6, 6, 12, 12, 12, 12, 12, 12};

        /**
         * Returns the start month for the given view unit and date.
         *
         * @param date the date for which to return the start month
         * @return the start month for the given view unit and date
         */
        public abstract YearMonth getStartMonth(LocalDate date);

        /**
         * Returns the end month for the given view unit and date.
         *
         * @param date the date for which to return the end month
         * @return the end month for the given view unit and date
         */
        public abstract YearMonth getEndMonth(LocalDate date);

        /**
         * Returns the number of months represented by the view unit, e.g. "3" for
         * "quarter".
         *
         * @return the month count of the view unit
         */
        public abstract int getMonthsCount();

        /**
         * Calculates the total number of months for the given
         * number of units, e.g. when "units" is "3" and the "view unit" is
         * "quarter" then the total number will be "9": three quarters equal
         * 9 months.
         *
         * @param units the number of units
         * @return the total number of months
         */
        public int toMonths(int units) {
            return getMonthsCount() * units;
        }
    }

    /**
     * A parameter object used by the cell factory of the month sheet view.
     */
    public static final class DateParameter {

        private MonthSheetView view;
        private LocalDate date;

        /**
         * Constructs a new parameter object.
         *
         * @param view the view for which a cell has to be created
         * @param date the date that the cell will represent
         */
        public DateParameter(MonthSheetView view, LocalDate date) {
            this.view = Objects.requireNonNull(view);
            this.date = date;
        }

        /**
         * Returns the view for which a cell has to be created.
         *
         * @return the month sheet view
         */
        public MonthSheetView getView() {
            return view;
        }

        /**
         * Returns the date for which a cell has to be created.
         *
         * @return the date
         */
        public LocalDate getDate() {
            return date;
        }
    }

    /**
     * A parameter object used by the "header" cell factory of the month sheet view.
     */
    public static final class HeaderParameter {

        private MonthSheetView view;
        private YearMonth yearMonth;

        public HeaderParameter(MonthSheetView view, YearMonth yearMonth) {
            this.view = Objects.requireNonNull(view);
            this.yearMonth = yearMonth;
        }

        public final MonthSheetView getView() {
            return view;
        }

        public final YearMonth getYearMonth() {
            return yearMonth;
        }
    }

    private static final String MONTH_SHEET_VIEW_CATEGORY = "Month Sheet View";

    @Override
    public ObservableList<PropertySheet.Item> getPropertySheetItems() {
        ObservableList<PropertySheet.Item> items = super.getPropertySheetItems();

        items.add(new PropertySheet.Item() {
            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(clickBehaviourProperty());
            }

            @Override
            public void setValue(Object value) {
                setClickBehaviour((ClickBehaviour) value);
            }

            @Override
            public Object getValue() {
                return getClickBehaviour();
            }

            @Override
            public Class<?> getType() {
                return ClickBehaviour.class;
            }

            @Override
            public String getName() {
                return "Click Behaviour"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Click behaviour"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_SHEET_VIEW_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return WeekDayLayoutStrategy.class;
            }

            @Override
            public String getCategory() {
                return MONTH_SHEET_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Week Day Layout";
            }

            @Override
            public String getDescription() {
                return "The layout strategy for the week day on the months";
            }

            @Override
            public Object getValue() {
                return getWeekDayLayout();
            }

            @Override
            public void setValue(Object value) {
                setWeekDayLayout((WeekDayLayoutStrategy) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(weekDayLayoutProperty());
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return ViewUnit.class;
            }

            @Override
            public String getCategory() {
                return MONTH_SHEET_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "View Unit";
            }

            @Override
            public String getDescription() {
                return "View Unit";
            }

            @Override
            public Object getValue() {
                return getViewUnit();
            }

            @Override
            public void setValue(Object value) {
                setViewUnit((ViewUnit) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(viewUnitProperty());
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return ViewUnit.class;
            }

            @Override
            public String getCategory() {
                return MONTH_SHEET_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Extended View Unit";
            }

            @Override
            public String getDescription() {
                return "Extended View Unit";
            }

            @Override
            public Object getValue() {
                return getExtendedViewUnit();
            }

            @Override
            public void setValue(Object value) {
                setExtendedViewUnit((ViewUnit) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(extendedViewUnitProperty());
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return Integer.class;
            }

            @Override
            public String getCategory() {
                return MONTH_SHEET_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Extended Units Forward";
            }

            @Override
            public String getDescription() {
                return "Extended Units Forward";
            }

            @Override
            public Object getValue() {
                return getExtendedUnitsForward();
            }

            @Override
            public void setValue(Object value) {
                setExtendedUnitsForward((Integer) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(extendedUnitsForwardProperty());
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return Integer.class;
            }

            @Override
            public String getCategory() {
                return MONTH_SHEET_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Extended Units Backward";
            }

            @Override
            public String getDescription() {
                return "Extended Units Backward";
            }

            @Override
            public Object getValue() {
                return getExtendedUnitsBackward();
            }

            @Override
            public void setValue(Object value) {
                setExtendedUnitsBackward((Integer) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(extendedUnitsBackwardProperty());
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getCategory() {
                return MONTH_SHEET_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Show Week Number";
            }

            @Override
            public String getDescription() {
                return "Show Week Number";
            }

            @Override
            public Object getValue() {
                return isShowWeekNumber();
            }

            @Override
            public void setValue(Object value) {
                setShowWeekNumber((Boolean) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showWeekNumberProperty());
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return DateSelectionModel.SelectionMode.class;
            }

            @Override
            public String getCategory() {
                return MONTH_SHEET_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Date Selection Mode";
            }

            @Override
            public String getDescription() {
                return "Date Selection Mode";
            }

            @Override
            public Object getValue() {
                return getDateSelectionModel().getSelectionMode();
            }

            @Override
            public void setValue(Object value) {
                getDateSelectionModel().setSelectionMode((DateSelectionModel.SelectionMode) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(getDateSelectionModel().selectionModeProperty());
            }
        });

        return items;
    }

    /**
     * The default cell used for month headers.
     *
     * @see MonthSheetView#setHeaderCellFactory(Callback)
     */
    public static class MonthHeaderCell extends Label {

        private final YearMonth yearMonth;
        private final MonthSheetView view;

        /**
         * Constructs a new month header cell.
         *
         * @param view      the view where the header is needed
         * @param yearMonth the month to display
         */
        public MonthHeaderCell(MonthSheetView view, YearMonth yearMonth) {
            this(view, yearMonth, TextStyle.FULL);
        }

        /**
         * Constructs a new month header cell.
         *
         * @param view      the view where the header is needed
         * @param yearMonth the month to display
         * @param textStyle the text style for the month label (full, short, ...)
         */
        public MonthHeaderCell(MonthSheetView view, YearMonth yearMonth, TextStyle textStyle) {
            this.view = Objects.requireNonNull(view);
            this.yearMonth = Objects.requireNonNull(yearMonth);
            setText(yearMonth.getMonth().getDisplayName(textStyle, Locale.getDefault()));
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            setMinWidth(0);
        }

        /**
         * Returns the view where the cell is used.
         *
         * @return the view
         */
        public final MonthSheetView getView() {
            return view;
        }

        /**
         * Returns the month for which the cell is used.
         *
         * @return the month
         */
        public final YearMonth getYearMonth() {
            return yearMonth;
        }

    }

    /**
     * The base class for all date cells that are used in combination with the
     * {@link MonthSheetView}. The base class provides support for the pseudo states
     * "today" and "selected". The cell also adds several stylesheets based on the
     * location of the cell and based on the date or weekday represented by the cell.
     * <ul>
     * <li><b>weekend-day</b> - if the date is on a weekend (e.g. a "Saturday" or "Sunday").</li>
     * <li><b>extended-date-cell</b> - if the cell is located within an "extension" month.</li>
     * <li><b>first-day-of-week</b> - if the day shown by the cell is the first day of the week (e.g. "Monday" in Europe, "Sunday" in the US).</li>
     * </ul>
     * The cell also automatically adds the weekday name as a style class to the date ("monday", "tuesday", ...).
     */
    public static abstract class DateCell extends Region {

        private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");
        private static final PseudoClass PSEUDO_CLASS_TODAY = PseudoClass.getPseudoClass("today");

        private static final String CELL_STYLE_CLASS = "date-cell";
        private static final String EXTENDED_CELL_STYLE_CLASS = "extended-date-cell";
        private static final String WEEKEND_DAY = "weekend-day";
        private static final String FIRST_DAY_OF_WEEK = "first-day-of-week";

        private final LocalDate date;
        private final MonthSheetView view;

        private boolean selected;
        private boolean today;

        /**
         * Constructs a new date cell.
         *
         * @param view the parent month sheet view
         * @param date the date shown by the cell (might be null for leading or trailing empty cells)
         */
        public DateCell(MonthSheetView view, LocalDate date) {
            this.view = Objects.requireNonNull(view);
            this.date = date;

            setMaxWidth(Double.MAX_VALUE);
            getStyleClass().add(CELL_STYLE_CLASS);

            setFocusTraversable(true);

            applyStyles();
        }

        /**
         * Returns the month sheet view to which the cell belongs.
         *
         * @return the parent month sheet view
         */
        public final MonthSheetView getView() {
            return view;
        }

        /**
         * Returns the date shown by the cell or null if it does
         * not show any date (might be the case when used as a filler
         * cell at the beginning or end of the month.
         *
         * @return the date shown by the cell
         */
        public final LocalDate getDate() {
            return date;
        }

        private void applyStyles() {
            YearMonth yearMonth;

            if (date == null) {
                // date can be null if cell is used to fill the month column
                return;
            }

            yearMonth = YearMonth.from(date);

            if (view.isExtendedMonth(yearMonth)) {
                getStyleClass().add(EXTENDED_CELL_STYLE_CLASS);
            }

            WeekFields fields = view.getWeekFields();
            DayOfWeek firstDayOfWeek = fields.getFirstDayOfWeek();
            if (date.getDayOfWeek().equals(firstDayOfWeek)) {
                getStyleClass().add(FIRST_DAY_OF_WEEK);
            }

            if (view.getWeekendDays().contains(date.getDayOfWeek())) {
                if (!getStyleClass().contains(WEEKEND_DAY)) {
                    getStyleClass().add(0, WEEKEND_DAY);
                }
            } else {
                getStyleClass().remove(WEEKEND_DAY);
            }

            getStyleClass().add(date.getDayOfWeek().name().toLowerCase());
        }

        /**
         * Returns true if the cell is currently selected.
         *
         * @return true if the cell is selected
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Tells the cell whether it is currently selected or not.
         *
         * @param selected if true the cell will be considered "selected"
         * @see MonthSheetView#getDateSelectionModel()
         */
        public void setSelected(boolean selected) {
            /*
             * Intentionally not made a final method. Cells might show
             * different content when they are showing today or when they
             * are not.
             */
            this.selected = selected;
            pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, selected);
        }

        // Today support.

        /**
         * Returns true if the cell represents the day / date that is "today".
         *
         * @return true if the cell is showing "today"
         * @see DateControl#todayProperty()
         */
        public boolean isToday() {
            return today;
        }

        /**
         * Specifies whether or not the cell represents the day / date that is "today".
         * Applications are free to override this method but should always call
         * super.setToday().
         *
         * @param today true tells the cell that it is showing "today"
         * @see DateControl#todayProperty()
         */
        public void setToday(boolean today) {
            /*
             * Intentionally not made a final method. Cells might show
             * different content when they are showing today or when they
             * are not.
             */
            this.today = today;
            pseudoClassStateChanged(PSEUDO_CLASS_TODAY, today);
        }

        /**
         * This method gets invoked whenever the {@link MonthSheetView} determines
         * that the content of the cell might need to be refreshed. This might be the
         * case when new entries are created or existing entries are deleted. But also
         * when an entry gets assigned to a new calendar or an entry's time interval
         * has changed.
         *
         * @param entries the current list of entries for the day represented
         *                by the cell
         */
        public void updateEntries(List<Entry<?>> entries) {
        }
    }

    /**
     * A specialization of the {@link SimpleDateCell} that adds a small canvas on the
     * right-hand side to visualize the utilization of the day (the entries that exist on
     * that date).
     */
    public static class DetailedDateCell extends SimpleDateCell {

        private DetailCanvas canvas;

        private static final Map<String, Color> calendarColors = new HashMap<>();

        static {
            calendarColors.put(Style.STYLE1.name().toLowerCase(), Color.rgb(119, 192, 75, 0.8));
            calendarColors.put(Style.STYLE2.name().toLowerCase(), Color.rgb(65, 143, 203, 0.8));
            calendarColors.put(Style.STYLE3.name().toLowerCase(), Color.rgb(247, 209, 91, 0.8));
            calendarColors.put(Style.STYLE4.name().toLowerCase(), Color.rgb(157, 91, 159, 0.8));
            calendarColors.put(Style.STYLE5.name().toLowerCase(), Color.rgb(208, 82, 95, 0.8));
            calendarColors.put(Style.STYLE6.name().toLowerCase(), Color.rgb(249, 132, 75, 0.8));
            calendarColors.put(Style.STYLE7.name().toLowerCase(), Color.rgb(174, 102, 62, 0.8));
        }

        /**
         * Constructs a new detailed date cell.
         *
         * @param view the parent month sheet view
         * @param date the date shown by the cell (might be null for leading or trailing empty cells)
         */
        public DetailedDateCell(MonthSheetView view, LocalDate date) {
            super(view, date);

            canvas = new DetailCanvas();
            canvas.setMouseTransparent(true);

            getChildren().add(canvas);
        }

        /**
         * Returns the color to be used for the given calendar style.
         *
         * @param style the calendar style
         * @return the color to be used for the given calendar
         * @see Calendar#setStyle(String)
         */
        public static final Color getCalendarColor(String style) {
            return calendarColors.get(style);
        }

        /**
         * Sets the color to be used for the given calendar style.
         *
         * @param style the calendar style
         * @param color the color
         */
        public static final void setCalendarColor(String style, Color color) {
            calendarColors.put(style, color);
        }

        @Override
        protected void layoutChildren() {
            Insets insets = getInsets();

            double top = insets.getTop();
            double bottom = insets.getBottom();
            double left = insets.getLeft();
            double right = insets.getRight();

            double w = getWidth();
            double h = getHeight();

            double availableHeight = h - top - bottom;

            double ps1 = dayOfMonthLabel.prefWidth(-1);
            double ps2 = dayOfWeekLabel.prefWidth(-1);
            double ps3 = weekNumberLabel.prefWidth(-1);
            double ps4 = 12; // width of canvas

            dayOfMonthLabel.resizeRelocate(left, top, ps1, availableHeight);
            dayOfWeekLabel.resizeRelocate(left + ps1, top, ps2, availableHeight);
            weekNumberLabel.resizeRelocate(w - right - ps3 - ps4, top, ps3, availableHeight);

            // canvas
            canvas.resizeRelocate(w - right - ps4, top, ps4, availableHeight);
            canvas.setWidth(ps4);
            canvas.setHeight(availableHeight);
            canvas.draw();
        }

        @Override
        protected double computePrefWidth(double height) {
            return dayOfMonthLabel.prefWidth(-1) + dayOfWeekLabel.prefWidth(-1) + weekNumberLabel.prefWidth(-1) + 12;
        }

        /*
         * Had to override because for some weird reason selected date cells were higher than deselected once.
         */
        @Override
        protected double computePrefHeight(double width) {
            // for the height we do not care about the canvas
            double h = Math.max(dayOfMonthLabel.prefHeight(-1), Math.max(dayOfWeekLabel.prefHeight(-1), weekNumberLabel.prefHeight(-1)));
            return h + getInsets().getTop() + getInsets().getBottom();
        }

        @Override
        public void updateEntries(List<Entry<?>> entries) {
            canvas.setEntries(entries);
        }

        private final class DetailCanvas extends Canvas {

            private List<Entry<?>> entries;

            private DetailCanvas() {
                getStyleClass().add("detail-canvas");
                draw();
            }

            @Override
            public boolean isResizable() {
                return true;
            }

            public void setEntries(List<Entry<?>> entries) {
                this.entries = entries;
                draw();
            }

            public void draw() {
                final double width = getWidth();
                final double height = getHeight();

                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, width, height);

                if (entries != null && !entries.isEmpty()) {
                    for (Entry<?> entry : entries) {
                        com.calendarfx.model.Calendar calendar = entry.getCalendar();
                        if (calendar == null) {
                            continue;
                        }

                        Color color = getCalendarColor(calendar.getStyle());
                        gc.setFill(color);

                        if (entry.isFullDay()) {
                            gc.fillRect(0, 0, width, height);
                        } else {
                            LocalTime startTime = entry.getStartTime();
                            LocalTime endTime = entry.getEndTime();

                            if (entry.getStartDate().isBefore(getDate())) {
                                startTime = LocalTime.MIN;
                            }

                            if (entry.getEndDate().isAfter(getDate())) {
                                endTime = LocalTime.MAX;
                            }

                            double y = height * (startTime.toSecondOfDay() / (double) LocalTime.MAX.toSecondOfDay());
                            double h = height * (endTime.toSecondOfDay() / (double) LocalTime.MAX.toSecondOfDay());
                            gc.fillRect(0, y, width, h - y);
                        }
                    }
                }
            }
        }
    }

    /**
     * The badge date cell extends the {@link SimpleDateCell} and adds another
     * label to it that is used to display a counter of the number of entries that
     * exist on that date.
     */
    public static class BadgeDateCell extends SimpleDateCell {

        private Label counterLabel;

        /**
         * Constructs a new badge date cell.
         *
         * @param view the parent month sheet view
         * @param date the date shown by the cell (might be null for leading or trailing empty cells)
         */
        public BadgeDateCell(MonthSheetView view, LocalDate date) {
            super(view, date);

            getStyleClass().add("badge-date-cell");

            counterLabel = new Label();
            counterLabel.getStyleClass().add("badge-label");
            counterLabel.setAlignment(Pos.CENTER_RIGHT);
            counterLabel.setVisible(false); // has to be initially invisible (to work with empty cells)
            getChildren().add(counterLabel);

            // this cell type can not display week numbers
            weekNumberLabel.setVisible(false);
        }

        @Override
        protected void layoutChildren() {
            Insets insets = getInsets();

            double top = insets.getTop();
            double bottom = insets.getBottom();
            double left = insets.getLeft();
            double right = insets.getRight();

            double w = getWidth();
            double h = getHeight();

            double availableHeight = h - top - bottom;

            double ps1 = dayOfMonthLabel.prefWidth(-1);
            double ps2 = dayOfWeekLabel.prefWidth(-1);
            double ps4 = counterLabel.prefWidth(-1);

            double ph = counterLabel.prefHeight(-1);

            dayOfMonthLabel.resizeRelocate(left, top, ps1, availableHeight);
            dayOfWeekLabel.resizeRelocate(left + ps1, top, ps2, availableHeight);

            // center the counter label, do not let it use the entire height
            counterLabel.resizeRelocate(w - right - ps4, top + availableHeight / 2 - ph / 2, ps4, Math.min(availableHeight, ph));
        }

        @Override
        protected double computePrefWidth(double height) {
            return dayOfMonthLabel.prefWidth(-1) + dayOfWeekLabel.prefWidth(-1) + weekNumberLabel.prefWidth(-1);
        }

        /*
         * Had to override because for some weird reason selected date cells were higher than deselected once.
         */
        @Override
        protected double computePrefHeight(double width) {
            double h = Math.max(counterLabel.prefHeight(-1), Math.max(dayOfMonthLabel.prefHeight(-1), Math.max(dayOfWeekLabel.prefHeight(-1), weekNumberLabel.prefHeight(-1))));
            return h + getInsets().getTop() + getInsets().getBottom();
        }

        @Override
        public void updateEntries(List<Entry<?>> entries) {
            super.updateEntries(entries);

            int entryCount = 0;
            if (entries != null) {
                entryCount = entries.size();
            }

            if (entryCount > 0) {
                counterLabel.setText(Integer.toString(entries.size()));
                counterLabel.setVisible(true);
            } else {
                counterLabel.setVisible(false);
            }

            counterLabel.getStyleClass().removeAll(USAGE_VERY_LOW, USAGE_LOW, USAGE_MEDIUM, USAGE_HIGH, USAGE_VERY_HIGH);

            final Callback<Integer, DateControl.Usage> usagePolicy = getView().getUsagePolicy();

            switch (usagePolicy.call(entryCount)) {
                case NONE:
                    break;
                case VERY_LOW:
                    counterLabel.getStyleClass().add(USAGE_VERY_LOW);
                    break;
                case LOW:
                    counterLabel.getStyleClass().add(USAGE_LOW);
                    break;
                case MEDIUM:
                    counterLabel.getStyleClass().add(USAGE_MEDIUM);
                    break;
                case HIGH:
                    counterLabel.getStyleClass().add(USAGE_HIGH);
                    break;
                case VERY_HIGH:
                default:
                    counterLabel.getStyleClass().add(USAGE_VERY_HIGH);
                    break;
            }
        }
    }

    /**
     * A date cell used to display the day of month, the day of week, and the week of year in
     * three separate labels. The styles used for these labels are "day-of-month", "day-of-week",
     * and "week-number".
     */
    public static class SimpleDateCell extends DateCell {

        private static final String DAY_OF_MONTH_STYLE = "day-of-month-label";
        private static final String DAY_OF_WEEK_STYLE = "day-of-week-label";
        private static final String WEEK_NUMBER_STYLE = "week-number-label";

        protected final Label dayOfMonthLabel = new Label();
        protected final Label dayOfWeekLabel = new Label();
        protected final Label weekNumberLabel = new Label();

        /**
         * Constructs a new simple date cell.
         *
         * @param view the parent month sheet view
         * @param date the date shown by the cell (might be null for leading or trailing empty cells)
         */
        public SimpleDateCell(MonthSheetView view, LocalDate date) {
            super(view, date);

            dayOfMonthLabel.getStyleClass().add(DAY_OF_MONTH_STYLE);
            dayOfWeekLabel.getStyleClass().add(DAY_OF_WEEK_STYLE);
            weekNumberLabel.getStyleClass().add(WEEK_NUMBER_STYLE);

            dayOfMonthLabel.setManaged(false);
            dayOfWeekLabel.setManaged(false);
            weekNumberLabel.setManaged(false);

            dayOfMonthLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayOfWeekLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            weekNumberLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            dayOfMonthLabel.setMouseTransparent(!view.isEnableHyperlinks());
            dayOfWeekLabel.setMouseTransparent(true);
            weekNumberLabel.setMouseTransparent(true);

            if (date != null) {
                String dayOfWeekName = date.getDayOfWeek().name().toLowerCase();
                dayOfMonthLabel.getStyleClass().add(dayOfWeekName + "-label");
                dayOfWeekLabel.getStyleClass().add(dayOfWeekName + "-label");
                weekNumberLabel.getStyleClass().add(dayOfWeekName + "-label");
            }

            getChildren().addAll(dayOfMonthLabel, dayOfWeekLabel, weekNumberLabel);

            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            updateLabels();

            if (view.isEnableHyperlinks()) {
                dayOfMonthLabel.getStyleClass().add("date-hyperlink");
                dayOfMonthLabel.setOnMouseClicked(evt -> {
                    if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 1) {
                        fireEvent(new RequestEvent(this, this, date));
                    }
                });
            }
        }

        @Override
        protected void layoutChildren() {
            Insets insets = getInsets();

            double top = insets.getTop();
            double bottom = insets.getBottom();
            double left = insets.getLeft();
            double right = insets.getRight();

            double w = getWidth();
            double h = getHeight();

            double availableHeight = h - top - bottom;

            double ps1 = dayOfMonthLabel.prefWidth(-1);
            double ps2 = dayOfWeekLabel.prefWidth(-1);
            double ps3 = weekNumberLabel.prefWidth(-1);

            dayOfMonthLabel.resizeRelocate(snapPosition(left), snapPosition(top), snapSize(ps1), snapSize(availableHeight));
            dayOfWeekLabel.resizeRelocate(snapPosition(left + ps1), snapPosition(top), snapSize(ps2), snapSize(availableHeight));
            weekNumberLabel.resizeRelocate(snapPosition(w - right - ps3), snapPosition(top), snapSize(ps3), snapSize(availableHeight));
        }

        @Override
        protected double computePrefWidth(double height) {
            return dayOfMonthLabel.prefWidth(-1) + dayOfWeekLabel.prefWidth(-1) + weekNumberLabel.prefWidth(-1);
        }

        /*
         * Had to override because for some weird reason selected date cells were higher than deselected once.
         */
        @Override
        protected double computePrefHeight(double width) {
            double h = Math.max(dayOfMonthLabel.prefHeight(-1), Math.max(dayOfWeekLabel.prefHeight(-1), weekNumberLabel.prefHeight(-1)));
            return h + getInsets().getTop() + getInsets().getBottom();
        }

        private void updateLabels() {
            LocalDate date = getDate();
            if (date == null) {
                dayOfMonthLabel.setText("");
                dayOfWeekLabel.setText("");
                weekNumberLabel.setText("");
            } else {
                dayOfMonthLabel.setText(String.valueOf(date.getDayOfMonth()));
                dayOfWeekLabel.setText(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));

                MonthSheetView view = getView();
                if (view != null && view.isShowWeekNumber()) {
                    WeekFields fields = view.getWeekFields();
                    DayOfWeek firstDayOfWeek = fields.getFirstDayOfWeek();
                    if (date.getDayOfWeek().equals(firstDayOfWeek)) {
                        String weekNumber = Integer.toString(date.get(fields.weekOfYear()));
                        weekNumberLabel.setText(weekNumber);
                    }
                }
            }
        }
    }

    /**
     * A specialization of the {@link SimpleDateCell} that adds utilization information
     * to the cell by coloring its background differently based on the number of entries
     * found on that day. The cell uses the "usage policy" of the month sheet view to
     * determine whether the utilization is low or high. The styles used for visualizing
     * the different utilizations are "usage-very-low", "usage-low", "usage-medium",
     * "usage-high", and "usage-very-high". If utilization is zero then none of these styles
     * will be applied.
     *
     * @see DateControl#usagePolicyProperty()
     */
    public static class UsageDateCell extends SimpleDateCell {

        /**
         * Constructs a new usage date cell.
         *
         * @param view the parent month sheet view
         * @param date the date shown by the cell (might be null for leading or trailing empty cells)
         */
        public UsageDateCell(MonthSheetView view, LocalDate date) {
            super(view, date);
        }

        @Override
        public void updateEntries(List<Entry<?>> entries) {

            getStyleClass().removeAll(USAGE_VERY_LOW, USAGE_LOW, USAGE_MEDIUM, USAGE_HIGH, USAGE_VERY_HIGH);

            int entryCount = 0;
            if (entries != null) {
                entryCount = entries.size();
            }

            final Callback<Integer, DateControl.Usage> usagePolicy = getView().getUsagePolicy();

            switch (usagePolicy.call(entryCount)) {
                case NONE:
                    break;
                case VERY_LOW:
                    getStyleClass().add(USAGE_VERY_LOW);
                    break;
                case LOW:
                    getStyleClass().add(USAGE_LOW);
                    break;
                case MEDIUM:
                    getStyleClass().add(USAGE_MEDIUM);
                    break;
                case HIGH:
                    getStyleClass().add(USAGE_HIGH);
                    break;
                case VERY_HIGH:
                default:
                    getStyleClass().add(USAGE_VERY_HIGH);
                    break;
            }
        }
    }
}
