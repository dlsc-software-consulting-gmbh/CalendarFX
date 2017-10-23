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

import com.calendarfx.model.Entry;
import impl.com.calendarfx.view.AllDayEntryViewSkin;
import javafx.scene.control.Skin;

/**
 * An entry view specialized for display in the {@link AllDayView} control.
 */
public class AllDayEntryView extends EntryViewBase<AllDayView> {

    /**
     * Constructs a new entry.
     *
     * @param entry the entry for which the view will be created
     */
    public AllDayEntryView(Entry<?> entry) {
        super(entry);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AllDayEntryViewSkin(this);
    }
}
