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

import com.calendarfx.view.DateControl;
import com.calendarfx.view.Messages;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.Property;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.Recur.Frequency;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Objects;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;

/**
 * Utility methods.
 */
public class Util {

    /**
     * An interface used for converting an object of one type to an object
     * of another type.
     *
     * @param <L> the first (left) type
     * @param <R> the second (right) type
     */
    public interface Converter<L, R> {

        L toLeft(R right);

        R toRight(L left);
    }

    /**
     * Converts the given recurrence rule (according to RFC 2445) into a human
     * readable text, e.g. "RRULE:FREQ=DAILY;" becomes "Every day".
     *
     * @param rrule     the rule
     * @param startDate the start date for the rule
     * @return a nice text describing the rule
     */
    public static String convertRFC2445ToText(String rrule,
                                              LocalDate startDate) {

        try {
            Recur<LocalDate> rule = new Recur<>(rrule.replaceFirst("^RRULE:", ""));
            StringBuilder sb = new StringBuilder();

            String granularity = "";
            String granularities = "";

            switch (rule.getFrequency()) {
                case DAILY:
                    granularity = Messages.getString("Util.DAY");
                    granularities = Messages.getString("Util.DAYS");
                    break;
                case MONTHLY:
                    granularity = Messages.getString("Util.MONTH");
                    granularities = Messages.getString("Util.MONTHS");
                    break;
                case WEEKLY:
                    granularity = Messages.getString("Util.WEEK");
                    granularities = Messages.getString("Util.WEEKS");
                    break;
                case YEARLY:
                    granularity = Messages.getString("Util.YEAR");
                    granularities = Messages.getString("Util.YEARS");
                    break;
                case HOURLY:
                    granularity = Messages.getString("Util.HOUR");
                    granularities = Messages.getString("Util.HOURS");
                    break;
                case MINUTELY:
                    granularity = Messages.getString("Util.MINUTE");
                    granularities = Messages.getString("Util.MINUTES");
                    break;
                case SECONDLY:
                    granularity = Messages.getString("Util.SECOND");
                    granularities = Messages.getString("Util.SECONDS");
                    break;
            }

            int interval = rule.getInterval();
            if (interval > 1) {
                sb.append(MessageFormat.format(Messages.getString("Util.EVERY_PLURAL"),
                        rule.getInterval(), granularities));
            } else {
                sb.append(MessageFormat.format(Messages.getString("Util.EVERY_SINGULAR"), granularity));
            }

            /*
             * Weekdays
             */

            if (rule.getFrequency().equals(Frequency.WEEKLY)) {
                WeekDayList byDay = rule.getDayList();
                if (!byDay.isEmpty()) {
                    sb.append(Messages.getString("Util.ON_WEEKDAY"));
                    for (int i = 0; i < byDay.size(); i++) {
                        WeekDay num = byDay.get(i);
                        sb.append(makeHuman(num.getDay()));
                        if (i < byDay.size() - 1) {
                            sb.append(", ");
                        }
                    }
                }
            }

            if (rule.getFrequency().equals(Frequency.MONTHLY)) {

                if (!rule.getMonthDayList().isEmpty()) {

                    int day = rule.getMonthDayList().get(0);
                    sb.append(Messages.getString("Util.ON_MONTH_DAY"));
                    sb.append(day);

                } else if (!rule.getDayList().isEmpty()) {

                    /*
                     * We only support one day.
                     */
                    WeekDay num = rule.getDayList().get(0);

                    sb.append(MessageFormat.format(Messages.getString("Util.ON_MONTH_WEEKDAY"),
                            makeHuman(num.getOffset()), makeHuman(num.getDay())));

                }
            }

            if (rule.getFrequency().equals(Frequency.YEARLY)) {
                sb.append(MessageFormat.format(Messages.getString("Util.ON_DATE"), DateTimeFormatter
                        .ofPattern(Messages.getString("Util.MONTH_AND_DAY_FORMAT")).format(startDate)));
            }

            int count = rule.getCount();
            if (count > 0) {
                if (count == 1) {
                    return Messages.getString("Util.ONCE");
                } else {
                    sb.append(MessageFormat.format(Messages.getString("Util.TIMES"), count));
                }
            } else {
                LocalDate until = rule.getUntil();
                if (until != null) {
                    sb.append(
                            MessageFormat.format(Messages.getString("Util.UNTIL_DATE"),
                                    DateTimeFormatter
                                            .ofLocalizedDate(FormatStyle.LONG)
                                            .format(until)));
                }
            }

            return sb.toString();
        } catch (IllegalArgumentException | DateTimeParseException e) {
            e.printStackTrace();
            return Messages.getString("Util.INVALID_RULE");
        }
    }

    private static String makeHuman(WeekDay.Day wday) {
        switch (wday) {
            case FR:
                return Messages.getString("Util.FRIDAY");
            case MO:
                return Messages.getString("Util.MONDAY");
            case SA:
                return Messages.getString("Util.SATURDAY");
            case SU:
                return Messages.getString("Util.SUNDAY");
            case TH:
                return Messages.getString("Util.THURSDAY");
            case TU:
                return Messages.getString("Util.TUESDAY");
            case WE:
                return Messages.getString("Util.WEDNESDAY");
            default:
                throw new IllegalArgumentException("unknown weekday: " + wday);
        }
    }

