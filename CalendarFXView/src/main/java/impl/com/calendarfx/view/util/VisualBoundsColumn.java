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

import com.calendarfx.model.Entry;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DraggedEntry;
import com.calendarfx.view.EntryViewBase;
import impl.com.calendarfx.view.util.VisualBoundsResolver.Range;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("javadoc")
public final class VisualBoundsColumn {

    private List<EntryViewBase<?>> entryViewBases;

    public void add(EntryViewBase<?> view) {
        if (entryViewBases == null) {
            entryViewBases = new ArrayList<>();
        }

        entryViewBases.add(view);
    }

    public boolean hasRoomFor(EntryViewBase<?> entryView, DayView dayView, double contentWidth) {
        if (entryViewBases == null) {
            return true;
        }

        Entry<?> entry = entryView.getEntry();

        Range entryRange = VisualBoundsResolver.getRange(entryView, dayView, contentWidth);

        if (entry.isFullDay()) {
            entryRange.y1 = 0;
            entryRange.y2 = dayView.getHeight();
        }

        for (EntryViewBase<?> otherView : entryViewBases) {

            if (isSameEntry(entryView, otherView)) {
                continue;
            }

            Range otherRange = VisualBoundsResolver.getRange(otherView, dayView, contentWidth);

            if (entry.isFullDay()) {
                otherRange.y1 = 0;
                otherRange.y2 = dayView.getHeight();
            }

            if (entryRange.y1 < otherRange.y2 && entryRange.y2 > otherRange.y1) {

                /*
                 * The two activities intersect, so we can not use this column
                 * for the passed activity.
                 */
                return false;
            }
        }

        return true;
    }

    private boolean isSameEntry(EntryViewBase<?> viewA, EntryViewBase<?> viewB) {
        Entry<?> entryA = viewA.getEntry();
        Entry<?> entryB = viewB.getEntry();

        if (entryA instanceof DraggedEntry) {
            return isSameEntry((DraggedEntry) entryA, entryB);
        }

        if (entryB instanceof DraggedEntry) {
            return isSameEntry((DraggedEntry) entryB, entryA);
        }

        return false;
    }

    private boolean isSameEntry(DraggedEntry draggedEntry, Entry<?> entry) {
        if (entry.isRecurrence()) {
            return draggedEntry.getOriginalEntry().getRecurrenceSourceEntry() == entry.getRecurrenceSourceEntry();
        }

        return draggedEntry.getOriginalEntry() == entry;
    }

    public List<EntryViewBase<?>> getEntryViews() {
        return entryViewBases;
    }
}
