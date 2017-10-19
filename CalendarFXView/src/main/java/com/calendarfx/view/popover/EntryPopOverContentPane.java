/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view.popover;

import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.Messages;
import javafx.beans.InvalidationListener;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

import java.util.Objects;

public class EntryPopOverContentPane extends PopOverContentPane {

	private Entry<?> entry;
	private DateControl dateControl;
	private PopOver popOver;

	public EntryPopOverContentPane(PopOver popOver, DateControl dateControl, Entry<?> entry) {
		getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm()); //$NON-NLS-1$

		this.popOver = popOver;
		this.dateControl = dateControl;
		this.entry = Objects.requireNonNull(entry);

		EntryDetailsView details = new EntryDetailsView(entry);

		PopOverTitledPane detailsPane = new PopOverTitledPane(Messages.getString("EntryPopOverContentPane.DETAILS"), //$NON-NLS-1$
				details);


		EntryHeaderView header = new EntryHeaderView(entry, dateControl.getCalendars());
		setHeader(header);

		if (Boolean.getBoolean("calendarfx.developer")) {
			EntryPropertiesView properties = new EntryPropertiesView(entry);
			PopOverTitledPane propertiesPane = new PopOverTitledPane("Properties", properties);
			getPanes().addAll(detailsPane, propertiesPane);
		} else {
			getPanes().addAll(detailsPane);
		}

		setExpandedPane(detailsPane);

		InvalidationListener listener = obs -> {
			if (entry.isFullDay() && !popOver.isDetached()) {
				popOver.setDetached(true);
			}
		};

		entry.fullDayProperty().addListener(listener);
		popOver.setOnHidden(evt -> entry.fullDayProperty().removeListener(listener));

		entry.calendarProperty().addListener(it -> {
			if (entry.getCalendar() == null) {
				popOver.hide(Duration.ZERO);
			}
		});
	}

	public final PopOver getPopOver() {
		return popOver;
	}

	public final DateControl getDateControl() {
		return dateControl;
	}

	public final Entry<?> getEntry() {
		return entry;
	}
}
