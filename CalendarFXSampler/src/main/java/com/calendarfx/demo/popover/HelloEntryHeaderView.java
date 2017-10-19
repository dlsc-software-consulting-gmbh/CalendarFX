/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.popover;

import java.util.ArrayList;
import java.util.List;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.Entry;
import com.calendarfx.view.popover.EntryHeaderView;

import javafx.scene.Node;

public class HelloEntryHeaderView extends CalendarFXSample {

	@Override
	public String getSampleName() {
		return "Entry Header View";
	}

	@Override
	protected Node createControl() {
		Calendar meetings = new Calendar("Meetings");
		Calendar training = new Calendar("Training");
		Calendar customers = new Calendar("Customers");
		Calendar holidays = new Calendar("Holidays");

		meetings.setStyle(Style.STYLE2);
		training.setStyle(Style.STYLE3);
		customers.setStyle(Style.STYLE4);
		holidays.setStyle(Style.STYLE5);

		List<Calendar> calendars = new ArrayList<>();
		calendars.add(meetings);
		calendars.add(training);
		calendars.add(customers);
		calendars.add(holidays);

		Entry<String> entry = new Entry<>("Hello Header View");
		entry.setCalendar(meetings);

		return new EntryHeaderView(entry, calendars);
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return EntryHeaderView.class;
	}

	@Override
	public String getSampleDescription() {
		return "A view used to select a calendar from a list of calendars.";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
