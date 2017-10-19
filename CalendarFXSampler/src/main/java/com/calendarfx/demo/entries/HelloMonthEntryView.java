/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.entries;

import com.calendarfx.model.Entry;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.MonthEntryView;

import java.time.LocalDate;

public class HelloMonthEntryView extends HelloEntryViewBase {

	public HelloMonthEntryView() {
		super();

		entry.setInterval(LocalDate.now(), LocalDate.now().plusDays(5));
	}

	@Override
	protected EntryViewBase<?> createEntryView(Entry<?> entry) {
		MonthEntryView view = new MonthEntryView(entry);
		view.setPrefSize(400, 20);
		return view;
	}

	@Override
	public String getSampleName() {
		return "Month Entry View";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return MonthEntryView.class;
	}

	@Override
	public String getSampleDescription() {
		return "This view is used to display a single entry in a month view.";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
