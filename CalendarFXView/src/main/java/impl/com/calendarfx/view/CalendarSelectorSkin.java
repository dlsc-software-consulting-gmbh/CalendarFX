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
import com.calendarfx.view.CalendarSelector;
import com.calendarfx.view.CalendarView;
import javafx.beans.Observable;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleGroup;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("javadoc")
public class CalendarSelectorSkin extends SkinBase<CalendarSelector> {

    private MenuButton button;
    private Rectangle buttonIcon;

    public CalendarSelectorSkin(CalendarSelector selector) {
        super(selector);

        buttonIcon = new Rectangle(12, 12);

        button = new MenuButton();
        button.setGraphic(buttonIcon);
        button.getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());

        getChildren().add(button);

        selector.calendarProperty().addListener(it -> updateButton());

        selector.getCalendars().addListener(
                (Observable evt) -> updateMenuItems());

        updateMenuItems();
        updateButton();
    }

    private void updateButton() {
        Calendar calendar = getSkinnable().getCalendar();
        if (calendar != null) {
            buttonIcon.getStyleClass().setAll(calendar.getStyle() + "-icon");
        } else {
            buttonIcon.getStyleClass().clear();
        }
    }

    private void updateMenuItems() {
        ToggleGroup group = new ToggleGroup();
        List<MenuItem> items = new ArrayList<>();
        for (Calendar calendar : getSkinnable().getCalendars()) {
            RadioMenuItem item = new RadioMenuItem(calendar.getName());
            Rectangle icon = new Rectangle(10, 10);
            icon.setArcHeight(2);
            icon.setArcWidth(2);
            icon.getStyleClass().add(calendar.getStyle() + "-icon");
            item.setGraphic(icon);
            item.setDisable(calendar.isReadOnly());
            item.setOnAction(evt -> getSkinnable().setCalendar(calendar));
            group.getToggles().add(item);
            items.add(item);
            if (calendar.equals(getSkinnable().getCalendar())) {
                item.setSelected(true);
            }
        }

        button.getItems().setAll(items);
    }
}
