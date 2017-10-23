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

import com.google.ical.util.Predicate;
import com.google.ical.values.DateValue;

/**
 * factory for predicates used to test whether a recurrence is over.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class Conditions {

    /** constructs a condition that fails after passing count dates. */
    static Predicate<DateValue> countCondition(final int count) {
        return new Predicate<DateValue>() {
            int count_ = count;

            public boolean apply(DateValue value) {
                return --count_ >= 0;
            }

            @Override
            public String toString() {
                return "CountCondition:" + count_;
            }
        };
    }

    /**
     * constructs a condition that passes for every date on or before until.
     * @param until non null.
     */
    static Predicate<DateValue> untilCondition(final DateValue until) {
        return new Predicate<DateValue>() {
            public boolean apply(DateValue date) {
                return date.compareTo(until) <= 0;
            }

            @Override
            public String toString() {
                return "UntilCondition:" + until;
            }
        };
    }

    private Conditions() {
        // uninstantiable
    }

}
