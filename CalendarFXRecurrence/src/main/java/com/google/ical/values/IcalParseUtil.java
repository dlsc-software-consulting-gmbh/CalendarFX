/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.ical.values;

import com.google.ical.util.DTBuilder;
import com.google.ical.util.TimeUtils;

import java.text.ParseException;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * static functions for parsing ical values.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public final class IcalParseUtil {

    private static final Pattern DATE_VALUE = Pattern.compile(
            "(\\d{4,})(\\d\\d)(\\d\\d)" +
                    "(?:T([0-1]\\d|2[0-3])([0-5]\\d)([0-5]\\d)(Z)?)?");

    /** parses a date of the form yyyymmdd or yyyymmdd'T'hhMMss */
    public static DateValue parseDateValue(String s) throws ParseException {
        return parseDateValue(s, null);
    }

    /**
     * parses a date of the form yyyymmdd or yyyymmdd'T'hhMMss converting from
     * the given timezone to UTC.
     */
    public static DateValue parseDateValue(String s, TimeZone tzid)
            throws ParseException {
        Matcher m = DATE_VALUE.matcher(s);
        if (!m.matches()) {
            throw new ParseException(s, 0);
        }
        int year = Integer.parseInt(m.group(1)),
                month = Integer.parseInt(m.group(2)),
                day = Integer.parseInt(m.group(3));
        if (null != m.group(4)) {
            int hour = Integer.parseInt(m.group(4)),
                    minute = Integer.parseInt(m.group(5)),
                    second = Integer.parseInt(m.group(6));
            boolean utc = null != m.group(7);

            DateValue dv = new DTBuilder(
                    year, month, day, hour, minute, second).toDateTime();
            if (!utc && null != tzid) {
                dv = TimeUtils.toUtc(dv, tzid);
            }
            return dv;
        } else {
            return new DTBuilder(year, month, day).toDate();
        }
    }

    /**
     * parse a period value of the form &lt;start&gt;/&lt;end&gt;.
     * This does not yet recognize the &lt;start&gt;/&lt;duration&gt; form.
     */
    public static PeriodValue parsePeriodValue(String s) throws ParseException {
        return parsePeriodValue(s, null);
    }

    /**
     * parse a period value of the form &lt;start&gt;/&lt;end&gt;, converting
     * from the given timezone to UTC.
     * This does not yet recognize the &lt;start&gt;/&lt;duration&gt; form.
     */
    public static PeriodValue parsePeriodValue(String s, TimeZone tzid)
            throws ParseException {
        int sep = s.indexOf('/');
        if (sep < 0) {
            throw new ParseException(s, s.length());
        }
        DateValue start = parseDateValue(s.substring(0, sep), tzid),
                end = parseDateValue(s.substring(sep + 1), tzid);
        if ((start instanceof TimeValue) != (end instanceof TimeValue)) {
            throw new ParseException(s, 0);
        }
        try {
            return PeriodValueImpl.create(start, end);
        } catch (IllegalArgumentException ex) {
            throw (ParseException) new ParseException(s, sep + 1).initCause(ex);
        }
    }

    /**
     * unfolds ical content lines as per RFC 2445 section 4.1.
     *
     * <h3>4.1 Content Lines</h3>
     *
     * <p>The iCalendar object is organized into individual lines of text, called
     * content lines. Content lines are delimited by a line break, which is a CRLF
     * sequence (US-ASCII decimal 13, followed by US-ASCII decimal 10).
     *
     * <p>Lines of text SHOULD NOT be longer than 75 octets, excluding the line
     * break. Long content lines SHOULD be split into a multiple line
     * representations using a line "folding" technique. That is, a long line can
     * be split between any two characters by inserting a CRLF immediately
     * followed by a single linear white space character (i.e., SPACE, US-ASCII
     * decimal 32 or HTAB, US-ASCII decimal 9). Any sequence of CRLF followed
     * immediately by a single linear white space character is ignored (i.e.,
     * removed) when processing the content type.
     */
    public static String unfoldIcal(String foldedContentLines) {
        return IGNORABLE_ICAL_WHITESPACE.matcher(foldedContentLines).replaceAll("");
    }

    private static final Pattern IGNORABLE_ICAL_WHITESPACE =
            Pattern.compile("(?:\\r\\n?|\\n)[ \t]");

    private IcalParseUtil() {
        // uninstantiable.
    }
}
