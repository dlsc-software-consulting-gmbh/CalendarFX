package com.calendarfx.google.view.task;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.view.data.GoogleCalendarData;
import com.calendarfx.google.view.data.IGoogleCalendarDataProvider;
import com.calendarfx.google.view.log.ActionType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Task that allows to refresh the data of a single calendar.
 *
 * Created by gdiaz on 10/04/2017.
 */
public final class RefreshCalendarsTask extends GoogleTask<List<GoogleCalendar>> {

	private final GoogleAccount account;
	private final IGoogleCalendarDataProvider provider;

	public RefreshCalendarsTask (GoogleAccount account, IGoogleCalendarDataProvider provider) {
		this.account = account;
		this.provider = provider;
	}

	@Override
	public ActionType getAction () {
		return ActionType.REFRESH;
	}

	@Override
	public String getDescription () {
		return "Refreshing all calendars";
	}

	@Override
	protected List<GoogleCalendar> call () throws Exception {
		return GoogleConnector.getInstance().getCalendarService(account.getId()).getCalendars();
	}

	@Override
	protected void succeeded () {
		super.succeeded();

		List<GoogleCalendar> oldCalendars = account.getGoogleCalendars();
		List<GoogleCalendar> newCalendars = getValue();
		List<GoogleCalendar> updCalendars = new ArrayList<>();

		for (Iterator<GoogleCalendar> newIterator = newCalendars.iterator(); newIterator.hasNext();) {
			GoogleCalendar newCalendar = newIterator.next();

			for (Iterator<GoogleCalendar> oldIterator = oldCalendars.iterator(); oldIterator.hasNext();) {
				GoogleCalendar oldCalendar = oldIterator.next();
				if (newCalendar.equals(oldCalendar)) {
					oldIterator.remove();
					newIterator.remove();
					updCalendars.add(oldCalendar);
					GoogleCalendarData data = provider.getCalendarData(oldCalendar);
					if (data != null) {
						data.clear();
					}
					break;
				}
			}
		}

		updCalendars.forEach(GoogleCalendar::clear);
		account.getCalendars().removeAll(oldCalendars);
		account.getCalendars().addAll(newCalendars);
	}
}
