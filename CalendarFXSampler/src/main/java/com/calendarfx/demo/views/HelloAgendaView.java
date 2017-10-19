/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.AgendaView;
import com.calendarfx.view.DateControl;

public class HelloAgendaView extends CalendarFXDateControlSample {

	private AgendaView agendaView;

	@Override
	public String getSampleName() {
		return "Agenda View";
	}

	@Override
	protected DateControl createControl() {
		agendaView = new AgendaView();
		agendaView.setPrefWidth(400);

		CalendarSource calendarSource = new CalendarSource();
		HelloCalendar calendar1 = new HelloCalendar();
		HelloCalendar calendar2 = new HelloCalendar();
		HelloCalendar calendar3 = new HelloCalendar();
		HelloCalendar calendar4 = new HelloCalendar();
		calendarSource.getCalendars().addAll(calendar1, calendar2, calendar3, calendar4);

		agendaView.getCalendarSources().add(calendarSource);

		return agendaView;
	}

	@Override
	protected boolean isSupportingDeveloperConsole() {
		return false;
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return AgendaView.class;
	}

	@Override
	public String getSampleDescription() {
		return "The agenda view displays a (text) list of calendar entries for today and several days into the future or past.";
	}

	public static void main(String[] args) {
		launch(args);
	}
}
