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

package com.google.ical.iter;

import com.google.ical.values.DateValue;
import com.google.ical.values.RDateList;
import junit.framework.TestCase;

import java.util.TimeZone;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class RDateIteratorImplTest extends TestCase {

    static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");
    static final TimeZone EST = TimeZone.getTimeZone("America/New_York");
    static final TimeZone UTC = TimeZone.getTimeZone("Etc/GMT");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertEquals(-8 * 60 * 60 * 1000, PST.getRawOffset());
        assertEquals(-5 * 60 * 60 * 1000, EST.getRawOffset());
        assertEquals(0, UTC.getRawOffset());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOneDate() throws Exception {
        runRecurrenceIteratorTest("RDATE:20060412", PST, "20060412");
        runRecurrenceIteratorTest("RDATE:20060412", EST, "20060412");
        runRecurrenceIteratorTest("RDATE:20060412", UTC, "20060412");
    }

    public void testOneDateTime() throws Exception {
        runRecurrenceIteratorTest("RDATE:20060412T120000", PST, "20060412T190000");
        runRecurrenceIteratorTest("RDATE:20060412T120000", EST, "20060412T160000");
        runRecurrenceIteratorTest("RDATE:20060412T120000", UTC, "20060412T120000");
    }

    public void testMore() throws Exception {
        runRecurrenceIteratorTest("RDATE:20060412,20060412", PST, "20060412");
        runRecurrenceIteratorTest(
                "RDATE:20060412,20060413", EST, "20060412,20060413");
        runRecurrenceIteratorTest(
                "RDATE:20060413,20060412", UTC, "20060412,20060413");
        runRecurrenceIteratorTest(
                "RDATE:20060413,20060412,20060413", UTC, "20060412,20060413");
        runRecurrenceIteratorTest(
                "RDATE:20060413,20060412,20060412", UTC, "20060412,20060413");
    }

    private void runRecurrenceIteratorTest(
            String icalText, TimeZone tz, String golden)
            throws Exception {
        runRecurrenceIteratorTest(icalText, tz, golden, null);
    }

    private void runRecurrenceIteratorTest(
            String icalText, TimeZone tz, String golden, DateValue advanceTo)
            throws Exception {
        RecurrenceIterator ri = RecurrenceIteratorFactory.createRecurrenceIterator(
                new RDateList(icalText, tz));
        if (null != advanceTo) {
            ri.advanceTo(advanceTo);
        }
        StringBuilder sb = new StringBuilder();
        int k = 0;
        while (ri.hasNext()) {
            if (k++ != 0) {
                sb.append(',');
            }
            sb.append(ri.next());
        }
        assertEquals(golden, sb.toString());
    }

}
