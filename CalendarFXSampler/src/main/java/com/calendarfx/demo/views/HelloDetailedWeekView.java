/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DetailedWeekView;

public class HelloDetailedWeekView extends CalendarFXDateControlSample {

    private DetailedWeekView detailedWeekView;

    @Override
    public String getSampleName() {
        return "Detailed Week View";
    }

    @Override
    protected DateControl createControl() {
        Calendar dirk = new Calendar("Dirk");
        Calendar katja = new Calendar("Katja");
        Calendar philip = new Calendar("Philip");
        Calendar jule = new Calendar("Jule");
        Calendar armin = new Calendar("Armin");

        dirk.setStyle(Style.STYLE1);
        katja.setStyle(Style.STYLE2);
        philip.setStyle(Style.STYLE3);
        jule.setStyle(Style.STYLE4);
        armin.setStyle(Style.STYLE5);

        CalendarSource calendarSource = new CalendarSource();
        calendarSource.getCalendars().setAll(dirk, katja, philip, jule, armin);

        detailedWeekView = new DetailedWeekView();
        detailedWeekView.getCalendarSources().setAll(calendarSource);

        return detailedWeekView;
    }

    @Override
    public String getSampleDescription() {
        return "The detailed week view displays several days inside a week view. " +
                "Additionally it shows an all day view at the top and a time scale " +
                "on its left-hand side";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DetailedWeekView.class;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
