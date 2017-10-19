package com.calendarfx.google.view.task;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.view.log.ActionType;

/**
 * Task that inserts an entry into google.
 *
 * Created by gdiaz on 12/03/2017.
 */
public final class InsertEntryTask extends GoogleTask<GoogleEntry> {

	private final GoogleEntry entry;
	private final GoogleCalendar calendar;
	private final GoogleAccount account;

	public InsertEntryTask (GoogleEntry entry, GoogleCalendar calendar, GoogleAccount account) {
		this.entry = entry;
		this.calendar = calendar;
		this.account = account;
		this.logItem.setCalendar(calendar.getName());
		this.logItem.setDescription(getDescription());
	}

	@Override
	public ActionType getAction () {
		return ActionType.INSERT;
	}

	@Override
	public String getDescription () {
		return "Insert " + entry;
	}

	@Override
	protected GoogleEntry call () throws Exception {
		return GoogleConnector.getInstance().getCalendarService(account.getId()).insertEntry(entry, calendar);
	}

	@Override
	protected void succeeded () {
		super.succeeded();
		calendar.addEntries(entry);
	}
}
