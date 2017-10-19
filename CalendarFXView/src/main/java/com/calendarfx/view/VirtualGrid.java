/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import impl.com.calendarfx.view.util.Util;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static java.util.Objects.requireNonNull;

/**
 * A virtual grid used to make entries snap to virtual grid lines while being
 * edited.
 *
 * @see DateControl#setVirtualGrid(VirtualGrid)
 */
public class VirtualGrid {

	/**
	 * The virtual grid in {@link DateControl} can never be null, hence we need
	 * a virtual grid that does nothing, the OFF grid.
	 *
	 * @see DateControl#setVirtualGrid(VirtualGrid)
	 */
	public static final VirtualGrid OFF = new VirtualGrid(Messages.getString("VirtualGrid.OFF"), Messages.getString("VirtualGrid.OFF_SHORT"), //$NON-NLS-1$ //$NON-NLS-2$
			ChronoUnit.SECONDS, 1) {

		@Override
		public Instant adjustTime(Instant instant, ZoneId zoneId,
				boolean roundUp, DayOfWeek firstDayOfWeek) {
			return instant;
		}

		@Override
		public LocalDateTime adjustTime(LocalDateTime time, boolean roundUp,
				DayOfWeek firstDayOfWeek) {
			return time;
		}

		@Override
		public ZonedDateTime adjustTime(ZonedDateTime time, boolean roundUp,
				DayOfWeek firstDayOfWeek) {
			return time;
		}
	};

	private String name;
	private String shortName;
	private ChronoUnit unit;
	private int amount;

	/**
	 * Constructs a new virtual grid.
	 *
	 * @param name
	 *            the name of the grid (e.g. "15 Minutes")
	 * @param shortName
	 *            the short name of the grid (e.g. "15 Min.")
	 * @param unit
	 *            the time unit of the grid (seconds, minutes, hours)
	 * @param amount
	 *            the amount of the unit (5, 10, 15 minutes)
	 */
	public VirtualGrid(String name, String shortName, ChronoUnit unit,
			int amount) {
		this.name = requireNonNull(name);
		this.shortName = requireNonNull(shortName);
		this.unit = requireNonNull(unit);

		if (amount <= 0) {
			throw new IllegalArgumentException(
					"grid amount must be larger than 0 but was " + amount); //$NON-NLS-1$
		}

		this.amount = amount;
	}

	/**
	 * Returns the grid name that can be used for grid selection controls.
	 *
	 * @return the name of the grid settings
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the grid short name that can be used for grid selection controls.
	 *
	 * @return the short name of the grid settings
	 */
	public final String getShortName() {
		return shortName;
	}

	/**
	 * The temporal unit used for the grid.
	 *
	 * @return the temporal unit of the grid
	 */
	public final ChronoUnit getUnit() {
		return unit;
	}

	/**
	 * The number of units used for the grid.
	 *
	 * @return the number of units
	 */
	public final int getAmount() {
		return amount;
	}

	/**
	 * Adjusts the given time either rounding it up or down.
	 *
	 * @param time
	 *            the time to adjust
	 * @param roundUp
	 *            the rounding direction
	 * @param firstDayOfWeek
	 *            the first day of the week (needed for rounding weeks)
	 * @return the adjusted time
	 */
	public ZonedDateTime adjustTime(ZonedDateTime time, boolean roundUp,
			DayOfWeek firstDayOfWeek) {
		Instant instant = time.toInstant();
		ZoneId zoneId = time.getZone();
		instant = adjustTime(instant, zoneId, roundUp, firstDayOfWeek);
		return ZonedDateTime.ofInstant(instant, zoneId);
	}

	/**
	 * Adjusts the given instant either rounding it up or down.
	 *
	 * @param instant
	 *            the instant to adjust
	 * @param zoneId
	 *            the time zone
	 * @param roundUp
	 *            the rounding direction
	 * @param firstDayOfWeek
	 *            the first day of the week (needed for rounding weeks)
	 * @return the adjusted instant
	 */
	public Instant adjustTime(Instant instant, ZoneId zoneId, boolean roundUp,
			DayOfWeek firstDayOfWeek) {

		requireNonNull(instant);
		requireNonNull(zoneId);
		requireNonNull(firstDayOfWeek);

		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
		if (roundUp) {
			zonedDateTime = zonedDateTime.plus(getAmount(), getUnit());
		}

		zonedDateTime = Util.truncate(zonedDateTime, getUnit(), getAmount(),
				firstDayOfWeek);

		return Instant.from(zonedDateTime);
	}

	/**
	 * Adjusts the given time either rounding it up or down.
	 *
	 * @param time
	 *            the time to adjust
	 * @param roundUp
	 *            the rounding direction
	 * @param firstDayOfWeek
	 *            the first day of the week (needed for rounding weeks)
	 * @return the adjusted time
	 */
	public LocalDateTime adjustTime(LocalDateTime time, boolean roundUp,
			DayOfWeek firstDayOfWeek) {
		requireNonNull(time);

		if (roundUp) {
			time = time.plus(getAmount(), getUnit());
		}

		return Util.truncate(time, getUnit(), getAmount(), firstDayOfWeek);
	}

	@Override
	public String toString() {
		return "VirtualGrid [name=" + name + ", shortName=" + shortName //$NON-NLS-1$ //$NON-NLS-2$
				+ ", unit=" + unit + ", amount=" + amount + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
