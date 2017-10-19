/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
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

import com.google.ical.util.TimeUtils;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import junit.framework.TestCase;

/**
 * testcases for {@link DateIteratorFactory}.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class DateIteratorFactoryTest extends TestCase {

  private static final TimeZone PST =
      TimeZone.getTimeZone("America/Los_Angeles");

  public void testDateValueToDate() throws Exception {
    assertEquals(createDateUtc(2006, 10, 13, 0, 0, 0),
                 DateIteratorFactory.dateValueToDate(
                     new DateValueImpl(2006, 10, 13)));
    assertEquals(createDateUtc(2006, 10, 13, 12, 30, 1),
                 DateIteratorFactory.dateValueToDate(
                     new DateTimeValueImpl(2006, 10, 13, 12, 30, 1)));
  }

  public void testDateToDateTimeValue() throws Exception {
    assertEquals(new DateTimeValueImpl(2006, 10, 13, 0, 0, 0),
                 DateIteratorFactory.dateToDateValue(
                     createDateUtc(2006, 10, 13, 0, 0, 0), false));
    assertEquals(new DateValueImpl(2006, 10, 13),
                 DateIteratorFactory.dateToDateValue(
                     createDateUtc(2006, 10, 13, 0, 0, 0), true));
    assertEquals(new DateTimeValueImpl(2006, 10, 13, 12, 30, 1),
                 DateIteratorFactory.dateToDateValue(
                     createDateUtc(2006, 10, 13, 12, 30, 1), false));
    assertEquals(new DateTimeValueImpl(2006, 10, 13, 12, 30, 1),
                 DateIteratorFactory.dateToDateValue(
                     createDateUtc(2006, 10, 13, 12, 30, 1), true));
  }

  public void testConsistency() throws Exception {
    DateValue dv = new DateValueImpl(2006, 10, 13),
             dtv = new DateTimeValueImpl(2006, 10, 13, 12, 30, 1);
    assertEquals(dv, DateIteratorFactory.dateToDateValue(
                          DateIteratorFactory.dateValueToDate(dv), true));
    assertEquals(dtv, DateIteratorFactory.dateToDateValue(
                          DateIteratorFactory.dateValueToDate(dtv), true));
  }

  public void testCreateDateIterableUntimed() throws Exception {
    DateIterable iterable = DateIteratorFactory.createDateIterable(
        "RRULE:FREQ=DAILY;INTERVAL=2;COUNT=8\n"
        + "EXDATE:20060103,20060105,20060107T000000,20060113",
        date(2006, 1, 1), PST, true);

    DateIterator it = iterable.iterator();
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 1), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 7), it.next());  // does not match midnight
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 9), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 11), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 15), it.next());
    assertTrue(!it.hasNext());

    it = iterable.iterator();
    it.advanceTo(date(2006, 1, 9));
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 9), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 11), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 15), it.next());
    assertTrue(!it.hasNext());
  }

  public void testCreateDateIterableTimed() throws Exception {
    DateIterable iterable = DateIteratorFactory.createDateIterable(
        "RRULE:FREQ=DAILY;INTERVAL=2;COUNT=8\n"
        + "EXDATE:20060103T123001,20060105T123001,20060107,20060113T123001",
        date(2006, 1, 1, 12, 30, 1), PST, true);

    DateIterator it = iterable.iterator();
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 1, 20, 30, 1), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 7, 20, 30, 1), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 9, 20, 30, 1), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 11, 20, 30, 1), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 15, 20, 30, 1), it.next());
    assertTrue(!it.hasNext());

    it = iterable.iterator();
    it.advanceTo(date(2006, 1, 9));
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 9, 20, 30, 1), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 11, 20, 30, 1), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 15, 20, 30, 1), it.next());
    assertTrue(!it.hasNext());


    it = iterable.iterator();
    it.advanceTo(date(2006, 1, 9, 22, 30, 1));  // advance past
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 11, 20, 30, 1), it.next());
    assertTrue(it.hasNext());
    assertEquals(date(2006, 1, 15, 20, 30, 1), it.next());
    assertTrue(!it.hasNext());
  }

  private Date createDateUtc(int ye, int mo, int da, int ho, int mi, int se) {
    Calendar c = new GregorianCalendar(TimeUtils.utcTimezone());
    c.clear();
    c.set(ye, mo - 1, da, ho, mi, se);
    return c.getTime();
  }

  private static Date date(int y, int m, int d) {
    return DateIteratorFactory.dateValueToDate(new DateValueImpl(y, m, d));
  }

  private static Date date(int y, int m, int d, int h, int n, int s) {
    return DateIteratorFactory.dateValueToDate(
        new DateTimeValueImpl(y, m, d, h, n, s));
  }
}
