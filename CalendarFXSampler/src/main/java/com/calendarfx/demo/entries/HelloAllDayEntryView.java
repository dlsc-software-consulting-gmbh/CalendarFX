/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.entries;

import com.calendarfx.model.Entry;
import com.calendarfx.view.AllDayEntryView;
import com.calendarfx.view.EntryViewBase;

public class HelloAllDayEntryView extends HelloEntryViewBase {

	@Override
	public String getSampleName() {
		return "All Day Entry View";
	}

	@Override
	protected EntryViewBase<?> createEntryView(Entry<?> entry) {
		AllDayEntryView view = new AllDayEntryView(entry);
		view.setPrefSize(400, 20);
		return view;
	}

	@Override
	public String getSampleDescription() {
		return "This view is used to display a single entry in an all day view.";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return AllDayEntryView.class;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
