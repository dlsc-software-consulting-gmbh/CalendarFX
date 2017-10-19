package com.calendarfx.google.view.task;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.view.log.ActionType;

import java.util.Map;

/**
 * Task that updates one entry in google.
 *
 * Created by gdiaz on 12/03/2017.
 */
public final class UpdateEntryTask extends GoogleTask<GoogleEntry> {

	private static final long TWO_SECONDS = 2 * 1000;

	private GoogleEntry entry;
	private final GoogleAccount account;
	private final Map<GoogleEntry, UpdateEntryTask> updateTasks;

	public UpdateEntryTask (GoogleEntry entry, GoogleAccount account, Map<GoogleEntry, UpdateEntryTask> updateTasks) {
		this.entry = entry;
		this.account = account;
		this.updateTasks = updateTasks;
		this.logItem.setCalendar(entry.getCalendar().getName());
		this.logItem.setDescription(getDescription());
	}

	public void append (GoogleEntry newVersion) {
		assert(entry.equals(newVersion));
		this.entry = newVersion;
		this.logItem.setDescription(getDescription());
	}

	@Override
	public ActionType getAction () {
		return ActionType.UPDATE;
	}

	@Override
	public String getDescription () {
		return "Update " + entry;
	}

	@Override
	protected GoogleEntry call () throws Exception {
		Thread.sleep(TWO_SECONDS);
		return GoogleConnector.getInstance().getCalendarService(account.getId()).updateEntry(entry);
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		updateTasks.remove(entry);
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		updateTasks.remove(entry);
	}

	@Override
	protected void failed() {
		super.failed();
		updateTasks.remove(entry);
	}
}
