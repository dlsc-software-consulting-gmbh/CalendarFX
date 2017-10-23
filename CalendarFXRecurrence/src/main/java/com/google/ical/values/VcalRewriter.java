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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * converts vcal recurrence rules to ical.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class VcalRewriter {

    private static final String DATE = "[0-9]{8,}(?:T[0-9]{6}Z?)?";
    /**
     * <a href="http://www.imc.org/pdi/vcal-10.txt"
     * >http://www.imc.org/pdi/vcal-10.txt</a>
     * <xmp>
     * Grammar
     * {}         0 or more
     * []         0 or 1
     *
     * start           ::= <daily> [<enddate>] |
     *                     <weekly> [<enddate>] |
     *                     <monthlybypos> [<enddate>] |
     *                     <monthlybyday> [<enddate>] |
     *                     <yearlybymonth> [<enddate>] |
     *                     <yearlybyday> [<enddate>]
     * digit           ::= <0|1|2|3|4|5|6|7|8|9>
     * digits          ::= <digit> {<digits>}
     * enddate         ::= ISO 8601_date_time value(e.g., 19940712T101530Z)
     * interval        ::= <digits>
     * duration        ::= #<digits>
     * lastday         ::= LD
     * plus            ::= +
     * minus           ::= -
     * daynumber       ::= <1-31> [<plus>|<minus>]| <lastday>
     * daynumberlist   ::= daynumber {<daynumberlist>}
     * month           ::= <1-12>
     * monthlist       ::= <month> {<monthlist>}
     * day             ::= <1-366>
     * daylist         ::= <day> {<daylist>}
     * occurrence      ::= <1-5><plus> | <1-5><minus>
     * occurrencelist  ::= <occurrence> {<occurrencelist>}
     * weekday         ::= <SU|MO|TU|WE|TH|FR|SA>
     * weekdaylist     ::= <weekday> {<weekdaylist>}
     * daily           ::= D<interval> [<duration>]
     * weekly          ::= W<interval> [<weekdaylist>] [<duration>]
     * monthlybypos    ::= MP<interval> [<occurrencelist> <weekdaylist>]
     *                     [<duration>]
     * monthlybyday    ::= MD<interval> [<daynumberlist>] [<duration>]
     * yearlybymonth   ::= YM<interval> [<monthlist>] [<duration>]
     * yearlybyday     ::= YD<interval> [<daylist>] [<duration>]
     *
     *
     * Glossary
     * enddate         Controls when a repeating event terminates. The enddate is
     *                 the last time an event can occur.
     * interval        Defines the frequency in which a rule repeats.
     * duration        Controls the number of events a rule generates.
     * lastday         Can be used as a replacement to daynumber to indicate the
     *                 last day of the month.
     * daynumber       A number representing a day of the month.
     * month           A number representing a month of the year.
     * day             A number representing a day of the year.
     * occurrence      Controls which week of the month a particular weekday event
     *                 occurs.
     * weekday         A symbol representing a day of the week.
     * daily           Defines a rule that repeats on a daily basis.
     * weekly          Defines a rule that repeats on a weekly basis.
     * monthlybypos    Defines a rule that repeats on a monthly basis on a
     *                 relative day and week.
     * monthlybyday    Defines a rule that repeats on a monthly basis on an
     *                 absolute day.
     * yearlybymonth   Defines a rule that repeats on specific months of the year.
     * yearlybyday     Defines a rule that repeats on specific days of the year.
     *
     *
     * Policies
     * The duration portion of a rule defines the total number of events the rule
     * generates, including the first event.
     * Information, not contained in the rule, necessary to determine the next
     * event time and date is derived from the Start Time entry attribute.
     * If an end date and a duration is specified in the rule, the recurring event
     * ceases when the end date is reached or the number of events indicated in
     * the duration occur; whichever comes first.
     * If the duration or an end date is not established in the rule (e.g., D4)
     * the event occurs twice. That is D4 is equivalent to D4 #2.
     * A duration of #0 means repeat this event forever.
     * Using the occurrence specifier 5+ (e.g. 5th Friday) or 5- (e.g. 5th from
     * last Friday) in a month that does not contain 5 weeks does not generate an
     * event and thus does not count against the duration. The same applies to
     * providing a day of the month that does not occur in the month. For example
     * the 30th or 31st .
     * The start time and date of an entry must be synchronized with one of the
     * repeating events defined by its recurrence rule. The following is not
     * allowed:
     *
     *         Initial Appt Date:        7/1/94  (Friday)
     *         Recurrence Rule:          W1 MO TH #5
     *
     * The following is acceptable:
     *
     *         Initial Appt Date:        7/1/94  (Friday)
     *         Recurrence Rule:          W1 MO FR #5  or W1 #5
     * If the optional <occurrencelist> and <weekdaylist> information is missing
     * from a <monthlybypos> occurrence the information is derived from the entry
     * attributes. The <occurrence> used in the recurring event is a count from
     * the beginning of the month to the entry date and the <weekday> used is the
     * day of the week the entry is scheduled to occur on.
     * </xmp>
     */
    private static final Pattern VCAL_RRULE = Pattern.compile(
            "^"
                    // name and parameters
                    + "("
                    + "(?:RRULE|EXRULE)"
                    + "(?:;[\\w-]+="
                    + "(?:\"[^\"]*\""
                    + "|[^;:\"]*)"
                    + ")*"
                    + ":"
                    + ")"
                    // frequency
                    + "("
                    + "D"                 // daily
                    + "|W"                // weekly
                    + "|M[DP]"            // monthly by day or by position
                    + "|Y[DM]"            // yearly by day or by month
                    + ")"
                    + "([0-9]*)"            // interval
                    // frequency modifier
                    + "("
                    + "(?:\\s+"
                    + "(?:MO|TU|WE|TH|FR|SA|SU|LD|(?:[0-9]{1,3}[+-]?))"
                    + ")*"
                    + ")"
                    // duration
                    + "(?:\\s+"
                    + "(?:"
                    + "#([0-9]+)"       // count
                    + "|(" + DATE + ")" // until
                    + ")"
                    + ")?"
                    + "$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /**
     * rewrite a vcal rrule to an ical rrule.
     * http://www.shuchow.com/vCalAddendum.html
     */
    static String rewriteRule(String vcalText) {
        Matcher m = VCAL_RRULE.matcher(vcalText.trim());
        if (!m.matches()) {
            return vcalText;
        }
        StringBuilder sb = new StringBuilder();
        String nameAndParams = m.group(1),
                freq = m.group(2).toUpperCase(),
                interval = m.group(3),
                modifier = m.group(4).trim().toUpperCase(),
                count = m.group(5),
                until = m.group(6);
        sb.append(nameAndParams);
        Frequency f;
        switch (freq.charAt(0)) {
            case 'Y':
                f = Frequency.YEARLY;
                break;
            case 'M':
                f = Frequency.MONTHLY;
                break;
            case 'W':
                f = Frequency.WEEKLY;
                break;
            case 'D':
                f = Frequency.DAILY;
                break;
            default:
                throw new AssertionError();
        }
        sb.append("FREQ=").append(f.name());
        if (!"".equals(interval) && !"1".equals(interval)) {
            sb.append(";INTERVAL=").append(interval);
        }

        if (!"".equals(modifier)) {
            String[] parts = WHITESPACE.split(modifier);
            for (int i = 0; i < parts.length; ++i) {
                String p = parts[i];
                char lastchar = p.charAt(p.length() - 1);
                switch (lastchar) {
                    case '+':
                        parts[i] = p.substring(0, p.length() - 1);
                        break;
                    case '-':
                        parts[i] = lastchar + p.substring(0, p.length() - 1);
                        break;
                }
                if (p.equals("LD")) {
                    parts[i] = "-1";
                }  // abbrev for last day
            }
            switch (f) {
                case YEARLY:
                    if ('D' == freq.charAt(1)) {
                        sb.append(";BYYEARDAY=");
                        join(sb, ",", parts);
                    } else {
                        sb.append(";BYMONTH=");
                        join(sb, ",", parts);
                    }
                    break;
                case MONTHLY:
                    if ('P' == freq.charAt(1)) {  // byday (position)
                        int pos = 0;
                        boolean comma = false;
                        sb.append(";BYDAY=");
                        for (int i = 0; i < parts.length; ++i) {
                            if (Character.isLetter(parts[i].charAt(0))) {
                                // a day name
                                if (i > pos) {
                                    for (int j = pos; j < i; ++j) {
                                        // week number followed by day of week
                                        if (comma) {
                                            sb.append(',');
                                        } else {
                                            comma = true;
                                        }
                                        sb.append(parts[j]).append(parts[i]);
                                    }
                                } else {
                                    if (comma) {
                                        sb.append(',');
                                    } else {
                                        comma = true;
                                    }
                                    sb.append(parts[i]);
                                }
                                pos = i + 1;
                            }
                        }
                    } else {  // bymonthday
                        sb.append(";BYMONTHDAY=");
                        join(sb, ",", parts);
                    }
                    break;
                case WEEKLY:
                    sb.append(";BYDAY=");
                    join(sb, ",", parts);
                    break;
                default:
            }
        }

        if (null != count) {
            if ("0".equals(count)) {
                // means forever
            } else {
                sb.append(";COUNT=").append(count);
            }
        } else if (null != until) {
            until = until.toUpperCase();
            sb.append(";UNTIL=").append(until);
            // treat as UTC if not already
            if (!until.endsWith("Z") && until.indexOf('T') >= 0) {
                sb.append('Z');
            }
        }
        return sb.toString();
    }

    private static void join(StringBuilder out, String delim, String[] parts) {
        if (0 != parts.length) {
            out.append(parts[0]);
            for (int i = 1; i < parts.length; ++i) {
                out.append(delim).append(parts[i]);
            }
        }
    }

    private VcalRewriter() {
        // uninstantiable
    }

}
