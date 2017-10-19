/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.YearView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;

public class HelloYearView extends CalendarFXDateControlSample {

    @Override
    public String getSampleName() {
        return "Year View";
    }

    @Override
    protected DateControl createControl() {
        YearView yearView = new YearView();

        CalendarSource calendarSource = new CalendarSource();
        calendarSource.getCalendars().add(new HelloCalendar(yearView.getYear()));

        yearView.getCalendarSources().add(calendarSource);

        return yearView;
    }

    @Override
    public String getSampleDescription() {
        return "The year view displays the twelve month of a given year.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return YearView.class;
    }

    class HelloCalendar extends Calendar {

        public HelloCalendar(Year year) {
            for (int i = 1; i < 365; i++) {

                LocalDate date = year.atDay(i);

                for (int j = 0; j < (int) (Math.random() * 4); j++) {
                    Entry<?> entry = new Entry<>();
                    entry.changeStartDate(date);
                    entry.changeEndDate(date);

                    entry.setTitle("Entry " + (j + 1));

                    int hour = (int) (Math.random() * 23);
                    int durationInHours = Math.min(24 - hour, (int) (Math.random() * 4));

                    LocalTime startTime = LocalTime.of(hour, 0);
                    LocalTime endTime = startTime.plusHours(durationInHours);

                    entry.changeStartTime(startTime);
                    entry.changeEndTime(endTime);

                    if (Math.random() < .3) {
                        entry.setFullDay(true);
                    }

                    entry.setCalendar(this);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
