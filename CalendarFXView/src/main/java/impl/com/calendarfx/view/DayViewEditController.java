/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.*;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.time.*;
import java.util.Objects;
import java.util.logging.Logger;

public class DayViewEditController {

    private static final Logger LOGGER = LoggingDomain.EDITING;

    private boolean dragging;
    private DayViewBase dayView;
    private DayEntryView dayEntryView;
    private Entry<?> entry;
    private DraggedEntry.DragMode dragMode;
    private Handle handle;
    private Duration offsetDuration;
    private Duration entryDuration;

    public DayViewEditController(DayViewBase dayView) {
        this.dayView = Objects.requireNonNull(dayView);

        dayView.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        dayView.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        dayView.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
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
            dragMode = DraggedEntry.DragMode.END_TIME;
            handle = Handle.BOTTOM;
        } else if (y < 5) {
            dragMode = DraggedEntry.DragMode.START_TIME;
            handle = Handle.TOP;
        } else {
            dragMode = DraggedEntry.DragMode.START_AND_END_TIME;
            handle = Handle.CENTER;
        }
    }

    private void mouseMoved(MouseEvent evt) {
        if (!dragging) {
            initDragModeAndHandle(evt);
        }

        if (handle == null) {
            return;
        }

        switch (handle) {
            case TOP:
                dayEntryView.setCursor(Cursor.N_RESIZE);
                break;
            case BOTTOM:
                dayEntryView.setCursor(Cursor.S_RESIZE);
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

        initDragModeAndHandle(evt);

        LOGGER.finer("drag mode: " + dragMode);
        LOGGER.finer("handle: " + handle);

        if (dragMode == null) {
            return;
        }
        dragging = true;

        switch (dragMode) {
            case START_AND_END_TIME:
                dayEntryView.getProperties().put("dragged", true); //$NON-NLS-1$

                LocalDateTime time = dayView.getZonedDateTimeAt(evt.getX(), evt.getY()).toLocalDateTime();
                offsetDuration = Duration.between(entry.getStartAsLocalDateTime(), time);
                entryDuration = entry.getDuration();

                LOGGER.finer("time at mouse pressed location: " + time);
                LOGGER.finer("offset duration: " + offsetDuration);
                LOGGER.finer("entry duration: " + entryDuration);

                dayView.requestLayout();
                break;
            case END_TIME:
                dayEntryView.getProperties().put("dragged-end", true); //$NON-NLS-1$
                break;
            case START_TIME:
                dayEntryView.getProperties().put("dragged-start", true); //$NON-NLS-1$
                break;
            default:
                break;

        }

        DayView dayView = dayEntryView.getDateControl();
        if (dayView != null) {
            DraggedEntry draggedEntry = new DraggedEntry(dayEntryView.getEntry(), dragMode);
            draggedEntry.setOffsetDuration(offsetDuration);
            dayView.setDraggedEntry(draggedEntry);
        }
    }


    private void mouseReleased(MouseEvent evt) {
        dragging = false;
        if (!evt.getButton().equals(MouseButton.PRIMARY) || dayEntryView == null || dragMode == null) {
            return;
        }

        Calendar calendar = entry.getCalendar();
        if (calendar.isReadOnly()) {
            return;
        }

        dayEntryView.getProperties().put("dragged", false); //$NON-NLS-1$
        dayEntryView.getProperties().put("dragged-start", false); //$NON-NLS-1$
        dayEntryView.getProperties().put("dragged-end", false); //$NON-NLS-1$

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
        if (!evt.getButton().equals(MouseButton.PRIMARY) || dayEntryView == null || dragMode == null) {
            return;
        }

        Calendar calendar = entry.getCalendar();
        if (calendar.isReadOnly()) {
            return;
        }

        if (dayView.getDraggedEntry() == null) {
            // we might see "mouse dragged" events close before "mouse pressed". in this case, our drag/dro handling
            // has not been fully initialized yet.
            return;
        }

        switch (dragMode) {
            case START_TIME:
                switch (handle) {
                    case TOP:
                        changeStartTime(evt);
                        break;
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
                        changeEndTime(evt);
                        break;
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

        LOGGER.finer("changing start time, time = " + time); //$NON-NLS-1$

        DraggedEntry draggedEntry = dayView.getDraggedEntry();

        if (isMinimumDuration(entry, entry.getEndAsLocalDateTime(), locationTime)) {

            Interval interval = draggedEntry.getInterval();

            LocalDate startDate = interval.getStartDate();
            LocalDate endDate = interval.getEndDate();

            LocalTime startTime;
            LocalTime endTime;

            if (locationTime.isAfter(entry.getEndAsLocalDateTime())) {
                startTime = entry.getEndTime();
                endTime = time.toLocalTime();
                endDate = time.toLocalDate();
            } else {
                startDate = time.toLocalDate();
                startTime = time.toLocalTime();
                endTime = entry.getEndTime();
            }

            LOGGER.finer("new interval: sd = " + startDate + ", st = " + startTime + ", ed = " + endDate + ", et = " + endTime);

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);

            requestLayout();
        }
    }

    private void changeEndTime(MouseEvent evt) {
        LocalDateTime locationTime = dayView.getZonedDateTimeAt(evt.getX(), evt.getY()).toLocalDateTime();
        LocalDateTime time = grid(locationTime);

        LOGGER.finer("changing end time, time = " + time); //$NON-NLS-1$

        DraggedEntry draggedEntry = dayView.getDraggedEntry();

        if (isMinimumDuration(entry, entry.getStartAsLocalDateTime(), locationTime)) {

            Interval interval = draggedEntry.getInterval();

            LOGGER.finer("dragged entry: " + draggedEntry.getInterval());

            LocalDate startDate = interval.getStartDate();
            LocalDate endDate = interval.getEndDate();

            LocalTime startTime;
            LocalTime endTime;

            if (locationTime.isBefore(entry.getStartAsLocalDateTime())) {
                endTime = entry.getStartTime();
                startTime = time.toLocalTime();
                startDate = time.toLocalDate();
            } else {
                startTime = entry.getStartTime();
                endTime = time.toLocalTime();
                endDate = time.toLocalDate();
            }

            LOGGER.finer("new interval: sd = " + startDate + ", st = " + startTime + ", ed = " + endDate + ", et = " + endTime);

            draggedEntry.setInterval(startDate, startTime, endDate, endTime);

            requestLayout();
        }
    }

    private void changeStartAndEndTime(MouseEvent evt) {
        DraggedEntry draggedEntry = dayView.getDraggedEntry();
        LocalDateTime locationTime = dayView.getZonedDateTimeAt(evt.getX(), evt.getY()).toLocalDateTime();

        LOGGER.fine("changing start/end time, time = " + locationTime //$NON-NLS-1$
                + " offset duration = " + offsetDuration); //$NON-NLS-1$

        if (locationTime != null && offsetDuration != null) {

            LocalDateTime newStartTime = locationTime.minus(offsetDuration);
            newStartTime = grid(newStartTime);
            LocalDateTime newEndTime = newStartTime.plus(entryDuration);

            LOGGER.fine("new start time = " + newStartTime); //$NON-NLS-1$
            LOGGER.fine("new start time (grid) = " + newStartTime); //$NON-NLS-1$
            LOGGER.fine("new end time = " + newEndTime); //$NON-NLS-1$

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
            if (duration.minus(minDuration).isNegative()) {
                return false;
            }
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
