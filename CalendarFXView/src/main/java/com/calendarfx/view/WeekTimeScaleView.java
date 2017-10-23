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

import impl.com.calendarfx.view.WeekTimeScaleViewSkin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Skin;

/**
 * A specialization of the regular {@link TimeScaleView} to support a reference to the
 * {@link WeekView} where this scale is being used.
 */
public class WeekTimeScaleView extends TimeScaleView {

    /**
     * Constructs a new scale view.
     */
    public WeekTimeScaleView() {
        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("week.view")) { //$NON-NLS-1$
                    detailedWeekView.set((DetailedWeekView) change.getValueAdded());
                }
            }
        };

        getProperties().addListener(propertiesListener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WeekTimeScaleViewSkin(this);
    }

    private final ReadOnlyObjectWrapper<DetailedWeekView> detailedWeekView = new ReadOnlyObjectWrapper<>(this, "detailedWeekView"); //$NON-NLS-1$

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
}
