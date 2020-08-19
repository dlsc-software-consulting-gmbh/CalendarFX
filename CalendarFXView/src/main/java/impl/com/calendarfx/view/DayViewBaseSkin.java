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

package impl.com.calendarfx.view;

import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import javafx.beans.InvalidationListener;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("javadoc")
public class DayViewBaseSkin<T extends DayViewBase> extends DateControlSkin<T> {

    public DayViewBaseSkin(T view) {
        super(view);

        InvalidationListener layoutListener = it -> getSkinnable().requestLayout();

        view.timeProperty().addListener(layoutListener);
        view.hourHeightProperty().addListener(layoutListener);
        view.hourHeightCompressedProperty().addListener(layoutListener);
        view.visibleHoursProperty().addListener(layoutListener);
        view.earlyLateHoursStrategyProperty().addListener(layoutListener);
        view.hoursLayoutStrategyProperty().addListener(layoutListener);
        view.startTimeProperty().addListener(layoutListener);
        view.endTimeProperty().addListener(layoutListener);
        view.getCalendars().addListener(layoutListener);
        view.enableCurrentTimeMarkerProperty().addListener(layoutListener);
        view.entryWidthPercentageProperty().addListener(layoutListener);
        view.showTodayProperty().addListener(layoutListener);

        view.setOnScroll(evt -> {
            final double oldLocation = evt.getY();
            final ZonedDateTime time = view.getZonedDateTimeAt(0, oldLocation);

            if (view.isScrollingEnabled()) {
                if (evt.isShortcutDown()) {
                    // first compute new hour height
                    final double delta = Math.max(-5, Math.min(5, evt.getDeltaY()));
                    view.setHourHeight(Math.min(getSkinnable().getMaxHourHeight(), Math.max(getSkinnable().getMinHourHeight(), view.getHourHeight() + delta)));

                    // then adjust scroll time to make sure the time found at mouse location stays where it is
                    final double newLocation = view.getLocation(time);
                    final double locationDelta = newLocation - oldLocation;
                    final ZonedDateTime newScrollTime = view.getZonedDateTimeAt(0, locationDelta);
                    view.setScrollTime(newScrollTime);

                } else {
                    view.setScrollTime(getSkinnable().getZonedDateTimeAt(0, -evt.getDeltaY()));
                }
            }
        });
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {

        if (getSkinnable().isScrollingEnabled()) {
            return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        T dayView = getSkinnable();
        double hourHeight = dayView.getHourHeight();

        EarlyLateHoursStrategy strategy = dayView.getEarlyLateHoursStrategy();

        switch (strategy) {
            case HIDE:
                long earlyHours = ChronoUnit.HOURS.between(LocalTime.MIN, dayView.getStartTime());
                long lateHours = ChronoUnit.HOURS.between(dayView.getEndTime(), LocalTime.MAX) + 1;
                long hours = 24 - earlyHours - lateHours;
                return hours * hourHeight;

            case SHOW:
                return 24 * hourHeight;

            case SHOW_COMPRESSED:
                earlyHours = ChronoUnit.HOURS.between(LocalTime.MIN, dayView.getStartTime());
                lateHours = ChronoUnit.HOURS.between(dayView.getEndTime(), LocalTime.MAX) + 1;
                hours = 24 - earlyHours - lateHours;
                double hourHeightCompressed = dayView.getHourHeightCompressed();
                return hours * hourHeight + (earlyHours + lateHours) * hourHeightCompressed;

            default:
                throw new IllegalArgumentException("unsupported early / late hours strategy: " + strategy);
        }
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().isScrollingEnabled()) {
            return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().isScrollingEnabled()) {
            return super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }
}
