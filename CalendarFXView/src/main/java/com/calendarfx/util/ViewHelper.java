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
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase;
import impl.com.calendarfx.view.DayViewScrollPane;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
import org.controlsfx.control.PopOver.ArrowLocation;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("javadoc")
public final class ViewHelper {

    public static double getTimeLocation(DayViewBase view, LocalTime time) {
        return getTimeLocation(view, ZonedDateTime.of(view.getDate(), time, view.getZoneId()));
    }

    public static double getTimeLocation(DayViewBase view, LocalTime time, boolean prefHeight) {
        return getTimeLocation(view, ZonedDateTime.of(view.getDate(), time, view.getZoneId()), prefHeight);
    }

    public static double getTimeLocation(DayViewBase view, ZonedDateTime time) {
        return getTimeLocation(view, time, false);
    }

    public static double getTimeLocation(DayViewBase view, Instant instant) {
        return getTimeLocation(view, ZonedDateTime.ofInstant(instant, view.getZoneId()), false);
    }

    public static double getTimeLocation(DayViewBase view, ZonedDateTime zonedTime, boolean prefHeight) {
        if (view.isScrollingEnabled()) {
            final Instant scrollInstant = view.getScrollTime().toInstant();
            final double mpp = DayView.MILLIS_PER_HOUR / view.getHourHeight();
            final long millis = zonedTime.toInstant().toEpochMilli() - scrollInstant.toEpochMilli();
            return millis / mpp;
        }

        double availableHeight = view.getHeight();
        if (prefHeight) {
            availableHeight = view.prefHeight(-1);
        }

        long epochMilli = zonedTime.toInstant().toEpochMilli();

        switch (view.getEarlyLateHoursStrategy()) {
            case SHOW:
                ZonedDateTime startTime = view.getZonedDateTimeMin();
                ZonedDateTime endTime = view.getZonedDateTimeMax();

                long startMillis = startTime.toInstant().toEpochMilli();
                long endMillis = endTime.toInstant().toEpochMilli();

                double mpp = (endMillis - startMillis) / availableHeight;

                return ((int) ((epochMilli - startMillis) / mpp)) + .5;
            case HIDE:
                startTime = view.getZonedDateTimeStart();
                endTime = view.getZonedDateTimeEnd();

                if (zonedTime.isBefore(startTime)) {
                    return -1;
                }

                if (zonedTime.isAfter(endTime)) {
                    return availableHeight;
                }

                startMillis = startTime.toInstant().toEpochMilli();
                endMillis = endTime.toInstant().toEpochMilli();

                mpp = (endMillis - startMillis) / availableHeight;

                return ((int) ((epochMilli - startMillis) / mpp)) + .5;
            case SHOW_COMPRESSED:
                ZonedDateTime minTime = view.getZonedDateTimeMin();
                ZonedDateTime maxTime = view.getZonedDateTimeMax();

                startTime = view.getZonedDateTimeStart();
                endTime = view.getZonedDateTimeEnd();

                long earlyHours = ChronoUnit.HOURS.between(minTime, startTime);
                long lateHours = ChronoUnit.HOURS.between(endTime, maxTime) + 1;

                double hourHeightCompressed = view.getHourHeightCompressed();
                double earlyHeight = hourHeightCompressed * earlyHours;
                double lateHeight = hourHeightCompressed * lateHours;

                if (zonedTime.isBefore(startTime)) {
                    /*
                     * Early compressed hours.
                     */
                    startMillis = minTime.toInstant().toEpochMilli();
                    endMillis = startTime.toInstant().toEpochMilli();

                    mpp = (endMillis - startMillis) / earlyHeight;

                    return ((int) ((epochMilli - startMillis) / mpp)) + .5;
                } else if (zonedTime.isAfter(endTime)) {
                    /*
                     * Late compressed hours.
                     */
                    startMillis = endTime.toInstant().toEpochMilli();
                    endMillis = maxTime.toInstant().toEpochMilli();

                    mpp = (endMillis - startMillis) / lateHeight;

                    return ((int) ((epochMilli - startMillis) / mpp)) + (availableHeight - lateHeight) + .5;
                } else {
                    /*
                     * Regular hours.
                     */
                    startMillis = startTime.toInstant().toEpochMilli();
                    endMillis = endTime.toInstant().toEpochMilli();
                    mpp = (endMillis - startMillis) / (availableHeight - earlyHeight - lateHeight);

                    return earlyHeight + ((int) ((epochMilli - startMillis) / mpp)) + .5;
                }
            default:
                return 0;
        }
    }

