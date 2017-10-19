/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.YearMonthView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

public class HelloYearMonthView extends CalendarFXDateControlSample {

	private YearMonthView yearMonthView;

	@Override
	public String getSampleName() {
		return "Year Month View";
	}

	@Override
	protected DateControl createControl() {
		yearMonthView = new YearMonthView();

		CalendarSource calendarSource = new CalendarSource();
		calendarSource.getCalendars().add(new HelloCalendar(yearMonthView.getYearMonth()));

		yearMonthView.getCalendarSources().add(calendarSource);

		return yearMonthView;
	}

	@Override
	public String getSampleDescription() {
		return "The month view displays the month of a given date.";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return YearMonthView.class;
	}

	class HelloCalendar extends Calendar {

		public HelloCalendar(YearMonth month) {
			createEntries(month);
		}

		private void createEntries(YearMonth month) {
			for (int i = 1; i < 28; i++) {

				LocalDate date = month.atDay(i);

				for (int j = 0; j < (int) (Math.random() * 7); j++) {
					Entry<?> entry = new Entry<>();
					entry.changeStartDate(date);
					entry.changeEndDate(date);

					entry.setTitle("Entry " + (j + 1));

					int hour = (int) (Math.random() * 23);
					int durationInHours = Math.min(24 - hour,
							(int) (Math.random() * 4));

					LocalTime startTime = LocalTime.of(hour, 0);
					LocalTime endTime = startTime.plusHours(durationInHours);

					entry.changeStartTime(startTime);
					entry.changeEndTime(endTime);

					if (Math.random() < .3) {
						entry.setFullDay(true);
					}

					entry.setCalendar(this);
				}
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
