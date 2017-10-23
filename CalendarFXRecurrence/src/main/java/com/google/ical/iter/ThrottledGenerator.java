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

/**
 * A generator that may stop generating values after some point, if e.g. it's
 * output is never productive.  This is used to stop rules like
 *   <blockquote>
 *     <code>RRULE:FREQ=YEARLY;BYMONTH=2;BYMONTHDAY=30</code>
 *   </blockquote>
 * from hanging an iterator.
 * <p>If a rule does prove productive though, it can be alerted to the fact by
 * the {@link #workDone} method, so that any throttle can be reset.
 * </p>
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
abstract class ThrottledGenerator extends Generator {

    /**
     * called to reset any throttle after work is done.  This must be called in
     * the outermost loop of any iterator.
     */
    abstract void workDone();

}
