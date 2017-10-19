/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
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

        for(int i = 0; i < this.getButtons().size(); ++i) {
            Button button = buttons.get(i);
            button.getStyleClass().removeAll(ONLY_BUTTON, LEFT_PILL, CENTER_PILL, RIGHT_PILL);
            button.setMaxHeight(Double.MAX_VALUE);
            this.container.getChildren().add(button);
            if(i == buttons.size() - 1) {
                if(i == 0) {
                    button.getStyleClass().add(ONLY_BUTTON);
                } else {
                    button.getStyleClass().add(RIGHT_PILL);
                }
            } else if(i == 0) {
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
