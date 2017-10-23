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

package com.calendarfx.view.print;

import javafx.print.PageOrientation;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

/**
 * An enumerator listing the different views that are supported
 * by the print preview functionality.
 */
public enum ViewType {

    DAY_VIEW {
        @Override
        public String getMessageKey() {
            return "PrintViewType.DAY_VIEW";
        }

        @Override
        public String getPluralChronoMessageKey() {
            return "PrintViewType.DAY_PLURAL_CHRONO";
        }

        @Override
        public PageOrientation getPageOrientation() {
            return PageOrientation.PORTRAIT;
        }

        @Override
        public DateTimeFormatter getDateTimeFormatter() {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        }

        @Override
        public ChronoUnit getChronoUnit() {
            return ChronoUnit.DAYS;
        }

    },

    WEEK_VIEW {
        @Override
        public String getMessageKey() {
            return "PrintViewType.WEEK_VIEW";
        }

        @Override
        public String getPluralChronoMessageKey() {
            return "PrintViewType.WEEK_PLURAL_CHRONO";
        }

        @Override
        public PageOrientation getPageOrientation() {
            return PageOrientation.LANDSCAPE;
        }

        @Override
        public DateTimeFormatter getDateTimeFormatter() {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        }

        @Override
        public ChronoUnit getChronoUnit() {
            return ChronoUnit.WEEKS;
        }
    },

    MONTH_VIEW {
        @Override
        public String getMessageKey() {
            return "PrintViewType.MONTH_VIEW";
        }

        @Override
        public String getPluralChronoMessageKey() {
            return "PrintViewType.MONTH_PLURAL_CHRONO";
        }

        @Override
        public PageOrientation getPageOrientation() {
            return PageOrientation.LANDSCAPE;
        }

        @Override
        public DateTimeFormatter getDateTimeFormatter() {
            return DateTimeFormatter.ofPattern("MMMM yyyy");
        }

        @Override
        public ChronoUnit getChronoUnit() {
            return ChronoUnit.MONTHS;
        }
    };

    public abstract String getMessageKey();

    public abstract String getPluralChronoMessageKey();

    public abstract PageOrientation getPageOrientation();

    public abstract DateTimeFormatter getDateTimeFormatter();

    public abstract ChronoUnit getChronoUnit();

}
