/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.ical;

public class ICalCalendarAppLauncher {

    public static void main(String[] args) {
        System.setProperty("calendarfx.developer", "true");
        ICalCalendarApp.main(args);
    }
}
