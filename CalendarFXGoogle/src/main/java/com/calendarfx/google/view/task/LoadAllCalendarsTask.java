package com.calendarfx.google.view.task;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.view.log.ActionType;

import java.util.List;

/**
 * Task that queries all calendars from google and updates the google calendar source.
 *
 * Created by gdiaz on 28/02/2017.
 */
public final class LoadAllCalendarsTask extends GoogleTask<List<GoogleCalendar>> {

	private final GoogleAccount account;

	public LoadAllCalendarsTask (GoogleAccount account) {
		super();
		this.account = account;
	}

	@Override
	public ActionType getAction () {
		return ActionType.LOAD;
	}

	@Override
	public String getDescription () {
		return "Loading all calendars";
	}

	@Override
	protected List<GoogleCalendar> call () throws Exception {
		return GoogleConnector.getInstance().getCalendarService(account.getId()).getCalendars();
	}

	@Override
	protected void succeeded () {
		super.succeeded();
		account.getCalendars().setAll(getValue());
	}
}
