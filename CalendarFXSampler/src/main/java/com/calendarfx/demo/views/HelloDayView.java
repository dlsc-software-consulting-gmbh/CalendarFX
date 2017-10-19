/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase;
import impl.com.calendarfx.view.CalendarPropertySheet;
import javafx.scene.Node;

import java.time.LocalDate;
import java.time.LocalTime;

public class HelloDayView extends CalendarFXSample {

	private DayView dayView = new DayView();

	@Override
	public String getSampleName() {
		return "Day View";
	}

	@Override
	public Node getControlPanel() {
		return new CalendarPropertySheet(dayView.getPropertySheetItems());
	}

	protected Node createControl() {
		CalendarSource calendarSource = new CalendarSource();
		calendarSource.getCalendars().add(new HelloDayViewCalendar());
		dayView.getCalendarSources().setAll(calendarSource);
		dayView.setHoursLayoutStrategy(DayViewBase.HoursLayoutStrategy.FIXED_HOUR_HEIGHT);
		dayView.setHourHeight(20);
		dayView.setPrefWidth(200);
		return dayView;
	}

	@Override
	public String getSampleDescription() {
		return "The day view displays a single day.";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return DayView.class;
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
				int durationInHours = Math.max(1, Math.min(24 - hour,
						(int) (Math.random() * 4)));

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
