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

package com.google.ical.compat.javatime;

import java.time.LocalDate;
import java.time.ZoneId;

import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;

import junit.framework.TestCase;

/**
 * testcases for {@link LocalDateIteratorFactory}.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class LocalDateIteratorFactoryTest extends TestCase {

    private static final ZoneId PST =
            ZoneId.of("America/Los_Angeles");

  public void testDateValueToLocalDate() throws Exception {
    assertEquals(date(2006, 10, 13),
                 LocalDateIteratorFactory.dateValueToLocalDate(
                     new DateValueImpl(2006, 10, 13)));
    assertEquals(date(2006, 10, 13),
                 LocalDateIteratorFactory.dateValueToLocalDate(
                     new DateTimeValueImpl(2006, 10, 13, 12, 30, 1)));
  }

  public void testLocalDateToDateTimeValue() throws Exception {
    assertEquals(new DateValueImpl(2006, 10, 13),
                 LocalDateIteratorFactory.localDateToDateValue(
                     date(2006, 10, 13)));
  }

  public void testConsistency() throws Exception {
    DateValue dv = new DateValueImpl(2006, 10, 13);
    assertEquals(dv, LocalDateIteratorFactory.localDateToDateValue(
                          LocalDateIteratorFactory.dateValueToLocalDate(dv)));
  }

  public void testCreateLocalDateIterableUntimed() throws Exception {
    LocalDateIterable iterable =
      LocalDateIteratorFactory.createLocalDateIterable(
          "RRULE:FREQ=DAILY;INTERVAL=2;COUNT=8\n"
          + "EXDATE:20060103,20060105,20060107T000000,20060113",
          date(2006, 1, 1), PST, true);

    LocalDateIterator it = iterable.iterator();
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

  public void testCreateLocalDateIterableTimed() throws Exception {
    LocalDateIterable iterable =
      LocalDateIteratorFactory.createLocalDateIterable(
          "RRULE:FREQ=DAILY;INTERVAL=2;COUNT=8\n"
          + "EXDATE:20060103,20060105,20060107T000000,20060113",
          date(2006, 1, 1), PST, true);

    LocalDateIterator it = iterable.iterator();
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

  private static LocalDate date(int y, int m, int d) {
    return LocalDate.of(y, m, d);
  }
}
