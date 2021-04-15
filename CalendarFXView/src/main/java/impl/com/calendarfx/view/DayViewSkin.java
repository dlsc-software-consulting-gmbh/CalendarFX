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

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase.OverlapResolutionStrategy;
import com.calendarfx.view.DraggedEntry;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.EntryViewBase.AlignmentStrategy;
import com.calendarfx.view.EntryViewBase.HeightLayoutStrategy;
import com.calendarfx.view.DateControl.Layer;
import com.calendarfx.view.EntryViewBase.Position;
import impl.com.calendarfx.view.util.Placement;
import impl.com.calendarfx.view.util.TimeBoundsResolver;
import impl.com.calendarfx.view.util.VisualBoundsResolver;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Duration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

@SuppressWarnings("javadoc")
public class DayViewSkin<T extends DayView> extends DayViewBaseSkin<T> implements LoadDataSettingsProvider {

    private static final String MIDNIGHT_LINE_STYLE_CLASS = "midnight-line";
    private static final String NOON_LINE_STYLE_CLASS = "noon-line";
    private static final String HALF_HOUR_LINE_STYLE_CLASS = "half-hour-line";
    private static final String FULL_HOUR_LINE_STYLE_CLASS = "full-hour-line";

    private final List<Line> lines = new ArrayList<>();

    private final DataLoader dataLoader = new DataLoader(this);

    private final Circle currentTimeCircle;

    private final Line currentTimeLine;

    private DayEntryView draggedEntryView;

    private final Region earlyHoursRegion;

    private final Region lateHoursRegion;

    private LocalDate displayedDate;

    private double startY;

    public DayViewSkin(T view) {
        super(view);

        earlyHoursRegion = new Region();
        earlyHoursRegion.setMouseTransparent(true);
        earlyHoursRegion.getStyleClass().add("early-hours-region");
        earlyHoursRegion.setManaged(false);
        getChildren().add(earlyHoursRegion);

        lateHoursRegion = new Region();
        lateHoursRegion.setMouseTransparent(true);
        lateHoursRegion.getStyleClass().add("late-hours-region");
        lateHoursRegion.setManaged(false);
        getChildren().add(lateHoursRegion);

        if (!view.isScrollingEnabled()) {
            // Static lines use different styling for early / late hours, we do not want that
            // when scrolling is enabled. In that case all lines need to look the same.
            createStaticLines();
        }

        currentTimeCircle = new Circle(4);
        currentTimeCircle.getStyleClass().add("current-time-circle");
        currentTimeCircle.setManaged(false);
        currentTimeCircle.setMouseTransparent(true);
        currentTimeCircle.setOpacity(0);
        currentTimeCircle.visibleProperty().bind(view.enableCurrentTimeCircleProperty());
        getChildren().add(currentTimeCircle);

        currentTimeLine = new Line();
        currentTimeLine.getStyleClass().add("current-time-line");
        currentTimeLine.setManaged(false);
        currentTimeLine.setMouseTransparent(true);
        currentTimeLine.setOpacity(0);
        currentTimeLine.visibleProperty().bind(view.enableCurrentTimeMarkerProperty());
        getChildren().add(currentTimeLine);

        if (!(this instanceof WeekDayViewSkin)) {
            /*
             * Dragging inside week day views will be handled by a drag controller
             * installed on the week view, not on the individual days.
             */
            new DayViewEditController(view);
        }

        setupCurrentTimeMarkerSupport();

        view.draggedEntryProperty().addListener(it -> addOrRemoveDraggedEntryView());

        view.showCurrentTimeMarkerProperty().addListener(it -> updateTimelineVisibility());
        view.showCurrentTimeTodayMarkerProperty().addListener(it -> updateTimelineVisibility());

        view.layoutProperty().addListener(it -> view.requestLayout());
        view.visibleLayersProperty().addListener((InvalidationListener) it -> view.requestLayout());

        // infinite scrolling
        view.scrollTimeProperty().addListener(it -> {
            loadData("view scrolled");
            view.requestLayout();
        });

        view.scrollingEnabledProperty().addListener(it -> {
            if (view.isScrollingEnabled()) {
                lines.clear();
            } else {
                createStaticLines();
            }
            view.requestLayout();
        });

        updateShowMarkers();
        updateTimelineVisibility();

        view.dateProperty().addListener(it -> {
            if (displayedDate == null || !displayedDate.equals(view.getDate())) {
                loadData("date changed");
            }
        });

        view.suspendUpdatesProperty().addListener(evt -> loadData("suspend updates was set to false"));
        view.getCalendars().addListener((javafx.beans.Observable obs) -> loadData("list of calendars changed"));

        final InvalidationListener styleLinesListener = it -> updateLineStyling();
        view.startTimeProperty().addListener(styleLinesListener);
        view.endTimeProperty().addListener(styleLinesListener);
        view.earlyLateHoursStrategyProperty().addListener(styleLinesListener);

        loadData("initial data loading");

        view.heightProperty().addListener(it -> {
            if (view.isScrollingEnabled()) {
                // run later, or we cause flickering
                Platform.runLater(() -> {
                    loadData("height changed");
                });
            }
        });
        view.hourHeightProperty().addListener(it -> loadData("hour height changed"));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        getSkinnable().setOnMousePressed(evt -> startY = evt.getScreenY());

        getSkinnable().setOnMouseDragged(evt -> {
            if (view.isScrollingEnabled()) {
                view.setScrollTime(view.getZonedDateTimeAt(0, startY - evt.getScreenY()));
                startY = evt.getScreenY();
            }
        });
    }

