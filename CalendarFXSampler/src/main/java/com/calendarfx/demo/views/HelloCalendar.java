/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

/**
 * Created by gdiaz on 26/11/2016.
 */
class HelloCalendar extends Calendar {

	public HelloCalendar () {
		for (int i = 1; i < 28; i++) {

			LocalDate date = YearMonth.now().atDay(i);

			for (int j = 0; j < (int) (Math.random() * 7); j++) {
				Entry<?> entry = new Entry<>();
				entry.changeStartDate(date);
				entry.changeEndDate(date.plusDays((int) (Math.random() * 4)));

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
