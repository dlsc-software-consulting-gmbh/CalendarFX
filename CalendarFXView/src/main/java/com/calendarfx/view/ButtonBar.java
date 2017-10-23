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

package com.calendarfx.view;

import impl.com.calendarfx.view.ButtonBarSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;

/**
 * A segmented button bar control based on the segmented button control
 * found in ControlsFX but this one working with regular buttons and not
 * toggle buttons.
 * <center><img src="doc-files/button-bar.png"></center>
 */
public class ButtonBar extends CalendarFXControl {

    private final ObservableList<Button> buttons;

    /**
     * Constructs a new button bar.
     */
    public ButtonBar() {
        this((ObservableList<Button>) null);
    }

    /**
     * Constructs a new button bar for the given buttons.
     *
     * @param buttons the buttons to add to the control
     */
    public ButtonBar(Button... buttons) {
        this(buttons == null ? FXCollections.observableArrayList() : FXCollections.observableArrayList(buttons));
    }

    /**
     * Constructs a new button bar for the given buttons.
     *
     * @param buttons the buttons to add to the control
     */
    public ButtonBar(ObservableList<Button> buttons) {
        this.getStyleClass().add("button-bar");
        this.buttons = buttons == null ? FXCollections.observableArrayList() : buttons;
        this.setFocusTraversable(false);
    }

    protected Skin<?> createDefaultSkin() {
        return new ButtonBarSkin(this);
    }

    /**
     * The list of buttons managed by this button bar.
     *
     * @return the list of buttons managed by the control
     */
    public final ObservableList<Button> getButtons() {
        return this.buttons;
    }
}
