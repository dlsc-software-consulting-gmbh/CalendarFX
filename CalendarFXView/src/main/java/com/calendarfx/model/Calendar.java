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

import com.calendarfx.view.DateControl;
import com.google.ical.compat.javatime.LocalDateIterator;
import com.google.ical.compat.javatime.LocalDateIteratorFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.calendarfx.model.CalendarEvent.CALENDAR_CHANGED;
import static com.calendarfx.model.CalendarEvent.ENTRY_CHANGED;
import static com.calendarfx.util.LoggingDomain.MODEL;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;

/**
 * A calendar is responsible for storing calendar entries. It provides methods
 * for adding, removing, and querying entries. A calendar also defines a visual
 * style / color theme that will be used throughout the UI controls. Calendars
 * fire events whenever entries are added or removed. Calendars are grouped together
 * inside a {@link CalendarSource}. These calendar sources are then added to
 * {@link DateControl#getCalendarSources()}.
 *
 * <h2>Example</h2>
 * <pre>
 *     {@code
 *     // create the calendar and listen to all changes
 *     Calendar calendar = new Calendar("Home");
 *     calendar.addEventHandler(CalendarEvent.ANY, evt -> handleEvent(evt));
 *
 *     // create the calendar source and attach the calendar
 *     CalendarSource source = new CalendarSource("Online Calendars");
 *     source.getCalendars().add(calendar);
 *
 *     // attach the source to the date control / calendar view.
 *     CalendarView view = new CalendarView();
 *     view.getCalendarSources().add(source);
 *     }
 * </pre>
 */
public class Calendar implements EventTarget {

    /**
     * Predefined visual styles for calendars. The actual CSS settings for these
     * styles can be found in the framework stylesheet, prefixed with "style1-",
     * "style2-", etc. The picture below shows the colors used for the various
     * styles.
     * <p>
     * <center><img src="doc-files/styles.png"></center>
     *
     * @see Calendar#setStyle(Style)
     */
    public enum Style {

        /**
         * Default style "1".
         */
        STYLE1,

        /**
         * Default style "2".
         */
        STYLE2,

        /**
         * Default style "3".
         */
        STYLE3,

        /**
         * Default style "4".
         */
        STYLE4,

        /**
         * Default style "5".
         */
        STYLE5,

        /**
         * Default style "6".
         */
        STYLE6,

        /**
         * Default style "7".
         */
        STYLE7;

        /**
         * Returns a style for the given ordinal. This method is implemented
         * with a roll over strategy: the final ordinal value is the given
         * ordinal value modulo the number of elements in this enum.
         *
         * @param ordinal the ordinal value for which to return a style
         * @return a style, guaranteed to be non null
         */
        public static Style getStyle(int ordinal) {
            return Style.values()[ordinal % Style.values().length];
        }
    }

    private IntervalTree<Entry<?>> intervalTree = new IntervalTree<>();

    /**
     * Constructs a new calendar.
     */
    public Calendar() {
        addEventHandler(evt -> {
            Entry<?> entry = evt.getEntry();
            if (evt.getEventType().getSuperType().equals(ENTRY_CHANGED) && entry.isRecurrence()) {
                updateRecurrenceSourceEntry(evt, entry.getRecurrenceSourceEntry());
            }
        });
    }

