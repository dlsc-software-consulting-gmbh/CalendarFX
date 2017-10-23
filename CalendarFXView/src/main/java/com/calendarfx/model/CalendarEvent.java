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

package com.calendarfx.model;

import javafx.event.Event;
import javafx.event.EventType;

import java.time.ZonedDateTime;

import static java.util.Objects.requireNonNull;

/**
 * An event class used to signal changes done within a calendar or changes done
 * to a calendar entry. Events of this type can be received by adding an event
 * handler to a calendar.
 * <p/>
 * <p>
 * <h2>Example</h2>
 * <p>
 * <pre>
 * {@code
 * Calendar calendar = new Calendar("Home");
 * calendar.addEventHandler(CalendarEvent.ENTRY_ADDED, evt -> {...});
 * }
 * </pre>
 *
 * @see Calendar#addEventHandler(javafx.event.EventHandler)
 */
public class CalendarEvent extends Event {

    private static final long serialVersionUID = 4279597664476680474L;


    /**
     * The supertype of all event types in this event class.
     */
    public static final EventType<CalendarEvent> ANY = new EventType<>(
            Event.ANY, "ANY"); //$NON-NLS-1$

    /**
     * An event type used to inform the application that "something" inside the
     * calendar has changed and that the views need to update their visuals
     * accordingly (brute force update).
     */
    public static final EventType<CalendarEvent> CALENDAR_CHANGED = new EventType<>(
            CalendarEvent.ANY, "CALENDAR_CHANGED"); //$NON-NLS-1$

    /**
     * The supertype of all events that a related to an entry itself and not the
     * calendar.
     */
    public static final EventType<CalendarEvent> ENTRY_CHANGED = new EventType<>(
            CalendarEvent.ANY, "ENTRY_CHANGED"); //$NON-NLS-1$

    /**
     * An event type used to inform the application that an entry has been moved
     * from one calendar to another.
     */
    public static final EventType<CalendarEvent> ENTRY_CALENDAR_CHANGED = new EventType<>(
            CalendarEvent.ENTRY_CHANGED, "ENTRY_CALENDAR_CHANGED"); //$NON-NLS-1$

    /**
     * An event type used to inform the application that an entry has become a
     * "full day" entry, meaning its start and end time are no longer relevant.
     * The entry should be visualized in a way that signals that the entry will
     * take all day (e.g. a birthday).
     */
    public static final EventType<CalendarEvent> ENTRY_FULL_DAY_CHANGED = new EventType<>(
            CalendarEvent.ENTRY_CHANGED, "ENTRY_FULL_DAY_CHANGED"); //$NON-NLS-1$

    /**
     * An event type used to inform the application that an entry has been
     * assigned a new user object.
     */
    public static final EventType<CalendarEvent> ENTRY_RECURRENCE_RULE_CHANGED = new EventType<>(
            CalendarEvent.ENTRY_CHANGED, "ENTRY_RECURRENCE_RULE_CHANGED"); //$NON-NLS-1$

    /**
     * An event type used to inform the application that an entry has been
     * assigned a new title.
     */
    public static final EventType<CalendarEvent> ENTRY_TITLE_CHANGED = new EventType<>(
            CalendarEvent.ENTRY_CHANGED, "ENTRY_TITLE_CHANGED"); //$NON-NLS-1$

    /**
     * An event type used to inform the application that an entry has been
     * assigned a new user object.
     */
    public static final EventType<CalendarEvent> ENTRY_USER_OBJECT_CHANGED = new EventType<>(
            CalendarEvent.ENTRY_CHANGED, "ENTRY_USER_OBJECT_CHANGED"); //$NON-NLS-1$

    /**
     * An event type used to inform the application that an entry has been
     * assigned a new user object.
     */
    public static final EventType<CalendarEvent> ENTRY_LOCATION_CHANGED = new EventType<>(
            CalendarEvent.ENTRY_CHANGED, "ENTRY_LOCATION_CHANGED"); //$NON-NLS-1$

