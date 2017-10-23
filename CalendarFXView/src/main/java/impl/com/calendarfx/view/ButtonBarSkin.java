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

import com.calendarfx.view.ButtonBar;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;

public class ButtonBarSkin extends SkinBase<ButtonBar> {

    private static final String ONLY_BUTTON = "only-button";
    private static final String LEFT_PILL = "left-pill";
    private static final String CENTER_PILL = "center-pill";
    private static final String RIGHT_PILL = "right-pill";

    private final HBox container = new HBox();

    public ButtonBarSkin(ButtonBar control) {
        super(control);

        this.container.getStyleClass().add("container");

        this.container.setFillHeight(true);

        this.getChildren().add(this.container);
        this.updateButtons();
        this.getButtons().addListener((Observable observable) -> updateButtons());
    }

    private ObservableList<Button> getButtons() {
        return getSkinnable().getButtons();
    }

    private void updateButtons() {
        ObservableList<Button> buttons = this.getButtons();
        this.container.getChildren().clear();

        for (int i = 0; i < this.getButtons().size(); ++i) {
            Button button = buttons.get(i);
            button.getStyleClass().removeAll(ONLY_BUTTON, LEFT_PILL, CENTER_PILL, RIGHT_PILL);
            button.setMaxHeight(Double.MAX_VALUE);
            this.container.getChildren().add(button);
            if (i == buttons.size() - 1) {
                if (i == 0) {
                    button.getStyleClass().add(ONLY_BUTTON);
                } else {
                    button.getStyleClass().add(RIGHT_PILL);
                }
            } else if (i == 0) {
                button.getStyleClass().add(LEFT_PILL);
            } else {
                button.getStyleClass().add(CENTER_PILL);
            }
        }

    }

    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(height);
    }

    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }
}
