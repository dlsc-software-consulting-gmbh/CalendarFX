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
import com.google.ical.util.TimeUtils;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.TimeValue;

import java.util.TimeZone;

/**
 * an iterator over dates in an RRULE or EXRULE series.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class RRuleIteratorImpl implements RecurrenceIterator {
    /**
     * a function that determines when the recurrence ends.
     * Takes a date builder and yields shouldContinue:boolean.
     * The condition is applied <b>after</b> the date is converted to utc.
     */
    private final Predicate<? super DateValue> condition_;
    /**
     * a function that applies the various period generators to generate an entire
     * date.
     * This may involve generating a set of dates and discarding all but those
     * that match the BYSETPOS rule.
     */
    private final Generator instanceGenerator_;
    /**
     * a function that takes a builder and populates the year field.
     * Returns false if there aren't more years available.
     */
    private final ThrottledGenerator yearGenerator_;
    /**
     * a function that takes a builder and populates the month field.
     * Returns false if there aren't more months available in the builder's year.
     */
    private final Generator monthGenerator_;
    /**
     * a date that has been computed but not yet yielded to the user.
     */
    private DateValue pendingUtc_;
    /**
     * used to build successive dates.
     * At the start of the building process, contains the last date generated.
     * Different periods are successively inserted into it.
     */
    private DTBuilder builder_;
    /** true iff the recurrence has been exhausted. */
    private boolean done_;
    /** the start date of the recurrence */
    private final DateValue dtStart_;
    /**
     * false iff shorcutting advance would break the semantics of the iteration.
     * This may happen when, for example, the end condition requires that it see
     * every item.
     */
    private final boolean canShortcutAdvance_;
    /**
     * the timezone that result dates should be converted <b>from</b>.
     * All date fields, parameters, and local variables in this class are in
     * the tzid_ timezone, unless they carry the Utc suffix.
     */
    private final TimeZone tzid_;

    /** An iterator that generates dates from an RFC2445 Recurrence Rule */
    RRuleIteratorImpl(
            DateValue dtStart, TimeZone tzid, Predicate<? super DateValue> condition,
            Generator instanceGenerator, ThrottledGenerator yearGenerator,
            Generator monthGenerator, Generator dayGenerator,
            Generator hourGenerator, Generator minuteGenerator,
            Generator secondGenerator,
            boolean canShortcutAdvance) {

        this.condition_ = condition;
        this.instanceGenerator_ = instanceGenerator;
        this.yearGenerator_ = yearGenerator;
        this.monthGenerator_ = monthGenerator;
        this.dtStart_ = dtStart;
        this.tzid_ = tzid;
        this.canShortcutAdvance_ = canShortcutAdvance;

        int initWorkLimit = 1000;

        // Initialize the builder and skip over any extraneous start instances
        DTBuilder builder = new DTBuilder(dtStart);
        this.builder_ = builder;
        // Apply the generators from largest field to smallest so we can start by
        // applying the smallest field iterator when asked to generate a date.
        try {
            Generator[] toInitialize;
            if (InstanceGenerators.skipSubDayGenerators(
                    hourGenerator, minuteGenerator, secondGenerator)) {
                toInitialize = new Generator[]{yearGenerator, monthGenerator};
                builder.hour = ((SingleValueGenerator) hourGenerator).getValue();
                builder.minute = ((SingleValueGenerator) minuteGenerator).getValue();
                builder.second = ((SingleValueGenerator) secondGenerator).getValue();
            } else {
                toInitialize = new Generator[]{
                        yearGenerator, monthGenerator, dayGenerator,
                        hourGenerator, minuteGenerator,
                };
            }
            for (int i = 0; i != toInitialize.length; ) {
                if (toInitialize[i].generate(builder)) {
                    ++i;
                } else {
                    if (--i < 0) {  // No years left.
                        this.done_ = true;
                        break;
                    }
                }
                if (--initWorkLimit == 0) {
                    this.done_ = true;
                    break;
                }
            }
        } catch (Generator.IteratorShortCircuitingException ex) {
            this.done_ = true;
        }

        while (!this.done_) {
            this.pendingUtc_ = this.generateInstance();
            if (null == this.pendingUtc_) {
                this.done_ = true;
                break;
            } else if (this.pendingUtc_.compareTo(
                    TimeUtils.toUtc(dtStart, tzid)) >= 0) {
                // We only apply the condition to the ones past dtStart to avoid
                // counting useless instances
                if (!this.condition_.apply(this.pendingUtc_)) {
                    this.done_ = true;
                    this.pendingUtc_ = null;
                }
                break;
            }
            if (--initWorkLimit == 0) {
                this.done_ = true;
                break;
            }
        }
    }

    /** are there more dates in this recurrence? */
    public boolean hasNext() {
        if (null == this.pendingUtc_) {
            this.fetchNext();
        }
        return null != this.pendingUtc_;
    }

    /** fetch and return the next date in this recurrence. */
    public DateValue next() {
        if (null == this.pendingUtc_) {
            this.fetchNext();
        }
        DateValue next = this.pendingUtc_;
        this.pendingUtc_ = null;
        return next;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Skip all instances of the recurrence before the given date, so that
     * the next call to {@link #next} will return a date on or after the given
     * date, assuming the recurrence includes such a date.
     */
    public void advanceTo(DateValue dateUtc) {
        // Don't throw away a future pending date since the iterators will not
        // generate it again.
        if (this.pendingUtc_ != null && dateUtc.compareTo(this.pendingUtc_) <= 0) {
            return;
        }

        DateValue dateLocal = TimeUtils.fromUtc(dateUtc, tzid_);
        // Short-circuit if we're already past dateUtc.
        if (dateLocal.compareTo(this.builder_.toDate()) <= 0) {
            return;
        }
        this.pendingUtc_ = null;

        try {
            if (this.canShortcutAdvance_) {
                // skip years before date.year
                if (this.builder_.year < dateLocal.year()) {
                    do {
                        if (!this.yearGenerator_.generate(this.builder_)) {
                            this.done_ = true;
                            return;
                        }
                    } while (this.builder_.year < dateLocal.year());
                    while (!this.monthGenerator_.generate(this.builder_)) {
                        if (!this.yearGenerator_.generate(this.builder_)) {
                            this.done_ = true;
                            return;
                        }
                    }
                }
                // skip months before date.year/date.month
                while (this.builder_.year == dateLocal.year()
                        && this.builder_.month < dateLocal.month()) {
                    while (!this.monthGenerator_.generate(this.builder_)) {
                        // if there are more years available fetch one
                        if (!this.yearGenerator_.generate(this.builder_)) {
                            // otherwise the recurrence is exhausted
                            this.done_ = true;
                            return;
                        }
                    }
                }
            }

            // consume any remaining instances
            while (!this.done_) {
                DateValue dUtc = this.generateInstance();
                if (null == dUtc) {
                    this.done_ = true;
                } else {
                    if (!this.condition_.apply(dUtc)) {
                        this.done_ = true;
                    } else if (dUtc.compareTo(dateUtc) >= 0) {
                        this.pendingUtc_ = dUtc;
                        break;
                    }
                }
            }
        } catch (Generator.IteratorShortCircuitingException ex) {
            this.done_ = true;
        }
    }

    /** calculates and stored the next date in this recurrence. */
    private void fetchNext() {
        if (null != this.pendingUtc_ || this.done_) {
            return;
        }

        DateValue dUtc = this.generateInstance();

        // check the exit condition
        if (null != dUtc && this.condition_.apply(dUtc)) {
            this.pendingUtc_ = dUtc;
            this.yearGenerator_.workDone();
        } else {
            this.done_ = true;
        }
    }

    private static final DateValue MIN_DATE =
            new DateValueImpl(Integer.MIN_VALUE, 1, 1);
    /**
     * make sure the iterator is monotonically increasing.
     * The local time is guaranteed to be monotonic, but because of daylight
     * savings shifts, the time in UTC may not be.
     */
    private DateValue lastUtc_ = MIN_DATE;

    /**
     * @return a date value in UTC.
     */
    private DateValue generateInstance() {
        try {
            do {
                if (!this.instanceGenerator_.generate(this.builder_)) {
                    return null;
                }
                DateValue dUtc = this.dtStart_ instanceof TimeValue
                        ? TimeUtils.toUtc(this.builder_.toDateTime(), this.tzid_)
                        : this.builder_.toDate();
                if (dUtc.compareTo(this.lastUtc_) > 0) {
                    return dUtc;
                }
            } while (true);
        } catch (Generator.IteratorShortCircuitingException ex) {
            return null;
        }
    }

}
