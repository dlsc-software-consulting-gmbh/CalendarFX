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
import java.util.regex.Pattern;

/**
 * ical objects are made up of parameters (key=value pairs) and the contents
 * are often one or more value types or key=value pairs.
 * This schema encapsulates rules that can be applied to parse each part before
 * inserting the results into the {@link IcalObject}.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
class IcalSchema {

    /** rules for decoding parameter values */
    private final Map<String, ParamRule> paramRules;

    /** rules for decoding parts of the content body */
    private final Map<String, ContentRule> contentRules;

    /** rules for breaking the content body or parameters into parts */
    private final Map<String, ObjectRule> objectRules;

    /** rules for parsing value types */
    private final Map<String, XformRule> xformRules;

    /** list of productions that we're processing for debugging. */
    private final List<String> ruleStack = new ArrayList<String>();

    private static final Pattern EXTENSION_PARAM_NAME_RE =
            Pattern.compile("^X-[A-Z0-9\\-]+$", Pattern.CASE_INSENSITIVE);

    IcalSchema(Map<String, ParamRule> paramRules,
               Map<String, ContentRule> contentRules,
               Map<String, ObjectRule> objectRules,
               Map<String, XformRule> xformRules) {
        this.paramRules = paramRules;
        this.contentRules = contentRules;
        this.objectRules = objectRules;
        this.xformRules = xformRules;
    }

    /////////////////////////////////
    // Parser Core
    /////////////////////////////////

    public void applyParamsSchema(
            String rule, Map<String, String> params, IcalObject out)
            throws ParseException {
        for (Map.Entry<String, String> param : params.entrySet()) {
            String name = param.getKey();
            applyParamSchema(rule, name, param.getValue(), out);
        }
    }

    public void applyParamSchema(
            String rule, String name, String value, IcalObject out)
            throws ParseException {
        // all elements are allowed extension parameters
        if (EXTENSION_PARAM_NAME_RE.matcher(name).find()) {
            out.getExtParams().put(name, value);
            return;
        }
        // if not an extension, apply the rule
        ruleStack.add(rule);
        try {
            (paramRules.get(rule)).apply(this, name, value, out);
        } finally {
            ruleStack.remove(ruleStack.get(ruleStack.size() - 1));
        }
    }

    public void applyContentSchema(String rule, String content, IcalObject out)
            throws ParseException {
        ruleStack.add(rule);
        try {
            try {
                (contentRules.get(rule)).apply(this, content, out);
            } catch (NumberFormatException ex) {
                badContent(content);
            } catch (IllegalArgumentException ex) {
                badContent(content);
            }
        } finally {
            ruleStack.remove(ruleStack.get(ruleStack.size() - 1));
        }
    }

    public void applyObjectSchema(
            String rule, Map<String, String> params, String content, IcalObject out)
            throws ParseException {
        ruleStack.add(rule);
        try {
            (objectRules.get(rule)).apply(this, params, content, out);
        } finally {
            ruleStack.remove(ruleStack.get(ruleStack.size() - 1));
        }
    }

    public Object applyXformSchema(String rule, String content)
            throws ParseException {
        ruleStack.add(rule);
        try {
            try {
                return (xformRules.get(rule)).apply(this, content);
            } catch (NumberFormatException ex) {
                badContent(content);
            } catch (IllegalArgumentException ex) {
                badContent(content);
            }
            throw new AssertionError();  // badContent raises an exception
        } finally {
            ruleStack.remove(ruleStack.get(ruleStack.size() - 1));
        }
    }

    /////////////////////////////////
    // Parser Error Handling
    /////////////////////////////////

    public void badParam(String name, String value)
            throws ParseException {
        throw new ParseException("parameter " + name + " has bad value [[" +
                value + "]] in " + ruleStack, 0);
    }

    public void badPart(String part, String msg) throws ParseException {
        if (null != msg) {
            msg = " : " + msg;
        } else {
            msg = "";
        }
        throw new ParseException("cannot parse [[" + part + "]] in " +
                ruleStack + msg, 0);
    }

    public void dupePart(String part) throws ParseException {
        throw new ParseException(
                "duplicate part [[" + part + "]] in " + ruleStack, 0);
    }

    public void missingPart(String partName, String content)
            throws ParseException {
        throw new ParseException("missing part " + partName + " from [[" + content +
                "]] in " + ruleStack, 0);
    }

    public void badContent(String content) throws ParseException {
        throw new ParseException(
                "cannot parse content line [[" + content + "]] in " + ruleStack, 0);
    }

    /**
     * rule applied to parse an entire content line after its been split into
     * unparsed/unescaped parameters and unescaped content.
     */
    public interface ObjectRule {
        /**
         * @param schema the schema used to provide further rules.
         */
        void apply(IcalSchema schema,
                   Map<String, String> params,
                   String content,
                   IcalObject target)
                throws ParseException;
    }

    /** rule applied to an ical content line parameter. */
    public interface ParamRule {
        void apply(
                IcalSchema schema, String name, String value, IcalObject out)
                throws ParseException;
    }

    /** rule applied to part of the ical content body. */
    public interface ContentRule {
        void apply(IcalSchema schema, String content, IcalObject target)
                throws ParseException;
    }

    /** rule applied to parse an ical data value from its string form. */
    public interface XformRule {
        Object apply(IcalSchema schema, String content)
                throws ParseException;
    }
}
