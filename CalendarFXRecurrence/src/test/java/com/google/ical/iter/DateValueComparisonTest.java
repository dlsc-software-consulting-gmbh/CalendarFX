/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
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

import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class DateValueComparisonTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testComparisonSameAsDateValueImpl() throws Exception {
    // It's more important for DateValueComparison to be a total ordering
    // (see the class comments) than it be consistent with DateValue.
    DateValue[] inOrder = {
      new DateValueImpl(2006, 4, 11),
      new DateTimeValueImpl(2006, 4, 11, 0, 0, 0),
      new DateTimeValueImpl(2006, 4, 11, 0, 0, 1),
      new DateTimeValueImpl(2006, 4, 11, 12, 30, 15),
      new DateTimeValueImpl(2006, 4, 11, 23, 59, 59),
      new DateValueImpl(2006, 4, 12),
      new DateValueImpl(2006, 4, 13),
      new DateTimeValueImpl(2006, 4, 14, 12, 0, 0),
      new DateTimeValueImpl(2006, 4, 14, 15, 0, 0),
    };

    for (int i = 1; i < inOrder.length; ++i) {
      assertTrue(DateValueComparison.comparable(inOrder[i - 1]) <
                 DateValueComparison.comparable(inOrder[i]));
    }
    for (int i = 0; i < inOrder.length; ++i) {
      for (int j = 0; j < inOrder.length; ++j) {
        int cmp1 = sign3(i - j);
        int cmp2 = sign3(inOrder[i].compareTo(inOrder[j]));
        int cmp3 = sign3(DateValueComparison.comparable(inOrder[i]),
                         DateValueComparison.comparable(inOrder[j]));
        assertEquals(cmp1, cmp2);
        assertEquals(cmp2, cmp3);
      }
    }
  }

  static final int sign3(int i) {
    return i < 0 ? -1 : i != 0 ? 1 : 0;
  }

  static final int sign3(long a, long b) {
    return a < b ? -1 : a != b ? 1 : 0;
  }
}
