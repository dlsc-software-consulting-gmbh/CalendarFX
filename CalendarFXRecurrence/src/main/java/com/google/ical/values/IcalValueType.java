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

/**
 * a value type for an Ical property.
 *
 * <pre>
 * 4.2.20 Value Data Types
 *
 * Parameter Name: VALUE
 *
 * Purpose: To explicitly specify the data type format for a property value.
 *
 * Format Definition: The "VALUE" property parameter is defined by the
 * following notation:
 *
 *      valuetypeparam = "VALUE" "=" valuetype
 *
 *      valuetype  = ("BINARY"
 *                 / "BOOLEAN"
 *                 / "CAL-ADDRESS"
 *                 / "DATE"
 *                 / "DATE-TIME"
 *                 / "DURATION"
 *                 / "FLOAT"
 *                 / "INTEGER"
 *                 / "PERIOD"
 *                 / "RECUR"
 *                 / "TEXT"
 *                 / "TIME"
 *                 / "URI"
 *                 / "UTC-OFFSET"
 *                 / x-name
 *                 ; Some experimental iCalendar data type.
 *                 / iana-token)
 *                 ; Some other IANA registered iCalendar data type.
 *
 *
 * Description: The parameter specifies the data type and format of the property
 * value. The property values MUST be of a single value type. For example, a
 * "RDATE" property cannot have a combination of DATE- TIME and TIME value
 * types.
 *
 * If the property's value is the default value type, then this parameter need
 * not be specified. However, if the property's default value type is overridden
 * by some other allowable value type, then this parameter MUST be specified.
 * </pre>
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public enum IcalValueType {

    BINARY,
    BOOLEAN,
    CAL_ADDRESS,
    DATE,
    DATE_TIME,
    DURATION,
    FLOAT,
    INTEGER,
    PERIOD,
    RECUR,
    TEXT,
    TIME,
    URI,
    UTC_OFFSET,

    X_NAME,
    OTHER,;

    public static IcalValueType fromIcal(String icalValue) {
        return valueOf(icalValue.toUpperCase().replace('-', '_'));
    }

    public String toIcal() {
        return name().replace('_', '-');
    }
}
