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

package com.calendarfx.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Common superclass for all controls in this framework.
 */
public final class CalendarFX {

    private static String version;

    /**
     * Returns the CalendarFX version number in the format major.minor.bug
     * (1.0.0).
     *
     * @return the CalendarFX version number
     */
    public static String getVersion() {
        if (version == null) {

            InputStream stream = CalendarFX.class.getResourceAsStream("version.properties"); //$NON-NLS-1$
            Properties props = new Properties();
            try {
                props.load(stream);
            } catch (IOException ex) {
                LoggingDomain.CONFIG.throwing(CalendarFX.class.getName(), "getVersion()", ex); //$NON-NLS-1$
            }

            version = props.getProperty("calendarfx.version", "1.0.0"); //$NON-NLS-1$ //$NON-NLS-2$

            LoggingDomain.CONFIG.info("CalendarFX Version: " + version); //$NON-NLS-1$
        }
        return version;
    }
}
