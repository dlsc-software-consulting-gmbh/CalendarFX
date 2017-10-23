/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.ical.compat.javatime;

import com.google.ical.iter.RecurrenceIterable;
import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.TimeValue;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * a factory for converting RRULEs and RDATEs into
 * <code>Iterator&lt;DateTime&gt;</code> and
 * <code>Iterable&lt;DateTime&gt;</code>.
 *
 * @see RecurrenceIteratorFactory
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class ZonedDateTimeIteratorFactory {

    /**
     * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
     * them into a single date time iterator.
     * @param rdata RRULE, EXRULE, RDATE, and EXDATE lines.
     * @param start the first occurrence of the series.
     * @param tzid the local timezone -- used to interpret start and any dates in
     *   RDATE and EXDATE lines that don't have TZID params.
     * @param strict true if any failure to parse should result in a
     *   ParseException.  false causes bad content lines to be logged and ignored.
     */
    public static ZonedDateTimeIterator createDateTimeIterator(
            String rdata, ZonedDateTime start, ZoneId tzid, boolean strict)
            throws ParseException {
        return new RecurrenceIteratorWrapper(
                RecurrenceIteratorFactory.createRecurrenceIterator(
                        rdata, zonedDateTimeToDateValue(start.withZoneSameInstant(tzid)),
                        TimeZoneConverter.toTimeZone(tzid), strict));
    }

    /**
     * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
     * them into a single date time iterable.
     * @param rdata RRULE, EXRULE, RDATE, and EXDATE lines.
     * @param start the first occurrence of the series.
     * @param tzid the local timezone -- used to interpret start and any dates in
     *   RDATE and EXDATE lines that don't have TZID params.
     * @param strict true if any failure to parse should result in a
     *   ParseException.  false causes bad content lines to be logged and ignored.
     */
    public static ZonedDateTimeIterable createDateTimeIterable(
            String rdata, ZonedDateTime start, ZoneId tzid, boolean strict)
            throws ParseException {
        return new RecurrenceIterableWrapper(
                RecurrenceIteratorFactory.createRecurrenceIterable(
                        rdata, zonedDateTimeToDateValue(start.withZoneSameInstant(tzid)),
                        TimeZoneConverter.toTimeZone(tzid), strict));
    }

    /**
     * creates a date-time iterator given a recurrence iterator from
     * {@link com.google.ical.iter.RecurrenceIteratorFactory}.
     */
    public static ZonedDateTimeIterator createDateTimeIterator(
            RecurrenceIterator rit) {
        return new RecurrenceIteratorWrapper(rit);
    }

    private static final class RecurrenceIterableWrapper
            implements ZonedDateTimeIterable {
        private final RecurrenceIterable it;

        public RecurrenceIterableWrapper(RecurrenceIterable it) {
            this.it = it;
        }

        @Override
        public ZonedDateTimeIterator iterator() {
            return new RecurrenceIteratorWrapper(it.iterator());
        }
    }

    private static final class RecurrenceIteratorWrapper
            implements ZonedDateTimeIterator {
        private final RecurrenceIterator it;

        RecurrenceIteratorWrapper(RecurrenceIterator it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public ZonedDateTime next() {
            return dateValueToDateTime(it.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void advanceTo(ZonedDateTime d) {
            ZonedDateTime dUtc = d.withZoneSameInstant(ZoneId.of("UTC"));
            it.advanceTo(zonedDateTimeToDateValue(dUtc));
        }
    }

    static ZonedDateTime dateValueToDateTime(DateValue dvUtc) {
        if (dvUtc instanceof TimeValue) {
            TimeValue tvUtc = (TimeValue) dvUtc;
            return ZonedDateTime.of(
                    dvUtc.year(),
                    dvUtc.month(),  // java.util's dates are zero-indexed
                    dvUtc.day(),
                    tvUtc.hour(),
                    tvUtc.minute(),
                    tvUtc.second(),
                    0,
                    ZoneId.of("UTC"));
        } else {
            return ZonedDateTime.of(
                    dvUtc.year(),
                    dvUtc.month(),  // java.util's dates are zero-indexed
                    dvUtc.day(),
                    0,
                    0,
                    0,
                    0,
                    ZoneId.of("UTC"));
        }
    }

    static DateValue zonedDateTimeToDateValue(ZonedDateTime dt) {
        return new DateTimeValueImpl(
                dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth(),
                dt.getHour(), dt.getMinute(), dt.getSecond());
    }

    private ZonedDateTimeIteratorFactory() {
        // uninstantiable
    }
}
