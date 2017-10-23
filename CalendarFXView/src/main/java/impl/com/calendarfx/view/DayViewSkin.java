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
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DraggedEntry;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.EntryViewBase.Position;
import impl.com.calendarfx.view.util.Placement;
import impl.com.calendarfx.view.util.Resolver;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Callback;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

@SuppressWarnings("javadoc")
public class DayViewSkin<T extends DayView> extends DayViewBaseSkin<T> implements LoadDataSettingsProvider {

    private List<Line> lines = new ArrayList<>();

    private DataLoader dataLoader = new DataLoader(this);

    private Circle currentTimeCircle;

    private Line currentTimeLine;

    private DayEntryView draggedEntryView;

    private Region earlyHoursRegion;

    private Region lateHoursRegion;

    private LocalDate displayedDate;

    public DayViewSkin(T view) {
        super(view);

        earlyHoursRegion = new Region();
        earlyHoursRegion.setMouseTransparent(true);
        earlyHoursRegion.getStyleClass().add("early-hours-region"); //$NON-NLS-1$
        earlyHoursRegion.setManaged(false);
        getChildren().add(earlyHoursRegion);

        lateHoursRegion = new Region();
        lateHoursRegion.setMouseTransparent(true);
        lateHoursRegion.getStyleClass().add("late-hours-region"); //$NON-NLS-1$
        lateHoursRegion.setManaged(false);
        getChildren().add(lateHoursRegion);

        for (int i = 1; i < 24; i++) {
            createLine("half-hour-line"); //$NON-NLS-1$
            createLine("full-hour-line"); //$NON-NLS-1$
        }

        createLine("half-hour-line"); //$NON-NLS-1$

        currentTimeCircle = new Circle(4);
        currentTimeCircle.getStyleClass().add("current-time-circle"); //$NON-NLS-1$
        currentTimeCircle.setManaged(false);
        currentTimeCircle.setMouseTransparent(true);
        currentTimeCircle.setOpacity(0);
        currentTimeCircle.visibleProperty().bind(view.enableCurrentTimeMarkerProperty());
        getChildren().add(currentTimeCircle);

        currentTimeLine = new Line();
        currentTimeLine.getStyleClass().add("current-time-line"); //$NON-NLS-1$
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

        updateShowMarkers();
        updateTimelineVisibility();

        view.dateProperty().addListener(it -> {
            if (displayedDate == null || !displayedDate.equals(view.getDate())) {
                loadData("date changed");
            }
        });

        view.suspendUpdatesProperty().addListener(evt -> loadData("suspend updates was set to false"));
        view.getCalendars().addListener((javafx.beans.Observable obs) -> loadData("list of calendars changed"));

        updateLineStyling();

        final InvalidationListener styleLinesListener = it -> updateLineStyling();
        view.startTimeProperty().addListener(styleLinesListener);
        view.endTimeProperty().addListener(styleLinesListener);
        view.earlyLateHoursStrategyProperty().addListener(styleLinesListener);

        loadData("initial data loading");
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
        view.getProperties().put("show.current.time.marker", //$NON-NLS-1$
                isShowingTimeMarker());
        view.getProperties().put("show.current.time.today.marker", //$NON-NLS-1$
                isShowingTimeTodayMarker());
    }

    protected boolean isShowingTimeMarker() {
        return getSkinnable().getDate().equals(getSkinnable().getToday());
    }

    protected boolean isShowingTimeTodayMarker() {
        return isShowingTimeMarker();
    }

    private InvalidationListener layoutListener = it -> getSkinnable().requestLayout();

    private WeakInvalidationListener weakLayoutListener = new WeakInvalidationListener(layoutListener);

