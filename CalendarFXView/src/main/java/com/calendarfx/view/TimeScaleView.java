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

import impl.com.calendarfx.view.TimeScaleViewSkin;
import javafx.scene.control.Skin;

/**
 * A control used for displaying a vertical time scale.
 * <p/>
 * <center><img src="doc-files/time-scale-view.png"></center>
 * <p/>
 */
public class TimeScaleView extends DayViewBase {

    /**
     * Constructs a new scale view.
     */
    public TimeScaleView() {
        getStyleClass().add("time-scale"); //$NON-NLS-1$
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimeScaleViewSkin<>(this);
    }
}
