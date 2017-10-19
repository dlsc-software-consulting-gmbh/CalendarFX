package com.calendarfx.google.converter;

import com.calendarfx.google.model.GoogleCalendar;
import com.google.api.services.calendar.model.Calendar;

import java.time.ZoneId;

/**
 * BeanConverter between calendarfx and google api.
 *
 * Created by gdiaz on 26/03/2017.
 */
public class GoogleCalendarToCalendarConverter  implements BeanConverter<GoogleCalendar, Calendar> {
	@Override
	public Calendar convert (GoogleCalendar source) {
		Calendar target = new Calendar();
		target.setSummary(source.getName());
		target.setTimeZone(ZoneId.systemDefault().getId());
		return target;
	}
}
