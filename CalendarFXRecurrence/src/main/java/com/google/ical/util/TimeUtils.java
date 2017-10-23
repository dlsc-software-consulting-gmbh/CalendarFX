/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

package com.google.ical.util;

import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.TimeValue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for working with times and dates.
 *
 * @author Neal Gafter
 */
public class TimeUtils {

    private static TimeZone ZULU = new SimpleTimeZone(0, "Etc/GMT");

    public static TimeZone utcTimezone() {
        return ZULU;
    }

    /**
     * Get a "time_t" in millis given a number of seconds since
     * Dershowitz/Reingold epoch relative to a given timezone.
     * @param epochSecs Number of seconds since Dershowitz/Reingold
     * epoch, relatve to zone.
     * @param zone Timezone against which epochSecs applies
     * @return Number of milliseconds since 00:00:00 Jan 1, 1970 GMT
     */
    private static long timetMillisFromEpochSecs(long epochSecs,
                                                 TimeZone zone) {
        DateTimeValue date = timeFromSecsSinceEpoch(epochSecs);
        Calendar cal = new GregorianCalendar(zone);
        cal.clear(); // clear millis
        cal.setTimeZone(zone);
        cal.set(date.year(), date.month() - 1, date.day(),
                date.hour(), date.minute(), date.second());
        return cal.getTimeInMillis();
    }

    private static DateTimeValue convert(DateTimeValue time,
                                         TimeZone zone,
                                         int sense) {
        if (zone == null ||
                zone.hasSameRules(ZULU) ||
                time.year() == 0) {
            return time;
        }

        long timetMillis = 0;

        if (sense > 0) {
            // time is in UTC
            timetMillis = timetMillisFromEpochSecs(secsSinceEpoch(time), ZULU);
        } else {
            // time is in local time; since zone.getOffset() expects millis
            // in UTC, need to convert before we can get the offset (ironic)
            timetMillis = timetMillisFromEpochSecs(secsSinceEpoch(time), zone);
        }

        int millisecondOffset = zone.getOffset(timetMillis);
        int millisecondRound = millisecondOffset < 0 ? -500 : 500;
        int secondOffset = (millisecondOffset + millisecondRound) / 1000;
        return addSeconds(time, sense * secondOffset);
    }

    public static DateValue fromUtc(DateValue date, TimeZone zone) {
        return (date instanceof DateTimeValue)
                ? fromUtc((DateTimeValue) date, zone)
                : date;
    }

    public static DateTimeValue fromUtc(DateTimeValue date, TimeZone zone) {
        return convert(date, zone, +1);
    }

    public static DateValue toUtc(DateValue date, TimeZone zone) {
        return (date instanceof TimeValue)
                ? convert((DateTimeValue) date, zone, -1)
                : date;
    }

    private static DateTimeValue addSeconds(DateTimeValue dtime, int seconds) {
        return new DTBuilder(dtime.year(), dtime.month(),
                dtime.day(), dtime.hour(),
                dtime.minute(),
                dtime.second() + seconds).toDateTime();
    }

    public static DateValue add(DateValue d, DateValue dur) {
        DTBuilder db = new DTBuilder(d);
        db.year += dur.year();
        db.month += dur.month();
        db.day += dur.day();
        if (dur instanceof TimeValue) {
            TimeValue tdur = (TimeValue) dur;
            db.hour += tdur.hour();
            db.minute += tdur.minute();
            db.second += tdur.second();
            return db.toDateTime();
        } else if (d instanceof TimeValue) {
            return db.toDateTime();
        }
        return db.toDate();
    }

    /**
     * the number of days between two dates.
     *
     * @param dv1 non null.
     * @param dv2 non null.
     * @return a number of days.
     */
    public static int daysBetween(DateValue dv1, DateValue dv2) {
        return fixedFromGregorian(dv1) - fixedFromGregorian(dv2);
    }

    public static int daysBetween(
            int y1, int m1, int d1,
            int y2, int m2, int d2) {
        return fixedFromGregorian(y1, m1, d1) - fixedFromGregorian(y2, m2, d2);
    }


    private static int fixedFromGregorian(DateValue date) {
        return fixedFromGregorian(date.year(), date.month(), date.day());
    }

    /**
     * the number of days since the <em>epoch</em>,
     * which is the imaginary beginning of year zero in a hypothetical
     * backward extension of the Gregorian calendar through time.
     * See "Calendrical Calculations" by Reingold and Dershowitz.
     */
    public static int fixedFromGregorian(int year, int month, int day) {
        int yearM1 = year - 1;
        return 365 * yearM1 +
                yearM1 / 4 -
                yearM1 / 100 +
                yearM1 / 400 +
                (367 * month - 362) / 12 +
                (month <= 2 ? 0 :
                        isLeapYear(year) ? -1 :
                                -2) +
                day;
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
    }

    /** count of days inthe given year */
    public static int yearLength(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    /** count of days in the given month (one indexed) of the given year. */
    public static int monthLength(int year, int month) {
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                return isLeapYear(year) ? 29 : 28;
            default:
                throw new AssertionError(month);
        }
    }

