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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

/**
 * Common superclass for all controls in the calendar framework.
 */
public abstract class CalendarFXControl extends Control {

    private static String stylesheet;

    /**
     * Constructs a new control.
     */
    protected CalendarFXControl() {
    }

    @Override
    public final String getUserAgentStylesheet() {
        if (stylesheet == null) {
            stylesheet = CalendarFXControl.class.getResource("calendar.css") //$NON-NLS-1$
                    .toExternalForm();
        }
        return stylesheet;
    }

    /**
     * Returns a list of property items that can be shown by the
     * {@link PropertySheet} of ControlsFX.
     *
     * @return the property sheet items
     */
    public ObservableList<Item> getPropertySheetItems() {
        return FXCollections.observableArrayList();
    }

}
