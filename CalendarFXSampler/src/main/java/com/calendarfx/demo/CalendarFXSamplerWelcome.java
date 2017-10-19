/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo;

import fxsampler.model.WelcomePage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;

public class CalendarFXSamplerWelcome extends WelcomePage {

	public CalendarFXSamplerWelcome() {
		super("CalendarFX", new Label(""));

		Label label = (Label) getContent();
		label.setWrapText(true);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setMaxHeight(Double.MAX_VALUE);
		label.setTextAlignment(TextAlignment.CENTER);
		label.setAlignment(Pos.CENTER);
		label.setPadding(new Insets(50));
		label.setText("Welcome to the CalendarFX sampler. This application allows you to quickly browse through the "
		        + "various controls that are available in this framework. In each sample you can play around with the "
		        + "properties and controls shown on the right-hand side.");
	}

}