    private void addOrRemoveDraggedEntryView() {
        DayView view = getSkinnable();
        DraggedEntry draggedEntry = view.getDraggedEntry();
        if (draggedEntry != null) {
            draggedEntryView = doAddEntryView(draggedEntry);
            draggedEntryView.toFront();
            draggedEntryView.setMouseTransparent(true);
            draggedEntryView.getProperties().put("selected", true); //$NON-NLS-1$
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

    private void createLine(String styleClass) {
        Line line = new Line();
        line.setManaged(false);
        line.getStyleClass().add(styleClass);
        line.setMouseTransparent(true);
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

            line.getStyleClass().removeAll("early-hour-line", "late-hour-line"); //$NON-NLS-1$ //$NON-NLS-2$

            int hour = (i + 1) / 2;
            int minute = 0;

            boolean halfHourLine = (i % 2 == 0);
            if (halfHourLine) {
                minute = 30;
            }

            LocalTime time = LocalTime.of(hour, minute);

            if (time.isBefore(startTime)) {
                if (!line.getStyleClass().contains("early-hour-line")) { //$NON-NLS-1$
                    line.getStyleClass().add("early-hour-line"); //$NON-NLS-1$
                }
            }
            if (time.isAfter(endTime)) {
                if (!line.getStyleClass().contains("late-hour-line")) { //$NON-NLS-1$
                    line.getStyleClass().add("late-hour-line"); //$NON-NLS-1$
                }
            }

            switch (dayView.getEarlyLateHoursStrategy()) {
                case HIDE:
                    /*
                     * We do not show ... a) lines before the start time and after
                     * the end time b) lines directly on the start time or end time
                     * because they make the UI look messy
                     */
                    if (time.isBefore(startTime) || time.equals(startTime) || time.isAfter(endTime) || time.equals(endTime)) {
                        line.setVisible(false);
                    } else {
                        line.setVisible(true);
                    }
                    break;
                case SHOW:
                    line.setVisible(true);
                    break;
                case SHOW_COMPRESSED:
                    if (halfHourLine) {
                        line.setVisible(false);
                    } else {
                        line.setVisible(true);
                    }
                    break;
                default:
                    break;

            }
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        int lineCount = lines.size();

        T dayView = getSkinnable();
        LocalTime startTime = dayView.getStartTime();
        LocalTime endTime = dayView.getEndTime();

        boolean showEarlyHoursRegion = startTime.isAfter(LocalTime.MIN);
        boolean showLateHoursRegion = endTime.isBefore(LocalTime.MAX);

        earlyHoursRegion.setVisible(showEarlyHoursRegion);
        lateHoursRegion.setVisible(showLateHoursRegion);

        double earlyHoursY = ViewHelper.getTimeLocation(dayView, startTime);
        double lateHoursY = ViewHelper.getTimeLocation(dayView, endTime);

        earlyHoursRegion.resizeRelocate(snapPosition(contentX), snapPosition(contentY), snapSize(contentWidth), snapSize(earlyHoursY));
        lateHoursRegion.resizeRelocate(snapPosition(contentX), snapPosition(lateHoursY), snapSize(contentWidth), snapSize(contentHeight - lateHoursY));

        for (int i = 0; i < lineCount; i++) {
            Line line = lines.get(i);

            int hour = (i + 1) / 2;
            int minute = 0;

            boolean halfHourLine = (i % 2 == 0);
            if (halfHourLine) {
                minute = 30;
            }

            LocalTime time = LocalTime.of(hour, minute);

            double yy = snapPosition(contentY + ViewHelper.getTimeLocation(dayView, time));

            line.setStartX(snapPosition(contentX + 4));
            line.setStartY(yy);
            line.setEndX(snapPosition(contentX + contentWidth - 4));
            line.setEndY(yy);
        }

        // the dragged entry view
        if (draggedEntryView != null) {
            boolean showing = isRelevant(draggedEntryView.getEntry());
            draggedEntryView.setVisible(showing);
        }

        switch (dayView.getLayout()) {
            case STANDARD:
                layoutStandard(dayView, contentX, contentY, contentWidth, contentHeight);
                break;
            case SWIMLANE:
                layoutSwimlane(dayView, contentX, contentY, contentWidth, contentHeight);
                break;
            default:
                throw new IllegalArgumentException("unknown layout: " + dayView.getLayout()); //$NON-NLS-1$
        }

        LocalTime time = dayView.getTime();
        double y = snapPosition(contentY + ViewHelper.getTimeLocation(dayView, time));

        currentTimeLine.setStartX(snapPosition(contentX));
        currentTimeLine.setStartY(snapPosition(y));
        currentTimeLine.setEndX(snapPosition(contentX + contentWidth));
        currentTimeLine.setEndY(snapPosition(y));
        currentTimeLine.toFront();
        currentTimeCircle.setCenterX(snapPosition(contentX + currentTimeCircle.getRadius() + 4));
        currentTimeCircle.setCenterY(y);
        currentTimeCircle.toFront();
    }

    private void layoutStandard(DayView dayView, double contentX, double contentY, double contentWidth, double contentHeight) {
        List<DayEntryView> entryViews = getChildren().stream().filter(node -> node instanceof DayEntryView).map(node -> (DayEntryView) node).collect(Collectors.toList());
        layoutEntryViews(entryViews, dayView, contentX, contentY, contentWidth, contentHeight);
    }

    private void layoutSwimlane(DayView dayView, double contentX, double contentY, double contentWidth, double contentHeight) {
        List<Calendar> visibleCalendars = dayView.getCalendars().filtered(c -> getSkinnable().isCalendarVisible(c));

        double x = contentX;
        double w = contentWidth / (visibleCalendars.size());

        for (Calendar calendar : visibleCalendars) {

            List<DayEntryView> entryViews = getChildren().stream().filter(node -> node instanceof DayEntryView).map(node -> (DayEntryView) node).filter(view -> {
                Calendar cal;
                Entry<?> entry = view.getEntry();
                if (entry instanceof DraggedEntry) {
                    DraggedEntry draggedEntry = (DraggedEntry) view.getEntry();
                    cal = draggedEntry.getOriginalCalendar();
                } else {
                    cal = entry.getCalendar();
                }
                return cal != null && cal.equals(calendar);
            }).collect(Collectors.toList());

            layoutEntryViews(entryViews, dayView, x, contentY, w, contentHeight);
            x += w;

        }
    }

    private void layoutEntryViews(List<DayEntryView> entryViews, DayView dayView, double contentX, double contentY, double contentWidth, double contentHeight) {
        List<Placement> placements = Resolver.resolve(entryViews);

        if (placements != null) {
            contentWidth = contentWidth * dayView.getEntryWidthPercentage() / 100d;

            for (Placement placement : placements) {
                EntryViewBase<?> view = placement.getEntryView();

                Entry<?> entry = view.getEntry();

                LocalDate viewDate = dayView.getDate();
                LocalTime viewStartTime = entry.getStartTime();
                LocalTime viewEndTime = entry.getEndTime();

                double y1 = ViewHelper.getTimeLocation(dayView, entry.getStartTime());
                double y2 = ViewHelper.getTimeLocation(dayView, entry.getEndTime());

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

                view.getProperties().put("position", position); //$NON-NLS-1$
                view.getProperties().put("startDate", viewDate); //$NON-NLS-1$
                view.getProperties().put("endDate", viewDate); //$NON-NLS-1$
                view.getProperties().put("startTime", viewStartTime); //$NON-NLS-1$
                view.getProperties().put("endTime", viewEndTime); //$NON-NLS-1$

                double minHeight = view.minHeight(contentWidth);

                double columnWidth = contentWidth / placement.getColumnCount();
                double x = contentX + placement.getColumnIndex() * columnWidth;

                /*
                 * -2 on height to always have a gap between entries
                 */
                view.resizeRelocate(snapPosition(x), snapPosition(y1), snapSize(columnWidth), snapSize(Math.max(minHeight, y2 - y1 - 2)));
            }
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

                if (viewEntry.getId().equals(removedEntry.getId())) {
                    return true;
                }
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
        view.getProperties().put("control", getSkinnable()); //$NON-NLS-1$
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
        List<Entry<?>> entryList = dataMap.get(getSkinnable().getDate());

        LocalTime earliest = null;
        LocalTime latest = null;

        if (entryList != null) {
            entryList.removeIf(Entry::isFullDay);

            for (Entry<?> entry : entryList) {
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

        getSkinnable().getProperties().put("earliest.time.used", earliest);
        getSkinnable().getProperties().put("latest.time.used", latest);

        getSkinnable().requestLayout();

        LoggingDomain.VIEW.fine("updating entries in day view " + getSkinnable().getDate() + ": reason = " + reason + ", entry count: " + getChildren().stream().filter(child -> child instanceof DayEntryView).count());
    }

    @Override
    public String getLoaderName() {
        return "Day View"; //$NON-NLS-1$
    }

    @Override
    public LocalDate getLoadStartDate() {
        return getSkinnable().getDate();
    }

    @Override
    public LocalDate getLoadEndDate() {
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
