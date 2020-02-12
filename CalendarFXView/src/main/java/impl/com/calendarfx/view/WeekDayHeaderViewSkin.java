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

import com.calendarfx.view.WeekDayHeaderView;
import com.calendarfx.view.WeekDayHeaderView.WeekDayCell;
import javafx.beans.InvalidationListener;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import java.time.LocalDate;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;

public class WeekDayHeaderViewSkin extends SkinBase<WeekDayHeaderView> {

    private static final String TODAY = "today";

    private GridPane pane;

    public WeekDayHeaderViewSkin(WeekDayHeaderView view) {
        super(view);

        pane = new GridPane();
        pane.getStyleClass().add("container");

        getChildren().add(pane);

        final InvalidationListener updateListener = it -> updateControl();

        view.numberOfDaysProperty().addListener(updateListener);
        view.showTodayProperty().addListener(updateListener);
        view.weekFieldsProperty().addListener(updateListener);
        view.enableHyperlinksProperty().addListener(updateListener);
        view.adjustToFirstDayOfWeekProperty().addListener(updateListener);

        updateControl();
    }

    private void updateControl() {
        pane.getChildren().clear();
        pane.getColumnConstraints().clear();

        // the day views
        WeekDayHeaderView view = getSkinnable();
        final int numberOfDays = view.getNumberOfDays();

        Callback<WeekDayHeaderView, WeekDayCell> cellFactory = view.getCellFactory();
        for (int i = 0; i < numberOfDays; i++) {
            ColumnConstraints con = new ColumnConstraints();
            con.setPercentWidth((double) 100 / (double) numberOfDays);
            pane.getColumnConstraints().add(con);
            WeekDayCell cell = cellFactory.call(view);
            GridPane.setHgrow(cell, Priority.ALWAYS);
            GridPane.setVgrow(cell, Priority.ALWAYS);
            GridPane.setFillHeight(cell, true);
            GridPane.setFillWidth(cell, true);

            final int dayCount = i;
            /*
             * TODO: listener must be removed when number of days change.
             */
            view.dateProperty().addListener(evt -> updateCell(cell, dayCount));
            updateCell(cell, dayCount);

            pane.add(cell, i, 0);
        }
    }

    private void updateCell(WeekDayCell cell, int dayCount) {
        LocalDate startDate = getSkinnable().getDate();
        LocalDate date = getDate(startDate, dayCount);
        cell.setDate(date);

        if (getSkinnable().isShowToday() && date.equals(getSkinnable().getToday())) {
            if (!cell.getStyleClass().contains(TODAY)) {
                cell.getStyleClass().add(TODAY);
            }
        } else {
            cell.getStyleClass().remove(TODAY);
        }
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
