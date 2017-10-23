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
 * a half-open range of {@link DateValue}s.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class PeriodValueImpl implements PeriodValue {

    private DateValue start, end;

    /**
     * returns a period with the given start and end dates.
     * @param start non null.
     * @param end on or after start.  May/must have a time if the start has a
     *   time.
     */
    public static PeriodValue create(DateValue start, DateValue end) {
        return new PeriodValueImpl(start, end);
    }

    /**
     * returns a period with the given start date and duration.
     * @param start non null.
     * @param dur a positive duration represented as a DateValue.
     */
    public static PeriodValue createFromDuration(DateValue start, DateValue dur) {
        DateValue end = TimeUtils.add(start, dur);
        if (end instanceof TimeValue && !(start instanceof TimeValue)) {
            start = TimeUtils.dayStart(start);
        }
        return new PeriodValueImpl(start, end);
    }

    protected PeriodValueImpl(DateValue start, DateValue end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException
                    ("Start (" + start + ") must precede end (" + end + ")");
        }
        if ((start instanceof TimeValue) ^ (end instanceof TimeValue)) {
            throw new IllegalArgumentException
                    ("Start (" + start + ") and end (" + end +
                            ") must both have times or neither have times.");
        }
        this.start = start;
        this.end = end;
    }

    public DateValue start() {
        return start;
    }

    public DateValue end() {
        return end;
    }

    /** true iff this period overlaps the given period. */
    public boolean intersects(PeriodValue pv) {
        DateValue sa = this.start,
                ea = this.end,
                sb = pv.start(),
                eb = pv.end();

        return sa.compareTo(eb) < 0 && sb.compareTo(ea) < 0;
    }

    /** true iff this period completely contains the given period. */
    public boolean contains(PeriodValue pv) {
        DateValue sa = this.start,
                ea = this.end,
                sb = pv.start(),
                eb = pv.end();

        return !(sb.compareTo(sa) < 0 || ea.compareTo(eb) < 0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PeriodValue)) {
            return false;
        }
        PeriodValue that = (PeriodValue) o;
        return this.start().equals(that.start())
                && this.end().equals(that.end());
    }

    @Override
    public int hashCode() {
        return start.hashCode() ^ (31 * end.hashCode());
    }

    @Override
    public String toString() {
        return start().toString() + "/" + end().toString();
    }

}  // PeriodValueImpl
