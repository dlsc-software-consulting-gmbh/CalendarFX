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
import impl.com.calendarfx.view.MonthEntryViewSkin;
import javafx.scene.control.Skin;

/**
 * A specialized entry view used by the {@link MonthView}.
 */
public class MonthEntryView extends EntryViewBase<MonthView> {

    /**
     * Constructs a new entry view.
     *
     * @param entry
     *            the calendar entry for which the view will be created
     */
    public MonthEntryView(Entry<?> entry) {
        super(entry);

        setMouseTransparent(false);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MonthEntryViewSkin(this);
    }
}
