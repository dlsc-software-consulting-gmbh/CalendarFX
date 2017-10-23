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

import com.google.ical.values.DateValue;
import com.google.ical.values.TimeValue;

/**
 * DateValue comparison methods.
 * <p>When we're pulling dates off the priority order, we need them to come off
 * in a consistent order, so we need a total ordering on date values.
 * <p>This means that a DateValue with no time must not be equal to a
 * DateTimeValue at midnight.  Since it obviously doesn't make sense for a
 * DateValue to be after a DateTimeValue the same day at 23:59:59, we put the
 * DateValue before 0 hours of the same day.
 * <p>If we didn't have a total ordering, then it would be harder to correctly
 * handle the case
 * <pre>
 *   RDATE:20060607
 *   EXDATE:20060607
 *   EXDATE:20060607T000000Z
 * </pre>
 * because we'd have two exdates that are equal according to the comparison, but
 * only the first should match.
 * <p>In the following example
 * <pre>
 *   RDATE:20060607
 *   RDATE:20060607T000000Z
 *   EXDATE:20060607
 * </pre>
 * the problem is worse because we may pull a candidate RDATE off the
 * priority queue and then not know whether to consume the EXDATE or not.
 * <p>Absent a total ordering, the following case could only be solved with
 * lookahead and ugly logic.
 * <pre>
 *   RDATE:20060607
 *   RDATE:20060607T000000Z
 *   EXDATE:20060607
 *   EXDATE:20060607T000000Z
 * </pre>
 * <p>The conversion to GMT is also an implementation detail, so it's not clear
 * which timezone we should consider midnight in, and a total ordering allows
 * us to avoid timezone conversions during iteration.</p>
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class DateValueComparison {

    /**
     * reduces a date to a value that can be easily compared to others, consistent
     * with {@link com.google.ical.values.DateValueImpl#compareTo}.
     */
    static long comparable(DateValue dv) {
        long comp = (((((long) dv.year()) << 4) + dv.month()) << 5) + dv.day();
        if (dv instanceof TimeValue) {
            TimeValue tv = (TimeValue) dv;
            // We add 1 to comparable for timed values to make sure that timed
            // events are distinct from all-day events, in keeping with
            // DateValue.compareTo.

            // It would be odd if an all day exclusion matched a midnight event on
            // the same day, but not one at another time of day.
            return (((((comp << 5) + tv.hour()) << 6) + tv.minute()) << 6)
                    + tv.second() + 1;
        } else {
            return comp << 17;
        }
    }

    private DateValueComparison() {
        // uninstantiable
    }

}
