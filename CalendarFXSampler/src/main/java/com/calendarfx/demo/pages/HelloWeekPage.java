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
import com.calendarfx.view.page.WeekPage;

public class HelloWeekPage extends CalendarFXDateControlSample {

	private WeekPage weekPage;

	@Override
	protected DateControl createControl() {
        CalendarSource calendarSource = new CalendarSource("My Calendars");
        calendarSource.getCalendars().add(new Calendar("Test"));

        weekPage = new WeekPage();
        weekPage.getCalendarSources().add(calendarSource);

        return weekPage;
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return WeekPage.class;
	}

	@Override
	public String getSampleName() {
		return "Week Page";
	}

	@Override
	public String getSampleDescription() {
		return "The week page displays a week view.";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
