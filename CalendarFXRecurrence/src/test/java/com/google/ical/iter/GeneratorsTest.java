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

import com.google.ical.util.DTBuilder;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.IcalParseUtil;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;
import java.lang.reflect.Field;
import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class GeneratorsTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private void runGeneratorTests(
      Generator generator, DTBuilder builder, String fieldName, String golden)
      throws Exception {
    runGeneratorTests(generator, builder, fieldName, golden, 50);
  }

  private void runGeneratorTests(
      Generator generator, DTBuilder builder, String fieldName, String golden,
      int max)
      throws Exception {

    Field field = DTBuilder.class.getDeclaredField(fieldName);
    StringBuilder output = new StringBuilder();
    for (int k = 0; k < max && generator.generate(builder); ++k) {
      if (0 != k) { output.append(", "); }
      output.append(String.valueOf(field.get(builder)));
    }
    assertEquals(golden, output.toString());
  }

  public void testByYearDayGenerator() throws Exception {
    runGeneratorTests(
        Generators.byYearDayGenerator(new int[] { 1, 5, -1, 100 },
                                      IcalParseUtil.parseDateValue("20060101")),
        new DTBuilder(2006, 1, 1), "day", "1, 5");
    runGeneratorTests(
        Generators.byYearDayGenerator(new int[] { 1, 5, -1, 100 },
                                      IcalParseUtil.parseDateValue("20060102")),
        new DTBuilder(2006, 1, 2), "day", "1, 5");
    runGeneratorTests(
        Generators.byYearDayGenerator(new int[] { 100 },
                                      IcalParseUtil.parseDateValue("20060106")),
        new DTBuilder(2006, 1, 6), "day", "");
    runGeneratorTests(
        Generators.byYearDayGenerator(new int[] {  },
                                      IcalParseUtil.parseDateValue("20060106")),
        new DTBuilder(2006, 1, 6), "day", "");
    runGeneratorTests(
        Generators.byYearDayGenerator(new int[] { 1, 5, -1, 100 },
                                      IcalParseUtil.parseDateValue("20060201")),
        new DTBuilder(2006, 2, 1), "day", "");
    runGeneratorTests(
        Generators.byYearDayGenerator(new int[] { 1, 5, -1, 100 },
                                      IcalParseUtil.parseDateValue("20061201")),
        new DTBuilder(2006, 12, 1), "day", "31");
    runGeneratorTests(
        Generators.byYearDayGenerator(new int[] { 1, 5, -1, 100 },
                                      IcalParseUtil.parseDateValue("20060401")),
        new DTBuilder(2006, 4, 1), "day", "10");
  }

  public void testByWeekNoGenerator() throws Exception {
    Generator g = Generators.byWeekNoGenerator(
        new int[] { 22 }, Weekday.SU, IcalParseUtil.parseDateValue("20060101"));
    runGeneratorTests(g, new DTBuilder(2006, 1, 1), "day", "");
    runGeneratorTests(g, new DTBuilder(2006, 2, 1), "day", "");
    runGeneratorTests(g, new DTBuilder(2006, 3, 1), "day", "");
    runGeneratorTests(g, new DTBuilder(2006, 4, 1), "day", "");
    runGeneratorTests(g, new DTBuilder(2006, 5, 1), "day", "28, 29, 30, 31");
    runGeneratorTests(g, new DTBuilder(2006, 6, 1), "day", "1, 2, 3");
    runGeneratorTests(g, new DTBuilder(2006, 7, 1), "day", "");

    // weekstart of monday shifts each week forward by one
    Generator g2 = Generators.byWeekNoGenerator(
        new int[] { 22 }, Weekday.MO, IcalParseUtil.parseDateValue("20060101"));
    runGeneratorTests(g2, new DTBuilder(2006, 1, 1), "day", "");
    runGeneratorTests(g2, new DTBuilder(2006, 2, 1), "day", "");
    runGeneratorTests(g2, new DTBuilder(2006, 3, 1), "day", "");
    runGeneratorTests(g2, new DTBuilder(2006, 4, 1), "day", "");
    runGeneratorTests(g2, new DTBuilder(2006, 5, 1), "day", "29, 30, 31");
    runGeneratorTests(g2, new DTBuilder(2006, 6, 1), "day", "1, 2, 3, 4");
    runGeneratorTests(g2, new DTBuilder(2006, 7, 1), "day", "");

    // 2004 with a week start of monday has no orphaned days.
    // 2004-01-01 falls on Thursday
    Generator g3 = Generators.byWeekNoGenerator(
        new int[] { 14 }, Weekday.MO, IcalParseUtil.parseDateValue("20040101"));
    runGeneratorTests(g3, new DTBuilder(2004, 1, 1), "day", "");
    runGeneratorTests(g3, new DTBuilder(2004, 2, 1), "day", "");
    runGeneratorTests(g3, new DTBuilder(2004, 3, 1), "day", "29, 30, 31");
    runGeneratorTests(g3, new DTBuilder(2004, 4, 1), "day", "1, 2, 3, 4");
    runGeneratorTests(g3, new DTBuilder(2004, 5, 1), "day", "");
  }

  public void testByDayGenerator() throws Exception {
    WeekdayNum[] days = new WeekdayNum[] {
      new WeekdayNum(0, Weekday.SU), // every sunday
      new WeekdayNum(1, Weekday.MO), // first monday
      new WeekdayNum(5, Weekday.MO), // fifth monday
      new WeekdayNum(-2, Weekday.TU) // second to last tuesday
    };
    Generator g = Generators.byDayGenerator(
        days, false, IcalParseUtil.parseDateValue("20060101"));
    runGeneratorTests(
        g, new DTBuilder(2006, 1, 1), "day", "1, 2, 8, 15, 22, 24, 29, 30");
    runGeneratorTests(
        g, new DTBuilder(2006, 2, 1), "day", "5, 6, 12, 19, 21, 26");
  }

  public void testByMonthDayGenerator() throws Exception {
    int[] monthDays = new int[] { 1, 15, 29 };
    runGeneratorTests(Generators.byMonthDayGenerator(
                          monthDays, IcalParseUtil.parseDateValue("20060101")),
                      new DTBuilder(2006, 1, 1), "day", "1, 15, 29");
    runGeneratorTests(Generators.byMonthDayGenerator(
                          monthDays, IcalParseUtil.parseDateValue("20060115")),
                      new DTBuilder(2006, 1, 15), "day", "1, 15, 29");
    runGeneratorTests(Generators.byMonthDayGenerator(
                          monthDays, IcalParseUtil.parseDateValue("20060201")),
                      new DTBuilder(2006, 2, 1), "day", "1, 15");
    runGeneratorTests(Generators.byMonthDayGenerator(
                          monthDays, IcalParseUtil.parseDateValue("20060216")),
                      new DTBuilder(2006, 2, 16), "day", "1, 15");

    monthDays = new int[] { 1, -30, 30 };
    Generator g2 = Generators.byMonthDayGenerator(
        monthDays, IcalParseUtil.parseDateValue("20060101"));
    runGeneratorTests(g2,
                      new DTBuilder(2006, 1, 1), "day", "1, 2, 30");
    runGeneratorTests(g2,
                      new DTBuilder(2006, 2, 1), "day", "1");
    runGeneratorTests(g2,
                      new DTBuilder(2006, 3, 1), "day", "1, 2, 30");
    runGeneratorTests(g2,
                      new DTBuilder(2006, 4, 1), "day", "1, 30");
  }

  public void testByMonthGenerator() throws Exception {
    runGeneratorTests(Generators.byMonthGenerator(
                          new int[] { 2, 8, 6, 10 },
                          IcalParseUtil.parseDateValue("20060101")),
                      new DTBuilder(2006, 1, 1), "month", "2, 6, 8, 10");
    Generator g = Generators.byMonthGenerator(
        new int[] { 2, 8, 6, 10 }, IcalParseUtil.parseDateValue("20060401"));
    runGeneratorTests(g, new DTBuilder(2006, 4, 1), "month", "2, 6, 8, 10");
    runGeneratorTests(g, new DTBuilder(2007, 11, 1), "month", "2, 6, 8, 10");
  }

  public void testByYearGenerator() throws Exception {
    runGeneratorTests(
        Generators.byYearGenerator(new int[] { 1066, 1492, 1876, 1975, 2006 },
                                   IcalParseUtil.parseDateValue("20060101")),
        new DTBuilder(2006, 1, 1), "year", "2006");
    runGeneratorTests(
        Generators.byYearGenerator(new int[] { 1066, 1492, 1876, 1975, 2006 },
                                   IcalParseUtil.parseDateValue("20070101")),
        new DTBuilder(2007, 1, 1), "year", "");
    runGeneratorTests(
        Generators.byYearGenerator(new int[] { 1066, 1492, 1876, 1975, 2006 },
                                   IcalParseUtil.parseDateValue("10660701")),
        new DTBuilder(1066, 7, 1), "year", "1066, 1492, 1876, 1975, 2006");
    runGeneratorTests(
        Generators.byYearGenerator(new int[] { 1066, 1492, 1876, 1975, 2006 },
                                   IcalParseUtil.parseDateValue("19000701")),
        new DTBuilder(1900, 7, 1), "year", "1975, 2006");
  }

  public void testSerialDayGenerator() throws Exception {
    runGeneratorTests(
        Generators.serialDayGenerator(
            1, IcalParseUtil.parseDateValue("20060115")),
        new DTBuilder(2006, 1, 15), "day",
          "15, 16, 17, 18, 19, 20, 21, 22, 23, 24, "
          + "25, 26, 27, 28, 29, 30, 31");
    runGeneratorTests(Generators.serialDayGenerator(
                          1, IcalParseUtil.parseDateValue("20060101")),
                      new DTBuilder(2006, 1, 1), "day",
                      "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, "
                      + "11, 12, 13, 14, 15, 16, 17, 18, 19, 20, "
                      + "21, 22, 23, 24, 25, 26, 27, 28, 29, 30, "
                      + "31");
    Generator g = Generators.serialDayGenerator(
        2, IcalParseUtil.parseDateValue("20060101"));
    runGeneratorTests(
        g, new DTBuilder(2006, 1, 1), "day",
        "1, 3, 5, 7, 9, "
        + "11, 13, 15, 17, 19, "
        + "21, 23, 25, 27, 29, "
        + "31");
    // now g should start on the second of February
    runGeneratorTests(
        g, new DTBuilder(2006, 2, 1), "day",
        "2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28");
    // and if we skip way ahead to June, it should start on the 1st
    // This test is limited to one output value
    runGeneratorTests(g,
                      new DTBuilder(2006, 4, 1), "day", "1", 1);
    // test with intervals longer than 30 days
    Generator g2 = Generators.serialDayGenerator(
        45, IcalParseUtil.parseDateValue("20060101"));
    runGeneratorTests(g2, new DTBuilder(2006, 1, 1), "day", "1");
    runGeneratorTests(g2, new DTBuilder(2006, 2, 1), "day", "15");
    runGeneratorTests(g2, new DTBuilder(2006, 3, 1), "day", "");
    runGeneratorTests(g2, new DTBuilder(2006, 4, 1), "day", "1");
    runGeneratorTests(g2, new DTBuilder(2006, 5, 1), "day", "16");
  }

  public void testSerialMonthGenerator() throws Exception {
    runGeneratorTests(
        Generators.serialMonthGenerator(
            1, IcalParseUtil.parseDateValue("20060101")),
        new DTBuilder(2006, 1, 1), "month",
        "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12");
    runGeneratorTests(
        Generators.serialMonthGenerator(
            1, IcalParseUtil.parseDateValue("20060401")),
        new DTBuilder(2006, 4, 1), "month", "4, 5, 6, 7, 8, 9, 10, 11, 12");
    runGeneratorTests(
        Generators.serialMonthGenerator(
            2, IcalParseUtil.parseDateValue("20060401")),
        new DTBuilder(2006, 4, 1), "month", "4, 6, 8, 10, 12");
    Generator g = Generators.serialMonthGenerator(
        7, IcalParseUtil.parseDateValue("20060401"));
    runGeneratorTests(g, new DTBuilder(2006, 4, 1), "month", "4, 11");
    runGeneratorTests(g, new DTBuilder(2007, 11, 1), "month", "6");
    runGeneratorTests(g, new DTBuilder(2008, 6, 1), "month", "1, 8");
    Generator g2 = Generators.serialMonthGenerator(
        18, IcalParseUtil.parseDateValue("20060401"));
    runGeneratorTests(g2, new DTBuilder(2006, 4, 1), "month", "4");
    runGeneratorTests(g2, new DTBuilder(2007, 11, 1), "month", "10");
    runGeneratorTests(g2, new DTBuilder(2008, 6, 1), "month", "");
    runGeneratorTests(g2, new DTBuilder(2009, 6, 1), "month", "4");
  }

  public void testSerialYearGenerator() throws Exception {
    runGeneratorTests(
        Generators.serialYearGenerator(
            1, IcalParseUtil.parseDateValue("20060101")),
        new DTBuilder(2006, 1, 1), "year", "2006, 2007, 2008, 2009, 2010", 5);
    runGeneratorTests(
        Generators.serialYearGenerator(
            2, IcalParseUtil.parseDateValue("20060101")),
        new DTBuilder(2006, 1, 1), "year", "2006, 2008, 2010, 2012, 2014", 5);
  }

  public void testSerialHourGeneratorGivenDate() throws Exception {
    DateValue dtStart = new DateValueImpl(2011, 8, 8);
    Generator g = Generators.serialHourGenerator(7, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 0:0:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 7:0:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 14:0:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 21:0:0", b.toString());
    assertFalse(g.generate(b));
    assertEquals("2011-8-8 21:0:0", b.toString());
    ++b.day;
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 4:0:0", b.toString());
    b.day += 2;
    assertTrue(g.generate(b));
    assertEquals("2011-8-11 5:0:0", b.toString());
  }

  public void testSerialHourGeneratorGivenTime() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 1, 25, 30);
    Generator g = Generators.serialHourGenerator(7, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 1:25:30", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 8:25:30", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 15:25:30", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 22:25:30", b.toString());
    assertFalse(g.generate(b));
    assertEquals("2011-8-8 22:25:30", b.toString());
    ++b.day;
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 5:25:30", b.toString());
    b.day += 2;
    assertTrue(g.generate(b));
    assertEquals("2011-8-11 6:25:30", b.toString());
  }

  public void testSerialHourGeneratorRolledBack() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 1, 25, 30);
    Generator g = Generators.serialHourGenerator(7, dtStart);
    DTBuilder b = new DTBuilder(new DateTimeValueImpl(2011, 8, 1, 0, 29, 50));
    assertTrue(g.generate(b));
    assertEquals("2011-8-1 1:29:50", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-1 8:29:50", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-1 15:29:50", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-1 22:29:50", b.toString());
    assertFalse(g.generate(b));
    assertEquals("2011-8-1 22:29:50", b.toString());
    ++b.day;
    assertTrue(g.generate(b));
    assertEquals("2011-8-2 5:29:50", b.toString());
    b.day += 2;
    assertTrue(g.generate(b));
    assertEquals("2011-8-4 6:29:50", b.toString());
  }

  public void testByHourGeneratorGivenDate() throws Exception {
    DateValue dtStart = new DateValueImpl(2011, 8, 8);
    Generator g = Generators.byHourGenerator(new int[] { 3, 9, 11 }, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 3:0:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 9:0:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 11:0:0", b.toString());
    assertFalse(g.generate(b));
    assertEquals("2011-8-8 11:0:0", b.toString());
    ++b.day;
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 3:0:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 9:0:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 11:0:0", b.toString());
    ++b.month;
    assertTrue(g.generate(b));
    assertEquals("2011-9-9 3:0:0", b.toString());
  }

  public void testByHourGeneratorGivenDateTime() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 3, 11, 12);
    Generator g = Generators.byHourGenerator(new int[] { 3, 9, 11 }, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 3:11:12", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 9:11:12", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 11:11:12", b.toString());
    assertFalse(g.generate(b));
    assertEquals("2011-8-8 11:11:12", b.toString());
    ++b.day;
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 3:11:12", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 9:11:12", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 11:11:12", b.toString());
    ++b.month;
    assertTrue(g.generate(b));
    assertEquals("2011-9-9 3:11:12", b.toString());
  }

  public void testSingleByHourGeneratorGivenDateTime() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 3, 11, 12);
    Generator g = Generators.byHourGenerator(new int[] { 7 }, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 7:11:12", b.toString());
    ++b.day;
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 7:11:12", b.toString());
    ++b.month;
    assertTrue(g.generate(b));
    assertEquals("2011-9-9 7:11:12", b.toString());
  }

  public void testSerialMinuteGeneratorBigInterval() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 15, 30, 0);
    Generator g = Generators.serialMinuteGenerator(100, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 15:30:0", b.toString());
    assertFalse(g.generate(b));
    assertEquals("2011-8-8 15:30:0", b.toString());
    ++b.hour;
    assertFalse(g.generate(b));
    assertEquals("2011-8-8 16:30:0", b.toString());
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 17:10:0", b.toString());
    assertFalse(g.generate(b));
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 18:50:0", b.toString());
    assertFalse(g.generate(b));
    ++b.hour;
    assertFalse(g.generate(b));
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 20:30:0", b.toString());
    assertFalse(g.generate(b));
    ++b.hour;
    assertFalse(g.generate(b));
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 22:10:0", b.toString());
    assertFalse(g.generate(b));
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 23:50:0", b.toString());
    assertFalse(g.generate(b));
    assertEquals("2011-8-8 23:50:0", b.toString());
    ++b.day;
    b.hour = 0;
    assertFalse(g.generate(b));
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 1:30:0", b.toString());
    b.day += 2;
    b.hour = 6;
    // Skipping over 3:10, 4:50, 6:30, 8:10, 9:50, 11:30, 13:10, 14:50, 16:30,
    //               18:10, 19:50, 21:30, 23:10,
    // on the 9th, and 0:50, 2:30, 4:10, 5:50, 7:30, 9:10, 10:50, 12:30,
    //               14:10, 15:50, 17:30, 19:10, 20:50, 22:30,
    // on the 10th, and 00:10, 1:50, 3:30, 5:10 on the 11th, to 6:50.
    assertTrue(g.generate(b));
    assertEquals("2011-8-11 6:50:0", b.toString());
  }

  public void testSerialMinuteGeneratorSmallInterval() throws Exception {
    DateValue dtStart = new DateValueImpl(2011, 8, 8);
    Generator g = Generators.serialMinuteGenerator(15, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 0:0:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 0:15:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 0:30:0", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 0:45:0", b.toString());
    assertFalse(g.generate(b));
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 1:0:0", b.toString());
  }

  public void testByMinuteGenerator() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 5, 0, 17);
    Generator g = Generators.byMinuteGenerator(
        new int[] { 3, 57, 20, 3 }, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 5:3:17", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 5:20:17", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 5:57:17", b.toString());
    assertFalse(g.generate(b));
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 6:3:17", b.toString());
  }

  public void testSingleByMinuteGenerator() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 5, 30, 17);
    Generator g = Generators.byMinuteGenerator(new int[0], dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 5:30:17", b.toString());
    assertFalse(g.generate(b));
    ++b.hour;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 6:30:17", b.toString());
    assertFalse(g.generate(b));
    b.day += 1;
    assertTrue(g.generate(b));
    assertEquals("2011-8-9 6:30:17", b.toString());
    assertFalse(g.generate(b));
  }

  public void testSerialSecondGenerator() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 19, 1, 23);
    Generator g = Generators.serialSecondGenerator(25, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:1:23", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:1:48", b.toString());
    assertFalse(g.generate(b));
    b.minute += 1;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:2:13", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:2:38", b.toString());
    assertFalse(g.generate(b));
    b.minute += 1;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:3:3", b.toString());
    b.minute += 1;
    // skipped 2:28, 2:53
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:4:18", b.toString());
  }

  public void testBySecondGenerator() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 19, 1, 23);
    Generator g = Generators.bySecondGenerator(
        new int[] { 25, 48, 2 }, dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:1:25", b.toString());
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:1:48", b.toString());
    assertFalse(g.generate(b));
    ++b.minute;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:2:2", b.toString());
  }

  public void testSingleBySecondGenerator() throws Exception {
    DateValue dtStart = new DateTimeValueImpl(2011, 8, 8, 19, 1, 23);
    Generator g = Generators.bySecondGenerator(new int[0], dtStart);
    DTBuilder b = new DTBuilder(dtStart);
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:1:23", b.toString());
    assertFalse(g.generate(b));
    ++b.minute;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:2:23", b.toString());
    assertFalse(g.generate(b));
    ++b.minute;
    assertTrue(g.generate(b));
    assertEquals("2011-8-8 19:3:23", b.toString());
  }
}
