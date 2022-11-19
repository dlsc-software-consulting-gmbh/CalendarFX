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
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarFXControl;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.CalendarView.Page;
import com.calendarfx.view.DeveloperConsole;
import com.calendarfx.view.Messages;
import com.calendarfx.view.RequestEvent;
import com.calendarfx.view.SearchResultView;
import com.calendarfx.view.SourceView;
import com.calendarfx.view.YearMonthView;
import com.calendarfx.view.page.PageBase;
import com.calendarfx.view.popover.ZoneIdComparator;
import com.calendarfx.view.print.PrintView;
import com.calendarfx.view.print.PrintablePage;
import com.calendarfx.view.print.ViewType;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.SortedList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.ZoneId;
import java.util.function.Consumer;

import static com.calendarfx.view.CalendarView.Page.DAY;
import static com.calendarfx.view.CalendarView.Page.MONTH;
import static com.calendarfx.view.CalendarView.Page.WEEK;
import static com.calendarfx.view.CalendarView.Page.YEAR;
import static com.calendarfx.view.RequestEvent.REQUEST_DATE;
import static com.calendarfx.view.RequestEvent.REQUEST_DATE_TIME;
import static com.calendarfx.view.RequestEvent.REQUEST_ENTRY;
import static com.calendarfx.view.RequestEvent.REQUEST_WEEK;
import static com.calendarfx.view.RequestEvent.REQUEST_YEAR;
import static com.calendarfx.view.RequestEvent.REQUEST_YEAR_MONTH;
import static com.calendarfx.view.YearMonthView.ClickBehaviour.PERFORM_SELECTION;
import static javafx.geometry.Side.RIGHT;
import static javafx.scene.control.SelectionMode.SINGLE;

public class CalendarViewSkin extends SkinBase<CalendarView> {

    private final MasterDetailPane leftMasterDetailPane;
    private final ToggleButton trayButton;
    private final Button addCalendarButton;
    private final Button printButton;
    private final SearchResultView searchResultView;
    private final StackPane stackPane;

    private final ToggleButton showYear;
    private final ToggleButton showMonth;
    private final ToggleButton showWeek;
    private final ToggleButton showDay;
    private final HBox leftToolBarBox;
    private final SegmentedButton switcher;

    private SourceView sourceView;

    private final InvalidationListener entriesVisibilityListener = obs -> updateCalendarVisibility();
    private final InvalidationListener weakEntriesVisibilityListener = new WeakInvalidationListener(entriesVisibilityListener);

    private final InvalidationListener printEntriesVisibilityListener = obs -> updatePrintVisibility();
    private final InvalidationListener weakPrintEntriesVisibilityListener = new WeakInvalidationListener(printEntriesVisibilityListener);

