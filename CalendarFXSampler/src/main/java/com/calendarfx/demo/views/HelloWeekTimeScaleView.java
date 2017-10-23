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

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.WeekTimeScaleView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class HelloWeekTimeScaleView extends CalendarFXDateControlSample {

    @Override
    public String getSampleName() {
        return "Week Time Scale";
    }

    @Override
    protected DateControl createControl() {
        return new WeekTimeScaleView();
    }

    @Override
    protected Node wrap(Node node) {
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

        return box;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return WeekTimeScaleView.class;
    }

    @Override
    public String getSampleDescription() {
        return "The scale shows the time of day vertically.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
