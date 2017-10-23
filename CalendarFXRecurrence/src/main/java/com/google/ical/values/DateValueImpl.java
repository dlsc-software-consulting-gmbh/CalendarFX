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
public class DateValueImpl implements DateValue {
    private final int year, month, day;

    public DateValueImpl(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int year() {
        return year;
    }

    public int month() {
        return month;
    }

    public int day() {
        return day;
    }

    @Override
    public String toString() {
        return String.format("%04d%02d%02d", year, month, day);
    }

    public final int compareTo(DateValue other) {
        int n0 = this.day() +               // 5 bits
                (this.month() << 5) +      // 4 bits
                (this.year() << 9);
        int n1 = other.day() +
                (other.month() << 5) +
                (other.year() << 9);
        if (n0 != n1) return n0 - n1;
        if (!(this instanceof TimeValue))
            return (other instanceof TimeValue) ? -1 : 0;

        TimeValue self = (TimeValue) this;
        if (!(other instanceof TimeValue)) return 1;
        TimeValue othr = (TimeValue) other;
        int m0 = self.second() +            // 6 bits
                (self.minute() << 6) +     // 6 bits
                (self.hour() << 12);
        int m1 = othr.second() +
                (othr.minute() << 6) +
                (othr.hour() << 12);
        return m0 - m1;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DateValue)) {
            return false;
        }

        return 0 == this.compareTo((DateValue) o);
    }

    @Override
    public int hashCode() {
        return (this.year << 9) + (this.month << 5) + this.day;
    }
}
