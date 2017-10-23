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
import com.calendarfx.view.RecurrenceView;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

public class HelloRecurrenceView extends CalendarFXSample {

    @Override
    public String getSampleName() {
        return "Recurrence View";
    }

    @Override
    protected Node createControl() {
        RecurrenceView view = new RecurrenceView();

        Label label = new Label("Rule: " + view.getRecurrenceRule());
        label.setMaxWidth(300);
        label.setWrapText(true);

        view.recurrenceRuleProperty().addListener(it -> label.setText(view.getRecurrenceRule()));

        Separator separator = new Separator(Orientation.HORIZONTAL);

        VBox box = new VBox(20);
        box.setFillWidth(true);
        box.getChildren().addAll(view, separator, label);
        box.setAlignment(Pos.CENTER);

        return box;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return RecurrenceView.class;
    }

    @Override
    public String getSampleDescription() {
        return "The recurrence view allows the user to specify recurrence rules according to RFC 2445.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
