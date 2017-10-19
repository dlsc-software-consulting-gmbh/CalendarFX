/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.ical.model;

import com.calendarfx.model.Interval;
import com.calendarfx.model.LoadEvent;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.filter.Rule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.Date;

import static java.util.Objects.requireNonNull;

public class ICalCalendar extends com.calendarfx.model.Calendar {

    private Set<Integer> alreadyLoadedYears = new HashSet<>();

    private Calendar calendar;

    private final Set<Uid> loadedEventIds = new HashSet<>();

    public ICalCalendar(String name, Calendar calendar) {
        super(name);

        requireNonNull(calendar);

        this.calendar = calendar;

        load(Year.now().getValue());
    }

    public void load(LoadEvent event) {
        load(event.getStartTime().getYear());
    }

    /*
     * Use synchronization to ensure consistency of already loaded dates.
     */
    private synchronized void load(int year) {
        if (alreadyLoadedYears.contains(year)) {
            return;
        }

        alreadyLoadedYears.add(year);

        ZonedDateTime st = ZonedDateTime.of(LocalDate.now().with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN, ZoneId.systemDefault());
        ZonedDateTime et = ZonedDateTime.of(LocalDate.now().with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX, ZoneId.systemDefault());

        try {
            startBatchUpdates();

            /*
             * Convert start and end times from new java.time API to old API and then to ical API :-)
             */
            Period period = new Period(
                    new DateTime(Date.from(st.toInstant())),
                    new DateTime(Date.from(et.toInstant())));

            Rule[] rules = new Rule[]{new PeriodRule(period)};
            Filter filter = new Filter(rules, Filter.MATCH_ANY);

            @SuppressWarnings("unchecked")
            Collection<VEvent> events = filter.filter(calendar.getComponents(Component.VEVENT));

            for (VEvent evt : events) {

                if (loadedEventIds.contains(evt.getUid())) {
                    continue;
                }

                loadedEventIds.add(evt.getUid());

                ICalCalendarEntry entry = new ICalCalendarEntry(evt);

                ZonedDateTime entryStart = ZonedDateTime.ofInstant(evt.getStartDate().getDate().toInstant(), ZoneId.systemDefault());

                ZonedDateTime entryEnd = ZonedDateTime.ofInstant(evt
                                .getEndDate().getDate().toInstant(),
                        ZoneId.systemDefault());

                if (entryEnd.toLocalDate().isAfter(entryStart.toLocalDate())) {
                    entryEnd = entryEnd.minusDays(1);
                    entry.setFullDay(true);
                }

                entry.setInterval(new Interval(entryStart, entryEnd));

                final Property prop = evt.getProperty("RRULE");
                if (prop instanceof RRule) {
                    RRule rrule = (RRule) prop;
                    entry.setRecurrenceRule("RRULE:" + rrule.getValue());
                }

                addEntry(entry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            stopBatchUpdates();
        }
    }
}
