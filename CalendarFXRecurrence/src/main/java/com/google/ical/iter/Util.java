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
import com.google.ical.values.DateValue;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;

/**
 * a dumping ground for utility functions that don't fit anywhere else.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
class Util {

    /**
     * advances builder to the earliest day on or after builder that falls on
     * wkst.
     * @param builder non null.
     * @param wkst the day of the week that the week starts on
     */
    static void rollToNextWeekStart(DTBuilder builder, Weekday wkst) {
        DateValue bd = builder.toDate();
        builder.day += (7 - ((7 + (Weekday.valueOf(bd).javaDayNum
                - wkst.javaDayNum))
                % 7)) % 7;
        builder.normalize();
    }

    /**
     * the earliest day on or after d that falls on wkst.
     * @param wkst the day of the week that the week starts on
     */
    static DateValue nextWeekStart(DateValue d, Weekday wkst) {
        DTBuilder builder = new DTBuilder(d);
        builder.day += (7 - ((7 + (Weekday.valueOf(d).javaDayNum
                - wkst.javaDayNum)) % 7))
                % 7;
        return builder.toDate();
    }

    /** returns a sorted unique copy of ints. */
    static int[] uniquify(int[] ints) {
        return uniquify(ints, 0, ints.length);
    }

    /** returns a sorted unique copy of ints. */
    static int[] uniquify(int[] ints, int start, int end) {
        IntSet iset = new IntSet();
        for (int i = end; --i >= start; ) {
            iset.add(ints[i]);
        }
        return iset.toIntArray();
    }

    /**
     * given a weekday number, such as -1SU, returns the day of the month that it
     * falls on.
     * The weekday number may be refer to a week in the current month in some
     * contexts or a week in the current year in other contexts.
     * @param dow0 the day of week of the first day in the current year/month.
     * @param nDays the number of days in the current year/month.
     *   In [28,29,30,31,365,366].
     * @param weekNum -1SU in the example above.
     * @param d0 the number of days between the 1st day of the current
     *   year/month and the current month.
     * @param nDaysInMonth the number of days in the current month.
     * @return 0 indicates no such day
     */
    static int dayNumToDate(Weekday dow0, int nDays, int weekNum,
                            Weekday dow, int d0, int nDaysInMonth) {
        // if dow is wednesday, then this is the date of the first wednesday
        int firstDateOfGivenDow = 1 + ((7 + dow.javaDayNum - dow0.javaDayNum) % 7);

        int date;
        if (weekNum > 0) {
            date = ((weekNum - 1) * 7) + firstDateOfGivenDow - d0;
        } else {  // count weeks from end of month
            // calculate last day of the given dow.
            // Since nDays <= 366, this should be > nDays
            int lastDateOfGivenDow = firstDateOfGivenDow + (7 * 54);
            lastDateOfGivenDow -= 7 * ((lastDateOfGivenDow - nDays + 6) / 7);
            date = lastDateOfGivenDow + 7 * (weekNum + 1) - d0;
        }
        if (date <= 0 || date > nDaysInMonth) {
            return 0;
        }
        return date;
    }

    /**
     * Compute an absolute week number given a relative one.
     * The day number -1SU refers to the last Sunday, so if there are 5 Sundays
     * in a period that starts on dow0 with nDays, then -1SU is 5SU.
     * Depending on where its used it may refer to the last Sunday of the year
     * or of the month.
     *
     * @param weekdayNum -1SU in the example above.
     * @param dow0 the day of the week of the first day of the week or month.
     *   One of the RRULE_WDAY_* constants.
     * @param nDays the number of days in the month or year.
     * @return an abolute week number, e.g. 5 in the example above.
     *   Valid if in [1,53].
     */
    static int invertWeekdayNum(
            WeekdayNum weekdayNum, Weekday dow0, int nDays) {
        assert weekdayNum.num < 0;
        // how many are there of that week?
        return countInPeriod(weekdayNum.wday, dow0, nDays) + weekdayNum.num + 1;
    }

    /**
     * the number of occurences of dow in a period nDays long where the first day
     * of the period has day of week dow0.
     */
    static int countInPeriod(Weekday dow, Weekday dow0, int nDays) {
        // Two cases
        //    (1a) dow >= dow0: count === (nDays - (dow - dow0)) / 7
        //    (1b) dow < dow0:  count === (nDays - (7 - dow0 - dow)) / 7
        if (dow.javaDayNum >= dow0.javaDayNum) {
            return 1 + ((nDays - (dow.javaDayNum - dow0.javaDayNum) - 1) / 7);
        } else {
            return 1 + ((nDays - (7 - (dow0.javaDayNum - dow.javaDayNum)) - 1) / 7);
        }
    }

    private Util() {
        // uninstantiable
    }

}
