package com.calendarfx.google.view.task;

import com.calendarfx.google.view.log.ActionType;
import com.calendarfx.google.view.log.LogItem;
import com.calendarfx.google.view.log.StatusType;
import javafx.concurrent.Task;

import java.time.LocalDateTime;

/**
 * Base class for tasks executed for google calendar interaction.
 *
 * Created by gdiaz on 28/02/2017.
 */
public abstract class GoogleTask <V> extends Task<V> {

	final LogItem logItem;

	protected GoogleTask() {
		super();
		logItem = new LogItem();
		logItem.setTime(LocalDateTime.now());
		logItem.setStatus(StatusType.PENDING);
		logItem.setDescription(getDescription());
		logItem.setAction(getAction());
	}

	public LogItem getLogItem () {
		return logItem;
	}

	public abstract ActionType getAction ();

	public abstract String getDescription ();

	@Override
	protected void failed () {
		logItem.setStatus(StatusType.FAILED);
		logItem.setException(getException());
	}

	@Override
	protected void cancelled () {
		logItem.setStatus(StatusType.CANCELLED);
	}

	@Override
	protected void succeeded () {
		logItem.setStatus(StatusType.SUCCEEDED);
	}
}
