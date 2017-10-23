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

package impl.com.calendarfx.view.util;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MICRO_OF_SECOND;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoUnit.DAYS;

@SuppressWarnings("javadoc")
public final class Util {

    public static boolean intersect(LocalDate aStart, LocalDate aEnd,
                                    LocalDate bStart, LocalDate bEnd) {

        // Same start time or same end time?
        if (aStart.equals(bStart) || aEnd.equals(bEnd)) {
            return true;
        }

        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);

    }


    public static boolean intersect(LocalTime aStart, LocalTime aEnd,
                                    LocalTime bStart, LocalTime bEnd) {

        // Same start time or same end time?
        if (aStart.equals(bStart) || aEnd.equals(bEnd)) {
            return true;
        }

        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);

    }

    public static boolean intersect(ZonedDateTime aStart, ZonedDateTime aEnd,
                                    ZonedDateTime bStart, ZonedDateTime bEnd) {

        // Same start time or same end time?
        if (aStart.equals(bStart) || aEnd.equals(bEnd)) {
            return true;
        }

        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);

    }

    public static LocalDateTime truncate(LocalDateTime time, ChronoUnit unit,
                                         int stepRate, DayOfWeek firstDayOfWeek) {
        switch (unit) {
            case DAYS:
                return adjustField(time, DAY_OF_YEAR, stepRate).truncatedTo(unit);
            case HALF_DAYS:
                return time.truncatedTo(unit);
            case HOURS:
                return adjustField(time, HOUR_OF_DAY, stepRate).truncatedTo(unit);
            case MINUTES:
                return adjustField(time, MINUTE_OF_HOUR, stepRate)
                        .truncatedTo(unit);
            case SECONDS:
                return adjustField(time, SECOND_OF_MINUTE, stepRate).truncatedTo(
                        unit);
            case MILLIS:
                return adjustField(time, MILLI_OF_SECOND, stepRate).truncatedTo(
                        unit);
            case MICROS:
                return adjustField(time, MICRO_OF_SECOND, stepRate).truncatedTo(
                        unit);
            case NANOS:
                return adjustField(time, NANO_OF_SECOND, stepRate)
                        .truncatedTo(unit);
            case MONTHS:
                return time
                        .with(MONTH_OF_YEAR,
                                Math.max(
                                        1,
                                        time.get(MONTH_OF_YEAR)
                                                - time.get(MONTH_OF_YEAR)
                                                % stepRate)).withDayOfMonth(1)
                        .truncatedTo(DAYS);
            case YEARS:
                return adjustField(time, ChronoField.YEAR, stepRate).withDayOfYear(
                        1).truncatedTo(DAYS);
            case WEEKS:
                return time.with(DAY_OF_WEEK, firstDayOfWeek.getValue()).truncatedTo(
                        DAYS);
            case DECADES:
                int decade = time.getYear() / 10 * 10;
                return time.with(ChronoField.YEAR, decade).withDayOfYear(1)
                        .truncatedTo(DAYS);
            case CENTURIES:
                int century = time.getYear() / 100 * 100;
                return time.with(ChronoField.YEAR, century).withDayOfYear(1)
                        .truncatedTo(DAYS);
            case MILLENNIA:
                int millenium = time.getYear() / 1000 * 1000;
                return time.with(ChronoField.YEAR, millenium).withDayOfYear(1)
                        .truncatedTo(DAYS);
            default:
        }

        return time;
    }

    public static ZonedDateTime truncate(ZonedDateTime time, ChronoUnit unit,
                                         int stepRate, DayOfWeek firstDayOfWeek) {
        switch (unit) {
            case DAYS:
                return adjustField(time, DAY_OF_YEAR, stepRate).truncatedTo(unit);
            case HALF_DAYS:
                return time.truncatedTo(unit);
            case HOURS:
                return adjustField(time, HOUR_OF_DAY, stepRate).truncatedTo(unit);
            case MINUTES:
                return adjustField(time, MINUTE_OF_HOUR, stepRate)
                        .truncatedTo(unit);
            case SECONDS:
                return adjustField(time, SECOND_OF_MINUTE, stepRate).truncatedTo(
                        unit);
            case MILLIS:
                return adjustField(time, MILLI_OF_SECOND, stepRate).truncatedTo(
                        unit);
            case MICROS:
                return adjustField(time, MICRO_OF_SECOND, stepRate).truncatedTo(
                        unit);
            case NANOS:
                return adjustField(time, NANO_OF_SECOND, stepRate)
                        .truncatedTo(unit);
            case MONTHS:
                return time
                        .with(MONTH_OF_YEAR,
                                Math.max(
                                        1,
                                        time.get(MONTH_OF_YEAR)
                                                - time.get(MONTH_OF_YEAR)
                                                % stepRate)).withDayOfMonth(1)
                        .truncatedTo(DAYS);
            case YEARS:
                return adjustField(time, ChronoField.YEAR, stepRate).withDayOfYear(
                        1).truncatedTo(DAYS);
            case WEEKS:
                return time.with(DAY_OF_WEEK, firstDayOfWeek.getValue()).truncatedTo(
                        DAYS);
            case DECADES:
                int decade = time.getYear() / 10 * 10;
                return time.with(ChronoField.YEAR, decade).withDayOfYear(1)
                        .truncatedTo(DAYS);
            case CENTURIES:
                int century = time.getYear() / 100 * 100;
                return time.with(ChronoField.YEAR, century).withDayOfYear(1)
                        .truncatedTo(DAYS);
            case MILLENNIA:
                int millenium = time.getYear() / 1000 * 1000;
                return time.with(ChronoField.YEAR, millenium).withDayOfYear(1)
                        .truncatedTo(DAYS);
            default:
        }

        return time;
    }

    public static LocalTime truncate(LocalTime time, ChronoUnit unit,
                                     int stepRate) {
        switch (unit) {
            case HOURS:
                return adjustField(time, HOUR_OF_DAY, stepRate).truncatedTo(unit);
            case MINUTES:
                return adjustField(time, MINUTE_OF_HOUR, stepRate)
                        .truncatedTo(unit);
            case SECONDS:
                return adjustField(time, SECOND_OF_MINUTE, stepRate).truncatedTo(
                        unit);
            case MILLIS:
                return adjustField(time, MILLI_OF_SECOND, stepRate).truncatedTo(
                        unit);
            case MICROS:
                return adjustField(time, MICRO_OF_SECOND, stepRate).truncatedTo(
                        unit);
            case NANOS:
                return adjustField(time, NANO_OF_SECOND, stepRate)
                        .truncatedTo(unit);
            default:
        }

        return time;
    }

    public static boolean equals(Object first, Object second) {
        if (first == null) {
            return second == null;
        }

        if (second == null) {
            // because we already know that first is not null (see above)
            return false;
        }

        return first.equals(second);
    }

    public static void runInFXThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static <T> MultipleSelectionModel<T> createEmptySelectionModel() {
        return new EmptySelectionModel<>();
    }

    private static ZonedDateTime adjustField(ZonedDateTime time, ChronoField field, int stepRate) {
        return time.with(field, time.get(field) - time.get(field) % stepRate);
    }

    private static LocalDateTime adjustField(LocalDateTime time, ChronoField field, int stepRate) {
        return time.with(field, time.get(field) - time.get(field) % stepRate);
    }

    private static LocalTime adjustField(LocalTime time, ChronoField field, int stepRate) {
        return time.with(field, time.get(field) - time.get(field) % stepRate);
    }

    private static class EmptySelectionModel<T> extends MultipleSelectionModel<T> {
        @Override
        public void selectPrevious() {
        }

        @Override
        public void selectNext() {
        }

        @Override
        public void select(int index) {
        }

        @Override
        public void select(T obj) {
        }

        @Override
        public boolean isSelected(int index) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void clearSelection(int index) {
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public void clearAndSelect(int index) {
        }

        @Override
        public void selectLast() {
        }

        @Override
        public void selectIndices(int index, int... indices) {
        }

        @Override
        public void selectFirst() {
        }

        @Override
        public void selectAll() {
        }

        private final ObservableList<T> selectedItems = FXCollections.observableArrayList();

        @Override
        public ObservableList<T> getSelectedItems() {
            return selectedItems;
        }

        private final ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();

        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return selectedIndices;
        }
    }

}