    private static String makeHuman(int num) {
        switch (num) {
            case 1:
                return Messages.getString("Util.FIRST");
            case 2:
                return Messages.getString("Util.SECOND");
            case 3:
                return Messages.getString("Util.THIRD");
            case 4:
                return Messages.getString("Util.FOURTH");
            case 5:
                return Messages.getString("Util.FIFTH");
            default:
                return Integer.toString(num);
        }
    }

    /**
     * Searches for a {@link ScrollBar} of the given orientation (vertical, horizontal)
     * somewhere in the containment hierarchy of the given parent node.
     *
     * @param parent      the parent node
     * @param orientation the orientation (horizontal, vertical)
     * @return a scrollbar or null if none can be found
     */
    public static ScrollBar findScrollBar(Parent parent, Orientation orientation) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof ScrollBar) {
                ScrollBar b = (ScrollBar) node;
                if (b.getOrientation().equals(orientation)) {
                    return b;
                }
            }

            if (node instanceof Parent) {
                ScrollBar b = findScrollBar((Parent) node, orientation);
                if (b != null) {
                    return b;
                }
            }
        }

        return null;
    }

    /**
     * Adjusts the given date to a new date that marks the beginning of the week where the
     * given date is located. If "Monday" is the first day of the week and the given date
     * is a "Wednesday" then this method will return a date that is two days earlier than the
     * given date.
     *
     * @param date           the date to adjust
     * @param firstDayOfWeek the day of week that is considered the start of the week ("Monday" in Germany, "Sunday" in the US)
     * @return the date of the first day of the week
     * @see #adjustToLastDayOfWeek(LocalDate, DayOfWeek)
     * @see DateControl#getFirstDayOfWeek()
     */
    public static LocalDate adjustToFirstDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate newDate = date.with(DAY_OF_WEEK, firstDayOfWeek.getValue());
        if (newDate.isAfter(date)) {
            newDate = newDate.minusWeeks(1);
        }

        return newDate;
    }

    /**
     * Adjusts the given date to a new date that marks the end of the week where the
     * given date is located. If "Monday" is the first day of the week and the given date
     * is a "Wednesday" then this method will return a date that is four days later than the
     * given date. This method calculates the first day of the week and then adds six days
     * to it.
     *
     * @param date           the date to adjust
     * @param firstDayOfWeek the day of week that is considered the start of the week ("Monday" in Germany, "Sunday" in the US)
     * @return the date of the first day of the week
     * @see #adjustToFirstDayOfWeek(LocalDate, DayOfWeek)
     * @see DateControl#getFirstDayOfWeek()
     */
    public static LocalDate adjustToLastDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate startOfWeek = adjustToFirstDayOfWeek(date, firstDayOfWeek);
        return startOfWeek.plusDays(6);
    }

    /**
     * Creates a bidirectional binding between the two given properties of different types via the
     * help of a {@link Converter}.
     *
     * @param leftProperty  the left property
     * @param rightProperty the right property
     * @param converter     the converter
     * @param <L>           the type of the left property
     * @param <R>           the type of the right property
     */
    public static <L, R> void bindBidirectional(Property<L> leftProperty, Property<R> rightProperty, Converter<L, R> converter) {
        BidirectionalConversionBinding<L, R> binding = new BidirectionalConversionBinding<>(leftProperty, rightProperty, converter);
        leftProperty.addListener(binding);
        rightProperty.addListener(binding);
        leftProperty.setValue(converter.toLeft(rightProperty.getValue()));
    }

    private static class BidirectionalConversionBinding<L, R> implements InvalidationListener, WeakListener {

        private final WeakReference<Property<L>> leftReference;
        private final WeakReference<Property<R>> rightReference;
        private final Converter<L, R> converter;
        private boolean updating;

        private BidirectionalConversionBinding(Property<L> leftProperty, Property<R> rightProperty, Converter<L, R> converter) {
            this.leftReference = new WeakReference<>(Objects.requireNonNull(leftProperty));
            this.rightReference = new WeakReference<>(Objects.requireNonNull(rightProperty));
            this.converter = Objects.requireNonNull(converter);
        }

        public Property<L> getLeftProperty() {
            return leftReference.get();
        }

        public Property<R> getRightProperty() {
            return rightReference.get();
        }

        @Override
        public boolean wasGarbageCollected() {
            return getLeftProperty() == null || getRightProperty() == null;
        }

        @Override
        public void invalidated(Observable observable) {
            if (updating) {
                return;
            }

            final Property<L> leftProperty = getLeftProperty();
            final Property<R> rightProperty = getRightProperty();

            if (wasGarbageCollected()) {
                if (leftProperty != null) {
                    leftProperty.removeListener(this);
                }
                if (rightProperty != null) {
                    rightProperty.removeListener(this);
                }
            } else {
                try {
                    updating = true;

                    if (observable == leftProperty) {
                        rightProperty.setValue(converter.toRight(leftProperty.getValue()));
                    } else {
                        leftProperty.setValue(converter.toLeft(rightProperty.getValue()));
                    }
                } finally {
                    updating = false;
                }
            }
        }
    }
}
