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

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("javadoc")
public final class ViewHelper {

    public static double getTimeLocation(DayViewBase view, LocalTime time) {
        return getTimeLocation(view, time, false, view.getZoneId());
    }
    public static double getTimeLocation(DayViewBase view, LocalTime time, ZoneId zoneId) {
        return getTimeLocation(view, time, false, zoneId);
    }

    public static double getTimeLocation(DayViewBase view, LocalTime time, boolean prefHeight) {
        return getTimeLocation(view, time, prefHeight, view.getZoneId());
    }

    public static double getTimeLocation(DayViewBase view, LocalTime time, boolean prefHeight, ZoneId zoneId) {
        System.out.println("getTimeLocation, zoneID = " + zoneId);

        ZonedDateTime startTime = ZonedDateTime.of(view.getDate(), view.getStartTime(), view.getZoneId());
        ZonedDateTime endTime = ZonedDateTime.of(view.getDate(), view.getEndTime(), view.getZoneId());
        ZonedDateTime zonedTime = ZonedDateTime.of(view.getDate(), time, zoneId);

        System.out.println("zoned time: " + zonedTime);

        double availableHeight = view.getHeight();
        if (prefHeight) {
            availableHeight = view.prefHeight(-1);
        }
        EarlyLateHoursStrategy strategy = view.getEarlyLateHoursStrategy();

        switch (strategy) {
            case SHOW:
                long startMillis = startTime.toInstant().toEpochMilli();
                long endMillis = endTime.toInstant().toEpochMilli();

                double npp = (endMillis - startMillis) / availableHeight;

                return ((int) ((zonedTime.toInstant().toEpochMilli() - startMillis) / npp)) + .5;
            case HIDE:
                if (zonedTime.isBefore(startTime)) {
                    return -1;
                }

                if (zonedTime.isAfter(endTime)) {
                    return availableHeight;
                }

                startMillis = startTime.toInstant().toEpochMilli();
                endMillis = endTime.toInstant().toEpochMilli();

                npp = (endMillis - startMillis) / availableHeight;

                return ((int) ((time.toNanoOfDay() - startMillis) / npp)) + .5;
            case SHOW_COMPRESSED:
                long earlyHours = ChronoUnit.HOURS.between(LocalTime.MIN, view.getStartTime());
                long lateHours = ChronoUnit.HOURS.between(endTime, LocalTime.MAX) + 1;
                double hourHeightCompressed = view.getHourHeightCompressed();
                double earlyHeight = hourHeightCompressed * earlyHours;
                double lateHeight = hourHeightCompressed * lateHours;

                if (zonedTime.isBefore(startTime)) {
                    /*
                     * Early compressed hours.
                     */
                    startMillis = 0;
                    endMillis = startTime.toInstant().toEpochMilli();

                    npp = (endMillis - startMillis) / earlyHeight;

                    return ((int) ((zonedTime.toInstant().toEpochMilli() - startMillis) / npp)) + .5;
                } else if (zonedTime.isAfter(endTime)) {
                    /*
                     * Late compressed hours.
                     */
                    startMillis = endTime.toInstant().toEpochMilli();
                    endMillis = ZonedDateTime.of(view.getDate(), LocalTime.MAX, view.getZoneId()).toInstant().toEpochMilli();

                    npp = (endMillis - startMillis) / lateHeight;

                    return ((int) ((zonedTime.toInstant().toEpochMilli() - startMillis) / npp)) + (availableHeight - lateHeight) + .5;
                } else {
                    /*
                     * Regular hours.
                     */
                    startMillis = startTime.toInstant().toEpochMilli();
                    endMillis = endTime.toInstant().toEpochMilli();
                    npp = (endMillis - startMillis) / (availableHeight - earlyHeight - lateHeight);

                    return earlyHeight + ((int) ((zonedTime.toInstant().toEpochMilli() - startMillis) / npp)) + .5;
                }
            default:
                return 0;
        }
    }

