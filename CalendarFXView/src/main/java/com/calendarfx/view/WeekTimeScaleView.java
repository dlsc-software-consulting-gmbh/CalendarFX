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

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import impl.com.calendarfx.view.WeekTimeScaleViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Skin;

/**
 * A specialization of the regular {@link TimeScaleView} to support a reference
 * to the {@link WeekView} where this scale is being used.
 */
public class WeekTimeScaleView extends TimeScaleView {

    private final ObjectProperty<DateTimeFormatter> formatter = new SimpleObjectProperty<>(
            this, "formatter",
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));

    /**
     * Constructs a new scale view.
     */
    public WeekTimeScaleView() {
        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("week.view")) {
                    detailedWeekView
                            .set((DetailedWeekView) change.getValueAdded());
                }
            }
        };

        getProperties().addListener(propertiesListener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WeekTimeScaleViewSkin(this);
    }

    private final ReadOnlyObjectWrapper<DetailedWeekView> detailedWeekView = new ReadOnlyObjectWrapper<>(
            this, "detailedWeekView");

    /**
     * The week view where this scale is being used.
     *
     * @return the week view
     */
    public final ReadOnlyObjectProperty<DetailedWeekView> detailedWeekViewProperty() {
        return detailedWeekView.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #detailedWeekViewProperty()}.
     *
     * @return the week view
     */
    public final DetailedWeekView getDetailedWeekView() {
        return detailedWeekView.get();
    }

    @Override
    protected ObjectProperty<DateTimeFormatter> timeFormatterProperty() {
        return formatter;
    }

    @Override
    public void setTimeFormatter(DateTimeFormatter formatter) {
        timeFormatterProperty().set(formatter);
    }

}
