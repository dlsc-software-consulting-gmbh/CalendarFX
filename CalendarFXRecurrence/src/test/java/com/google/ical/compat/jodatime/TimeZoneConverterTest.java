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

import junit.framework.TestCase;
import org.joda.time.DateTimeZone;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.TimeZone;

/**
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class TimeZoneConverterTest extends TestCase {

    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long MILLIS_PER_YEAR = (long) (365.25 * MILLIS_PER_DAY);

    public void testConvertMonteCarlo() {
        long seed = 1161647988961L;
        Random rand = new Random(seed);
        System.out.println("seed=" + seed);

        // DateTimeZone id, followed by TimeZone id (which is null if we
        // are to reuse the DateTimeZone id)
        String[] tzids = {
                "America/Los_Angeles", null,  // one in the Western hemisphire
                // with daylight
                "America/Belize", null,
                "UTC", null,                  // UTC
                "-07:00", "GMT-07:00",
                "+08:15", "GMT+08:15",
                "Europe/Paris", null,         // one in the Eastern hemisphere with
                // daylight savings
                "Asia/Shanghai", null,        // has daylight savings
                "Pacific/Tongatapu", null,    // outside [-12,+12]
        };

        long soon = System.currentTimeMillis() + (7 * MILLIS_PER_DAY);
        for (int i = 0; i < tzids.length; i += 2) {
            String tzid = tzids[i];
            String timeZoneTzid = tzids[i + 1] == null ? tzid : tzids[i + 1];
            TimeZone utilTz = TimeZone.getTimeZone(timeZoneTzid);
            DateTimeZone jodaTz = DateTimeZone.forID(tzid);
            // make sure that the timezone is recognized and we're not just testing
            // UTC over and over.
            assertTrue(utilTz.getID(),
                    "UTC".equals(utilTz.getID()) ||
                            !utilTz.hasSameRules(TimeZone.getTimeZone("UTC")));

            // check that we're working a week out.
            assertOffsetsEqualForDate(utilTz, jodaTz, soon);

            // generate a bunch of random times in 2006 and test that the offsets
            // convert properly
            for (int run = 5000; --run >= 0; ) {
                // pick a random time in 2006
                long t = ((2000 - 1970) * MILLIS_PER_YEAR)
                        + (rand.nextLong() % (10L * MILLIS_PER_YEAR));
                assertOffsetsEqualForDate(utilTz, jodaTz, t);
            }
        }
    }

    public void testCleanUpTzid() {
        assertEquals("GMT+06:12", TimeZoneConverter.cleanUpTzid("06:12"));
        assertEquals("GMT-07:00", TimeZoneConverter.cleanUpTzid("-07:00"));
        assertEquals("GMT+12:00", TimeZoneConverter.cleanUpTzid("+12:00"));
        // Improbable case (DateTimeZone won't return an ID of this form)
        assertEquals("GMT+1:00", TimeZoneConverter.cleanUpTzid("+1:00"));
    }

    /**
     * These may change as the DateTimeZone is updated!  Check
     * http://www.worldtimezone.com/dst_news/ for updates.
     */
    public void testSomeTimeZonesDST() {
        assertDST("UTC", false);
        assertDST("America/Chicago", true);
        assertDST("America/Guatemala", true);
        // China should not observe DST
        assertDST("Asia/Shanghai", false);
        assertDST("Europe/Uzhgorod", true);
        assertDST("Europe/Helsinki", true);
        assertDST("Etc/GMT+3", false);
        assertDST("Pacific/Port_Moresby", false);
        assertDST("Australia/Sydney", true);
        assertDST("Australia/Brisbane", false);
        assertDST("Australia/Darwin", false);
        assertDST("Australia/Hobart", true);
        assertDST("Australia/Melbourne", true);
        assertDST("Australia/Adelaide", true);
    }

    public void testEquality() {
        TimeZone tz1 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("America/Los_Angeles"));
        TimeZone tz2 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("America/Los_Angeles"));
        checkEqualsAndHashCodeMethods(tz1, tz2, true);

        // Try two timezones with the same offsets but different names
        tz1 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("America/Los_Angeles"));
        tz2 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("US/Pacific"));
        checkEqualsAndHashCodeMethods(tz1, tz2, true);

        // Try two timezones that should be different
        tz1 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("America/Los_Angeles"));
        tz2 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("America/Chicago"));
        checkEqualsAndHashCodeMethods(tz1, tz2, false);

        // Try two timezones that are similar but not the same (first has
        // dst, second does not)
        tz1 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("America/Los_Angeles"));
        tz2 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("Etc/GMT+8"));
        checkEqualsAndHashCodeMethods(tz1, tz2, false);

        // Some more cases
        tz1 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("UTC"));
        tz2 = TimeZoneConverter.toTimeZone(
                DateTimeZone.forID("Etc/GMT+8"));
        checkEqualsAndHashCodeMethods(tz1, tz2, false);
    }

    private static void assertDST(String tzid, boolean expectedHasDST) {
        TimeZone tz = TimeZoneConverter.toTimeZone(DateTimeZone.forID(tzid));
        assertEquals(tzid, tz.getID());
        assertEquals(tzid + " has DST at "
                        + new Date(DateTimeZone.forID(tzid).nextTransition(1L))
                        + "?: " + expectedHasDST,
                expectedHasDST, tz.useDaylightTime());
        assertEquals(tzid + " has DST?: " + expectedHasDST,
                expectedHasDST ?
                        1 * TimeZoneConverter.MILLISECONDS_PER_HOUR :
                        0, tz.getDSTSavings());
    }

    private static void assertOffsetsEqualForDate(
            TimeZone utilTz, DateTimeZone jodaTz, long offset) {

        TimeZone convertedTz = TimeZoneConverter.toTimeZone(jodaTz);

        // Test that the util timezone and it's joda timezone are equivalent.
        assertEquals("offset=" + offset + " in " + utilTz.getID(),
                utilTz.getOffset(offset), convertedTz.getOffset(offset));

        // Test the complicated getOffset method.
        // We don't care which tz the output fields are in since we're not
        // concerned that the output from getOffset(...) == offset,
        // just that the utilTz.getOffset(...) == jodaTz.getOffset(...) computed
        // from it are equal for both timezones.
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date(offset));
        assertEquals("offset=" + offset + " in " + utilTz.getID(),
                utilTz.getOffset(
                        c.get(Calendar.ERA),
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH),
                        c.get(Calendar.DAY_OF_WEEK),
                        (int) (offset % MILLIS_PER_DAY)),
                convertedTz.getOffset(
                        c.get(Calendar.ERA),
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH),
                        c.get(Calendar.DAY_OF_WEEK),
                        (int) (offset % MILLIS_PER_DAY)));
    }

    private static void checkEqualsAndHashCodeMethods(
            TimeZone tz1, TimeZone tz2, boolean equal) {
        if (equal) {
            assertEquals(tz1, tz2);
            assertEquals(tz1 + " == " + tz2, tz1.hashCode(), tz2.hashCode());
        } else {
            assertTrue(tz1 + " != " + tz2, !tz1.equals(tz2));
        }
    }
}
