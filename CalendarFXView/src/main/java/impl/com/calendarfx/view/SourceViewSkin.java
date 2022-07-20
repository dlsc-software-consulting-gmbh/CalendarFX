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

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.SourceView;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

@SuppressWarnings("javadoc")
public class SourceViewSkin extends SkinBase<SourceView> {

    private final VBox vbox;

    private final InvalidationListener updater = obs -> updateView();

    public SourceViewSkin(SourceView view) {
        super(view);

        vbox = new VBox();
        vbox.getStyleClass().add("container");

        getChildren().add(vbox);

        view.getCalendarSources().addListener(updater);
        updateView();
    }

    private void updateView() {
        vbox.getChildren().clear();
        for (CalendarSource source : getSkinnable().getCalendarSources()) {
            source.getCalendars().removeListener(updater);
            source.getCalendars().addListener(updater);

            VBox box = new VBox(8);
            box.getStyleClass().add("single-calendar-group");

            for (Calendar calendar : source.getCalendars()) {
                CheckBox checkBox = new CheckBox();
                checkBox.textProperty().bind(calendar.nameProperty());
                checkBox.getStyleClass().addAll("default-style-visibility-checkbox",
                        calendar.getStyle() + "-visibility-checkbox");
                Bindings.bindBidirectional(checkBox.selectedProperty(), getSkinnable().getCalendarVisibilityProperty(calendar));
                box.getChildren().add(checkBox);
            }

            if (getSkinnable().getCalendarSources().size() == 1) {
                vbox.getChildren().add(box);
            } else {
                TitledPane titledPane = new TitledPane();
                titledPane.textProperty().bind(source.nameProperty());
                titledPane.setContent(box);
                vbox.getChildren().add(titledPane);
            }
        }
    }
}
