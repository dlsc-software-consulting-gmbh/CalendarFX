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
import com.calendarfx.view.EntryViewBase.HeightLayoutStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("javadoc")
public final class VisualBoundsResolver {

    static class Range {
        String title;
        double y1;
        double y2;

        @Override
        public String toString() {
            return "title = " + title + ", y1 = " + y1 + ", y2 = " + y2;
        }
    }

    public static Range getRange(EntryViewBase entryView, DayView dayView, double contentWidth) {
        Entry<?> entry = entryView.getEntry();

        Range range = new Range();

        range.y1 = dayView.getLocation(entry.getStartAsZonedDateTime());
        range.y2 = dayView.getLocation(entry.getEndAsZonedDateTime());

        if (entryView.getHeightLayoutStrategy().equals(HeightLayoutStrategy.COMPUTE_PREF_SIZE)) {
            range.y2 = range.y1 + entryView.prefHeight(contentWidth);
        }

        range.title = entry.getTitle();
        return range;
    }


    private static Comparator<EntryViewBase<?>> additionalComparator;

    /**
     * The resolver always sorts entries based on their visual bounds but when
     * entries have the same bounds the application might want to sort them based on
     * additional criteria. This can be implemented this way.
     *
     * @return
     */
    public static Comparator<EntryViewBase<?>> getAdditionalComparator() {
        return additionalComparator;
    }

    public static void setAdditionalComparator(Comparator<EntryViewBase<?>> additionalComparator) {
        VisualBoundsResolver.additionalComparator = additionalComparator;
    }

    public static <T extends EntryViewBase> List<Placement> resolve(List<T> entryViews, DayView dayView, double contentWidth) {
        final Comparator<T> comparator = (o1, o2) -> {

            final Range range1 = getRange(o1, dayView, contentWidth);
            final Range range2 = getRange(o2, dayView, contentWidth);

            if (range1.y1 < range2.y1) {
                return -1;
            } else if (range1.y1 > range2.y1) {
                return +1;
            }

            if (additionalComparator != null) {
                return additionalComparator.compare(o1, o2);
            }

            return 0;
        };

        Collections.sort(entryViews, comparator);

        List<Placement> placements = new ArrayList<>();
        List<VisualBoundsCluster> clusters = new ArrayList<>();

        VisualBoundsCluster cluster = null;

        for (T entryView : entryViews) {
            if (entryView.isVisible()) {
                if (cluster == null || !cluster.intersects(entryView, dayView, contentWidth)) {
                    cluster = new VisualBoundsCluster();
                    clusters.add(cluster);
                }

                cluster.add(entryView, dayView, contentWidth);
            }
        }

        for (VisualBoundsCluster c : new ArrayList<>(clusters)) {
            placements.addAll(c.resolve(dayView, contentWidth));
        }

        return placements;
    }
}