    /**
     * An event type used to inform the application that the time bounds of an
     * entry have been changed. One or several of start / end date, start / end
     * time.
     */
    public static final EventType<CalendarEvent> ENTRY_INTERVAL_CHANGED = new EventType<>(
            CalendarEvent.ENTRY_CHANGED, "ENTRY_INTERVAL_CHANGED"); //$NON-NLS-1$

    private Entry<?> entry;

    private Calendar calendar;

    private boolean oldFullDay;

    private String oldText;

    private Calendar oldCalendar;

    private Interval oldInterval;

    private Object oldUserObject;

    /**
     * Constructs a new event for subclass.
     *
     * @param eventType the event type
     * @param calendar  the calendar where the event occurred.
     */
    protected CalendarEvent(EventType<? extends CalendarEvent> eventType,
                            Calendar calendar) {
        super(calendar, calendar, eventType);

        this.calendar = requireNonNull(calendar);
    }

    /**
     * Constructs a new event.
     *
     * @param eventType the event type
     * @param calendar  the calendar where the event occured
     * @param entry     the affected entry
     */
    public CalendarEvent(EventType<? extends CalendarEvent> eventType,
                         Calendar calendar, Entry<?> entry) {
        super(calendar, calendar, eventType);

        this.calendar = calendar;
        this.entry = requireNonNull(entry);
    }

    /**
     * Constructs a new event used for signalling that an entry was assigned to
     * a new calendar. The entry already carries a reference to new calendar and
     * the event object will know the old calendar.
     *
     * @param eventType   the event type
     * @param calendar    the calendar where the event occured
     * @param entry       the affected entry
     * @param oldCalendar the calendar to which the event belonged before
     */
    public CalendarEvent(EventType<CalendarEvent> eventType, Calendar calendar,
                         Entry<?> entry, Calendar oldCalendar) {
        this(eventType, calendar, entry);
        this.oldCalendar = oldCalendar;
    }

    /**
     * Constructs a new event used for signalling that an entry has been
     * assigned a new user object. The entry already carries a reference to the
     * new user object and the event object will know the old user object.
     *
     * @param eventType     the event type
     * @param calendar      the calendar where the event occured
     * @param entry         the affected entry
     * @param oldUserObject the calendar to which the event belonged before
     */
    public CalendarEvent(EventType<CalendarEvent> eventType, Calendar calendar,
                         Entry<?> entry, Object oldUserObject) {
        this(eventType, calendar, entry);
        this.oldUserObject = oldUserObject;
    }

    /**
     * Constructs a new event used for signalling that an entry was assigned a
     * new start end date / time. The entry already carries the new values,
     * while the old values can be retrieved from the event object.
     *
     * @param eventType   the event type
     * @param calendar    the calendar where the event occured
     * @param entry       the affected entry
     * @param oldInterval the previous time interval
     */
    public CalendarEvent(EventType<CalendarEvent> eventType, Calendar calendar,
                         Entry<?> entry, Interval oldInterval) {
        this(eventType, calendar, entry);
        this.oldInterval = requireNonNull(oldInterval);
    }

    /**
     * Constructs a new event used for signalling that an entry was assigned a
     * new text (normally the title). The entry already carries a reference to
     * new text and the event object will know the old one.
     *
     * @param eventType the event type
     * @param calendar  the calendar where the event occured
     * @param entry     the affected entry
     * @param oldText   the previous value of the text
     */
    public CalendarEvent(EventType<CalendarEvent> eventType, Calendar calendar,
                         Entry<?> entry, String oldText) {
        this(eventType, calendar, entry);
        this.oldText = oldText;
    }

    /**
     * Constructs a new event used for signaling that an entry was set to full
     * day. The entry already carries a reference to new full day value and the
     * event object will know the old one.
     *
     * @param eventType  the event type
     * @param calendar   the calendar where the event occured
     * @param entry      the affected entry
     * @param oldFullDay the previous value of the full day
     */
    public CalendarEvent(EventType<CalendarEvent> eventType, Calendar calendar,
                         Entry<?> entry, boolean oldFullDay) {
        this(eventType, calendar, entry);
        this.oldFullDay = oldFullDay;
    }

