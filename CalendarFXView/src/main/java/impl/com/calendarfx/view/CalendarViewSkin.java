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

import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarFXControl;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DeveloperConsole;
import com.calendarfx.view.Messages;
import com.calendarfx.view.SearchResultView;
import com.calendarfx.view.SourceView;
import com.calendarfx.view.YearMonthView;
import com.calendarfx.view.page.DayPage;
import com.calendarfx.view.page.MonthPage;
import com.calendarfx.view.page.PageBase;
import com.calendarfx.view.page.WeekPage;
import com.calendarfx.view.page.YearPage;
import com.calendarfx.view.print.PrintView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.textfield.CustomTextField;

import java.util.ArrayList;
import java.util.List;

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

    private MasterDetailPane leftMasterDetailPane;
    private ToggleButton trayButton;
    private Button addCalendarButton;
    private Button printButton;
    private SearchResultView searchResultView;
    private StackPane stackPane;

    private DayPage dayPage;
    private WeekPage weekPage;
    private MonthPage monthPage;
    private YearPage yearPage;

    private List<PageBase> pageList = new ArrayList<>();
    private ToggleButton showYear;
    private ToggleButton showMonth;
    private ToggleButton showWeek;
    private ToggleButton showDay;
    private HBox leftToolBarBox;
    private SegmentedButton switcher;

    public CalendarViewSkin(CalendarView view) {
        super(view);

        if (Boolean.getBoolean("calendarfx.developer")) { //$NON-NLS-1$
            view.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
                if (evt.isMetaDown() && evt.getCode().equals(KeyCode.D)) {
                    view.setShowDeveloperConsole(
                            !view.isShowDeveloperConsole());
                }
            });
        }

        view.addEventHandler(REQUEST_DATE, evt -> view.showDate(evt.getDate()));
        view.addEventHandler(REQUEST_DATE_TIME, evt -> view.showDateTime(evt.getDateTime()));
        view.addEventHandler(REQUEST_WEEK, evt -> view.showWeek(evt.getYear(), evt.getWeekOfYear()));
        view.addEventHandler(REQUEST_YEAR_MONTH, evt -> view.showYearMonth(evt.getYearMonth()));
        view.addEventHandler(REQUEST_YEAR, evt -> view.showYear(evt.getYear()));
        view.addEventHandler(REQUEST_ENTRY, evt -> view.getSelectedPage().editEntry(evt.getEntry()));

        this.dayPage = view.getDayPage();
        this.weekPage = view.getWeekPage();
        this.monthPage = view.getMonthPage();
        this.yearPage = view.getYearPage();

        this.pageList.add(dayPage);
        this.pageList.add(weekPage);
        this.pageList.add(monthPage);
        this.pageList.add(yearPage);

        view.bind(dayPage, true);
        view.bind(weekPage, true);
        view.bind(monthPage, true);
        view.bind(yearPage, true);

        InvalidationListener updateSwitcherListener = it -> buildSwitcher();
        dayPage.hiddenProperty().addListener(updateSwitcherListener);
        weekPage.hiddenProperty().addListener(updateSwitcherListener);
        monthPage.hiddenProperty().addListener(updateSwitcherListener);
        yearPage.hiddenProperty().addListener(updateSwitcherListener);

        this.leftMasterDetailPane = new MasterDetailPane(Side.LEFT);
        TrayPane trayPane = new TrayPane();
        this.trayButton = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SOURCE_TRAY")); //$NON-NLS-1$
        this.trayButton.setId("show-tray-button");
        this.addCalendarButton = new Button();
        this.addCalendarButton.setId("add-calendar-button");
        this.addCalendarButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        Text addIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PLUS);
        addIcon.getStyleClass().addAll("button-icon", "add-calendar-button-icon");  //$NON-NLS-1$ //$NON-NLS-2$
        this.addCalendarButton.setGraphic(addIcon);

        this.addCalendarButton.setOnAction(evt -> view.createCalendarSource());
        this.printButton = new Button();
        this.printButton.setId("print-button");
        this.printButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.printButton.setOnAction(evt -> print());

        Text printIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PRINT);
        printIcon.getStyleClass().addAll("button-icon", "print-button-icon"); //$NON-NLS-1$ //$NON-NLS-2$
        this.printButton.setGraphic(printIcon);

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

        ColumnConstraints leftColumn = new ColumnConstraints();
        ColumnConstraints centerColumn = new ColumnConstraints();
        ColumnConstraints rightColumn = new ColumnConstraints();

        leftColumn.setPercentWidth(35);
        centerColumn.setPercentWidth(30);
        rightColumn.setPercentWidth(35);

        GridPane toolBarGridPane = new GridPane();
        toolBarGridPane.setMinWidth(0);
        toolBarGridPane.getColumnConstraints().addAll(leftColumn, centerColumn, rightColumn);

        toolBarGridPane.setId("toolbar"); //$NON-NLS-1$

        /*
         * Toolbar box on the left - hand side.Gets rebuild when some of the
         * control 's properties change.
         */
        leftToolBarBox = new HBox();
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
        showDay = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SHOW_DAY")); //$NON-NLS-1$
        showWeek = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SHOW_WEEK")); //$NON-NLS-1$
        showMonth = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SHOW_MONTH")); //$NON-NLS-1$
        showYear = new ToggleButton(Messages.getString("CalendarViewSkin.TOGGLE_SHOW_YEAR")); //$NON-NLS-1$

        showDay.setOnAction(evt -> view.showDayPage());
        showWeek.setOnAction(evt -> view.showWeekPage());
        showMonth.setOnAction(evt -> view.showMonthPage());
        showYear.setOnAction(evt -> view.showYearPage());

        switcher = new SegmentedButton();
        switcher.setId("switcher"); //$NON-NLS-1$
        switcher.visibleProperty().bind(view.showPageSwitcherProperty());
        buildSwitcher();

        GridPane centerToolBarBox = new GridPane();
        GridPane.setHalignment(switcher, HPos.CENTER);
        GridPane.setValignment(switcher, VPos.CENTER);
        centerToolBarBox.add(switcher, 0, 0);
        centerToolBarBox.setAlignment(Pos.CENTER);
        toolBarGridPane.add(centerToolBarBox, 1, 0);

        // tooltips
        trayButton.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SOURCE_TRAY"))); //$NON-NLS-1$
        addCalendarButton.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_ADD_CALENDAR"))); //$NON-NLS-1$
        printButton.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_PRINT"))); //$NON-NLS-1$
        showDay.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SHOW_DAY"))); //$NON-NLS-1$
        showWeek.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SHOW_WEEK"))); //$NON-NLS-1$
        showMonth.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SHOW_MONTH"))); //$NON-NLS-1$
        showYear.setTooltip(new Tooltip(Messages.getString("CalendarViewSkin.TOOLTIP_SHOW_YEAR"))); //$NON-NLS-1$

        // toolbar right

        Text searchIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SEARCH);
        searchIcon.setId("search-icon"); //$NON-NLS-1$

        CustomTextField searchField = view.getSearchField();
        searchField.setPrefColumnCount(20);
        searchField.setLeft(searchIcon);
        searchField.setId("search-field"); //$NON-NLS-1$
        searchField.setPromptText(Messages.getString("CalendarViewSkin.PROMPT_SEARCH_FIELD")); //$NON-NLS-1$
        searchField.getStylesheets().add(CalendarFXControl.class.getResource("calendar.css").toExternalForm()); //$NON-NLS-1$
        GridPane.setFillWidth(searchField, false);
        GridPane.setHalignment(searchField, HPos.RIGHT);
        toolBarGridPane.add(searchField, 2, 0);

        BorderPane borderPane1 = new BorderPane();

        borderPane1.topProperty().bind(view.headerProperty());
        borderPane1.setCenter(stackPane = new StackPane());
        borderPane1.bottomProperty().bind(view.footerProperty());

        view.selectedPageProperty().addListener(it -> changePage());

        leftMasterDetailPane.setMasterNode(borderPane1);
        leftMasterDetailPane.setDetailNode(trayPane);
        leftMasterDetailPane.setId("tray-pane"); //$NON-NLS-1$
        leftMasterDetailPane.animatedProperty().bindBidirectional(view.traysAnimatedProperty());
        leftMasterDetailPane.getStylesheets().add(CalendarFXControl.class.getResource("calendar.css").toExternalForm()); //$NON-NLS-1$

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

        if (Boolean.getBoolean("calendarfx.developer")) { //$NON-NLS-1$
            DeveloperConsole developerConsole = view.getDeveloperConsole();
            MasterDetailPane developerConsoleMasterDetailPane = new MasterDetailPane(Side.BOTTOM);
            developerConsoleMasterDetailPane.setDividerPosition(.6);
            developerConsoleMasterDetailPane.animatedProperty().bind(view.traysAnimatedProperty());
            developerConsoleMasterDetailPane.getStyleClass().add("developer-master-detail-pane"); //$NON-NLS-1$
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

        stackPane.getChildren().setAll(dayPage, weekPage, monthPage, yearPage);

        final PageBase selectedPage = view.getSelectedPage();
        selectedPage.toFront();

        hideNonSelectedPages();
    }

    private void openTray() {
        leftMasterDetailPane.resetDividerPosition();
        leftMasterDetailPane.setShowDetailNode(true);
    }

    private void closeTray() {
        leftMasterDetailPane.setShowDetailNode(false);
    }

    private void buildSwitcher() {
        switcher.getButtons().clear();
        if (!dayPage.isHidden()) {
            switcher.getButtons().add(showDay);
        }
        if (!weekPage.isHidden()) {
            switcher.getButtons().add(showWeek);
        }
        if (!monthPage.isHidden()) {
            switcher.getButtons().add(showMonth);
        }
        if (!yearPage.isHidden()) {
            switcher.getButtons().add(showYear);
        }

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

        if (getSkinnable().isShowPrintButton()) {
            leftToolBarBox.getChildren().add(printButton);
        }

        if (getSkinnable().isShowPageToolBarControls()) {
            PageBase page = getSkinnable().getSelectedPage();
            Node toolBarControls = page.getToolBarControls();

            if (toolBarControls != null &&
                    !((toolBarControls instanceof Pane) && ((Pane) toolBarControls).getChildrenUnmodifiable().isEmpty())) {
                if (!leftToolBarBox.getChildren().isEmpty()) {
                    leftToolBarBox.getChildren().add(new Separator(Orientation.VERTICAL));
                }
                leftToolBarBox.getChildren().add(toolBarControls);
            }
        }
    }

    private Timeline timeline;

    private void changePage() {
        CalendarView view = getSkinnable();

        if (view.isTransitionsEnabled()) {
            animateChangePage();
        } else {
            updateToggleButtons();

            PageBase selectedPage = view.getSelectedPage();

            selectedPage.setManaged(true);
            selectedPage.setVisible(true);

            /*
             * These values might have been changed if transitions were used
             * before.
             */
            selectedPage.setScaleX(1);
            selectedPage.setScaleY(1);
            selectedPage.setOpacity(1);
            selectedPage.toFront();

            hideNonSelectedPages();
        }
    }

    private void hideNonSelectedPages() {
        PageBase selectedPage = getSkinnable().getSelectedPage();

        pageList.forEach(page -> {
            if (!(page == selectedPage)) {
                page.setVisible(false);
                page.setManaged(false);
            }
        });
    }

    private void updateToggleButtons() {
        CalendarView view = getSkinnable();
        PageBase page = view.getSelectedPage();
        if (page == view.getDayPage()) {
            showDay.setSelected(true);
        } else if (page == view.getWeekPage()) {
            showWeek.setSelected(true);
        } else if (page == view.getMonthPage()) {
            showMonth.setSelected(true);
        } else if (page == view.getYearPage()) {
            showYear.setSelected(true);
        }
    }

    private void animateChangePage() {
        if (timeline != null && timeline.getStatus().equals(Status.RUNNING)) {
            return;
        }

        PageBase oldPage = null;

        final ObservableList<Node> children = stackPane.getChildren();
        if (!children.isEmpty()) {
            oldPage = (PageBase) children.get(children.size() - 1);
        }

        final Node fOldPage = oldPage;

        boolean zoomIn = false;

        PageBase newPage = getSkinnable().getSelectedPage();

        timeline = new Timeline();

        double small = .6;
        double large = 1.4;
        Duration duration = Duration.seconds(.2);

        if (oldPage != null) {
            oldPage.setCache(true);
            oldPage.setCacheHint(CacheHint.SCALE);

            zoomIn = pageList.indexOf(newPage) < pageList.indexOf(oldPage);

            KeyValue oldOpacity = new KeyValue(oldPage.opacityProperty(), 0);
            KeyValue oldScaleX = new KeyValue(oldPage.scaleXProperty(),
                    zoomIn ? large : small);
            KeyValue oldScaleY = new KeyValue(oldPage.scaleYProperty(),
                    zoomIn ? large : small);
            KeyFrame frame1 = new KeyFrame(duration, oldOpacity, oldScaleX,
                    oldScaleY);
            timeline.getKeyFrames().add(frame1);

            oldPage.setCache(true);
            oldPage.setCacheHint(CacheHint.SCALE);

            timeline.setOnFinished(evt -> {
                fOldPage.setVisible(false);
                fOldPage.setManaged(false);
                fOldPage.setCache(false);
                newPage.setCache(false);
                updateToggleButtons();
            });
        } else {
            timeline.setOnFinished(evt -> updateToggleButtons());
        }

        newPage.setOpacity(0);
        newPage.setScaleX(zoomIn ? small : large);
        newPage.setScaleY(zoomIn ? small : large);
        newPage.setCache(true);
        newPage.setCacheHint(CacheHint.SCALE);
        newPage.toFront();

        pageList.forEach(page -> {
            if (!(page == newPage || page == fOldPage)) {
                page.setVisible(false);
                page.setManaged(false);
            } else {
                page.setVisible(true);
                page.setManaged(true);
            }
        });

        KeyValue newOpacity = new KeyValue(newPage.opacityProperty(), 1);
        KeyValue newScaleX = new KeyValue(newPage.scaleXProperty(), 1);
        KeyValue newScaleY = new KeyValue(newPage.scaleYProperty(), 1);

        if (!children.contains(newPage)) {
            children.add(newPage);
        }

        KeyFrame frame2 = new KeyFrame(duration, newOpacity, newScaleX,
                newScaleY);
        timeline.getKeyFrames().add(frame2);

        timeline.play();
    }

    private void showSelectedSearchResult() {
        Entry<?> result = searchResultView.getSelectedEntry();
        if (result != null) {
            getSkinnable().showEntry(result);
        }
    }

    class TrayPane extends BorderPane {
        private SourceView sourceView;
        private YearMonthView yearMonthView;

        public TrayPane() {
            // source view
            sourceView = getSkinnable().getSourceView();
            sourceView.bind(getSkinnable());

            // year month view
            yearMonthView = getSkinnable().getYearMonthView();
            yearMonthView.setShowToday(false);
            yearMonthView.setShowTodayButton(false);
            yearMonthView.setId("date-picker"); //$NON-NLS-1$
            yearMonthView.setSelectionMode(SINGLE);
            yearMonthView.setClickBehaviour(PERFORM_SELECTION);
            yearMonthView.getSelectedDates().add(getSkinnable().getDate());
            yearMonthView.getSelectedDates().addListener((Observable evt) -> {
                if (yearMonthView.getSelectedDates().size() > 0) {
                    yearMonthView.setDate(
                            yearMonthView.getSelectedDates().iterator().next());
                }
            });

            getSkinnable().dateProperty().addListener(it -> {
                yearMonthView.getSelectedDates().clear();
                yearMonthView.getSelectedDates().add(getSkinnable().getDate());
            });

            Bindings.bindBidirectional(yearMonthView.todayProperty(),
                    getSkinnable().todayProperty());
            Bindings.bindBidirectional(yearMonthView.dateProperty(),
                    getSkinnable().dateProperty());
            yearMonthView.weekFieldsProperty().bind(getSkinnable().weekFieldsProperty());

            ScrollPane scrollPane = new ScrollPane(sourceView);

            scrollPane.getStyleClass().add("source-view-scroll-pane"); //$NON-NLS-1$
            setCenter(scrollPane);
            setBottom(yearMonthView);
        }
    }

    private PrintView printView;

    private void print() {
        if (printView == null) {
            printView = getSkinnable().getPrintView();
        }
        printView.setToday(getSkinnable().getToday());
        printView.setWeekFields(getSkinnable().getWeekFields());
        printView.getCalendarSources().setAll(getSkinnable().getCalendarSources());
        printView.setLayout(getSkinnable().getSelectedPage().getLayout());
        printView.setViewType(getSkinnable().getSelectedPage().getPrintViewType());
        printView.requestStartDate(getSkinnable().getDate());
        printView.show(getSkinnable().getScene().getWindow());
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double dayHeight = dayPage.prefHeight(-1);
        double weekHeight = weekPage.prefHeight(-1);
        double monthHeight = monthPage.prefHeight(-1);
        double yearHeight = yearPage.prefHeight(-1);
        return Math.max(dayHeight, Math.max(weekHeight, Math.max(monthHeight, yearHeight)));
    }

    @Override
    protected double computePrefWidth(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double dayWidth = dayPage.prefWidth(-1);
        double weekWidth = weekPage.prefWidth(-1);
        double monthWidth = monthPage.prefWidth(-1);
        double yearWidth = yearPage.prefWidth(-1);
        return Math.max(dayWidth, Math.max(weekWidth, Math.max(monthWidth, yearWidth)));
    }
}
