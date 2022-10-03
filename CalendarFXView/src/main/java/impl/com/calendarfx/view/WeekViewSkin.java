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

import com.calendarfx.view.WeekDayView;
import com.calendarfx.view.WeekView;
import com.calendarfx.view.WeekView.WeekDayParameter;
import javafx.beans.InvalidationListener;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;

public class WeekViewSkin extends SkinBase<WeekView> {

    private final HBox container = new HBox();

    public WeekViewSkin(WeekView view) {
        super(view);


        final InvalidationListener rebuildListener = it -> buildDays();
        view.numberOfDaysProperty().addListener(rebuildListener);
        view.adjustToFirstDayOfWeekProperty().addListener(rebuildListener);
        view.weekDayViewFactoryProperty().addListener(rebuildListener);

        buildDays();

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        new DayViewEditController(view);

        getChildren().add(container);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        final double height = container.prefHeight(-1);
        container.resizeRelocate(contentX, contentY, contentWidth, height);
    }

    private void buildDays() {
        WeekView weekView = getSkinnable();
        List<WeekDayView> weekDayViews = weekView.getWeekDayViews();

        // before rebuilding make sure to unbind previous day view children
        weekDayViews.forEach(view -> getSkinnable().unbind(view));

        container.getChildren().clear();

        weekDayViews.clear();

        Callback<WeekDayParameter, WeekDayView> weekDayViewFactory = weekView.getWeekDayViewFactory();

        int numberOfDays = weekView.getNumberOfDays();

        Callback<WeekView, Region> separatorFactory = weekView.getSeparatorFactory();

        for (int i = 0; i < numberOfDays; i++) {
            WeekDayParameter param = new WeekDayParameter(weekView);

            WeekDayView weekDayView = weekDayViewFactory.call(param);
            weekDayView.setPrefWidth(1); // equal width distribution
            weekDayView.getProperties().put("week.view", weekView);

            InvalidationListener updateUsedTimesListener = it -> updateUsedTimes();
            weekDayView.earliestTimeUsedProperty().addListener(updateUsedTimesListener);
            weekDayView.latestTimeUsedProperty().addListener(updateUsedTimesListener);

            weekDayView.showTodayProperty().bindBidirectional(weekView.showTodayProperty());

            if (i == 0) {
                weekDayView.getStyleClass().add("first-day");
            } else if (i == numberOfDays - 1) {
                weekDayView.getStyleClass().add("last-day");
            }

            final int dayCount = i;

            final InvalidationListener updateListener = it -> updateDate(weekDayView, dayCount);
            weekView.dateProperty().addListener(updateListener);
            weekView.weekFieldsProperty().addListener(updateListener);

            updateDate(weekDayView, dayCount);

            getSkinnable().bind(weekDayView, false);

            HBox.setHgrow(weekDayView, Priority.ALWAYS);
            container.getChildren().add(weekDayView);

            if (separatorFactory != null && i < numberOfDays - 1) {
                Region separator = separatorFactory.call(weekView);
                if (separator != null) {
                    container.getChildren().add(separator);
                }
            }

            weekDayViews.add(weekDayView);
        }
    }

    private void updateUsedTimes() {
        LocalTime earliestTime = null;
        LocalTime latestTime = null;

        for (WeekDayView view : getSkinnable().getWeekDayViews()) {

            LocalTime etu = view.getEarliestTimeUsed();
            LocalTime ltu = view.getLatestTimeUsed();

            if (earliestTime == null || (etu != null && etu.isBefore(earliestTime))) {
                earliestTime = etu;
            }

            if (latestTime == null || (ltu != null && ltu.isAfter(latestTime))) {
                latestTime = ltu;
            }
        }

        getSkinnable().getProperties().put("earliest.time.used", earliestTime);
        getSkinnable().getProperties().put("latest.time.used", latestTime);
    }

    private void updateDate(WeekDayView view, int dayCount) {
        updateDate(view, getSkinnable().getDate(), dayCount);
    }

    private void updateDate(WeekDayView view, LocalDate startDate, int dayCount) {
        LocalDate date = getDate(startDate, dayCount);
        view.setDate(date);
    }

    private LocalDate getDate(LocalDate startDate, int dayCount) {
        if (getSkinnable().isAdjustToFirstDayOfWeek()) {
            LocalDate newStartDate = startDate.with(DAY_OF_WEEK, getSkinnable().getFirstDayOfWeek().getValue());
            if (newStartDate.isAfter(startDate)) {
                startDate = newStartDate.minusWeeks(1);
            } else {
                startDate = newStartDate;
            }
        }

        return startDate.plusDays(dayCount);
    }
}
