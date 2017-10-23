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

import java.util.logging.Logger;

/**
 * Globally defined loggers. Alternative approach to using class based loggers.
 */
public final class LoggingDomain {

    private static final String PREFIX = "com.calendarfx"; //$NON-NLS-1$

    /**
     * Logger used for anything related to the configuration of the calendar.
     */
    public static final Logger CONFIG = Logger.getLogger(PREFIX + ".config"); //$NON-NLS-1$

    /**
     * Logger used for anything related to the creation, fireing, and handling
     * of events.
     */
    public static final Logger EVENTS = Logger.getLogger(PREFIX + ".events"); //$NON-NLS-1$

    /**
     * Logger used for anything related to the model, adding / removing entries.
     */
    public static final Logger MODEL = Logger.getLogger(PREFIX + ".model"); //$NON-NLS-1$

    /**
     * Logger used for anything related to the creation of the view.
     */
    public static final Logger VIEW = Logger.getLogger(PREFIX + ".view"); //$NON-NLS-1$

    /**
     * Logger used for the search service.
     */
    public static final Logger SEARCH = Logger.getLogger(PREFIX + ".search"); //$NON-NLS-1$

    /**
     * Logger used for anything related to the editing of entries.
     */
    public static final Logger EDITING = Logger.getLogger(PREFIX + ".editing"); //$NON-NLS-1$

    /**
     * Logger used for anything related to recurrence.
     */
    public static final Logger RECURRENCE = Logger.getLogger(PREFIX + ".recurrence"); //$NON-NLS-1$

    /**
     * Logger used for anything related to printing.
     */
    public static final Logger PRINTING = Logger.getLogger(PREFIX + ".printing"); //$NON-NLS-1$

    /**
     * Logger used for anything related to performance.
     */
    public static final Logger PERFORMANCE = Logger.getLogger(PREFIX + ".performance"); //$NON-NLS-1$
}
