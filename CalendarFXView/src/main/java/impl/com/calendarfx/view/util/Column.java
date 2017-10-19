/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.util;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.calendarfx.model.Entry;
import com.calendarfx.view.DraggedEntry;
import com.calendarfx.view.EntryViewBase;

@SuppressWarnings("javadoc")
public final class Column {

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
