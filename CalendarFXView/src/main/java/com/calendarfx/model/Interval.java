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

package com.calendarfx.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.util.Objects.requireNonNull;

/**
 * A class used for storing the time interval and time zone of an entry.
 *
 * @see Entry#setInterval(Interval)
 */
public final class Interval {

    private static final LocalDate defaultDate = LocalDate.now();

    private static final LocalTime defaultStartTime = LocalTime.of(12, 0);

    private static final LocalTime defaultEndTime = LocalTime.of(13, 0);

    private static final ZoneId defaultZoneId = ZoneId.systemDefault();

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private ZonedDateTime zonedStartDateTime;

    private ZonedDateTime zonedEndDateTime;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private ZoneId zoneId;

    private long startMillis = Long.MIN_VALUE;

    private long endMillis = Long.MAX_VALUE;

    /**
     * Constructs a new time interval with start and end dates equal to
     * {@link LocalDate#now()}. The start and end times will be set to
     * {@link LocalTime#now()} and {@link LocalTime#now()} plus one hour. The
     * time zone will be set to {@link ZoneId#systemDefault()}.
     */
    public Interval() {
        this(defaultDate, defaultStartTime, defaultDate, defaultEndTime, defaultZoneId);
    }

    /**
     * Constructs a new time interval with the given start and end dates /
     * times. The time zone will be initialized with
     * {@link ZoneId#systemDefault()}.
     *
     * @param startDate the start date (e.g. Oct. 3rd, 2015)
     * @param startTime the start time (e.g. 10:45am)
     * @param endDate   the end date
     * @param endTime   the end time
     */
    public Interval(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        this(startDate, startTime, endDate, endTime, defaultZoneId);
    }

    /**
     * Constructs a new time interval with the given start and end dates /
     * times. The time zone will be initialized with
     * {@link ZoneId#systemDefault()}.
     *
     * @param startDateTime the start date and time (e.g. Oct. 3rd, 2015, 6:15pm)
     * @param endDateTime   the end date and time
     */
    public Interval(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this(startDateTime, endDateTime, ZoneId.systemDefault());
    }

    /**
     * Constructs a new time interval with the given start and end zoned dates /
     * times. The time zone will be initialized with the time zone of the start
     * date time. However, if the zone ID of the second argument is different then
     * an exception will be thrown.
     *
     * @throws IllegalArgumentException if two different time zones are used
     *
     * @param zonedStartDateTime the start date and time (e.g. Oct. 3rd, 2015, 6:15pm)
     * @param zonedEndDateTime   the end date and time
     */
    public Interval(ZonedDateTime zonedStartDateTime, ZonedDateTime zonedEndDateTime) {
        this(zonedStartDateTime.toLocalDateTime(), zonedEndDateTime.toLocalDateTime(), zonedStartDateTime.getZone());

        if (!zonedStartDateTime.getZone().equals(zonedEndDateTime.getZone())) {
            throw new IllegalArgumentException("the zoned start and end times use different time zones, zone1 = " + zonedStartDateTime.getZone() +
                    ", zone2 = " + zonedEndDateTime.getZone());
        }
    }

    /**
     * Constructs a new time interval with the given start and end dates /
     * times and time zone.
     *
     * @param startDateTime the start date and time (e.g. Oct. 3rd, 2015, 6:15pm)
     * @param endDateTime   the end date and time
     * @param zoneId        the time zone
     */
    public Interval(LocalDateTime startDateTime, LocalDateTime endDateTime, ZoneId zoneId) {
        this(startDateTime.toLocalDate(), startDateTime.toLocalTime(), endDateTime.toLocalDate(), endDateTime.toLocalTime(), zoneId);
    }

