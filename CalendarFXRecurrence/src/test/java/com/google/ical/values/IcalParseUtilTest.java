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

import java.text.ParseException;
import java.util.TimeZone;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class IcalParseUtilTest extends TestCase {

    private static final TimeZone PDT =
            TimeZone.getTimeZone("America/Los_Angeles");
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    public void testParseDates() throws Exception {
        assertEquals(new DateValueImpl(2006, 2, 25),
                IcalParseUtil.parseDateValue("20060225", null));
        assertEquals(new DateValueImpl(2006, 2, 25),
                IcalParseUtil.parseDateValue("20060225", PDT));
        assertEquals(new DateValueImpl(2006, 2, 25),
                IcalParseUtil.parseDateValue("20060225", UTC));
    }

    public void testParseDateTimes() throws Exception {
        assertEquals(new DateTimeValueImpl(2006, 2, 25, 12, 0, 0),
                IcalParseUtil.parseDateValue("20060225T120000", null));
        assertEquals(new DateTimeValueImpl(2006, 2, 25, 20, 0, 0),
                IcalParseUtil.parseDateValue("20060225T120000", PDT));
        assertEquals(new DateTimeValueImpl(2006, 2, 25, 12, 30, 5),
                IcalParseUtil.parseDateValue("20060225T123005", UTC));
    }

    public void testParseDateTimesUtc() throws Exception {
        assertEquals(new DateTimeValueImpl(2006, 2, 25, 12, 0, 0),
                IcalParseUtil.parseDateValue("20060225T120000Z", null));
        assertEquals(new DateTimeValueImpl(2006, 2, 25, 12, 0, 0),
                IcalParseUtil.parseDateValue("20060225T120000Z", PDT));
        assertEquals(new DateTimeValueImpl(2006, 2, 25, 12, 30, 5),
                IcalParseUtil.parseDateValue("20060225T123005Z", UTC));
    }

    public void testBadDates() {
        try {
            IcalParseUtil.parsePeriodValue("19700101/19700101T010000");
            fail("mismatched types");
        } catch (ParseException ex) {
            // pass
        }
        try {
            IcalParseUtil.parsePeriodValue("19700101T120000/19700102");
            fail("mismatched types");
        } catch (ParseException ex) {
            // pass
        }
        try {
            IcalParseUtil.parsePeriodValue("19700102/19700101");
            fail("misordered dates");
        } catch (ParseException ex) {
            // pass
        }
        try {
            IcalParseUtil.parsePeriodValue("19700102");
            fail("missing end");
        } catch (ParseException ex) {
            // pass
        }
        try {
            IcalParseUtil.parsePeriodValue("so/bogus");
            fail("bogus");
        } catch (ParseException ex) {
            // pass
        }
    }

    public void testNormalized() throws Exception {
        assertEquals(new DateTimeValueImpl(2006, 3, 1, 12, 0, 0),
                IcalParseUtil.parseDateValue("20060229T120000Z", null));
    }

    public void testUnfold() {
        assertEquals("", IcalParseUtil.unfoldIcal(""));
        assertEquals("foo", IcalParseUtil.unfoldIcal("foo"));
        assertEquals("foo", IcalParseUtil.unfoldIcal("f\r\to\n o\r\n "));
    }

}
