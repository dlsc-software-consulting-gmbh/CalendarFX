/**
 * Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */
package com.calendarfx.model;

public enum DragDropReschedulePolicy {
    /**
     * Allow any drag drop operation. The default.
     */
    ANY(true, true, true),

    /**
     * Allow no drag drop operation
     */
    CHANGE_NOTHING(false, false, false),

    /**
     * Allow no drag drop operation
     */
    MOVE(false, false, true),

    /**
     * Allow dragging the start of the schedule
     */
    CHANGE_SCHEDULE_START(true, false, false),

    /**
     * Allow dragging the end of the schedule
     */
    CHANGE_SCHEDULE_END(false, true, false);

    private final boolean allowChangeStart;
    private final boolean allowChangeEnd;
    private final boolean allowMove;

    DragDropReschedulePolicy(boolean allowChangeStart, boolean allowChangeEnd, boolean allowMove) {
        this.allowChangeStart = allowChangeStart;
        this.allowChangeEnd = allowChangeEnd;
        this.allowMove = allowMove;
    }

    public boolean isAllowChangeStart() {
        return allowChangeStart;
    }

    public boolean isAllowChangeEnd() {
        return allowChangeEnd;
    }

    public boolean isAllowMove() {
        return allowMove;
    }

    public boolean isNone() {
        return !allowChangeStart && !allowChangeEnd && !allowMove;
    }
}
