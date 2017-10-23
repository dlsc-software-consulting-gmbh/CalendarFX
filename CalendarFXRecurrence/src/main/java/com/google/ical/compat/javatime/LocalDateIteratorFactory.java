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
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * a factory for converting RRULEs and RDATEs into
 * <code>Iterator&lt;LocalDate&gt;</code> and
 * <code>Iterable&lt;LocalDate&gt;</code>.
 *
 * @see RecurrenceIteratorFactory
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class LocalDateIteratorFactory {

    /**
     * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
     * them into a single local date iterator.
     *
     * @param rdata
     *            RRULE, EXRULE, RDATE, and EXDATE lines.
     * @param start
     *            the first occurrence of the series.
     * @param zoneId
     *            the local timezone -- used to interpret any dates in RDATE and
     *            EXDATE lines that don't have TZID params.
     * @param strict
     *            true if any failure to parse should result in a
     *            ParseException. false causes bad content lines to be logged
     *            and ignored.
     */
    public static LocalDateIterator createLocalDateIterator(String rdata,
                                                            LocalDate start, ZoneId zoneId, boolean strict)
            throws ParseException {
        return new RecurrenceIteratorWrapper(RecurrenceIteratorFactory
                .createRecurrenceIterator(rdata, localDateToDateValue(start),
                        TimeZoneConverter.toTimeZone(zoneId), strict));
    }

    /**
     * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
     * them into a single local date iterator.
     *
     * @param rdata
     *            RRULE, EXRULE, RDATE, and EXDATE lines.
     * @param start
     *            the first occurrence of the series.
     * @param strict
     *            true if any failure to parse should result in a
     *            ParseException. false causes bad content lines to be logged
     *            and ignored.
     */
    public static LocalDateIterator createLocalDateIterator(String rdata,
                                                            LocalDate start, boolean strict) throws ParseException {
        return createLocalDateIterator(rdata, start, ZoneId.of("UTC"), strict);
    }

    /**
     * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
     * them into a single local date iterable.
     *
     * @param rdata
     *            RRULE, EXRULE, RDATE, and EXDATE lines.
     * @param start
     *            the first occurrence of the series.
     * @param tzid
     *            the local timezone -- used to interpret any dates in RDATE and
     *            EXDATE lines that don't have TZID params.
     * @param strict
     *            true if any failure to parse should result in a
     *            ParseException. false causes bad content lines to be logged
     *            and ignored.
     */
    public static LocalDateIterable createLocalDateIterable(String rdata,
                                                            LocalDate start, ZoneId tzid, boolean strict)
            throws ParseException {
        return new RecurrenceIterableWrapper(RecurrenceIteratorFactory
                .createRecurrenceIterable(rdata, localDateToDateValue(start),
                        TimeZoneConverter.toTimeZone(tzid), strict));
    }

    /**
     * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
     * them into a single local date iterable.
     *
     * @param rdata
     *            RRULE, EXRULE, RDATE, and EXDATE lines.
     * @param start
     *            the first occurrence of the series.
     * @param strict
     *            true if any failure to parse should result in a
     *            ParseException. false causes bad content lines to be logged
     *            and ignored.
     */
    public static LocalDateIterable createLocalDateIterable(String rdata,
                                                            LocalDate start, boolean strict) throws ParseException {
        return createLocalDateIterable(rdata, start, ZoneId.of("UTC"), strict);
    }

    /**
     * creates a local date iterator given a recurrence iterator from
     * {@link com.google.ical.iter.RecurrenceIteratorFactory}.
     */
    public static LocalDateIterator createLocalDateIterator(
            RecurrenceIterator rit) {
        return new RecurrenceIteratorWrapper(rit);
    }

    private static final class RecurrenceIterableWrapper
            implements LocalDateIterable {
        private final RecurrenceIterable it;

        public RecurrenceIterableWrapper(RecurrenceIterable it) {
            this.it = it;
        }

        @Override
        public LocalDateIterator iterator() {
            return new RecurrenceIteratorWrapper(it.iterator());
        }
    }

    private static final class RecurrenceIteratorWrapper
            implements LocalDateIterator {
        private final RecurrenceIterator it;

        RecurrenceIteratorWrapper(RecurrenceIterator it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public LocalDate next() {
            return dateValueToLocalDate(it.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void advanceTo(LocalDate d) {
            // we need to treat midnight as a date value so that passing in
            // dateValueToDate(<some-date-value>) will not advance past any
            // occurrences of some-date-value in the iterator.
            it.advanceTo(localDateToDateValue(d));
        }
    }

    static LocalDate dateValueToLocalDate(DateValue dvUtc) {
        return LocalDate.of(dvUtc.year(), dvUtc.month(), dvUtc.day());
    }

    static DateValue localDateToDateValue(LocalDate date) {
        return new DateValueImpl(date.getYear(), date.getMonthValue(),
                date.getDayOfMonth());
    }

    private LocalDateIteratorFactory() {
        // uninstantiable
    }
}
