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

/**
 * represents a day of the week in a month or year such as the
 * third monday of the month.  A negative num indicates it counts from the
 * end of the month or year.
 *
 * <blockquote>
 * Each BYDAY value can also be preceded by a positive (+n) or negative
 * (-n) integer. If present, this indicates the nth occurrence of the
 * specific day within the MONTHLY or YEARLY RRULE. For example, within
 * a MONTHLY rule, +1MO (or simply 1MO) represents the first Monday
 * within the month, whereas -1MO represents the last Monday of the
 * month. If an integer modifier is not present, it means all days of
 * this type within the specified frequency. For example, within a
 * MONTHLY rule, MO represents all Mondays within the month.
 * </blockquote>
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class WeekdayNum {
    public final int num;
    public final Weekday wday;

    /**
     * @param num in -53,53
     * @param wday non null.
     */
    public WeekdayNum(int num, Weekday wday) {
        if (!(-53 <= num && 53 >= num && null != wday)) {
            throw new IllegalArgumentException();
        }
        this.num = num;
        this.wday = wday;
    }

    public String toIcal() {
        return (0 != this.num)
                ? String.valueOf(this.num) + this.wday
                : this.wday.toString();
    }

    @Override
    public String toString() {
        return toIcal();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WeekdayNum)) {
            return false;
        }
        WeekdayNum wdn = (WeekdayNum) o;
        return this.num == wdn.num && this.wday == wdn.wday;
    }

    @Override
    public int hashCode() {
        return num ^ (53 * wday.hashCode());
    }
}
