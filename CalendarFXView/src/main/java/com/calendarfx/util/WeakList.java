package com.calendarfx.util;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A simple list wich holds only weak references to the original objects.
 */
public class WeakList<T> extends AbstractList<T> {

    private final ArrayList<WeakReference<T>> items;

    /**
     * Creates new WeakList
     */
    public WeakList() {
        items = new ArrayList<>();
    }

    public WeakList(Collection c) {
        items = new ArrayList();
        addAll(0, c);
    }

    public void add(int index, Object element) {
        items.add(index, new WeakReference(element));
    }

    public Iterator<T> iterator() {
        return new WeakListIterator();
    }

    public int size() {
        removeReleased();
        return items.size();
    }

    public T get(int index) {
        return items.get(index).get();
    }

    private void removeReleased() {
        List<WeakReference<T>> temp = new ArrayList<>(items);
        temp.forEach(ref -> {
            if (ref.get() == null) {
                items.remove(ref);
            }
        });
    }

    private class WeakListIterator implements Iterator<T> {

        private final int n;
        private int i;

        public WeakListIterator() {
            n = size();
            i = 0;
        }

        public boolean hasNext() {
            return i < n;
        }

        public T next() {
            return get(i++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}