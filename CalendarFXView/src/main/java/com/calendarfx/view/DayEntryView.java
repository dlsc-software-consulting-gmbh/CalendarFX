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
import impl.com.calendarfx.view.DayEntryViewSkin;
import javafx.scene.control.Skin;

/**
 * A view representing an entry inside the {@link DayView} control. Instances of
 * this type are created by the {@link DayView} itelf via a pluggable factory.
 * The image below shows the default apperance of this view.
 * <p/>
 * <center><img src="doc-files/day-entry-view.png"></center>
 * <p/>
 *
 * @see DayView#entryViewFactoryProperty()
 */
public class DayEntryView extends EntryViewBase<DayView> {

    /**
     * Constructs a new entry view for the given calendar entry.
     *
     * @param entry the entry for which the view will be created
     */
    public DayEntryView(Entry<?> entry) {
        super(entry);

//		setMinSize(0, 0);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DayEntryViewSkin(this);
    }
}
