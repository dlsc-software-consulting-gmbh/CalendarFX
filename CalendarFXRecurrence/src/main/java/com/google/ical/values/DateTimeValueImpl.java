/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

package com.google.ical.values;

/**
 * An instant in time.
 *
 * @author Neal Gafter
 */
public class DateTimeValueImpl
        extends DateValueImpl
        implements DateTimeValue {
    private final int hour, minute, second;

    public DateTimeValueImpl(int year, int month, int day,
                             int hour, int minute, int second) {
        super(year, month, day);
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public int hour() {
        return hour;
    }

    public int minute() {
        return minute;
    }

    public int second() {
        return second;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^
                ((this.hour << 12) + (this.minute << 6) + this.second);
    }

    @Override
    public String toString() {
        return String.format("%sT%02d%02d%02d",
                super.toString(),
                hour, minute, second);
    }
}





