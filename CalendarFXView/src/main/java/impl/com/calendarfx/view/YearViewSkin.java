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

import com.calendarfx.view.YearMonthView;
import com.calendarfx.view.YearView;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import static java.lang.Double.MAX_VALUE;

public class YearViewSkin extends DateControlSkin<YearView> {

    public YearViewSkin(YearView view) {
        super(view);

        view.dateProperty().addListener(evt -> updateMonths());

        ScrollPane scrollPane = new ScrollPane();
        
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("container");
        gridPane.setMaxSize(MAX_VALUE, MAX_VALUE);

        for (int row = 0; row < 3; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(Region.USE_PREF_SIZE);
            rowConstraints.setPrefHeight(Region.USE_COMPUTED_SIZE);
            rowConstraints.setMaxHeight(Region.USE_COMPUTED_SIZE);
            rowConstraints.setVgrow(Priority.ALWAYS);
            rowConstraints.setValignment(VPos.CENTER);
            gridPane.getRowConstraints().add(rowConstraints);
        }

        for (int col = 0; col < 4; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setMinWidth(Region.USE_PREF_SIZE);
            colConstraints.setPrefWidth(Region.USE_COMPUTED_SIZE);
            colConstraints.setMaxWidth(Region.USE_COMPUTED_SIZE);
            colConstraints.setHgrow(Priority.ALWAYS);
            colConstraints.setHalignment(HPos.CENTER);
            gridPane.getColumnConstraints().add(colConstraints);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                Month month = Month.of(row * 4 + col + 1);

                YearMonthView yearMonthView = view.getMonthView(month);
                yearMonthView.setShowMonthArrows(false);
                yearMonthView.setShowTodayButton(false);
                yearMonthView.setShowUsageColors(true);
                yearMonthView.setClickBehaviour(YearMonthView.ClickBehaviour.SHOW_DETAILS);
                gridPane.add(yearMonthView, col, row);

                // do not bind date, we manage it manually
                view.bind(yearMonthView, false);
            }
        }

        scrollPane.setContent(gridPane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        getChildren().add(scrollPane);

        updateMonths();
    }

    private void updateMonths() {
        YearView yearPage = getSkinnable();
        LocalDate date = yearPage.getDate();
        int year = date.getYear();

        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(year, month);
            YearMonthView view = yearPage.getMonthView(month);
            view.setMinSize(0, 0);
            view.setDate(yearMonth.atDay(1));
        }
    }

}
