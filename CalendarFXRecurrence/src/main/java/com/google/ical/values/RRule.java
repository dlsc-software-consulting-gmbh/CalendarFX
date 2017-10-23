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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * encapsulates an RFC 2445 RRULE or EXRULE.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class RRule extends AbstractIcalObject {

    private Frequency freq;
    private Weekday wkst;
    private DateValue until;
    private int count;
    private int interval;
    private List<WeekdayNum> byDay = new ArrayList<WeekdayNum>();
    private int[] byMonth = NO_INTS;  // in +/-[1-12]
    private int[] byMonthDay = NO_INTS;  // in +/-[1-31]
    private int[] byWeekNo = NO_INTS;  // in +/-[1-53]
    private int[] byYearDay = NO_INTS;  // in +/-[1-366]
    private int[] byHour = NO_INTS;  // in [0-23]
    private int[] byMinute = NO_INTS;  // in [0-59]
    private int[] bySecond = NO_INTS;  // in [0-60]
    private int[] bySetPos = NO_INTS;  // in +/-[1-366]

    private static final int[] NO_INTS = new int[0];

    public RRule() {
        this.freq = Frequency.DAILY;
        setName("RRULE");
    }

    public RRule(String icalString) throws ParseException {
        parse(VcalRewriter.rewriteRule(icalString), RRuleSchema.instance());
    }

    /**
     * formats as an *unfolded* RFC 2445 content line.
     */
    public String toIcal() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getName());
        if (hasExtParams()) {
            for (Map.Entry<String, String> param : getExtParams().entrySet()) {
                String k = param.getKey(),
                        v = param.getValue();
                if (ICAL_SPECIALS.matcher(v).find()) {
                    v = "\"" + v + "\"";
                }
                buf.append(';').append(k).append('=').append(v);
            }
        }
        buf.append(":FREQ=").append(freq);
        if (null != wkst) {
            buf.append(";WKST=").append(wkst.toString());
        }
        if (null != this.until) {
            buf.append(";UNTIL=").append(until);
            if (until instanceof TimeValue) {
                buf.append('Z');
            }
        }
        if (count != 0) {
            buf.append(";COUNT=").append(count);
        }
        if (interval != 0) {
            buf.append(";INTERVAL=").append(interval);
        }
        if (0 != byYearDay.length) {
            buf.append(";BYYEARDAY=");
            writeIntList(byYearDay, buf);
        }
        if (0 != byMonth.length) {
            buf.append(";BYMONTH=");
            writeIntList(byMonth, buf);
        }
        if (0 != byMonthDay.length) {
            buf.append(";BYMONTHDAY=");
            writeIntList(byMonthDay, buf);
        }
        if (0 != byWeekNo.length) {
            buf.append(";BYWEEKNO=");
            writeIntList(byWeekNo, buf);
        }
        if (!byDay.isEmpty()) {
            buf.append(";BYDAY=");
            boolean first = true;
            for (WeekdayNum day : this.byDay) {
                if (!first) {
                    buf.append(',');
                } else {
                    first = false;
                }
                buf.append(day);
            }
        }
        if (0 != byHour.length) {
            buf.append(";BYHOUR=");
            writeIntList(byHour, buf);
        }
        if (0 != byMinute.length) {
            buf.append(";BYMINUTE=");
            writeIntList(byMinute, buf);
        }
        if (0 != bySecond.length) {
            buf.append(";BYSECOND=");
            writeIntList(bySecond, buf);
        }
        if (0 != bySetPos.length) {
            buf.append(";BYSETPOS=");
            writeIntList(bySetPos, buf);
        }
        return buf.toString();
    }

    private static void writeIntList(int[] nums, StringBuilder out) {
        for (int i = 0; i < nums.length; ++i) {
            if (0 != i) {
                out.append(',');
            }
            out.append(nums[i]);
        }
    }

    /** an approximate number of days between occurences. */
    public int approximateIntervalInDays() {
        int freqLengthDays;
        int nPerPeriod = 0;
        switch (this.freq) {
            case DAILY:
                freqLengthDays = 1;
                break;
            case WEEKLY:
                freqLengthDays = 7;
                if (!this.byDay.isEmpty()) {
                    nPerPeriod = this.byDay.size();
                }
                break;
            case MONTHLY:
                freqLengthDays = 30;
                if (!this.byDay.isEmpty()) {
                    for (WeekdayNum day : byDay) {
                        // if it's every weekday in the month, assume four of that weekday,
                        // otherwise there is one of that week-in-month,weekday pair
                        nPerPeriod += 0 != day.num ? 1 : 4;
                    }
                } else {
                    nPerPeriod = this.byMonthDay.length;
                }
                break;
            case YEARLY:
                freqLengthDays = 365;

                int monthCount = 12;
                if (0 != this.byMonth.length) {
                    monthCount = this.byMonth.length;
                }

                if (!this.byDay.isEmpty()) {
                    for (WeekdayNum day : byDay) {
                        // if it's every weekend in the months in the year,
                        // assume 4 of that weekday per month,
                        // otherwise there is one of that week-in-month,weekday pair per
                        // month
                        nPerPeriod += (0 != day.num ? 1 : 4) * monthCount;
                    }
                } else if (0 != this.byMonthDay.length) {
                    nPerPeriod += monthCount * this.byMonthDay.length;
                } else {
                    nPerPeriod += this.byYearDay.length;
                }
                break;
            default:
                freqLengthDays = 0;
        }
        if (0 == nPerPeriod) {
            nPerPeriod = 1;
        }

        return ((freqLengthDays / nPerPeriod) * this.interval);
    }

    /** the frequency of repetition */
    public Frequency getFreq() {
        return this.freq;
    }

    public void setFreq(Frequency freq) {
        this.freq = freq;
    }

    /** day of the week the week starts on */
    public Weekday getWkSt() {
        return this.wkst;
    }

    public void setWkSt(Weekday wkst) {
        this.wkst = wkst;
    }

    public DateValue getUntil() {
        return this.until;
    }

    public void setUntil(DateValue until) {
        this.until = until;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public List<WeekdayNum> getByDay() {
        return this.byDay;
    }

    public void setByDay(List<WeekdayNum> byDay) {
        this.byDay = new ArrayList<WeekdayNum>(byDay);
    }

    public int[] getByMonth() {
        return this.byMonth;
    }

    public void setByMonth(int[] byMonth) {
        this.byMonth = byMonth.clone();
    }

    public int[] getByMonthDay() {
        return this.byMonthDay;
    }

    public void setByMonthDay(int[] byMonthDay) {
        this.byMonthDay = byMonthDay.clone();
    }

    public int[] getByWeekNo() {
        return this.byWeekNo;
    }

    public void setByWeekNo(int[] byWeekNo) {
        this.byWeekNo = byWeekNo.clone();
    }

    public int[] getByYearDay() {
        return this.byYearDay;
    }

    public void setByYearDay(int[] byYearDay) {
        this.byYearDay = byYearDay.clone();
    }

    public int[] getBySetPos() {
        return this.bySetPos;
    }

    public void setBySetPos(int[] bySetPos) {
        this.bySetPos = bySetPos.clone();
    }

    public int[] getByHour() {
        return this.byHour;
    }

    public void setByHour(int[] byHour) {
        this.byHour = byHour.clone();
    }

    public int[] getByMinute() {
        return this.byMinute;
    }

    public void setByMinute(int[] byMinute) {
        this.byMinute = byMinute.clone();
    }

    public int[] getBySecond() {
        return this.bySecond;
    }

    public void setBySecond(int[] bySecond) {
        this.bySecond = bySecond.clone();
    }

}
