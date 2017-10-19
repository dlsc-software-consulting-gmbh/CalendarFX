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
import com.calendarfx.view.WeekDayView;

import java.time.LocalDate;
import java.time.LocalTime;

public class HelloWeekDayView extends CalendarFXDateControlSample {

	private WeekDayView weekDayView;

	@Override
	public String getSampleName() {
		return "Week Day View";
	}

	@Override
	protected DateControl createControl() {
		CalendarSource calendarSource = new CalendarSource();
		calendarSource.getCalendars().add(new HelloDayViewCalendar());

        weekDayView = new WeekDayView();
		weekDayView.getCalendarSources().add(calendarSource);

		return weekDayView;
	}

	@Override
	public String getSampleDescription() {
		return "The day view displays a single day.";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return WeekDayView.class;
	}

	class HelloDayViewCalendar extends Calendar {

		public HelloDayViewCalendar() {
			createEntries(LocalDate.now());
		}

		private void createEntries(LocalDate startDate) {
			for (int j = 0; j < 5 + (int) (Math.random() * 7); j++) {
				Entry<?> entry = new Entry<>();
				entry.changeStartDate(startDate);
				entry.changeEndDate(startDate);

				entry.setTitle("Entry " + (j + 1));

				int hour = (int) (Math.random() * 23);
				int durationInHours = Math.min(24 - hour,
						(int) (Math.random() * 4));

				LocalTime startTime = LocalTime.of(hour, 0);
				LocalTime endTime = startTime.plusHours(durationInHours);

				entry.changeStartTime(startTime);
				entry.changeEndTime(endTime);

				entry.setCalendar(this);
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
