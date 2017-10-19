/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.pages;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.page.DayPage;

public class HelloDayPage extends CalendarFXDateControlSample {

	private DayPage dayPage;

	@Override
	protected DateControl createControl() {
        CalendarSource calendarSource = new CalendarSource("My Calendars");
		final Calendar calendar = new Calendar("Calendar");
		calendar.setShortName("C");
		calendar.setStyle(Style.STYLE2);
		calendarSource.getCalendars().add(calendar);

        dayPage = new DayPage();
        dayPage.getCalendarSources().add(calendarSource);

		return dayPage;
	}

	@Override
	public String getSampleName() {
		return "Day Page";
	}

	@Override
	public String getSampleDescription() {
		return "The day page displays the calendar information for a single day.";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return DayPage.class;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
