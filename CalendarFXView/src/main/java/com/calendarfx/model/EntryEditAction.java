/**
 * Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */
package com.calendarfx.model;

/**
 * class used for parameter of {@link com.calendarfx.view.DateControl#entryActionPolicy} functional interface
 */
public class EntryEditAction {

    /**
     * the entity the operation is operated on
     */
    private final Entry<?> entry;

    /**
     * the operation
     */
    private final EditOperation editOperation;

    public EntryEditAction(Entry<?> entry, EditOperation editOperation) {
        this.entry = entry;
        this.editOperation = editOperation;
    }

    public Entry<?> getEntry() {
        return entry;
    }

    public EditOperation getEditOperation() {
        return editOperation;
    }
}
