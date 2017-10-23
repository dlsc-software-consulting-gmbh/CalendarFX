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

import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValueImpl;
import junit.framework.TestCase;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * testcases for {@link ZonedDateTimeIteratorFactory}.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class ZonedDateTimeIteratorFactoryTest extends TestCase {

    private static final ZoneId PST =
            ZoneId.of("America/Los_Angeles");

    public void testDateValueToDateTime() throws Exception {
        assertEquals(dateTime(2006, 10, 13, 0, 0, 0),
                ZonedDateTimeIteratorFactory.dateValueToDateTime(
                        new DateValueImpl(2006, 10, 13)));
        assertEquals(dateTime(2006, 10, 13, 12, 30, 1),
                ZonedDateTimeIteratorFactory.dateValueToDateTime(
                        new DateTimeValueImpl(2006, 10, 13, 12, 30, 1)));
    }

    public void testDateToDateTimeValue() throws Exception {
        assertEquals(new DateTimeValueImpl(2006, 10, 13, 0, 0, 0),
                ZonedDateTimeIteratorFactory.zonedDateTimeToDateValue(
                        dateTime(2006, 10, 13, 0, 0, 0)));
        assertEquals(new DateTimeValueImpl(2006, 10, 13, 12, 30, 1),
                ZonedDateTimeIteratorFactory.zonedDateTimeToDateValue(
                        dateTime(2006, 10, 13, 12, 30, 1)));
    }

    public void testConsistency() throws Exception {
        DateTimeValue dtv = new DateTimeValueImpl(2006, 10, 13, 12, 30, 1);
        assertEquals(dtv, ZonedDateTimeIteratorFactory.zonedDateTimeToDateValue(
                ZonedDateTimeIteratorFactory.dateValueToDateTime(dtv)));
    }

    public void testCreateDateTimeIterableTimed() throws Exception {
        ZonedDateTimeIterable iterable = ZonedDateTimeIteratorFactory.createDateTimeIterable(
                "RRULE:FREQ=DAILY;INTERVAL=2;COUNT=8\n"
                        + "EXDATE:20060103T123001,20060105T123001,20060107,20060113T123001",
                dateTime(2006, 1, 1, 12, 30, 1, PST), PST, true);

        ZonedDateTimeIterator it = iterable.iterator();
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

    private static ZonedDateTime dateTime(int y, int m, int d, int h, int n, int s) {
        return dateTime(y, m, d, h, n, s, ZoneId.of("UTC"));
    }

    private static ZonedDateTime dateTime(int y, int m, int d, int h, int n, int s,
                                          ZoneId tz) {
        return ZonedDateTime.of(y, m, d, h, n, s, 0, tz);
    }
}
