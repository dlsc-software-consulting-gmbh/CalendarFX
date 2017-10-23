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
import java.util.Map;
import java.util.TimeZone;

/**
 * a list of dates, as from an RDATE or EXDATE ical property.
 *
 * <p>See RFC 2445 sections 4.8.5.1 and 4.8.5.3.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class RDateList extends AbstractIcalObject {

    private TimeZone tzid;
    private DateValue[] datesUtc;
    private IcalValueType valueType;

    public RDateList(String icalString, TimeZone tzid) throws ParseException {
        setTzid(tzid);
        parse(icalString, RRuleSchema.instance());
    }

    public RDateList(TimeZone tzid) {
        setTzid(tzid);
        setName("RDATE");
        datesUtc = new DateValue[0];
    }

    public TimeZone getTzid() {
        return this.tzid;
    }

    public void setTzid(TimeZone tzid) {
        assert null != tzid;
        this.tzid = tzid;
    }

    public DateValue[] getDatesUtc() {
        return null != this.datesUtc ? this.datesUtc.clone() : null;
    }

    public void setDatesUtc(DateValue[] datesUtc) {
        this.datesUtc = datesUtc.clone();
        if (datesUtc.length > 0) {
            setValueType((datesUtc[0] instanceof TimeValue)
                    ? IcalValueType.DATE_TIME
                    : IcalValueType.DATE);
        }
    }

    /**
     * the type of the values contained by this list as reported by the ical
     * "VALUE" parameter, typically DATE or DATE-TIME.
     */
    public IcalValueType getValueType() {
        return valueType;
    }

    public void setValueType(IcalValueType valueType) {
        this.valueType = valueType;
    }

    /** returns a String containing ical content lines. */
    public String toIcal() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getName().toUpperCase());
        buf.append(";TZID=\"").append(tzid.getID()).append('"');
        buf.append(";VALUE=").append(valueType.toIcal());
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
        buf.append(':');
        for (int i = 0; i < datesUtc.length; ++i) {
            if (0 != i) {
                buf.append(',');
            }
            DateValue v = datesUtc[i];
            buf.append(v);
            if (v instanceof TimeValue) {
                buf.append('Z');
            }
        }
        return buf.toString();
    }

}