    public static ZonedDateTime getLocationTime(DayViewBase view, double y, boolean trim, boolean prefHeight, ZoneId zoneId) {

        System.out.println("getLocationTime.zoneID: " + zoneId);
        /**
         * When the early and late hours are not showing then we have to trim for sure.
         */
        if (view.getEarlyLateHoursStrategy().equals(EarlyLateHoursStrategy.HIDE)) {
            trim = true;
        }

        ZonedDateTime localDateTime = ZonedDateTime.of(view.getDate(), LocalTime.MIN, view.getZoneId());

        double availableHeight = view.getHeight();
        if (prefHeight) {
            availableHeight = view.prefHeight(-1);
        }

        if (y < 0) {
            if (trim) {
                return ZonedDateTime.of(view.getDate(), view.getStartTime(), view.getZoneId());
            }
            y = availableHeight + y;
            localDateTime = localDateTime.minusDays(1);
        } else if (y > availableHeight) {
            if (trim) {
                return ZonedDateTime.of(view.getDate(), view.getEndTime(), view.getZoneId());
            }
            y = y - availableHeight;
            localDateTime = localDateTime.plusDays(1);
        }

        ZonedDateTime startTime = ZonedDateTime.of(view.getDate(), view.getStartTime(), view.getZoneId());
        ZonedDateTime endTime = ZonedDateTime.of(view.getDate(), view.getEndTime(), view.getZoneId());

        switch (view.getEarlyLateHoursStrategy()) {
            case SHOW:
                long startMillis = ZonedDateTime.of(view.getDate(), LocalTime.MIN, view.getZoneId()).toInstant().toEpochMilli();
                long endMillis = ZonedDateTime.of(view.getDate(), LocalTime.MAX, view.getZoneId()).toInstant().toEpochMilli();

                double mpp = (endMillis - startMillis) / availableHeight;

                long millis = Math.min(endMillis, (long) (mpp * y));

                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis + millis), zoneId);
            case HIDE:
                startMillis = startTime.toInstant().toEpochMilli();
                endMillis = endTime.toInstant().toEpochMilli();

                mpp = (endMillis - startMillis) / availableHeight;

                millis = Math.min(endMillis, (long) (mpp * y) + startMillis);

                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis + millis), zoneId);
            case SHOW_COMPRESSED:
                long earlyHours = ChronoUnit.HOURS.between(LocalTime.MIN, startTime);
                long lateHours = ChronoUnit.HOURS.between(endTime, LocalTime.MAX) + 1;
                double hourHeightCompressed = view.getHourHeightCompressed();
                double earlyHeight = hourHeightCompressed * earlyHours;
                double lateHeight = hourHeightCompressed * lateHours;

                if (y < earlyHeight) {
                    /*
                     * Early compressed hours.
                     */
                    startMillis = LocalTime.MIN.toNanoOfDay();
                    endMillis = startTime.toInstant().toEpochMilli();

                    mpp = (endMillis - startMillis) / earlyHeight;

                    millis = Math.min(LocalTime.MAX.toNanoOfDay(), (long) (mpp * y));

                    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis + millis), zoneId);
                } else if (y > availableHeight - lateHeight) {
                    /*
                     * Late compressed hours.
                     */
                    startMillis = endTime.toInstant().toEpochMilli();
                    endMillis = ZonedDateTime.of(view.getDate(), LocalTime.MAX, view.getZoneId()).toInstant().toEpochMilli();

                    mpp = (endMillis - startMillis) / lateHeight;

                    millis = Math.min(endMillis, (long) (mpp * (y - (availableHeight - lateHeight))) + startMillis);

                    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis + millis), zoneId);
                } else {
                    /*
                     * Regular hours.
                     */
                    startMillis = startTime.toInstant().toEpochMilli();
                    endMillis = endTime.toInstant().toEpochMilli();

                    mpp = (endMillis - startMillis) / (availableHeight - earlyHeight - lateHeight);

                    millis = Math.min(endMillis, (long) (mpp * (y - earlyHeight)) + startMillis);

                    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis + millis), zoneId);
                }
            default:
                return localDateTime;
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
