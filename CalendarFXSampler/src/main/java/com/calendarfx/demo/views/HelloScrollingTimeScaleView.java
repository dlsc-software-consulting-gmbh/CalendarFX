/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *  Copyright (C) 2006 Google Inc.
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

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.TimeScaleView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class HelloScrollingTimeScaleView extends CalendarFXSample {

    public static final String STYLE_LABEL_TIME_EVEN_HOURS = "-fx-text-fill: gray;";
    public static final String STYLE_LABEL_TIME_ODD_HOURS = "-fx-text-fill: darkblue;";
    public static final String STYLE_LABEL_DATE_SUNDAY = "-fx-background-color: red;";

    @Override
    public String getSampleName() {
        return "Time Scale (Scrolling)";
    }

    @Override
    protected Node createControl() {
        return null;
    }

    @Override
    public Node getPanel(Stage stage) {
        TimeScaleView view = new TimeScaleView();
        view.setTimeStyleProvider(this::provideTimeStyle);
        view.setDateStyleProvider(this::provideDateStyle);
        view.setScrollingEnabled(true);

        return wrap(view);
    }

    @Override
    public Node wrap(Node node) {

        HBox box = new HBox();
        box.setStyle("-fx-padding: 100px;");
        box.setAlignment(Pos.CENTER);
        box.setFillHeight(false);

        StackPane stackPane = new StackPane();
        stackPane.setStyle(
                "-fx-background-color: white; -fx-border-color: gray; -fx-border-width: .25px; -fx-padding: 0 20 0 20;");
        box.getChildren().add(stackPane);

        stackPane.getChildren().add(node);
        stackPane.setEffect(new Reflection());

        stackPane.setPrefHeight(2000);

        return box;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return TimeScaleView.class;
    }

    @Override
    public String getSampleDescription() {
        return "The scale shows the time of day vertically.";
    }

    private String provideTimeStyle(LocalDateTime dateTime) {
        return dateTime.getHour() % 2 == 0 ? STYLE_LABEL_TIME_EVEN_HOURS : STYLE_LABEL_TIME_ODD_HOURS;
    }

    private String provideDateStyle(LocalDateTime dateTime) {
        return dateTime.getDayOfWeek() == DayOfWeek.SUNDAY ? STYLE_LABEL_DATE_SUNDAY : null;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