    private void createStaticLines() {
        for (int i = 1; i < 24; i++) {
            createLine(HALF_HOUR_LINE_STYLE_CLASS);
            createLine(FULL_HOUR_LINE_STYLE_CLASS);
        }

        createLine(HALF_HOUR_LINE_STYLE_CLASS);

        updateLineStyling();
    }

    @Override
    protected void calendarVisibilityChanged() {
        getSkinnable().requestLayout();
    }

    @Override
    protected void refreshData() {
        loadData("refreshData() was called");
    }

    private void updateTimelineVisibility() {
        double lineOpacity = getSkinnable().isShowCurrentTimeMarker() ? 1 : 0;
        FadeTransition lineTransition = new FadeTransition(Duration.millis(600), currentTimeLine);
        lineTransition.setToValue(lineOpacity);
        lineTransition.play();

        double circleOpacity = getSkinnable().isShowCurrentTimeTodayMarker() ? 1 : 0;
        FadeTransition circleTransition = new FadeTransition(Duration.millis(600), currentTimeCircle);
        circleTransition.setToValue(circleOpacity);
        circleTransition.play();
    }

    private void setupCurrentTimeMarkerSupport() {
        T view = getSkinnable();
        InvalidationListener listener = evt -> updateShowMarkers();
        view.dateProperty().addListener(listener);
        view.todayProperty().addListener(listener);
    }

    private void updateShowMarkers() {
        T view = getSkinnable();
        view.getProperties().put("show.current.time.marker", isShowingTimeMarker());
        view.getProperties().put("show.current.time.today.marker", isShowingTimeTodayMarker());
    }

    protected boolean isShowingTimeMarker() {
        return getSkinnable().getDate().equals(getSkinnable().getToday());
    }

    protected boolean isShowingTimeTodayMarker() {
        return isShowingTimeMarker();
    }

    private final InvalidationListener layoutListener = it -> getSkinnable().requestLayout();

    private final WeakInvalidationListener weakLayoutListener = new WeakInvalidationListener(layoutListener);

    private void addOrRemoveDraggedEntryView() {
        DayView view = getSkinnable();
        DraggedEntry draggedEntry = view.getDraggedEntry();
        if (draggedEntry != null) {
            draggedEntryView = doAddEntryView(draggedEntry);
            draggedEntryView.toFront();
            draggedEntryView.setMouseTransparent(true);
            draggedEntryView.getProperties().put("selected", true);
            draggedEntry.intervalProperty().addListener(weakLayoutListener);
        } else {
            if (draggedEntryView != null) {
                removeEntryView(draggedEntryView.getEntry());
                draggedEntryView = null;
            }
        }

        view.requestLayout();
    }