    private static int[] MONTH_START_TO_DOY = new int[12];

    static {
        assert !isLeapYear(1970);
        for (int m = 1; m < 12; ++m) {
            MONTH_START_TO_DOY[m] = MONTH_START_TO_DOY[m - 1] + monthLength(1970, m);
        }
        assert 365 == MONTH_START_TO_DOY[11] + monthLength(1970, 12) :
                "" + (MONTH_START_TO_DOY[11] + monthLength(1970, 12));
    }

    /** the day of the year in [0-365] of the given date. */
    public static int dayOfYear(int year, int month, int date) {
        int leapAdjust = month > 2 && isLeapYear(year) ? 1 : 0;
        return MONTH_START_TO_DOY[month - 1] + leapAdjust + date - 1;
    }

    /**
     * Compute the gregorian time from the number of seconds since the
     * Proleptic Gregorian Epoch.
     * See "Calendrical Calculations", Reingold and Dershowitz.
     */
    public static DateTimeValue timeFromSecsSinceEpoch(long secsSinceEpoch) {
        // TODO: should we handle -ve years?
        int secsInDay = (int) (secsSinceEpoch % SECS_PER_DAY);
        int daysSinceEpoch = (int) (secsSinceEpoch / SECS_PER_DAY);
        int approx = (int) ((daysSinceEpoch + 10) * 400L / 146097);
        int year = (daysSinceEpoch >= fixedFromGregorian(approx + 1, 1, 1))
                ? approx + 1 : approx;
        int jan1 = fixedFromGregorian(year, 1, 1);
        int priorDays = daysSinceEpoch - jan1;
        int march1 = fixedFromGregorian(year, 3, 1);
        int correction = (daysSinceEpoch < march1) ? 0 :
                isLeapYear(year) ? 1 : 2;
        int month = (12 * (priorDays + correction) + 373) / 367;
        int month1 = fixedFromGregorian(year, month, 1);
        int day = daysSinceEpoch - month1 + 1;
        int second = secsInDay % 60;
        int minutesInDay = secsInDay / 60;
        int minute = minutesInDay % 60;
        int hour = minutesInDay / 60;
        if (!(hour >= 0 && hour < 24)) throw new AssertionError(
                "Input was: " + secsSinceEpoch + "to make hour: " + hour);
        DateTimeValue result =
                new DateTimeValueImpl(year, month, day, hour, minute, second);
        // assert result.equals(normalize(result));
        // assert secsSinceEpoch(result) == secsSinceEpoch;
        return result;
    }

    private static final long SECS_PER_DAY = 60L * 60 * 24;

    /**
     * Compute the number of seconds from the Proleptic Gregorian epoch
     * to the given time.
     */
    public static long secsSinceEpoch(DateValue date) {
        long result = fixedFromGregorian(date) *
                SECS_PER_DAY;
        if (date instanceof TimeValue) {
            TimeValue time = (TimeValue) date;
            result +=
                    time.second() +
                            60 * (time.minute() +
                                    60 * time.hour());
        }
        return result;
    }

    public static DateTimeValue dayStart(DateValue dv) {
        return new DateTimeValueImpl(dv.year(), dv.month(), dv.day(), 0, 0, 0);
    }

    /**
     * a DateValue with the same year, month, and day as the given instance that
     * is not a TimeValue.
     */
    public static DateValue toDateValue(DateValue dv) {
        return (!(dv instanceof TimeValue) ? dv
                : new DateValueImpl(dv.year(), dv.month(), dv.day()));
    }

    private static final TimeZone BOGUS_TIMEZONE =
            TimeZone.getTimeZone("noSuchTimeZone");

    private static final Pattern UTC_TZID =
            Pattern.compile("^GMT([+-]0(:00)?)?$|UTC|Zulu|Etc\\/GMT|Greenwich.*",
                    Pattern.CASE_INSENSITIVE);

    /**
     * returns the timezone with the given name or null if no such timezone.
     * calendar/common/ICalUtil uses this function
     */
    public static TimeZone timeZoneForName(String tzString) {
        // This is a horrible hack since there is no easier way to get a timezone
        // only if the string is recognized as a timezone.
        // The TimeZone.getTimeZone javadoc says the following:
        //   Returns:
        //       the specified TimeZone, or the GMT zone if the given ID cannot be
        //       understood.
        TimeZone tz = TimeZone.getTimeZone(tzString);
        if (tz.hasSameRules(BOGUS_TIMEZONE)) {
            // see if the user really was asking for GMT because if
            // TimeZone.getTimeZone can't recognize tzString, then that is what it
            // will return.
            Matcher m = UTC_TZID.matcher(tzString);
            if (m.matches()) {
                return TimeUtils.utcTimezone();
            }
            // unrecognizable timezone
            return null;
        }
        return tz;
    }

    private TimeUtils() {
        // uninstantiable
    }

}
