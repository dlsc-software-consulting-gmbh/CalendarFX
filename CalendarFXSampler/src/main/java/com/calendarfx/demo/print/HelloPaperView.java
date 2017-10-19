/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.print;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.print.PaperView;

import javafx.scene.Node;

public class HelloPaperView extends CalendarFXSample {
	
	@Override
	protected Node createControl() {
		return new PaperView();
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return PaperView.class;
	}

	@Override
	public String getSampleName() {
		return "Paper View";
	}

	@Override
	public String getSampleDescription() {
		return "This control allows to select the view that is going to be printed and configure the paper type and print margins.";
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
