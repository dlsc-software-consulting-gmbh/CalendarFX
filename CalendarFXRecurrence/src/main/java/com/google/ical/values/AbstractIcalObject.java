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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for a mutable ICAL object.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
abstract class AbstractIcalObject implements IcalObject {
    private static final Pattern CONTENT_LINE_RE = Pattern.compile(
            "^((?:[^:;\"]|\"[^\"]*\")+)(;(?:[^:\"]|\"[^\"]*\")+)?:(.*)$");
    private static final Pattern PARAM_RE = Pattern.compile(
            "^;([^=]+)=(?:\"([^\"]*)\"|([^\";:]*))");
    static final Pattern ICAL_SPECIALS = Pattern.compile("[:;]");

    private String name;
    /**
     * paramter values.  Does not currently allow multiple values for the same
     * property.
     */
    private Map<String, String> extParams = null;

    /**
     * parse the ical object from the given ical content using the given schema.
     * Modifies the current object in place.
     *
     * @param schema rules for processing individual parameters and body content.
     */
    protected void parse(String icalString, IcalSchema schema)
            throws ParseException {

        String paramText;
        String content;
        {
            String unfolded = IcalParseUtil.unfoldIcal(icalString);
            Matcher m = CONTENT_LINE_RE.matcher(unfolded);
            if (!m.matches()) {
                schema.badContent(icalString);
            }

            setName(m.group(1).toUpperCase());
            paramText = m.group(2);
            if (null == paramText) {
                paramText = "";
            }
            content = m.group(3);
        }

        // parse parameters
        Map<String, String> params = new HashMap<String, String>();
        String rest = paramText;
        while (!"".equals(rest)) {
            Matcher m = PARAM_RE.matcher(rest);
            if (!m.find()) {
                schema.badPart(rest, null);
            }
            rest = rest.substring(m.end(0));
            String k = m.group(1).toUpperCase();
            String v = m.group(2);
            if (null == v) {
                v = m.group(3);
            }
            if (params.containsKey(k)) {
                schema.dupePart(k);
            }
            params.put(k, v);
        }
        // parse the content and individual attribute values
        schema.applyObjectSchema(this.name, params, content, this);
    }

    /** the object name such as RRULE, EXRULE, VEVENT.  @see #setName */
    public String getName() {
        return name;
    }

    /** @see #getName */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * a map of any extension parameters such as the X-FOO=BAR in RRULE;X-FOO=BAR.
     * Maps the parameter name, X-FOO, to the parameter value, BAR.
     */
    public Map<String, String> getExtParams() {
        if (null == extParams) {
            extParams = new LinkedHashMap<String, String>();
        }
        return extParams;
    }

    public boolean hasExtParams() {
        return null != extParams && !extParams.isEmpty();
    }

}
