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
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import impl.com.calendarfx.view.DayViewScrollPane;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.effect.Light.Point;
import javafx.stage.Screen;
import org.controlsfx.control.PopOver.ArrowLocation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("javadoc")
public final class ViewHelper {

    public static double getTimeLocation(DayViewBase view, LocalTime time) {
        return getTimeLocation(view, time, false);
    }

    public static double getTimeLocation(DayViewBase view, LocalTime time, boolean prefHeight) {
        LocalTime startTime = view.getStartTime();
        LocalTime endTime = view.getEndTime();
        double availableHeight = view.getHeight();
        if (prefHeight) {
            availableHeight = view.prefHeight(-1);
        }
        EarlyLateHoursStrategy strategy = view.getEarlyLateHoursStrategy();

        switch (strategy) {
            case SHOW:
                long startNano = LocalTime.MIN.toNanoOfDay();
                long endNano = LocalTime.MAX.toNanoOfDay();

                double npp = (endNano - startNano) / availableHeight;

                return ((int) ((time.toNanoOfDay() - startNano) / npp)) + .5;
            case HIDE:
                if (time.isBefore(startTime)) {
                    return -1;
                }

                if (time.isAfter(endTime)) {
                    return availableHeight;
                }

                startNano = startTime.toNanoOfDay();
                endNano = endTime.toNanoOfDay();

                npp = (endNano - startNano) / availableHeight;

                return ((int) ((time.toNanoOfDay() - startNano) / npp)) + .5;
            case SHOW_COMPRESSED:
                long earlyHours = ChronoUnit.HOURS
                        .between(LocalTime.MIN, startTime);
                long lateHours = ChronoUnit.HOURS.between(endTime, LocalTime.MAX) + 1;
                double hourHeightCompressed = view.getHourHeightCompressed();
                double earlyHeight = hourHeightCompressed * earlyHours;
                double lateHeight = hourHeightCompressed * lateHours;

                if (time.isBefore(startTime)) {
                    /*
                     * Early compressed hours.
                     */
                    startNano = LocalTime.MIN.toNanoOfDay();
                    endNano = startTime.toNanoOfDay();

                    npp = (endNano - startNano) / earlyHeight;

                    return ((int) ((time.toNanoOfDay() - startNano) / npp)) + .5;
                } else if (time.isAfter(endTime)) {
                    /*
                     * Late compressed hours.
                     */
                    startNano = endTime.toNanoOfDay();
                    endNano = LocalTime.MAX.toNanoOfDay();

                    npp = (endNano - startNano) / lateHeight;

                    return ((int) ((time.toNanoOfDay() - startNano) / npp))
                            + (availableHeight - lateHeight) + .5;
                } else {
                    /*
                     * Regular hours.
                     */
                    startNano = startTime.toNanoOfDay();
                    endNano = endTime.toNanoOfDay();
                    npp = (endNano - startNano)
                            / (availableHeight - earlyHeight - lateHeight);

                    return earlyHeight
                            + ((int) ((time.toNanoOfDay() - startNano) / npp)) + .5;
                }
            default:
                return 0;
        }
    }

