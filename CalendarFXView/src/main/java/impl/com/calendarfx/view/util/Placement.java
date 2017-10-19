/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.util;

import java.util.Objects;

import com.calendarfx.view.EntryViewBase;

@SuppressWarnings("javadoc")
public final class Placement {

	private int columnIndex;

	private int columnCount;

	private EntryViewBase<?> entryViewBase;

	public Placement(EntryViewBase<?> activity, int columnIndex, int columnCount) {
		this.entryViewBase = Objects.requireNonNull(activity);
		this.columnIndex = columnIndex;
		this.columnCount = columnCount;
	}

	public EntryViewBase<?> getEntryView() {
		return entryViewBase;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public String toString() {
		return "Placement [columnIndex=" + columnIndex + ", columnCount=" //$NON-NLS-1$ //$NON-NLS-2$
				+ columnCount + ", entry=" + entryViewBase.getEntry() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
