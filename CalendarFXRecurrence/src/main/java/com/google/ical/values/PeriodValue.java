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
 * a half-open range of {@link DateValue}s.  The start is inclusive, and the
 * end is exclusive.  The end must be on or after the start.  When the start and
 * end are the same, the period is zero width, i.e. contains zero seconds.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public interface PeriodValue {

    /**
     * the start of the period.
     * @return non null.
     */
    DateValue start();

    /**
     * the end of the period.
     * The end must be &gt;= {@link #start()}, and
     * <tt>(start() instanceof {@link TimeValue}) ==
     *     (end() instanceof TimeValue)</tt>.
     * @return non null.
     */
    DateValue end();

    /**
     * true iff this period overlaps the given period.
     * @param pv not null.
     */
    boolean intersects(PeriodValue pv);

    /**
     * true iff this period completely contains the given period.
     * @param pv not null.
     */
    boolean contains(PeriodValue pv);

}  // PeriodValue
