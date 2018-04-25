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

import com.google.ical.util.DTBuilder;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;
import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class UtilTest extends TestCase {

    public void testDayNumToDateInMonth() {
        //        March 2006
        // Su Mo Tu We Th Fr Sa
        //           1  2  3  4
        //  5  6  7  8  9 10 11
        // 12 13 14 15 16 17 18
        // 19 20 21 22 23 24 25
        // 26 27 28 29 30 31
        Weekday dow0 = Weekday.WE;
        int nDays = 31;
        int d0 = 0;

        assertEquals(1, Util.dayNumToDate(dow0, nDays, 1, Weekday.WE, d0, nDays));
        assertEquals(8, Util.dayNumToDate(dow0, nDays, 2, Weekday.WE, d0, nDays));
        assertEquals(29, Util.dayNumToDate(dow0, nDays, -1, Weekday.WE, d0, nDays));
        assertEquals(22, Util.dayNumToDate(dow0, nDays, -2, Weekday.WE, d0, nDays));

        assertEquals(3, Util.dayNumToDate(dow0, nDays, 1, Weekday.FR, d0, nDays));
        assertEquals(10, Util.dayNumToDate(dow0, nDays, 2, Weekday.FR, d0, nDays));
        assertEquals(31, Util.dayNumToDate(dow0, nDays, -1, Weekday.FR, d0, nDays));
        assertEquals(24, Util.dayNumToDate(dow0, nDays, -2, Weekday.FR, d0, nDays));

        assertEquals(7, Util.dayNumToDate(dow0, nDays, 1, Weekday.TU, d0, nDays));
        assertEquals(14, Util.dayNumToDate(dow0, nDays, 2, Weekday.TU, d0, nDays));
        assertEquals(28, Util.dayNumToDate(dow0, nDays, 4, Weekday.TU, d0, nDays));
        assertEquals(0, Util.dayNumToDate(dow0, nDays, 5, Weekday.TU, d0, nDays));
        assertEquals(28, Util.dayNumToDate(dow0, nDays, -1, Weekday.TU, d0, nDays));
        assertEquals(21, Util.dayNumToDate(dow0, nDays, -2, Weekday.TU, d0, nDays));
        assertEquals(7, Util.dayNumToDate(dow0, nDays, -4, Weekday.TU, d0, nDays));
        assertEquals(0, Util.dayNumToDate(dow0, nDays, -5, Weekday.TU, d0, nDays));
    }

    public void testDayNumToDateInYear() {
        //        January 2006
        //  # Su Mo Tu We Th Fr Sa
        //  1  1  2  3  4  5  6  7
        //  2  8  9 10 11 12 13 14
        //  3 15 16 17 18 19 20 21
        //  4 22 23 24 25 26 27 28
        //  5 29 30 31

        //      February 2006
        //  # Su Mo Tu We Th Fr Sa
        //  5           1  2  3  4
        //  6  5  6  7  8  9 10 11
        //  7 12 13 14 15 16 17 18
        //  8 19 20 21 22 23 24 25
        //  9 26 27 28

        //           March 2006
        //  # Su Mo Tu We Th Fr Sa
        //  9           1  2  3  4
        // 10  5  6  7  8  9 10 11
        // 11 12 13 14 15 16 17 18
        // 12 19 20 21 22 23 24 25
        // 13 26 27 28 29 30 31

        Weekday dow0 = Weekday.SU;
        int nInMonth = 31;
        int nDays = 365;
        int d0 = 59;

        // TODO(msamuel): check that these answers are right
        assertEquals(
                1, Util.dayNumToDate(dow0, nDays, 9, Weekday.WE, d0, nInMonth));
        assertEquals(
                8, Util.dayNumToDate(dow0, nDays, 10, Weekday.WE, d0, nInMonth));
        assertEquals(
                29, Util.dayNumToDate(dow0, nDays, -40, Weekday.WE, d0, nInMonth));
        assertEquals(
                22, Util.dayNumToDate(dow0, nDays, -41, Weekday.WE, d0, nInMonth));

        assertEquals(
                3, Util.dayNumToDate(dow0, nDays, 9, Weekday.FR, d0, nInMonth));
        assertEquals(
                10, Util.dayNumToDate(dow0, nDays, 10, Weekday.FR, d0, nInMonth));
        assertEquals(
                31, Util.dayNumToDate(dow0, nDays, -40, Weekday.FR, d0, nInMonth));
        assertEquals(
                24, Util.dayNumToDate(dow0, nDays, -41, Weekday.FR, d0, nInMonth));

        assertEquals(
                7, Util.dayNumToDate(dow0, nDays, 10, Weekday.TU, d0, nInMonth));
        assertEquals(
                14, Util.dayNumToDate(dow0, nDays, 11, Weekday.TU, d0, nInMonth));
        assertEquals(
                28, Util.dayNumToDate(dow0, nDays, 13, Weekday.TU, d0, nInMonth));
        assertEquals(
                0, Util.dayNumToDate(dow0, nDays, 14, Weekday.TU, d0, nInMonth));
        assertEquals(
                28, Util.dayNumToDate(dow0, nDays, -40, Weekday.TU, d0, nInMonth));
        assertEquals(
                21, Util.dayNumToDate(dow0, nDays, -41, Weekday.TU, d0, nInMonth));
        assertEquals(
                7, Util.dayNumToDate(dow0, nDays, -43, Weekday.TU, d0, nInMonth));
        assertEquals(
                0, Util.dayNumToDate(dow0, nDays, -44, Weekday.TU, d0, nInMonth));
    }

    public void testUniquify() {
        int[] ints = new int[]{1, 4, 4, 2, 7, 3, 8, 0, 0, 3};
        ints = Util.uniquify(ints);
        assertEquals("0,1,2,3,4,7,8", arrToString(ints));
    }

    public void testRollToNextWeekStart() {
        DTBuilder builder;

        builder = new DTBuilder(2006, 1, 23);
        Util.rollToNextWeekStart(builder, Weekday.TU);
        assertEquals("20060124", builder.toDate().toString());

        builder = new DTBuilder(2006, 1, 24);
        Util.rollToNextWeekStart(builder, Weekday.TU);
        assertEquals("20060124", builder.toDate().toString());

        builder = new DTBuilder(2006, 1, 25);
        Util.rollToNextWeekStart(builder, Weekday.TU);
        assertEquals("20060131", builder.toDate().toString());

        builder = new DTBuilder(2006, 1, 23);
        Util.rollToNextWeekStart(builder, Weekday.MO);
        assertEquals("20060123", builder.toDate().toString());

        builder = new DTBuilder(2006, 1, 24);
        Util.rollToNextWeekStart(builder, Weekday.MO);
        assertEquals("20060130", builder.toDate().toString());

        builder = new DTBuilder(2006, 1, 25);
        Util.rollToNextWeekStart(builder, Weekday.MO);
        assertEquals("20060130", builder.toDate().toString());

        builder = new DTBuilder(2006, 1, 31);
        Util.rollToNextWeekStart(builder, Weekday.MO);
        assertEquals("20060206", builder.toDate().toString());
    }

    public void testNextWeekStart() {
        assertEquals(new DateValueImpl(2006, 1, 24),
                Util.nextWeekStart(new DateValueImpl(2006, 1, 23),
                        Weekday.TU));

        assertEquals(new DateValueImpl(2006, 1, 24),
                Util.nextWeekStart(new DateValueImpl(2006, 1, 24),
                        Weekday.TU));

        assertEquals(new DateValueImpl(2006, 1, 31),
                Util.nextWeekStart(new DateValueImpl(2006, 1, 25),
                        Weekday.TU));

        assertEquals(new DateValueImpl(2006, 1, 23),
                Util.nextWeekStart(new DateValueImpl(2006, 1, 23),
                        Weekday.MO));

        assertEquals(new DateValueImpl(2006, 1, 30),
                Util.nextWeekStart(new DateValueImpl(2006, 1, 24),
                        Weekday.MO));

        assertEquals(new DateValueImpl(2006, 1, 30),
                Util.nextWeekStart(new DateValueImpl(2006, 1, 25),
                        Weekday.MO));

        assertEquals(new DateValueImpl(2006, 2, 6),
                Util.nextWeekStart(new DateValueImpl(2006, 1, 31),
                        Weekday.MO));
    }

    public void testCountInPeriod() {
        //        January 2006
        //  Su Mo Tu We Th Fr Sa
        //   1  2  3  4  5  6  7
        //   8  9 10 11 12 13 14
        //  15 16 17 18 19 20 21
        //  22 23 24 25 26 27 28
        //  29 30 31
        assertEquals(5, Util.countInPeriod(Weekday.SU, Weekday.SU, 31));
        assertEquals(5, Util.countInPeriod(Weekday.MO, Weekday.SU, 31));
        assertEquals(5, Util.countInPeriod(Weekday.TU, Weekday.SU, 31));
        assertEquals(4, Util.countInPeriod(Weekday.WE, Weekday.SU, 31));
        assertEquals(4, Util.countInPeriod(Weekday.TH, Weekday.SU, 31));
        assertEquals(4, Util.countInPeriod(Weekday.FR, Weekday.SU, 31));
        assertEquals(4, Util.countInPeriod(Weekday.SA, Weekday.SU, 31));

        //      February 2006
        //  Su Mo Tu We Th Fr Sa
        //            1  2  3  4
        //   5  6  7  8  9 10 11
        //  12 13 14 15 16 17 18
        //  19 20 21 22 23 24 25
        //  26 27 28
        assertEquals(4, Util.countInPeriod(Weekday.SU, Weekday.WE, 28));
        assertEquals(4, Util.countInPeriod(Weekday.MO, Weekday.WE, 28));
        assertEquals(4, Util.countInPeriod(Weekday.TU, Weekday.WE, 28));
        assertEquals(4, Util.countInPeriod(Weekday.WE, Weekday.WE, 28));
        assertEquals(4, Util.countInPeriod(Weekday.TH, Weekday.WE, 28));
        assertEquals(4, Util.countInPeriod(Weekday.FR, Weekday.WE, 28));
        assertEquals(4, Util.countInPeriod(Weekday.SA, Weekday.WE, 28));
    }

    public void testInvertWeekdayNum() {

        //        January 2006
        //  # Su Mo Tu We Th Fr Sa
        //  1  1  2  3  4  5  6  7
        //  2  8  9 10 11 12 13 14
        //  3 15 16 17 18 19 20 21
        //  4 22 23 24 25 26 27 28
        //  5 29 30 31

        // the 1st falls on a sunday, so dow0 == SU
        assertEquals(
                5,
                Util.invertWeekdayNum(new WeekdayNum(-1, Weekday.SU), Weekday.SU, 31));
        assertEquals(
                5,
                Util.invertWeekdayNum(new WeekdayNum(-1, Weekday.MO), Weekday.SU, 31));
        assertEquals(
                5,
                Util.invertWeekdayNum(new WeekdayNum(-1, Weekday.TU), Weekday.SU, 31));
        assertEquals(
                4,
                Util.invertWeekdayNum(new WeekdayNum(-1, Weekday.WE), Weekday.SU, 31));
        assertEquals(
                3,
                Util.invertWeekdayNum(new WeekdayNum(-2, Weekday.WE), Weekday.SU, 31));


        //      February 2006
        //  # Su Mo Tu We Th Fr Sa
        //  1           1  2  3  4
        //  2  5  6  7  8  9 10 11
        //  3 12 13 14 15 16 17 18
        //  4 19 20 21 22 23 24 25
        //  5 26 27 28

        assertEquals(
                4,
                Util.invertWeekdayNum(new WeekdayNum(-1, Weekday.SU), Weekday.WE, 28));
        assertEquals(
                4,
                Util.invertWeekdayNum(new WeekdayNum(-1, Weekday.MO), Weekday.WE, 28));
        assertEquals(
                4,
                Util.invertWeekdayNum(new WeekdayNum(-1, Weekday.TU), Weekday.WE, 28));
        assertEquals(
                4,
                Util.invertWeekdayNum(new WeekdayNum(-1, Weekday.WE), Weekday.WE, 28));
        assertEquals(
                3,
                Util.invertWeekdayNum(new WeekdayNum(-2, Weekday.WE), Weekday.WE, 28));
    }

    private static String arrToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; ++i) {
            if (0 != i) {
                sb.append(',');
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}

// For dow0 == MO
//        January 2006
//  # Mo Tu We Th Fr Sa Su
//  1                    1
//  2  2  3  4  5  6  7  8
//  3  9 10 11 12 13 14 15
//  4 16 17 18 19 20 21 22
//  5 23 24 25 26 27 28 29
//  6 30 31
