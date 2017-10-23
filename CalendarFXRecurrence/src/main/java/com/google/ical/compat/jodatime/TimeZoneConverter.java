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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replacement for Joda-time's broken {@link DateTimeZone#toTimeZone} which
 * returns a <code>java.util.TimeZone</code> that supposedly is equivalent to
 * the <code>DateTimeZone</code>.
 * Joda time's implementation simply uses the ID to look up the corresponding
 * <code>java.util.TimeZone</code>s which should not be used since they're
 * frequently out-of-date re Brazilian timezones.
 *
 * <p>See <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4328058"
 * >Sun bug 4328058</a>.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class TimeZoneConverter {

    static final int MILLISECONDS_PER_SECOND = 1000;
    static final int MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND;
    static final int MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE;

    private static final Pattern HOUR_MINUTE = Pattern.compile(
            "^[+-]?[0-9]{1,2}:[0-9]{2}(:[0-9]{2})?$");

    private static final TimeZone UTC = new SimpleTimeZone(0, "UTC");

    private static final long MILLIS_SINCE_1_JAN_2000_UTC;

    static {
        GregorianCalendar c = new GregorianCalendar(UTC);
        c.set(2000, 0, 1, 0, 0, 0);
        MILLIS_SINCE_1_JAN_2000_UTC = c.getTimeInMillis();
    }

    /**
     * return a <code>java.util.Timezone</code> object that delegates to
     * the given Joda <code>DateTimeZone</code>.
     */
    public static TimeZone toTimeZone(final DateTimeZone dtz) {

        TimeZone tz = new TimeZone() {
            @Override
            public void setRawOffset(int n) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean useDaylightTime() {
                long firstTransition = MILLIS_SINCE_1_JAN_2000_UTC;
                return firstTransition != dtz.nextTransition(firstTransition);
            }

            @Override
            public boolean inDaylightTime(Date d) {
                long t = d.getTime();
                return dtz.getStandardOffset(t) != dtz.getOffset(t);
            }

            @Override
            public int getRawOffset() {
                return dtz.getStandardOffset(0);
            }

            @Override
            public int getOffset(long instant) {
                // This method is not abstract, but it normally calls through to the
                // method below.
                // It's optimized here since there's a direct equivalent in
                // DateTimeZone.
                // DateTimeZone and java.util.TimeZone use the same
                // epoch so there's no translation of instant required.
                return dtz.getOffset(instant);
            }

            @Override
            public int getOffset(
                    int era, int year, int month, int day, int dayOfWeek,
                    int milliseconds) {
                int millis = milliseconds;  // milliseconds is day in standard time
                int hour = millis / MILLISECONDS_PER_HOUR;
                millis %= MILLISECONDS_PER_HOUR;
                int minute = millis / MILLISECONDS_PER_MINUTE;
                millis %= MILLISECONDS_PER_MINUTE;
                int second = millis / MILLISECONDS_PER_SECOND;
                millis %= MILLISECONDS_PER_SECOND;
                if (era == GregorianCalendar.BC) {
                    year = -(year - 1);
                }

                // get the time in UTC in case a timezone has changed it's standard
                // offset, e.g. rid of a half hour from UTC.
                DateTime dt = null;
                try {
                    dt = new DateTime(year, month + 1, day, hour, minute,
                            second, millis, dtz);
                } catch (IllegalArgumentException ex) {
                    // Java does not complain if you try to convert a Date that does not
                    // exist due to the offset shifting forward, but Joda time does.
                    // Since we're trying to preserve the semantics of TimeZone, shift
                    // forward over the gap so that we're on a time that exists.
                    // This assumes that the DST correction is one hour long or less.
                    if (hour < 23) {
                        dt = new DateTime(year, month + 1, day, hour + 1, minute,
                                second, millis, dtz);
                    } else {  // Some timezones shift at midnight.
                        Calendar c = new GregorianCalendar();
                        c.clear();
                        c.setTimeZone(TimeZone.getTimeZone("UTC"));
                        c.set(year, month, day, hour, minute, second);
                        c.add(Calendar.HOUR_OF_DAY, 1);
                        int year2 = c.get(Calendar.YEAR),
                                month2 = c.get(Calendar.MONTH),
                                day2 = c.get(Calendar.DAY_OF_MONTH),
                                hour2 = c.get(Calendar.HOUR_OF_DAY);
                        dt = new DateTime(year2, month2 + 1, day2, hour2, minute,
                                second, millis, dtz);
                    }
                }
                // since millis is in standard time, we construct the equivalent
                // GMT+xyz timezone and use that to convert.
                int offset = dtz.getStandardOffset(dt.getMillis());
                DateTime stdDt = new DateTime(
                        year, month + 1, day, hour, minute,
                        second, millis, DateTimeZone.forOffsetMillis(offset));
                return getOffset(stdDt.getMillis());
            }

            @Override
            public String toString() {
                return dtz.toString();
            }

            @Override
            public boolean equals(Object that) {
                if (!(that instanceof TimeZone)) {
                    return false;
                }
                TimeZone thatTz = (TimeZone) that;
                return getID().equals(thatTz.getID()) && hasSameRules(thatTz);
            }

            @Override
            public int hashCode() {
                return getID().hashCode();
            }

            private static final long serialVersionUID = 58752546800455L;
        };
        // Now fix the tzids.  DateTimeZone has a bad habit of returning
        // "+06:00" when it should be "GMT+06:00"
        String newTzid = cleanUpTzid(dtz.getID());
        tz.setID(newTzid);
        return tz;
    }

    /**
     * If tzid is of the form [+-]hh:mm, we rewrite it to GMT[+-]hh:mm
     * Otherwise return it unchanged.
     */
    static String cleanUpTzid(String tzid) {
        Matcher m = HOUR_MINUTE.matcher(tzid);
        return m.matches() ?  // of the form [+-]hh:mm
                "GMT" +
                        (tzid.startsWith("-") || tzid.startsWith("+") ? "" : "+") + tzid :
                tzid;
    }

    private TimeZoneConverter() {
        // uninstantiable
    }
}
