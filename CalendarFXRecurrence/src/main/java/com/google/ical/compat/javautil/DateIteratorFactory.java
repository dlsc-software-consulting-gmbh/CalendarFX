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

package com.google.ical.compat.javautil;

import com.google.ical.iter.RecurrenceIterable;
import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.util.TimeUtils;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.TimeValue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * a factory for converting RRULEs and RDATEs into
 * <code>Iterator&lt;Date&gt;</code> and <code>Iterable&lt;Date&gt;</code>.
 *
 * @see RecurrenceIteratorFactory
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class DateIteratorFactory {

    /**
     * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
     * them into a single date iterator.
     * @param rdata RRULE, EXRULE, RDATE, and EXDATE lines.
     * @param start the first occurrence of the series.
     * @param tzid the local timezone -- used to interpret start and any dates in
     *   RDATE and EXDATE lines that don't have TZID params.
     * @param strict true if any failure to parse should result in a
     *   ParseException.  false causes bad content lines to be logged and ignored.
     */
    public static DateIterator createDateIterator(
            String rdata, Date start, TimeZone tzid, boolean strict)
            throws ParseException {
        return new RecurrenceIteratorWrapper(
                RecurrenceIteratorFactory.createRecurrenceIterator(
                        rdata, dateToDateValue(start, true),
                        tzid, strict));
    }

    /**
     * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
     * them into a single date iterable.
     * @param rdata RRULE, EXRULE, RDATE, and EXDATE lines.
     * @param start the first occurrence of the series.
     * @param tzid the local timezone -- used to interpret start and any dates in
     *   RDATE and EXDATE lines that don't have TZID params.
     * @param strict true if any failure to parse should result in a
     *   ParseException.  false causes bad content lines to be logged and ignored.
     */
    public static DateIterable createDateIterable(
            String rdata, Date start, TimeZone tzid, boolean strict)
            throws ParseException {
        return new RecurrenceIterableWrapper(
                RecurrenceIteratorFactory.createRecurrenceIterable(
                        rdata, dateToDateValue(start, true),
                        tzid, strict));
    }

    /**
     * creates a date iterator given a recurrence iterator from
     * {@link com.google.ical.iter.RecurrenceIteratorFactory}.
     */
    public static DateIterator createDateIterator(RecurrenceIterator rit) {
        return new RecurrenceIteratorWrapper(rit);
    }

    private static final class RecurrenceIterableWrapper
            implements DateIterable {
        private final RecurrenceIterable it;

        public RecurrenceIterableWrapper(RecurrenceIterable it) {
            this.it = it;
        }

        public DateIterator iterator() {
            return new RecurrenceIteratorWrapper(it.iterator());
        }
    }

    private static final class RecurrenceIteratorWrapper
            implements DateIterator {
        private final RecurrenceIterator it;

        RecurrenceIteratorWrapper(RecurrenceIterator it) {
            this.it = it;
        }

        public boolean hasNext() {
            return it.hasNext();
        }

        public Date next() {
            return dateValueToDate(it.next());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void advanceTo(Date d) {
            // we need to treat midnight as a date value so that passing in
            // dateValueToDate(<some-date-value>) will not advance past any
            // occurrences of some-date-value in the iterator.
            it.advanceTo(dateToDateValue(d, true));
        }
    }

    static Date dateValueToDate(DateValue dvUtc) {
        GregorianCalendar c = new GregorianCalendar(TimeUtils.utcTimezone());
        c.clear();
        if (dvUtc instanceof TimeValue) {
            TimeValue tvUtc = (TimeValue) dvUtc;
            c.set(dvUtc.year(),
                    dvUtc.month() - 1,  // java.util's dates are zero-indexed
                    dvUtc.day(),
                    tvUtc.hour(),
                    tvUtc.minute(),
                    tvUtc.second());
        } else {
            c.set(dvUtc.year(),
                    dvUtc.month() - 1,  // java.util's dates are zero-indexed
                    dvUtc.day(),
                    0,
                    0,
                    0);
        }
        return c.getTime();
    }

    static DateValue dateToDateValue(Date date, boolean midnightAsDate) {
        GregorianCalendar c = new GregorianCalendar(TimeUtils.utcTimezone());
        c.setTime(date);
        int h = c.get(Calendar.HOUR_OF_DAY),
                m = c.get(Calendar.MINUTE),
                s = c.get(Calendar.SECOND);
        if (midnightAsDate && 0 == (h | m | s)) {
            return new DateValueImpl(c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH) + 1,
                    c.get(Calendar.DAY_OF_MONTH));
        } else {
            return new DateTimeValueImpl(c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH) + 1,
                    c.get(Calendar.DAY_OF_MONTH),
                    h,
                    m,
                    s);
        }
    }

    private DateIteratorFactory() {
        // uninstantiable
    }
}
