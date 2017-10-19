/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import impl.com.calendarfx.view.DayEntryViewSkin;
import javafx.scene.control.Skin;

import com.calendarfx.model.Entry;

/**
 * A view representing an entry inside the {@link DayView} control. Instances of
 * this type are created by the {@link DayView} itelf via a pluggable factory.
 * The image below shows the default apperance of this view.
 * <p/>
 * <center><img src="doc-files/day-entry-view.png"></center>
 * <p/>
 *
 * @see DayView#entryViewFactoryProperty()
 */
public class DayEntryView extends EntryViewBase<DayView> {

	/**
	 * Constructs a new entry view for the given calendar entry.
	 *
	 * @param entry the entry for which the view will be created
	 */
	public DayEntryView(Entry<?> entry) {
		super(entry);

//		setMinSize(0, 0);
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new DayEntryViewSkin(this);
	}
}
