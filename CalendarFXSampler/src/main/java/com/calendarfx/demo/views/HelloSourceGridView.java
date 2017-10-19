/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.SourceGridView;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class HelloSourceGridView extends CalendarFXSample {
	
	private SourceGridView sourceView;

	@Override
	public String getSampleName() {
		return "Source Grid View";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return SourceGridView.class;
	}

	@Override
	protected Node createControl() {
		sourceView = new SourceGridView();

		Calendar meetings = new Calendar("Meetings");
		Calendar training = new Calendar("Training");
		Calendar customers = new Calendar("Customers");
		Calendar holidays = new Calendar("Holidays");

		meetings.setStyle(Style.STYLE2);
		training.setStyle(Style.STYLE3);
		customers.setStyle(Style.STYLE4);
		holidays.setStyle(Style.STYLE5);

		CalendarSource workCalendarSource = new CalendarSource("Work");
		workCalendarSource.getCalendars().addAll(meetings, training, customers, holidays);

		Calendar birthdays = new Calendar("Birthdays");
		Calendar katja = new Calendar("Katja");
		Calendar dirk = new Calendar("Dirk");
		Calendar philip = new Calendar("Philip");
		Calendar jule = new Calendar("Jule");
		Calendar armin = new Calendar("Armin");

		CalendarSource familyCalendarSource = new CalendarSource("Family");
		familyCalendarSource.getCalendars().addAll(birthdays, katja, dirk, philip, jule, armin);

		sourceView.getCalendarSources().addAll(workCalendarSource, familyCalendarSource);

		return sourceView;
	}
	
	@Override
	public Node getControlPanel() {
		Node controlPanel = super.getControlPanel();
		
		VBox vBox = new VBox();
		vBox.setSpacing(5);
		vBox.getChildren().add(controlPanel);
		
		for (CalendarSource calendarSource : sourceView.getCalendarSources()) {
			for (Calendar calendar : calendarSource.getCalendars()) {
				CheckBox checkBox = new CheckBox(calendar.getName());
				checkBox.selectedProperty().bindBidirectional(sourceView.getCalendarVisibilityProperty(calendar));
				vBox.getChildren().add(checkBox);
			}
		}
		
		return vBox;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
