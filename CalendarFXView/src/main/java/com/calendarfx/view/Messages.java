/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
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
    
    public static String getString (String key, Object... args) {
        try {
            String message = RESOURCE_BUNDLE.getString(key);
			return MessageFormat.format(message, args);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
