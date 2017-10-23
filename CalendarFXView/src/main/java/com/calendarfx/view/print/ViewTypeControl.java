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

package com.calendarfx.view.print;

import com.calendarfx.view.CalendarFXControl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;

import java.util.Objects;
import java.util.Optional;

/**
 * The abstract superclass of all controls inside the print package that are
 * supporting the different view types listed inside the {@link ViewType}
 * enumerator.
 */
public abstract class ViewTypeControl extends CalendarFXControl {

    // view type support

    private final ObjectProperty<ViewType> viewType = new SimpleObjectProperty<ViewType>(this, "viewType", ViewType.DAY_VIEW) {
        @Override
        public void set(ViewType newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    /**
     * A property used to store the currently active view type (e.g. "day").
     *
     * @return the current view type
     */
    public final ObjectProperty<ViewType> viewTypeProperty() {
        return viewType;
    }

    /**
     * Returns the value of {@link #viewTypeProperty()}.
     *
     * @return the current view type
     */
    public final ViewType getViewType() {
        return viewTypeProperty().get();
    }

    /**
     * Sets the value of {@link #viewTypeProperty()}.
     *
     * @param type the new view type
     */
    public final void setViewType(ViewType type) {
        viewTypeProperty().set(type);
    }

    private static final String VIEW_TYPE_CONTROL = "View Type";

    public ObservableList<PropertySheet.Item> getPropertySheetItems() {
        ObservableList<PropertySheet.Item> items = super.getPropertySheetItems();

        items.add(new PropertySheet.Item() {
            @Override
            public void setValue(Object value) {
                setViewType((ViewType) value);
            }

            @Override
            public Object getValue() {
                return getViewType();
            }

            @Override
            public Class<?> getType() {
                return ViewType.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(viewTypeProperty());
            }

            @Override
            public String getName() {
                return "View Type";
            }

            @Override
            public String getDescription() {
                return "Print View Type";
            }

            @Override
            public String getCategory() {
                return VIEW_TYPE_CONTROL;
            }
        });

        return items;
    }
}
