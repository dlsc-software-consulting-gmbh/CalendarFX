/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.TimeField;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HelloTimeField extends CalendarFXSample {

	@Override
	public String getSampleName() {
		return "Time Field";
	}

	@Override
	protected Node createControl() {
		TimeField timeField = new TimeField();
		Label label = new Label("Time: ");
		label.setText("Time: " + timeField.getValue().toString());
		timeField.valueProperty().addListener(it -> label.setText("Time: " + timeField.getValue().toString()));

		VBox box = new VBox();
		box.setSpacing(20);
		box.getChildren().addAll(timeField, label);

		return box;
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return TimeField.class;
	}

	@Override
	public String getSampleDescription() {
		return "A control used to specify a local time (hour, minute).";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