    public CalendarViewSkin(CalendarView view) {
        super(view);

        if (Boolean.getBoolean("calendarfx.developer")) {
            view.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
                if (evt.isMetaDown() && evt.getCode().equals(KeyCode.D)) {
                    view.setShowDeveloperConsole(
                            !view.isShowDeveloperConsole());
                }
            });
        }

        view.addEventHandler(REQUEST_DATE, evt -> maybeRunAndConsume(evt, e -> view.showDate(evt.getDate())));
        view.addEventHandler(REQUEST_DATE_TIME, evt -> maybeRunAndConsume(evt, e -> view.showDateTime(evt.getDateTime())));
        view.addEventHandler(REQUEST_WEEK, evt -> maybeRunAndConsume(evt, e -> view.showWeek(evt.getYear(), evt.getWeekOfYear())));
        view.addEventHandler(REQUEST_YEAR_MONTH, evt -> maybeRunAndConsume(evt, e -> view.showYearMonth(evt.getYearMonth())));
        view.addEventHandler(REQUEST_YEAR, evt -> maybeRunAndConsume(evt, e -> view.showYear(evt.getYear())));
        view.addEventHandler(REQUEST_ENTRY, evt -> maybeRunAndConsume(evt, e -> view.getSelectedPageView().editEntry(evt.getEntry())));

        view.getAvailablePages().addListener((Observable it) -> buildSwitcher());

        TrayPane trayPane = new TrayPane();
        //this.trayButton = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SOURCE_TRAY"));
        this.trayButton = new ToggleButton("Menu");
        this.trayButton.setId("show-tray-button");
        this.trayButton.setMaxHeight(Double.MAX_VALUE);

        FontIcon addIcon = new FontIcon(FontAwesome.PLUS);
        addIcon.getStyleClass().addAll("button-icon", "add-calendar-button-icon");

        this.addCalendarButton = new Button();
        this.addCalendarButton.setId("add-calendar-button");
        this.addCalendarButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.addCalendarButton.setMaxHeight(Double.MAX_VALUE);
        this.addCalendarButton.setGraphic(addIcon);
        this.addCalendarButton.setOnAction(evt -> view.createCalendarSource());

        FontIcon printIcon = new FontIcon(FontAwesome.PRINT);
        printIcon.getStyleClass().addAll("button-icon", "print-button-icon");

        this.printButton = new Button();
        this.printButton.setId("print-button");
        this.printButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.printButton.setOnAction(evt -> print());
        this.printButton.setMaxHeight(Double.MAX_VALUE);
        this.printButton.setGraphic(printIcon);

        this.leftMasterDetailPane = new MasterDetailPane(Side.LEFT);

        if (view.isShowSourceTray()) {
            openTray();
        } else {
            closeTray();
        }

        Bindings.bindBidirectional(trayButton.selectedProperty(), view.showSourceTrayProperty());

        view.showSourceTrayProperty().addListener(it -> {
            if (view.isShowSourceTray()) {
                openTray();
            } else {
                closeTray();
            }
        });

        Platform.runLater(() -> sourceView.getCalendarVisibilityMap().keySet().forEach(calendar -> sourceView.getCalendarVisibilityProperty(calendar).addListener(weakEntriesVisibilityListener)));

        view.selectedPageProperty().addListener(weakEntriesVisibilityListener);

        ColumnConstraints leftColumn = new ColumnConstraints();
        ColumnConstraints centerColumn = new ColumnConstraints();
        ColumnConstraints rightColumn = new ColumnConstraints();

        leftColumn.setPercentWidth(35);
        centerColumn.setPercentWidth(30);
        rightColumn.setPercentWidth(35);

        GridPane toolBarGridPane = new GridPane();
        toolBarGridPane.setMinWidth(0);
        toolBarGridPane.getColumnConstraints().addAll(leftColumn, centerColumn, rightColumn);
        toolBarGridPane.setId("toolbar");
        toolBarGridPane.getStyleClass().add("tool-bar");

        /*
         * Toolbar box on the left - hand side.Gets rebuild when some of the
         * control 's properties change.
         */
        leftToolBarBox = new HBox();
        leftToolBarBox.getStyleClass().add("left-toolbar-container");
        leftToolBarBox.setSpacing(5);

        buildLeftToolBarBox();

        InvalidationListener buildLeftToolBarBoxListener = it -> buildLeftToolBarBox();
        view.showSourceTrayButtonProperty().addListener(buildLeftToolBarBoxListener);
        view.showAddCalendarButtonProperty().addListener(buildLeftToolBarBoxListener);
        view.showPrintButtonProperty().addListener(buildLeftToolBarBoxListener);
        view.showPageToolBarControlsProperty().addListener(buildLeftToolBarBoxListener);
        view.selectedPageProperty().addListener(buildLeftToolBarBoxListener);

        toolBarGridPane.add(leftToolBarBox, 0, 0);

        // toolbar center
        showDay = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SHOW_DAY"));
        showWeek = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SHOW_WEEK"));
        showMonth = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SHOW_MONTH"));
        showYear = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SHOW_YEAR"));

        showDay.setOnAction(evt -> {
            view.showDayPage();
            updateToggleButtons();
        });

        showWeek.setOnAction(evt -> {
            view.showWeekPage();
            updateToggleButtons();
        });

        showMonth.setOnAction(evt -> {
            view.showMonthPage();
            updateToggleButtons();
        });

        showYear.setOnAction(evt -> {
            view.showYearPage();
            updateToggleButtons();
        });

        switcher = new SegmentedButton();
        switcher.setId("switcher");
        switcher.visibleProperty().bind(view.showPageSwitcherProperty());
        buildSwitcher();

        GridPane centerToolBarBox = new GridPane();
        GridPane.setHalignment(switcher, HPos.CENTER);
        GridPane.setValignment(switcher, VPos.CENTER);
        centerToolBarBox.add(switcher, 0, 0);
        centerToolBarBox.setAlignment(Pos.CENTER);
        toolBarGridPane.add(centerToolBarBox, 1, 0);

        // tooltips
        trayButton.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SOURCE_TRAY")));
        addCalendarButton.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_ADD_CALENDAR")));
        printButton.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_PRINT")));
        showDay.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SHOW_DAY")));
        showWeek.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SHOW_WEEK")));
        showMonth.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SHOW_MONTH")));
        showYear.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SHOW_YEAR")));

        // toolbar right
        FontIcon searchIcon = new FontIcon(FontAwesome.SEARCH);
        searchIcon.setId("search-icon");

        SortedList<ZoneId> sortedZones = new SortedList<>(view.getAvailableZoneIds());
        sortedZones.setComparator(new ZoneIdComparator());

        ChoiceBox<ZoneId> zoneIdBox = new ChoiceBox<>();
        zoneIdBox.setItems(sortedZones);
        zoneIdBox.valueProperty().bindBidirectional(view.zoneIdProperty());
        zoneIdBox.setConverter(new ZoneIdStringConverter());
        zoneIdBox.visibleProperty().bind(view.enableTimeZoneSupportProperty());
        zoneIdBox.managedProperty().bind(view.enableTimeZoneSupportProperty());

        CustomTextField searchField = view.getSearchField();
        searchField.setPrefColumnCount(20);
        searchField.setLeft(searchIcon);
        searchField.setId("search-field");
        searchField.setPromptText(Messages.getString("CalendarViewSkin.PROMPT_SEARCH_FIELD"));
        searchField.getStylesheets().add(CalendarFXControl.class.getResource("calendar.css").toExternalForm());
        GridPane.setFillWidth(searchField, false);
        GridPane.setHalignment(searchField, HPos.RIGHT);

        //HBox rightToolbarContainer = new HBox(zoneIdBox, searchField);
        HBox rightToolbarContainer = new HBox(zoneIdBox);
        rightToolbarContainer.setAlignment(Pos.CENTER_RIGHT);
        rightToolbarContainer.getStyleClass().add("right-toolbar-container");
        //toolBarGridPane.add(rightToolbarContainer, 2, 0);

        BorderPane borderPane1 = new BorderPane();
        borderPane1.topProperty().bind(view.headerProperty());
        borderPane1.setCenter(stackPane = new StackPane());
        borderPane1.bottomProperty().bind(view.footerProperty());

        view.selectedPageProperty().addListener(it -> changePage());

        leftMasterDetailPane.setMasterNode(borderPane1);
        leftMasterDetailPane.setDetailNode(trayPane);
        leftMasterDetailPane.setId("tray-pane");
        leftMasterDetailPane.animatedProperty().bindBidirectional(view.traysAnimatedProperty());
        leftMasterDetailPane.getStylesheets().add(CalendarFXControl.class.getResource("calendar.css").toExternalForm());

        MasterDetailPane rightMasterDetailPane = new MasterDetailPane(RIGHT);
        searchResultView = view.getSearchResultView();

        Bindings.bindContentBidirectional(searchResultView.getCalendarSources(), view.getCalendarSources());

        searchResultView.zoneIdProperty().bind(view.zoneIdProperty());
        searchResultView.searchTextProperty().bind(searchField.textProperty());
        searchResultView.selectedEntryProperty().addListener(evt -> showSelectedSearchResult());

        view.showSearchResultsTrayProperty().bind(Bindings.not(Bindings.isEmpty(searchResultView.getSearchResults())));

        rightMasterDetailPane.setDetailNode(searchResultView);
        rightMasterDetailPane.setMasterNode(leftMasterDetailPane);
        rightMasterDetailPane.showDetailNodeProperty().bind(view.showSearchResultsTrayProperty());

        BorderPane borderPane = new BorderPane();

        if (view.isShowToolBar()) {
            borderPane.setTop(toolBarGridPane);
        }

        view.showToolBarProperty().addListener(it -> {
            if (view.isShowToolBar()) {
                borderPane.setTop(toolBarGridPane);
            } else {
                borderPane.setTop(null);
            }
        });

        borderPane.setCenter(rightMasterDetailPane);

        if (Boolean.getBoolean("calendarfx.developer")) {
            DeveloperConsole developerConsole = view.getDeveloperConsole();
            MasterDetailPane developerConsoleMasterDetailPane = new MasterDetailPane(Side.BOTTOM);
            developerConsoleMasterDetailPane.setDividerPosition(.6);
            developerConsoleMasterDetailPane.animatedProperty().bind(view.traysAnimatedProperty());
            developerConsoleMasterDetailPane.getStyleClass().add("developer-master-detail-pane");
            developerConsoleMasterDetailPane.setDetailSide(Side.BOTTOM);
            developerConsoleMasterDetailPane.setMasterNode(borderPane);
            developerConsoleMasterDetailPane.setDetailNode(developerConsole);
            developerConsoleMasterDetailPane.setShowDetailNode(true);
            developerConsoleMasterDetailPane.showDetailNodeProperty().bind(view.showDeveloperConsoleProperty());
            developerConsoleMasterDetailPane.getStylesheets().add(CalendarFXControl.class.getResource("calendar.css").toExternalForm());
            getChildren().add(developerConsoleMasterDetailPane);
        } else {
            getChildren().add(borderPane);
        }

        final PageBase selectedPage = view.getSelectedPageView();
        selectedPage.toFront();

        updateToggleButtons();
    }

    private void maybeRunAndConsume(RequestEvent evt, Consumer<RequestEvent> consumer) {
        if (!evt.isConsumed()) {
            consumer.accept(evt);
            evt.consume();
        }
    }

    private void openTray() {
        leftMasterDetailPane.resetDividerPosition();
        leftMasterDetailPane.setShowDetailNode(true);
    }

    private void closeTray() {
        leftMasterDetailPane.setShowDetailNode(false);
    }

    /**
     * Refresh entries from specific page <b>(Selected Page)</b>. It is called
     * after change selected Page (ButtonSwitcher) or check/uncheck any
     * CalendarSource.
     */
    private void updateCalendarVisibility() {
        CalendarView view = getSkinnable();

        if (view.getSelectedPage().equals(DAY)) {
            view.getDayPage().refreshData();
        } else if (view.getSelectedPage().equals(WEEK)) {
            view.getWeekPage().refreshData();
        }
    }

    /**
     * Refresh entries in <b>PrintablePage</b>. It is called after change type
     * of view or check/uncheck any CalendarSource in Print dialog.
     */
    private void updatePrintVisibility() {
        PrintablePage printablePage = printView.getPreviewPane().getPrintablePage();

        if (printablePage.getViewType() == ViewType.DAY_VIEW) {
            printablePage.getDayView().refreshData();
        } else if (printablePage.getViewType() == ViewType.WEEK_VIEW) {
            printablePage.getWeekView().refreshData();
        }
    }

    private void buildSwitcher() {
        CalendarView view = getSkinnable();
        switcher.getButtons().clear();
        if (view.getAvailablePages().contains(DAY)) {
            switcher.getButtons().add(showDay);
        }
       // if (view.getAvailablePages().contains(WEEK)) {
      //      switcher.getButtons().add(showWeek);
      //  }
        if (view.getAvailablePages().contains(MONTH)) {
            switcher.getButtons().add(showMonth);
        }
      //  if (view.getAvailablePages().contains(YEAR)) {
       //     switcher.getButtons().add(showYear);
       // }

        // no need to show anything if there is only one page left
        if (switcher.getButtons().size() == 1) {
            switcher.getButtons().clear();
        }
    }

    private void buildLeftToolBarBox() {
        leftToolBarBox.getChildren().clear();

        if (getSkinnable().isShowSourceTrayButton()) {
            leftToolBarBox.getChildren().add(trayButton);
        }

        if (getSkinnable().isShowAddCalendarButton()) {
            leftToolBarBox.getChildren().add(addCalendarButton);
        }

        if (!leftToolBarBox.getChildren().isEmpty() && getSkinnable().isShowPrintButton()) {
            leftToolBarBox.getChildren().add(new Separator(Orientation.VERTICAL));
        }

        //if (getSkinnable().isShowPrintButton()) {
          //  leftToolBarBox.getChildren().add(printButton);
        //}

        if (getSkinnable().isShowPageToolBarControls()) {
            Page page = getSkinnable().getSelectedPage();
            Node toolBarControls = getSkinnable().getPageView(page).getToolBarControls();

            if (toolBarControls != null && !((toolBarControls instanceof Pane) && ((Pane) toolBarControls).getChildrenUnmodifiable().isEmpty())) {
                if (!leftToolBarBox.getChildren().isEmpty()) {
                    leftToolBarBox.getChildren().add(new Separator(Orientation.VERTICAL));
                }
                leftToolBarBox.getChildren().add(toolBarControls);
            }
        }
    }

    private void changePage() {
        CalendarView view = getSkinnable();
        updateToggleButtons();
        stackPane.getChildren().setAll(view.getSelectedPageView());
    }


    private void updateToggleButtons() {
        CalendarView view = getSkinnable();
        Page page = view.getSelectedPage();
        if (page.equals(DAY)) {
            showDay.setSelected(true);
        } else if (page.equals(WEEK)) {
            showWeek.setSelected(true);
        } else if (page.equals(MONTH)) {
            showMonth.setSelected(true);
        } else if (page.equals(YEAR)) {
            showYear.setSelected(true);
        }

        stackPane.getChildren().setAll(view.getSelectedPageView());
    }

    private void showSelectedSearchResult() {
        Entry<?> result = searchResultView.getSelectedEntry();
        if (result != null) {
            getSkinnable().showEntry(result);
        }
    }

    class TrayPane extends BorderPane {

        private final YearMonthView yearMonthView;

        public TrayPane() {
            // source view
            sourceView = getSkinnable().getSourceView();
            sourceView.bind(getSkinnable());

            // year month view
            yearMonthView = getSkinnable().getYearMonthView();
            yearMonthView.setShowToday(true);
            yearMonthView.setShowTodayButton(false);
            yearMonthView.setId("date-picker");
            yearMonthView.setSelectionMode(SINGLE);
            yearMonthView.setClickBehaviour(PERFORM_SELECTION);
            yearMonthView.getSelectedDates().add(getSkinnable().getDate());
            yearMonthView.getSelectedDates().addListener((Observable evt) -> {
                if (!yearMonthView.getSelectedDates().isEmpty()) {
                    yearMonthView.setDate(yearMonthView.getSelectedDates().iterator().next());
                }
            });

            getSkinnable().dateProperty().addListener(it -> {
                yearMonthView.getSelectedDates().clear();
                yearMonthView.getSelectedDates().add(getSkinnable().getDate());
            });

            Bindings.bindBidirectional(yearMonthView.todayProperty(), getSkinnable().todayProperty());
            Bindings.bindBidirectional(yearMonthView.dateProperty(), getSkinnable().dateProperty());
            yearMonthView.weekFieldsProperty().bind(getSkinnable().weekFieldsProperty());

            ScrollPane scrollPane = new ScrollPane(sourceView);

            scrollPane.getStyleClass().add("source-view-scroll-pane");
            setCenter(scrollPane);
            setBottom(yearMonthView);
        }
    }

    private PrintView printView;

    private void print() {
        if (printView == null) {
            printView = getSkinnable().getPrintView();
            printView.dateProperty().bind(getSkinnable().dateProperty());
            printView.zoneIdProperty().bind(getSkinnable().zoneIdProperty());
        }

        printView.setToday(getSkinnable().getToday());
        printView.getPreviewPane().getPrintablePage().setPageStartDate(getSkinnable().getDate());

        printView.setWeekFields(getSkinnable().getWeekFields());
        printView.getCalendarSources().setAll(getSkinnable().getCalendarSources());
        printView.setLayout(getSkinnable().getSelectedPageView().getLayout());
        printView.setViewType(getSkinnable().getSelectedPageView().getPrintViewType());
        printView.loadDropDownValues(getSkinnable().getDate());

        printView.show(getSkinnable().getScene().getWindow());

        Platform.runLater(() -> {

            SourceView printSource = printView.getSettingsView().getSourceView();

            for (Calendar calendar : printSource.getCalendarVisibilityMap().keySet()) {
                printSource.getCalendarVisibilityProperty(calendar).removeListener(weakPrintEntriesVisibilityListener);
                printSource.getCalendarVisibilityProperty(calendar).addListener(weakPrintEntriesVisibilityListener);
            }

        });
    }
}
