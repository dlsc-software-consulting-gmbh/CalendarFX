/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.entries;

import com.calendarfx.model.Entry;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.EntryViewBase;

public class HelloDayEntryView extends HelloEntryViewBase {

    @Override
	public String getSampleName() {
		return "Day Entry View";
	}

	@Override
	protected EntryViewBase<?> createEntryView(Entry<?> entry) {
		DayEntryView view = new DayEntryView(entry);
		view.setPrefSize(200, 300);
		return view;
	}

	@Override
	public String getSampleDescription() {
		return "This view is used to display a single entry in a day view.";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return DayEntryView.class;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
