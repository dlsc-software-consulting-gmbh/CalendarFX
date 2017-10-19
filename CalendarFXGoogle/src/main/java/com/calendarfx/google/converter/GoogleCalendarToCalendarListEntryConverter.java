package com.calendarfx.google.converter;

import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntryReminder;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.EventReminder;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts from calendar (calendarfx api) to calendar list entry (google api).
 *
 * Created by gdiaz on 20/02/2017.
 */
public final class GoogleCalendarToCalendarListEntryConverter implements BeanConverter<GoogleCalendar, CalendarListEntry> {
	@Override
	public CalendarListEntry convert (GoogleCalendar source) {
		CalendarListEntry calendarListEntry = new CalendarListEntry();
		calendarListEntry.setId(source.getId());
		calendarListEntry.setSummary(source.getName());
		calendarListEntry.setPrimary(source.isPrimary());
		List<EventReminder> reminders = new ArrayList<>();
		for (GoogleEntryReminder reminder : source.getDefaultReminders()) {
			EventReminder er = new EventReminder();
			er.setMethod(reminder.getMethod().getId());
			er.setMinutes(reminder.getMinutes());
			reminders.add(er);
		}
		calendarListEntry.setDefaultReminders(reminders);
		return calendarListEntry;
	}
}
