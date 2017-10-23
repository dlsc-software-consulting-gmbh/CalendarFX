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

/**
 * a stateful operation that can be successively invoked to generate the next
 * part of a date in series.
 *
 * <p>Each field generator takes as input the larger fields, and modifies its
 * field, leaving the other fields unchanged.
 * A year generator will update bldr.year, leaving the smaller fields unchanged,
 * a month generator will update bldr.month, taking its cue from bldr.year,
 * also leaving the smaller fields unchanged.</p>
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
abstract class Generator {

    /**
     * @param bldr both input and output.  non null.  modified in place.
     * @return true iff there are more instances of the generator's field to
     *   generate.  If a generator is exhausted, generating a new value of a
     *   larger field may allow it to continue, so a month generator that runs
     *   out of months at 12, may start over at 1 if called with a bldr with
     *   a different year.
     * @throws IteratorShortCircuitingException when an iterator reaches
     *   a threshold past which it cannot generate any more dates.  This indicates
     *   that the entire iteration process should end.
     */
    abstract boolean generate(DTBuilder bldr)
            throws IteratorShortCircuitingException;

    /**
     * thrown when an iteration process should be ended completely due to an
     * artificial system limit.  This allows us to make a distinction between
     * normal exhaustion of iteration, and an artificial limit that may fall in
     * a set, and so affect subsequent evaluation of BYSETPOS rules.
     *
     * <p>Since this class is meant to be thrown as a flow control construct to
     * indicate an artificial limit has been reached, not really an exceptional
     * condition, and since its clients have no need of the stacktrace, we use a
     * singleton to avoid forcing the JVM to unoptimize and decompile the
     * RecurrenceIterator's inner loop.</p>
     */
    static class IteratorShortCircuitingException extends Exception {
        private IteratorShortCircuitingException() {
            super();
            setStackTrace(new StackTraceElement[0]);
        }

        private static final IteratorShortCircuitingException INSTANCE =
                new IteratorShortCircuitingException();

        static IteratorShortCircuitingException instance() {
            return INSTANCE;
        }
    }

    static {
        // suffer the stack trace generation on class load of Generator, which will
        // happen before any of the recuriter stuff could possibly have been JIT
        // compiled.
        IteratorShortCircuitingException.instance();
    }

}
