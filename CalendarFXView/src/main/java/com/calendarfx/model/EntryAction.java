/**
 * Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */
package com.calendarfx.model;

/**
 * configures the allowed actions with a given entry
 */
public class EntryAction {

    /**
     * return true if drag/drop move of entry should be allowed
     */
    public boolean allowMove(Entry<?> entry) {
        return true;
    }

    /**
     * return true if drag/drop move of the start of the entry should be allowed
     */
    public boolean allowChangeStart(Entry<?> entry) {
        return true;
    }

    /**
     * return true if drag/drop move of the end of the entry should be allowed
     */
    public boolean allowChangeEnd(Entry<?> entry) {
        return true;
    }

    /**
     * return true if delete of the entry should be allowed
     */
    public boolean allowDelete(Entry<?> entry) {
        return true;
    }

    /**
     * return true if any drag/drop operation should be allowed.
     * if any of allowChangeStart, allowChangeEnd or allowMove returns true, this method also returns true
     */
    public boolean allowAnyDnD(Entry entry) {
        return allowChangeStart(entry) || allowChangeEnd(entry) || allowMove(entry);
    }
}
