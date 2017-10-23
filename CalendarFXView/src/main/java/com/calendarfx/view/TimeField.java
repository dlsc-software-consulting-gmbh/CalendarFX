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

import impl.com.calendarfx.view.TimeFieldSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

import java.time.LocalTime;
import java.util.Optional;

/**
 * A control for editing a {@link LocalTime}.
 * <p/>
 * <center><img src="doc-files/time-field.png"></center>
 * <p/>
 */
public class TimeField extends CalendarFXControl {

    /**
     * Constructs a new field.
     */
    public TimeField() {
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimeFieldSkin(this);
    }

    private final BooleanProperty rollOver = new SimpleBooleanProperty(this,
            "rollOver", true); //$NON-NLS-1$

    /**
     * Determines if the field for hours will jump from 23 to 0 when the user
     * increases the hour value (and vice versa when decreasing). Same for
     * minutes.
     *
     * @return the roll over flag
     */
    public final BooleanProperty rollOverProperty() {
        return rollOver;
    }

    /**
     * Returns the value of {@link #rollOverProperty()}.
     *
     * @return true if the fields will roll over
     */
    public final boolean isRollOver() {
        return rollOver.get();
    }

    /**
     * Sets the value of {@link #rollOverProperty()}.
     *
     * @param roll if true the fields will roll over (e.g. from 23 to 0 or from 59 to 0).
     */
    public final void setRollOver(boolean roll) {
        rollOver.set(roll);
    }

    private final ObjectProperty<LocalTime> value = new SimpleObjectProperty<>(
            this, "value", LocalTime.now()); //$NON-NLS-1$

    /**
     * The current local time value of the field.
     *
     * @return the local time value
     */
    public final ObjectProperty<LocalTime> valueProperty() {
        return this.value;
    }

    /**
     * Returns the value of {@link #valueProperty()}.
     *
     * @return the local time value
     */
    public final LocalTime getValue() {
        return valueProperty().get();
    }

    /**
     * Sets the value of {@link #valueProperty()}.
     *
     * @param localTime the new time
     */
    public final void setValue(LocalTime localTime) {
        valueProperty().set(localTime);
    }

    private static final String TIME_FIELD_CATEGORY = "Time Field"; //$NON-NLS-1$

    /**
     * Returns a list of property items that can be shown by the
     * {@link PropertySheet} of ControlsFX.
     *
     * @return the property sheet items
     */
    public ObservableList<Item> getPropertySheetItems() {

        ObservableList<Item> items = FXCollections.observableArrayList();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(rollOverProperty());
            }

            @Override
            public void setValue(Object value) {
                setRollOver((boolean) value);
            }

            @Override
            public Object getValue() {
                return isRollOver();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Roll Over"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Roll Over"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return TIME_FIELD_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(valueProperty());
            }

            @Override
            public void setValue(Object value) {
                TimeField.this.setValue((LocalTime) value);
            }

            @Override
            public Object getValue() {
                return TimeField.this.getValue();
            }

            @Override
            public Class<?> getType() {
                return LocalTime.class;
            }

            @Override
            public String getName() {
                return "Value"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Local Time"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return TIME_FIELD_CATEGORY;
            }
        });

        return items;
    }
}
