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

package com.google.ical;

import junit.framework.TestSuite;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class AllTests extends TestSuite {

  public AllTests() {
    this.addTestSuite(
        com.google.ical.compat.javautil.DateIteratorFactoryTest.class);
    this.addTestSuite(
        com.google.ical.compat.jodatime.DateTimeIteratorFactoryTest.class);
    this.addTestSuite(
        com.google.ical.compat.jodatime.LocalDateIteratorFactoryTest.class);
    this.addTestSuite(
        com.google.ical.compat.jodatime.TimeZoneConverterTest.class);
    this.addTestSuite(com.google.ical.iter.CompoundIteratorImplTest.class);
    this.addTestSuite(com.google.ical.iter.ConditionsTest.class);
    this.addTestSuite(com.google.ical.iter.DateValueComparisonTest.class);
    this.addTestSuite(com.google.ical.iter.FiltersTest.class);
    this.addTestSuite(com.google.ical.iter.GeneratorsTest.class);
    this.addTestSuite(com.google.ical.iter.IntSetTest.class);
    this.addTestSuite(com.google.ical.iter.MonkeyKeyboardTest.class);
    this.addTestSuite(com.google.ical.iter.RDateIteratorImplTest.class);
    this.addTestSuite(com.google.ical.iter.RRuleIteratorImplTest.class);
    this.addTestSuite(com.google.ical.iter.StressTest.class);
    this.addTestSuite(com.google.ical.iter.UtilTest.class);
    this.addTestSuite(com.google.ical.util.DTBuilderTest.class);
    this.addTestSuite(com.google.ical.values.IcalParseUtilTest.class);
    this.addTestSuite(com.google.ical.values.PeriodValueImplTest.class);
    this.addTestSuite(com.google.ical.values.RDateListTest.class);
    this.addTestSuite(com.google.ical.values.RRuleTest.class);
    this.addTestSuite(com.google.ical.values.VcalRewriterTest.class);
  }

  public static TestSuite suite() { return new AllTests(); }

}
