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
import com.calendarfx.view.SourceGridView;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class SourceGridViewSkin extends SkinBase<SourceGridView> {

    private static final String DEFAULT_STYLE = "source-grid-view";

    private final InvalidationListener buildListener = obs -> build();
    private final WeakInvalidationListener buildWeakListener = new WeakInvalidationListener(buildListener);

    private final ListChangeListener<CalendarSource> sourcesChangeListener = c -> {
        while (c.next()) {
            for (CalendarSource added : c.getAddedSubList()) {
                added.getCalendars().addListener(buildWeakListener);
            }

            for (CalendarSource removed : c.getRemoved()) {
                removed.getCalendars().removeListener(buildWeakListener);
            }
        }

        build();
    };
    private final WeakListChangeListener<CalendarSource> sourcesWeakChangeListener = new WeakListChangeListener<>(sourcesChangeListener);

    private final HBox container;

    public SourceGridViewSkin(SourceGridView control) {
        super(control);

        container = new HBox();
        container.getStyleClass().add(DEFAULT_STYLE);
        build();

        control.getCalendarSources().addListener(sourcesWeakChangeListener);
        control.maximumRowsPerColumnProperty().addListener(buildWeakListener);
        getChildren().add(container);
    }

    private void build() {
        List<Node> children = new ArrayList<>();

        VBox row = null;
        int count = 0;

        final SourceGridView view = getSkinnable();

        for (CalendarSource source : view.getCalendarSources()) {
            for (Calendar calendar : source.getCalendars()) {

                view.getCalendarVisibilityProperty(calendar).removeListener(buildWeakListener);
                view.getCalendarVisibilityProperty(calendar).addListener(buildWeakListener);

                if (!view.isCalendarVisible(calendar)) {
                    continue;
                }

                if (count == 0) {
                    row = new VBox();
                    row.getStyleClass().add("column");
                    children.add(row);
                }

                row.getChildren().add(new CalendarItem(calendar));
                count++;

                if (count == view.getMaximumRowsPerColumn()) {
                    count = 0;
                }
            }
        }

        container.getChildren().setAll(children);
    }

    private static class CalendarItem extends Label {

        public CalendarItem(Calendar calendar) {
            Pane graphic = new Pane();
            graphic.getStyleClass().add("item-box");
            graphic.getStyleClass().add(calendar.getStyle() + "-source-grid-item-box");

            getStyleClass().add("item");
            setGraphic(graphic);
            setText(calendar.getName());
        }

    }
}
