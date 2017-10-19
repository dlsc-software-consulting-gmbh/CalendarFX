/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.popover;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.popover.EntryDetailsView;

import javafx.scene.Node;

public class HelloEntryDetailsView extends CalendarFXSample {

	@Override
	public String getSampleName() {
		return "Entry Details";
	}

	@Override
	protected Node createControl() {
		Entry<String> entry = new Entry<>("Hello Entry");
		entry.setCalendar(new Calendar("Dummy Calendar"));
		return new EntryDetailsView(entry);
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return EntryDetailsView.class;
	}

	@Override
	public String getSampleDescription() {
		return "A view used to edit various properties of a calendar entry.";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