    /**
     * Constructs a new calendar with the given name.
     *
     * @param name the name of the calendar
     */
    public Calendar(String name) {
        this();

        setName(name);

        if (name != null) {
            setShortName(name.substring(0, 1));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void updateRecurrenceSourceEntry(CalendarEvent evt, Entry source) {
        Entry recurrence = evt.getEntry();
        if (evt.getEventType().equals(CalendarEvent.ENTRY_INTERVAL_CHANGED)) {
            Interval oldInterval = evt.getOldInterval();
            Interval newInterval = calculateSourceBoundsFromRecurrenceBounds(source, recurrence, oldInterval);
            source.setInterval(newInterval);
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_LOCATION_CHANGED)) {
            source.setLocation(recurrence.getLocation());
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_RECURRENCE_RULE_CHANGED)) {
            source.setRecurrenceRule(recurrence.getRecurrenceRule());
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_TITLE_CHANGED)) {
            source.setTitle(recurrence.getTitle());
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_USER_OBJECT_CHANGED)) {
            source.setUserObject(recurrence.getUserObject());
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED)) {
            source.setCalendar(recurrence.getCalendar());
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_FULL_DAY_CHANGED)) {
            source.setFullDay(recurrence.isFullDay());
        }
    }

    private Interval calculateSourceBoundsFromRecurrenceBounds(Entry<?> source, Entry<?> recurrence, Interval oldInterval) {
        ZonedDateTime recurrenceStart = recurrence.getStartAsZonedDateTime();
        ZonedDateTime recurrenceEnd = recurrence.getEndAsZonedDateTime();

        Duration startDelta = Duration.between(oldInterval.getStartZonedDateTime(), recurrenceStart);
        Duration endDelta = Duration.between(oldInterval.getEndZonedDateTime(), recurrenceEnd);

        ZonedDateTime sourceStart = source.getStartAsZonedDateTime();
        ZonedDateTime sourceEnd = source.getEndAsZonedDateTime();

        sourceStart = sourceStart.plus(startDelta);
        sourceEnd = sourceEnd.plus(endDelta);

        return new Interval(sourceStart.toLocalDate(), sourceStart.toLocalTime(), sourceEnd.toLocalDate(), sourceEnd.toLocalTime(), source.getZoneId());
    }

    /**
     * Gets the earliest time used by this calendar, that means the start of the
     * first entry stored.
     *
     * @return An instant representing the earliest time, can be null if no
     * entries are contained.
     */
    public final Instant getEarliestTimeUsed() {
        return intervalTree.getEarliestTimeUsed();
    }

    /**
     * Gets the latest time used by this calendar, that means the end of the
     * last entry stored.
     *
     * @return An instant representing the latest time, can be null if no
     * entries are contained.
     */
    public final Instant getLatestTimeUsed() {
        return intervalTree.getLatestTimeUsed();
    }

    private boolean batchUpdates;

    private boolean dirty;

    /**
     * Tells the calendar that the application will perform a large number of changes.
     * While batch updates in progress the calendar will stop to fire events. To finish
     * this mode the application has to call {@link #stopBatchUpdates()}.
     */
    public final void startBatchUpdates() {
        batchUpdates = true;
        dirty = false;
    }

    /**
     * Tells the calendar that the application is done making big changes. Invoking
     * this method will trigger a calendar event of type {@link CalendarEvent#CALENDAR_CHANGED} which
     * will then force an update of the views.
     */
    public final void stopBatchUpdates() {
        batchUpdates = false;

        if (dirty) {
            dirty = false;
            fireEvent(new CalendarEvent(CalendarEvent.CALENDAR_CHANGED, this));
        }
    }

    /**
     * Queries the calendar for all entries within the time interval defined by
     * the start date and end date.
     *
     * @param startDate the start of the time interval
     * @param endDate   the end of the time interval
     * @param zoneId    the time zone for which to find entries
     * @return a map filled with list of entries for given days
     */
    public final Map<LocalDate, List<Entry<?>>> findEntries(LocalDate startDate, LocalDate endDate, ZoneId zoneId) {
        fireEvents = false;

        Map<LocalDate, List<Entry<?>>> result;

        try {
            result = doGetEntries(startDate, endDate, zoneId);
        } finally {
            fireEvents = true;
        }

        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<LocalDate, List<Entry<?>>> doGetEntries(LocalDate startDate, LocalDate endDate, ZoneId zoneId) {
        if (MODEL.isLoggable(FINE)) {
            MODEL.fine(getName() + ": getting entries from " + startDate //$NON-NLS-1$
                    + " until " + endDate + ", zone = " + zoneId); //$NON-NLS-1$ //$NON-NLS-2$
        }

        ZonedDateTime st = ZonedDateTime.of(startDate, LocalTime.MIN, zoneId);
        ZonedDateTime et = ZonedDateTime.of(endDate, LocalTime.MAX, zoneId);

        Collection<Entry<?>> intersectingEntries = intervalTree.getIntersectingObjects(st.toInstant(), et.toInstant());

        if (intersectingEntries.isEmpty()) {
            if (MODEL.isLoggable(FINE)) {
                MODEL.fine(getName() + ": found no entries"); //$NON-NLS-1$
            }
            return Collections.emptyMap();
        }

        if (MODEL.isLoggable(FINE)) {
            MODEL.fine(getName() + ": found " + intersectingEntries.size() //$NON-NLS-1$
                    + " entries"); //$NON-NLS-1$
        }

        Map<LocalDate, List<Entry<?>>> result = new HashMap<>();
        for (Entry<?> entry : intersectingEntries) {

            if (entry.isRecurring()) {

                /*
                 * The recurring entry / entries.
                 */
                String recurrenceRule = entry.getRecurrenceRule();
                if (recurrenceRule != null && !recurrenceRule.trim().equals("")) { //$NON-NLS-1$

                    LocalDate utilStartDate = entry.getStartAsZonedDateTime().toLocalDate();

                    try {
                        LocalDate utilEndDate = et.toLocalDate();

                        LocalDateIterator iterator = LocalDateIteratorFactory.createLocalDateIterator(recurrenceRule, utilStartDate, zoneId, true);

                        /*
                         * TODO: for performance reasons we should definitely
                         * use the advanceTo() call, but unfortunately this
                         * collides with the fact that e.g. the DetailedWeekView loads
                         * data day by day. So a given day would not show
                         * entries that start on the day before but intersect
                         * with the given day. We have to find a solution for
                         * this.
                         */
                        // iterator.advanceTo(org.joda.time.LocalDate.fromDateFields(Date.from(st.toInstant())));

                        while (iterator.hasNext()) {
                            LocalDate repeatingDate = iterator.next();
                            if (repeatingDate.isAfter(utilEndDate)) {
                                break;
                            } else {
                                ZonedDateTime zonedDateTime = ZonedDateTime.of(repeatingDate, LocalTime.MIN, zoneId);

                                Entry recurrence = entry.createRecurrence();
                                recurrence.setId(entry.getId());
                                recurrence.getProperties().put("com.calendarfx.recurrence.source", entry);
                                recurrence.getProperties().put("com.calendarfx.recurrence.id", zonedDateTime.toString());
                                recurrence.setRecurrenceRule(entry.getRecurrenceRule());

                                LocalDate recurrenceStartDate = zonedDateTime.toLocalDate();
                                LocalDate recurrenceEndDate = recurrenceStartDate.plus(entry.getStartDate().until(entry.getEndDate()));

                                Interval recurrenceInterval = entry.getInterval().withDates(recurrenceStartDate, recurrenceEndDate);

                                recurrence.setInterval(recurrenceInterval);
                                recurrence.setUserObject(entry.getUserObject());
                                recurrence.setTitle(entry.getTitle());
                                recurrence.setMinimumDuration(entry.getMinimumDuration());
                                recurrence.setFullDay(entry.isFullDay());
                                recurrence.setLocation(entry.getLocation());
                                recurrence.setCalendar(this);

                                addEntryToResult(result, recurrence, startDate, endDate);
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                addEntryToResult(result, entry, startDate, endDate);
            }
        }

        if (MODEL.isLoggable(FINE)) {
            MODEL.fine(getName() + ": found entries for " + result.size() //$NON-NLS-1$
                    + " different days"); //$NON-NLS-1$
        }

        result.values().forEach(Collections::sort);

        return result;
    }

    /*
     * Assign the given entry to each date that it intersects with in the given search interval.
     */
    private void addEntryToResult(Map<LocalDate, List<Entry<?>>> result, Entry<?> entry, LocalDate startDate, LocalDate endDate) {
        LocalDate entryStartDate = entry.getStartDate();
        LocalDate entryEndDate = entry.getEndDate();

        // entry does not intersect with time interval
        if (entryEndDate.isBefore(startDate) || entryStartDate.isAfter(endDate)) {
            return;
        }

        if (entryStartDate.isAfter(startDate)) {
            startDate = entryStartDate;
        }

        if (entryEndDate.isBefore(endDate)) {
            endDate = entryEndDate;
        }

        LocalDate date = startDate;
        do {
            result.computeIfAbsent(date, it -> new ArrayList<>()).add(entry);
            date = date.plusDays(1);
        } while (!date.isAfter(endDate));
    }

    private final ObjectProperty<Duration> lookAheadDuration = new SimpleObjectProperty<>(this, "lookAheadDuration", Duration.ofDays(730)); //$NON-NLS-1$

    /**
     * Stores a time duration used for the entry search functionality of this
     * calendar. The look ahead and the look back durations limit the search to
     * the time interval [now - lookBackDuration, now + lookAheadDuration]. The
     * default value of this property is 730 days (2 years).
     *
     * @return the look ahead duration
     * @see #findEntries(String)
     */
    public final ObjectProperty<Duration> lookAheadDurationProperty() {
        return lookAheadDuration;
    }

    /**
     * Sets the value of {@link #lookAheadDurationProperty()}.
     *
     * @param duration the look ahead duration
     */
    public final void setLookAheadDuration(Duration duration) {
        requireNonNull(duration);
        lookAheadDurationProperty().set(duration);
    }

    /**
     * Returns the value of {@link #lookAheadDurationProperty()}.
     *
     * @return the look ahead duration
     */
    public final Duration getLookAheadDuration() {
        return lookAheadDurationProperty().get();
    }

    private final ObjectProperty<Duration> lookBackDuration = new SimpleObjectProperty<>(this, "lookBackDuration", Duration.ofDays(730)); //$NON-NLS-1$

    /**
     * Stores a time duration used for the entry search functionality of this
     * calendar. The look ahead and the look back durations limit the search to
     * the time interval [now - lookBackDuration, now + lookAheadDuration]. The
     * default value of this property is 730 days (2 years).
     *
     * @return the look back duration
     * @see #findEntries(String)
     */
    public final ObjectProperty<Duration> lookBackDurationProperty() {
        return lookBackDuration;
    }

    /**
     * Sets the value of {@link #lookBackDurationProperty()}.
     *
     * @param duration the look back duration
     */
    public final void setLookBackDuration(Duration duration) {
        requireNonNull(duration);
        lookBackDurationProperty().set(duration);
    }

    /**
     * Returns the value of {@link #lookBackDurationProperty()}.
     *
     * @return the look back duration
     */
    public final Duration getLookBackDuration() {
        return lookBackDurationProperty().get();
    }

    /**
     * Queries the calendar for entries that match the given search text. The method
     * can be overridden to implement custom find / search strategies.
     *
     * @param searchText the search text
     * @return a list of entries that match the search
     * @see Entry#matches(String)
     */
    public List<Entry<?>> findEntries(String searchText) {
        if (MODEL.isLoggable(FINE)) {
            MODEL.fine(getName() + ": getting entries for search term: " //$NON-NLS-1$
                    + searchText);
        }

        Instant horizonStart = Instant.now().minus(getLookBackDuration());
        Instant horizonEnd = Instant.now().plus(getLookAheadDuration());

        ZoneId zoneId = ZoneId.systemDefault();

        ZonedDateTime st = ZonedDateTime.ofInstant(horizonStart, zoneId);
        ZonedDateTime et = ZonedDateTime.ofInstant(horizonEnd, zoneId);

        List<Entry<?>> result = new ArrayList<>();

        Map<LocalDate, List<Entry<?>>> map = findEntries(st.toLocalDate(), et.toLocalDate(), zoneId);
        for (List<Entry<?>> list : map.values()) {
            for (Entry<?> entry : list) {
                if (entry.matches(searchText)) {
                    result.add(entry);
                }
            }
        }

        if (MODEL.isLoggable(FINE)) {
            MODEL.fine(getName() + ": found " + result.size() + " entries"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    /**
     * Removes all entries from the calendar. Fires an
     * {@link CalendarEvent#CALENDAR_CHANGED} event.
     */
    public final void clear() {
        intervalTree.clear();
        fireEvent(new CalendarEvent(CALENDAR_CHANGED, this));
    }

    // support for adding entries

    /**
     * Adds the given entry to the calendar. This is basically just a convenience
     * method as the actual work of adding an entry to a calendar is done inside
     * {@link Entry#setCalendar(Calendar)}.
     *
     * @param entry the entry to add
     */
    public final void addEntry(Entry<?> entry) {
        addEntries(entry);
    }

    /**
     * Adds the given entries to the calendar. This is basically just a convenience
     * method as the actual work of adding an entry to a calendar is done inside
     * {@link Entry#setCalendar(Calendar)}.
     *
     * @param entries the entries to add
     */
    public final void addEntries(Entry<?>... entries) {
        if (entries != null) {
            for (Entry<?> entry : entries) {
                entry.setCalendar(this);
            }
        }
    }

    /**
     * Adds the given entries to the calendar. This is basically just a convenience
     * method as the actual work of adding an entry to a calendar is done inside
     * {@link Entry#setCalendar(Calendar)}.
     *
     * @param entries the collection of entries to add
     */
    public final void addEntries(Collection<Entry<?>> entries) {
        if (entries != null) {
            entries.forEach(this::addEntry);
        }
    }

    /**
     * Adds the entries returned by the iterator to the calendar. This is basically just a convenience
     * method as the actual work of adding an entry to a calendar is done inside {@link Entry#setCalendar(Calendar)}.
     *
     * @param entries the entries to add
     */
    public final void addEntries(Iterator<Entry<?>> entries) {
        if (entries != null) {
            while (entries.hasNext()) {
                addEntry(entries.next());
            }
        }
    }

    /**
     * Adds the entries returned by the iterable to the calendar. This is basically just a convenience
     * method as the actual work of adding an entry to a calendar is done inside {@link Entry#setCalendar(Calendar)}.
     *
     * @param entries the entries to add
     */
    public final void addEntries(Iterable<Entry<?>> entries) {
        if (entries != null) {
            addEntries(entries.iterator());
        }
    }

    // support for removing entries

    /**
     * Removes the given entry from the calendar. This is basically just a convenience
     * method as the actual work of removing an entry from a calendar is done inside
     * {@link Entry#setCalendar(Calendar)}.
     *
     * @param entry the entry to remove
     */
    public final void removeEntry(Entry<?> entry) {
        removeEntries(entry);
    }

    /**
     * Removes the given entries from the calendar. This is basically just a convenience
     * method as the actual work of removing an entry from a calendar is done inside
     * {@link Entry#setCalendar(Calendar)}.
     *
     * @param entries the entries to remove
     */
    public final void removeEntries(Entry<?>... entries) {
        if (entries != null) {
            for (Entry<?> entry : entries) {
                entry.setCalendar(null);
            }
        }
    }

    /**
     * Removes the given entries from the calendar. This is basically just a convenience
     * method as the actual work of removing an entry from a calendar is done inside
     * {@link Entry#setCalendar(Calendar)}.
     *
     * @param entries the collection of entries to remove
     */
    public final void removeEntries(Collection<Entry<?>> entries) {
        if (entries != null) {
            entries.forEach(this::removeEntry);
        }
    }

    /**
     * Removes the entries returned by the iterator from the calendar. This is basically just a convenience
     * method as the actual work of removing an entry from a calendar is done inside {@link Entry#setCalendar(Calendar)}.
     *
     * @param entries the entries to remove
     */
    public final void removeEntries(Iterator<Entry<?>> entries) {
        if (entries != null) {
            while (entries.hasNext()) {
                removeEntry(entries.next());
            }
        }
    }

    /**
     * Adds the entries returned by the iterable to the calendar. This is basically just a convenience
     * method as the actual work of adding an entry to a calendar is done inside {@link Entry#setCalendar(Calendar)}.
     *
     * @param entries the entries to add
     */
    public final void removeEntries(Iterable<Entry<?>> entries) {
        if (entries != null) {
            removeEntries(entries.iterator());
        }
    }

    final void impl_addEntry(Entry<?> entry) {
        if (entry.isRecurrence()) {
            throw new IllegalArgumentException("a recurrence entry can not be added to a calendar"); //$NON-NLS-1$
        }

        dirty = true;

        intervalTree.add(entry);
    }

    final void impl_removeEntry(Entry<?> entry) {
        if (entry.isRecurrence()) {
            throw new IllegalArgumentException("a recurrence entry can not be added to a calendar"); //$NON-NLS-1$
        }

        dirty = true;

        intervalTree.remove(entry);
    }

    // Name support.

    private final StringProperty name = new SimpleStringProperty(this, "name", "Untitled"); //$NON-NLS-1$

    /**
     * A property used to store the name of the calendar.
     *
     * @return the property used for storing the calendar name
     */
    public final StringProperty nameProperty() {
        return name;
    }

    /**
     * Sets the value of {@link #nameProperty()}.
     *
     * @param name the new name for the calendar
     */
    public final void setName(String name) {
        nameProperty().set(name);
    }

    /**
     * Returns the value of {@link #nameProperty()}.
     *
     * @return the name of the calendar
     */
    public final String getName() {
        return nameProperty().get();
    }

    // Short name support.

    private final StringProperty shortName = new SimpleStringProperty(this, "shortName", "Unt."); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * A property used to store the short name of the calendar.
     *
     * @return the property used for storing the calendar short name
     */
    public final StringProperty shortNameProperty() {
        return shortName;
    }

    /**
     * Sets the value of {@link #shortNameProperty()}.
     *
     * @param name the new short name for the calendar
     */
    public final void setShortName(String name) {
        shortNameProperty().set(name);
    }

    /**
     * Returns the value of {@link #shortNameProperty()}.
     *
     * @return the short name of the calendar
     */
    public final String getShortName() {
        return shortNameProperty().get();
    }

    // Style prefix support.

    private final StringProperty style = new SimpleStringProperty(this, "style", //$NON-NLS-1$
            Style.STYLE1.name().toLowerCase());

    /**
     * A property used to store the visual style that will be used for the
     * calendar in the UI. A style can be any arbitrary name. The style will be
     * used as a prefix to find the styles in the stylesheet. For examples
     * please search the standard framework stylesheet for the predefined styles
     * "style1-", "style2-", etc.
     *
     * @return the visual calendar style
     */
    public final StringProperty styleProperty() {
        return style;
    }

    /**
     * Sets the value of {@link #styleProperty()} based on one of the predefined
     * styles (see also the enum {@link Style}). The image below shows how the
     * styles appear in the UI.
     * <p>
     * <center><img src="doc-files/styles.png"></center>
     *
     * @param style the calendar style
     */
    public final void setStyle(Style style) {
        MODEL.finer(getName() + ": setting style to: " + style); //$NON-NLS-1$
        setStyle(style.name().toLowerCase());
    }

    /**
     * Sets the value of {@link #styleProperty()}.
     *
     * @param stylePrefix the calendar style
     */
    public final void setStyle(String stylePrefix) {
        requireNonNull(stylePrefix);
        MODEL.finer(getName() + ": setting style to: " + style); //$NON-NLS-1$
        styleProperty().set(stylePrefix);
    }

    /**
     * Returns the value of {@link #styleProperty()}.
     *
     * @return the current calendar style
     */
    public final String getStyle() {
        return styleProperty().get();
    }

    // Read only support.

    private final BooleanProperty readOnly = new SimpleBooleanProperty(this, "readOnly", false); //$NON-NLS-1$

    /**
     * A property used to control if the calendar is read-only or not.
     *
     * @return true if the calendar is read-only (default is false)
     */
    public final BooleanProperty readOnlyProperty() {
        return readOnly;
    }

    /**
     * Returns the value of {@link #readOnlyProperty()}.
     *
     * @return true if the calendar can not be edited by the user
     */
    public final boolean isReadOnly() {
        return readOnlyProperty().get();
    }

    /**
     * Sets the value of {@link #readOnlyProperty()}.
     *
     * @param readOnly the calendar can not be edited by the user if true
     */
    public final void setReadOnly(boolean readOnly) {
        MODEL.finer(getName() + ": setting read only to: " + readOnly); //$NON-NLS-1$
        readOnlyProperty().set(readOnly);
    }

    private ObservableList<EventHandler<CalendarEvent>> eventHandlers = FXCollections.observableArrayList();

    /**
     * Adds an event handler for calendar events. Handlers will be called when
     * an entry gets added, removed, changes, etc.
     *
     * @param l the event handler to add
     */
    public final void addEventHandler(EventHandler<CalendarEvent> l) {
        if (l != null) {
            if (MODEL.isLoggable(FINER)) {
                MODEL.finer(getName() + ": adding event handler: " + l); //$NON-NLS-1$
            }
            eventHandlers.add(l);
        }
    }

    /**
     * Removes an event handler from the calendar.
     *
     * @param l the event handler to remove
     */
    public final void removeEventHandler(EventHandler<CalendarEvent> l) {
        if (l != null) {
            if (MODEL.isLoggable(FINER)) {
                MODEL.finer(getName() + ": removing event handler: " + l); //$NON-NLS-1$
            }
            eventHandlers.remove(l);
        }
    }

    private boolean fireEvents = true;

    /**
     * Fires the given calendar event to all event handlers currently registered
     * with this calendar.
     *
     * @param evt the event to fire
     */
    public final void fireEvent(CalendarEvent evt) {
        if (fireEvents && !batchUpdates) {
            if (MODEL.isLoggable(FINER)) {
                MODEL.finer(getName() + ": fireing event: " + evt); //$NON-NLS-1$
            }

            requireNonNull(evt);
            Event.fireEvent(this, evt);
        }
    }

    @Override
    public final EventDispatchChain buildEventDispatchChain(EventDispatchChain givenTail) {
        return givenTail.append((event, tail) -> {
            if (event instanceof CalendarEvent) {
                for (EventHandler<CalendarEvent> handler : eventHandlers) {
                    handler.handle((CalendarEvent) event);
                }
            }

            return event;
        });
    }

    @Override
    public String toString() {
        return "Calendar [name=" + getName() + ", style=" + getStyle() //$NON-NLS-1$ //$NON-NLS-2$
                + ", readOnly=" + isReadOnly() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
