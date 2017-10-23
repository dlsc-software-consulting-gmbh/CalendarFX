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

package com.google.ical.util;

import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.TimeValue;

/**
 * a mutable buffer that can be used to build {@link DateValue}s and
 * {@link DateTimeValue}s.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class DTBuilder {

    /** in AD.  0 -> 1BC. */
    public int year;
    /** one indexed. */
    public int month;
    /** one indexed */
    public int day;
    /** zero indexed in 24 hour time. */
    public int hour;
    /** zero indexed */
    public int minute;
    /** zero indexed */
    public int second;

    public DTBuilder(int year, int month, int day,
                     int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public DTBuilder(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public DTBuilder(DateValue dv) {
        this.year = dv.year();
        this.month = dv.month();
        this.day = dv.day();
        if (dv instanceof TimeValue) {
            TimeValue tv = (TimeValue) dv;
            this.hour = tv.hour();
            this.minute = tv.minute();
            this.second = tv.second();
        }
    }

    /**
     * produces a normalized date time, using zero for the time fields if none
     * were provided.
     * @return not null
     */
    public DateTimeValue toDateTime() {
        normalize();
        return new DateTimeValueImpl(year, month, day, hour, minute, second);
    }

    /**
     * produces a normalized date.
     * @return not null
     */
    public DateValue toDate() {
        normalize();
        return new DateValueImpl(year, month, day);
    }

    /**
     * behavior undefined unless normalized.
     * If you're not sure whether it's appropriate to use this method, use
     * <code>toDateValue().compareTo(dv)</code> instead.
     */
    public int compareTo(DateValue dv) {
        long dvComparable =
                (((((long) dv.year()) << 4) + dv.month()) << 5) + dv.day();
        long dtbComparable =
                ((((long) year << 4) + month << 5)) + day;
        if (dv instanceof TimeValue) {
            TimeValue tv = (TimeValue) dv;
            dvComparable = (((((dvComparable << 5) + tv.hour()) << 6) + tv.minute())
                    << 6) + tv.second();
            dtbComparable = (((((dtbComparable << 5) + hour) << 6) + minute)
                    << 6) + second;
        }
        long delta = dtbComparable - dvComparable;
        return delta < 0 ? -1 : delta == 0 ? 0 : 1;
    }

    /**
     * makes sure that the fields are in the proper ranges, by e.g. converting
     * 32 January to 1 February, or month 0 to December of the year before.
     */
    public void normalize() {
        this.normalizeTime();
        this.normalizeDate();
    }

    @Override
    public String toString() {
        return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":"
                + second;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DTBuilder)) {
            return false;
        }
        DTBuilder that = (DTBuilder) o;
        return this.year == that.year
                && this.month == that.month
                && this.day == that.day
                && this.hour == that.hour
                && this.minute == that.minute
                && this.second == that.second;
    }

    @Override
    public int hashCode() {
        return
                ((((((((year << 4) + month << 5) + day) << 5) + hour) << 6) + minute)
                        << 6) + second;
    }

    private void normalizeTime() {
        int addMinutes = ((second < 0) ? (second - 59) : second) / 60;
        second -= addMinutes * 60;
        minute += addMinutes;
        int addHours = ((minute < 0) ? (minute - 59) : minute) / 60;
        minute -= addHours * 60;
        hour += addHours;
        int addDays = ((hour < 0) ? (hour - 23) : hour) / 24;
        hour -= addDays * 24;
        day += addDays;
    }

    private void normalizeDate() {
        while (day <= 0) {
            int days = TimeUtils.yearLength(month > 2 ? year : year - 1);
            day += days;
            --year;
        }
        if (month <= 0) {
            int years = month / 12 - 1;
            year += years;
            month -= 12 * years;
        } else if (month > 12) {
            int years = (month - 1) / 12;
            year += years;
            month -= 12 * years;
        }
        while (true) {
            if (month == 1) {
                int yearLength = TimeUtils.yearLength(year);
                if (day > yearLength) {
                    ++year;
                    day -= yearLength;
                }
            }
            int monthLength = TimeUtils.monthLength(year, month);
            if (day > monthLength) {
                day -= monthLength;
                if (++month > 12) {
                    month -= 12;
                    ++year;
                }
            } else {
                break;
            }
        }
    }

}
