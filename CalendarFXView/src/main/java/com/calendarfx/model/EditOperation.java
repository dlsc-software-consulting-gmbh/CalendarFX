/**
 * Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */
package com.calendarfx.model;

/**
 * possible edit operations with an entry
 */
public enum EditOperation {

    /**
     * checked if any drag/drop operation is allowed
     */
    DRAG_AND_DROP,

    /**
     * checked if the start of an entry can be changed (e.g. using drag/drop).
     * for drag and drop you also have to allow {@link #DRAG_AND_DROP}
     */
    CHANGE_START,

    /**
     * checked if the end of an entry can be changed (e.g. using drag/drop)
     * for drag and drop you also have to allow {@link #DRAG_AND_DROP}
     */
    CHANGE_END,

    /**
     * checked if entry can be moved using drag/drop
     * for drag and drop you also have to allow {@link #DRAG_AND_DROP}
     */
    MOVE,

    /**
     * checked if an entry can be deleted
     */
    DELETE
}
