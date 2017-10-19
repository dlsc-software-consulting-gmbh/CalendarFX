package com.calendarfx.google.view.task;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.view.data.GoogleCalendarData;
import com.calendarfx.google.view.log.ActionType;

import java.util.List;

/**
 * Search entries by text task.
 *
 * Created by gdiaz on 21/03/2017.
 */
public final class LoadEntriesByTextTask extends GoogleTask<List<GoogleEntry>> {

	private final String searchText;
	private final GoogleCalendar calendar;
	private final GoogleCalendarData data;
	private final GoogleAccount account;

	public LoadEntriesByTextTask (String searchText, GoogleCalendar calendar, GoogleCalendarData data, GoogleAccount account) {
		this.searchText = searchText;
		this.calendar = calendar;
		this.data = data;
		this.account = account;
		this.logItem.setCalendar(calendar.getName());
		this.logItem.setDescription(getDescription());
	}

	@Override
	public ActionType getAction () {
		return ActionType.LOAD;
	}

	@Override
	public String getDescription () {
		return "Loading \"" + searchText + "\"";
	}

	@Override
	protected List<GoogleEntry> call () throws Exception {
		return GoogleConnector.getInstance().getCalendarService(account.getId()).getEntries(calendar, searchText);
	}

	@Override
	protected void succeeded () {
		super.succeeded();
		for (GoogleEntry entry : getValue()) {
			if (!data.isLoadedEntry(entry)) {
				calendar.addEntry(entry);
				data.addLoadedEntry(entry);
			}
		}
		data.addLoadedSearchText(searchText);
	}
}
