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

import impl.com.calendarfx.view.DeveloperConsoleSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TabPane;

/**
 * A control used for showing the internals of CalendarFX at work. Helps
 * detecting problems. Developers can freely add their own tabs to the tab pane.
 * <h3>Screenshot</h3>
 * <center><img alt="developer console" src="doc-files/developer-console.png"></center>
 */
public class DeveloperConsole extends CalendarFXControl {

    private static final String DEFAULT_STYLE_CLASS = "developer-console"; //$NON-NLS-1$

    private TabPane tabPane = new TabPane();

    /**
     * Constructs a new view.
     */
    public DeveloperConsole() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        this.tabPane = new TabPane();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DeveloperConsoleSkin(this);
    }

    /**
     * Returns the tab pane used by the console to display different sections.
     * Subclasses can simply add their own tabs.
     *
     * @return the tab pane
     */
    public final TabPane getTabPane() {
        return tabPane;
    }

    private final ObjectProperty<DateControl> dateControl = new SimpleObjectProperty<>(
            this, "dateControl"); //$NON-NLS-1$

    /**
     * Stores a reference to the date control that will be monitored by the
     * console.
     *
     * @return the date control
     */
    public final ObjectProperty<DateControl> dateControlProperty() {
        return dateControl;
    }

    /**
     * Sets the value of {@link #dateControlProperty()}.
     *
     * @param control
     *            the control
     */
    public final void setDateControl(DateControl control) {
        dateControlProperty().set(control);
    }

    /**
     * Returns the value of {@link #dateControlProperty()}.
     *
     * @return the date control
     */
    public final DateControl getDateControl() {
        return dateControlProperty().get();
    }
}
