/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

package com.google.ical.values;

/**
 * A time of day.
 *
 * @author Neal Gafter
 */
public interface TimeValue {

    /** The hour in the range 0 through 24. */
    int hour();

    /**
     * The minute in the range 0 through 59.
     * If hour()==24, then minute() == 0
     */
    int minute();

    /**
     * The second in the range 0 through 59.
     * If hour()==24, then second() == 0.
     */
    int second();
}
