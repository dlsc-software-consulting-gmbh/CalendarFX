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
 * possible recurrence frequencies.  Names correspond to RFC2445 literals.
 *
 * <p>According to section 4.3.10 of RFC 2445:</p>
 * <blockquote>
 * The FREQ rule part identifies the type of recurrence rule. This rule
 * part MUST be specified in the recurrence rule. Valid values include
 * SECONDLY, to specify repeating events based on an interval of a
 * second or more; MINUTELY, to specify repeating events based on an
 * interval of a minute or more; HOURLY, to specify repeating events
 * based on an interval of an hour or more; DAILY, to specify repeating
 * events based on an interval of a day or more; WEEKLY, to specify
 * repeating events based on an interval of a week or more; MONTHLY, to
 * specify repeating events based on an interval of a month or more; and
 * YEARLY, to specify repeating events based on an interval of a year or
 * more.
 * </blockquote>
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public enum Frequency {
    // in order of increasing length
    SECONDLY,
    MINUTELY,
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,;
}
