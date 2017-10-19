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

package com.google.ical.iter;

import com.google.ical.values.RRule;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.util.TimeUtils;
import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class StressTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // prime the VM
    for (int runs = 10; --runs >= 0;) {
      runOne();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testSpeed() throws Exception {
    long t0 = System.nanoTime();
    // cycle through 10 recurrence rules, advancing and pulling a few dates off
    // each
    for (int runs = 5000; --runs >= 0;) {
      runOne();
    }
    long dt = System.nanoTime() - t0;
    System.out.println(getName() + " took " + (dt / 1e6) + " ms");
  }

  static String[] RECURRENCE_RULES = {
    "RRULE:FREQ=DAILY",
    "RRULE:FREQ=WEEKLY;BYDAY=TU,TH",
    "RRULE:FREQ=WEEKLY;BYDAY=TU,TH;COUNT=10",
    "RRULE:FREQ=WEEKLY;BYDAY=TU,TH;UNTIL=20060801",
    "RRULE:FREQ=MONTHLY;INTERVAL=2;BYDAY=TH;UNTIL=20060801",
    "RRULE:FREQ=MONTHLY;BYMONTHDAY=13;BYDAY=FR;UNTIL=20060801",
    "RRULE:FREQ=YEARLY;BYMONTH=6;BYMONTHDAY=15;UNTIL=20200615",
  };

  private static final DateValue DT_START = new DateValueImpl(2006, 4, 3);
  private static final DateValue T0 = new DateValueImpl(2006, 8, 3);

  void runOne() throws Exception {
    for (String rdata : RECURRENCE_RULES) {
      RRule rrule = new RRule(rdata);
      RecurrenceIterator iter =
        RecurrenceIteratorFactory.createRecurrenceIterator(
            rrule, DT_START, TimeUtils.utcTimezone());
      iter.advanceTo(T0);
      for (int k = 20; iter.hasNext() && --k >= 0;) {
        iter.next();
      }
    }
  }

}
