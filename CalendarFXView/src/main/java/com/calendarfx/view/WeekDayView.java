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

import impl.com.calendarfx.view.WeekDayViewSkin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Skin;

/**
 * A specialization of the regular {@link DayView} in order to support some customized
 * styling / customized behaviour. Additionally this view always has a reference to the
 * {@link WeekView} where it is being used.
 */
public class WeekDayView extends DayView {

    private static final String WEEKDAY_VIEW = "weekday-view"; //$NON-NLS-1$

    /**
     * Constructs a new day view.
     */
    public WeekDayView() {
        getStyleClass().add(WEEKDAY_VIEW);

        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("week.view")) { //$NON-NLS-1$
                    WeekView view = (WeekView) change.getValueAdded();
                    weekView.set(view);
                }
            }
        };

        getProperties().addListener(propertiesListener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WeekDayViewSkin(this);
    }

    private final ReadOnlyObjectWrapper<WeekView> weekView = new ReadOnlyObjectWrapper<>(this, "weekView"); //$NON-NLS-1$

    /**
     * The week view where the view is being used.
     *
     * @return the week view
     */
    public final ReadOnlyObjectProperty<WeekView> weekViewProperty() {
        return weekView;
    }

    /**
     * Returns the value of {@link #weekViewProperty()}.
     *
     * @return the week view
     */
    public final WeekView getWeekView() {
        return weekView.get();
    }
}
