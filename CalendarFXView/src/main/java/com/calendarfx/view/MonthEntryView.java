/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import impl.com.calendarfx.view.MonthEntryViewSkin;
import javafx.scene.control.Skin;

import com.calendarfx.model.Entry;

/**
 * A specialized entry view used by the {@link MonthView}.
 */
public class MonthEntryView extends EntryViewBase<MonthView> {

	/**
	 * Constructs a new entry view.
	 *
	 * @param entry
	 *            the calendar entry for which the view will be created
	 */
	public MonthEntryView(Entry<?> entry) {
		super(entry);

		setMouseTransparent(false);
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new MonthEntryViewSkin(this);
	}
}
