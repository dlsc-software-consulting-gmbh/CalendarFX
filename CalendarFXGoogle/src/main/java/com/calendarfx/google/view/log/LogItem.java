package com.calendarfx.google.view.log;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;

/**
 * Item of the log that provides information about one performed task.
 *
 * Created by gdiaz on 22/02/2017.
 */
public class LogItem {

	private final ObjectProperty<LocalDateTime> time = new SimpleObjectProperty<>(this, "time");

	public final ObjectProperty<LocalDateTime> timeProperty () {
		return time;
	}

	public final LocalDateTime getTime () {
		return timeProperty().get();
	}

	public final void setTime (LocalDateTime time) {
		timeProperty().set(time);
	}

	private final ObjectProperty<ActionType> action = new SimpleObjectProperty<>(this, "action");

	public final ObjectProperty<ActionType> actionProperty () {
		return action;
	}

	public ActionType getAction () {
		return actionProperty().get();
	}

	public void setAction (ActionType action) {
		actionProperty().set(action);
	}

	private final StringProperty calendar = new SimpleStringProperty(this, "calendar");

	public final StringProperty calendarProperty () {
		return calendar;
	}

	public final String getCalendar () {
		return calendarProperty().get();
	}

	public final void setCalendar (String calendar) {
		calendarProperty().set(calendar);
	}

	private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<>(this, "exception");

	public final ObjectProperty<Throwable> exceptionProperty () {
		return exception;
	}

	public final Throwable getException () {
		return exceptionProperty().get();
	}

	public final void setException (Throwable exception) {
		exceptionProperty().set(exception);
	}

	private final StringProperty description = new SimpleStringProperty(this, "description");

	public final StringProperty descriptionProperty () {
		return description;
	}

	public final String getDescription () {
		return descriptionProperty().get();
	}

	public final void setDescription (String description) {
		descriptionProperty().set(description);
	}

	private final ObjectProperty<StatusType> status = new SimpleObjectProperty<>(this, "status");

	public final ObjectProperty<StatusType> statusProperty () {
		return status;
	}

	public final StatusType getStatus () {
		return statusProperty().get();
	}

	public final void setStatus (StatusType status) {
		statusProperty().set(status);
	}

}
