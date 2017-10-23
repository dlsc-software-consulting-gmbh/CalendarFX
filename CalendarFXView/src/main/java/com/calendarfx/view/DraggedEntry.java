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

package com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Dragged entry is used internally only to represent the calendar entry that is
 * currently being dragged. The entry wraps the original entry and stores some
 * additional information about the ongoing drag.
 */
public final class DraggedEntry extends Entry<Object> {

    /**
     * An enum used for defining which drag operation is currently in progress.
     */
    public enum DragMode {

        /**
         * The user is changing the start time of an entry.
         */
        START_TIME,

        /**
         * The user is changing the end time of an entry.
         */
        END_TIME,

        /**
         * The user is dragging the entire entry, hence changing start and end
         * time at the same time.
         */
        START_AND_END_TIME
    }

    private Duration offsetDuration;
    private Entry<?> originalEntry;
    private Calendar originalCalendar;
    private DragMode dragMode;

    /**
     * Constructs a new dragged entry
     *
     * @param entry
     *            the original entry being dragged
     * @param dragMode
     *            the drag mode (start time, end time, or both)
     */
    public DraggedEntry(Entry<?> entry, DragMode dragMode) {
        this.originalEntry = requireNonNull(entry);
        this.originalCalendar = requireNonNull(entry.getCalendar());
        this.dragMode = dragMode;

        setTitle(entry.getTitle());
        setUserObject(entry.getUserObject());
        setFullDay(entry.isFullDay());
        setInterval(entry.getInterval());

        getStyleClass().add("dragged-entry");
    }

    /**
     * Returns the current drag mode (start time, end time, or both).
     *
     * @return the drag mode
     */
    public DragMode getDragMode() {
        return dragMode;
    }

    /**
     * Sets the current drag mode (start time, end time, or both).
     *
     * @param dragMode
     *            the drag mode
     */
    public void setDragMode(DragMode dragMode) {
        requireNonNull(dragMode);
        this.dragMode = dragMode;
    }

    /**
     * Returns the original entry that the user wants to edit.
     *
     * @return the original calendar entry
     */
    public Entry<?> getOriginalEntry() {
        return originalEntry;
    }

    /**
     * Returns the original calendar where the entry is located that is being
     * dragged.
     *
     * @return the calendar where the entry originated from
     */
    public Calendar getOriginalCalendar() {
        return originalCalendar;
    }

    /**
     * Sets the duration between the mouse press location and the start time of
     * the entry.
     *
     * @param duration
     *            the offset duration
     */
    public void setOffsetDuration(Duration duration) {
        this.offsetDuration = duration;
    }

    /**
     * Returns the duration between the mouse press location and the start time
     * of the entry.
     *
     * @return the offset duration
     */
    public Duration getOffsetDuration() {
        return offsetDuration;
    }
}
