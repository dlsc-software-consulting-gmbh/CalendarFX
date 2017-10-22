/**
 * Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */
package com.calendarfx.model;

import com.calendarfx.view.DateControl;

/**
 * Class used for parameter of {@link com.calendarfx.view.DateControl#entryEditPolicy} functional interface
 */
public final class EntryEditParameter {

    /**
     * The date control the entity is associated with
     */
    private final DateControl dateControl;

    /**
     * The entity the operation is operated on
     */
    private final Entry<?> entry;

    /**
     * The operation
     */
    private final DateControl.EditOperation editOperation;

    public EntryEditParameter(DateControl dateControl, Entry<?> entry, DateControl.EditOperation editOperation) {
        this.dateControl = dateControl;
        this.entry = entry;
        this.editOperation = editOperation;
    }

    /**
     * The {@link DateControl} which is asking for a specific {@link com.calendarfx.view.DateControl.EditOperation} permission.
     */
    public DateControl getDateControl() {
        return dateControl;
    }

    /**
     * The entry where the {@link com.calendarfx.view.DateControl.EditOperation} should be applied
     */
    public Entry<?> getEntry() {
        return entry;
    }

    /**
     * The actual edit operation
     */
    public DateControl.EditOperation getEditOperation() {
        return editOperation;
    }
}