    public static LocalDateTime getLocationTime(DayViewBase view, double y, boolean trim, boolean prefHeight) {

        /**
         * When the early and late hours are not showing then we have to trim for sure.
         */
        if (view.getEarlyLateHoursStrategy().equals(EarlyLateHoursStrategy.HIDE)) {
            trim = true;
        }

        LocalDate localDate = view.getDate();

        double availableHeight = view.getHeight();
        if (prefHeight) {
            availableHeight = view.prefHeight(-1);
        }

        if (y < 0) {
            if (trim) {
                return LocalDateTime.of(view.getDate(), view.getStartTime());
            }
            y = availableHeight + y;
            localDate = localDate.minusDays(1);
        } else if (y > availableHeight) {
            if (trim) {
                return LocalDateTime.of(view.getDate(), view.getEndTime());
            }
            y = y - availableHeight;
            localDate = localDate.plusDays(1);
        }

        LocalTime startTime = view.getStartTime();
        LocalTime endTime = view.getEndTime();

        switch (view.getEarlyLateHoursStrategy()) {
            case SHOW:
                long startNano = LocalTime.MIN.toNanoOfDay();
                long endNano = LocalTime.MAX.toNanoOfDay();

                double npp = (endNano - startNano) / availableHeight;

                long nanos = Math.min(LocalTime.MAX.toNanoOfDay(), (long) (npp * y));

                return LocalDateTime.of(localDate, LocalTime.ofNanoOfDay(nanos));
            case HIDE:
                startNano = startTime.toNanoOfDay();
                endNano = endTime.toNanoOfDay();

                npp = (endNano - startNano) / availableHeight;

                nanos = Math.min(endTime.toNanoOfDay(), (long) (npp * y) + startNano);

                return LocalDateTime.of(localDate, LocalTime.ofNanoOfDay(nanos));
            case SHOW_COMPRESSED:
                long earlyHours = ChronoUnit.HOURS
                        .between(LocalTime.MIN, startTime);
                long lateHours = ChronoUnit.HOURS.between(endTime, LocalTime.MAX) + 1;
                double hourHeightCompressed = view.getHourHeightCompressed();
                double earlyHeight = hourHeightCompressed * earlyHours;
                double lateHeight = hourHeightCompressed * lateHours;

                if (y < earlyHeight) {
                    /*
                     * Early compressed hours.
                     */
                    startNano = LocalTime.MIN.toNanoOfDay();
                    endNano = startTime.toNanoOfDay();

                    npp = (endNano - startNano) / earlyHeight;

                    nanos = Math.min(LocalTime.MAX.toNanoOfDay(), (long) (npp * y));

                    return LocalDateTime.of(localDate, LocalTime.ofNanoOfDay(nanos));
                } else if (y > availableHeight - lateHeight) {
                    /*
                     * Late compressed hours.
                     */
                    startNano = endTime.toNanoOfDay();
                    endNano = LocalTime.MAX.toNanoOfDay();

                    npp = (endNano - startNano) / lateHeight;

                    nanos = Math.min(LocalTime.MAX.toNanoOfDay(), (long) (npp * (y - (availableHeight - lateHeight))) + startNano);

                    return LocalDateTime.of(localDate, LocalTime.ofNanoOfDay(nanos));
                } else {
                    /*
                     * Regular hours.
                     */
                    startNano = startTime.toNanoOfDay();
                    endNano = endTime.toNanoOfDay();

                    npp = (endNano - startNano)
                            / (availableHeight - earlyHeight - lateHeight);

                    nanos = Math.min(LocalTime.MAX.toNanoOfDay(), (long) (npp * (y - earlyHeight)) + startNano);

                    return LocalDateTime.of(localDate, LocalTime.ofNanoOfDay(nanos));
                }
            default:
                return LocalDateTime.of(localDate, LocalTime.MIN);
        }
    }

    public static ArrowLocation findPopOverArrowLocation(Node view) {
        Bounds localBounds = view.getBoundsInLocal();
        Bounds entryBounds = view.localToScreen(localBounds);

        ObservableList<Screen> screens = Screen.getScreensForRectangle(
                entryBounds.getMinX(), entryBounds.getMinY(),
                entryBounds.getWidth(), entryBounds.getHeight());
        Rectangle2D screenBounds = screens.get(0).getVisualBounds();

        double spaceLeft = entryBounds.getMinX();
        double spaceRight = screenBounds.getWidth() - entryBounds.getMaxX();
        double spaceTop = entryBounds.getMinY();
        double spaceBottom = screenBounds.getHeight() - entryBounds.getMaxY();

        if (spaceLeft > spaceRight) {
            if (spaceTop > spaceBottom) {
                return ArrowLocation.RIGHT_BOTTOM;
            }
            return ArrowLocation.RIGHT_TOP;
        }

        if (spaceTop > spaceBottom) {
            return ArrowLocation.LEFT_BOTTOM;
        }

        return ArrowLocation.LEFT_TOP;
    }

    public static Point findPopOverArrowPosition(Node node, double screenY, double arrowSize, ArrowLocation arrowLocation) {
        Point point = new Point();
        point.setY(screenY);

        Bounds entryBounds = node.localToScreen(node.getBoundsInLocal());

        if (arrowLocation == ArrowLocation.LEFT_TOP || arrowLocation == ArrowLocation.LEFT_BOTTOM) {
            point.setX(entryBounds.getMaxX());
        } else {
            point.setX(entryBounds.getMinX() - arrowSize);
        }

        return point;
    }

    public static void scrollToRequestedTime(DateControl control, DayViewScrollPane scrollPane) {
        LocalTime requestedTime = control.getRequestedTime();
        if (requestedTime != null) {
            scrollPane.scrollToTime(requestedTime);
        }
    }
}
