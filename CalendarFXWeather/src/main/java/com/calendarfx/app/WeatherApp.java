/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.app;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.MonthSheetView;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIconView;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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

        private WeatherIconView icon = new WeatherIconView(WeatherIcon.DAY_SUNNY);

        public WeatherCell(MonthSheetView view, LocalDate date) {
            super(view, date);

            weekNumberLabel.setVisible(false);

            if (getDate() != null) {

                switch ((int) (Math.random() * 9)) {
                    case 0:
                        icon = new WeatherIconView(WeatherIcon.DAY_SUNNY);
                        break;
                    case 1:
                        icon = new WeatherIconView(WeatherIcon.DAY_RAIN);
                        break;
                    case 2:
                        icon = new WeatherIconView(WeatherIcon.DAY_CLOUDY);
                        break;
                    case 3:
                        icon = new WeatherIconView(WeatherIcon.DAY_FOG);
                        break;
                    case 4:
                        icon = new WeatherIconView(WeatherIcon.DAY_LIGHTNING);
                        break;
                    case 5:
                        icon = new WeatherIconView(WeatherIcon.DAY_HAIL);
                        break;
                    case 6:
                        icon = new WeatherIconView(WeatherIcon.DAY_CLOUDY_HIGH);
                        break;
                    case 7:
                        icon = new WeatherIconView(WeatherIcon.DAY_HAZE);
                        break;
                    default:
                        icon = new WeatherIconView(WeatherIcon.DAY_SHOWERS);
                        break;
                }
            }

            getChildren().add(icon);

            icon.setGlyphSize(14);
            updateFillColor(icon);
            WeatherIconView fIcon = icon;
            getView().getDateSelectionModel().getSelectedDates().addListener((Observable it) -> updateFillColor(fIcon));
        }

        private void updateFillColor(WeatherIconView icon) {
            if (getDate() != null && getDate().equals(getView().getToday()) || getView().getDateSelectionModel().isSelected(getDate())) {
                icon.setFill(Color.WHITE);
            } else {
                icon.setFill(Color.CADETBLUE);
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
