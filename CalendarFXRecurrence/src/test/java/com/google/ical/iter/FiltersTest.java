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
import com.google.ical.values.IcalParseUtil;
import com.google.ical.values.Weekday;
import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class FiltersTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWeekIntervalFilter() throws Exception {
        // *s match those that are in the weeks that should pass the filter

        Predicate<? super DateValue> f1 = Filters.weekIntervalFilter(
                2, Weekday.MO, IcalParseUtil.parseDateValue("20050911"));
        // FOR f1
        //    September 2005
        //  Su  Mo  Tu  We  Th  Fr  Sa
        //                   1   2   3
        //   4  *5  *6  *7  *8  *9 *10
        // *11  12  13  14  15  16  17
        //  18 *19 *20 *21 *22 *23 *24
        // *25  26  27  28  29  30
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050909")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050910")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050911")));
        assertTrue(!f1.apply(IcalParseUtil.parseDateValue("20050912")));
        assertTrue(!f1.apply(IcalParseUtil.parseDateValue("20050913")));
        assertTrue(!f1.apply(IcalParseUtil.parseDateValue("20050914")));
        assertTrue(!f1.apply(IcalParseUtil.parseDateValue("20050915")));
        assertTrue(!f1.apply(IcalParseUtil.parseDateValue("20050916")));
        assertTrue(!f1.apply(IcalParseUtil.parseDateValue("20050917")));
        assertTrue(!f1.apply(IcalParseUtil.parseDateValue("20050918")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050919")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050920")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050921")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050922")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050923")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050924")));
        assertTrue(f1.apply(IcalParseUtil.parseDateValue("20050925")));
        assertTrue(!f1.apply(IcalParseUtil.parseDateValue("20050926")));

        Predicate<? super DateValue> f2 = Filters.weekIntervalFilter(
                2, Weekday.SU, IcalParseUtil.parseDateValue("20050911"));
        // FOR f2
        //    September 2005
        //  Su  Mo  Tu  We  Th  Fr  Sa
        //                   1   2   3
        //   4   5   6   7   8   9  10
        // *11 *12 *13 *14 *15 *16 *17
        //  18  19  20  21  22  23  24
        // *25 *26 *27 *28 *29 *30
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050909")));
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050910")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050911")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050912")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050913")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050914")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050915")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050916")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050917")));
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050918")));
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050919")));
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050920")));
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050921")));
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050922")));
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050923")));
        assertTrue(!f2.apply(IcalParseUtil.parseDateValue("20050924")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050925")));
        assertTrue(f2.apply(IcalParseUtil.parseDateValue("20050926")));
    }

}
