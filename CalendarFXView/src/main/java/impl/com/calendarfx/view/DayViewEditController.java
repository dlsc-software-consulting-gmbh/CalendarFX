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
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.DraggedEntry;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.EntryViewBase.HeightLayoutStrategy;
import com.calendarfx.view.VirtualGrid;
import com.calendarfx.view.WeekView;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.logging.Logger;

public class DayViewEditController {

    private static final Logger LOGGER = LoggingDomain.EDITING;

    private boolean dragging;
    private final DayViewBase dayView;
    private DayEntryView dayEntryView;
    private Entry<?> entry;
    private DraggedEntry.DragMode dragMode;
    private Handle handle;
    private Duration offsetDuration;
    private Duration entryDuration;

    public DayViewEditController(DayViewBase dayView) {
        this.dayView = Objects.requireNonNull(dayView);

        final EventHandler<MouseEvent> mouseReleasedHandler = this::mouseReleased;

        dayView.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        dayView.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        // mouse released is very important for us. register with the scene so we get that in any case.
        if (dayView.getScene() != null) {
            dayView.getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            dayView.getScene().addEventFilter(MouseEvent.MOUSE_EXITED, mouseReleasedHandler);
        }
        // also register with the scene property. Mostly to remove our event filter if the component gets destroyed.
        dayView.sceneProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
                oldValue.removeEventFilter(MouseEvent.MOUSE_EXITED, mouseReleasedHandler);
            }
            if (newValue != null) {
                newValue.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
                newValue.addEventFilter(MouseEvent.MOUSE_EXITED, mouseReleasedHandler);
            }
        }));
        dayView.addEventFilter(MouseEvent.MOUSE_MOVED, this::mouseMoved);
    }

    private enum Handle {
        TOP,
        CENTER,
        BOTTOM
    }

    private void initDragModeAndHandle(MouseEvent evt) {
        dragMode = null;
        handle = null;

        if (!(evt.getTarget() instanceof EntryViewBase)) {
            return;
        }

        dayEntryView = (DayEntryView) evt.getTarget();

        entry = dayEntryView.getEntry();
        Calendar calendar = entry.getCalendar();
        if (calendar.isReadOnly()) {
            return;
        }

        double y = evt.getY() - dayEntryView.getBoundsInParent().getMinY();

        LOGGER.finer("y-coordinate inside entry view: " + y);

        if (y > dayEntryView.getHeight() - 5) {
            if (dayEntryView.getHeightLayoutStrategy().equals(HeightLayoutStrategy.USE_START_AND_END_TIME) && dayView.getEntryEditPolicy().call(new DateControl.EntryEditParameter(dayView, entry, DateControl.EditOperation.CHANGE_END))) {
                dragMode = DraggedEntry.DragMode.END_TIME;
                handle = Handle.BOTTOM;
            }
        } else if (y < 5) {
            if (dayEntryView.getHeightLayoutStrategy().equals(HeightLayoutStrategy.USE_START_AND_END_TIME) && dayView.getEntryEditPolicy().call(new DateControl.EntryEditParameter(dayView, entry, DateControl.EditOperation.CHANGE_START))) {
                dragMode = DraggedEntry.DragMode.START_TIME;
                handle = Handle.TOP;
            }
        } else {
            if (dayView.getEntryEditPolicy().call(new DateControl.EntryEditParameter(dayView, entry, DateControl.EditOperation.MOVE))) {
                dragMode = DraggedEntry.DragMode.START_AND_END_TIME;
                handle = Handle.CENTER;
            }
        }
    }

    private void mouseMoved(MouseEvent evt) {
        if (!dragging) {
            initDragModeAndHandle(evt);
        }

        if (handle == null) {
            if (dayEntryView != null) {
                dayEntryView.setCursor(Cursor.DEFAULT);
            }
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


    private void mousePressed(MouseEvent evt) {
        dragMode = null;
        handle = null;

        if (!evt.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        LOGGER.finer("mouse event source: " + evt.getSource());
        LOGGER.finer("mouse event target: " + evt.getTarget());
        LOGGER.finer("mouse event y-coordinate:" + evt.getY());
        LOGGER.finer("time: " + dayView.getZonedDateTimeAt(evt.getX(), evt.getY()));

        if (!(evt.getTarget() instanceof EntryViewBase)) {
            return;
        }
        Entry entry = ((EntryViewBase) evt.getTarget()).getEntry();
        if (entry == null) {
            return;
        }

        initDragModeAndHandle(evt);

        LOGGER.finer("drag mode: " + dragMode);
        LOGGER.finer("handle: " + handle);

        if (dragMode == null) {
            return;
        }

        switch (dragMode) {
            case START_AND_END_TIME:
                if (dayView.getEntryEditPolicy().call(new DateControl.EntryEditParameter(dayView, entry, DateControl.EditOperation.MOVE))) {
                    dragging = true;
                    dayEntryView.getProperties().put("dragged", true); 

                    LocalDateTime time = dayView.getZonedDateTimeAt(evt.getX(), evt.getY()).toLocalDateTime();
                    offsetDuration = Duration.between(entry.getStartAsLocalDateTime(), time);
                    entryDuration = entry.getDuration();

                    LOGGER.finer("time at mouse pressed location: " + time);
                    LOGGER.finer("offset duration: " + offsetDuration);
                    LOGGER.finer("entry duration: " + entryDuration);

                    dayView.requestLayout();
                }
                break;
            case END_TIME:
                if (dayView.getEntryEditPolicy().call(new DateControl.EntryEditParameter(dayView, entry, DateControl.EditOperation.CHANGE_END))) {
                    dragging = true;
                    dayEntryView.getProperties().put("dragged-end", true); 
                }
                break;
            case START_TIME:
                if (dayView.getEntryEditPolicy().call(new DateControl.EntryEditParameter(dayView, entry, DateControl.EditOperation.CHANGE_START))) {
                    dragging = true;
                    dayEntryView.getProperties().put("dragged-start", true); 
                }
                break;
            default:
                break;

        }

        if (!dragging) {
            return;
        }

        DayView dayView = dayEntryView.getDateControl();
        if (dayView != null) {
            DraggedEntry draggedEntry = new DraggedEntry(dayEntryView.getEntry(), dragMode);
            draggedEntry.setOffsetDuration(offsetDuration);
            dayView.setDraggedEntry(draggedEntry);
        }
    }


    private void mouseReleased(MouseEvent evt) {
        if (!evt.getButton().equals(MouseButton.PRIMARY) || dayEntryView == null || dragMode == null || !dragging) {
            return;
        }
        dragging = false;

        Calendar calendar = entry.getCalendar();
        if (calendar.isReadOnly()) {
            return;
        }

        dayEntryView.getProperties().put("dragged", false); 
        dayEntryView.getProperties().put("dragged-start", false); 
        dayEntryView.getProperties().put("dragged-end", false); 

        /*
         * We might run in the sampler application. Then the entry view will not
         * be inside a date control.
         */
        DraggedEntry draggedEntry = dayView.getDraggedEntry();

        if (draggedEntry != null) {
            entry.setInterval(draggedEntry.getInterval());
            dayView.setDraggedEntry(null);
        }
    }

    private void mouseDragged(MouseEvent evt) {
        if (!evt.getButton().equals(MouseButton.PRIMARY) || dayEntryView == null || dragMode == null || !dragging) {
            return;
        }

        Calendar calendar = entry.getCalendar();
        if (calendar.isReadOnly()) {
            return;
        }

        if (dayView.getDraggedEntry() == null || dayEntryView.getParent() == null) {
            // we might see "mouse dragged" events close before "mouse pressed". in this case, our drag/dro handling
            // has not been fully initialized yet.
            return;
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

    private void changeStartTime(MouseEvent evt) {
        LocalDateTime locationTime = dayView.getZonedDateTimeAt(evt.getX(), evt.getY()).toLocalDateTime();
        LocalDateTime time = grid(locationTime);

        if (evt.getX() > dayView.getWidth() || evt.getX() < 0) {
            time = LocalDateTime.of(entry.getStartDate(), time.toLocalTime());
        }

        LOGGER.finer("changing start time, time = " + time); 

        DraggedEntry draggedEntry = dayView.getDraggedEntry();

        if (isMinimumDuration(entry, entry.getEndAsLocalDateTime(), locationTime)) {

            LocalDate startDate;
            LocalDate endDate;

            LocalTime startTime;
            LocalTime endTime;

            if (time.isAfter(entry.getEndAsLocalDateTime())) {
                startTime = entry.getEndTime();
                startDate = entry.getEndDate();
                endTime = time.toLocalTime();
                endDate = time.toLocalDate();
            } else {
                startDate = time.toLocalDate();
                startTime = time.toLocalTime();
                endTime = entry.getEndTime();
                endDate = entry.getEndDate();
            }

            LOGGER.finer("new interval: sd = " + startDate + ", st = " + startTime + ", ed = " + endDate + ", et = " + endTime);

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);

            requestLayout();
        }
    }

    private void changeEndTime(MouseEvent evt) {
        LocalDateTime locationTime = dayView.getZonedDateTimeAt(evt.getX(), evt.getY()).toLocalDateTime();
        LocalDateTime time = grid(locationTime);

        DraggedEntry draggedEntry = dayView.getDraggedEntry();

        if (evt.getX() > dayView.getWidth() || evt.getX() < 0) {
            time = LocalDateTime.of(entry.getEndDate(), time.toLocalTime());
        }

        LOGGER.finer("changing end time, time = " + time); 

        if (isMinimumDuration(entry, entry.getStartAsLocalDateTime(),
                locationTime)) {

            LOGGER.finer("dragged entry: " + draggedEntry.getInterval());

            LocalDate startDate;
            LocalDate endDate;

            LocalTime startTime;
            LocalTime endTime;

            if (time.isBefore(entry.getStartAsLocalDateTime())) {
                endTime = entry.getStartTime();
                endDate = entry.getStartDate();
                startTime = time.toLocalTime();
                startDate = time.toLocalDate();
            } else {
                startTime = entry.getStartTime();
                startDate = entry.getStartDate();
                endTime = time.toLocalTime();
                endDate = time.toLocalDate();
            }

            LOGGER.finer("new interval: sd = " + startDate + ", st = "
                    + startTime + ", ed = " + endDate + ", et = " + endTime);

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);

            requestLayout();
        }
    }

    private void changeStartAndEndTime(MouseEvent evt) {
        DraggedEntry draggedEntry = dayView.getDraggedEntry();
        LocalDateTime locationTime = dayView.getZonedDateTimeAt(evt.getX(), evt.getY()).toLocalDateTime();

        LOGGER.fine("changing start/end time, time = " + locationTime + " offset duration = " + offsetDuration);

        if (locationTime != null && offsetDuration != null) {

            LocalDateTime newStartTime = locationTime.minus(offsetDuration);
            newStartTime = grid(newStartTime);
            LocalDateTime newEndTime = newStartTime.plus(entryDuration);

            LOGGER.fine("new start time = " + newStartTime); 
            LOGGER.fine("new start time (grid) = " + newStartTime); 
            LOGGER.fine("new end time = " + newEndTime); 

            LocalDate startDate = newStartTime.toLocalDate();
            LocalTime startTime = newStartTime.toLocalTime();

            LocalDate endDate = LocalDateTime.of(startDate, startTime).plus(entryDuration).toLocalDate();
            LocalTime endTime = newEndTime.toLocalTime();

            LOGGER.finer("new interval: sd = " + startDate + ", st = " + startTime + ", ed = " + endDate + ", et = " + endTime);

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);

            requestLayout();
        }
    }

    private boolean isMinimumDuration(Entry<?> entry, LocalDateTime timeA,
                                      LocalDateTime timeB) {
        Duration minDuration = entry.getMinimumDuration().abs();
        if (minDuration != null) {
            Duration duration = Duration.between(timeA, timeB).abs();
            return !duration.minus(minDuration).isNegative();
        }

        return true;
    }

    private void requestLayout() {
        dayView.requestLayout();
        dayEntryView.getParent().requestLayout();

        if (dayView instanceof WeekView) {
            ((WeekView) dayView).getWeekDayViews().forEach(Parent::requestLayout);
        }
    }

    private LocalDateTime grid(LocalDateTime time) {
        DayOfWeek firstDayOfWeek = dayView.getFirstDayOfWeek();
        VirtualGrid grid = dayView.getVirtualGrid();
        LocalDateTime lowerTime = grid.adjustTime(time, false, firstDayOfWeek);
        LocalDateTime upperTime = grid.adjustTime(time, true, firstDayOfWeek);
        if (Duration.between(time, upperTime).abs().minus(Duration.between(time, lowerTime).abs()).isNegative()) {
            return upperTime;
        }

        return lowerTime;
    }
}
