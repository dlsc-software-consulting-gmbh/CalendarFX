/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
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
public final class Cluster {

	private List<EntryViewBase<?>> entryViews;

	private ZonedDateTime startTime;

	private ZonedDateTime endTime;

	private List<Column> columns;

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

		return entryStartTime.isBefore(endTime)
				&& entryEndTime.isAfter(startTime);

	}

	public List<Placement> resolve() {
		if (entryViews == null || entryViews.isEmpty()) {
			return Collections.emptyList();
		}

		columns = new ArrayList<>();
		columns.add(new Column());

		for (EntryViewBase<?> view : entryViews) {

			boolean added = false;

			// Try to add the activity to an existing column.
			for (Column column : columns) {
				if (column.hasRoomFor(view)) {
					column.add(view);
					added = true;
					break;
				}
			}

			// No column found, create a new column.
			if (!added) {
				Column column = new Column();
				columns.add(column);
				column.add(view);
			}
		}

		final List<Placement> placements = new ArrayList<>();
		final int colCount = columns.size();

		for (int col = 0; col < columns.size(); col++) {
			Column column = columns.get(col);
			for (EntryViewBase<?> view : column.getEntryViews()) {
				placements.add(new Placement(view, col, colCount));
			}
		}

		return placements;
	}

	public List<Column> getColumns() {
		return columns;
	}
}
