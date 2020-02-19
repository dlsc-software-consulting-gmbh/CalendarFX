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

import com.calendarfx.view.AllDayView;
import com.calendarfx.view.CalendarHeaderView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.Messages;
import com.calendarfx.view.WeekDayHeaderView;
import com.calendarfx.view.WeekTimeScaleView;
import com.calendarfx.view.WeekView;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

import static com.calendarfx.util.ViewHelper.scrollToRequestedTime;

public class DetailedWeekViewSkin extends DateControlSkin<DetailedWeekView> {

    private final Label allDayLabel;
    private final DayViewScrollPane weekViewScrollPane;
    private final DayViewScrollPane timeScaleScrollPane;
    private final GridPane weekViewContainer;
    private final AllDayView allDayView;
    private final CalendarHeaderView calendarHeaderView;
    private final WeekDayHeaderView weekdayHeaderView;
    private final ScrollBar scrollBar;
    private final Region weekdayFillerLeft;
    private final Region weekdayFillerRight;
    private final Region allDayFiller;

    public DetailedWeekViewSkin(DetailedWeekView view) {
        super(view);

        WeekView weekView = view.getWeekView();
        allDayView = view.getAllDayView();
        calendarHeaderView = view.getCalendarHeaderView();
        scrollBar = new ScrollBar();

        weekViewScrollPane = new DayViewScrollPane(weekView, scrollBar);
        weekViewScrollPane.getStyleClass().add("week-view-scroll-pane");

        allDayLabel = new Label(Messages.getString("DetailedWeekViewSkin.ALL_DAY"));
        allDayLabel.setTextOverrun(OverrunStyle.CLIP);
        allDayLabel.getStyleClass().add("all-day-label");
        allDayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        weekdayFillerLeft = new Region();
        weekdayFillerLeft.getStyleClass().addAll("filler-left");

        weekdayFillerRight = new Region();
        weekdayFillerRight.getStyleClass().addAll("filler-right");

        allDayFiller = new Region();
        allDayFiller.getStyleClass().add("all-day-filler");

        WeekTimeScaleView timeScale = view.getTimeScaleView();
        timeScale.getProperties().put("week.view", view);

        timeScaleScrollPane = new DayViewScrollPane(timeScale, scrollBar);
        timeScaleScrollPane.getStyleClass().addAll("timescale-scroll-pane");

        // synchronous scrolling
        Bindings.bindBidirectional(timeScale.translateYProperty(), weekView.translateYProperty());

        weekdayHeaderView = view.getWeekDayHeaderView();

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
        row2.setVgrow(Priority.NEVER);

        RowConstraints row3 = new RowConstraints();
        row3.setFillHeight(true);
        row3.setPrefHeight(Region.USE_COMPUTED_SIZE);
        row3.setVgrow(Priority.ALWAYS);

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setFillWidth(true);
        col0.setHgrow(Priority.NEVER);
        col0.setPrefWidth(Region.USE_COMPUTED_SIZE);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setFillWidth(true);
        col1.setHgrow(Priority.ALWAYS);
        col1.setPrefWidth(Region.USE_COMPUTED_SIZE);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow(Priority.NEVER);
        col2.setPrefWidth(Region.USE_COMPUTED_SIZE);

        weekViewContainer = new GridPane();
        weekViewContainer.getStyleClass().add("container");
        weekViewContainer.setGridLinesVisible(true);
        weekViewContainer.getRowConstraints().setAll(row0, row1, row2, row3);
        weekViewContainer.getColumnConstraints().setAll(col0, col1, col2);

        getChildren().add(weekViewContainer);

        final InvalidationListener visibilityListener = it -> updateVisibilities();
        view.showTimeScaleViewProperty().addListener(visibilityListener);
        view.showWeekDayHeaderViewProperty().addListener(visibilityListener);
        view.showAllDayViewProperty().addListener(visibilityListener);
        view.layoutProperty().addListener(visibilityListener);
        view.showScrollBarProperty().addListener(visibilityListener);

        /*
         * Run later when the control has become visible.
         */
        Platform.runLater(() -> scrollToRequestedTime(view, weekViewScrollPane));

        view.requestedTimeProperty().addListener(it -> scrollToRequestedTime(view, weekViewScrollPane));

        updateVisibilities();
    }

    private void updateVisibilities() {
        weekViewContainer.getChildren().clear();

        final DetailedWeekView view = getSkinnable();

        if (view.isShowTimeScaleView()) {
            weekViewContainer.add(timeScaleScrollPane, 0, 3);
            if (view.isShowAllDayView()) {
                weekViewContainer.add(allDayLabel, 0, 1);
            }
        }

        if (view.isShowWeekDayHeaderView()) {
            weekViewContainer.add(weekdayFillerLeft, 0, 0);
            weekViewContainer.add(weekdayHeaderView, 1, 0);
            weekViewContainer.add(weekdayFillerRight, 2, 0);
        }

        if (view.isShowAllDayView()) {
            weekViewContainer.add(allDayView, 1, 1);
            weekViewContainer.add(allDayFiller, 2, 1);
        }

        if (view.getLayout().equals(DateControl.Layout.SWIMLANE)) {
            weekViewContainer.add(calendarHeaderView, 1, 2);
        }

        weekViewContainer.add(weekViewScrollPane, 1, 3);

        if (view.isShowScrollBar()) {
            weekViewContainer.add(scrollBar, 2, 3);
        }
    }
}
