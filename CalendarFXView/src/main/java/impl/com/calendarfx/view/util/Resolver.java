/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.calendarfx.view.EntryViewBase;

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
