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
import com.calendarfx.view.print.PreviewPane;

import javafx.scene.Node;

public class HelloPreviewPane extends CalendarFXSample {

	@Override
	public String getSampleName() {
		return "Preview Pane";
	}
	
	@Override
	public String getSampleDescription() {
		return "Preview of the page to be printed";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return PreviewPane.class;
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

		PreviewPane printPreview = new PreviewPane();
		printPreview.getPrintablePage().getCalendarSources().addAll(workCalendarSource, familyCalendarSource);
		
		return printPreview;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
