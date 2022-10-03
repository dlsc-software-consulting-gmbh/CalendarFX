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

package com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.util.ViewHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.controlsfx.control.PropertySheet.Item;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

/**
 * The common superclass for all date controls that are used to display the 24
 * hours of a day: day view, week day view, timescale, week timescale, and
 * week view. Instances of this type can be configured to display hours at a
 * fixed height or alternatively a fixed number of hours for a given viewport
 * height (see {@link HoursLayoutStrategy}). This control also supports an early /
 * late hours concept where these hours can either be shown, or hidden, or shown
 * in a compressed way (see {@link EarlyLateHoursStrategy}). The idea behind
 * early / late hours is that often applications do not work with all 24 hours
 * of a day and hiding or compressing these hours allow the user to focus on
 * the relevant hours.
 */
public abstract class DayViewBase extends DateControl implements ZonedDateTimeProvider {

    /**
     * Constructs a new view.
     */
    public DayViewBase() {
        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("show.current.time.marker")) {
                    Boolean show = (Boolean) change.getValueAdded();
                    showCurrentTimeMarker.set(show);
                } else if (change.getKey().equals("show.current.time.today.marker")) {
                    Boolean show = (Boolean) change.getValueAdded();
                    showCurrentTimeTodayMarker.set(show);
                } else if (change.getKey().equals("earliest.time.used")) {
                    LocalTime time = (LocalTime) change.getValueAdded();
                    earliestTimeUsed.set(time);
                } else if (change.getKey().equals("latest.time.used")) {
                    LocalTime time = (LocalTime) change.getValueAdded();
                    latestTimeUsed.set(time);
                }
            }
        };

        getProperties().addListener(propertiesListener);

        final InvalidationListener trimListener = it -> {
            if (isTrimTimeBounds()) {
                trimTimeBounds();
            }
        };

        earliestTimeUsedProperty().addListener(trimListener);
        latestTimeUsedProperty().addListener(trimListener);
        trimTimeBoundsProperty().addListener(trimListener);

        installDefaultLassoFinishedBehaviour();
    }

    /**
     * A list of possible grid types supported by the day view.
     *
     * @see #gridTypeProperty()
     * @see #gridLinesProperty()
     * @see #gridLineColorProperty()
     */
    public enum GridType {
        STANDARD,
        CUSTOM
    }

    private final ObjectProperty<GridType> gridType = new SimpleObjectProperty<>(this, "gridType", GridType.STANDARD);

    public final GridType getGridType() {
        return gridType.get();
    }

    /**
     * Determines the type of grid / grid lines will be used for full hours,
     * half hours, and so on. The {@link GridType#STANDARD} only supports grid lines
     * for full hours and half hours. The {@link GridType#CUSTOM} can be configured
     * via a {@link VirtualGrid} to show any kind of grid lines.
     *
     * @return the grid type
     *
     * @see #gridLinesProperty()
     * @see #gridLineColorProperty()
     */
    public final ObjectProperty<GridType> gridTypeProperty() {
        return gridType;
    }

    public final void setGridType(GridType gridType) {
        this.gridType.set(gridType);
    }

    private final ObjectProperty<VirtualGrid> gridLines = new SimpleObjectProperty<>(this, "virtualGrid", new VirtualGrid("Grid Lines", "Grid", ChronoUnit.MINUTES, 30));

    public final VirtualGrid getGridLines() {
        return gridLines.get();
    }

    /**
     * A virtual grid used to control the placement of lightweight grid lines in the background
     * of the view. These grid lines are drawn via the canvas API.
     *
     * @return the grid lines virtual grid object
     */
    public final ObjectProperty<VirtualGrid> gridLinesProperty() {
        return gridLines;
    }

    public final void setGridLines(VirtualGrid gridLines) {
        this.gridLines.set(gridLines);
    }

    private final ObjectProperty<Paint> gridLineColor = new SimpleObjectProperty<>(this, "gridLineColor", Color.LIGHTGRAY);

    public final Paint getGridLineColor() {
        return gridLineColor.get();
    }

    /**
     * A color used to draw the lightweight grid lines in a background canvas.
     *
     * @return the grid line color
     */
    public final ObjectProperty<Paint> gridLineColorProperty() {
        return gridLineColor;
    }

    public final void setGridLineColor(Paint gridLineColor) {
        this.gridLineColor.set(gridLineColor);
    }

    /**
     * A list of possible ways that entries can behave when the user switches
     * to availability editing. The values determine if the entries should
     * disappear, if they should become semi-transparent, or if they should stay
     * completely visible.
     */
    public enum AvailabilityEditingEntryBehaviour {
        /**
         * Entry views will stay visible.
         */
        SHOW,

        /**
         * Entry views will be completely hidden.
         */
        HIDE,

        /**
         * Entry views will be semi-transparent.
         */
        OPACITY
    }

    private final ObjectProperty<AvailabilityEditingEntryBehaviour> entryViewAvailabilityEditingBehaviour = new SimpleObjectProperty<>(this, "entryViewAvailabilityEditingBehaviour", AvailabilityEditingEntryBehaviour.OPACITY);

    public final AvailabilityEditingEntryBehaviour getEntryViewAvailabilityEditingBehaviour() {
        return entryViewAvailabilityEditingBehaviour.get();
    }

    /**
     * Determines how entry views should behave when the user switches to the availability editing
     * mode. Entries can disappear, they can become semi-transparent, or the can stay completely
     * visible.
     *
     * @return the behaviour of entry views when in availability editing mode
     * @see DateControl#setEditAvailability(boolean)
     */
    public final ObjectProperty<AvailabilityEditingEntryBehaviour> entryViewAvailabilityEditingBehaviourProperty() {
        return entryViewAvailabilityEditingBehaviour;
    }

    public final void setEntryViewAvailabilityEditingBehaviour(AvailabilityEditingEntryBehaviour behaviour) {
        this.entryViewAvailabilityEditingBehaviour.set(behaviour);
    }

    private final DoubleProperty entryViewAvailabilityEditingOpacity = new SimpleDoubleProperty(this, "entryViewAvailabilityEditingOpacity", .25);

    public final double getEntryViewAvailabilityEditingOpacity() {
        return entryViewAvailabilityEditingOpacity.get();
    }

    /**
     * A double value that determines how opaque entry views will be while the user is
     * performing availability editing.
     *
     * @return the opacity value
     */
    public final DoubleProperty entryViewAvailabilityEditingOpacityProperty() {
        return entryViewAvailabilityEditingOpacity;
    }

    public final void setEntryViewAvailabilityEditingOpacity(double entryViewAvailabilityEditingOpacity) {
        this.entryViewAvailabilityEditingOpacity.set(entryViewAvailabilityEditingOpacity);
    }

    /**
     * Registers a {@link #onLassoFinishedProperty()} that will add a
     * calendar entry for the lasso selection into the current availability
     * calendar.
     *
     * @see #availabilityCalendarProperty()
     */
    public void installDefaultLassoFinishedBehaviour() {
        setOnLassoFinished((start, end) -> {
            if (start == null || end == null) {
                return;
            }

            if (isEditAvailability()) {
                if (start.isAfter(end)) {
                    Instant tmp = start;
                    start = end;
                    end = tmp;
                }

                Entry<?> entry = new Entry<>("Availability Entry", new Interval(start, end, getZoneId()));
                Calendar availabilityCalendar = getAvailabilityCalendar();
                availabilityCalendar.addEntry(entry);
            }
        });
    }

    /**
     * Returns the zoned start time of the day view for the view's current date,
     * its start time, and its time zone.
     *
     * @return the zoned start time of the view
     * @see #getDate()
     * @see #getStartTime()
     * @see #getZoneId()
     */
    public final ZonedDateTime getZonedDateTimeStart() {
        return ZonedDateTime.of(getDate(), getStartTime(), getZoneId());
    }

    /**
     * Returns the zoned minimum time of the day view for the view's current date,
     * for {@link LocalTime#MIN}, and its time zone.
     *
     * @return the zoned minimum time of the view
     * @see #getDate()
     * @see #getZoneId()
     */
    public final ZonedDateTime getZonedDateTimeMin() {
        return ZonedDateTime.of(getDate(), LocalTime.MIN, getZoneId());
    }

    /**
     * Returns the zoned end time of the day view for the view's current date,
     * its end time, and its time zone.
     *
     * @return the zoned end time of the view
     * @see #getDate()
     * @see #getEndTime() ()
     * @see #getZoneId()
     */
    public final ZonedDateTime getZonedDateTimeEnd() {
        return ZonedDateTime.of(getDate(), getEndTime(), getZoneId());
    }

    /**
     * Returns the zoned maximum time of the day view for the view's current date,
     * for {@link LocalTime#MAX}, and its time zone.
     *
     * @return the zoned maximum time of the view
     * @see #getDate()
     * @see #getZoneId()
     */
    public final ZonedDateTime getZonedDateTimeMax() {
        return ZonedDateTime.of(getDate(), LocalTime.MAX, getZoneId());
    }

    private final ObjectProperty<ZonedDateTime> scrollTime = new SimpleObjectProperty<>(this, "scrollTime", ZonedDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneId.systemDefault()));

    public final ZonedDateTime getScrollTime() {
        return scrollTime.get();
    }

    public final ObjectProperty<ZonedDateTime> scrollTimeProperty() {
        return scrollTime;
    }

    public final void setScrollTime(ZonedDateTime scrollTime) {
        this.scrollTime.set(scrollTime);
    }

    private final BooleanProperty scrollingEnabled = new SimpleBooleanProperty(this, "scrollingEnabled", false);

    public final boolean isScrollingEnabled() {
        return scrollingEnabled.get();
    }

    public final BooleanProperty scrollingEnabledProperty() {
        return scrollingEnabled;
    }

    public final void setScrollingEnabled(boolean scrollingEnabled) {
        this.scrollingEnabled.set(scrollingEnabled);
    }

    private final DoubleProperty entryWidthPercentage = new SimpleDoubleProperty(this, "entryWidthPercentage", 100) {

        @Override
        public void set(double newValue) {
            if (newValue < 10 || newValue > 100) {
                throw new IllegalArgumentException("percentage width must be between 10 and 100 but was " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * A percentage value used to specify how much of the available width inside the
     * view will be utilized by the entry views. The default value is 100%, however
     * applications might want to set a smaller value to allow the user to click and
     * create new entries in already used time intervals.
     *
     * @return the entry percentage width
     */
    public final DoubleProperty entryWidthPercentageProperty() {
        return entryWidthPercentage;
    }

    /**
     * Sets the value of {@link #entryWidthPercentage}.
     *
     * @param percentage the new percentage width
     */
    public final void setEntryWidthPercentage(double percentage) {
        this.entryWidthPercentage.set(percentage);
    }

    /**
     * Returns the value of {@link #entryWidthPercentageProperty()}.
     *
     * @return the percentage width
     */
    public double getEntryWidthPercentage() {
        return entryWidthPercentage.get();
    }

    public static final double MILLIS_PER_HOUR = 3_600_000d;

    /**
     * Returns the time instant at the location of the given mouse event.
     *
     * @param evt the mouse event
     * @return the time at the mouse event location
     */
    public Instant getInstantAt(MouseEvent evt) {
        return getInstantAt(evt.getX(), evt.getY());
    }

    /**
     * Returns the time instant at the given location.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the time at the given location
     */
    public Instant getInstantAt(double x, double y) {
        return getZonedDateTimeAt(x, y).toInstant();
    }

    /**
     * Returns the zoned date time at the given location.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the time at the given location
     */
    public ZonedDateTime getZonedDateTimeAt(double x, double y) {
        return getZonedDateTimeAt(x, y, getZoneId());
    }

    @Override
    public ZonedDateTime getZonedDateTimeAt(double x, double y, ZoneId zoneId) {
        if (isScrollingEnabled()) {

            final double mpp = MILLIS_PER_HOUR / getHourHeight();
            final long millis = (long) (y * mpp);
            return getScrollTime().plus(millis, ChronoUnit.MILLIS);

        } else {

            return ZonedDateTime.ofInstant(ViewHelper.getInstantAt(this, y), getZoneId());

        }
    }

    /**
     * Returns the zoned date time version of the given time for the view and
     * its current date and time zone.
     *
     * @param time the local time to convert to a zoned date time
     * @return the zoned date time version of the given time
     */
    public final ZonedDateTime getZonedDateTime(LocalTime time) {
        return ZonedDateTime.of(getDate(), time, getZoneId());
    }

    /**
     * Returns the y coordinate for the given time instance. Uses {@link #getZoneId()} to
     * create a {@link ZonedDateTime} object on-the-fly.
     *
     * @param instant the time for which to return the coordinate
     * @return the y coordinate
     */
    public final double getLocation(Instant instant) {
        return ViewHelper.getTimeLocation(this, instant);
    }

    /**
     * Returns the y coordinate for the given time. This method delegates
     * to {@link ViewHelper#getTimeLocation(DayViewBase, ZonedDateTime)}.
     *
     * @param time the time for which to return the coordinate
     * @return the y coordinate
     * @throws UnsupportedOperationException if {@link #scrollingEnabled} is set to true
     */
    public final double getLocation(ZonedDateTime time) {
        return ViewHelper.getTimeLocation(this, time);
    }

    /**
     * An enum listing possible methods of determining overlapping entries. Calendar entries
     * can either overlap because of their time intervals or because of their visual bounds.
     *
     * @see DayEntryView#alignmentStrategyProperty()
     */
    public enum OverlapResolutionStrategy {
        TIME_BOUNDS,
        VISUAL_BOUNDS,
        OFF,
    }

    private final ObjectProperty<OverlapResolutionStrategy> overlapResolutionStrategy = new SimpleObjectProperty<>(this, "overlapCriteria", OverlapResolutionStrategy.TIME_BOUNDS);

    /**
     * A property used to instruct the view how to resolve overlapping
     * entries, whether the resolution should be based on the time intervals of calendar
     * entries or on their visual bounds or completely disabled. The default value is
     * {@link OverlapResolutionStrategy#TIME_BOUNDS}.
     *
     * @return the overlap resolution strategy
     */
    public final ObjectProperty<OverlapResolutionStrategy> overlapResolutionStrategyProperty() {
        return overlapResolutionStrategy;
    }

    public final OverlapResolutionStrategy getOverlapResolutionStrategy() {
        return overlapResolutionStrategy.get();
    }

    public final void setOverlapResolutionStrategy(OverlapResolutionStrategy strategy) {
        this.overlapResolutionStrategy.set(strategy);
    }

    /**
     * An enumerator used for specifying how to deal with late and early hours.
     *
     * @see DayViewBase#earlyLateHoursStrategyProperty()
     * @see DayViewBase#startTimeProperty()
     * @see DayViewBase#endTimeProperty()
     */
    public enum EarlyLateHoursStrategy {

        /**
         * Show all hours with the same height.
         */
        SHOW,

        /**
         * Shows early / late hours with a compressed height and all other hours
         * with the standard height.
         *
         * @see DayViewBase#setHourHeight(double)
         * @see DayViewBase#setHourHeightCompressed(double)
         */
        SHOW_COMPRESSED,

        /**
         * Hide the early / late hours.
         */
        HIDE
    }

    private final ObjectProperty<EarlyLateHoursStrategy> earlyLateHoursStrategy = new SimpleObjectProperty<>(this, "earlyLateHoursStrategy", EarlyLateHoursStrategy.SHOW);

    /**
     * Specifies a strategy for dealing with early / late hours. The idea behind
     * early / late hours is that often applications do not work with all 24
     * hours of a day and hiding or compressing these hours allow the user to
     * focus on the relevant hours.
     *
     * @return the early / late hours strategy
     * @see #startTimeProperty()
     * @see #endTimeProperty()
     */
    public final ObjectProperty<EarlyLateHoursStrategy> earlyLateHoursStrategyProperty() {
        return earlyLateHoursStrategy;
    }

    /**
     * Sets the value of {@link #earlyLateHoursStrategyProperty()}.
     *
     * @param strategy the strategy to use for early / late hours
     */
    public final void setEarlyLateHoursStrategy(EarlyLateHoursStrategy strategy) {
        requireNonNull(strategy);
        earlyLateHoursStrategy.set(strategy);
    }

    /**
     * Returns the value of {@link #earlyLateHoursStrategyProperty()}.
     *
     * @return the early / late hours strategy
     */
    public final EarlyLateHoursStrategy getEarlyLateHoursStrategy() {
        return earlyLateHoursStrategy.get();
    }

    /**
     * An enumerator used for specifying how to lay out hours. The view can
     * either show a fixed number of hours in the available viewport or each
     * hour at a fixed height.
     *
     * @see DayViewBase#hoursLayoutStrategyProperty()
     * @see DayViewBase#visibleHoursProperty()
     */
    public enum HoursLayoutStrategy {

        /**
         * Keeps the height of each hour constant.
         */
        FIXED_HOUR_HEIGHT,

        /**
         * Keeps the number of hours shown in the viewport of the scrollpane
         * constant.
         */
        FIXED_HOUR_COUNT
    }

    private final ObjectProperty<HoursLayoutStrategy> hoursLayoutStrategy = new SimpleObjectProperty<>(this, "hoursLayoutStrategy", HoursLayoutStrategy.FIXED_HOUR_COUNT);

    /**
     * The layout strategy used by this view for showing hours. The view can
     * either show a fixed number of hours in the available viewport or each
     * hour at a fixed height.
     *
     * @return the hours layout strategy
     * @see DayViewBase#visibleHoursProperty()
     */
    public final ObjectProperty<HoursLayoutStrategy> hoursLayoutStrategyProperty() {
        return hoursLayoutStrategy;
    }

    /**
     * Sets the value of {@link #hoursLayoutStrategyProperty()}.
     *
     * @param strategy the hours layout strategy
     */
    public final void setHoursLayoutStrategy(HoursLayoutStrategy strategy) {
        requireNonNull(strategy);
        hoursLayoutStrategy.set(strategy);
    }

    /**
     * Returns the value of {@link #hoursLayoutStrategyProperty()}.
     *
     * @return the hours layout strategy
     */
    public final HoursLayoutStrategy getHoursLayoutStrategy() {
        return hoursLayoutStrategy.get();
    }

    private final IntegerProperty visibleHours = new SimpleIntegerProperty(this, "visibleHours", 10);

    /**
     * The number of visible hours that the application wants to present to the
     * user at any time.
     *
     * @return the number of visible hours
     * @see #hoursLayoutStrategyProperty()
     */
    public final IntegerProperty visibleHoursProperty() {
        return visibleHours;
    }

    /**
     * Sets the value of {@link #visibleHoursProperty()}.
     *
     * @param hours the number of visible hours
     */
    public final void setVisibleHours(int hours) {
        visibleHoursProperty().set(hours);
    }

    /**
     * Returns the value of {@link #visibleHoursProperty()}.
     *
     * @return the number of visible hours
     */
    public final int getVisibleHours() {
        return visibleHoursProperty().get();
    }

    private final DoubleProperty minHourHeight = new SimpleDoubleProperty(this, "hourHeight", 20);

    public final double getMinHourHeight() {
        return minHourHeight.get();
    }

    /**
     * Returns the minimum height of an hour interval. Used for zoom in / out operations.
     *
     * @return the minimum hour height
     * @see #hourHeightProperty()
     */
    public final DoubleProperty minHourHeightProperty() {
        return minHourHeight;
    }

    public final void setMinHourHeight(double minHourHeight) {
        this.minHourHeight.set(minHourHeight);
    }

    private final DoubleProperty maxHourHeight = new SimpleDoubleProperty(this, "hourHeight", 200);

    public final double getMaxHourHeight() {
        return maxHourHeight.get();
    }

    /**
     * Returns the maximum height of an hour interval. Used for zoom in / out operations.
     *
     * @return the maximum hour height
     * @see #hourHeightProperty()
     */
    public final DoubleProperty maxHourHeightProperty() {
        return maxHourHeight;
    }

    public final void setMaxHourHeight(double maxHourHeight) {
        this.maxHourHeight.set(maxHourHeight);
    }

    private final DoubleProperty hourHeight = new SimpleDoubleProperty(this, "hourHeight", 70);

    /**
     * The height used for each hour shown by the view.
     *
     * @return the hour height
     * @see #hoursLayoutStrategyProperty()
     */
    public final DoubleProperty hourHeightProperty() {
        return hourHeight;
    }

    /**
     * Sets the value of {@link #hourHeightProperty()}.
     *
     * @param height the hour height
     */
    public final void setHourHeight(double height) {
        if (height < 1) {
            throw new IllegalArgumentException("height must be larger than 0 but was " + height);
        }
        hourHeightProperty().set(height);
    }

    /**
     * Returns the value of {@link #hourHeightProperty()}.
     *
     * @return the hour height
     */
    public final double getHourHeight() {
        return hourHeightProperty().get();
    }

    private final DoubleProperty hourHeightCompressed = new SimpleDoubleProperty(this, "hourHeightCompressed", 10);

    /**
     * The height used for each early / late hour shown by the view when using
     * the {@link EarlyLateHoursStrategy#SHOW_COMPRESSED} strategy.
     *
     * @return the compressed hour height
     * @see #hoursLayoutStrategyProperty()
     */
    public final DoubleProperty hourHeightCompressedProperty() {
        return hourHeightCompressed;
    }

    /**
     * Sets the value of {@link #hourHeightCompressedProperty()}.
     *
     * @param height the compressed hour height
     */
    public final void setHourHeightCompressed(double height) {
        if (height < 1) {
            throw new IllegalArgumentException("height must be larger than 0 but was " + height);
        }
        hourHeightCompressedProperty().set(height);
    }

    /**
     * Returns the value of {@link #hourHeightCompressedProperty()}.
     *
     * @return the compressed hour height
     */
    public final double getHourHeightCompressed() {
        return hourHeightCompressedProperty().get();
    }

    // Current time marker support.

    private final ReadOnlyBooleanWrapper showCurrentTimeMarker = new ReadOnlyBooleanWrapper(this, "showCurrentTimeMarker", false);

    /**
     * A read-only property used to indicate whether the view should show the
     * "current time" marker. Normally a day view will show this marker if the
     * date shown by the view is equal to the value of "today". Depending on the
     * subclass the marker can be a red label (see {@link TimeScaleView}) or a
     * thin red line (see red time label and line in image below).
     *
     * <img src="doc-files/current-time-marker.png" alt="Current Time Marker">
     *
     * @return true if the current time marker should be shown
     * @see #showCurrentTimeTodayMarkerProperty()
     * @see #dateProperty()
     * @see #todayProperty()
     */
    public final ReadOnlyBooleanProperty showCurrentTimeMarkerProperty() {
        return showCurrentTimeMarker;
    }

    /**
     * Returns the value of {@link #showCurrentTimeMarkerProperty()}.
     *
     * @return true if the view should display the marker
     */
    public final boolean isShowCurrentTimeMarker() {
        return showCurrentTimeMarker.get();
    }

    private final ReadOnlyBooleanWrapper showCurrentTimeTodayMarker = new ReadOnlyBooleanWrapper(this, "showCurrentTimeTodayMarker", false);

    /**
     * A read-only property used to indicate whether the view should show the
     * "current time today" marker. Normally a day view will show this marker if
     * the date shown by the view is equal to the value of "today". The default
     * marker is a red circle (see circle in the image below).
     *
     * <img src="doc-files/current-time-marker.png" alt="Current Time Marker">
     *
     * @return true if the current time marker should be shown
     * @see #showCurrentTimeMarkerProperty()
     * @see #dateProperty()
     * @see #todayProperty()
     */
    public final ReadOnlyBooleanProperty showCurrentTimeTodayMarkerProperty() {
        return showCurrentTimeTodayMarker;
    }

    /**
     * Returns the value of {@link #showCurrentTimeTodayMarkerProperty()}.
     *
     * @return true if the view should show the marker
     */
    public final boolean isShowCurrentTimeTodayMarker() {
        return showCurrentTimeTodayMarker.get();
    }

    private final BooleanProperty enableCurrentTimeCircle = new SimpleBooleanProperty(this, "enableCurrentTimeCircle", true);

    /**
     * A property used to signal whether the application wants to use the red (default) dot
     * used for marking the current system time. The default value is true, so the dot will be
     * shown.
     *
     * @return true if the current time will be marked with a red dot
     */
    public final BooleanProperty enableCurrentTimeCircleProperty() {
        return enableCurrentTimeCircle;
    }

    /**
     * Returns the value of {@link #enableCurrentTimeCircleProperty}.
     *
     * @return true if the current time will be marked with a red dot
     */
    public final boolean isEnableCurrentCircleMarker() {
        return enableCurrentTimeCircle.get();
    }

    /**
     * Sets the value of {@link #enableCurrentTimeCircleProperty}.
     *
     * @param enable if true the current time will be marked with a red dot
     */
    public final void setEnableCurrentTimeCircle(boolean enable) {
        enableCurrentTimeCircle.set(enable);
    }

    private final BooleanProperty enableCurrentTimeMarker = new SimpleBooleanProperty(this, "enableCurrentTimeMarker", true);

    /**
     * A property used to signal whether the application wants to use the red (default) line
     * used for marking the current system time. The default value is true, so the line will be
     * shown.
     *
     * @return true if the current time will be marked with a red line / text
     */
    public final BooleanProperty enableCurrentTimeMarkerProperty() {
        return enableCurrentTimeMarker;
    }

    /**
     * Returns the value of {@link #enableCurrentTimeMarkerProperty}.
     *
     * @return true if the current time will be marked with a red line / text
     */
    public final boolean isEnableCurrentTimeMarker() {
        return enableCurrentTimeMarker.get();
    }

    /**
     * Sets the value of {@link #enableCurrentTimeMarkerProperty}.
     *
     * @param enable if true the current time will be marked with a red line / text
     */
    public final void setEnableCurrentTimeMarker(boolean enable) {
        enableCurrentTimeMarker.set(enable);
    }

    private final ReadOnlyObjectWrapper<LocalTime> earliestTimeUsed = new ReadOnlyObjectWrapper<>(this, "earliestTimeUsed");

    /**
     * A read-only property used for informing the application about the earliest time point used
     * by any of the calendar entries currently shown by the view.
     *
     * @return the earliest time used by the view
     */
    public final ReadOnlyObjectProperty<LocalTime> earliestTimeUsedProperty() {
        return earliestTimeUsed.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #earliestTimeUsedProperty()}.
     *
     * @return the earliest time used
     */
    public final LocalTime getEarliestTimeUsed() {
        return earliestTimeUsed.get();
    }

    private final ReadOnlyObjectWrapper<LocalTime> latestTimeUsed = new ReadOnlyObjectWrapper<>(this, "latestTimeUsed");

    /**
     * A read-only property used for informing the application about the latest time point used
     * by any of the calendar entries currently shown by the view.
     *
     * @return the latest time used by the view
     */
    public final ReadOnlyObjectProperty<LocalTime> latestTimeUsedProperty() {
        return latestTimeUsed.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #latestTimeUsedProperty()}.
     *
     * @return the latest time used
     */
    public final LocalTime getLatestTimeUsed() {
        return latestTimeUsed.get();
    }

    private final BooleanProperty trimTimeBounds = new SimpleBooleanProperty(this, "timTimeBounds", false);

    /**
     * A property to control whether the view should automatically adjust the earliest and latest
     * time used properties based on the currently showing entries.
     *
     * @return true if the time bounds will be automatically trimmed
     * @see #earliestTimeUsedProperty()
     * @see #latestTimeUsedProperty()
     */
    public final BooleanProperty trimTimeBoundsProperty() {
        return trimTimeBounds;
    }

    public final boolean isTrimTimeBounds() {
        return trimTimeBounds.get();
    }

    public final void setTrimTimeBounds(boolean trimTimeBounds) {
        this.trimTimeBounds.set(trimTimeBounds);
    }

    private void trimTimeBounds() {
        if (this instanceof WeekDayView) {
            return;
        }

        LoggingDomain.PRINTING.fine("trimming hours");

        LocalTime st = LocalTime.of(8, 0);
        LocalTime et = LocalTime.of(19, 0);

        LocalTime etu = getEarliestTimeUsed();
        LocalTime ltu = getLatestTimeUsed();

        LoggingDomain.PRINTING.fine("earliest time: " + etu + ", latest time: " + ltu);

        setEarlyLateHoursStrategy(EarlyLateHoursStrategy.HIDE);

        if (etu != null && ltu != null && ltu.isAfter(etu)) {
            // some padding before the first entry
            if (!etu.isBefore(LocalTime.of(1, 0))) {
                etu = etu.minusHours(1);
            } else {
                etu = LocalTime.MIN;
            }

            // some padding after the last entry
            if (!ltu.isAfter(LocalTime.of(23, 0))) {
                ltu = ltu.plusHours(1);
            } else {
                ltu = LocalTime.MAX;
            }

            // only adjust start time if it is too late
            if (etu.isBefore(st.plusHours(1))) {
                setStartTime(etu);
            } else {
                setStartTime(st);
            }

            // only adjust end time if it is too early
            if (ltu.isAfter(et.minusHours(1))) {
                setEndTime(ltu);
            } else {
                setEndTime(et);
            }
        } else {
            setStartTime(st);
            setEndTime(et);
        }

        setVisibleHours(Math.min(24, (int) getStartTime().until(getEndTime(), ChronoUnit.HOURS)));
    }

    private final ObjectProperty<BiConsumer<Instant, Instant>> onLassoFinished = new SimpleObjectProperty<>(this, "onLassoFinished", (start, end) -> {});

    public final BiConsumer<Instant, Instant> getOnLassoFinished() {
        return onLassoFinished.get();
    }

    public final ObjectProperty<BiConsumer<Instant, Instant>> onLassoFinishedProperty() {
        return onLassoFinished;
    }

    public final void setOnLassoFinished(BiConsumer<Instant, Instant> onLassoFinished) {
        this.onLassoFinished.set(onLassoFinished);
    }

    private final ObjectProperty<Paint> lassoColor = new SimpleObjectProperty<>(this, "lassoColor", Color.GREY);

    public final Paint getLassoColor() {
        return lassoColor.get();
    }

    public final ObjectProperty<Paint> lassoColorProperty() {
        return lassoColor;
    }

    public final void setLassoColor(Paint lassoColor) {
        this.lassoColor.set(lassoColor);
    }

    private final ObjectProperty<Instant> lassoStart = new SimpleObjectProperty<>(this, "lassoStart");

    public final Instant getLassoStart() {
        return lassoStart.get();
    }

    public final ObjectProperty<Instant> lassoStartProperty() {
        return lassoStart;
    }

    public final void setLassoStart(Instant lassoStart) {
        this.lassoStart.set(lassoStart);
    }

    private final ObjectProperty<Instant> lassoEnd = new SimpleObjectProperty<>(this, "lassoEnd");

    public final Instant getLassoEnd() {
        return lassoEnd.get();
    }

    public final ObjectProperty<Instant> lassoEndProperty() {
        return lassoEnd;
    }

    public final void setLassoEnd(Instant lassoEnd) {
        this.lassoEnd.set(lassoEnd);
    }

    private final BooleanProperty enableStartAndEndTimesFlip = new SimpleBooleanProperty(this, "enableStartAndEndTimesFlip", false);

    public final boolean isEnableStartAndEndTimesFlip() {
        return enableStartAndEndTimesFlip.get();
    }

    /**
     * Determines whether the user can flip the start and the end time of an entry by
     * dragging either one to the other side of the other one.
     *
     * @return whether start and end times can be flipped
     */
    public final BooleanProperty enableStartAndEndTimesFlipProperty() {
        return enableStartAndEndTimesFlip;
    }

    public final void setEnableStartAndEndTimesFlip(boolean enableStartAndEndTimesFlip) {
        this.enableStartAndEndTimesFlip.set(enableStartAndEndTimesFlip);
    }

    /**
     * Invokes {@link DateControl#bind(DateControl, boolean)} and adds some more
     * bindings between this control and the given control.
     *
     * @param otherControl the control that will be bound to this control
     * @param bindDate     if true will also bind the date property
     */
    public final void bind(DayViewBase otherControl, boolean bindDate) {
        super.bind(otherControl, bindDate);

        Bindings.bindBidirectional(otherControl.earlyLateHoursStrategyProperty(), earlyLateHoursStrategyProperty());
        Bindings.bindBidirectional(otherControl.hoursLayoutStrategyProperty(), hoursLayoutStrategyProperty());
        Bindings.bindBidirectional(otherControl.hourHeightProperty(), hourHeightProperty());
        Bindings.bindBidirectional(otherControl.hourHeightCompressedProperty(), hourHeightCompressedProperty());
        Bindings.bindBidirectional(otherControl.visibleHoursProperty(), visibleHoursProperty());
        Bindings.bindBidirectional(otherControl.enableCurrentTimeMarkerProperty(), enableCurrentTimeMarkerProperty());
        Bindings.bindBidirectional(otherControl.enableCurrentTimeCircleProperty(), enableCurrentTimeCircleProperty());
        Bindings.bindBidirectional(otherControl.trimTimeBoundsProperty(), trimTimeBoundsProperty());
        Bindings.bindBidirectional(otherControl.scrollingEnabledProperty(), scrollingEnabledProperty());
        Bindings.bindBidirectional(otherControl.scrollTimeProperty(), scrollTimeProperty());
        Bindings.bindBidirectional(otherControl.overlapResolutionStrategyProperty(), overlapResolutionStrategyProperty());
        Bindings.bindBidirectional(otherControl.lassoStartProperty(),  lassoStartProperty());
        Bindings.bindBidirectional(otherControl.lassoEndProperty(),  lassoEndProperty());
        Bindings.bindBidirectional(otherControl.onLassoFinishedProperty(),  onLassoFinishedProperty());
        Bindings.bindBidirectional(otherControl.startTimeProperty(), startTimeProperty());
        Bindings.bindBidirectional(otherControl.endTimeProperty(), endTimeProperty());
        Bindings.bindBidirectional(otherControl.entryViewAvailabilityEditingBehaviourProperty(), entryViewAvailabilityEditingBehaviourProperty());
        Bindings.bindBidirectional(otherControl.entryViewAvailabilityEditingOpacityProperty(), entryViewAvailabilityEditingOpacityProperty());
        Bindings.bindBidirectional(otherControl.gridLinesProperty(), gridLinesProperty());
        Bindings.bindBidirectional(otherControl.gridLineColorProperty(), gridLineColorProperty());
        Bindings.bindBidirectional(otherControl.gridTypeProperty(), gridTypeProperty());
        Bindings.bindBidirectional(otherControl.enableStartAndEndTimesFlipProperty(), enableStartAndEndTimesFlipProperty());
    }

    public final void unbind(DayViewBase otherControl) {
        super.unbind(otherControl);

        Bindings.unbindBidirectional(otherControl.earlyLateHoursStrategyProperty(), earlyLateHoursStrategyProperty());
        Bindings.unbindBidirectional(otherControl.hoursLayoutStrategyProperty(), hoursLayoutStrategyProperty());
        Bindings.unbindBidirectional(otherControl.hourHeightProperty(), hourHeightProperty());
        Bindings.unbindBidirectional(otherControl.hourHeightCompressedProperty(), hourHeightCompressedProperty());
        Bindings.unbindBidirectional(otherControl.visibleHoursProperty(), visibleHoursProperty());
        Bindings.unbindBidirectional(otherControl.enableCurrentTimeMarkerProperty(), enableCurrentTimeMarkerProperty());
        Bindings.unbindBidirectional(otherControl.enableCurrentTimeCircleProperty(), enableCurrentTimeCircleProperty());
        Bindings.unbindBidirectional(otherControl.trimTimeBoundsProperty(), trimTimeBoundsProperty());
        Bindings.unbindBidirectional(otherControl.scrollingEnabledProperty(), scrollingEnabledProperty());
        Bindings.unbindBidirectional(otherControl.scrollTimeProperty(), scrollTimeProperty());
        Bindings.unbindBidirectional(otherControl.overlapResolutionStrategyProperty(), overlapResolutionStrategyProperty());
        Bindings.unbindBidirectional(otherControl.lassoStartProperty(),  lassoStartProperty());
        Bindings.unbindBidirectional(otherControl.lassoEndProperty(),  lassoEndProperty());
        Bindings.unbindBidirectional(otherControl.onLassoFinishedProperty(),  onLassoFinishedProperty());
        Bindings.unbindBidirectional(otherControl.startTimeProperty(), startTimeProperty());
        Bindings.unbindBidirectional(otherControl.endTimeProperty(), endTimeProperty());
        Bindings.unbindBidirectional(otherControl.entryViewAvailabilityEditingBehaviourProperty(), entryViewAvailabilityEditingBehaviourProperty());
        Bindings.unbindBidirectional(otherControl.entryViewAvailabilityEditingOpacityProperty(), entryViewAvailabilityEditingOpacityProperty());
        Bindings.unbindBidirectional(otherControl.gridLinesProperty(), gridLinesProperty());
        Bindings.unbindBidirectional(otherControl.gridLineColorProperty(), gridLineColorProperty());
        Bindings.unbindBidirectional(otherControl.gridTypeProperty(), gridTypeProperty());
        Bindings.unbindBidirectional(otherControl.enableStartAndEndTimesFlipProperty(), enableStartAndEndTimesFlipProperty());
    }

    private static final String DAY_VIEW_BASE_CATEGORY = "Date View Base";

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(scrollingEnabledProperty());
            }

            @Override
            public void setValue(Object value) {
                setScrollingEnabled((boolean) value);
            }

            @Override
            public Object getValue() {
                return isScrollingEnabled();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Enable Scrolling";
            }

            @Override
            public String getDescription() {
                return "Support scrolling to previous or next days";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(enableCurrentTimeMarkerProperty());
            }

            @Override
            public void setValue(Object value) {
                setEnableCurrentTimeMarker((boolean) value);
            }

            @Override
            public Object getValue() {
                return isEnableCurrentTimeMarker();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Current time marker";
            }

            @Override
            public String getDescription() {
                return "Show current time marker.";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(enableCurrentTimeCircleProperty());
            }

            @Override
            public void setValue(Object value) {
                setEnableCurrentTimeCircle((boolean) value);
            }

            @Override
            public Object getValue() {
                return isEnableCurrentCircleMarker();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Current time circle";
            }

            @Override
            public String getDescription() {
                return "Show current time circle.";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(earlyLateHoursStrategyProperty());
            }

            @Override
            public void setValue(Object value) {
                setEarlyLateHoursStrategy((EarlyLateHoursStrategy) value);
            }

            @Override
            public Object getValue() {
                return getEarlyLateHoursStrategy();
            }

            @Override
            public Class<?> getType() {
                return EarlyLateHoursStrategy.class;
            }

            @Override
            public String getName() {
                return "Early / Late Hours";
            }

            @Override
            public String getDescription() {
                return "Early / Late Hours Layout Strategy";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(visibleHoursProperty());
            }

            @Override
            public void setValue(Object value) {
                setVisibleHours((int) value);
            }

            @Override
            public Object getValue() {
                return getVisibleHours();
            }

            @Override
            public Class<?> getType() {
                return Integer.class;
            }

            @Override
            public String getName() {
                return "Visible Hours";
            }

            @Override
            public String getDescription() {
                return "Number of visible hours";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(hourHeightProperty());
            }

            @Override
            public void setValue(Object value) {
                setHourHeight((double) value);
            }

            @Override
            public Object getValue() {
                return getHourHeight();
            }

            @Override
            public Class<?> getType() {
                return Double.class;
            }

            @Override
            public String getName() {
                return "Hour Height";
            }

            @Override
            public String getDescription() {
                return "Height of one hour";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(hourHeightCompressedProperty());
            }

            @Override
            public void setValue(Object value) {
                setHourHeightCompressed((double) value);
            }

            @Override
            public Object getValue() {
                return getHourHeightCompressed();
            }

            @Override
            public Class<?> getType() {
                return Double.class;
            }

            @Override
            public String getName() {
                return "Hour Height Compressed";
            }

            @Override
            public String getDescription() {
                return "Height of one hour when shown compressed (early / late hours).";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(hoursLayoutStrategyProperty());
            }

            @Override
            public void setValue(Object value) {
                setHoursLayoutStrategy((HoursLayoutStrategy) value);
            }

            @Override
            public Object getValue() {
                return getHoursLayoutStrategy();
            }

            @Override
            public Class<?> getType() {
                return HoursLayoutStrategy.class;
            }

            @Override
            public String getName() {
                return "Layout Strategy";
            }

            @Override
            public String getDescription() {
                return "Layout Strategy";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(trimTimeBoundsProperty());
            }

            @Override
            public void setValue(Object value) {
                setTrimTimeBounds((boolean) value);
            }

            @Override
            public Object getValue() {
                return isTrimTimeBounds();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Trim Time Bounds";
            }

            @Override
            public String getDescription() {
                return "Adjust earliest / latest times shown";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(earliestTimeUsedProperty());
            }

            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return getEarliestTimeUsed();
            }

            @Override
            public Class<?> getType() {
                return LocalTime.class;
            }

            @Override
            public String getName() {
                return "Earliest Time Used";
            }

            @Override
            public String getDescription() {
                return "Earliest start time of any entry view";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(latestTimeUsedProperty());
            }

            @Override
            public void setValue(Object value) {
            }

            @Override
            public Object getValue() {
                return getLatestTimeUsed();
            }

            @Override
            public Class<?> getType() {
                return LocalTime.class;
            }

            @Override
            public String getName() {
                return "Latest Time Used";
            }

            @Override
            public String getDescription() {
                return "Latest end time of any entry view";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(overlapResolutionStrategyProperty());
            }

            @Override
            public void setValue(Object value) {
                setOverlapResolutionStrategy((OverlapResolutionStrategy) value);
            }

            @Override
            public Object getValue() {
                return getOverlapResolutionStrategy();
            }

            @Override
            public Class<?> getType() {
                return OverlapResolutionStrategy.class;
            }

            @Override
            public String getName() {
                return "Overlap Resolution";
            }

            @Override
            public String getDescription() {
                return "Controls when entries are considered overlapping";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(entryViewAvailabilityEditingBehaviourProperty());
            }

            @Override
            public void setValue(Object value) {
                setEntryViewAvailabilityEditingBehaviour((AvailabilityEditingEntryBehaviour) value);
            }

            @Override
            public Object getValue() {
                return getEntryViewAvailabilityEditingBehaviour();
            }

            @Override
            public Class<?> getType() {
                return AvailabilityEditingEntryBehaviour.class;
            }

            @Override
            public String getName() {
                return "Availability Editing Entry Behaviour";
            }

            @Override
            public String getDescription() {
                return "Determines how an entry view will be shown during availability editing.";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(gridLinesProperty());
            }

            @Override
            public void setValue(Object value) {
                setGridLines((VirtualGrid) value);
            }

            @Override
            public Object getValue() {
                return getGridLines();
            }

            @Override
            public Class<?> getType() {
                return VirtualGrid.class;
            }

            @Override
            public String getName() {
                return "Grid used for drawing grid lines";
            }

            @Override
            public String getDescription() {
                return "Specifies the grid size.";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(gridLineColorProperty());
            }

            @Override
            public void setValue(Object value) {
                setGridLineColor((Paint) value);
            }

            @Override
            public Object getValue() {
                return getGridLineColor();
            }

            @Override
            public Class<?> getType() {
                return Paint.class;
            }

            @Override
            public String getName() {
                return "Color used for drawing grid lines";
            }

            @Override
            public String getDescription() {
                return "Specifies the grid line color.";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(gridTypeProperty());
            }

            @Override
            public void setValue(Object value) {
                setGridType((GridType) value);
            }

            @Override
            public Object getValue() {
                return getGridType();
            }

            @Override
            public Class<?> getType() {
                return GridType.class;
            }

            @Override
            public String getName() {
                return "Grid Type";
            }

            @Override
            public String getDescription() {
                return "Specifies the grid type.";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(entryViewAvailabilityEditingOpacityProperty());
            }

            @Override
            public void setValue(Object value) {
                setEntryViewAvailabilityEditingOpacity((double) value);
            }

            @Override
            public Object getValue() {
                return getEntryViewAvailabilityEditingOpacity();
            }

            @Override
            public Class<?> getType() {
                return Double.class;
            }

            @Override
            public String getName() {
                return "Availability Editing Entry Opacity";
            }

            @Override
            public String getDescription() {
                return "Determines how opaque an entry view will be shown during availability editing.";
            }

            @Override
            public String getCategory() {
                return DAY_VIEW_BASE_CATEGORY;
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        });

        return items;
    }
}
