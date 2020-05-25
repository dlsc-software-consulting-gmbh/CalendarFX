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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("javadoc")
public final class TimeBoundsResolver {

    public TimeBoundsResolver() {
    }

    private static Comparator<EntryViewBase<?>> additionalComparator;

    /**
     * The resolver always sorts entries based on their time bounds but when
     * entries have the same bounds the application might want to sort them based on
     * additional criteria. This can be implemented this way.
     *
     * @return
     */
    public static Comparator<EntryViewBase<?>> getAdditionalComparator() {
        return additionalComparator;
    }

    public static void setAdditionalComparator(Comparator<EntryViewBase<?>> additionalComparator) {
        TimeBoundsResolver.additionalComparator = additionalComparator;
    }

    public static <T extends EntryViewBase> List<Placement> resolve(List<T> entryViews) {

        final Comparator<T> comparator = (o1, o2) -> {

            int result = o1.compareTo(o2);

            if (result == 0 && additionalComparator != null) {
                return additionalComparator.compare(o1, o2);
            }

            return 0;
        };

        entryViews.sort(comparator);

        List<Placement> placements = new ArrayList<>();
        List<TimeBoundsCluster> clusters = new ArrayList<>();

        TimeBoundsCluster cluster = null;

        for (T view : entryViews) {
            if (view.isVisible()) {
                if (cluster == null || !cluster.intersects(view)) {
                    cluster = new TimeBoundsCluster();
                    clusters.add(cluster);
                }

                cluster.add(view);
            }
        }

        for (TimeBoundsCluster c : new ArrayList<>(clusters)) {
            placements.addAll(c.resolve());
        }

        return placements;
    }
}
