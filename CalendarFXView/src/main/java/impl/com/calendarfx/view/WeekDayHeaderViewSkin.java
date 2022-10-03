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
import com.calendarfx.view.WeekDayHeaderView.WeekDayHeaderCell;
import javafx.beans.InvalidationListener;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;

public class WeekDayHeaderViewSkin extends SkinBase<WeekDayHeaderView> {

    private static final String TODAY = "today";

    private final HBox hbox;

    public WeekDayHeaderViewSkin(WeekDayHeaderView view) {
        super(view);

        hbox = new HBox();
        hbox.getStyleClass().add("container");

        getChildren().add(hbox);

        final InvalidationListener updateListener = it -> updateControl();

        view.numberOfDaysProperty().addListener(updateListener);
        view.showTodayProperty().addListener(updateListener);
        view.weekFieldsProperty().addListener(updateListener);
        view.enableHyperlinksProperty().addListener(updateListener);
        view.adjustToFirstDayOfWeekProperty().addListener(updateListener);

        updateControl();
    }

    private final List<InvalidationListener> listeners = new ArrayList<>();

    private void updateControl() {
        hbox.getChildren().clear();
        // the day views
        WeekDayHeaderView view = getSkinnable();
        listeners.forEach(l -> view.dateProperty().removeListener(l));

        final int numberOfDays = view.getNumberOfDays();

        Callback<WeekDayHeaderView, Region> separatorFactory = view.getSeparatorFactory();
        Callback<WeekDayHeaderView, WeekDayHeaderCell> cellFactory = view.getCellFactory();

        for (int i = 0; i < numberOfDays; i++) {
            WeekDayHeaderCell cell = cellFactory.call(view);
            cell.setPrefWidth(1); // equal width distribution

            final int dayCount = i;

            InvalidationListener invalidationListener = evt -> updateCell(cell, dayCount);
            listeners.add(invalidationListener);

            view.dateProperty().addListener(invalidationListener);
            updateCell(cell, dayCount);

            HBox.setHgrow(cell, Priority.ALWAYS);
            hbox.getChildren().add(cell);

            if (separatorFactory != null && i < numberOfDays - 1) {
                Region separator = separatorFactory.call(view);
                if (separator != null) {
                    HBox.setHgrow(separator, Priority.NEVER);
                    hbox.getChildren().add(separator);
                }
            }
        }
    }

    private void updateCell(WeekDayHeaderCell cell, int dayCount) {
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
