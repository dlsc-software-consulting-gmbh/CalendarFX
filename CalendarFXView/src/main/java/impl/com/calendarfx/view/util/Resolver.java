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
import java.util.Collections;
import java.util.List;

@SuppressWarnings("javadoc")
public final class Resolver {

    protected Resolver() {
    }

    public static <T extends EntryViewBase<?>> List<Placement> resolve(
            List<T> entryViews) {
        Collections.sort(entryViews);

        List<Placement> placements = new ArrayList<>();
        List<Cluster> clusters = new ArrayList<>();

        Cluster cluster = null;

        for (T view : entryViews) {
            if (view.isVisible()) {
                if (cluster == null || !cluster.intersects(view)) {
                    cluster = new Cluster();
                    clusters.add(cluster);
                }

                cluster.add(view);
            }
        }

        for (Cluster c : new ArrayList<>(clusters)) {
            placements.addAll(c.resolve());
        }

        return placements;
    }
}
