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

import com.google.ical.util.TimeUtils;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.RDateList;
import org.junit.Assert;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.TimeZone;

/**
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class CompoundIteratorImplTest extends TestCase {
    // TODO(msamuel): add these tests to javascript too

    static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");
    static final TimeZone EST = TimeZone.getTimeZone("America/New_York");
    static final TimeZone UTC = TimeUtils.utcTimezone();

    private void runRecurrenceIteratorTest(
            String rdata, DateValue dtStart, TimeZone tz, int limit,
            DateValue advanceTo, String golden)
            throws Exception {
        RecurrenceIterator ri = RecurrenceIteratorFactory.createRecurrenceIterator(
                rdata, dtStart, tz);
        runRecurrenceIteratorTest(ri, limit, advanceTo, golden);
    }

    private void runRecurrenceIteratorTest(
            RecurrenceIterator ri, int limit, DateValue advanceTo, String golden) {
        if (null != advanceTo) {
            ri.advanceTo(advanceTo);
        }
        StringBuilder sb = new StringBuilder();
        int k = 0;
        while (ri.hasNext() && --limit >= 0) {
            if (k++ != 0) {
                sb.append(',');
            }
            sb.append(ri.next());
        }
        if (limit < 0) {
            sb.append(",...");
        }
        assertEquals(golden, sb.toString());
    }

    public void testEmptyCompoundIterators() throws Exception {
        runRecurrenceIteratorTest(
                "", new DateValueImpl(2006, 4, 13), PST, 10, null, "20060413");
    }

    public void testMultipleChecksDontChange() throws Exception {
        RecurrenceIterator ri = RecurrenceIteratorFactory.createRecurrenceIterator(
                "RRULE:FREQ=WEEKLY;BYDAY=TH;COUNT=3", new DateValueImpl(2006, 4, 9),
                PST);
        StringBuilder sb = new StringBuilder();
        int k = 0;
        int limit = 50;
        while (ri.hasNext() && ri.hasNext() && --limit >= 0) {
            if (k++ != 0) {
                sb.append(',');
            }
            sb.append(ri.next());
        }
        if (limit < 0) {
            sb.append(",...");
        }
        assertEquals("20060409,20060413,20060420,20060427", sb.toString());
    }

    public void testInterleavingOfDateIterators() throws Exception {
        runRecurrenceIteratorTest(
                "RDATE:20060418,20070101\n"
                        + "RDATE:20060422,20060417,2006\n 0412",
                new DateValueImpl(2006, 4, 13), PST, 10, null,
                "20060412,20060413,20060417,20060418,20060422,20070101");
    }

    public void testInterleavingOfDateIterators2() throws Exception {
        runRecurrenceIteratorTest(
                "RDATE:20060418,20070101\n"
                        + "RDATE:20060422,20060417,2006\n 0412",
                new DateValueImpl(2006, 4, 13), PST, 10, new DateValueImpl(2006, 4, 10),
                "20060412,20060413,20060417,20060418,20060422,20070101");
    }

    public void testInterleavingOfDateIterators3() throws Exception {
        runRecurrenceIteratorTest(
                "RDATE:20060418,20070101\n"
                        + "RDATE:20060422,20060417,2006\n 0412",
                new DateValueImpl(2006, 4, 13), PST, 10, new DateValueImpl(2006, 4, 20),
                /*"20060412,20060413,20060417,20060418,*/"20060422,20070101");
    }

    public void testInterleavingOfDateIteratorsWithExclusions() throws Exception {
        runRecurrenceIteratorTest(
                "RDATE:20060417,20060418,20070101,20060417\n"
                        + "EXDATE:20060417,20060415\n\n"
                        + "RDATE:20060422,20060417,2006\n 0412",
                new DateValueImpl(2006, 4, 13), PST, 10, null,
                "20060412,20060413,20060418,20060422,20070101");
    }

    public void testInterleavingOfDateIteratorsWithExclusions2()
            throws Exception {
        runRecurrenceIteratorTest(
                "RDATE:20060417,20060418,20070101,20060417\n"
                        + "EXDATE:20060417,20060415\n\n"
                        + "RDATE:20060422,20060417,2006\n 0412",
                new DateValueImpl(2006, 4, 13), PST, 10, new DateValueImpl(2006, 4, 18),
                /*"20060412,20060413,*/"20060418,20060422,20070101");
    }

    public void testInfiniteRecurrences() throws Exception {
        runRecurrenceIteratorTest(
                "\r\n\n \r\n"
                        // every weekday
                        + "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR\n\n"
                        // except every second thursday
                        + "EXRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=TH\n",
                new DateValueImpl(2006, 4, 11), PST, 30, null,
                "20060411,20060412,20060414,"
                        + "20060417,20060418,20060419,20060420,20060421,"
                        + "20060424,20060425,20060426,20060428,"
                        + "20060501,20060502,20060503,20060504,20060505,"
                        + "20060508,20060509,20060510,20060512,"
                        + "20060515,20060516,20060517,20060518,20060519,"
                        + "20060522,20060523,20060524,20060526,"
                        + "..."
        );
    }

    public void testInfiniteRecurrences2() throws Exception {
        runRecurrenceIteratorTest(
                "\r\n\n \r\n"
                        // every weekday
                        + "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR\n\n"
                        // except every second thursday
                        + "EXRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=TH\n",
                new DateValueImpl(2006, 4, 11), PST, 30, new DateValueImpl(2006, 4, 20),
        /*"20060411,20060412,20060414,"
        + "20060417,20060418,20060419,*/"20060420,20060421,"
                        + "20060424,20060425,20060426,20060428,"
                        + "20060501,20060502,20060503,20060504,20060505,"
                        + "20060508,20060509,20060510,20060512,"
                        + "20060515,20060516,20060517,20060518,20060519,"
                        + "20060522,20060523,20060524,20060526,"
                        + "20060529,20060530,20060531,20060601,20060602,"
                        + "20060605,"
                        + "..."
        );
    }

    public void testInfiniteExclusionsAndFiniteInclusions() throws Exception {
        runRecurrenceIteratorTest(
                "\r\n\n \r\n"
                        // every weekday
                        + "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;UNTIL=20060520\n\n"
                        // except every second thursday
                        + "EXRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=TH\n",
                new DateValueImpl(2006, 4, 11), PST, 30, null,
                "20060411,20060412,20060414,"
                        + "20060417,20060418,20060419,20060420,20060421,"
                        + "20060424,20060425,20060426,20060428,"
                        + "20060501,20060502,20060503,20060504,20060505,"
                        + "20060508,20060509,20060510,20060512,"
                        + "20060515,20060516,20060517,20060518,20060519"
        );
    }

    public void testInfiniteExclusionsAndFiniteInclusions2() throws Exception {
        runRecurrenceIteratorTest(
                "\r\n\n \r\n"
                        // every weekday
                        + "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;UNTIL=20060520\n\n"
                        // except every second thursday
                        + "EXRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=TH\n",
                new DateValueImpl(2006, 4, 11), PST, 30, new DateValueImpl(2006, 5, 4),
        /*"20060411,20060412,20060414,"
        + "20060417,20060418,20060419,20060420,20060421,"
        + "20060424,20060425,20060426,20060428,"
        + "20060501,20060502,20060503,*/"20060504,20060505,"
                        + "20060508,20060509,20060510,20060512,"
                        + "20060515,20060516,20060517,20060518,20060519"
        );
    }

    public void testIdenticalInclusionsAndExclusions() throws Exception {
        // According to rfc2445, EXDATE can be used to exclude the DTSTART time from
        // the instances of a recurring event:

        //   The "EXDATE" property can be used to exclude the value specified in
        //   "DTSTART". However, in such cases the original "DTSTART" date MUST
        //   still be maintained by the calendaring and scheduling system because
        //   the original "DTSTART" value has inherent usage dependencies by other
        //   properties such as the "RECURRENCE-ID".

        // Similar language appears in the EXRULE section.
        runRecurrenceIteratorTest(
                "RDATE:20060411,20060412,20060414,20060417,20060418,\r\n"
                        + " 20060419,20060420,20060421\r\n"
                        + "EXDATE:20060411,20060412,20060414,20060417,20060418,\r\n"
                        + " 20060419,20060420,20060421\r\n",
                new DateValueImpl(2006, 4, 11), PST, 30, null,
                ""
        );
        runRecurrenceIteratorTest(
                "RDATE:20060411,20060412,20060414,20060417,20060418,\r\n"
                        + " 20060419,20060420,20060421\r\n"
                        + "EXDATE:20060411,20060412,20060414,20060417,20060418,\r\n"
                        + " 20060419,20060420,20060421\r\n",
                new DateValueImpl(2006, 4, 11), PST, 30, new DateValueImpl(2006, 4, 14),
                ""
        );
    }

    public void testIdenticalInclusionsAndExclusionsNoDtstartPrivilege()
            throws Exception {
        RecurrenceIterator it = new CompoundIteratorImpl(
                Collections.singleton(
                        RecurrenceIteratorFactory.createRecurrenceIterator(
                                new RDateList("RDATE:20060411,20060412,20060414,20060417,"
                                        + "20060418,20060419,20060420,20060421", PST))),
                Collections.singleton(
                        RecurrenceIteratorFactory.createRecurrenceIterator(
                                new RDateList("EXDATE:20060411,20060412,20060414,20060417,"
                                        + "20060418,20060419,20060420,20060421", PST))));
        runRecurrenceIteratorTest(it, 10, null, "");
    }

    // monkey tests
    public void testMonkey1() throws Exception {
        runRecurrenceIteratorTest(
                "RRULE:FREQ=MONTHLY;INTERVAL=1;BYMONTH=9,5,3",
                new DateValueImpl(2006, 5, 3), PST, 6, null,
                "20060503,20060903,20070303,20070503,20070903,20080303,...");
    }

    public void testMonkey2() throws Exception {
        runRecurrenceIteratorTest(
                "RRULE:FREQ=YEARLY;COUNT=19;INTERVAL=1;BYMONTH=1,11,6;BYSETPOS=7,-4,6",
                new DateValueImpl(2006, 4, 22), PST, 6, null,
                "20060422");
    }

    public void testMonkey3() throws Exception {
        runRecurrenceIteratorTest(
                "RRULE:FREQ=YEARLY;WKST=SU;INTERVAL=1;BYSECOND=14",
                new DateTimeValueImpl(2006, 5, 9, 3 /* + 7 */, 45, 40), PST, 3, null,
                "20060509T104540,20070509T104514,20080509T104514,...");
    }

    public void testMonkey4() throws Exception {
        runRecurrenceIteratorTest(
                "RRULE:FREQ=MONTHLY;WKST=SU;INTERVAL=1;"
                        + "BYMONTHDAY=31,-16,23,-4,-19,-23,6,-4,-10,1,10",
                new DateValueImpl(2006, 5, 1), PST, 12, null,
                "20060501,20060506,20060509,20060510,20060513,20060516,"
                        + "20060522,20060523,20060528,20060531,20060601,20060606,...");
    }

    public void testMonkey5() throws Exception {
        // for some reason, libical goes backwards in time on this one
        runRecurrenceIteratorTest(
                "RRULE:FREQ=WEEKLY;COUNT=14;INTERVAL=1;BYMONTH=11,6,7,7,12",
                new DateTimeValueImpl(2006, 5, 2, 22, 46, 53), PST, 15, null,
                "20060503T054653,"
                        // dtstart doesn't match the BYMONTH, so it is not counted towards the
                        // count.  There are 14 instances following.
                        + "20060607T054653,20060614T054653,20060621T054653,20060628T054653,"
                        + "20060705T054653,20060712T054653,20060719T054653,20060726T054653,"
                        // the first of Novemver is NOT part of this recurrence because the
                        // day is calculated relative to the 2nd as per dtstart and then the
                        // timezone calculation pushes it first a day when translating into
                        // UTC.
                        + "20061108T064653,20061115T064653,20061122T064653,20061129T064653,"
                        + "20061206T064653,20061213T064653");
    }

    public void testMonkey6() throws Exception {
        // libical doesn't include dtstart for this one
        runRecurrenceIteratorTest(
                "RRULE:FREQ=MONTHLY;UNTIL=20060510T151044Z;INTERVAL=1;BYSECOND=48",
                new DateTimeValueImpl(2006, 4, 5, 4, 42, 26), PST, 5, null,
                // dtstart
                "20060405T114226,"
                        // bysecond rule kicks in
                        + "20060405T114248,20060505T114248");
    }

    public void testMonkey7() throws Exception {
        // a bug in the by week generator was causing us to skip Feb 2007
        runRecurrenceIteratorTest(
                "RRULE:FREQ=WEEKLY;WKST=SU;INTERVAL=1",
                new DateTimeValueImpl(2006, 5, 8, 9, 47, 41), PST, 48, null,
                "20060508T164741,20060515T164741,20060522T164741,20060529T164741,"
                        + "20060605T164741,20060612T164741,20060619T164741,20060626T164741,"
                        + "20060703T164741,20060710T164741,20060717T164741,20060724T164741,"
                        + "20060731T164741,20060807T164741,20060814T164741,20060821T164741,"
                        + "20060828T164741,20060904T164741,20060911T164741,20060918T164741,"
                        + "20060925T164741,20061002T164741,20061009T164741,20061016T164741,"
                        + "20061023T164741,20061030T174741,20061106T174741,20061113T174741,"
                        + "20061120T174741,20061127T174741,20061204T174741,20061211T174741,"
                        + "20061218T174741,20061225T174741,20070101T174741,20070108T174741,"
                        + "20070115T174741,20070122T174741,20070129T174741,20070205T174741,"
                        + "20070212T174741,20070219T174741,20070226T174741,20070305T174741,"
                        + "20070312T164741,20070319T164741,20070326T164741,20070402T164741,..."
        );
    }

    public void testMonkey8() throws Exception {
        // I don't know which side this failing on?
        runRecurrenceIteratorTest(
                "RRULE:FREQ=WEEKLY;COUNT=18;BYMONTH=7,11",
                new DateValueImpl(2006, 4, 27), PST, 20, null,
                // doesn't count towards count since not in july or november
                "20060427,"
                        // all the Thursdays in July and November
                        + "20060706,20060713,20060720,20060727,20061102,20061109,"
                        + "20061116,20061123,20061130,20070705,20070712,20070719,"
                        + "20070726,20071101,20071108,20071115,20071122,20071129"
        );
    }

    public void testMonkey9() throws Exception {
        // another libical crasher
        runRecurrenceIteratorTest(
                "RRULE:FREQ=MONTHLY;WKST=SU;INTERVAL=1;BYWEEKNO=5,-5;BYSECOND=0",
                new DateValueImpl(2006, 4, 27), PST, 5, null,
                // all the 27ths of the month.  BYWEEKNO ignores since not yearly
                "20060427,20060527,20060627,20060727,20060827,..."
        );
    }

    public void testMonkey10() throws Exception {
        // another libical hanger
        runRecurrenceIteratorTest(
                "RRULE:FREQ=YEARLY;BYYEARDAY=1;BYMONTHDAY=1",
                new DateValueImpl(2006, 4, 27), PST, 5, null,
                "20060427,20070101,20080101,20090101,20100101,..."
        );
        runRecurrenceIteratorTest(
                "RRULE:FREQ=YEARLY;BYYEARDAY=1;BYMONTHDAY=2",
                new DateValueImpl(2006, 4, 27), PST, 5, null,
                "20060427"
        );
    }

    public void testMonkey11() throws Exception {
        // Days of the month in December
        // 8, 10, 9, 6, 4, 14, 22, 23, 18, 24, 4, 24, 18
        // Unique
        // 4, *6, 8, 9, 10, 14, *18, *22, 23, *24
        runRecurrenceIteratorTest(
                "RRULE:FREQ=YEARLY;INTERVAL=1"
                        + ";BYMONTHDAY=8,-22,9,-26,-28,14,-10,-9,-14,-8,4,24,-14"
                        + ";BYSETPOS=-1,-3,-4,-9",
                new DateValueImpl(2006, 4, 9), PST, 5, null,
                "20060409,20061206,20061218,20061222,20061224,..."
        );
    }

    public void testMonkey11WithAdvanceTo() throws Exception {
        runRecurrenceIteratorTest(
                "RRULE:FREQ=YEARLY;INTERVAL=1"
                        + ";BYMONTHDAY=8,-22,9,-26,-28,14,-10,-9,-14,-8,4,24,-14"
                        + ";BYSETPOS=-1,-3,-4,-9",
                new DateValueImpl(2006, 4, 9), PST, 5, new DateValueImpl(2006, 12, 20),
                /*"20060409,20061206,20061218,*/"20061222,20061224,"
                        + "20071206,20071218,20071222,..."
        );
    }

    public void testMonkey12() throws Exception {
        runRecurrenceIteratorTest( // 4, 5, 13, 29, 31
                "RRULE:FREQ=DAILY;INTERVAL=1;BYMONTHDAY=5,29,31,-19,-28",
                new DateTimeValueImpl(2006, 5, 2, 18, 47, 45), PST, 6, null,
                "20060503T014745,20060505T014745,20060506T014745,"
                        + "20060514T014745,20060530T014745,20060601T014745,..."
        );
    }

    public void testMonkey13() throws Exception {
        runRecurrenceIteratorTest( // 4, 5, 13, 29, 31
                "RRULE:FREQ=DAILY;WKST=SU;COUNT=4;INTERVAL=1;BYMONTHDAY=20,-20",
                new DateValueImpl(2006, 5, 19), PST, 5, null,
                "20060519,20060520,20060611,20060620,20060712"
        );
    }

    public void testMonkey14() throws Exception {
        runRecurrenceIteratorTest( // 4, 5, 13, 29, 31
                "RRULE:FREQ=YEARLY;BYDAY=TH",
                new DateValueImpl(2006, 5, 6), PST, 4, null,
                "20060506,20060511,20060518,20060525,..."
        );
    }

    public void testExcludedStart() throws Exception {
        runRecurrenceIteratorTest(
                "RRULE:FREQ=YEARLY;UNTIL=20070414;INTERVAL=1;BYDAY=3SU;BYMONTH=4\n"
                        + "\n"
                        + "EXDATE;VALUE=DATE:20060416\n"
                        + "\n"
                        + "\n",
                new DateValueImpl(2006, 4, 16), PST, 4, null,
                ""
        );
    }

    public void testMonkeySeptember1() throws Exception {
        // From the Monkey Tester
        // RANDOM SEED 1156837020593
        // RRULE:FREQ=DAILY;WKST=SU;INTERVAL=1;BYMINUTE=60 / 2006-09-20 23:15:51

        // last=2006-09-21 07:00:51, current=2006-09-21 07:00:51

        runRecurrenceIteratorTest(
                "RRULE:FREQ=DAILY;WKST=SU;INTERVAL=1;BYMINUTE=59",
                new DateTimeValueImpl(2006, 9, 20, 23, 15, 51), PST, 4, null,
                "20060921T061551,20060921T065951,20060922T065951,20060923T065951,..."
        );
        // we can't actually create an RRULE with BYMINUTE=60 since that's out of
        // range
        // TODO(msamuel): write me
    }


    // reimplement assertEquals so that it doesn't suck.  sign...
    public static void assertEquals(String a, String b) {
        assertEquals(null, a, b);
    }

    public static void assertEquals(String message, String a, String b) {
        if (null != a ? a.equals(b) : null == b) {
            return;
        }

        if (a != null && b != null) {
            int prefixLen = commonPrefix(a, b, 0);
            int suffixLen = commonSuffix(a, b, 0);
            suffixLen = Math.min(Math.min(a.length(), b.length())
                    - prefixLen, suffixLen);
            int suffixPos = b.length() - suffixLen;
            System.err.println("Actual=<<<\n" + b.substring(0, prefixLen)
                    + "  ==>" + b.substring(prefixLen, suffixPos)
                    + "<==  " + b.substring(suffixPos)
                    + "\n>>>");
        }
        Assert.assertEquals(message, a, b);
    }

    private static int commonPrefix(CharSequence a, CharSequence b, int i) {
        int n = Math.min(a.length(), b.length());
        while (i < n && a.charAt(i) == b.charAt(i)) {
            ++i;
        }
        return i;
    }

    private static int commonSuffix(CharSequence a, CharSequence b, int i) {
        int m = a.length() - i - 1, n = b.length() - i - 1;
        while ((m | n) > 0 && a.charAt(m) == b.charAt(n)) {
            --m;
            --n;
        }
        return a.length() - i - m - 1;
    }
}