    /**
     * Returns the entry for which the event was fired.
     *
     * @return the affected entry
     */
    public Entry<?> getEntry() {
        return entry;
    }

    /**
     * Returns the calendar for which the event was fired.
     *
     * @return the affected calendar
     */
    public final Calendar getCalendar() {
        return calendar;
    }

    /**
     * Returns the old user object of the modified entry.
     *
     * @return the old user object
     */
    public Object getOldUserObject() {
        return oldUserObject;
    }

    /**
     * Returns the old time interval of the modified entry.
     *
     * @return the old time interval
     */
    public Interval getOldInterval() {
        return oldInterval;
    }

    /**
     * Returns the old text.
     *
     * @return the old text
     */
    public final String getOldText() {
        return oldText;
    }

    /**
     * Returns the old value of the "full day" flag.
     *
     * @return the old value of "full day"
     */
    public final boolean getOldFullDay() {
        return oldFullDay;
    }

    /**
     * Returns the old calendar.
     *
     * @return the old calendar
     */
    public final Calendar getOldCalendar() {
        return oldCalendar;
    }

    /**
     * A utility method to determine if the event describes the creation of a new
     * entry. This is the case when the event type is {@link #ENTRY_CALENDAR_CHANGED} and
     * the old calendar was null.
     *
     * @return true if the event describes the creation of a new entry
     */
    public final boolean isEntryAdded() {
        if (eventType == ENTRY_CALENDAR_CHANGED) {
            return (getOldCalendar() == null && entry.getCalendar() != null);
        }

        return false;
    }

    /**
     * A utility method to determine if the event describes the removal of an
     * entry. This is the case when the event type is {@link #ENTRY_CALENDAR_CHANGED} and
     * the old calendar was not null but the new calendar is null.
     *
     * @return true if the event describes the removal of a new entry
     */
    public final boolean isEntryRemoved() {
        if (eventType == ENTRY_CALENDAR_CHANGED) {
            return (getOldCalendar() != null && entry.getCalendar() == null);
        }

        return false;
    }

    /**
     * Determines whether the event will have an impact on different days.
     * The method will return false if the user simply changed the start and / or
     * end time of an entry within the current day. However, when the user drags
     * the entry from one day to another then this method will return true.
     *
     * @return true if the new time interval of the entry touches other days than the old time interval of the entry
     */
    public final boolean isDayChange() {
        if (!getEventType().equals(ENTRY_INTERVAL_CHANGED)) {
            return false;
        }

        Interval newInterval = entry.getInterval();
        Interval oldInterval = getOldInterval();

        ZonedDateTime newStart = newInterval.getStartZonedDateTime();
        ZonedDateTime oldStart = oldInterval.getStartZonedDateTime();
        if (!newStart.toLocalDate().equals(oldStart.toLocalDate())) {
            return true;
        }

        ZonedDateTime newEnd = newInterval.getEndZonedDateTime();
        ZonedDateTime oldEnd = oldInterval.getEndZonedDateTime();
        return !newEnd.toLocalDate().equals(oldEnd.toLocalDate());

    }

    @Override
    public String toString() {
        return "CalendarEvent [" //$NON-NLS-1$
                + (entry == null ? "" : ("entry=" + entry + ", ")) + "calendar=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + calendar + ", oldInterval=" + oldInterval + ", oldFullDay=" //$NON-NLS-1$ //$NON-NLS-2$
                + oldFullDay + ", oldText=" + oldText + ", oldCalendar=" //$NON-NLS-1$ //$NON-NLS-2$
                + oldCalendar + ", eventType=" + eventType + ", target=" //$NON-NLS-1$ //$NON-NLS-2$
                + target + ", consumed=" + consumed + ", source=" + source //$NON-NLS-1$ //$NON-NLS-2$
                + "]"; //$NON-NLS-1$
    }
}
