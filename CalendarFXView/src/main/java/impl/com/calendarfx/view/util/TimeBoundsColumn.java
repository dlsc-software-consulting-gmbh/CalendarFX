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
import com.calendarfx.view.DraggedEntry;
import com.calendarfx.view.EntryViewBase;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("javadoc")
public final class TimeBoundsColumn {

    private List<EntryViewBase<?>> entryViewBases;

    public void add(EntryViewBase<?> view) {
        if (entryViewBases == null) {
            entryViewBases = new ArrayList<>();
        }

        entryViewBases.add(view);
    }

    public boolean hasRoomFor(EntryViewBase<?> view) {
        if (entryViewBases == null) {
            return true;
        }

        Entry<?> entry = view.getEntry();
        ZonedDateTime entryStartTime = entry.getStartAsZonedDateTime();
        ZonedDateTime entryEndTime = entry.getEndAsZonedDateTime();

        if (entry.isFullDay()) {
            entryStartTime = entryStartTime.with(LocalTime.MIN);
            entryEndTime = entryEndTime.with(LocalTime.MAX);
        }

        for (EntryViewBase<?> otherView : entryViewBases) {

            if (isSameEntry(view, otherView)) {
                continue;
            }

            Entry<?> otherEntry = otherView.getEntry();
            ZonedDateTime otherEntryStartTime = otherEntry.getStartAsZonedDateTime();
            ZonedDateTime otherEntryEndTime = otherEntry.getEndAsZonedDateTime();

            if (entry.isFullDay()) {
                otherEntryStartTime = otherEntryStartTime.with(LocalTime.MIN);
                otherEntryEndTime = otherEntryEndTime.with(LocalTime.MAX);
            }

            if (Util.intersect(entryStartTime, entryEndTime,
                    otherEntryStartTime, otherEntryEndTime)) {

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
