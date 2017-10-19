/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import com.calendarfx.model.Entry;

import impl.com.calendarfx.view.AllDayEntryViewSkin;
import javafx.scene.control.Skin;

/**
 * An entry view specialized for display in the {@link AllDayView} control.
 */
public class AllDayEntryView extends EntryViewBase<AllDayView> {

	/**
	 * Constructs a new entry.
	 *
	 * @param entry the entry for which the view will be created
	 */
	public AllDayEntryView(Entry<?> entry) {
		super(entry);
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new AllDayEntryViewSkin(this);
	}
}
