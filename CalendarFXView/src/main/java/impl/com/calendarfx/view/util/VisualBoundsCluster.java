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
import com.calendarfx.view.EntryViewBase;
import impl.com.calendarfx.view.util.VisualBoundsResolver.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("javadoc")
public final class VisualBoundsCluster {

    private List<EntryViewBase<?>> entryViews;

    private Range clusterRange;

    private List<VisualBoundsColumn> columns;

    public int getColumnCount() {
        if (columns == null || columns.isEmpty()) {
            return -1;
        }

        return columns.size();
    }

    public void add(EntryViewBase<?> entryView, DayView dayView, double contentWidth) {
        if (entryViews == null) {
            entryViews = new ArrayList<>();
        }

        entryViews.add(entryView);

        Entry<?> entry = entryView.getEntry();

        Range entryRange = VisualBoundsResolver.getRange(entryView, dayView, contentWidth);

        if (entry.isFullDay()) {
            entryRange.y1 = 0;
            entryRange.y2 = dayView.getHeight();
        }

        if (clusterRange == null) {
            clusterRange = new Range();
            clusterRange.title = "Cluster Range";
            clusterRange.y1 = entryRange.y1;
            clusterRange.y2 = entryRange.y2;
        } else {
            clusterRange.y1 = Math.min(clusterRange.y1, entryRange.y1);
            clusterRange.y2 = Math.max(clusterRange.y2, entryRange.y2);
        }
    }

    public boolean intersects(EntryViewBase<?> entryView, DayView dayView, double contentWidth) {
        if (clusterRange == null) {
            /*
             * The first added activity initializes the cluster.
             */
            return true;
        }

        Entry<?> entry = entryView.getEntry();

        Range entryRange = VisualBoundsResolver.getRange(entryView, dayView, contentWidth);

        if (entry.isFullDay()) {
            entryRange.y1 = 0;
            entryRange.y2 = dayView.getHeight();
        }

        return entryRange.y1 < clusterRange.y2 && entryRange.y2 > clusterRange.y1;
    }

    public List<Placement> resolve(DayView dayView, double contentWidth) {
        if (entryViews == null || entryViews.isEmpty()) {
            return Collections.emptyList();
        }

        columns = new ArrayList<>();
        columns.add(new VisualBoundsColumn());

        for (EntryViewBase<?> entryView : entryViews) {

            boolean added = false;

            // Try to add the activity to an existing column.
            for (VisualBoundsColumn column : columns) {
                if (column.hasRoomFor(entryView, dayView, contentWidth)) {
                    column.add(entryView);
                    added = true;
                    break;
                }
            }

            // No column found, create a new column.
            if (!added) {
                VisualBoundsColumn column = new VisualBoundsColumn();
                columns.add(column);
                column.add(entryView);
            }
        }

        final List<Placement> placements = new ArrayList<>();
        final int colCount = columns.size();

        for (int col = 0; col < columns.size(); col++) {
            VisualBoundsColumn column = columns.get(col);
            for (EntryViewBase<?> view : column.getEntryViews()) {
                placements.add(new Placement(view, col, colCount));
            }
        }

        return placements;
    }

    public List<VisualBoundsColumn> getColumns() {
        return columns;
    }
}
