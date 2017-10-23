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

package com.google.ical.values;

import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class VcalRewriterTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRewriteRule() throws Exception {
        // Daily for the rest of 2002
        assertEquals("RRULE:FREQ=DAILY;UNTIL=20021231T000000Z",
                VcalRewriter.rewriteRule("RRULE:D1 20021231T000000"));

        // Every 10th day forever
        assertEquals("RRULE:FREQ=DAILY;INTERVAL=10",
                VcalRewriter.rewriteRule("RRULE:D10 #0"));

        // Every other Sunday forever:
        assertEquals("RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=SU",
                VcalRewriter.rewriteRule("RRULE:W2 SU #0"));

        // Every Tuesday and Thursday until the end of October:
        assertEquals(
                "RRULE:FREQ=WEEKLY;BYDAY=TU,TH;UNTIL=20021031T000000Z",
                VcalRewriter.rewriteRule("RRULE:W1 TU TH 20021031T000000"));

        // First and third Tuesday of every month:
        assertEquals("RRULE:FREQ=MONTHLY;BYDAY=1TU,3TU",
                VcalRewriter.rewriteRule("RRULE:MP1 1+ 3+ TU #0"));

        // Second to last Friday of every other month:
        assertEquals("RRULE:FREQ=MONTHLY;INTERVAL=2;BYDAY=-2FR",
                VcalRewriter.rewriteRule("RRULE:MP2 2- FR #0"));

        // Every Friday in the month:  (not in spec)
        assertEquals("RRULE:FREQ=MONTHLY;BYDAY=FR",
                VcalRewriter.rewriteRule("RRULE:MP FR #0"));

        // Every Thursday and Friday of every second month:  (not in spec)
        assertEquals("RRULE:FREQ=MONTHLY;INTERVAL=2;BYDAY=TH,FR",
                VcalRewriter.rewriteRule("RRULE:MP2 TH FR"));

        // First of every fourth month:
        assertEquals("RRULE:FREQ=MONTHLY;INTERVAL=4;BYMONTHDAY=1",
                VcalRewriter.rewriteRule("RRULE:MD4 1 #0"));

        // My paydays (15th and Last day of every month):
        assertEquals("RRULE:FREQ=MONTHLY;BYMONTHDAY=15,-1",
                VcalRewriter.rewriteRule("RRULE:MD1 15 1- #0"));
        assertEquals("RRULE:FREQ=MONTHLY;BYMONTHDAY=15,-1",
                VcalRewriter.rewriteRule("RRULE:MD1 15 LD #0"));
        // Every four years in February:
        assertEquals("RRULE:FREQ=YEARLY;INTERVAL=4;BYMONTH=2;COUNT=10",
                VcalRewriter.rewriteRule("RRULE:YM4 2 #10"));

        // Every year in June, July, and August:
        assertEquals("RRULE:FREQ=YEARLY;BYMONTH=6,7,8;COUNT=10",
                VcalRewriter.rewriteRule("RRULE:YM1 6 7 8 #10"));

        // Every year on the 100th day:
        assertEquals("RRULE:FREQ=YEARLY;BYYEARDAY=100",
                VcalRewriter.rewriteRule("RRULE:YD1 100 #0"));

        // Every other year on the 243rd day:
        assertEquals("RRULE:FREQ=YEARLY;INTERVAL=2;BYYEARDAY=243",
                VcalRewriter.rewriteRule("RRULE:YD2 243 #0"));
    }

    public void testRewriteDoesntInterfere() throws Exception {
        assertEquals("RRULE:FREQ=YEARLY;INTERVAL=2;BYYEARDAY=243",
                VcalRewriter.rewriteRule(
                        "RRULE:FREQ=YEARLY;INTERVAL=2;BYYEARDAY=243"));
    }
}
