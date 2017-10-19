/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.pages;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.page.YearPage;

public class HelloYearPage extends CalendarFXDateControlSample {

	private YearPage yearPage;

	@Override
	protected DateControl createControl() {
        CalendarSource calendarSource = new CalendarSource("My Calendars");
        calendarSource.getCalendars().add(new Calendar("Test"));

        yearPage = new YearPage();
        yearPage.getCalendarSources().add(calendarSource);

        return yearPage;
	}

	@Override
	public String getSampleName() {
		return "Year Page";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return YearPage.class;
	}

	@Override
	public String getSampleDescription() {
		return "The year page displays the calendar information for a full year.";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
