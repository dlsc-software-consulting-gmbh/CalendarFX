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

package com.calendarfx.demo;

import com.calendarfx.view.DateControl;
import com.calendarfx.view.DeveloperConsole;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.MasterDetailPane;

import static java.util.Objects.requireNonNull;

public abstract class CalendarFXDateControlSample extends CalendarFXSample {

    @Override
    public final Node getPanel(Stage stage) {
        DateControl dateControl = createControl();
        control = dateControl;
        requireNonNull(control, "missing date control");

        DeveloperConsole console = new DeveloperConsole();
        console.setDateControl(dateControl);

        if (isSupportingDeveloperConsole()) {
            MasterDetailPane masterDetailPane = new MasterDetailPane();
            masterDetailPane.setMasterNode(wrap(dateControl));
            masterDetailPane.setDetailSide(Side.BOTTOM);
            masterDetailPane.setDetailNode(console);
            masterDetailPane.setShowDetailNode(true);
            return masterDetailPane;
        }

        return wrap(dateControl);
    }

    @Override
    protected Node wrap(Node node) {
        StackPane outerPane = new StackPane();
        outerPane.setStyle("-fx-padding: 20px;");

        StackPane stackPane = new StackPane();
        stackPane.setStyle(
                "-fx-background-color: white; -fx-border-color: gray; -fx-border-width: .25px; -fx-padding: 20px;");
        outerPane.getChildren().add(stackPane);

        StackPane.setAlignment(node, Pos.CENTER);
        stackPane.getChildren().add(node);

        return outerPane;
    }

    protected boolean isSupportingDeveloperConsole() {
        return true;
    }

    @Override
    protected abstract DateControl createControl();

}
