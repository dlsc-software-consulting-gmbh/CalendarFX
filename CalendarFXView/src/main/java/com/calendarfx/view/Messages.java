/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.calendarfx.view;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for looking up i18n strings.
 */
public class Messages {

    private static final String BUNDLE_NAME = "com.calendarfx.view.messages";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    /**
     * Returns the translation for the given key.
     *
     * @param key the i18n key
     * @return the translation
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getString(String key, Object... args) {
        try {
            String message = RESOURCE_BUNDLE.getString(key);
            return MessageFormat.format(message, args);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
