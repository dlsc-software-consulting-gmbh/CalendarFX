/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.calendarfx.model;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * An interval tree implementation to store entries based on their start and end
 * time.
 *
 * @param <E>
 *            the entry type
 */
class IntervalTree<E extends Entry<?>> {
    // package private on purpose

    private TreeEntry<E> root;
    private int treeSize;
    private Set<String> entryIDs = new HashSet<>();

    public final Instant getEarliestTimeUsed() {
        if (root != null) {
            return Instant.ofEpochMilli(getEarliestTimeUsed(root));
        }

        return null;
    }

    private long getEarliestTimeUsed(TreeEntry<E> entry) {
        if (entry.getLeft() != null) {
            return getEarliestTimeUsed(entry.getLeft());
        }

        return entry.low;
    }

    public final Instant getLatestTimeUsed() {
        if (root != null) {
            return Instant.ofEpochMilli(getLatestTimeUsed(root));
        }

        return null;
    }

    private long getLatestTimeUsed(TreeEntry<E> entry) {
        if (entry.getRight() != null) {
            return getLatestTimeUsed(entry.getRight());
        }

        return entry.high;
    }

    public final boolean add(E entry) {
        TreeEntry<E> e = addEntry(entry);
        return e != null;
    }

    /**
     * Method to remove period/key object from tree. Entry to delete will be
     * found by period and key values of given parameter p (not by given object
     * reference).
     *
     * @param entry
     *            the entry to remove
     * @return true if the entry was a member of this tree
     */
    public final boolean remove(E entry) {
        TreeEntry<E> e = getEntry(entry);

        if (e == null) {
            return false;
        } else {
            deleteEntry(e);
        }

        return true;
    }

    /**
     * Method to determine if the interval tree contains the given entry.
     *
     * @param entry
     *            the entry to check
     * @return true if the entry is a member of this tree
     */
    public final boolean contains(E entry) {
        TreeEntry<E> e = getEntry(entry);
        return e != null;
    }

    public final Collection<E> removePeriod(Instant start, Instant end) {
        Collection<E> result = getIntersectingObjects(start, end);

        for (E p : result) {
            deleteEntry(getEntry(p));
        }

        return result;
    }

    public final Collection<E> getIntersectingObjects(Instant start,
                                                      Instant end) {
        Collection<E> result = new ArrayList<>();
        if (root == null) {
            return result;
        }

        searchIntersecting(root, new TimeInterval(start, end), result);

        return result;
    }

    private void searchIntersecting(TreeEntry<E> entry,
                                    TimeInterval timeInterval, Collection<E> result) {
        // Don't search nodes that don't exist
        if (entry == null) {
            return;
        }

        long pLow = getLow(timeInterval);
        long pHigh = getHigh(timeInterval);

        // If p is to the right of the rightmost point of any interval
        // in this node and all children, there won't be any matches.
        if (entry.maxHigh < pLow) {
            return;
        }

        // Search left children
        if (entry.left != null) {
            searchIntersecting(entry.left, timeInterval, result);
        }

        // Check this node
        if (checkPLow(entry, pLow) || checkPHigh(entry, pHigh)
                || (pLow <= entry.low && entry.high <= pHigh)) {
            result.add(entry.value);
        }

        // If p is to the left of the start of this interval,
        // then it can't be in any child to the right.
        if (pHigh < entry.low) {
            return;
        }

        // Otherwise, search right children
        if (entry.right != null) {
            searchIntersecting(entry.right, timeInterval, result);
        }
    }

    private boolean checkPLow(TreeEntry<E> n, long pLow) {
        return n.low <= pLow && n.high > pLow;
    }

    private boolean checkPHigh(TreeEntry<E> n, long pHigh) {
        return n.low < pHigh && n.high >= pHigh;
    }

    public final long size() {
        return treeSize;
    }

    public final void clear() {
        treeSize = 0;
        root = null;
    }

