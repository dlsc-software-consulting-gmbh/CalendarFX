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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A formatter for the logging framework. Formats logging messages in a very
 * compact format.
 *
 * @author Dirk Lemmermann
 */
public class LoggingFormatter extends Formatter {

    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT);

    private static int MAX_LEVEL_SIZE = 0;

    static {
        MAX_LEVEL_SIZE = Math.max(MAX_LEVEL_SIZE, Level.INFO.getLocalizedName()
                .length());
        MAX_LEVEL_SIZE = Math.max(MAX_LEVEL_SIZE, Level.CONFIG
                .getLocalizedName().length());
        MAX_LEVEL_SIZE = Math.max(MAX_LEVEL_SIZE, Level.FINE.getLocalizedName()
                .length());
        MAX_LEVEL_SIZE = Math.max(MAX_LEVEL_SIZE, Level.FINER
                .getLocalizedName().length());
        MAX_LEVEL_SIZE = Math.max(MAX_LEVEL_SIZE, Level.FINEST
                .getLocalizedName().length());
        MAX_LEVEL_SIZE = Math.max(MAX_LEVEL_SIZE, Level.SEVERE
                .getLocalizedName().length());
        MAX_LEVEL_SIZE = Math.max(MAX_LEVEL_SIZE, Level.WARNING
                .getLocalizedName().length());
    }

    /**
     * Format the given LogRecord.
     *
     * @param record
     *            the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        ZonedDateTime timestamp = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(record.getMillis()),
                ZoneId.systemDefault());

        sb.append(formatter.format(timestamp));
        sb.append(" | ");//$NON-NLS-1$

        if (record.getSourceClassName() != null) {
            sb.append(truncate(
                    record.getSourceClassName().substring(
                            record.getSourceClassName().lastIndexOf('.') + 1),
                    30));
        } else {
            sb.append(truncate(record.getLoggerName(), 10));
        }

        sb.append(" | "); //$NON-NLS-1$

        if (record.getSourceMethodName() != null) {
            sb.append(truncate(record.getSourceMethodName(), 30));
        }

        sb.append(" | "); //$NON-NLS-1$

        String message = formatMessage(record);
        sb.append(truncate(record.getLevel().getLocalizedName(), MAX_LEVEL_SIZE));

        sb.append(" | ");//$NON-NLS-1$

        sb.append(message);

        sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
        }
        return sb.toString();
    }

    private CharSequence truncate(String s, int n) {
        if (s.length() > n) {
            s = s.substring(0, n);
        }
        return String.format("%1$-" + n + "s", s); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
