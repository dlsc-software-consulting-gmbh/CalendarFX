/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.view.CalendarSelector;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class HelloCalendarSelector extends CalendarFXSample {

	@Override
	public String getSampleName() {
		return "Calendar Selector";
	}

	@Override
	protected Node createControl() {
		Calendar meetings = new Calendar("Meetings");
		Calendar training = new Calendar("Training");
		Calendar customers = new Calendar("Customers");
		Calendar holidays = new Calendar("Holidays");

		meetings.setStyle(Style.STYLE2);
		training.setStyle(Style.STYLE3);
		customers.setStyle(Style.STYLE4);
		holidays.setStyle(Style.STYLE5);

		CalendarSelector view = new CalendarSelector();
		view.getCalendars().addAll(meetings, training, customers, holidays);
		view.setCalendar(meetings);
		
		Label label = new Label("Selected: " + view.getCalendar().getName());
		label.setMaxHeight(Double.MAX_VALUE);
		view.calendarProperty().addListener(it -> label.setText("Selected: " + view.getCalendar().getName()));
		
		HBox box = new HBox(20);
		box.setFillHeight(true);
		box.getChildren().addAll(view, label);
		
		return box;
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return CalendarSelector.class;
	}

	@Override
	public String getSampleDescription() {
		return "A view used to select a calendar from a list of calendars.";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