    private long getLow(TimeInterval obj) {
        try {
            return obj.getStartTime() == null ? Long.MIN_VALUE
                    : obj.getStartTime().toEpochMilli();
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private long getHigh(TimeInterval interval) {
        try {
            return interval.getEndTime() == null ? Long.MAX_VALUE
                    : interval.getEndTime().toEpochMilli();
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    private long getLow(Entry<?> entry) {
        try {
            return entry.getStartMillis();
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    private long getHigh(Entry<?> entry) {
        try {
            return entry.isRecurring()
                    ? ZonedDateTime.of(entry.getRecurrenceEnd(), LocalTime.MAX,
                    entry.getZoneId()).toInstant().toEpochMilli()
                    : entry.getEndMillis();
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    private void fixUpMaxHigh(TreeEntry<E> entry) {
        while (entry != null) {
            entry.maxHigh = Math.max(entry.high,
                    Math.max(entry.left != null ? entry.left.maxHigh
                                    : Long.MIN_VALUE,
                            entry.right != null ? entry.right.maxHigh
                                    : Long.MIN_VALUE));
            entry = entry.parent;
        }
    }

    /**
     * Method to find entry by period. Period start, period end and object key
     * are used to identify each entry.
     *
     * @param entry the calendar entry
     * @return appropriate entry, or null if not found
     */
    private TreeEntry<E> getEntry(Entry<?> entry) {
        TreeEntry<E> t = root;
        while (t != null) {
            int cmp = compareLongs(getLow(entry), t.low);
            if (cmp == 0)
                cmp = compareLongs(getHigh(entry), t.high);
            if (cmp == 0)
                cmp = entry.hashCode() - t.value.hashCode();

            if (cmp < 0) {
                t = t.left;
            } else if (cmp > 0) {
                t = t.right;
            } else {
                return t;
            }
        }

        return null;
    }

    private TreeEntry<E> addEntry(E entry) {
        Objects.requireNonNull(entry, "null entry is not supported");

        String id = entry.getId();
        if (entryIDs.contains(id)) {
            // TODO: reactivate this check, currently does not work when the start and end time
            // of an entry get changed inside the EntryDetailView (two lambda expressions being evaluated
            // in parallel).
//            throw new IllegalArgumentException("an entry with ID = " + entry.getId() + " was already added to the calendar");
        }

        entryIDs.add(id);

        TreeEntry<E> t = root;
        if (t == null) {
            root = new TreeEntry<>(getLow(entry), getHigh(entry),
                    entry, null);
            treeSize = 1;
            return root;
        }

        long cmp;
        TreeEntry<E> parent;

        do {
            parent = t;
            cmp = compareLongs(getLow(entry), t.low);
            if (cmp == 0) {
                cmp = compareLongs(getHigh(entry), t.high);
                if (cmp == 0)
                    cmp = entry.hashCode() - t.value.hashCode();
            }

            if (cmp < 0) {
                t = t.left;
            } else if (cmp > 0) {
                t = t.right;
            } else {
                return null;
            }
        } while (t != null);

        TreeEntry<E> e = new TreeEntry<>(getLow(entry), getHigh(entry),
                entry, parent);
        if (cmp < 0) {
            parent.left = e;
        } else {
            parent.right = e;
        }

        fixAfterInsertion(e);
        treeSize++;

        return e;
    }

    private static int compareLongs(long val1, long val2) {
        return val1 < val2 ? -1 : (val1 == val2 ? 0 : 1);
    }

    // This part of code was copied from java.util.TreeMap

    // Red-black mechanics

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    /**
     * Internal Entry class.
     *
     * @author koop
     *
     * @param <V>
     */
    private static final class TreeEntry<V> {
        private long low;
        private long high;
        private V value;
        private long maxHigh;
        private TreeEntry<V> left;
        private TreeEntry<V> right;
        private TreeEntry<V> parent;
        private boolean color = BLACK;

        /**
         * Make a new cell with given key, value, and parent, and with
         * <tt>null</tt> child links, and BLACK color.
         */
        TreeEntry(long low, long high, V value, TreeEntry<V> parent) {
            this.low = low;
            this.high = high;
            this.value = value;
            this.parent = parent;
            this.maxHigh = high;
        }

        @Override
        public String toString() {
            return "[" + Instant.ofEpochMilli(low) + " - " //$NON-NLS-1$ //$NON-NLS-2$
                    + Instant.ofEpochMilli(high) + "]=" + value; //$NON-NLS-1$
        }

        public TreeEntry<V> getLeft() {
            return left;
        }

        public TreeEntry<V> getRight() {
            return right;
        }
    }

    /**
     * Returns the successor of the specified Entry, or null if no such.
     *
     * @param <V> the value type
     */
    private static <V> TreeEntry<V> successor(TreeEntry<V> t) {
        if (t == null) {
            return null;
        } else if (t.right != null) {
            TreeEntry<V> p = t.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        } else {
            TreeEntry<V> p = t.parent;
            TreeEntry<V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Balancing operations.
     *
     * Implementations of rebalancings during insertion and deletion are
     * slightly different than the CLR version. Rather than using dummy
     * nilnodes, we use a set of accessors that deal properly with null. They
     * are used to avoid messiness surrounding nullness checks in the main
     * algorithms.
     */

    private static <V> boolean colorOf(TreeEntry<V> p) {
        return p == null ? BLACK : p.color;
    }

    private static <V> TreeEntry<V> parentOf(TreeEntry<V> p) {
        return p == null ? null : p.parent;
    }

    private static <V> void setColor(TreeEntry<V> p, boolean c) {
        if (p != null) {
            p.color = c;
        }
    }

    private static <V> TreeEntry<V> leftOf(TreeEntry<V> p) {
        return (p == null) ? null : p.left;
    }

    private static <V> TreeEntry<V> rightOf(TreeEntry<V> p) {
        return (p == null) ? null : p.right;
    }

    /* From CLR */
    private void rotateLeft(TreeEntry<E> p) {
        if (p != null) {
            TreeEntry<E> r = p.right;
            p.right = r.left;
            if (r.left != null) {
                r.left.parent = p;
            }
            r.parent = p.parent;
            if (p.parent == null) {
                root = r;
            } else if (p.parent.left == p) {
                p.parent.left = r;
            } else {
                p.parent.right = r;
            }
            r.left = p;
            p.parent = r;

            // Original C code:
            // x->maxHigh=ITMax(x->left->maxHigh,ITMax(x->right->maxHigh,x->high))
            // Original C Code:
            // y->maxHigh=ITMax(x->maxHigh,ITMax(y->right->maxHigh,y->high))
            p.maxHigh = Math.max(
                    p.left != null ? p.left.maxHigh : Long.MIN_VALUE,
                    Math.max(p.right != null ? p.right.maxHigh : Long.MIN_VALUE,
                            p.high));
            r.maxHigh = Math.max(p.maxHigh,
                    Math.max(r.right != null ? r.right.maxHigh : Long.MIN_VALUE,
                            r.high));
        }
    }

    /* From CLR */
    private void rotateRight(TreeEntry<E> p) {
        if (p != null) {
            TreeEntry<E> l = p.left;
            p.left = l.right;
            if (l.right != null) {
                l.right.parent = p;
            }
            l.parent = p.parent;
            if (p.parent == null) {
                root = l;
            } else if (p.parent.right == p) {
                p.parent.right = l;
            } else {
                p.parent.left = l;
            }
            l.right = p;
            p.parent = l;

            // Original C code:
            // y->maxHigh=ITMax(y->left->maxHigh,ITMax(y->right->maxHigh,y->high))
            // Original C code:
            // x->maxHigh=ITMax(x->left->maxHigh,ITMax(y->maxHigh,x->high))
            p.maxHigh = Math.max(
                    p.left != null ? p.left.maxHigh : Long.MIN_VALUE,
                    Math.max(p.right != null ? p.right.maxHigh : Long.MIN_VALUE,
                            p.high));
            l.maxHigh = Math.max(p.maxHigh, Math.max(
                    l.left != null ? l.left.maxHigh : Long.MIN_VALUE, l.high));
        }
    }

    /* From CLR */
    private void fixAfterInsertion(TreeEntry<E> x) {

        fixUpMaxHigh(x.parent); // augmented interval tree

        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                TreeEntry<E> y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                TreeEntry<E> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    /**
     * Delete node p, and then rebalance the tree.
     */
    private void deleteEntry(TreeEntry<E> p) {
        entryIDs.remove(p.value.getId());

        treeSize--;

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != null && p.right != null) {
            TreeEntry<E> s = successor(p);
            p.low = s.low;
            p.high = s.high;
            p.value = s.value;
            p.maxHigh = s.maxHigh;
            p = s;
        }      // p has 2 children

        // Start fixup at replacement node, if it exists.
        TreeEntry<E> replacement = p.left != null ? p.left : p.right;

        if (replacement != null) {
            // Link replacement to parent
            replacement.parent = p.parent;
            if (p.parent == null) {
                root = replacement;
            } else if (p == p.parent.left) {
                p.parent.left = replacement;
            } else {
                p.parent.right = replacement;
            }

            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = null;
            p.right = null;
            p.parent = null;

            fixUpMaxHigh(replacement.parent); // augmented interval tree

            // Fix replacement
            if (p.color == BLACK) {
                fixAfterDeletion(replacement);
            }
        } else if (p.parent == null) { // return if we are the only node.
            root = null;
        } else { // No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK) {
                fixAfterDeletion(p);
            }

            if (p.parent != null) {
                if (p == p.parent.left) {
                    p.parent.left = null;
                } else if (p == p.parent.right) {
                    p.parent.right = null;
                }

                fixUpMaxHigh(p.parent); // augmented interval tree

                p.parent = null;
            }
        }
    }

    /* From CLR */
    private void fixAfterDeletion(TreeEntry<E> x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                TreeEntry<E> sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib)) == BLACK
                        && colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
                TreeEntry<E> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK
                        && colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }

    private class TimeInterval {

        private Instant startTime;

        private Instant endTime;

        public TimeInterval(Instant startTime, Instant endTime) {
            requireNonNull(startTime);
            requireNonNull(endTime);

            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException(
                        "start time can not be after end time, start = " //$NON-NLS-1$
                                + startTime + ", end = " + endTime); //$NON-NLS-1$
            }

            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public Instant getEndTime() {
            return endTime;
        }
    }
}
