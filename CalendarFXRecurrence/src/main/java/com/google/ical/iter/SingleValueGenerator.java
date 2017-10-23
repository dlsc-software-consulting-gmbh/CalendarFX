/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

package com.google.ical.iter;

/**
 * A marker for Generators that generate exactly one value per
 * outer cycle.  For example, BYHOUR=3 generates exactly one hour per day and
 * BYMONTHDAY=12 generates exactly one day per month but BYHOUR=3,6 does not
 * nor does BYMONTHDAY=31.
 */
abstract class SingleValueGenerator extends Generator {
    abstract int getValue();
}
