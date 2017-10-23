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
import com.google.ical.values.DateValueImpl;
import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class ConditionsTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCountCondition() throws Exception {
        Predicate<DateValue> cc = Conditions.countCondition(3);
        assertTrue(cc.apply(new DateValueImpl(2006, 2, 1)));
        assertTrue(cc.apply(new DateValueImpl(2006, 2, 2)));
        assertTrue(cc.apply(new DateValueImpl(2006, 2, 3)));
        assertTrue(!cc.apply(new DateValueImpl(2006, 2, 4)));
    }


}
