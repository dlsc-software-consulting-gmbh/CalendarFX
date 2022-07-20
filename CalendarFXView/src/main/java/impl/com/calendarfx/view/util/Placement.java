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

package impl.com.calendarfx.view.util;

import com.calendarfx.view.EntryViewBase;

import java.util.Objects;

@SuppressWarnings("javadoc")
public final class Placement {

    private final int columnIndex;

    private final int columnCount;

    private final EntryViewBase<?> entryViewBase;

    public Placement(EntryViewBase<?> activity, int columnIndex, int columnCount) {
        this.entryViewBase = Objects.requireNonNull(activity);
        this.columnIndex = columnIndex;
        this.columnCount = columnCount;
    }

    public EntryViewBase<?> getEntryView() {
        return entryViewBase;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public String toString() {
        return "Placement [columnIndex=" + columnIndex + ", columnCount="
                + columnCount + ", entry=" + entryViewBase.getEntry() + "]";
    }
}