    public static Instant getInstantAt(DayViewBase view, double y) {

        ZonedDateTime zonedDateTime = view.getZonedDateTimeStart();

        double availableHeight = view.getHeight();

        switch (view.getEarlyLateHoursStrategy()) {
            case SHOW:
                long startMillis = view.getZonedDateTimeMin().toInstant().toEpochMilli();
                long endMillis = view.getZonedDateTimeMax().toInstant().toEpochMilli();

                double mpp = (endMillis - startMillis) / availableHeight;

                long millis = (long) (mpp * y) + startMillis;

                return Instant.ofEpochMilli(millis);
            case HIDE:
                ZonedDateTime startTime = view.getZonedDateTimeStart();
                ZonedDateTime endTime = view.getZonedDateTimeEnd();

                startMillis = startTime.toInstant().toEpochMilli();
                endMillis = endTime.toInstant().toEpochMilli();

                mpp = (endMillis - startMillis) / availableHeight;

                millis = (long) (mpp * y) + startMillis;

                return Instant.ofEpochMilli(millis);
            case SHOW_COMPRESSED:
                startTime = view.getZonedDateTimeStart();
                endTime = view.getZonedDateTimeEnd();

                ZonedDateTime minTime = view.getZonedDateTimeMin();
                ZonedDateTime maxTime = view.getZonedDateTimeMax();

                long earlyHours = ChronoUnit.HOURS.between(minTime, startTime);
                long lateHours = ChronoUnit.HOURS.between(endTime, maxTime) + 1;

                double hourHeightCompressed = view.getHourHeightCompressed();
                double earlyHeight = hourHeightCompressed * earlyHours;
                double lateHeight = hourHeightCompressed * lateHours;

                if (y < earlyHeight) {
                    /*
                     * Early compressed hours.
                     */
                    startMillis = minTime.toInstant().toEpochMilli();
                    endMillis = startTime.toInstant().toEpochMilli();

                    mpp = (endMillis - startMillis) / earlyHeight;

                    millis = (long) (mpp * y) + startMillis;

                    return Instant.ofEpochMilli(millis);
                } else if (y > availableHeight - lateHeight) {
                    /*
                     * Late compressed hours.
                     */
                    startMillis = endTime.toInstant().toEpochMilli();
                    endMillis = maxTime.toInstant().toEpochMilli();

                    mpp = (endMillis - startMillis) / lateHeight;

                    millis = (long) (mpp * (y - (availableHeight - lateHeight))) + startMillis;

                    return Instant.ofEpochMilli(millis);
                } else {
                    /*
                     * Regular hours.
                     */
                    startMillis = startTime.toInstant().toEpochMilli();
                    endMillis = endTime.toInstant().toEpochMilli();

                    mpp = (endMillis - startMillis) / (availableHeight - earlyHeight - lateHeight);

                    millis = (long) (mpp * (y - earlyHeight)) + startMillis;

                    return Instant.ofEpochMilli(millis);
                }
            default:
                return zonedDateTime.toInstant();
        }
    }

    public static ArrowLocation findPopOverArrowLocation(Node view) {
        Bounds localBounds = view.getBoundsInLocal();
        Bounds entryBounds = view.localToScreen(localBounds);

        ObservableList<Screen> screens = Screen.getScreensForRectangle(
                entryBounds.getMinX(), entryBounds.getMinY(),
                entryBounds.getWidth(), entryBounds.getHeight());

        if (screens.isEmpty()) {
            return null;

        }
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

    public static Point2D findPopOverArrowPosition(Node node, double screenY, double arrowSize, ArrowLocation arrowLocation) {

        Bounds entryBounds = node.localToScreen(node.getBoundsInLocal());

        double screenX;

        if (arrowLocation == ArrowLocation.LEFT_TOP || arrowLocation == ArrowLocation.LEFT_BOTTOM) {
            screenX = entryBounds.getMaxX();
        } else {
            screenX = entryBounds.getMinX() - arrowSize;
        }

        return new Point2D(screenX, screenY);
    }

    public static void scrollToRequestedTime(DateControl control, DayViewScrollPane scrollPane) {
        LocalTime requestedTime = control.getRequestedTime();
        if (requestedTime != null) {
            scrollPane.scrollToTime(requestedTime);
        }
    }
}
