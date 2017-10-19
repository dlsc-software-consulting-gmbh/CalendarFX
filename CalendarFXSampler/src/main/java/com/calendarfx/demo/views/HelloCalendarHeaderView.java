/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.view.CalendarHeaderView;
import javafx.scene.Node;

public class HelloCalendarHeaderView extends CalendarFXSample {

    @Override
    public String getSampleName() {
        return "Calendar Header View";
    }
    
    @Override
    protected Node createControl() {
    	CalendarHeaderView calendarHeaderView = new CalendarHeaderView();
		calendarHeaderView.setNumberOfDays(5);
		calendarHeaderView.setMaxHeight(30);
		
		Calendar dirk = new Calendar("Dirk");
		Calendar katja = new Calendar("Katja");
		Calendar philip = new Calendar("Philip");
		Calendar jule = new Calendar("Jule");
		Calendar armin = new Calendar("Armin");
		
		dirk.setStyle(Style.STYLE1);
		katja.setStyle(Style.STYLE1);
		philip.setStyle(Style.STYLE2);
		jule.setStyle(Style.STYLE1);
		armin.setStyle(Style.STYLE3);
		
		calendarHeaderView.getCalendars().add(dirk);
		calendarHeaderView.getCalendars().add(katja);
		calendarHeaderView.getCalendars().add(philip);
		calendarHeaderView.getCalendars().add(jule);
		calendarHeaderView.getCalendars().add(armin);
		
		return calendarHeaderView;
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return CalendarHeaderView.class;
    }

    @Override
    public String getSampleDescription() {
        return "The all-day view displays entries that last all day / span multiple days.";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