    private void loadData(String reason) {
        if (getSkinnable().isSuspendUpdates()) {
            return;
        }
        updateEntries(reason);
    }

    private void createLine() {
        createLine(null);
    }

    private void createLine(String styleClass) {
        Line line = new Line();
        line.setManaged(false);
        line.setMouseTransparent(true);
        if (styleClass != null) {
            line.getStyleClass().add(styleClass);
        }
        lines.add(line);
        getChildren().add(line);
    }

    private void updateLineStyling() {
        T dayView = getSkinnable();

        LocalTime startTime = dayView.getStartTime();
        LocalTime endTime = dayView.getEndTime();

        boolean showEarlyHoursRegion = startTime.isAfter(LocalTime.MIN);
        boolean showLateHoursRegion = endTime.isBefore(LocalTime.MAX);

        earlyHoursRegion.setVisible(showEarlyHoursRegion);
        lateHoursRegion.setVisible(showLateHoursRegion);

        int lineCount = lines.size();

        for (int i = 0; i < lineCount; i++) {
            Line line = lines.get(i);

            line.getStyleClass().removeAll("early-hour-line", "late-hour-line");

            int hour = (i + 1) / 2;
            int minute = 0;

            boolean halfHourLine = (i % 2 == 0);
            if (halfHourLine) {
                minute = 30;
            }

            LocalTime time = LocalTime.of(hour, minute);

            if (time.isBefore(startTime)) {
                if (!line.getStyleClass().contains("early-hour-line")) {
                    line.getStyleClass().add("early-hour-line");
                }
            }
            if (time.isAfter(endTime)) {
                if (!line.getStyleClass().contains("late-hour-line")) {
                    line.getStyleClass().add("late-hour-line");
                }
            }

            switch (dayView.getEarlyLateHoursStrategy()) {
                case HIDE:
                    /*
                     * We do not show ... a) lines before the start time and after
                     * the end time b) lines directly on the start time or end time
                     * because they make the UI look messy
                     */
                    line.setVisible(!time.isBefore(startTime) && !time.equals(startTime) && !time.isAfter(endTime) && !time.equals(endTime));
                    break;
                case SHOW:
                    line.setVisible(true);
                    break;
                case SHOW_COMPRESSED:
                    line.setVisible(!halfHourLine);
                    break;
                default:
                    break;

            }
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        if (getSkinnable().isScrollingEnabled()) {
            layoutChildrenInfiniteScrolling(contentX, contentY, contentWidth, contentHeight);
        } else {
            layoutChildrenStatic(contentX, contentY, contentWidth, contentHeight);
        }
    }

    protected void layoutChildrenInfiniteScrolling(double contentX, double contentY, double contentWidth, double contentHeight) {
        final T view = getSkinnable();
        final ZonedDateTime scrollTime = view.getScrollTime();
        Instant time = scrollTime.toInstant().truncatedTo(ChronoUnit.HOURS);

        double y = view.getLocation(time);

        int lineIndex = 0;

        do {

            LocalTime localTime = LocalTime.ofInstant(time, view.getZoneId());

            if (lineIndex >= lines.size()) {
                createLine();
                Line line = lines.get(lineIndex);
                line.toBack();
            }

            Line line = lines.get(lineIndex);
            line.toBack();
            line.setVisible(true);

            line.getStyleClass().removeAll(HALF_HOUR_LINE_STYLE_CLASS, FULL_HOUR_LINE_STYLE_CLASS, MIDNIGHT_LINE_STYLE_CLASS, NOON_LINE_STYLE_CLASS);

            if (localTime.getMinute() == 30) {
                line.getStyleClass().add(HALF_HOUR_LINE_STYLE_CLASS);
            } else {
                line.getStyleClass().add(FULL_HOUR_LINE_STYLE_CLASS);
            }

            if (localTime.equals(LocalTime.MIDNIGHT)) {
                line.getStyleClass().add(MIDNIGHT_LINE_STYLE_CLASS);
                line.setStartX(snapPositionX(contentX));
                line.setEndX(snapPositionX(contentX + contentWidth));
            } else if (localTime.equals(LocalTime.NOON)) {
                line.getStyleClass().add(NOON_LINE_STYLE_CLASS);
                line.setStartX(snapPositionX(contentX));
                line.setEndX(snapPositionX(contentX + contentWidth));
            } else {
                line.setStartX(snapPositionX(contentX + 4));
                line.setEndX(snapPositionX(contentX + contentWidth - 4));
            }

            line.setStartY(snapPositionY(y));
            line.setEndY(snapPositionY(y));

            lineIndex++;

            time = time.plus(30, ChronoUnit.MINUTES);
            y = view.getLocation(time);

        } while (y < contentY + contentHeight);

        for (int i = lineIndex; i < lines.size(); i++) {
            Line line = lines.get(i);
            line.setVisible(false);
        }

        layoutEntries(contentX, contentY, contentWidth, contentHeight);
        layoutCurrentTime(contentX, contentY, contentWidth);
    }

    protected void layoutChildrenStatic(double contentX, double contentY, double contentWidth, double contentHeight) {
        int lineCount = lines.size();

        T dayView = getSkinnable();

        LocalTime startTime = dayView.getStartTime();
        LocalTime endTime = dayView.getEndTime();

        boolean showEarlyHoursRegion = startTime.isAfter(LocalTime.MIN);
        boolean showLateHoursRegion = endTime.isBefore(LocalTime.MAX);

        earlyHoursRegion.setVisible(showEarlyHoursRegion);
        lateHoursRegion.setVisible(showLateHoursRegion);

        double earlyHoursY = dayView.getLocation(startTime);
        double lateHoursY = dayView.getLocation(endTime);

        earlyHoursRegion.resizeRelocate(snapPositionX(contentX), snapPositionY(contentY), snapSizeX(contentWidth), snapSizeY(earlyHoursY));
        lateHoursRegion.resizeRelocate(snapPositionX(contentX), snapPositionY(lateHoursY), snapSizeX(contentWidth), snapSizeY(contentHeight - lateHoursY));

        for (int i = 0; i < lineCount; i++) {

            Line line = lines.get(i);

            int hour = (i + 1) / 2;
            int minute = 0;

            boolean halfHourLine = (i % 2 == 0);
            if (halfHourLine) {
                minute = 30;
            }

            LocalTime time = LocalTime.of(hour, minute);

            double yy = snapPositionY(contentY + dayView.getLocation(time));

            line.setStartX(snapPositionX(contentX + 4));
            line.setStartY(yy);
            line.setEndX(snapPositionX(contentX + contentWidth - 4));
            line.setEndY(yy);
        }

        // the dragged entry view
        if (draggedEntryView != null) {
            boolean showing = isRelevant(draggedEntryView.getEntry());
            draggedEntryView.setVisible(showing);
        }

        layoutEntries(contentX, contentY, contentWidth, contentHeight);
        layoutCurrentTime(contentX, contentY, contentWidth);
    }

    protected void layoutEntries(double contentX, double contentY, double contentWidth, double contentHeight) {
        T dayView = getSkinnable();

        switch (dayView.getLayout()) {
            case STANDARD:
                layoutStandard(dayView, contentX, contentY, contentWidth, contentHeight);
                break;
            case SWIMLANE:
                layoutSwimlane(dayView, contentX, contentY, contentWidth, contentHeight);
                break;
            default:
                throw new IllegalArgumentException("unknown layout: " + dayView.getLayout());
        }
    }

    protected void layoutCurrentTime(double contentX, double contentY, double contentWidth) {
        T dayView = getSkinnable();

        double y;

        LocalTime time = dayView.getTime();

        if (dayView.isScrollingEnabled()) {
            y = snapPositionY(dayView.getLocation(ZonedDateTime.of(getSkinnable().getDate(), time, getSkinnable().getZoneId()).toInstant()));
        } else {
            y = snapPositionY(contentY + dayView.getLocation(time));
        }

        currentTimeLine.setStartX(snapPositionX(contentX));
        currentTimeLine.setStartY(snapPositionY(y));
        currentTimeLine.setEndX(snapPositionX(contentX + contentWidth));
        currentTimeLine.setEndY(snapPositionY(y));
        currentTimeLine.toFront();

        currentTimeCircle.setCenterX(snapPositionX(contentX + currentTimeCircle.getRadius() + 4));
        currentTimeCircle.setCenterY(y);
        currentTimeCircle.toFront();
    }

    private void layoutStandard(DayView dayView, double contentX, double contentY, double contentWidth, double contentHeight) {

        Predicate<DayEntryView> isRelatedToVisibleLayer = view -> dayView.visibleLayersProperty().contains(view.getLayer());

        Map<Layer, List<DayEntryView>> layerGroupedEntryViews = groupEntryViewsBy(EntryViewBase::getLayer, isRelatedToVisibleLayer);

        layoutOnLayers(layerGroupedEntryViews, dayView, contentX, contentY, contentWidth, contentHeight);
    }

    private void layoutSwimlane(DayView dayView, double contentX, double contentY, double contentWidth, double contentHeight) {
        List<Calendar> visibleCalendars = dayView.getCalendars().filtered(c -> getSkinnable().isCalendarVisible(c));

        double x = contentX;
        double w = contentWidth / (visibleCalendars.size());

        Predicate<DayEntryView> isRelatedToVisibleLayer = view -> dayView.visibleLayersProperty().contains(view.getLayer());
        Predicate<DayEntryView> isRelatedToVisibleCalendar = view -> visibleCalendars.contains(getEntryViewCalendar(view));

        Predicate<DayEntryView> entryViewFilter = isRelatedToVisibleLayer.and(isRelatedToVisibleCalendar);
        Map<Calendar, List<DayEntryView>> calendarGroupedEntryViews = groupEntryViewsBy(this::getEntryViewCalendar, entryViewFilter);

        for (Calendar calendar : visibleCalendars) {
            Map<Layer, List<DayEntryView>> layerGroupedEntryViews = calendarGroupedEntryViews.getOrDefault(calendar, Collections.emptyList()).stream()
                    .collect(Collectors.groupingBy(EntryViewBase::getLayer));

            layoutOnLayers(layerGroupedEntryViews, dayView, x, contentY, w, contentHeight);
            x += w;
        }
    }

    private <G> Map<G, List<DayEntryView>> groupEntryViewsBy(Function<DayEntryView, G> groupByFunction, Predicate<DayEntryView> viewEntryFilter) {
        return getChildren().stream()
                .filter(DayEntryView.class::isInstance)
                .map(DayEntryView.class::cast)
                .filter(viewEntryFilter)
                .collect(Collectors.groupingBy(groupByFunction));
    }

    private void layoutOnLayers(Map<Layer, List<DayEntryView>> layerGroupedViewEntries, DayView dayView, double contentX, double contentY, double contentWidth, double contentHeight) {
        List<DayEntryView> baseEntryViews = layerGroupedViewEntries.getOrDefault(DateControl.Layer.BASE, Collections.emptyList());
        layoutBaseEntryViews(baseEntryViews, dayView, contentX, contentY, contentWidth, contentHeight);

        List<DayEntryView> topEntryViews = layerGroupedViewEntries.getOrDefault(DateControl.Layer.TOP, Collections.emptyList());
        layoutTopEntryViews(topEntryViews, dayView, contentX, contentY, contentWidth, contentHeight);
    }

    private Calendar getEntryViewCalendar(DayEntryView view) {
        Calendar cal;
        Entry<?> entry = view.getEntry();
        if (entry instanceof DraggedEntry) {
            DraggedEntry draggedEntry = (DraggedEntry) view.getEntry();
            cal = draggedEntry.getOriginalCalendar();
        } else {
            cal = entry.getCalendar();
        }
        return cal;
    }

    protected void layoutBaseEntryViews(List<DayEntryView> entryViews, DayView dayView, double contentX, double contentY, double contentWidth, double contentHeight) {
        List<Placement> placements;

        if (dayView.getOverlapResolutionStrategy().equals(OverlapResolutionStrategy.VISUAL_BOUNDS)) {
            placements = VisualBoundsResolver.resolve(entryViews, dayView, contentWidth);
        } else {
            placements = TimeBoundsResolver.resolve(entryViews);
        }

        if (placements != null) {
            contentWidth = contentWidth * dayView.getEntryWidthPercentage() / 100d;

            for (Placement placement : placements) {
                EntryViewBase<?> entryView = placement.getEntryView();

                Entry<?> entry = entryView.getEntry();

                double y1;
                double y2;

                if (dayView.isScrollingEnabled()) {

                    y1 = dayView.getLocation(entry.getStartAsZonedDateTime());
                    y2 = dayView.getLocation(entry.getEndAsZonedDateTime());

                } else {

                    y1 = dayView.getLocation(entry.getStartTime());
                    y2 = dayView.getLocation(entry.getEndTime());
                }

                if (entryView.getHeightLayoutStrategy().equals(HeightLayoutStrategy.COMPUTE_PREF_SIZE)) {

                    y2 = y1 + entryView.prefHeight(contentWidth);

                }

                LocalDate viewDate = dayView.getDate();
                LocalTime viewStartTime = entry.getStartTime();
                LocalTime viewEndTime = entry.getEndTime();

                if (!dayView.isScrollingEnabled()) {
                    boolean startsBefore = false;
                    boolean endsAfter = false;

                    LocalDate startDate = entry.getStartDate();

                    if (startDate.isBefore(dayView.getDate())) {
                        y1 = contentY;
                        viewStartTime = dayView.getStartTime();
                        startsBefore = true;
                    }

                    LocalDate endDate = entry.getEndDate();
                    if (endDate.isAfter(dayView.getDate())) {
                        y2 = contentHeight;
                        viewEndTime = dayView.getEndTime();
                        endsAfter = true;
                    }

                    Position position = Position.ONLY;

                    if (startsBefore && endsAfter) {
                        position = Position.MIDDLE;
                    } else if (startsBefore) {
                        position = Position.LAST;
                    } else if (endsAfter) {
                        position = Position.FIRST;
                    }

                    entryView.getProperties().put("position", position);
                }

                entryView.getProperties().put("startDate", viewDate);
                entryView.getProperties().put("endDate", viewDate);
                entryView.getProperties().put("startTime", viewStartTime);
                entryView.getProperties().put("endTime", viewEndTime);

                double minHeight = entryView.minHeight(contentWidth);

                double columnWidth = contentWidth / placement.getColumnCount();
                double x = contentX;

                if (!dayView.getOverlapResolutionStrategy().equals(OverlapResolutionStrategy.OFF)) {
                    x += placement.getColumnIndex() * columnWidth;
                }

                double entryWidth = computeEntryWidth(entryView, columnWidth);
                double entryLeftOffset = computeEntryLeftOffset(entryView, entryWidth, columnWidth);

                /*
                 * -2 on height to always have a gap between entries
                 */
                entryView.resizeRelocate(snapPositionX(x + entryLeftOffset), snapPositionY(y1), snapSizeX(entryWidth), snapSizeY(Math.max(minHeight, y2 - y1 - 2)));
            }
        }
    }

    protected void layoutTopEntryViews(List<DayEntryView> entryViews, DayView dayView, double contentX, double contentY, double contentWidth, double contentHeight) {

        entryViews.sort(Comparator.comparing(EntryViewBase::getStartDate));

        for (DayEntryView entryView : entryViews) {
            Entry<?> entry = entryView.getEntry();

            double y1;
            double y2;

            if (dayView.isScrollingEnabled()) {

                y1 = dayView.getLocation(entry.getStartAsZonedDateTime());
                y2 = dayView.getLocation(entry.getEndAsZonedDateTime());

            } else {

                y1 = dayView.getLocation(entry.getStartTime());
                y2 = dayView.getLocation(entry.getEndTime());

            }

            double entryWidth = computeEntryWidth(entryView, contentWidth);
            double entryLeftOffset = computeEntryLeftOffset(entryView, entryWidth, contentWidth);

            double minHeight = entryView.minHeight(entryWidth);

            entryView.resizeRelocate(snapPositionX(contentX + entryLeftOffset), snapPositionY(y1),
                    snapSizeX(entryWidth), snapSizeY(Math.max(minHeight, y2 - y1 - 2)));
            entryView.toFront();
        }
    }

    /**
     * Compute entry width during layout phase.
     *
     * @param entryView view entry
     * @param availableWidth maximum available horizontal space for entry view
     */
    private double computeEntryWidth(EntryViewBase<?> entryView, double availableWidth) {
        if (entryView.getAlignmentStrategy().equals(AlignmentStrategy.FILL)) {
            return availableWidth;
        }
        double preferredWidth = entryView.prefWidth(-1);
        if (preferredWidth != 0.0) {
            return preferredWidth;
        }
        return availableWidth * (entryView.getWidthPercentage() * 0.01);
    }

    private double computeEntryLeftOffset(EntryViewBase<?> entryView, double entryWidth, double availableWidth) {
        switch (entryView.getAlignmentStrategy()) {
            default:
            case FILL:
            case ALIGN_LEFT:
                return 0.0;
            case ALIGN_CENTER:
                return (availableWidth - entryWidth) * 0.5;
            case ALIGN_RIGHT:
                return (availableWidth - entryWidth);
        }
    }

    @Override
    protected void calendarChanged(Calendar calendar) {
        if (!getSkinnable().isSuspendUpdates()) {
            loadData("changes in calendar " + calendar.getName());
        }
    }

    @Override
    protected void entryCalendarChanged(CalendarEvent evt) {
        Entry<?> entry = evt.getEntry();

        /*
         * We do not care about full day entries in this view.
         */
        if (entry.isFullDay()) {
            return;
        }

        if (evt.isEntryRemoved()) {
            removeEntryView(entry);
            getSkinnable().requestLayout();
        }

        if (evt.isEntryAdded() && isRelevant(entry)) {
            addEntryView(entry);
        }

        if (isRelevant(entry)) {
            getSkinnable().requestLayout();
        }
    }

    @Override
    protected void entryFullDayChanged(CalendarEvent evt) {
        Entry<?> entry = evt.getEntry();
        if (isRelevant(entry)) {
            if (entry.isFullDay()) {
                removeEntryView(entry);
            } else {
                addEntryView(entry);
            }

            getSkinnable().requestLayout();
        }
    }

    @Override
    protected void entryRecurrenceRuleChanged(CalendarEvent evt) {
        Entry<?> entry = evt.getEntry();

        /*
         * We do not care about full day entries in this view.
         */
        if (entry.isFullDay()) {
            return;
        }

        removeEntryView(entry);
        addEntryView(entry);
    }

    @Override
    protected void entryIntervalChanged(CalendarEvent evt) {
        Entry<?> entry = evt.getEntry();

        /*
         * We do not care about full day entries in this view.
         */
        if (entry.isFullDay()) {
            return;
        }

        removeEntryView(entry);

        if (isRelevant(entry)) {
            addEntryView(entry);
        }
    }

    private boolean removeEntryView(Entry<?> entry) {
        boolean removed = getChildren().removeIf(node -> {
            if (node instanceof DayEntryView) {
                DayEntryView view = (DayEntryView) node;

                Entry<?> removedEntry = entry;
                if (removedEntry.getRecurrenceSourceEntry() != null) {
                    removedEntry = removedEntry.getRecurrenceSourceEntry();
                }

                Entry<?> viewEntry = view.getEntry();
                if (viewEntry.getRecurrenceSourceEntry() != null) {
                    viewEntry = viewEntry.getRecurrenceSourceEntry();
                }

                return viewEntry.getId().equals(removedEntry.getId());
            }

            return false;
        });

        if (removed && !(entry instanceof DraggedEntry) && LoggingDomain.VIEW.isLoggable(Level.FINE)) {
            LoggingDomain.VIEW.fine("successfully removed the entry view of entry " + entry);
        }

        return removed;
    }

    private void addEntryView(Entry<?> entry) {
        if (entry.isRecurring()) {
            Calendar calendar = entry.getCalendar();
            LocalDate date = getSkinnable().getDate();
            final Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(date, date, getZoneId());
            List<Entry<?>> entriesOnDate = entries.get(date);
            if (entriesOnDate != null && !entriesOnDate.isEmpty()) {
                doAddEntryView(entriesOnDate.get(0));
            }
        } else {
            doAddEntryView(entry);
        }
    }

    private DayEntryView doAddEntryView(Entry<?> entry) {
        Callback<Entry<?>, DayEntryView> factory = getSkinnable().getEntryViewFactory();
        DayEntryView view = factory.call(entry);
        view.getProperties().put("control", getSkinnable());
        view.setManaged(false);

        int index = findIndex(entry);

        getChildren().add(index, view);

        if (!(entry instanceof DraggedEntry) && LoggingDomain.VIEW.isLoggable(Level.FINE)) {
            LoggingDomain.VIEW.fine("added entry view " + entry.getTitle() + ", day = " + getSkinnable().getDate());
        }

        return view;
    }

    /*
     * Utility method to find the right place for inserting a new day entry
     * view. The right order is important for TAB traversal to work properly.
     */
    private int findIndex(Entry<?> entry) {
        int childrenSize = getChildren().size();

        for (int i = 0; i < childrenSize; i++) {
            Node node = getChildren().get(i);
            if (node instanceof DayEntryView) {
                DayEntryView view = (DayEntryView) node;
                Entry<?> viewEntry = view.getEntry();
                if (viewEntry.getStartAsZonedDateTime().isAfter(entry.getStartAsZonedDateTime())) {
                    return i;
                }
            }
        }

        return childrenSize;
    }

    private void updateEntries(String reason) {
        displayedDate = getSkinnable().getDate();

        getChildren().removeIf(node -> node instanceof DayEntryView);

        Map<LocalDate, List<Entry<?>>> dataMap = new HashMap<>();
        dataLoader.loadEntries(dataMap);

        LocalDate date = getLoadStartDate();

        LocalTime earliest = null;
        LocalTime latest = null;

        List<Entry> processedEntries = new ArrayList<>();

        do {
            List<Entry<?>> entryList = dataMap.get(date);


            if (entryList != null) {
                entryList.removeIf(Entry::isFullDay);

                for (Entry<?> entry : entryList) {

                    if (processedEntries.contains(entry)) {
                        continue;
                    }

                    processedEntries.add(entry);

                    doAddEntryView(entry);

                    if (earliest == null || entry.getStartTime().isBefore(earliest)) {
                        earliest = entry.getStartTime();
                    }

                    if (entry.getStartDate().isBefore(getSkinnable().getDate())) {
                        earliest = LocalTime.MIN;
                    }

                    if (latest == null || entry.getEndTime().isAfter(latest)) {
                        latest = entry.getEndTime();
                    }

                    if (entry.getEndDate().isAfter(getSkinnable().getDate())) {
                        latest = LocalTime.MAX;
                    }
                }
            }

            date = date.plusDays(1);
        } while (!date.isAfter(getLoadEndDate()));

        getSkinnable().getProperties().put("earliest.time.used", earliest);
        getSkinnable().getProperties().put("latest.time.used", latest);

        getSkinnable().requestLayout();

        LoggingDomain.VIEW.fine("updating entries in day view " + getSkinnable().getDate() + ": reason = " + reason + ", entry count: " + getChildren().stream().filter(child -> child instanceof DayEntryView).count());
    }

    @Override
    public String getLoaderName() {
        return "Day View";
    }

    @Override
    public LocalDate getLoadStartDate() {
        if (getSkinnable().isScrollingEnabled()) {
            return getSkinnable().getScrollTime().toLocalDate().minusDays(1);
        }

        return getSkinnable().getDate();
    }

    @Override
    public LocalDate getLoadEndDate() {
        if (getSkinnable().isScrollingEnabled()) {
            return getSkinnable().getZonedDateTimeAt(0, getSkinnable().getHeight()).toLocalDate();
        }

        return getSkinnable().getDate();
    }

    @Override
    public ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    @Override
    public List<CalendarSource> getCalendarSources() {
        return getSkinnable().getCalendarSources();
    }

    @Override
    public Control getControl() {
        return getSkinnable();
    }

    @Override
    public boolean isCalendarVisible(Calendar calendar) {
        return getSkinnable().isCalendarVisible(calendar);
    }
}
