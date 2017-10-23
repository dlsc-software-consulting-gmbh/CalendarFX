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

import java.util.Iterator;

/**
 * an iterator over date values in order.  Does not support the
 * <code>remove</code> operation.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public interface RecurrenceIterator extends Iterator<DateValue> {

    /** true iff there are more dates in the series. */
    boolean hasNext();

    /**
     * returns the next date in the series, in UTC.
     * If <code>!hasNext()</code>, then behavior is undefined.
     *
     * @return a DateValue that is strictly later than any date previously
     *   returned by this iterator.
     */
    DateValue next();

    /**
     * skips all dates in the series before the given date.
     *
     * @param newStartUtc non null.
     */
    void advanceTo(DateValue newStartUtc);

    /**
     * unsupported.
     * @throws UnsupportedOperationException always
     */
    void remove();
}
