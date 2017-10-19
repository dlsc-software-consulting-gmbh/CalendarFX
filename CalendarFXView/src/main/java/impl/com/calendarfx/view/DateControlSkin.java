/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.util.CalendarFX;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DraggedEntry;
import impl.com.calendarfx.view.util.Util;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.MapChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.SkinBase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;

import static com.calendarfx.model.CalendarEvent.CALENDAR_CHANGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

public abstract class DateControlSkin<C extends DateControl> extends SkinBase<C> {

    public DateControlSkin(C control) {
        super(control);

        control.addEventHandler(MOUSE_PRESSED, evt -> control.clearSelection());

        control.getCalendars().addListener(this::calendarListChanged);

        for (Calendar calendar : control.getCalendars()) {
            calendar.addEventHandler(calendarListener);
        }

        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("refresh.data")) { //$NON-NLS-1$
                    LoggingDomain.VIEW.fine("data refresh was requested by the application"); //$NON-NLS-1$
                    control.getProperties().remove("refresh.data"); //$NON-NLS-1$
                    refreshData();
                }
            }
        };

        control.getProperties().addListener(propertiesListener);

        InvalidationListener calendarVisibilityListener = it -> calendarVisibilityChanged();

        for (Calendar calendar : control.getCalendars()) {
            control.getCalendarVisibilityProperty(calendar).addListener(calendarVisibilityListener);
        }

        control.getCalendars().addListener((Change<? extends Calendar> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Calendar calendar : change.getAddedSubList()) {
                        control.getCalendarVisibilityProperty(calendar).addListener(calendarVisibilityListener);
                    }
                } else if (change.wasRemoved()) {
                    for (Calendar calendar : change.getRemoved()) {
                        control.getCalendarVisibilityProperty(calendar).removeListener(calendarVisibilityListener);
                    }
                }
            }
        });

        nagging();
    }

    protected void refreshData() {
    }

    private InvalidationListener calendarVisibilityChanged = it -> calendarVisibilityChanged();

    protected void calendarVisibilityChanged() {
    }

    private EventHandler<CalendarEvent> calendarListener = this::calendarChanged;

    private void calendarListChanged(Change<? extends Calendar> change) {
        C dateControl = getSkinnable();
        while (change.next()) {
            if (change.wasAdded()) {
                for (Calendar calendar : change.getAddedSubList()) {
                    calendar.addEventHandler(calendarListener);
                    dateControl.getCalendarVisibilityProperty(calendar).addListener(calendarVisibilityChanged);
                }
            } else if (change.wasRemoved()) {
                for (Calendar calendar : change.getRemoved()) {
                    calendar.removeEventHandler(calendarListener);
                    dateControl.getCalendarVisibilityProperty(calendar).removeListener(calendarVisibilityChanged);
                }
            }
        }
    }

    private void calendarChanged(CalendarEvent evt) {

        if (LoggingDomain.EVENTS.isLoggable(Level.FINE) && !(evt.getEntry() instanceof DraggedEntry)) {
            LoggingDomain.EVENTS.fine("calendar event in " + getSkinnable().getClass().getSimpleName() + ": " + evt.getEventType() + ", details: " + evt.toString());
        }

        if (getSkinnable().isSuspendUpdates()) {
            return;
        }

        if (evt.getEventType().getSuperType().equals(CalendarEvent.ENTRY_CHANGED) && evt.getEntry().isRecurrence()) {
            return;
        }

        Util.runInFXThread(() -> {
            EventType<? extends Event> eventType = evt.getEventType();
            if (eventType.equals(CalendarEvent.ENTRY_INTERVAL_CHANGED)) {
                entryIntervalChanged(evt);
            } else if (eventType.equals(CalendarEvent.ENTRY_FULL_DAY_CHANGED)) {
                entryFullDayChanged(evt);
            } else if (eventType.equals(CalendarEvent.ENTRY_RECURRENCE_RULE_CHANGED)) {
                entryRecurrenceRuleChanged(evt);
            } else if (eventType.equals(CalendarEvent.ENTRY_CALENDAR_CHANGED)) {
                entryCalendarChanged(evt);
            } else if (eventType.equals(CALENDAR_CHANGED)) {
                calendarChanged(evt.getCalendar());
            }
        });
    }

    protected void entryIntervalChanged(CalendarEvent evt) {
    }

    protected void entryFullDayChanged(CalendarEvent evt) {
    }

    protected void entryRecurrenceRuleChanged(CalendarEvent evt) {
    }

    protected void entryCalendarChanged(CalendarEvent evt) {
    }

    protected void calendarChanged(Calendar calendar) {
    }

    protected boolean isRelevant(Entry<?> entry) {

        if (this instanceof LoadDataSettingsProvider) {
            C dateControl = getSkinnable();

            if (!(entry instanceof DraggedEntry) && !dateControl.isCalendarVisible(entry.getCalendar())) {
                return false;
            }

            LoadDataSettingsProvider provider = (LoadDataSettingsProvider) this;

            ZoneId zoneId = getSkinnable().getZoneId();
            LocalDate loadStartDate = provider.getLoadStartDate();
            LocalDate loadEndDate = provider.getLoadEndDate();

            return entry.isShowing(loadStartDate, loadEndDate, zoneId);
        }

        return false;
    }

    protected boolean isRelevant(Interval interval) {
        LoadDataSettingsProvider provider = (LoadDataSettingsProvider) this;
        ZoneId zoneId = getSkinnable().getZoneId();
        LocalDate loadStartDate = provider.getLoadStartDate();
        LocalDate loadEndDate = provider.getLoadEndDate();
        ZonedDateTime st = ZonedDateTime.of(loadStartDate, LocalTime.MIN, zoneId);
        ZonedDateTime et = ZonedDateTime.of(loadEndDate, LocalTime.MAX, zoneId);
        return Util.intersect(interval.getStartZonedDateTime(), interval.getEndZonedDateTime(), st, et);
    }

    private synchronized void nagging() {
        System.out.println("CalendarFX user interface framework for Java, (Version " + CalendarFX.getVersion() + ")"); //$NON-NLS-1$
        System.out.println("(c) 2014 - 2017 DLSC Software & Consulting"); //$NON-NLS-1$
        System.out.println("http://www.dlsc.com"); //$NON-NLS-1$
    }
}
