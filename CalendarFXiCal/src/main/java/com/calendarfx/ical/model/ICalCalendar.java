/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.calendarfx.ical.model;

import com.calendarfx.model.Interval;
import com.calendarfx.model.LoadEvent;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.predicate.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ICalCalendar extends com.calendarfx.model.Calendar {

    private final Set<Integer> alreadyLoadedYears = new HashSet<>();

    private final Calendar calendar;

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

            Period<Instant> period = new Period<>(st.toInstant(), et.toInstant());
            Filter<VEvent> filter = new Filter<>(new PeriodRule<>(period));
            Collection<VEvent> events = calendar.getComponents(Component.VEVENT);

            for (VEvent evt : events) {

                try {
                    Optional<Uid> uid = evt.getProperty(Property.UID);
                    if (!uid.isPresent()) {
                        continue;
                    }

                    if (loadedEventIds.contains(uid.get())) {
                        continue;
                    }

                    loadedEventIds.add(uid.get());

                    ICalCalendarEntry entry = new ICalCalendarEntry(evt);

                    Optional<DtStart> dtStartOptional = evt.getProperty(Property.DTSTART);
                    Optional<DtEnd> dtEndOptional = evt.getProperty(Property.DTEND);

                    if (!(dtStartOptional.isPresent() && dtEndOptional.isPresent())) {
                        continue;
                    }

                    Temporal start = dtStartOptional.get().getDate();
                    Temporal end = dtEndOptional.get().getDate();

                    ZonedDateTime entryStart = null;
                    ZonedDateTime entryEnd = null;

                    if (start instanceof Instant) {
                        entryStart = ZonedDateTime.ofInstant((Instant) start, ZoneId.systemDefault());
                        entryEnd = ZonedDateTime.ofInstant((Instant) end, ZoneId.systemDefault());
                    } else if (start instanceof LocalDate) {
                        entryStart = ZonedDateTime.of((LocalDate) start, LocalTime.MIN, ZoneId.systemDefault());
                        entryEnd = ZonedDateTime.of((LocalDate) end, LocalTime.MAX, ZoneId.systemDefault());
                        entry.setFullDay(true);
                    } else if (start instanceof ZonedDateTime) {
                        entryStart = (ZonedDateTime) start;
                        entryEnd = (ZonedDateTime) end;
                        entryStart = entryStart.withZoneSameInstant(ZoneId.systemDefault());
                        entryEnd = entryEnd.withZoneSameInstant(ZoneId.systemDefault());
                    }

                    if (entryStart == null || entryEnd == null) {
                        continue;
                    }

                    entry.setInterval(new Interval(entryStart, entryEnd));

                    final Optional<RRule> prop = evt.getProperty(Property.RRULE);
                    if (prop.isPresent()) {
                        RRule rrule = prop.get();
                        entry.setRecurrenceRule("RRULE:" + rrule.getValue());
                    }

                    addEntry(entry);
                } catch (Throwable t) {
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            stopBatchUpdates();
        }
    }
}
