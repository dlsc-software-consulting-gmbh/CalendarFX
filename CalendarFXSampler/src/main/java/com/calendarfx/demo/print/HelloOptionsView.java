/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.print;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.print.OptionsView;
import javafx.scene.Node;

public class HelloOptionsView extends CalendarFXSample {

	private final OptionsView view = new OptionsView();

	@Override
	protected Class<?> getJavaDocClass() {
		return OptionsView.class;
	}

	@Override
	protected Node createControl() {
		return view;
	}

	@Override
	public String getSampleName() {
		return "Options View";
	}

	@Override
	public String getSampleDescription() {
		return "A control that allows to change a few basic settings before printing a calendar view.";
	}

	public static void main(String[] args) {
		launch(args);
	}

}
