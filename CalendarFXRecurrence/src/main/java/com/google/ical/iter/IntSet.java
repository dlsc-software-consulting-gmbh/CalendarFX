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

import java.util.BitSet;

/**
 * a set of integers in a small range.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class IntSet {

    BitSet ints = new BitSet();

    void add(int n) {
        ints.set(encode(n));
    }

    int encode(int n) {
        return n < 0 ? ((-n << 1) + 1) : (n << 1);
    }

    int decode(int i) {
        return (i >>> 1) * (-(i & 1) | 1);
    }

    boolean contains(int n) {
        return ints.get(encode(n));
    }

    int size() {
        return ints.cardinality();
    }

    int[] toIntArray() {
        int[] out = new int[ints.cardinality()];
        int a = 0, b = out.length;
        for (int i = -1; (i = ints.nextSetBit(i + 1)) >= 0; ) {
            int n = decode(i);
            if (n < 0) {
                out[a++] = n;
            } else {
                out[--b] = n;
            }
        }
        // if it contains  -3, -1, 0, 1, 2, 4
        // Then out will be -1, -3, 4, 2, 1, 0
        reverse(out, 0, a);
        reverse(out, a, out.length);

        return out;
    }

    private static void reverse(int[] arr, int s, int e) {
        for (int i = s, j = e; i < --j; ++i) {
            int t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
    }

}
