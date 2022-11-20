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

import com.calendarfx.view.AgendaView;
import com.calendarfx.view.AllDayView;
import com.calendarfx.view.CalendarHeaderView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DetailedDayView;
import com.calendarfx.view.Messages;
import com.calendarfx.view.TimeScaleView;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import static com.calendarfx.util.ViewHelper.scrollToRequestedTime;

public class DetailedDayViewSkin extends DateControlSkin<DetailedDayView> {

    private final GridPane gridPane;
    private final Label allDayLabel;
    private final DayViewScrollPane timeScaleScrollPane;
    private final DayViewScrollPane dayViewScrollPane;
    private final Separator separator;
    private final AllDayView allDayView;
    private final ScrollBar scrollBar;
    private final CalendarHeaderView calendarHeaderView;
    private final Region allDayFiller;
    private final AgendaView agendaView;
    private final ColumnConstraints col0;
    private final ColumnConstraints col1;
    private final ColumnConstraints col2;
    private final ColumnConstraints col3;
    private final ColumnConstraints col4;

    private VBox entryBox;

    public DetailedDayViewSkin(DetailedDayView view) {
        super(view);

        scrollBar = new ScrollBar();


        // the day view (scroll pane)
        DayView dayView = view.getDayView();
        dayViewScrollPane = new DayViewScrollPane(dayView, scrollBar);

        dayViewScrollPane.getStyleClass().addAll("calendar-scroll-pane", "day-view-scroll-pane");


        // the timescale
        TimeScaleView timeScale = view.getTimeScaleView();
        Bindings.bindBidirectional(timeScale.translateYProperty(), dayView.translateYProperty());

        // the all-day view
        allDayView = view.getAllDayView();
        allDayView.showTodayProperty().unbindBidirectional(view.showTodayProperty());
        allDayView.setShowToday(false);
        allDayView.setAdjustToFirstDayOfWeek(false);

        // all day label
        allDayLabel = new Label(Messages.getString("DetailedDayViewSkin.ALL_DAY"));
        allDayLabel.setTextOverrun(OverrunStyle.CLIP);
        allDayLabel.getStyleClass().add("all-day-label");
        allDayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // all day filler
        allDayFiller = new Region();
        allDayFiller.getStyleClass().add("header-all-day-filler");

        // time scale scroll pane
        timeScaleScrollPane = new DayViewScrollPane(timeScale, scrollBar);
        timeScaleScrollPane.getStyleClass().addAll("calendar-scroll-pane", "day-view-timescale-scroll-pane");
        timeScaleScrollPane.setMinWidth(Region.USE_PREF_SIZE);

        // separator
        separator = new Separator(Orientation.VERTICAL);

        final InvalidationListener visibilityListener = it -> updateVisibilities();
        view.showAllDayViewProperty().addListener(visibilityListener);
        view.showTimeScaleViewProperty().addListener(visibilityListener);
        view.layoutProperty().addListener(visibilityListener);
        view.showAgendaViewProperty().addListener(visibilityListener);
        view.showScrollBarProperty().addListener(visibilityListener);

        calendarHeaderView = view.getCalendarHeaderView();
        calendarHeaderView.visibleProperty().bind(view.layoutProperty().isEqualTo(DateControl.Layout.SWIMLANE));

        agendaView = view.getAgendaView();

        RowConstraints row0 = new RowConstraints();
        row0.setFillHeight(true);
        row0.setPrefHeight(Region.USE_COMPUTED_SIZE);
        row0.setVgrow(Priority.NEVER);

        RowConstraints row1 = new RowConstraints();
        row1.setFillHeight(true);
        row1.setPrefHeight(Region.USE_COMPUTED_SIZE);
        row1.setVgrow(Priority.NEVER);

        RowConstraints row2 = new RowConstraints();
        row2.setFillHeight(true);
        row2.setPrefHeight(Region.USE_COMPUTED_SIZE);
        row2.setVgrow(Priority.ALWAYS);

        col0 = new ColumnConstraints();
        col0.setFillWidth(true);
        col0.setHgrow(Priority.NEVER);
        col0.setPrefWidth(Region.USE_COMPUTED_SIZE);

        col1 = new ColumnConstraints();
        col1.setFillWidth(true);
        col1.setHgrow(Priority.ALWAYS);
        col1.setPrefWidth(Region.USE_COMPUTED_SIZE);

        col2 = new ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow(Priority.NEVER);
        col2.setPrefWidth(Region.USE_COMPUTED_SIZE);

        col3 = new ColumnConstraints();
        col3.setFillWidth(true);
        col3.setHgrow(Priority.NEVER);
        col3.setPrefWidth(Region.USE_COMPUTED_SIZE);

        col4 = new ColumnConstraints();
        col4.setFillWidth(true);
        col4.setHgrow(Priority.ALWAYS);
        col4.setPrefWidth(Region.USE_COMPUTED_SIZE);

        GridPane.setRowSpan(agendaView, 3);
        GridPane.setRowSpan(separator, 3);

        gridPane = new GridPane();
        gridPane.getRowConstraints().setAll(row0, row1, row2);
        gridPane.getStyleClass().add("container");

        getChildren().add(gridPane);

        agendaView.setPrefWidth(0);
        dayView.setPrefWidth(0);

        /*
         * Run later when the control has become visible.
         */
        Platform.runLater(() -> scrollToRequestedTime(view, dayViewScrollPane));

        view.requestedTimeProperty().addListener(it -> scrollToRequestedTime(view, dayViewScrollPane));

        updateVisibilities();
    }

    private void updateVisibilities() {
        gridPane.getChildren().clear();

        final DetailedDayView view = getSkinnable();

        if (view.isShowTimeScaleView()) {
            gridPane.add(timeScaleScrollPane, 0, 2);
            if (view.isShowAllDayView()) {
                gridPane.add(allDayLabel, 0, 0);
            }
        }

        if (view.isShowAllDayView()) {
            gridPane.add(allDayView, 1, 0);
            gridPane.add(allDayFiller, 2, 0);
        }

        if (view.getLayout().equals(DateControl.Layout.SWIMLANE)) {
            gridPane.add(calendarHeaderView, 1, 1);
        }

        gridPane.add(dayViewScrollPane, 1, 2);

        if (view.isShowScrollBar()) {
            Label promptLabel = new Label("Double-click area to add entry...");
            gridPane.add(promptLabel, 1,0);
            gridPane.add(scrollBar, 2, 2);
        }

        if (view.isShowAgendaView()) {
            gridPane.getColumnConstraints().setAll(col0, col1, col2, col3, col4);
            gridPane.add(separator, 3, 0);
            gridPane.add(agendaView, 4, 0);
        } else {
            gridPane.getColumnConstraints().setAll(col0, col1, col2);
        }
    }
}
