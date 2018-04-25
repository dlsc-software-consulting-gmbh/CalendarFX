/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

// Copyright (C) 2008 Google Inc.
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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class RRuleTest extends TestCase {

    /**
     * Test that the RRule parser is re-entrant.
     * See http://code.google.com/p/google-rfc-2445/issues/detail?id=2
     */
    public void testThreadSafety() throws Throwable {
        int nThreads = 10;
        final int nRuns = 10000;

        final List<Throwable> errorList
                = Collections.synchronizedList(new ArrayList<>());

        Runnable r = () -> {
            RRule rule = null;
            for (int i = nRuns; --i >= 0; ) {
                try {
                    rule = new RRule(
                            "RRULE:FREQ=MONTHLY;INTERVAL=2;BYDAY=FR;BYSETPOS=-1");
                } catch (Error err) {
                    throw err;
                } catch (Throwable th) {
                    System.err.println("At run " + i + ", " + rule);
                    errorList.add(th);
                }
            }
        };

        // Run once to make sure all static data initialized is shared by child
        // threads.
        r.run();

        Thread[] threads = new Thread[nThreads];
        for (int i = 0; i < threads.length; ++i) {
            (threads[i] = new Thread(r)).setDaemon(true);
        }

        synchronized (errorList) {
            for (Thread th : threads) {
                th.start();
            }
        }

        for (Thread th : threads) {
            try {
                th.join();
            } catch (InterruptedException ex) {
                errorList.add(ex);
            }
        }

        if (!errorList.isEmpty()) {
            // ConcurrentModificationException possible if all threads not halted.
            for (Throwable th : errorList) {
                th.printStackTrace();
            }

            throw errorList.get(0);
        }
    }
}
