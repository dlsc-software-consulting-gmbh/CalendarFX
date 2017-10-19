/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.print;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.print.SettingsView;

import javafx.scene.Node;

public class HelloSettingsView extends CalendarFXSample {

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

		CalendarSource workCalendarSource = new CalendarSource("Work");
		workCalendarSource.getCalendars().addAll(meetings, training, customers, holidays);

		Calendar birthdays = new Calendar("Birthdays");
		Calendar katja = new Calendar("Katja");
		Calendar dirk = new Calendar("Dirk");
		Calendar philip = new Calendar("Philip");

		CalendarSource familyCalendarSource = new CalendarSource("Family");
		familyCalendarSource.getCalendars().addAll(birthdays, katja, dirk, philip);

		SettingsView printSettingsView = new SettingsView();
		printSettingsView.getSourceView().getCalendarSources().addAll(workCalendarSource, familyCalendarSource);

		return printSettingsView;
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return SettingsView.class;
	}

	@Override
	public String getSampleName() {
		return "Print Settings View";
	}

	@Override
	public String getSampleDescription() {
		return "Print Settings represents the right panel on the Print dialog that allows to setup the printing.";
	}

	public static void main(String[] args) {
		launch(args);
	}

}
