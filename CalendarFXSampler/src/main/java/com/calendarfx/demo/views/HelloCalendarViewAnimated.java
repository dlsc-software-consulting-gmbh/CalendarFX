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
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;

public class HelloCalendarViewAnimated extends CalendarFXDateControlSample {

    private CalendarView calendarView;

    @Override
    public String getSampleName() {
        return "Calendar View (Animated)";
    }

    @Override
    protected DateControl createControl() {
        CalendarSource calendarSource = new CalendarSource("My Calendars");
        calendarSource.getCalendars().add(new HelloCalendar());

        calendarView = new CalendarView();
        calendarView.getCalendarSources().add(calendarSource);
        calendarView.setTransitionsEnabled(true);

        return calendarView;
    }

    @Override
    public String getSampleDescription() {
        return "The calendar view displays a single day, a week, a month, and a year.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return CalendarView.class;
    }

    class HelloCalendar extends Calendar {

        public HelloCalendar() {
            for (Month month : Month.values()) {

                YearMonth yearMonth = YearMonth.of(LocalDate.now().getYear(), month);

                for (int i = 1; i < 28; i++) {

                    LocalDate date = yearMonth.atDay(i);

                    for (int j = 0; j < (int) (Math.random() * 7); j++) {
                        Entry<?> entry = new Entry<>();
                        entry.changeStartDate(date);
                        entry.changeEndDate(date);

                        entry.setTitle("Entry " + (j + 1));

                        int hour = (int) (Math.random() * 23);
                        int durationInHours = Math.min(24 - hour,
                                (int) (Math.random() * 4));

                        LocalTime startTime = LocalTime.of(hour, 0);
                        LocalTime endTime = startTime
                                .plusHours(durationInHours);

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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
