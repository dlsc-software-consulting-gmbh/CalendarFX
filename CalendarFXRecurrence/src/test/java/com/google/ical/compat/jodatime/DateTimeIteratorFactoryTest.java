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

package com.google.ical.compat.jodatime;

import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValueImpl;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * testcases for {@link DateTimeIteratorFactory}.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class DateTimeIteratorFactoryTest extends TestCase {

    private static final DateTimeZone PST =
            DateTimeZone.forID("America/Los_Angeles");

    public void testDateValueToDateTime() {
        assertEquals(dateTime(2006, 10, 13, 0, 0, 0),
                DateTimeIteratorFactory.dateValueToDateTime(
                        new DateValueImpl(2006, 10, 13)));
        assertEquals(dateTime(2006, 10, 13, 12, 30, 1),
                DateTimeIteratorFactory.dateValueToDateTime(
                        new DateTimeValueImpl(2006, 10, 13, 12, 30, 1)));
    }

    public void testDateToDateTimeValue() {
        assertEquals(new DateTimeValueImpl(2006, 10, 13, 0, 0, 0),
                DateTimeIteratorFactory.dateTimeToDateValue(
                        dateTime(2006, 10, 13, 0, 0, 0)));
        assertEquals(new DateTimeValueImpl(2006, 10, 13, 12, 30, 1),
                DateTimeIteratorFactory.dateTimeToDateValue(
                        dateTime(2006, 10, 13, 12, 30, 1)));
    }

    public void testConsistency() {
        DateTimeValue dtv = new DateTimeValueImpl(2006, 10, 13, 12, 30, 1);
        assertEquals(dtv, DateTimeIteratorFactory.dateTimeToDateValue(
                DateTimeIteratorFactory.dateValueToDateTime(dtv)));
    }

    public void testCreateDateTimeIterableTimed() throws Exception {
        DateTimeIterable iterable = DateTimeIteratorFactory.createDateTimeIterable(
                "RRULE:FREQ=DAILY;INTERVAL=2;COUNT=8\n"
                        + "EXDATE:20060103T123001,20060105T123001,20060107,20060113T123001",
                dateTime(2006, 1, 1, 12, 30, 1, PST), PST, true);

        DateTimeIterator it = iterable.iterator();
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 1, 20, 30, 1), it.next());
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 7, 20, 30, 1), it.next());
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 9, 20, 30, 1), it.next());
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 11, 20, 30, 1), it.next());
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 15, 20, 30, 1), it.next());
        assertTrue(!it.hasNext());

        it = iterable.iterator();
        it.advanceTo(dateTime(2006, 1, 9, 0, 0, 0));
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 9, 20, 30, 1), it.next());
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 11, 20, 30, 1), it.next());
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 15, 20, 30, 1), it.next());
        assertTrue(!it.hasNext());

        it = iterable.iterator();
        it.advanceTo(dateTime(2006, 1, 9, 22, 30, 1));  // advance past
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 11, 20, 30, 1), it.next());
        assertTrue(it.hasNext());
        assertEquals(dateTime(2006, 1, 15, 20, 30, 1), it.next());
        assertTrue(!it.hasNext());
    }

    private static DateTime dateTime(int y, int m, int d, int h, int n, int s) {
        return dateTime(y, m, d, h, n, s, DateTimeZone.UTC);
    }

    private static DateTime dateTime(int y, int m, int d, int h, int n, int s,
                                     DateTimeZone tz) {
        return new DateTime(y, m, d, h, n, s, 0, tz);
    }
}
