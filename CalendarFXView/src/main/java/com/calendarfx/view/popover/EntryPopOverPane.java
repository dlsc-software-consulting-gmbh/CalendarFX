/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view.popover;

import javafx.scene.layout.StackPane;

import com.calendarfx.view.CalendarView;

public abstract class EntryPopOverPane extends StackPane {

	public EntryPopOverPane() {
		getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());
	}
}
