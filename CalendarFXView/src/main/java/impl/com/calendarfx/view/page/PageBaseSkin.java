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

package impl.com.calendarfx.view.page;

import com.calendarfx.view.Messages;
import com.calendarfx.view.page.PageBase;
import impl.com.calendarfx.view.NavigateDateView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;

public abstract class PageBaseSkin<C extends PageBase> extends SkinBase<C> {

    private final Text dateText;
    private final BorderPane headerPane;
    private final BorderPane borderPane;

    public PageBaseSkin(C page) {
        super(page);

        // Navigation

        NavigateDateView navigateDateButton = new NavigateDateView();
        navigateDateButton.getTodayButton().setText(Messages.getString("PageBaseSkin.TODAY"));
        navigateDateButton.setOnBackward(() -> page.goBack());
        navigateDateButton.setOnForward(() -> page.goForward());
        navigateDateButton.setOnToday(() -> page.goToday());

        navigateDateButton.visibleProperty().bind(page.showNavigationProperty());

        // Date label
        this.dateText = new Text("Date");
        this.dateText.getStyleClass().add("date-text");
        this.dateText.visibleProperty().bind(page.showDateProperty());
        page.dateProperty().addListener(evt -> updateDateText());

        BorderPane.setMargin(navigateDateButton, new Insets(10));
        BorderPane.setMargin(dateText, new Insets(10));
        BorderPane.setAlignment(navigateDateButton, Pos.CENTER_LEFT);
        BorderPane.setAlignment(dateText, Pos.CENTER_RIGHT);

        headerPane = new BorderPane();
        headerPane.getStyleClass().add("header");
        headerPane.setLeft(navigateDateButton);
        headerPane.setRight(dateText);

        Node content = createContent();
        content.getStyleClass().add("content");
        content.sceneProperty().addListener(it -> {
            if (content.getScene() != null) {
                content.applyCss();
            }
        });

        borderPane = new BorderPane();
        borderPane.getStyleClass().add("container");
        borderPane.setCenter(content);

        getChildren().add(borderPane);

        updateDateText();
        updateHeaderVisibility();

        page.showDateProperty().addListener(it -> updateHeaderVisibility());
        page.showNavigationProperty().addListener(it -> updateHeaderVisibility());
    }

    private void updateHeaderVisibility() {
        if (getSkinnable().isShowDate() || getSkinnable().isShowNavigation()) {
            borderPane.setTop(headerPane);
        } else {
            borderPane.setTop(null);
        }
    }

    private void updateDateText() {
        DateTimeFormatter formatter = getSkinnable().getDateTimeFormatter();
        String text = formatter.format(getSkinnable().getDate());
        dateText.setText(text);
    }

    protected abstract Node createContent();
}
