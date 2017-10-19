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

package com.google.ical.util;

import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValueImpl;

import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class DTBuilderTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testEquals() throws Exception {
    assertTrue(!new DTBuilder(2006, 1, 2).equals(null));
    assertTrue(!new DTBuilder(2006, 1, 2).equals(new Object()));
    assertTrue(!new DTBuilder(2006, 1, 2).equals(
                   new DTBuilder(2006, 1, 2).toString()));

    assertTrue(new DTBuilder(2006, 1, 2).equals(new DTBuilder(2006, 1, 2)));
    assertTrue(new DTBuilder(2006, 1, 2, 12, 30, 0).equals(
                   new DTBuilder(2006, 1, 2, 12, 30, 0)));
    assertTrue(!new DTBuilder(2005, 1, 2, 12, 30, 0).equals(
                   new DTBuilder(2006, 1, 2, 12, 30, 0)));
    assertTrue(!new DTBuilder(2006, 3, 2, 12, 30, 0).equals(
                   new DTBuilder(2006, 1, 2, 12, 30, 0)));
    assertTrue(!new DTBuilder(2006, 1, 3, 12, 30, 0).equals(
                   new DTBuilder(2006, 1, 2, 12, 30, 0)));
    assertTrue(!new DTBuilder(2006, 1, 2, 13, 30, 0).equals(
                   new DTBuilder(2006, 1, 2, 12, 30, 0)));
    assertTrue(!new DTBuilder(2006, 1, 2, 12, 45, 0).equals(
                   new DTBuilder(2006, 1, 2, 12, 30, 0)));
    assertTrue(!new DTBuilder(2006, 1, 2, 12, 30, 1).equals(
                   new DTBuilder(2006, 1, 2, 12, 30, 0)));
    assertEquals(new DTBuilder(2006, 1, 2).hashCode(),
                 new DTBuilder(2006, 1, 2).hashCode());
    assertEquals(new DTBuilder(0, 0, 0), new DTBuilder(0, 0, 0, 0, 0, 0));
  }

  public void testToDate() throws Exception {
    assertEquals(new DateValueImpl(2006, 1, 2),
                 new DTBuilder(2006, 1, 2).toDate());
    assertEquals(new DateValueImpl(2006, 1, 2),
                 new DTBuilder(2006, 1, 2, 12, 30, 45).toDate());
    // test normalization
    assertEquals(new DateValueImpl(2006, 1, 2),
                 new DTBuilder(2005, 12, 33).toDate());
  }

  public void testToDateTime() throws Exception {
    assertEquals(new DateTimeValueImpl(2006, 1, 2, 0, 0, 0),
                 new DTBuilder(2006, 1, 2).toDateTime());
    assertEquals(new DateTimeValueImpl(2006, 1, 2, 12, 30, 45),
                 new DTBuilder(2006, 1, 2, 12, 30, 45).toDateTime());
    // test normalization
    assertEquals(new DateTimeValueImpl(2006, 1, 2, 0, 0, 0),
                 new DTBuilder(2005, 12, 33, 0, 0, 0).toDateTime());
    assertEquals(new DateTimeValueImpl(2006, 1, 2, 12, 0, 0),
                 new DTBuilder(2005, 12, 31, 60, 0, 0).toDateTime());
  }

  public void testCompareTo() throws Exception {
    assertTrue(
        new DTBuilder(2005, 6, 15).compareTo(new DateValueImpl(2005, 6, 15))
        == 0);
    assertTrue(
        new DTBuilder(2005, 6, 15).compareTo(new DateValueImpl(2006, 6, 15))
        < 0);
    assertTrue(
        new DTBuilder(2005, 6, 15).compareTo(new DateValueImpl(2005, 7, 15))
        < 0);
    assertTrue(
        new DTBuilder(2005, 6, 15).compareTo(new DateValueImpl(2005, 6, 16))
        < 0);
    assertTrue(
        new DTBuilder(2006, 6, 15).compareTo(new DateValueImpl(2005, 6, 15))
        > 0);
    assertTrue(
        new DTBuilder(2005, 7, 15).compareTo(new DateValueImpl(2005, 6, 15))
        > 0);
    assertTrue(
        new DTBuilder(2005, 6, 16).compareTo(new DateValueImpl(2005, 6, 15))
        > 0);
    assertTrue(
        new DTBuilder(2005, 6, 15, 12, 0, 0).compareTo(
            new DateTimeValueImpl(2005, 6, 15, 12, 0, 0))
        == 0);
    assertTrue(
        new DTBuilder(2005, 6, 15, 11, 0, 0).compareTo(
            new DateTimeValueImpl(2005, 6, 15, 12, 0, 0))
        < 0);
    assertTrue(
        new DTBuilder(2005, 6, 15, 13, 0, 0).compareTo(
            new DateTimeValueImpl(2005, 6, 15, 12, 0, 0))
        > 0);
  }

  public void testNormalize() throws Exception {
    DTBuilder dtb = new DTBuilder(2006, 1, 1);
    assertEquals("2006-1-1 0:0:0", dtb.toString());
    dtb.day -= 1;
    dtb.normalize();
    assertEquals("2005-12-31 0:0:0", dtb.toString());
    dtb.day -=61;
    dtb.normalize();
    assertEquals("2005-10-31 0:0:0", dtb.toString());
    dtb.day -= 365;
    dtb.normalize();
    assertEquals("2004-10-31 0:0:0", dtb.toString());
    dtb.month += 25; // + 24 -> 2006-10-31, + 1 -> 2006-11-31 -> 2006-12-1
    dtb.normalize();
    assertEquals("2006-12-1 0:0:0", dtb.toString());
    dtb.month -= 13;
    dtb.normalize();
    assertEquals("2005-11-1 0:0:0", dtb.toString());
    dtb.month += 2;
    dtb.normalize();
    assertEquals("2006-1-1 0:0:0", dtb.toString());
    dtb.day += 398;  // 1 year + 1 month + 2 days
    dtb.normalize();
    assertEquals("2007-2-3 0:0:0", dtb.toString());
    dtb.hour += 252;
    dtb.normalize();
    assertEquals("2007-2-13 12:0:0", dtb.toString());
    dtb.hour -= 365 * 24 - 8;
    dtb.normalize();
    assertEquals("2006-2-13 20:0:0", dtb.toString());
    dtb.minute -= 24 * 60;
    dtb.normalize();
    assertEquals("2006-2-12 20:0:0", dtb.toString());
    dtb.second -= 12 * 60 * 60;
    dtb.normalize();
    assertEquals("2006-2-12 8:0:0", dtb.toString());
  }
}
