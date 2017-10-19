/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.ical.model;

import net.fortuna.ical4j.model.component.VEvent;

import com.calendarfx.model.Entry;

public class ICalCalendarEntry extends Entry<VEvent> {

	public ICalCalendarEntry(VEvent event) {
		super(event.getSummary().getValue());
	}
}
