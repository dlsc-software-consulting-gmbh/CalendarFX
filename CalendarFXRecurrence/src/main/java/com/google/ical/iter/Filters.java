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
import com.google.ical.util.Predicate;
import com.google.ical.util.Predicates;
import com.google.ical.util.TimeUtils;
import com.google.ical.values.DateValue;
import com.google.ical.values.TimeValue;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;


/**
 * predicates used to filter out dates produced by a generator that do not
 * pass some secondary criterion.  For example, the recurrence rule
 * <tt>FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13</tt> should generate every friday the
 * 13th.  It is implemented as a generator that generates the 13th of every
 * month -- a byMonthDay generator, and then the results of that are filtered
 * by a byDayFilter that tests whether the date falls on Friday.
 *
 * <p>A filter returns true to indicate the item is included in the
 * recurrence.</p>
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
class Filters {

    /**
     * constructs a day filter based on a BYDAY rule.
     * @param days non null
     * @param weeksInYear are the week numbers meant to be weeks in the
     *   current year, or weeks in the current month.
     */
    static Predicate<DateValue> byDayFilter(
            final WeekdayNum[] days, final boolean weeksInYear, final Weekday wkst) {
        return new Predicate<DateValue>() {
            public boolean apply(DateValue date) {
                Weekday dow = Weekday.valueOf(date);

                int nDays;
                // first day of the week in the given year or month
                Weekday dow0;
                // where does date appear in the year or month?
                // in [0, lengthOfMonthOrYear - 1]
                int instance;
                if (weeksInYear) {
                    nDays = TimeUtils.yearLength(date.year());
                    dow0 = Weekday.firstDayOfWeekInMonth(date.year(), 1);
                    instance = TimeUtils.dayOfYear(
                            date.year(), date.month(), date.day());
                } else {
                    nDays = TimeUtils.monthLength(date.year(), date.month());
                    dow0 = Weekday.firstDayOfWeekInMonth(date.year(), date.month());
                    instance = date.day() - 1;
                }

                // which week of the year or month does this date fall on?
                // one-indexed
                int dateWeekNo;
                if (wkst.javaDayNum <= dow.javaDayNum) {
                    dateWeekNo = 1 + (instance / 7);
                } else {
                    dateWeekNo = (instance / 7);
                }

                // TODO(msamuel): according to section 4.3.10
                //     Week number one of the calendar year is the first week which
                //     contains at least four (4) days in that calendar year. This
                //     rule part is only valid for YEARLY rules.
                // That's mentioned under the BYWEEKNO rule, and there's no mention
                // of it in the earlier discussion of the BYDAY rule.
                // Does it apply to yearly week numbers calculated for BYDAY rules in
                // a FREQ=YEARLY rule?

                for (int i = days.length; --i >= 0; ) {
                    WeekdayNum day = days[i];

                    if (day.wday == dow) {
                        int weekNo = day.num;
                        if (0 == weekNo) {
                            return true;
                        }

                        if (weekNo < 0) {
                            weekNo = Util.invertWeekdayNum(day, dow0, nDays);
                        }

                        if (dateWeekNo == weekNo) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    /**
     * constructs a day filter based on a BYDAY rule.
     * @param monthDays days of the month in [-31, 31] != 0
     */
    static Predicate<DateValue> byMonthDayFilter(final int[] monthDays) {
        return new Predicate<DateValue>() {
            public boolean apply(DateValue date) {
                int nDays = TimeUtils.monthLength(date.year(), date.month());
                for (int i = monthDays.length; --i >= 0; ) {
                    int day = monthDays[i];
                    if (day < 0) {
                        day += nDays + 1;
                    }
                    if (day == date.day()) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * constructs a filter that accepts only every interval-th week from the week
     * containing dtStart.
     * @param interval > 0 number of weeks
     * @param wkst day of the week that the week starts on.
     * @param dtStart non null
     */
    static Predicate<DateValue> weekIntervalFilter(
            final int interval, final Weekday wkst, final DateValue dtStart) {
        return new Predicate<DateValue>() {
            DateValue wkStart;

            {
                // the latest day with day of week wkst on or before dtStart
                DTBuilder wkStartB = new DTBuilder(dtStart);
                wkStartB.day -=
                        (7 + Weekday.valueOf(dtStart).javaDayNum - wkst.javaDayNum) % 7;
                wkStart = wkStartB.toDate();
            }

            public boolean apply(DateValue date) {
                int daysBetween = TimeUtils.daysBetween(date, wkStart);
                if (daysBetween < 0) {
                    // date must be before dtStart.  Shouldn't occur in practice.
                    daysBetween += (interval * 7 * (1 + daysBetween / (-7 * interval)));
                }
                int off = (daysBetween / 7) % interval;
                return 0 == off;
            }
        };
    }

    private static final int LOW_24_BITS = ~(-1 << 24);
    private static final long LOW_60_BITS = ~(-1L << 60);

    /**
     * constructs an hour filter based on a BYHOUR rule.
     * @param hours hours of the day in [0, 23]
     */
    static Predicate<DateValue> byHourFilter(int[] hours) {
        int hoursByBit = 0;
        for (int hour : hours) {
            hoursByBit |= 1 << hour;
        }
        if ((hoursByBit & LOW_24_BITS) == LOW_24_BITS) {
            return Predicates.alwaysTrue();
        }
        final int bitField = hoursByBit;
        return new Predicate<DateValue>() {
            public boolean apply(DateValue date) {
                if (!(date instanceof TimeValue)) {
                    return false;
                }
                TimeValue tv = (TimeValue) date;
                return (bitField & (1 << tv.hour())) != 0;
            }
        };
    }

    /**
     * constructs a minute filter based on a BYMINUTE rule.
     * @param minutes minutes of the hour in [0, 59]
     */
    static Predicate<DateValue> byMinuteFilter(int[] minutes) {
        long minutesByBit = 0;
        for (int minute : minutes) {
            minutesByBit |= 1L << minute;
        }
        if ((minutesByBit & LOW_60_BITS) == LOW_60_BITS) {
            return Predicates.alwaysTrue();
        }
        final long bitField = minutesByBit;
        return new Predicate<DateValue>() {
            public boolean apply(DateValue date) {
                if (!(date instanceof TimeValue)) {
                    return false;
                }
                TimeValue tv = (TimeValue) date;
                return (bitField & (1L << tv.minute())) != 0;
            }
        };
    }


    /**
     * constructs a second filter based on a BYMINUTE rule.
     * @param seconds seconds of the minute in [0, 59]
     */
    static Predicate<DateValue> bySecondFilter(int[] seconds) {
        long secondsByBit = 0;
        for (int second : seconds) {
            secondsByBit |= 1L << second;
        }
        if ((secondsByBit & LOW_60_BITS) == LOW_60_BITS) {
            return Predicates.alwaysTrue();
        }
        final long bitField = secondsByBit;
        return new Predicate<DateValue>() {
            public boolean apply(DateValue date) {
                if (!(date instanceof TimeValue)) {
                    return false;
                }
                TimeValue tv = (TimeValue) date;
                return (bitField & (1L << tv.second())) != 0;
            }
        };
    }

    private Filters() {
        // uninstantiable
    }

}
