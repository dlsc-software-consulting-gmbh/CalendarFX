/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.print;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.print.TimeRangeField;

import javafx.scene.Node;

public class HelloTimeRangeField extends CalendarFXSample {

	@Override
	public String getSampleName() {
		return "Time Range Field";
	}
	
	@Override
	public String getSampleDescription() {
		return "Allows to setup a field on the time range selector.";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return TimeRangeField.class;
	}

	@Override
	protected Node createControl() {
		return new TimeRangeField();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
