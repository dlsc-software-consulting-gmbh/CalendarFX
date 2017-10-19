/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.AgendaView;
import com.calendarfx.view.WeekFieldsView;
import javafx.scene.control.Control;

public class HelloWeekFieldsView extends CalendarFXSample {

	private WeekFieldsView view;

	@Override
	public String getSampleName() {
		return "Week Fields View";
	}

	@Override
	protected Control createControl() {
		view = new WeekFieldsView();
		return view;
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return AgendaView.class;
	}

	@Override
	public String getSampleDescription() {
		return "The week fields view lets the user specify the first day of the week " +
				"(e.g. MONDAY in Germany, SUNDAY in the US) and the minimum number of " +
				"days in the first week of the year.";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
