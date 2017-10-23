/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

package com.google.ical.values;

/**
 * A calendar date.
 *
 * @author Neal Gafter
 */
public interface DateValue extends Comparable<DateValue> {

    /** The Gregorian year, for example 2004. */
    int year();

    /** The Gregorian month, in the range 1-12. */
    int month();

    /** The Gregorian day of the month, in the range 1-31. */
    int day();
}
