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

import com.calendarfx.util.CalendarFX;
import com.calendarfx.view.CalendarFXControl;
import fxsampler.SampleBase;
import impl.com.calendarfx.view.CalendarPropertySheet;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static java.util.Objects.requireNonNull;

public abstract class CalendarFXSample extends SampleBase {

    protected Node control;

    @Override
    public Node getPanel(Stage stage) {
        control = createControl();
        requireNonNull(control, "missing date control");
        return wrap(control);
    }

    protected Node wrap(Node node) {
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: .25px; -fx-padding: 20px;");
        stackPane.getChildren().add(node);

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setFillHeight(false);
        box.getChildren().add(stackPane);

        return box;
    }

    @Override
    public String getProjectName() {
        return "CalendarFX";
    }

    @Override
    public String getProjectVersion() {
        return CalendarFX.getVersion().toString();
    }

    @Override
    public double getControlPanelDividerPosition() {
        return .7;
    }

    @Override
    public Node getControlPanel() {
        if (control instanceof CalendarFXControl) {
            return new CalendarPropertySheet(((CalendarFXControl) control).getPropertySheetItems());
        }
        return null;
    }

    @Override
    public String getControlStylesheetURL() {
        return "/calendar.css";
    }

    @Override
    public final String getSampleSourceURL() {
        return getSampleSourceBase() + getClass().getSimpleName() + ".java";
    }

    private final String getSampleSourceBase() {
        return "http://dlsc.com/wp-content/html/calendarfx/sampler/";
    }

    // Javadoc support.

    protected Class<?> getJavaDocClass() {
        return null;
    }

    private String getJavaDocBase() {
        return "http://dlsc.com/wp-content/html/calendarfx/apidocs/";
    }

    @Override
    public final String getJavaDocURL() {
        Class<?> cl = getJavaDocClass();
        String url;
        if (cl == null) {
            url = getJavaDocBase() + "index.html?sampler=true";
        } else {
            url = getJavaDocBase() + cl.getName().replace(".", "/") + ".html";
        }
        return url;
    }

    protected abstract Node createControl();

}
