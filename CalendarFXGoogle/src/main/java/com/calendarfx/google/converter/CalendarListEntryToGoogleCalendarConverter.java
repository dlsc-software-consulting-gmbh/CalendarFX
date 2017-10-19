package com.calendarfx.google.converter;

import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntryReminder;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.EventReminder;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts from google api to calendarfx api.
 *
 * Created by gdiaz on 20/02/2017.
 */
public final class CalendarListEntryToGoogleCalendarConverter implements BeanConverter<CalendarListEntry, GoogleCalendar> {
	@Override
	public GoogleCalendar convert (CalendarListEntry source) {
		GoogleCalendar calendar = new GoogleCalendar();
		calendar.setId(source.getId());
		calendar.setName(source.getSummary());
		calendar.setShortName(source.getSummary());
		calendar.setPrimary(source.isPrimary());
		calendar.setReadOnly(GoogleCalendar.isReadOnlyAccessRole(source.getAccessRole()));
		List<GoogleEntryReminder> reminders = new ArrayList<>();
		if (source.getDefaultReminders() != null) {
			for (EventReminder r : source.getDefaultReminders()) {
				reminders.add(new GoogleEntryReminder(r));
			}
		}
		calendar.getDefaultReminders().setAll(reminders);
		return calendar;
	}
}
