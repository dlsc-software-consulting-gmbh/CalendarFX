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

package impl.com.calendarfx.view.print;

import com.calendarfx.view.print.SettingsView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SettingsViewSkin extends SkinBase<SettingsView> {

    public SettingsViewSkin(SettingsView control) {
        super(control);

        VBox container = new VBox();
        container.getStyleClass().add("container");

        ScrollPane scrollPane = new ScrollPane(control.getSourceView());
        scrollPane.setPrefViewportHeight(180);

        container.getChildren().addAll(
                new SectionTitle("Paper"),
                control.getPaperView(),
                new SectionTitle("Time Range"),
                control.getTimeRangeView(),
                new SectionTitle("Calendars"),
                scrollPane,
                new SectionTitle("Options"),
                control.getOptionsView());

        getChildren().add(container);
    }

    private static class SectionTitle extends HBox {

        public SectionTitle(String name) {
            getStyleClass().add("section-title");

            Label titleLabel = new Label(name);
            Separator separator = new Separator();
            separator.setPadding(new Insets(5, 0, 0, 0));

            HBox.setHgrow(separator, Priority.ALWAYS);
            HBox.setHgrow(titleLabel, Priority.NEVER);

            setSpacing(10);
            setAlignment(Pos.CENTER);

            getChildren().addAll(titleLabel, separator);
        }
    }
}
