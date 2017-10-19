/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.google.view;

import com.calendarfx.google.view.log.LogPane;
import com.calendarfx.view.CalendarFXControl;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import impl.com.calendarfx.google.view.GoogleCalendarAppViewSkin;
import javafx.scene.control.Skin;

import java.util.Objects;

/**
 * Control which allows to log in by using a Google account in order to get
 * access to the user calendar data. Displays the Google Login web page and then
 * lets the user authorize us to read/write his calendar information. After
 * authorization this displays a {@link DateControl} configured externally.
 *
 * @author Gabriel Diaz, 14.02.2015.
 */
public class GoogleCalendarAppView extends CalendarFXControl {

	private final CalendarView calendarView;

	private final LogPane logPane;

	public GoogleCalendarAppView (CalendarView calendarView) {
		super();
		this.calendarView = Objects.requireNonNull(calendarView);
		this.logPane = new LogPane();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new GoogleCalendarAppViewSkin(this);
	}

	public CalendarView getCalendarView() {
		return calendarView;
	}

	public LogPane getLogPane() {
		return logPane;
	}
}
