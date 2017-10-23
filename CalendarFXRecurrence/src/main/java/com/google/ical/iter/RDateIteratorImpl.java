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

/**
 * a recurrence iterator that iterates over an array of dates.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class RDateIteratorImpl implements RecurrenceIterator {
    private int i;
    private DateValue[] datesUtc;

    RDateIteratorImpl(DateValue[] datesUtc) {
        this.datesUtc = datesUtc.clone();  // defensive copy
        assert increasing(datesUtc);  // indirectly checks that not-null.
    }

    public boolean hasNext() {
        return i < datesUtc.length;
    }

    public DateValue next() {
        return datesUtc[i++];
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void advanceTo(DateValue newStartUtc) {
        long startCmp = DateValueComparison.comparable(newStartUtc);
        while (i < datesUtc.length
                && startCmp > DateValueComparison.comparable(datesUtc[i])) {
            ++i;
        }
    }

    /** monotonically. */
    private static <C extends Comparable<C>> boolean increasing(C[] els) {
        for (int i = els.length; --i >= 1; ) {
            if (els[i - 1].compareTo(els[i]) > 0) {
                return false;
            }
        }
        return true;
    }

}
