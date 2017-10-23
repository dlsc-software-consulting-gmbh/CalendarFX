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
import com.calendarfx.view.TimeField;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HelloTimeField extends CalendarFXSample {

    @Override
    public String getSampleName() {
        return "Time Field";
    }

    @Override
    protected Node createControl() {
        TimeField timeField = new TimeField();
        Label label = new Label("Time: ");
        label.setText("Time: " + timeField.getValue().toString());
        timeField.valueProperty().addListener(it -> label.setText("Time: " + timeField.getValue().toString()));

        VBox box = new VBox();
        box.setSpacing(20);
        box.getChildren().addAll(timeField, label);

        return box;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return TimeField.class;
    }

    @Override
    public String getSampleDescription() {
        return "A control used to specify a local time (hour, minute).";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
