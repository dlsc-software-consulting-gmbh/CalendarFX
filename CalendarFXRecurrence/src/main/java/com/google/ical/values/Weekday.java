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

import com.google.ical.util.TimeUtils;

/**
 * days of the week enum.  Names correspond to RFC2445 literals.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public enum Weekday {
    SU(0),
    MO(1),
    TU(2),
    WE(3),
    TH(4),
    FR(5),
    SA(6),;

    /**
     * agrees with values returned by the javascript builtin Date.getDay and the
     * corresponding ical.js function/method, but is one less than the Java
     * Calendar's <code>DAY_OF_WEEK</code> values.
     */
    public final int jsDayNum;
    /**
     * agrees with the java weekday values as found in java.util.Calendar.
     */
    public final int javaDayNum;

    Weekday(int wDayNum) {
        this.jsDayNum = wDayNum;
        this.javaDayNum = 1 + wDayNum;
    }

    private static Weekday[] VALUES = new Weekday[7];

    static {
        System.arraycopy(values(), 0, VALUES, 0, 7);
    }

    public static Weekday valueOf(DateValue dv) {
        int dayIndex =
                TimeUtils.fixedFromGregorian(dv.year(), dv.month(), dv.day()) % 7;
        if (dayIndex < 0) {
            dayIndex += 7;
        }
        return VALUES[dayIndex];
    }

    public static Weekday firstDayOfWeekInMonth(int year, int month) {
        int result = TimeUtils.fixedFromGregorian(year, month, 1) % 7;
        return VALUES[(result >= 0) ? result : result + 7];
    }

    public Weekday successor() {
        return VALUES[(ordinal() + 1) % 7];
    }

    public Weekday predecessor() {
        return VALUES[(ordinal() - 1 + 7) % 7];
    }

}
