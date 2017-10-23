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

import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class IntSetTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddAndContainsAndSize() throws Exception {
        IntSet a = new IntSet();
        assertTrue(!a.contains(-2));
        assertTrue(!a.contains(-1));
        assertTrue(!a.contains(0));
        assertTrue(!a.contains(1));
        assertTrue(!a.contains(2));
        assertEquals(0, a.size());

        a.add(1);

        assertTrue(!a.contains(-2));
        assertTrue(!a.contains(-1));
        assertTrue(!a.contains(0));
        assertTrue(a.contains(1));
        assertTrue(!a.contains(2));
        assertEquals(1, a.size());

        a.add(1);

        assertTrue(!a.contains(-2));
        assertTrue(!a.contains(-1));
        assertTrue(!a.contains(0));
        assertTrue(a.contains(1));
        assertTrue(!a.contains(2));
        assertEquals(1, a.size());

        a.add(-2);

        assertTrue(a.contains(-2));
        assertTrue(!a.contains(-1));
        assertTrue(!a.contains(0));
        assertTrue(a.contains(1));
        assertTrue(!a.contains(2));
        assertEquals(2, a.size());
    }

    public void testToIntArray() throws Exception {
        IntSet a = new IntSet();
        assertEquals(0, a.toIntArray().length);

        a.add(17);
        a.add(0);
        a.add(0);
        a.add(-24);
        a.add(-12);
        a.add(4);

        int[] ints = a.toIntArray();
        assertEquals(5, ints.length);
        assertEquals(-24, ints[0]);
        assertEquals(-12, ints[1]);
        assertEquals(0, ints[2]);
        assertEquals(4, ints[3]);
        assertEquals(17, ints[4]);
    }
}
