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

package com.calendarfx.demo.entries;

import com.calendarfx.model.Entry;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.EntryViewBase;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

public class HelloDayEntryView extends HelloEntryViewBase {

    @Override
    public String getSampleName() {
        return "Day Entry View";
    }

    @Override
    protected EntryViewBase<?> createEntryView(Entry<?> entry) {
        DayEntryView view = new DayEntryView(entry);
        ContextMenu menu = new ContextMenu();
        for (Pos pos : Pos.values()) {
            MenuItem item = new MenuItem(pos.name());
            item.setOnAction(evt -> {
                view.clearNodes();

                for (int i = 0; i < 3; i++) {
                    Region region = new Region();

                    switch (i) {
                        case 0:
                            region.setStyle("-fx-background-color: orange;");
                            break;
                        case 1:
                            region.setStyle("-fx-background-color: yellow;");
                            break;
                        case 2:
                            region.setStyle("-fx-background-color: blue;");
                            break;
                    }

                    region.setPrefSize(8, 8);
                    view.addNode(pos, region);
                }

            });

            menu.getItems().add(item);
        }

        view.setContextMenu(menu);
        view.setPrefSize(200, 300);
        return view;
    }

    @Override
    public String getSampleDescription() {
        return "This view is used to display a single entry in a day view.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DayEntryView.class;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
