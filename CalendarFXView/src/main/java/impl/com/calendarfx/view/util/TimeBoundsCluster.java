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
import com.calendarfx.view.EntryViewBase;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("javadoc")
public final class TimeBoundsCluster {

    private List<EntryViewBase<?>> entryViews;

    private ZonedDateTime startTime;

    private ZonedDateTime endTime;

    private List<TimeBoundsColumn> columns;

    public int getColumnCount() {
        if (columns == null || columns.isEmpty()) {
            return -1;
        }

        return columns.size();
    }

    public void add(EntryViewBase<?> view) {
        if (entryViews == null) {
            entryViews = new ArrayList<>();
        }

        entryViews.add(view);

        Entry<?> entry = view.getEntry();

        ZonedDateTime entryStartTime = entry.getStartAsZonedDateTime();
        ZonedDateTime entryEndTime = entry.getEndAsZonedDateTime();

        if (entry.isFullDay()) {
            entryStartTime = entryStartTime.with(LocalTime.MIN);
            entryEndTime = entryEndTime.with(LocalTime.MAX);
        }

        if (startTime == null || entryStartTime.isBefore(startTime)) {
            startTime = entryStartTime;
        }

        if (endTime == null || entryEndTime.isAfter(endTime)) {
            endTime = entryEndTime;
        }
    }

    public boolean intersects(EntryViewBase<?> view) {
        if (startTime == null) {
            /*
             * The first added activity initializes the cluster.
             */
            return true;
        }

        Entry<?> entry = view.getEntry();

        ZonedDateTime entryStartTime = entry.getStartAsZonedDateTime();
        ZonedDateTime entryEndTime = entry.getEndAsZonedDateTime();

        if (entry.isFullDay()) {
            entryStartTime = entryStartTime.with(LocalTime.MIN);
            entryEndTime = entryEndTime.with(LocalTime.MAX);
        }

        return entryStartTime.isBefore(endTime) && entryEndTime.isAfter(startTime);
    }

    public List<Placement> resolve() {
        if (entryViews == null || entryViews.isEmpty()) {
            return Collections.emptyList();
        }

        columns = new ArrayList<>();
        columns.add(new TimeBoundsColumn());

        for (EntryViewBase<?> view : entryViews) {

            boolean added = false;

            // Try to add the activity to an existing column.
            for (TimeBoundsColumn column : columns) {
                if (column.hasRoomFor(view)) {
                    column.add(view);
                    added = true;
                    break;
                }
            }

            // No column found, create a new column.
            if (!added) {
                TimeBoundsColumn column = new TimeBoundsColumn();
                columns.add(column);
                column.add(view);
            }
        }

        final List<Placement> placements = new ArrayList<>();
        final int colCount = columns.size();

        for (int col = 0; col < columns.size(); col++) {
            TimeBoundsColumn column = columns.get(col);
            for (EntryViewBase<?> view : column.getEntryViews()) {
                placements.add(new Placement(view, col, colCount));
            }
        }

        return placements;
    }

    public List<TimeBoundsColumn> getColumns() {
        return columns;
    }
}
