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
import com.calendarfx.view.AllDayEntryView;
import com.calendarfx.view.AllDayView;
import com.calendarfx.view.DraggedEntry;
import com.calendarfx.view.EntryViewBase;
import impl.com.calendarfx.view.util.Placement;
import impl.com.calendarfx.view.util.TimeBoundsResolver;
import impl.com.calendarfx.view.util.Util;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AllDayViewSkin extends DateControlSkin<AllDayView> implements LoadDataSettingsProvider {

    private static final String ALL_DAY_BACKGROUND_REGION = "day-region";
    private static final String ALL_DAY_BACKGROUND_REGION_TODAY = "today";
    private static final String ALL_DAY_BACKGROUND_REGION_WEEKEND = "weekend";

    private final DataLoader dataLoader;
    private final HBox container;

    private final Group entryViewGroup = new Group();

    public AllDayViewSkin(AllDayView view) {
        super(view);

        view.setFocusTraversable(true);

        container = new HBox();
        container.getStyleClass().add("container");
        getChildren().add(container);

        entryViewGroup.setMouseTransparent(false);
        entryViewGroup.setManaged(false);
        getChildren().add(entryViewGroup);

        // update backgrounds
        InvalidationListener updateBackgroundsListener = evt -> updateBackgrounds();
        view.numberOfDaysProperty().addListener(updateBackgroundsListener);
        view.showTodayProperty().addListener(updateBackgroundsListener);
        view.weekFieldsProperty().addListener(updateBackgroundsListener);
        view.adjustToFirstDayOfWeekProperty().addListener(updateBackgroundsListener);

        // update entries
        InvalidationListener updateEntriesListener = evt -> updateEntries("a view property changed");
        view.numberOfDaysProperty().addListener(updateEntriesListener);
        view.dateProperty().addListener(updateEntriesListener);
        view.extraPaddingProperty().addListener(updateEntriesListener);
        view.rowHeightProperty().addListener(updateEntriesListener);
        view.rowSpacingProperty().addListener(updateEntriesListener);
        view.columnSpacingProperty().addListener(updateEntriesListener);
        view.weekFieldsProperty().addListener(updateEntriesListener);
        view.adjustToFirstDayOfWeekProperty().addListener(updateEntriesListener);

        updateBackgrounds();

        dataLoader = new DataLoader(this);
        Label text = new Label();
        text.setText("TEST");



        updateEntries("initial load");
    }

    @Override
    protected void refreshData() {
        updateEntries("refreshData() called");
    }

    private void updateEntries(String reason) {
        LoggingDomain.PERFORMANCE.fine("updating entries, reason: " + reason);

        entryViewGroup.getChildren().clear();

        Map<LocalDate, List<Entry<?>>> dataMap = new HashMap<>();
        dataLoader.loadEntries(dataMap);

        Set<Entry<?>> entrySet = new HashSet<>();
        for (List<Entry<?>> entryList : dataMap.values()) {
            entrySet.addAll(entryList);
        }

        entrySet.removeIf(ref -> !ref.isFullDay());

        List<Entry<?>> entryList = new ArrayList<>(entrySet);

        for (Entry<?> entry : entryList) {
            doAddEntryView(entry);
        }

        getSkinnable().autosize();


        getSkinnable().requestLayout();
    }

    private List<EntryViewBase> findEntryViews(Entry<?> entry) {
        return entryViewGroup.getChildren().stream()
                .map(node -> (EntryViewBase) node)
                .filter(e -> e.getEntry().getId().equals(entry.getId()))
                .collect(Collectors.toList());
    }

    private boolean removeEntryViews(Entry<?> entry, String reason) {
        if (reason != null) {
            LoggingDomain.VIEW.fine("removing entry, reason = " + reason + ", date = " + getSkinnable().getDate());
        }

        boolean removed = Util.removeChildren(entryViewGroup, node -> {
            AllDayEntryView view = (AllDayEntryView) node;
            Entry<?> viewEntry = view.getEntry();
            return viewEntry.getId().equals(entry.getId());
        });

        if (removed && !(entry instanceof DraggedEntry) && LoggingDomain.VIEW.isLoggable(Level.FINE)) {
            LoggingDomain.VIEW.fine("successfully removed the entry view of entry " + entry);
        }

        return removed;
    }

    private void addEntryViews(Entry<?> entry, String reason) {
        LoggingDomain.VIEW.fine("adding entry, reason = " + reason + ", date = " + getSkinnable().getDate());
        if (entry.isRecurring()) {
            Map<LocalDate, Entry<?>> recurrenceEntries = findRecurrenceEntries(entry);
            recurrenceEntries.forEach((date, recurrence) -> doAddEntryView(recurrence));
        } else {
            doAddEntryView(entry);
        }

        getSkinnable().requestLayout();
    }

    private Map<LocalDate, Entry<?>> findRecurrenceEntries(Entry<?> entry) {
        Calendar calendar = entry.getCalendar();
        LocalDate startDate = getLoadStartDate();
        LocalDate endDate = getLoadEndDate();

        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, endDate, getZoneId());
        Map<LocalDate, Entry<?>> result = new HashMap<>();

        entries.forEach((date, list) -> {
            if (!list.isEmpty()) {
                Optional<Entry<?>> first = list.stream()
                        .filter(e -> e.getId().equals(entry.getId()))
                        .filter(e -> e.getStartDate().equals(date))
                        .findFirst();
                if (first.isPresent()) {
                    result.put(date, first.get());
                }
            }
        });

        return result;
    }

    private AllDayEntryView doAddEntryView(Entry<?> entry) {
        Callback<Entry<?>, AllDayEntryView> factory = getSkinnable().getEntryViewFactory();
        AllDayEntryView view = factory.call(entry);
        view.getProperties().put("control", getSkinnable());
        view.setManaged(false);

        int index = findIndex(entry);
        entryViewGroup.getChildren().add(index, view);

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
        int childrenSize = entryViewGroup.getChildren().size();

        for (int i = 0; i < childrenSize; i++) {
            Node node = entryViewGroup.getChildren().get(i);
            AllDayEntryView view = (AllDayEntryView) node;
            Entry<?> viewEntry = view.getEntry();
            if (viewEntry.getStartAsZonedDateTime().isAfter(entry.getStartAsZonedDateTime())) {
                return i;
            }
        }

        return childrenSize;
    }

    @Override
    protected void calendarChanged(Calendar calendar) {
        updateEntries("calendar changed");
    }

    @Override
    protected void entryTitleChanged(CalendarEvent evt) {
        LoggingDomain.VIEW.fine("handle entry title changed, date = " + getSkinnable().getDate());
        Entry<?> entry = evt.getEntry();
        if (entry.isFullDay()) {
            // no need to check for relevance, probably faster to just look for entry views
            findEntryViews(entry).forEach(entryView -> entryView.getEntry().setTitle(evt.getEntry().getTitle()));
        }
    }

    @Override
    protected void entryLocationChanged(CalendarEvent evt) {
        LoggingDomain.VIEW.fine("handle entry location changed, date = " + getSkinnable().getDate());
        Entry<?> entry = evt.getEntry();
        if (entry.isFullDay()) {
            // no need to check for relevance, probably faster to just look for entry views
            findEntryViews(entry).forEach(entryView -> entryView.getEntry().setLocation(evt.getEntry().getLocation()));
        }
    }

    @Override
    protected void entryUserObjectChanged(CalendarEvent evt) {
        LoggingDomain.VIEW.fine("handle entry user object changed, date = " + getSkinnable().getDate());
        Entry<?> entry = evt.getEntry();
        if (entry.isFullDay()) {
            // no need to check for relevance, probably faster to just look for entry views
            findEntryViews(entry).forEach(entryView -> entryView.getEntry().setUserObject(evt.getEntry().getUserObject()));
        }
    }

    @Override
    protected void entryCalendarChanged(CalendarEvent evt) {
        LoggingDomain.VIEW.fine("handle entry calendar changed, date = " + getSkinnable().getDate());

        Entry<?> entry = evt.getEntry();
        if (evt.getCalendar() == null) {
            removeEntryViews(entry, "entry was deleted");
        } else {
            if (entry.isFullDay() && isRelevant(entry)) {
                List<EntryViewBase> entryView = findEntryViews(entry);
                if (!entryView.isEmpty()) {
                    entryView.forEach(view -> view.getEntry().setCalendar(evt.getCalendar()));
                } else {
                    addEntryViews(entry, "entry calendar changed");
                }
            }
        }

        getSkinnable().requestLayout();
    }

    @Override
    protected void entryFullDayChanged(CalendarEvent evt) {
        LoggingDomain.VIEW.fine("handle entry full day flag changed, date = " + getSkinnable().getDate());
        Entry<?> entry = evt.getEntry();
        if (isRelevant(entry)) {
            removeEntryViews(entry, "full day flag changed to false");
            if (entry.isFullDay()) {
                addEntryViews(entry, "full day flag changed to true, no entry view can be present");
            }
        }
        getSkinnable().requestLayout();
    }

    @Override
    protected void entryRecurrenceRuleChanged(CalendarEvent evt) {
        LoggingDomain.VIEW.fine("handle entry recurrence rule changed, date = " + getSkinnable().getDate());
        Entry<?> entry = evt.getEntry();

        /*
         * We only care about full day entries in this view.
         */
        if (entry.isFullDay()) {
            // remove all entry views
            removeEntryViews(entry, "recurrence rule changed");
            if (isRelevant(entry)) {
                addEntryViews(entry, "recurrence rule changed");
            }
        }

        getSkinnable().requestLayout();
    }

    @Override
    protected void entryIntervalChanged(CalendarEvent evt) {
        LoggingDomain.VIEW.fine("handle entry interval changed, date = " + getSkinnable().getDate());

        Entry<?> entry = evt.getEntry();

        /*
         * We only care about full day entries in this view.
         */
        if (entry.isFullDay()) {
            // remove all entry views
            removeEntryViews(entry, "interval changed");
            if (isRelevant(entry)) {
                addEntryViews(entry, "interval changed");
            }
        }

        getSkinnable().requestLayout();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {

        List<AllDayEntryView> entryViews = entryViewGroup.getChildren().stream().filter(node -> node instanceof AllDayEntryView).map(node -> (AllDayEntryView) node).collect(Collectors.toList());

        List<Placement> placements = TimeBoundsResolver.resolve(entryViews);

        int maxPosition = 0;
        for (Placement p : placements) {
            maxPosition = Math.max(maxPosition, p.getColumnIndex());
        }

        AllDayView view = getSkinnable();

        Insets insets = view.getInsets();
        Insets extraPadding = view.getExtraPadding();

        double rowHeight = view.getRowHeight();
        double rowSpacing = view.getRowSpacing();
        return (maxPosition + 1) * rowHeight + (maxPosition * rowSpacing) + insets.getTop() + insets.getBottom() * extraPadding.getTop() + extraPadding.getBottom();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        AllDayView view = getSkinnable();

        double rowHeight = view.getRowHeight();
        double rowSpacing = view.getRowSpacing();

        double height = 0;

        Insets extraPadding = view.getExtraPadding();

        List<AllDayEntryView> entryViews = entryViewGroup.getChildren().stream().map(node -> (AllDayEntryView) node).collect(Collectors.toList());

        List<Placement> placements = TimeBoundsResolver.resolve(entryViews);

        for (Placement placement : placements) {
            EntryViewBase<?> entryView = placement.getEntryView();
            Entry<?> entry = entryView.getEntry();

            LocalDate startDate = view.getDate();
            if (view.isAdjustToFirstDayOfWeek()) {
                startDate = Util.adjustToFirstDayOfWeek(view.getDate(), view.getFirstDayOfWeek());
            }

            LocalDate endDate = startDate.plusDays(view.getNumberOfDays() - 1);

            long deltaDays = ChronoUnit.DAYS.between(startDate, entry.getStartDate());

            long entryDurationInDays = ChronoUnit.DAYS.between(entry.getStartDate(), entry.getEndDate()) + 1;

            if (deltaDays < 0) {
                entryDurationInDays += deltaDays;
            }

            if (entry.getStartDate().isBefore(startDate)) {
                entryView.getProperties().put("startDate", startDate);
            } else {
                entryView.getProperties().put("startDate", entry.getStartDate());
            }

            if (entry.getEndDate().isAfter(endDate)) {
                entryView.getProperties().put("endDate", endDate);
            } else {
                entryView.getProperties().put("endDate", entry.getEndDate());
            }

            entryDurationInDays = Math.max(entryDurationInDays, 1);

            double dayWidth = contentWidth / view.getNumberOfDays();

            double x = Math.max(0, contentX + (deltaDays * dayWidth));
            double y = contentY + placement.getColumnIndex() * (rowHeight + rowSpacing) + extraPadding.getTop();

            double w;
            if (view.getNumberOfDays() == 1) {
                w = contentWidth + 1;
            } else {
                w = Math.min(entryDurationInDays * dayWidth - view.getColumnSpacing(), contentWidth - x);
            }

            entryView.setMaxHeight(rowHeight);

            entryView.resizeRelocate(snapPositionX(x), snapPositionY(y), snapSizeX(w), snapSizeY(rowHeight));

            height = Math.max(height, y + rowHeight);
        }
    }

    private void updateBackgrounds() {
        container.getChildren().clear();

        AllDayView allDayView = getSkinnable();
        Callback<AllDayView, Region> separatorFactory = allDayView.getSeparatorFactory();

        int numberOfDays = allDayView.getNumberOfDays();
        for (int i = 0; i < numberOfDays; i++) {
            Region region = new Region();
            region.setPrefWidth(1); // equal width distribution
            region.setMaxWidth(Double.MAX_VALUE);
            region.getStyleClass().add(ALL_DAY_BACKGROUND_REGION);

            final int day = i;
            allDayView.dateProperty().addListener(evt -> updateRegion(region, day));
            updateRegion(region, day);

            HBox.setHgrow(region, Priority.ALWAYS);
            container.getChildren().add(region);

            if (separatorFactory != null && i < numberOfDays - 1) {
                Region separator = separatorFactory.call(allDayView);
                if (separator != null) {
                    container.getChildren().add(separator);
                    HBox.setHgrow(separator, Priority.NEVER);
                }
            }
        }

        allDayView.requestLayout();
    }

    private void updateRegion(Region region, int day) {
        final AllDayView view = getSkinnable();

        LocalDate startDate = view.getDate();

        if (view.isAdjustToFirstDayOfWeek()) {
            startDate = Util.adjustToFirstDayOfWeek(view.getDate(), view.getFirstDayOfWeek());
        }

        LocalDate date = getDate(startDate, day);

        if (view.isShowToday() && date.equals(view.getToday())) {
            if (!region.getStyleClass().contains(ALL_DAY_BACKGROUND_REGION_TODAY)) {
                region.getStyleClass().add(ALL_DAY_BACKGROUND_REGION_TODAY);
            }
        } else {
            region.getStyleClass().remove(ALL_DAY_BACKGROUND_REGION_TODAY);
        }

        if (view.getWeekendDays().contains(date.getDayOfWeek())) {
            if (!region.getStyleClass().contains(ALL_DAY_BACKGROUND_REGION_WEEKEND)) {
                region.getStyleClass().add(ALL_DAY_BACKGROUND_REGION_WEEKEND);
            }
        } else {
            region.getStyleClass().remove(ALL_DAY_BACKGROUND_REGION_WEEKEND);
        }
    }

    private LocalDate getDate(LocalDate startDate, int dayCount) {
        return startDate.plusDays(dayCount);
    }

    @Override
    public String getLoaderName() {
        return "All Day View";
    }

    @Override
    public LocalDate getLoadStartDate() {
        AllDayView view = getSkinnable();

        if (view.isAdjustToFirstDayOfWeek()) {

            /*
             * The month view also shows the last couple of days of the previous
             * month.
             */
            return Util.adjustToFirstDayOfWeek(view.getDate(), view.getFirstDayOfWeek());

        }

        return view.getDate();
    }

    @Override
    public LocalDate getLoadEndDate() {
        return getLoadStartDate().plusDays(getSkinnable().getNumberOfDays() - 1);
    }

    @Override
    public ZoneId getZoneId() {
        return getSkinnable().getZoneId();
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