    /**
     * Constructs a new time interval with the given start and end dates / times
     * and time zone.
     *
     * @param startDate the start date (e.g. Oct. 3rd, 2015)
     * @param startTime the start time (e.g. 10:45am)
     * @param endDate   the end date
     * @param endTime   the end time
     * @param zoneId    the time zone
     */
    public Interval(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, ZoneId zoneId) {
        this.startDate = requireNonNull(startDate);
        this.startTime = requireNonNull(startTime);
        this.endDate = requireNonNull(endDate);
        this.endTime = requireNonNull(endTime);
        this.zoneId = requireNonNull(zoneId);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("the start date can never be after the end date");
        }

        /*
         * Now we know that the start date is either earlier than the end date or
         * on the same date.
         */

        if (startDate.equals(endDate)) {

            /*
             * If the start date and the end date are on the same date then we have to make sure that the
             * start time is not after the end time.
             */
            if (getStartTime().isAfter(getEndTime())) {
                throw new IllegalArgumentException("the start time can not be after the end time if both are on the same date");
            }
        }
    }

    /**
     * Returns the time zone ID.
     *
     * @return the time zone
     */
    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Returns the start date of the interval.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Returns the start time of the interval.
     *
     * @return the start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * A convenience method to retrieve a zoned date time based on the start
     * date, start time, and time zone id.
     *
     * @return the zoned start time
     */
    public ZonedDateTime getStartZonedDateTime() {
        if (zonedStartDateTime == null) {
            zonedStartDateTime = ZonedDateTime.of(startDate, startTime, zoneId);
        }

        return zonedStartDateTime;
    }

    /**
     * Returns the start time in milliseconds since 1.1.1970.
     *
     * @return the start time in milliseconds
     */
    public long getStartMillis() {
        if (startMillis == Long.MIN_VALUE) {
            startMillis = getStartZonedDateTime().toInstant().toEpochMilli();
        }

        return startMillis;
    }

    /**
     * Returns the end date of the interval.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Returns the end time of the interval.
     *
     * @return the end time
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * A convenience method to retrieve a zoned date time based on the end date,
     * end time, and time zone id.
     *
     * @return the zoned end time
     */
    public ZonedDateTime getEndZonedDateTime() {
        if (zonedEndDateTime == null) {
            zonedEndDateTime = ZonedDateTime.of(endDate, endTime, zoneId);
        }

        return zonedEndDateTime;
    }

    /**
     * Returns the start time in milliseconds since 1.1.1970.
     *
     * @return the start time in milliseconds
     */
    public long getEndMillis() {
        if (endMillis == Long.MAX_VALUE) {
            endMillis = getEndZonedDateTime().toInstant().toEpochMilli();
        }

        return endMillis;
    }

    /**
     * Returns a new interval based on this interval but with a different start
     * and end date.
     *
     * @param startDate the new start date
     * @param endDate the new end date
     * @return a new interval
     */
    public Interval withDates(LocalDate startDate, LocalDate endDate) {
        requireNonNull(startDate);
        requireNonNull(endDate);
        return new Interval(startDate, this.startTime, endDate, this.endTime, this.zoneId);
    }

    /**
     * Returns a new interval based on this interval but with a different start
     * and end date.
     *
     * @param startDateTime the new start date
     * @param endDateTime the new end date
     * @return a new interval
     */
    public Interval withDates(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        requireNonNull(startDateTime);
        requireNonNull(endDateTime);
        return new Interval(startDateTime, endDateTime, this.zoneId);
    }

    /**
     * Returns a new interval based on this interval but with a different start
     * and end time.
     *
     * @param startTime the new start time
     * @param endTime the new end time
     * @return a new interval
     */
    public Interval withTimes(LocalTime startTime, LocalTime endTime) {
        requireNonNull(startTime);
        requireNonNull(endTime);
        return new Interval(this.startDate, startTime, this.endDate, endTime, this.zoneId);
    }

    /**
     * Returns a new interval based on this interval but with a different start
     * date.
     *
     * @param date the new start date
     * @return a new interval
     */
    public Interval withStartDate(LocalDate date) {
        requireNonNull(date);
        return new Interval(date, startTime, endDate, endTime, zoneId);
    }

    /**
     * Returns a new interval based on this interval but with a different end
     * date.
     *
     * @param date the new end date
     * @return a new interval
     */
    public Interval withEndDate(LocalDate date) {
        requireNonNull(date);
        return new Interval(startDate, startTime, date, endTime, zoneId);
    }

    /**
     * Returns a new interval based on this interval but with a different start
     * time.
     *
     * @param time the new start time
     * @return a new interval
     */
    public Interval withStartTime(LocalTime time) {
        requireNonNull(time);
        return new Interval(startDate, time, endDate, endTime, zoneId);
    }

    /**
     * Returns a new interval based on this interval but with a different start date
     * and time.
     *
     * @param dateTime the new start date and time
     * @return a new interval
     */
    public Interval withStartDateTime(LocalDateTime dateTime) {
        requireNonNull(dateTime);
        return new Interval(dateTime.toLocalDate(), dateTime.toLocalTime(), endDate, endTime);
    }

    /**
     * Returns a new interval based on this interval but with a different end
     * time.
     *
     * @param time the new end time
     * @return a new interval
     */
    public Interval withEndTime(LocalTime time) {
        requireNonNull(time);
        return new Interval(startDate, startTime, endDate, time, zoneId);
    }

    /**
     * Returns a new interval based on this interval but with a different end
     * date and time.
     *
     * @param dateTime the new end date and time
     * @return a new interval
     */
    public Interval withEndDateTime(LocalDateTime dateTime) {
        requireNonNull(dateTime);
        return new Interval(startDate, startTime, dateTime.toLocalDate(), dateTime.toLocalTime());
    }

    /**
     * Returns a new interval based on this interval but with a different time
     * zone id.
     *
     * @param zone the new time zone
     * @return a new interval
     */
    public Interval withZoneId(ZoneId zone) {
        requireNonNull(zone);
        return new Interval(startDate, startTime, endDate, endTime, zone);
    }

    /**
     * Utility method to get the local start date time. This method combines the
     * start date and the start time to create a date time object.
     *
     * @return the start local date time
     * @see #getStartDate()
     * @see #getStartTime()
     */
    public LocalDateTime getStartDateTime() {
        if (startDateTime == null) {
            startDateTime = LocalDateTime.of(getStartDate(), getStartTime());
        }

        return startDateTime;
    }

    /**
     * Returns the duration of this interval.
     *
     * @return the duration between the zoned start and end date and time
     */
    public Duration getDuration() {
        return Duration.between(getStartZonedDateTime(), getEndZonedDateTime());
    }

    /**
     * Utility method to get the local end date time. This method combines the
     * end date and the end time to create a date time object.
     *
     * @return the end local date time
     * @see #getEndDate()
     * @see #getEndTime()
     */
    public LocalDateTime getEndDateTime() {
        if (endDateTime == null) {
            endDateTime = LocalDateTime.of(getEndDate(), getEndTime());
        }

        return endDateTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + ((zoneId == null) ? 0 : zoneId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Interval other = (Interval) obj;
        if (endDate == null) {
            if (other.endDate != null)
                return false;
        } else if (!endDate.equals(other.endDate))
            return false;
        if (endTime == null) {
            if (other.endTime != null)
                return false;
        } else if (!endTime.equals(other.endTime))
            return false;
        if (startDate == null) {
            if (other.startDate != null)
                return false;
        } else if (!startDate.equals(other.startDate))
            return false;
        if (startTime == null) {
            if (other.startTime != null)
                return false;
        } else if (!startTime.equals(other.startTime))
            return false;
        if (zoneId == null) {
            if (other.zoneId != null)
                return false;
        } else if (!zoneId.equals(other.zoneId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Interval [startDate=" + startDate + ", endDate=" + endDate //$NON-NLS-1$ //$NON-NLS-2$
                + ", startTime=" + startTime + ", endTime=" + endTime //$NON-NLS-1$ //$NON-NLS-2$
                + ", zoneId=" + zoneId + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
