/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import java.time.ZonedDateTime;

/**
 * An interface that needs to be implemented by those date controls that want to
 * support the creation of new entries with a double click. For this to work
 * they need to supply the time at the given click location.
 */
public interface ZonedDateTimeProvider {

	/**
	 * Returns the time at the given location.
	 *
	 * @param x
	 *            the x coordinate of the input event
	 * @param y
	 *            the y coordinate of the input event
	 * @return the time at the given location
	 */
	ZonedDateTime getZonedDateTimeAt(double x, double y);
}
