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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * testcases for {@link PeriodValueImpl}.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class PeriodValueImplTest extends TestCase {

    private static final DateValue[] DATE_VALUES = new DateValue[16];
    private static final DateTimeValue[] DATE_TIME_VALUES = new DateTimeValue[16];

    static {
        Random rnd = new Random();
        for (int i = 0; i < DATE_VALUES.length; ++i) {
            DATE_VALUES[i] =
                    new DateValueImpl((int) (2000 + rnd.nextGaussian() * 100),
                            1 + rnd.nextInt(12),
                            1 + i /* guarantee no equal items */);
        }
        for (int i = 0; i < DATE_TIME_VALUES.length; ++i) {
            DATE_TIME_VALUES[i] =
                    new DateTimeValueImpl((int) (2000 + rnd.nextGaussian() * 100),
                            1 + rnd.nextInt(12),
                            1 + rnd.nextInt(28),
                            rnd.nextInt(24),
                            rnd.nextInt(60),
                            i /* guarantee no equal items */);
        }
        Arrays.sort(DATE_VALUES);
        Arrays.sort(DATE_TIME_VALUES);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor() throws Exception {
        Random rnd = new Random();
        for (DateValue[] values :
                Arrays.asList(new DateValue[][]{DATE_VALUES, DATE_TIME_VALUES})) {
            for (int i = 0; i < 100; i++) {
                int k = rnd.nextInt(values.length / 2);
                DateValue start = values[k];
                DateValue end = values[k + rnd.nextInt(values.length - k)];
                PeriodValue p = PeriodValueImpl.create(start, end);
                assertEquals(start, p.start());
                assertEquals(end, p.end());
            }
        }
        // check out of order failures
        for (DateValue[] values :
                Arrays.asList(new DateValue[][]{DATE_VALUES, DATE_TIME_VALUES})) {
            for (int i = 0; i < 100; i++) {
                int k = rnd.nextInt(values.length / 2);
                DateValue start = values[k + 1 + rnd.nextInt(values.length - k - 1)];
                DateValue end = values[k];
                try {
                    PeriodValue p = PeriodValueImpl.create(start, end);
                    fail("Bad PeriodValue: " + p);
                } catch (RuntimeException ex) {
                    // pass
                }
            }
        }
        // check cross genus failures
        for (int i = 0; i < 100; i++) {
            DateValue a = DATE_VALUES[rnd.nextInt(DATE_VALUES.length)],
                    b = DATE_TIME_VALUES[rnd.nextInt(DATE_TIME_VALUES.length)];
            if (a.compareTo(b) > 0) {
                DateValue t = a;
                a = b;
                b = t;
            }
            try {
                PeriodValue p = PeriodValueImpl.create(a, b);
                fail("Bad PeriodValue: " + p);
            } catch (RuntimeException ex) {
                // pass
            }
        }
    }

    public void testDurationConstructor() throws Exception {
        final DateValue DV = new DateValueImpl(2005, 2, 15);
        final DateTimeValue DTV0 = new DateTimeValueImpl(2005, 2, 15, 0, 0, 0),
                DTV12 = new DateTimeValueImpl(2005, 2, 15, 12, 0, 0);
        DateValue ONE_DAY = new DateValueImpl(0, 0, 1),
                YESTERDAY = new DateValueImpl(0, 0, -1),
                ONE_WEEK = new DateValueImpl(0, 0, 7),
                ONE_MONTH = new DateValueImpl(0, 1, 0),
                ONE_YEAR = new DateValueImpl(1, 0, 0);
        DateTimeValue ONE_HOUR = new DateTimeValueImpl(0, 0, 0, 1, 0, 0),
                TWELVE_HOURS = new DateTimeValueImpl(0, 0, 0, 12, 0, 0),
                SAME_TIME = new DateTimeValueImpl(0, 0, 0, 0, 0, 0);

        assertEquals(PeriodValueImpl.create(DV, new DateValueImpl(2005, 2, 16)),
                PeriodValueImpl.createFromDuration(DV, ONE_DAY));
        assertEquals(PeriodValueImpl.create(DV, new DateValueImpl(2005, 2, 22)),
                PeriodValueImpl.createFromDuration(DV, ONE_WEEK));
        assertEquals(PeriodValueImpl.create(DV, new DateValueImpl(2005, 3, 15)),
                PeriodValueImpl.createFromDuration(DV, ONE_MONTH));
        assertEquals(PeriodValueImpl.create(DV, new DateValueImpl(2006, 2, 15)),
                PeriodValueImpl.createFromDuration(DV, ONE_YEAR));
        try {
            PeriodValueImpl.createFromDuration(DV, YESTERDAY);
            fail("I believe our adventures in time have taken a most serious turn.");
        } catch (IllegalArgumentException ex) {
            // pass
        }
        assertEquals(PeriodValueImpl.create(
                DTV0, new DateTimeValueImpl(2005, 2, 16, 0, 0, 0)),
                PeriodValueImpl.createFromDuration(DTV0, ONE_DAY));
        assertEquals(PeriodValueImpl.create(
                DTV0, new DateTimeValueImpl(2005, 2, 15, 1, 0, 0)),
                PeriodValueImpl.createFromDuration(DTV0, ONE_HOUR));
        assertEquals(PeriodValueImpl.create(
                DTV12, new DateTimeValueImpl(2005, 2, 15, 13, 0, 0)),
                PeriodValueImpl.createFromDuration(DTV12, ONE_HOUR));
        assertEquals(PeriodValueImpl.create(
                DTV0, new DateTimeValueImpl(2005, 2, 15, 12, 0, 0)),
                PeriodValueImpl.createFromDuration(DV, TWELVE_HOURS));
        assertEquals(PeriodValueImpl.create(
                DTV0, new DateTimeValueImpl(2005, 2, 15, 12, 0, 0)),
                PeriodValueImpl.createFromDuration(DTV0, TWELVE_HOURS));
        assertEquals(PeriodValueImpl.create(DTV12, DTV12),
                PeriodValueImpl.createFromDuration(DTV12, SAME_TIME));
    }

    public void testContains() throws Exception {
        Random rnd = new Random();
        for (DateValue[] values :
                Arrays.asList(new DateValue[][]{DATE_VALUES, DATE_TIME_VALUES})) {
            int a = rnd.nextInt(4),
                    b = a + 1 + rnd.nextInt(4),
                    c = b + 1 + rnd.nextInt(4),
                    d = c + 1 + rnd.nextInt(4);
            PeriodValue ab = PeriodValueImpl.create(values[a], values[b]),
                    ad = PeriodValueImpl.create(values[a], values[d]),
                    bc = PeriodValueImpl.create(values[b], values[c]),
                    cd = PeriodValueImpl.create(values[c], values[d]);

            assertTrue(ab.contains(ab));
            assertTrue(!ab.contains(ad));
            assertTrue(!ab.contains(bc));
            assertTrue(!ab.contains(cd));
            assertTrue(ad.contains(ab));
            assertTrue(ad.contains(ad));
            assertTrue(ad.contains(bc));
            assertTrue(ad.contains(cd));
            assertTrue(!bc.contains(ab));
            assertTrue(!bc.contains(ad));
            assertTrue(bc.contains(bc));
            assertTrue(!bc.contains(cd));
            assertTrue(!cd.contains(ab));
            assertTrue(!cd.contains(ad));
            assertTrue(!cd.contains(bc));
            assertTrue(cd.contains(cd));
        }
    }

    public void testIntersects() throws Exception {
        Random rnd = new Random();
        for (DateValue[] values :
                Arrays.asList(new DateValue[][]{DATE_VALUES, DATE_TIME_VALUES})) {
            int a = rnd.nextInt(4),
                    b = a + 1 + rnd.nextInt(4),
                    c = b + 1 + rnd.nextInt(4),
                    d = c + 1 + rnd.nextInt(4);
            PeriodValue ab = PeriodValueImpl.create(values[a], values[b]),
                    ad = PeriodValueImpl.create(values[a], values[d]),
                    bc = PeriodValueImpl.create(values[b], values[c]),
                    cd = PeriodValueImpl.create(values[c], values[d]);

            assertTrue(ab.intersects(ab));
            assertTrue(ab.intersects(ad));
            assertTrue(!ab.intersects(bc));
            assertTrue(!ab.intersects(cd));
            assertTrue(ad.intersects(ab));
            assertTrue(ad.intersects(ad));
            assertTrue(ad.intersects(bc));
            assertTrue(ad.intersects(cd));
            assertTrue(!bc.intersects(ab));
            assertTrue(bc.intersects(ad));
            assertTrue(bc.intersects(bc));
            assertTrue(!bc.intersects(cd));
            assertTrue(!cd.intersects(ab));
            assertTrue(cd.intersects(ad));
            assertTrue(!cd.intersects(bc));
            assertTrue(cd.intersects(cd));
        }
    }

    public void testEqualsAndHashcode() throws Exception {
        Random rnd = new Random();
        for (DateValue[] values :
                Arrays.asList(new DateValue[][]{DATE_VALUES, DATE_TIME_VALUES})) {
            Set<Integer> hashCodes = new HashSet<Integer>();
            for (DateValue dv : values) {
                hashCodes.add(dv.hashCode());
            }
            double collisionRate =
                    ((hashCodes.size() - values.length) / (double) values.length);
            if (collisionRate > 0.10) {
                fail("Unacceptable hash collision rate: " + collisionRate + " for "
                        + Arrays.asList(values));
            }
            for (int i = 0; i < 10000; ++i) {
                DateValue a = values[rnd.nextInt(values.length)],
                        b = values[rnd.nextInt(values.length)];
                implies(a.equals(b), a.hashCode() == b.hashCode());
            }
            for (int i = 0; i < 1000; ++i) {
                int a = rnd.nextInt(4),
                        b = a + rnd.nextInt(4),
                        c = rnd.nextInt(4),
                        d = c + rnd.nextInt(4);
                // should see at least 6 equal pairs chosen per 100 runs
                PeriodValue pAB = PeriodValueImpl.create(values[a], values[b]),
                        pCD = PeriodValueImpl.create(values[c], values[d]);
                assertEquals(a == c && b == d, pAB.equals(pCD));
                implies(pAB.equals(pCD), pAB.hashCode() == pCD.hashCode());
            }
            for (int i = 0; i < 10000; ++i) {
                int a = rnd.nextInt(values.length),
                        b = rnd.nextInt(values.length);
                DateValue dvA = values[a], dvB = values[b];
                assertEquals(a == b, dvA.equals(dvB));
                assertEquals(dvA.equals(dvB), 0 == dvA.compareTo(dvB));
            }
        }
    }

    public void testToString() throws Exception {
        String a = "20050411T120000",
                b = "20050413T174330";
        assertEquals(a + "/" + b, PeriodValueImpl.create
                (IcalParseUtil.parseDateValue(a),
                        IcalParseUtil.parseDateValue(b)).toString());
    }

    private static void implies(boolean a, boolean b) {
        assertTrue(!a | b);
    }

}
