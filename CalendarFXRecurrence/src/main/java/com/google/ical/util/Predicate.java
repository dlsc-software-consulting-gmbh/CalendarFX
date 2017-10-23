/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

// CopyrightGoogle Inc. All rights reserved.

package com.google.ical.util;

/**
 * A function with a boolean return value useful for filtering.
 */
public interface Predicate<T> {

    /**
     * Applies this Predicate to the given object.
     *
     * @param t may be null.
     * @return the value of this Predicate when applied to input {@code t}
     */
    boolean apply(T t);
}

