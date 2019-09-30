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

package com.calendarfx.weather;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.MonthSheetView;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.weathericons.WeatherIcons;

import java.time.LocalDate;

public class WeatherApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Calendar zurich = new Calendar("Zurich");
        CalendarSource calendarSource = new CalendarSource("Weather");
        calendarSource.getCalendars().addAll(zurich);

        MonthSheetView sheetView = new MonthSheetView();
        sheetView.setPadding(new Insets(20));
        sheetView.setCellFactory(param -> new WeatherCell(param.getView(), param.getDate()));
        sheetView.getCalendarSources().setAll(calendarSource);
        sheetView.setContextMenu(null);

        Scene scene = new Scene(sheetView);
        primaryStage.setTitle("Weather Calendar");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class WeatherCell extends MonthSheetView.SimpleDateCell {

        private FontIcon icon = new FontIcon(WeatherIcons.DAY_SUNNY);

        public WeatherCell(MonthSheetView view, LocalDate date) {
            super(view, date);

            weekNumberLabel.setVisible(false);

            if (getDate() != null) {

                switch ((int) (Math.random() * 9)) {
                    case 0:
                        icon = new FontIcon(WeatherIcons.DAY_SUNNY);
                        break;
                    case 1:
                        icon = new FontIcon(WeatherIcons.DAY_RAIN);
                        break;
                    case 2:
                        icon = new FontIcon(WeatherIcons.DAY_CLOUDY);
                        break;
                    case 3:
                        icon = new FontIcon(WeatherIcons.DAY_FOG);
                        break;
                    case 4:
                        icon = new FontIcon(WeatherIcons.DAY_LIGHTNING);
                        break;
                    case 5:
                        icon = new FontIcon(WeatherIcons.DAY_HAIL);
                        break;
                    case 6:
                        icon = new FontIcon(WeatherIcons.DAY_CLOUDY_HIGH);
                        break;
                    case 7:
                        icon = new FontIcon(WeatherIcons.DAY_HAZE);
                        break;
                    default:
                        icon = new FontIcon(WeatherIcons.DAY_SHOWERS);
                        break;
                }
            }

            getChildren().add(icon);

            icon.setIconSize(14);
            updateFillColor(icon);
            FontIcon fIcon = icon;
            getView().getDateSelectionModel().getSelectedDates().addListener((Observable it) -> updateFillColor(fIcon));
        }

        private void updateFillColor(FontIcon icon) {
            if (getDate() != null && getDate().equals(getView().getToday()) || getView().getDateSelectionModel().isSelected(getDate())) {
                icon.setIconColor(Color.WHITE);
            } else {
                icon.setIconColor(Color.CADETBLUE);
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
            double ps3 = icon.prefWidth(-1);

            dayOfMonthLabel.resizeRelocate(left, top, ps1, availableHeight);
            dayOfWeekLabel.resizeRelocate(left + ps1, top, ps2, availableHeight);
            icon.resizeRelocate(w - right - ps3, (availableHeight - icon.prefHeight(-1)) / 2, ps3, availableHeight);
        }
    }
}
