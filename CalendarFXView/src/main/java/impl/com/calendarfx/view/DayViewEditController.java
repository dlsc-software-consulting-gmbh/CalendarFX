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
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.DateControl.EditOperation;
import com.calendarfx.view.DateControl.EntryEditParameter;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.DraggedEntry;
import com.calendarfx.view.DraggedEntry.DragMode;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.EntryViewBase.HeightLayoutStrategy;
import com.calendarfx.view.RequestEvent;
import com.calendarfx.view.VirtualGrid;
import com.calendarfx.view.WeekView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class DayViewEditController {

    private static final Logger LOGGER = LoggingDomain.EDITING;

    private final DayViewBase view;
    private DayEntryView dayEntryView;
    private Entry<?> entry;
    private DragMode dragMode;
    private Handle handle;
    private Duration offsetDuration;
    private Duration entryDuration;

    public DayViewEditController(DayViewBase dayView) {
        this.view = Objects.requireNonNull(dayView);

        dayView.addEventFilter(MouseEvent.MOUSE_CLICKED, this::mouseClicked);
        dayView.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        dayView.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);

        final EventHandler<MouseEvent> mouseReleasedHandler = this::mouseReleased;

        // mouse released is very important for us. register with the scene, so we get that in any case.
        if (dayView.getScene() != null) {
            dayView.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        }

        // also register with the scene property. Mostly to remove our event filter if the component gets destroyed.
        dayView.sceneProperty().addListener(((observable, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            }
            if (newScene != null) {
                newScene.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            }
        }));
        dayView.addEventFilter(MouseEvent.MOUSE_MOVED, this::mouseMoved);

        lassoStartProperty().addListener(it -> dayView.setLassoStart(getLassoStart()));
        lassoEndProperty().addListener(it -> dayView.setLassoEnd(getLassoEnd()));

        setOnLassoFinished(dayView.getOnLassoFinished());
        dayView.onLassoFinishedProperty().addListener(it -> setOnLassoFinished(dayView.getOnLassoFinished()));
    }

    private enum Handle {
        TOP,
        CENTER,
        BOTTOM
    }

    private boolean initDragModeAndHandle(MouseEvent evt) {
        dragMode = null;
        handle = null;

        if (!(evt.getTarget() instanceof EntryViewBase)) {
            return false;
        }

        dayEntryView = (DayEntryView) evt.getTarget();
        entry = dayEntryView.getEntry();

        Calendar calendar = entry.getCalendar();
        if (calendar != null && calendar.isReadOnly()) {
            return false;
        }

        double y = evt.getY() - dayEntryView.getBoundsInParent().getMinY();

        LOGGER.finer("y-coordinate inside entry view: " + y);

        if (y > dayEntryView.getHeight() - 5) {
            if (dayEntryView.getHeightLayoutStrategy().equals(HeightLayoutStrategy.USE_START_AND_END_TIME) && view.getEntryEditPolicy().call(new EntryEditParameter(view, entry, EditOperation.CHANGE_END))) {
                dragMode = DragMode.END_TIME;
                handle = Handle.BOTTOM;
            }
        } else if (y < 5) {
            if (dayEntryView.getHeightLayoutStrategy().equals(HeightLayoutStrategy.USE_START_AND_END_TIME) && view.getEntryEditPolicy().call(new EntryEditParameter(view, entry, EditOperation.CHANGE_START))) {
                dragMode = DragMode.START_TIME;
                handle = Handle.TOP;
            }
        } else {
            if (view.getEntryEditPolicy().call(new EntryEditParameter(view, entry, EditOperation.MOVE))) {
                dragMode = DragMode.START_AND_END_TIME;
                handle = Handle.CENTER;
            }
        }

        return entry != null && dragMode != null && handle != null;
    }

    private void mouseMoved(MouseEvent evt) {
        initDragModeAndHandle(evt);

        if (dayEntryView != null) {
            if (handle == null) {
                dayEntryView.setCursor(Cursor.DEFAULT);
                return;
            }

            switch (handle) {
                case TOP:
                    dayEntryView.setCursor(Cursor.N_RESIZE);
                    break;
                case BOTTOM:
                    dayEntryView.setCursor(Cursor.S_RESIZE);
                    break;
                case CENTER:
                    dayEntryView.setCursor(Cursor.MOVE);
                    break;
                default:
                    dayEntryView.setCursor(Cursor.DEFAULT);
                    break;
            }
        }
    }

    private enum Operation {
        NONE,
        EDIT_ENTRY,
        EDIT_AVAILABILITY,
        CREATE_ENTRY
    }

    private void mouseClicked(MouseEvent evt) {
        // standard checks
        if (evt.isConsumed() || !evt.isStillSincePress() || !evt.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        // do not create new entries while the user is editing the availability calendar
        if (view.isEditAvailability()) {
            return;
        }

        // mouse clicks only work on day views, not on entry views
        if (!(evt.getTarget() instanceof DayViewBase)) {
            return;
        }

        if (evt.getClickCount() == view.getCreateEntryClickCount()) {
            Entry<?> entry = createEntryAt(evt);
            evt.consume();

            if (view.isShowDetailsUponEntryCreation()) {
                view.fireEvent(new RequestEvent(view, view, entry));
            }
        }
    }

    private Operation operation = Operation.NONE;

    private MouseEvent mousePressedEvent;

    private void mousePressed(MouseEvent evt) {
        if (evt.isConsumed() || !evt.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        mousePressedEvent = evt;

        entry = null;

        LOGGER.finer("mouse event source: " + evt.getSource());
        LOGGER.finer("mouse event target: " + evt.getTarget());
        LOGGER.finer("mouse event y-coordinate:" + evt.getY());
        LOGGER.finer("time: " + view.getZonedDateTimeAt(evt.getX(), evt.getY(), view.getZoneId()));

        if (evt.getTarget() instanceof DayViewBase) {
            mousePressedOnDayView(evt);
        } else if (evt.getTarget() instanceof EntryViewBase) {
            if (view.isEditAvailability()) {
                if (!view.isScrollingEnabled()) {
                    mousePressedEditAvailability(evt);
                }
            } else {
                mousePressedEditEntry(evt);
            }
        }
    }

    private boolean entryEditingAllowed;

    private void mousePressedEditEntry(MouseEvent evt) {
        operation = Operation.EDIT_ENTRY;

        entryEditingAllowed = false;

        dragMode = null;
        handle = null;

        LOGGER.finer("mouse event source: " + evt.getSource());
        LOGGER.finer("mouse event target: " + evt.getTarget());
        LOGGER.finer("mouse event y-coordinate:" + evt.getY());
        LOGGER.finer("time: " + view.getZonedDateTimeAt(evt.getX(), evt.getY(), view.getZoneId()));

        boolean successfulInitialization = initDragModeAndHandle(evt);

        if (successfulInitialization) {

            LOGGER.finer("drag mode: " + dragMode);
            LOGGER.finer("handle: " + handle);

            Callback<EntryEditParameter, Boolean> entryEditPolicy = view.getEntryEditPolicy();

            boolean operationAllowed = false;

            switch (dragMode) {
                case START_AND_END_TIME:
                    if (entryEditPolicy.call(new EntryEditParameter(view, entry, EditOperation.MOVE))) {
                        operationAllowed = true;

                        Instant time = view.getInstantAt(evt);
                        offsetDuration = Duration.between(entry.getStartAsZonedDateTime().toInstant(), time);
                        entryDuration = entry.getDuration();

                        LOGGER.finer("time at mouse pressed location: " + time);
                        LOGGER.finer("offset duration: " + offsetDuration);
                        LOGGER.finer("entry duration: " + entryDuration);
                    }
                    break;
                case END_TIME:
                    if (entryEditPolicy.call(new EntryEditParameter(view, entry, EditOperation.CHANGE_END))) {
                        operationAllowed = true;
                    }
                    break;
                case START_TIME:
                    if (entryEditPolicy.call(new EntryEditParameter(view, entry, EditOperation.CHANGE_START))) {
                        operationAllowed = true;
                    }
                    break;
                default:
                    break;
            }

            if (!operationAllowed) {
                return;
            }

            entryEditingAllowed = true;
        }
    }

    private void mousePressedOnDayView(MouseEvent evt) {
        if (view.isEditAvailability()) {
            mousePressedEditAvailability(evt);
        } else {
            operation = Operation.CREATE_ENTRY;
        }
    }

    private void mousePressedEditAvailability(MouseEvent evt) {
        operation = Operation.EDIT_AVAILABILITY;
        VirtualGrid availabilityGrid = view.getAvailabilityGrid();
        setLassoStart(snapToGrid(view.getInstantAt(evt), availabilityGrid, false));
        setLassoEnd(snapToGrid(view.getInstantAt(evt), availabilityGrid, false).plus(availabilityGrid.getAmount(), availabilityGrid.getUnit()));
    }

    private Entry<?> createEntryAt(MouseEvent evt) {
        Optional<Calendar> calendar = view.getCalendarAt(evt.getX(), evt.getY());
        Instant instantAt = view.getInstantAt(evt);

        VirtualGrid virtualGrid = view.getVirtualGrid();
        if (virtualGrid != null) {
            instantAt = snapToGrid(instantAt, virtualGrid, false);
        }

        ZonedDateTime time = ZonedDateTime.ofInstant(instantAt, view.getZoneId());
        //Entry<?> newEntry = view.createEntryAt(time, calendar.orElse(null), false);
        Entry<?> newEntry = view.createEntryAt(time, calendar.orElse(null), false);
        newEntry.changeStartTime(LocalTime.MIDNIGHT);
        view.getParent();
        Duration duration = newEntry.getMinimumDuration();

        LOGGER.fine("minimum duration for the entry is " + duration);

        if (virtualGrid != null) {
            LOGGER.fine("checking the virtual grid duration");
            Duration gridAmount = Duration.of(virtualGrid.getAmount(), virtualGrid.getUnit());
            if (gridAmount.toMillis() > duration.toMillis()) {
                LOGGER.fine("using the grid amount as it is longer than the minimum duration of the entry");
                duration = gridAmount;
            }
        }

        newEntry.setInterval(newEntry.getInterval().withEndTime(newEntry.getInterval().getStartTime().plus(duration)));
        System.out.println("ENTRIES:" + view.getSelections());
        view.getSelections().clear();
        view.getSelections().add(newEntry);

        return newEntry;
    }

    private void mouseDragged(MouseEvent evt) {
        if (evt.isStillSincePress()) {
            return;
        }

        switch (operation) {
            case NONE:
                break;
            case EDIT_ENTRY:
                Calendar calendar = entry.getCalendar();
                if (!calendar.isReadOnly()) {
                    mouseDraggedEditEntry(evt);
                }
                break;
            case EDIT_AVAILABILITY:
                mouseDraggedEditAvailability(evt);
                break;
            case CREATE_ENTRY:
                mouseDraggedCreateEntry(evt);
                break;
        }
    }

    private void mouseDraggedEditAvailability(MouseEvent evt) {
        setLassoEnd(snapToGrid(view.getInstantAt(evt), view.getAvailabilityGrid(), true));
    }

    private void mouseDraggedCreateEntry(MouseEvent evt) {
        if (entry == null) {
            dragMode = null;
            handle = null;

            // Important, use the initial mouse event when the user pressed the button
            entry = createEntryAt(mousePressedEvent);

            DayView dayView = null;

            if (view instanceof DayView) {
                dayView = (DayView) view;
            } else if (view instanceof WeekView) {
                WeekView weekView = (WeekView) view;
                dayView = weekView.getWeekDayViews().get(0);
            }

            if (dayView != null) {
                dragMode = DragMode.END_TIME;
                handle = Handle.BOTTOM;
            }

            if (dayEntryView != null) {
                dayEntryView.getProperties().put("dragged-end", true);
            }

            DraggedEntry draggedEntry = new DraggedEntry(entry, dragMode);
            draggedEntry.setOffsetDuration(offsetDuration);
            view.setDraggedEntry(draggedEntry);
        }

        changeEndTime(evt);
    }

    private void mouseDraggedEditEntry(MouseEvent evt) {
        if (entryEditingAllowed && view.getDraggedEntry() == null) {
            DraggedEntry draggedEntry = new DraggedEntry(entry, dragMode);
            draggedEntry.setOffsetDuration(offsetDuration);
            view.setDraggedEntry(draggedEntry);

            switch (dragMode) {
                case START_AND_END_TIME:
                    if (dayEntryView != null) {
                        dayEntryView.getProperties().put("dragged", true);
                    }
                    break;
                case END_TIME:
                    if (dayEntryView != null) {
                        dayEntryView.getProperties().put("dragged-end", true);
                    }
                    break;
                case START_TIME:
                    if (dayEntryView != null) {
                        dayEntryView.getProperties().put("dragged-start", true);
                    }
                    break;
                default:
                    break;
            }
        }

        switch (dragMode) {
            case START_TIME:
                switch (handle) {
                    case TOP:
                    case BOTTOM:
                        changeStartTime(evt);
                        break;
                    case CENTER:
                        break;
                }
                break;
            case END_TIME:
                switch (handle) {
                    case TOP:
                    case BOTTOM:
                        changeEndTime(evt);
                        break;
                    case CENTER:
                        break;
                }
                break;
            case START_AND_END_TIME:
                changeStartAndEndTime(evt);
                break;
        }
    }

    private void mouseReleased(MouseEvent evt) {
        if (!evt.getButton().equals(MouseButton.PRIMARY) || evt.getClickCount() > 1) {
            return;
        }

        switch (operation) {
            case NONE:
                break;
            case EDIT_ENTRY:
                mouseReleasedEditEntry();
                break;
            case EDIT_AVAILABILITY:
                mouseReleasedEditAvailability();
                break;
            case CREATE_ENTRY:
                mouseReleasedCreateEntry();
                break;
        }

        operation = Operation.NONE;
    }

    private void mouseReleasedEditAvailability() {
        getOnLassoFinished().accept(getLassoStart(), getLassoEnd());
        setLassoStart(null);
        setLassoEnd(null);
    }

    private void mouseReleasedEditEntry() {
        if (dayEntryView != null) {
            dayEntryView.getProperties().put("dragged", false);
            dayEntryView.getProperties().put("dragged-start", false);
            dayEntryView.getProperties().put("dragged-end", false);
        }

        DraggedEntry draggedEntry = view.getDraggedEntry();

        if (draggedEntry != null) {
            view.setDraggedEntry(null);

            Interval newInterval = draggedEntry.getInterval();

//            if (entry.isRecurrence()) {
//                Entry sourceEntry = entry.getRecurrenceSourceEntry();
//                Interval sourceInterval = sourceEntry.getInterval();
//
//                sourceInterval = sourceInterval.withStartTime(newInterval.getStartTime());
//                sourceInterval = sourceInterval.withDuration(newInterval.getDuration());
//
//                sourceEntry.setInterval(sourceInterval);
//            } else {
                entry.setInterval(newInterval);
//            }

            if (view.isShowDetailsUponEntryCreation() && operation.equals(Operation.CREATE_ENTRY)) {
                view.fireEvent(new RequestEvent(view, view, entry));
            }
        }
    }

    private void mouseReleasedCreateEntry() {
        if (entry != null) {
            Calendar calendar = entry.getCalendar();
            if (calendar.isReadOnly()) {
                return;
            }

            if (dayEntryView != null) {
                dayEntryView.getProperties().put("dragged", false);
                dayEntryView.getProperties().put("dragged-start", false);
                dayEntryView.getProperties().put("dragged-end", false);
            }

            /*
             * We might run in the sampler application. Then the entry view will not
             * be inside a date control.
             */
            DraggedEntry draggedEntry = view.getDraggedEntry();

            if (draggedEntry != null) {
                view.setDraggedEntry(null);
                entry.setInterval(draggedEntry.getInterval());
                if (view.isShowDetailsUponEntryCreation() && operation.equals(Operation.CREATE_ENTRY)) {
                    view.fireEvent(new RequestEvent(view, view, entry));
                }
            }
        }
    }

    private void changeStartTime(MouseEvent evt) {
        DraggedEntry draggedEntry = view.getDraggedEntry();

        Instant gridTime = fixTimeIfOutsideView(evt, snapToGrid(view.getInstantAt(evt), view.getVirtualGrid(), true));

        LOGGER.finer("changing start time, time = " + gridTime);

        if (isMinimumDuration(entry, entry.getEndAsZonedDateTime().toInstant(), gridTime)) {

            LocalDate startDate;
            LocalDate endDate;

            LocalTime startTime;
            LocalTime endTime;

            ZonedDateTime gridZonedTime = ZonedDateTime.ofInstant(gridTime, draggedEntry.getZoneId());

            if (gridTime.isAfter(entry.getEndAsZonedDateTime().toInstant())) {
                if (view.isEnableStartAndEndTimesFlip()) {
                    startTime = entry.getEndTime();
                    startDate = entry.getEndDate();
                    endTime = gridZonedTime.toLocalTime();
                    endDate = gridZonedTime.toLocalDate();
                } else {
                    startTime = entry.getEndTime().minus(entry.getMinimumDuration());
                    startDate = entry.getEndDate();
                    endTime = entry.getEndTime();
                    endDate = entry.getEndDate();
                }
            } else {
                startDate = gridZonedTime.toLocalDate();
                startTime = gridZonedTime.toLocalTime();
                endTime = entry.getEndTime();
                endDate = entry.getEndDate();
            }

            LOGGER.finer("new interval: sd = " + startDate + ", st = " + startTime + ", ed = " + endDate + ", et = " + endTime);

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);
        }
    }

    private void changeEndTime(MouseEvent evt) {
        DraggedEntry draggedEntry = view.getDraggedEntry();

        Instant gridTime = fixTimeIfOutsideView(evt, snapToGrid(view.getInstantAt(evt), view.getVirtualGrid(), true));

        LOGGER.finer("changing end time, time = " + gridTime);

        if (isMinimumDuration(entry, entry.getStartAsZonedDateTime().toInstant(), gridTime)) {

            LOGGER.finer("dragged entry: " + draggedEntry.getInterval());

            LocalDate startDate;
            LocalDate endDate;

            LocalTime startTime;
            LocalTime endTime;

            ZonedDateTime gridZonedTime = ZonedDateTime.ofInstant(gridTime, draggedEntry.getZoneId());

            if (gridTime.isBefore(entry.getStartAsZonedDateTime().toInstant())) {
                if (view.isEnableStartAndEndTimesFlip()) {
                    endTime = entry.getStartTime();
                    endDate = entry.getStartDate();
                    startTime = gridZonedTime.toLocalTime();
                    startDate = gridZonedTime.toLocalDate();
                } else {
                    startTime = entry.getStartTime();
                    startDate = entry.getStartDate();
                    endTime = entry.getStartTime().plus(entry.getMinimumDuration());
                    endDate = entry.getStartDate();
                }
            } else {
                startTime = entry.getStartTime();
                startDate = entry.getStartDate();
                endTime = gridZonedTime.toLocalTime();
                endDate = gridZonedTime.toLocalDate();
            }

            LOGGER.finer("new interval: sd = " + startDate + ", st = " + startTime + ", ed = " + endDate + ", et = " + endTime);

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);
        }
    }

    private void changeStartAndEndTime(MouseEvent evt) {
        DraggedEntry draggedEntry = view.getDraggedEntry();

        Instant locationTime = fixTimeIfOutsideView(evt, view.getInstantAt(evt));

        LOGGER.fine("changing start/end time, time = " + locationTime + " offset duration = " + offsetDuration);

        if (locationTime != null && offsetDuration != null) {

            Instant newStartTime = locationTime.minus(offsetDuration);
            LOGGER.fine("new start time = " + newStartTime);

            newStartTime = snapToGrid(newStartTime, view.getVirtualGrid(), true);
            Instant newEndTime = newStartTime.plus(entryDuration);

            LOGGER.fine("new start time (grid) = " + newStartTime);
            LOGGER.fine("new end time = " + newEndTime);

            ZonedDateTime gridStartZonedTime = ZonedDateTime.ofInstant(newStartTime, draggedEntry.getZoneId());
            ZonedDateTime gridEndZonedTime = ZonedDateTime.ofInstant(newEndTime, draggedEntry.getZoneId());

            LocalDate startDate = gridStartZonedTime.toLocalDate();
            LocalTime startTime = gridStartZonedTime.toLocalTime();

            LocalDate endDate = LocalDateTime.of(startDate, startTime).plus(entryDuration).toLocalDate();
            LocalTime endTime = gridEndZonedTime.toLocalTime();

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);
        }
    }

    private Instant fixTimeIfOutsideView(MouseEvent evt, Instant gridTime) {
        /*
         * Fix the time calculation if the mouse cursor exits the day view area.
         * Note: day view can also be a WeekView as it extends DayViewBase.
         */
        if (evt.getX() > view.getWidth() || evt.getX() < 0) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(gridTime, entry.getZoneId());
            gridTime = ZonedDateTime.of(entry.getStartDate(), zdt.toLocalTime(), zdt.getZone()).toInstant();
        }
        return gridTime;
    }

    private boolean isMinimumDuration(Entry<?> entry, Instant
            timeA, Instant timeB) {
        Duration minDuration = entry.getMinimumDuration().abs();
        if (minDuration != null) {
            Duration duration = Duration.between(timeA, timeB).abs();
            return !duration.minus(minDuration).isNegative();
        }

        return true;
    }

    private Instant snapToGrid(Instant time, VirtualGrid grid,
                               boolean checkCloser) {
        if (grid == null) {
            return time;
        }

        DayOfWeek firstDayOfWeek = view.getFirstDayOfWeek();
        Instant lowerTime = grid.adjustTime(time, view.getZoneId(), false, firstDayOfWeek);

        if (checkCloser) {
            Instant upperTime = grid.adjustTime(time, view.getZoneId(), true, firstDayOfWeek);
            if (Duration.between(time, upperTime).abs().minus(Duration.between(time, lowerTime).abs()).isNegative()) {
                return upperTime;
            }
        }

        return lowerTime;
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

    private final ObjectProperty<BiConsumer<Instant, Instant>> onLassoFinished = new SimpleObjectProperty<>(this, "onLassoFinished", (start, end) -> {
    });

    public final BiConsumer<Instant, Instant> getOnLassoFinished() {
        return onLassoFinished.get();
    }

    public final ObjectProperty<BiConsumer<Instant, Instant>> onLassoFinishedProperty
            () {
        return onLassoFinished;
    }

    public final void setOnLassoFinished
            (BiConsumer<Instant, Instant> onLassoFinished) {
        this.onLassoFinished.set(onLassoFinished);
    }
}
