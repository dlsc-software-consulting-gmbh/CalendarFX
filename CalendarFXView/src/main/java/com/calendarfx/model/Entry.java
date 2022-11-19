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

import impl.com.calendarfx.view.util.Util;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import net.fortuna.ical4j.model.Recur;
import org.controlsfx.control.PropertySheet.Item;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.calendarfx.util.LoggingDomain.MODEL;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.FINE;

/**
 * An entry inside a calendar, for example "Dentist Appointment, Feb 2nd, 9am".
 * Entries are added to and managed by calendars. The main attributes of an
 * entry are:
 *
 * <ul>
 * <li><b>Title</b> - the title shown to the user in the UI</li>
 * <li><b>Interval</b> - the time interval and time zone occupied by the entry</li>
 * <li><b>Full Day</b> - a flag signalling whether the entry should be treated as a "full day" event, e.g. a birthday</li>
 * <li><b>Calendar</b> - the calendar to which the entry belongs</li>
 * </ul>
 * The default minimum duration of an entry is 15 minutes.
 *
 * <h2>Visual Appearance</h2>
 * The image below shows an entry called "dentist appointment" as it would be visualized
 * via an {@link com.calendarfx.view.DayEntryView} inside a {@link com.calendarfx.view.DayView}.
 *
 * <img src="doc-files/entry.png" alt="Entry">
 *
 *
 * <h2>Recurrence</h2>
 * <p>
 * This class supports the industry standard for defining recurring events (RFC
 * 2445). For recurring events the method {@link #setRecurrenceRule(String)}
 * must be fed with a valid RRULE string, for example "RRULE:FREQ=DAILY" for an
 * event that occures every day. The calendar to which the entry belongs is then
 * responsible for creating the recurring entries when its
 * {@link Calendar#findEntries(LocalDate, LocalDate, ZoneId)} method gets
 * invoked. Recurring entries will return "true" when their
 * {@link #isRecurrence()} method is called and they will also be able to return
 * the "source" entry ({@link #getRecurrenceSourceEntry()}).
 *
 * <h2>Example</h2>
 *
 * <pre>
 * Entry entry = new Entry(&quot;Dentist Appointment&quot;);
 * Interval interval = new Interval(...);
 * entry.setInterval(interval);
 * entry.setRecurrenceRule("RRULE:FREQ=DAILY;INTERVAL=2;");
 *
 * Calendar calendar = new Calendar(&quot;Health&quot;);
 * calendar.addEntry(entry);
 * </pre>
 *
 * @param <T> the type of the user object
 */
public class Entry<T> implements Comparable<Entry<?>> {

    private static final Duration DEFAULT_MINIMUM_DURATION = Duration.ofMinutes(15);

    private String id;

    private ObjectProperty<String> entryNotes = new SimpleObjectProperty<>();

    /**
     * Constructs a new entry with a default time interval. The ID will be generated
     * via {@link UUID#randomUUID()}.
     */
    public Entry() {
        this(UUID.randomUUID().toString());
    }

    /**
     * Constructs a new entry with the given title and a default time interval.
     * The ID will be generated via {@link UUID#randomUUID()}.
     *
     * @param title the title shown to the user
     */
    public Entry(String title) {
        this(title, new Interval(), UUID.randomUUID().toString());
    }

    /**
     * Constructs a new entry with the given title, a default time interval, and
     * the given ID.
     *
     * @param title the title shown to the user
     * @param id the unique id of the entry
     */
    public Entry(String title, String id) {
        this(title, new Interval(), id);
    }

    /**
     * Constructs a new entry with the given title. The ID will be generated
     * via {@link UUID#randomUUID()}.
     *
     * @param title    the title shown to the user
     * @param interval the time interval where the entry is located
     */
    public Entry(String title, Interval interval) {
        this(title, interval, UUID.randomUUID().toString());
    }

    /**
     * Constructs a new entry with the given title.
     *
     * @param title    the title shown to the user
     * @param interval the time interval where the entry is located
     * @param id       a unique ID, e.g. UUID.randomUUID();
     */
    public Entry(String title, Interval interval, String id) {
        requireNonNull(title);
        requireNonNull(interval);
        requireNonNull(id);

        setTitle(title);
        setInterval(interval);
        this.id = id;
        //this.entryNotes = "Enter Text...";
    }
    //TD: Constructor that now includes notes field
    public Entry(String title, Interval interval, String id, String notes) {
        requireNonNull(title);
        requireNonNull(interval);
        requireNonNull(id);

        setTitle(title);
        setInterval(interval);
        this.id = id;
        //this.entryNotes = "Enter Text...";
    }

    // A map containing a set of properties for this entry
    private ObservableMap<Object, Object> properties;

    /**
     * Returns an observable map of properties on this entry for use primarily
     * by application developers.
     *
     * @return an observable map of properties on this entry for use primarily
     * by application developers
     */
    public final ObservableMap<Object, Object> getProperties() {
        if (properties == null) {
            properties = FXCollections.observableMap(new HashMap<>());

            MapChangeListener<? super Object, ? super Object> changeListener = change -> {
                if (change.getKey().equals("com.calendarfx.recurrence.source")) {
                    if (change.getValueAdded() != null) {
                        @SuppressWarnings("unchecked")
                        Entry<T> source = (Entry<T>) change.getValueAdded();

                        // lookup of property first to instantiate
                        recurrenceSourceProperty();
                        recurrenceSource.set(source);
                    }
                } else if (change.getKey().equals("com.calendarfx.recurrence.id")) {
                    if (change.getValueAdded() != null) {
                        setRecurrenceId((String) change.getValueAdded());
                    }
                }
            };

            properties.addListener(changeListener);
        }

        return properties;
    }

    /**
     * Tests if the entry has properties.
     *
     * @return true if the entry has properties.
     */
    public final boolean hasProperties() {
        return properties != null && !properties.isEmpty();
    }

    // Lazily instantiated to save memory.
    private ObservableList<String> styleClass;

    /**
     * Checks whether the entry has defined any custom styles at all. Calling
     * this method is better than calling {@link #getStyleClass()} directly as
     * it does not instantiate the lazily created style class list if it doesn't
     * exist, yet.
     *
     * @return true if the entry defines any styles on its own
     */
    public final boolean hasStyleClass() {
        return styleClass != null && !styleClass.isEmpty();
    }

    /**
     * Returns a list of style classes. Adding styles to this list allows the
     * application to style individual calendar entries (e.g. based on some kind
     * of status).
     *
     * @return a list of style classes
     */
    public final ObservableList<String> getStyleClass() {
        if (styleClass == null) {
            styleClass = FXCollections.observableArrayList();
        }
        return styleClass;
    }

    private final ObjectProperty<Interval> interval = new SimpleObjectProperty<>(this, "interval") {
        @Override
        public void set(Interval newInterval) {

            if (newInterval == null) {
                return;
            }

            Interval oldInterval = getValue();

            if (!Objects.equals(newInterval, oldInterval)) {

                Calendar calendar = getCalendar();

                if (!isRecurrence() && calendar != null) {
                    calendar.impl_removeEntry(Entry.this);
                }

                super.set(newInterval);

                /*
                 * Update the read-only properties if needed.
                 */
                if (startDate != null) {
                    startDate.set(newInterval.getStartDate());
                }
                if (startTime != null) {
                    startTime.set(newInterval.getStartTime());
                }
                if (endDate != null) {
                    endDate.set(newInterval.getEndDate());
                }
                if (endTime != null) {
                    endTime.set(newInterval.getEndTime());
                }
                if (zoneId != null) {
                    zoneId.set(newInterval.getZoneId());
                }

                updateMultiDay();

                if (calendar != null) {

                    if (!isRecurrence()) {
                        calendar.impl_addEntry(Entry.this);
                    }

                    calendar.fireEvent(new CalendarEvent(CalendarEvent.ENTRY_INTERVAL_CHANGED, calendar, Entry.this, oldInterval));
                }
            }
        }
    };

    /**
     * A property used to store the time interval occupied by this entry. The
     * interval object stores the start and end dates, the start and end times,
     * and the time zone. Changes to this property will automatically update the
     * read-only properties {@link #startDateProperty()},
     * {@link #endDateProperty()}, {@link #startTimeProperty()},
     * {@link #endTimeProperty()}, {@link #zoneIdProperty()}, and
     * {@link #multiDayProperty()}.
     *
     * @return the time interval used by the entry
     */
    public final ObjectProperty<Interval> intervalProperty() {
        return interval;
    }

    /**
     * Returns the value of {@link #intervalProperty()}.
     *
     * @return the time interval used by the entry
     */
    public final Interval getInterval() {
        return interval.get();
    }

    /**
     * Sets the value of {@link #intervalProperty()}.
     *
     * @param interval the new time interval used by the entry
     */

    public final String getEntryNotes(){
        if(entryNotes == null) {
            return null;
        }
        else{
            return entryNotes.get();
        }
    }
    public final void setInterval(Interval interval) {
        requireNonNull(interval);
        intervalProperty().set(interval);
    }

    //TD: Set Notes
    public final void setNotes(String notes) {
        requireNonNull(notes);
        entryNotes.set(notes);
    }

    // Set Interval: LocalDate support

    public final void setInterval(LocalDate date) {
        setInterval(date, getZoneId());
    }

    public final void setInterval(LocalDate date, ZoneId zoneId) {
        setInterval(date, date, zoneId);
    }

    public final void setInterval(LocalDate startDate, LocalDate endDate) {
        setInterval(startDate, endDate, getZoneId());
    }

    public final void setInterval(LocalDate startDate, LocalDate endDate, ZoneId zoneId) {
        setInterval(startDate, LocalTime.MIN, endDate, LocalTime.MAX, zoneId);
    }

    public final void setInterval(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        setInterval(startDate, startTime, endDate, endTime, getZoneId());
    }

    public final void setInterval(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, ZoneId zoneId) {
        setInterval(new Interval(startDate, startTime, endDate, endTime, zoneId));
    }

    // Set Interval: LocalTime support

    public final void setInterval(LocalTime startTime, LocalTime endTime) {
        setInterval(startTime, endTime, getZoneId());
    }

    public final void setInterval(LocalTime startTime, LocalTime endTime, ZoneId zoneId) {
        setInterval(getStartDate(), startTime, getEndDate(), endTime, zoneId);
    }

    // Set Interval: LocalDateTime support

    public final void setInterval(LocalDateTime dateTime) {
        setInterval(dateTime, dateTime);
    }

    public final void setInterval(LocalDateTime dateTime, ZoneId zoneId) {
        setInterval(dateTime, dateTime, zoneId);
    }

    public final void setInterval(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        setInterval(startDateTime, endDateTime, getZoneId());
    }

    public final void setInterval(LocalDateTime startDateTime, LocalDateTime endDateTime, ZoneId zoneId) {
        setInterval(new Interval(startDateTime, endDateTime, zoneId));
    }

    // Set Interval: ZonedDateTime support

    public final void setInterval(ZonedDateTime date) {
        setInterval(date, date);
    }

    public final void setInterval(ZonedDateTime startDate, ZonedDateTime endDate) {
        setInterval(new Interval(startDate, endDate));
    }

    /**
     * Changes the start date of the entry interval and ensures that the entry's interval
     * stays valid, which means that the start time will be before the end time and that the
     * duration of the entry will be at least the duration defined by the {@link #minimumDurationProperty()}.
     *
     * @param date the new start date
     */
    public final void changeStartDate(LocalDate date) {
        changeStartDate(date, false);
    }

    /**
     * Changes the start date of the entry interval.
     *
     * @param date         the new start date
     * @param keepDuration if true then this method will also change the end date and time in such a way that the total duration
     *                     of the entry will not change. If false then this method will ensure that the entry's interval
     *                     stays valid, which means that the start time will be before the end time and that the
     *                     duration of the entry will be at least the duration defined by the {@link #minimumDurationProperty()}.
     */
    public final void changeStartDate(LocalDate date, boolean keepDuration) {
        requireNonNull(date);

        Interval interval = getInterval();

        LocalDateTime newStartDateTime = getStartAsLocalDateTime().with(date);
        LocalDateTime endDateTime = getEndAsLocalDateTime();

        if (keepDuration) {
            endDateTime = newStartDateTime.plus(getDuration());
            setInterval(newStartDateTime, endDateTime, getZoneId());
        } else {

            /*
             * We might have a problem if the new start time is AFTER the current end time.
             */
            if (newStartDateTime.isAfter(endDateTime)) {
                interval = interval.withEndDateTime(newStartDateTime.plus(interval.getDuration()));
            }

            setInterval(interval.withStartDate(date));
        }
    }

    /**
     * Changes the start time of the entry interval and ensures that the entry's interval
     * stays valid, which means that the start time will be before the end time and that the
     * duration of the entry will be at least the duration defined by the {@link #minimumDurationProperty()}.
     *
     * @param time the new start time
     */
    public final void changeStartTime(LocalTime time) {
        changeStartTime(time, false);
    }

    /**
     * Changes the start time of the entry interval.
     *
     * @param time         the new start time
     * @param keepDuration if true then this method will also change the end time in such a way that the total duration
     *                     of the entry will not change. If false then this method will ensure that the entry's interval
     *                     stays valid, which means that the start time will be before the end time and that the
     *                     duration of the entry will be at least the duration defined by the {@link #minimumDurationProperty()}.
     */
    public final void changeStartTime(LocalTime time, boolean keepDuration) {
        requireNonNull(time);

        Interval interval = getInterval();

        LocalDateTime newStartDateTime = getStartAsLocalDateTime().with(time);
        LocalDateTime endDateTime = getEndAsLocalDateTime();

        if (keepDuration) {
            endDateTime = newStartDateTime.plus(getDuration());
            setInterval(newStartDateTime, endDateTime);
        } else {
            /*
             * We might have a problem if the new start time is AFTER the current end time.
             */
            if (newStartDateTime.isAfter(endDateTime.minus(getMinimumDuration()))) {
                interval = interval.withEndDateTime(newStartDateTime.plus(getMinimumDuration()));
            }

            setInterval(interval.withStartTime(time));
        }
    }

    /**
     * Changes the end date of the entry interval and ensures that the entry's interval
     * stays valid, which means that the start time will be before the end time and that the
     * duration of the entry will be at least the duration defined by the {@link #minimumDurationProperty()}.
     *
     * @param date the new end date
     */
    public final void changeEndDate(LocalDate date) {
        changeEndDate(date, false);
    }

    /**
     * Changes the end date of the entry interval.
     *
     * @param date         the new end date
     * @param keepDuration if true then this method will also change the start date and time in such a way that the total duration
     *                     of the entry will not change. If false then this method will ensure that the entry's interval
     *                     stays valid, which means that the start time will be before the end time and that the
     *                     duration of the entry will be at least the duration defined by the {@link #minimumDurationProperty()}.
     */
    public final void changeEndDate(LocalDate date, boolean keepDuration) {
        requireNonNull(date);

        Interval interval = getInterval();

        LocalDateTime newEndDateTime = getEndAsLocalDateTime().with(date);
        LocalDateTime startDateTime = getStartAsLocalDateTime();

        if (keepDuration) {
            startDateTime = newEndDateTime.minus(getDuration());
            setInterval(startDateTime, newEndDateTime, getZoneId());
        } else {
            /*
             * We might have a problem if the new end time is BEFORE the current start time.
             */
            if (newEndDateTime.isBefore(startDateTime)) {
                interval = interval.withStartDateTime(newEndDateTime.minus(interval.getDuration()));
            }

            setInterval(interval.withEndDate(date));
        }
    }

    /**
     * Changes the end time of the entry interval and ensures that the entry's interval
     * stays valid, which means that the start time will be before the end time and that the
     * duration of the entry will be at least the duration defined by the {@link #minimumDurationProperty()}.
     *
     * @param time the new end time
     */
    public final void changeEndTime(LocalTime time) {
        changeEndTime(time, false);
    }

    /**
     * Changes the end time of the entry interval.
     *
     * @param time         the new end time
     * @param keepDuration if true then this method will also change the start time in such a way that the total duration
     *                     of the entry will not change. If false then this method will ensure that the entry's interval
     *                     stays valid, which means that the start time will be before the end time and that the
     *                     duration of the entry will be at least the duration defined by the {@link #minimumDurationProperty()}.
     */
    public final void changeEndTime(LocalTime time, boolean keepDuration) {
        requireNonNull(time);

        Interval interval = getInterval();

        LocalDateTime newEndDateTime = getEndAsLocalDateTime().with(time);
        LocalDateTime startDateTime = getStartAsLocalDateTime();

        if (keepDuration) {
            startDateTime = newEndDateTime.minus(getDuration());
            setInterval(startDateTime, newEndDateTime, getZoneId());
        } else {
            /*
             * We might have a problem if the new end time is BEFORE the current start time.
             */
            if (newEndDateTime.isBefore(startDateTime.plus(getMinimumDuration()))) {
                interval = interval.withStartDateTime(newEndDateTime.minus(getMinimumDuration()));
            }

            setInterval(interval.withEndTime(time));
        }
    }

    /**
     * Changes the zone ID of the entry interval.
     *
     * @param zoneId the new zone
     */
    public final void changeZoneId(ZoneId zoneId) {
        requireNonNull(zoneId);
        setInterval(getInterval().withZoneId(zoneId));
    }

    private ReadOnlyObjectWrapper<Entry<T>> recurrenceSource;

    /**
     * If the entry is a recurrence (see {@link #recurrenceProperty()}) then
     * this property will store a reference to the entry for which the
     * recurrence was created.
     *
     * @return the entry that was the source of the recurrence
     */
    public final ReadOnlyObjectProperty<Entry<T>> recurrenceSourceProperty() {
        if (recurrenceSource == null) {
            recurrenceSource = new ReadOnlyObjectWrapper<Entry<T>>(this, "recurrenceSource") {
                @Override
                public void set(Entry<T> newEntry) {
                    super.set(newEntry);
                    setRecurrence(newEntry != null);
                }
            };
        }

        return recurrenceSource.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #recurrenceSourceProperty()}.
     *
     * @return the recurrence source
     */
    public final Entry<T> getRecurrenceSourceEntry() {
        return recurrenceSource == null ? null : recurrenceSource.get();
    }

    /**
     * If the entry defines a recurrence rule (see
     * {@link #recurrenceRuleProperty()}) then the calendar will use this method
     * to create one or more "copies" of the entry. The default implementation
     * of this method will simply create a new instance of type {@link Entry}.
     * The initialization of the standard fields (e.g. "Interval" or "Title") of
     * the recurrence copy will be done by the calendar. <b>Subclasses should
     * override this method to also initialize additional fields.</b>
     *
     * @return a recurrence "copy" of the entry.
     */
    public Entry<T> createRecurrence() {
        return new Entry<>();
    }

    private boolean _recurrence;

    private ReadOnlyBooleanWrapper recurrence;

    /**
     * A read-only property used to indicate whether the entry is a recurrence
     * copy of a recurrence source. This property will be set to true if the
     * property {@link #recurrenceSourceProperty()} gets initialized with a
     * value other than null.
     *
     * @return true if the entry is a recurrence copy
     * @see #recurrenceRuleProperty()
     * @see #recurrenceSourceProperty()
     */
    public final ReadOnlyBooleanProperty recurrenceProperty() {
        if (recurrence == null) {
            recurrence = new ReadOnlyBooleanWrapper(this, "recurrence", _recurrence);
        }
        return recurrence.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #recurrenceProperty()}.
     *
     * @return true if the entry is a recurrence copy
     */
    public final boolean isRecurrence() {
        return recurrence == null ? _recurrence : recurrence.get();
    }

    private void setRecurrence(boolean b) {
        if (recurrence == null) {
            _recurrence = b;
        } else {
            recurrence.set(b);
        }
    }

    /**
     * Determines if the entry describes a recurring event.
     *
     * @return true if the entry is recurring
     * @see #recurrenceRuleProperty()
     */
    public final boolean isRecurring() {
        return recurrenceRule != null && !(recurrenceRule.get() == null) && !recurrenceRule.get().isBlank();
    }

    /*
     * Recurrence support.
     */

    private StringProperty recurrenceRule;

    /**
     * A property used to store a recurrence rule according to RFC-2445.
     * <h3>Example</h3> Repeat entry / event every other day until September
     * 1st, 2015.
     *
     * <pre>
     * String rrule = "RRULE:FREQ=DAILY;INTERVAL=2;UNTIL=20150901";
     * setRecurrenceRule(rrule);
     * </pre>
     *
     * @return the recurrenceRule property
     * @see #recurrenceEndProperty()
     */
    public final StringProperty recurrenceRuleProperty() {
        if (recurrenceRule == null) {
            recurrenceRule = new SimpleStringProperty(null, "recurrenceRule") {
                @Override
                public void set(String newRecurrence) {
                    String oldRecurrence = get();

                    if (!Objects.equals(oldRecurrence, newRecurrence)) {

                        Calendar calendar = getCalendar();

                        if (calendar != null && !isRecurrence()) {
                            calendar.impl_removeEntry(Entry.this);
                        }

                        super.set(newRecurrence);

                        updateRecurrenceEndProperty(newRecurrence);

                        if (calendar != null) {
                            if (!isRecurrence()) {
                                calendar.impl_addEntry(Entry.this);
                            }
                            calendar.fireEvent(new CalendarEvent(CalendarEvent.ENTRY_RECURRENCE_RULE_CHANGED, calendar, Entry.this, oldRecurrence));
                        }
                    }
                }

                private void updateRecurrenceEndProperty(String newRecurrence) {
                    if (newRecurrence != null && !newRecurrence.trim().equals("")) {
                        try {
                            Recur<LocalDate> recur = new Recur<>(newRecurrence.replaceFirst("^RRULE:", ""));
                            setRecurrenceEnd(Objects.requireNonNullElse(recur.getUntil(), LocalDate.MAX));
                        } catch (IllegalArgumentException |
                                 DateTimeParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        setRecurrenceEnd(LocalDate.MAX);
                    }
                }
            };
        }

        return recurrenceRule;
    }

    /**
     * Sets the value of {@link #recurrenceRuleProperty()}.
     *
     * @param rec the new recurrence rule
     */
    public final void setRecurrenceRule(String rec) {
        if (recurrenceRule == null && rec == null) {
            // no unnecessary property creation if everything is null
            return;
        }
        recurrenceRuleProperty().set(rec);
    }

    /**
     * Returns the value of {@link #recurrenceRuleProperty()}.
     *
     * @return the recurrence rule
     */
    public final String getRecurrenceRule() {
        return recurrenceRule == null ? null : recurrenceRule.get();
    }

    private String _recurrenceId;

    private ReadOnlyStringWrapper recurrenceId;

    /**
     * Stores the recurrence ID which is being generated on-the-fly by the
     * {@link Calendar} to which the recurrence source entry belongs.
     *
     * @return the recurrence ID property
     */
    public final ReadOnlyStringProperty recurrenceIdProperty() {
        if (recurrenceId == null) {
            recurrenceId = new ReadOnlyStringWrapper(this, "recurrenceId", _recurrenceId);
        }

        return recurrenceId.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #recurrenceIdProperty()}.
     *
     * @return the recurrence ID
     */
    public final String getRecurrenceId() {
        return recurrenceId == null ? _recurrenceId : recurrenceId.get();
    }

    private void setRecurrenceId(String id) {
        if (recurrenceId == null) {
            _recurrenceId = id;
        } else {
            recurrenceId.set(id);
        }
    }

    private LocalDate _recurrenceEnd;

    private ReadOnlyObjectWrapper<LocalDate> recurrenceEnd;

    /**
     * The property used to store the end time of the recurrence rule.
     *
     * @return the recurrence rule end time
     * @see #recurrenceRuleProperty()
     */
    public final ReadOnlyObjectProperty<LocalDate> recurrenceEndProperty() {
        if (recurrenceEnd == null) {
            recurrenceEnd = new ReadOnlyObjectWrapper<>(this, "recurrenceEnd", _recurrenceEnd);
        }

        return recurrenceEnd.getReadOnlyProperty();
    }

    public final ObjectProperty<String> notesProperty(){
        return entryNotes;
    }

    /**
     * Returns the value of {@link #recurrenceRuleProperty()}.
     *
     * @return the recurrence rule end time
     */
    public final LocalDate getRecurrenceEnd() {
        return recurrenceEnd == null ? _recurrenceEnd : recurrenceEnd.get();
    }

    private void setRecurrenceEnd(LocalDate date) {
        if (recurrenceEnd == null) {
            _recurrenceEnd = date;
        } else {
            recurrenceEnd.set(date);
        }
    }

    /**
     * Assigns a new ID to the entry. IDs do not have to be unique. If several
     * entries share the same ID it means that they are representing the same
     * "real world" entry. An entry spanning multiple days will be shown via
     * several entries in the month view. Clicking on one of them will select
     * all of them as they all represent the same thing.
     *
     * @param id the new ID of the entry
     */
    public final void setId(String id) {
        requireNonNull(id);
        if (MODEL.isLoggable(FINE)) {
            MODEL.fine("setting id to " + id);
        }
        this.id = id;
    }

    /**
     * Returns the ID of the entry.
     *
     * @return the id object
     */
    public final String getId() {
        return id;
    }

    /*
     * Calendar support.
     */
    private final SimpleObjectProperty<Calendar> calendar = new SimpleObjectProperty<Calendar>(this, "calendar") {

        @Override
        public void set(Calendar newCalendar) {
            Calendar oldCalendar = get();

            if (!Objects.equals(oldCalendar, newCalendar)) {

                if (oldCalendar != null) {
                    if (!isRecurrence()) {
                        oldCalendar.impl_removeEntry(Entry.this);
                    }
                }

                super.set(newCalendar);

                if (newCalendar != null) {
                    if (!isRecurrence()) {
                        newCalendar.impl_addEntry(Entry.this);
                    }
                }

                if (newCalendar != null) {
                    newCalendar.fireEvent(new CalendarEvent(CalendarEvent.ENTRY_CALENDAR_CHANGED, newCalendar, Entry.this, oldCalendar));
                } else if (oldCalendar != null) {
                    oldCalendar.fireEvent(new CalendarEvent(CalendarEvent.ENTRY_CALENDAR_CHANGED, newCalendar, Entry.this, oldCalendar));
                }
            }
        }
    };

    /**
     * A property used to store a reference to the calendar that owns the entry.
     *
     * @return the calendar property
     */
    public final ObjectProperty<Calendar> calendarProperty() {
        return calendar;
    }

    /**
     * Sets the value of {@link #calendarProperty()}.
     *
     * @param cal the new owning calendar of this entry
     */
    public final void setCalendar(Calendar cal) {
        calendar.set(cal);
    }

    /**
     * Returns the value of {@link #calendarProperty()}.
     *
     * @return the owning calendar of this entry
     */
    public final Calendar getCalendar() {
        return calendar.get();
    }

    /**
     * A convenience method to easily remove the entry from its calendar. Delegates
     * to {@link #setCalendar(Calendar)} and passes a null value.
     */
    public final void removeFromCalendar() {
        setCalendar(null);
    }

    /*
     * User object support.
     */

    private ObjectProperty<T> userObject;

    /**
     * A property used to store a reference to an optional user object. The user
     * object is usually the reason why the entry was created.
     *
     * @return the user object property
     */
    public final ObjectProperty<T> userObjectProperty() {
        if (userObject == null) {
            userObject = new SimpleObjectProperty<T>(this, "userObject") {
                @Override
                public void set(T newObject) {
                    T oldUserObject = get();

                    // We do not use .equals() here to allow to reset the object even if is "looks" the same e.g. if it
                    // has some .equals() method implemented which just compares an id/business key.
                    if (oldUserObject != newObject) {
                        super.set(newObject);

                        Calendar calendar = getCalendar();
                        if (calendar != null) {
                            calendar.fireEvent(new CalendarEvent(CalendarEvent.ENTRY_USER_OBJECT_CHANGED, calendar, Entry.this, oldUserObject));
                        }
                    }
                }
            };
        }

        return userObject;
    }

    /**
     * Sets the value of {@link #userObjectProperty()}.
     *
     * @param object the new user object
     */
    public final void setUserObject(T object) {
        if (userObject == null && object == null) {
            // no unnecessary property creation if everything is null
            return;
        }
        userObjectProperty().set(object);
    }

    /**
     * Returns the value of {@link #userObjectProperty()}.
     *
     * @return the user object
     */
    public final T getUserObject() {
        return userObject == null ? null : userObject.get();
    }

    /*
     * Zone ID support.
     */

    private ReadOnlyObjectWrapper<ZoneId> zoneId;

    /**
     * A property used to store a time zone for the entry. The time zone is
     * needed for properly interpreting the dates and times of the entry.
     *
     * @return the time zone property
     */
    public final ReadOnlyObjectProperty<ZoneId> zoneIdProperty() {
        if (zoneId == null) {
            zoneId = new ReadOnlyObjectWrapper<>(this, "zoneId", getInterval().getZoneId());
        }

        return zoneId.getReadOnlyProperty();
    }

    /**
     * Sets the value of {@link #zoneIdProperty()}.
     *
     * @param zoneId the new time zone to use for this entry
     */
    public final void setZoneId(ZoneId zoneId) {
        requireNonNull(zoneId);
        setInterval(getInterval().withZoneId(zoneId));
    }

    /**
     * Returns the value of {@link #zoneIdProperty()}.
     *
     * @return the entry's time zone
     */
    public final ZoneId getZoneId() {
        return getInterval().getZoneId();
    }

    /*
     * Title support.
     */
    private final StringProperty title = new SimpleStringProperty(this, "title") {
        @Override
        public void set(String newTitle) {
            String oldTitle = get();

            if (!Objects.equals(oldTitle, newTitle)) {
                super.set(newTitle);

                Calendar calendar = getCalendar();
                if (calendar != null) {
                    calendar.fireEvent(new CalendarEvent(CalendarEvent.ENTRY_TITLE_CHANGED, calendar, Entry.this, oldTitle));
                }
            }
        }
    };

    /**
     * A property used to store the title of the entry.
     *
     * @return the title property
     */
    public final StringProperty titleProperty() {
        return title;
    }

    /**
     * Sets the value of {@link #titleProperty()}.
     *
     * @param title the title shown by the entry
     */
    public final void setTitle(String title) {
        titleProperty().set(title);
    }

    /**
     * Returns the value of {@link #titleProperty()}.
     *
     * @return the title of the entry
     */
    public final String getTitle() {
        return titleProperty().get();
    }

    /*
     * Location support.
     */

    private StringProperty location;

    /**
     * A property used to store a free-text location specification for the given
     * entry. This could be as simple as "New York" or a full address as in
     * "128 Madison Avenue, New York, USA".
     *
     * @return the location of the event specified by the entry
     */
    public final StringProperty locationProperty() {
        if (location == null) {
            location = new SimpleStringProperty(null, "location") {
                @Override
                public void set(String newLocation) {
                    String oldLocation = get();

                    if (!Objects.equals(oldLocation, newLocation)) {

                        super.set(newLocation);

                        Calendar calendar = getCalendar();
                        if (calendar != null) {
                            calendar.fireEvent(new CalendarEvent(CalendarEvent.ENTRY_LOCATION_CHANGED, calendar, Entry.this, oldLocation));
                        }
                    }
                }
            };
        }

        return location;
    }

    /**
     * Sets the value of {@link #locationProperty()}.
     *
     * @param loc the new location
     */
    public final void setLocation(String loc) {
        if (location == null && loc == null) {
            // no unnecessary property creation if everything is null
            return;
        }
        locationProperty().set(loc);
    }

    /**
     * Returns the value of {@link #locationProperty()}.
     *
     * @return the location
     */
    public final String getLocation() {
        return location == null ? null : location.get();
    }

    private ReadOnlyObjectWrapper<LocalDate> startDate;

    /**
     * A read-only property used for retrieving the start date of the entry. The
     * property gets updated whenever the start date inside the entry interval
     * changes (see {@link #intervalProperty()}).
     *
     * @return the start date of the entry
     */
    public final ReadOnlyObjectProperty<LocalDate> startDateProperty() {
        if (startDate == null) {
            startDate = new ReadOnlyObjectWrapper<>(this, "startDate", getInterval().getStartDate());
        }
        return startDate.getReadOnlyProperty();
    }

    /**
     * Returns the start date of the entry's interval (see
     * {@link #intervalProperty()}).
     *
     * @return the entry's start date
     */
    public final LocalDate getStartDate() {
        return getInterval().getStartDate();
    }

    private ReadOnlyObjectWrapper<LocalTime> startTime;

    /**
     * A read-only property used for retrieving the start time of the entry. The
     * property gets updated whenever the start time inside the entry interval
     * changes (see {@link #intervalProperty()}).
     *
     * @return the start time of the entry
     */
    public final ReadOnlyObjectProperty<LocalTime> startTimeProperty() {
        if (startTime == null) {
            startTime = new ReadOnlyObjectWrapper<>(this, "startTime", getInterval().getStartTime());
        }
        return startTime.getReadOnlyProperty();
    }

    /**
     * Returns the start time of the entry's interval (see
     * {@link #intervalProperty()}).
     *
     * @return the entry's start time
     */
    public final LocalTime getStartTime() {
        return getInterval().getStartTime();
    }

    private ReadOnlyObjectWrapper<LocalDate> endDate;

    /**
     * A read-only property used for retrieving the end date of the entry. The
     * property gets updated whenever the end date inside the entry interval
     * changes (see {@link #intervalProperty()}).
     *
     * @return the end date of the entry
     */
    public final ReadOnlyObjectProperty<LocalDate> endDateProperty() {
        if (endDate == null) {
            endDate = new ReadOnlyObjectWrapper<>(this, "endDate", getInterval().getEndDate());
        }

        return endDate.getReadOnlyProperty();
    }

    /**
     * Returns the end date of the entry's interval (see
     * {@link #intervalProperty()}).
     *
     * @return the entry's end date
     */
    public final LocalDate getEndDate() {
        return getInterval().getEndDate();
    }

    private ReadOnlyObjectWrapper<LocalTime> endTime;

    /**
     * A read-only property used for retrieving the end time of the entry. The
     * property gets updated whenever the end time inside the entry interval
     * changes (see {@link #intervalProperty()}).
     *
     * @return the end time of the entry
     */
    public final ReadOnlyObjectProperty<LocalTime> endTimeProperty() {
        if (endTime == null) {
            endTime = new ReadOnlyObjectWrapper<>(this, "endTime", getInterval().getEndTime());
        }

        return endTime.getReadOnlyProperty();
    }

    /**
     * Returns the end time of the entry's interval (see
     * {@link #intervalProperty()}).
     *
     * @return the entry's end time
     */
    public final LocalTime getEndTime() {
        return getInterval().getEndTime();
    }

    /*
     * Full day support.
     */
    private final BooleanProperty fullDay = new SimpleBooleanProperty(this, "fullDay", false) {

        @Override
        public void set(boolean newFullDay) {
            boolean oldFullDay = get();

            if (!Objects.equals(oldFullDay, newFullDay)) {

                super.set(newFullDay);

                Calendar calendar = getCalendar();
                if (calendar != null) {
                    calendar.fireEvent(new CalendarEvent(CalendarEvent.ENTRY_FULL_DAY_CHANGED, calendar, Entry.this));
                }
            }
        }
    };

    /**
     * A property used to signal whether an entry is considered to be a
     * "full day" entry, for example a birthday. The image below shows how full
     * day entries are shown in the UI.
     *
     * <img width="100%" src="doc-files/full-day.png" alt="Full Day">
     *
     * @return the full day property
     */
    public final BooleanProperty fullDayProperty() {
        return fullDay;
    }

    /**
     * Returns the value of {@link #fullDayProperty()}.
     *
     * @return true if the entry is a full day entry, e.g. a birthday
     */
    public final boolean isFullDay() {
        return fullDayProperty().get();
    }

    /**
     * Sets the value of {@link #fullDayProperty()}.
     *
     * @param fullDay true if entry is a full day entry, e.g. a birthday
     */
    public final void setFullDay(boolean fullDay) {
        fullDayProperty().set(fullDay);
    }

    // shadow field
    private Duration _minimumDuration = DEFAULT_MINIMUM_DURATION;

    private ObjectProperty<Duration> minimumDuration;

    /**
     * A property used to store the minimum duration an entry can have. It is
     * often the case that applications do not allow calendar entries to be
     * shorter than a certain duration. This property can be used to specify
     * this. The default value is 15 minutes. Use {@link Duration#ZERO} to allow
     * zero duration entries.
     *
     * @return the minimum duration of the entry
     */
    public final ObjectProperty<Duration> minimumDurationProperty() {
        if (minimumDuration == null) {
            minimumDuration = new SimpleObjectProperty<>(this, "minimumDuration", _minimumDuration);
        }
        return minimumDuration;
    }

    /**
     * Returns the value of {@link #minimumDurationProperty()}.
     *
     * @return the minimum duration of the entry
     */
    public final Duration getMinimumDuration() {
        return minimumDuration == null ? _minimumDuration : minimumDuration.get();
    }

    /**
     * Sets the value of {@link #minimumDurationProperty()}.
     *
     * @param duration the minimum duration
     */
    public final void setMinimumDuration(Duration duration) {
        Objects.requireNonNull(duration);
        if (minimumDuration != null) {
            minimumDuration.set(duration);
        } else {
            _minimumDuration = duration;
        }
    }

    /*
     * Utility methods.
     */

    /**
     * Used by the {@link Calendar#findEntries(String)} to find entries based on
     * a text search. This method can be overriden. The default implementation
     * compares the given text with the title of the entry (lower case
     * comparison).
     *
     * @param searchTerm the search term
     * @return true if the entry matches the given search term
     */
    public boolean matches(String searchTerm) {
        String title = getTitle();
        if (title != null) {
            return title.toLowerCase().contains(searchTerm.toLowerCase());
        }

        return false;
    }

    /**
     * Utility method to get the zoned start time. This method combines the
     * start date, start time, and the zone id to create a zoned date time
     * object.
     *
     * @return the zoned start time
     * @see #getStartDate()
     * @see #getStartTime()
     * @see #getZoneId()
     */
    public final ZonedDateTime getStartAsZonedDateTime() {
        return getInterval().getStartZonedDateTime();
    }

    /**
     * Returns the start time in milliseconds since 1.1.1970.
     *
     * @return the start time in milliseconds
     */
    public final long getStartMillis() {
        return getInterval().getStartMillis();
    }

    /**
     * Utility method to get the local start date time. This method combines the
     * start date and the start time to create a date time object.
     *
     * @return the start local date time
     * @see #getStartDate()
     * @see #getStartTime()
     */
    public final LocalDateTime getStartAsLocalDateTime() {
        return getInterval().getStartDateTime();
    }

    /**
     * Utility method to get the zoned end time. This method combines the end
     * date, end time, and the zone id to create a zoned date time object.
     *
     * @return the zoned end time
     * @see #getEndDate()
     * @see #getEndTime()
     * @see #getZoneId()
     */
    public final ZonedDateTime getEndAsZonedDateTime() {
        return getInterval().getEndZonedDateTime();
    }

    /**
     * Returns the end time in milliseconds since 1.1.1970.
     *
     * @return the end time in milliseconds
     */
    public final long getEndMillis() {
        return getInterval().getEndMillis();
    }

    /**
     * Utility method to get the local end date time. This method combines the
     * end date and the end time to create a date time object.
     *
     * @return the end local date time
     * @see #getEndDate()
     * @see #getEndTime()
     */
    public final LocalDateTime getEndAsLocalDateTime() {
        return getInterval().getEndDateTime();
    }

    private void updateMultiDay() {
        setMultiDay(getEndDate().isAfter(getStartDate()));
    }

    private boolean _multiDay;

    private ReadOnlyBooleanWrapper multiDay;

    /**
     * A read-only property to determine if the entry spans several days. The
     * image below shows such an entry.
     *
     * <img src="doc-files/multi-day.png" alt="Multi Day">
     *
     * @return true if the end date is after the start date (multiple days)
     * @see #getStartDate()
     * @see #getEndDate()
     */
    public final ReadOnlyBooleanProperty multiDayProperty() {
        if (multiDay == null) {
            multiDay = new ReadOnlyBooleanWrapper(this, "multiDay", _multiDay);
        }
        return multiDay.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #multiDayProperty()}.
     *
     * @return true if the entry spans multiple days
     */
    public final boolean isMultiDay() {
        return multiDay == null ? _multiDay : multiDay.get();
    }

    private void setMultiDay(boolean b) {
        if (multiDay == null) {
            _multiDay = b;
        } else {
            multiDay.set(b);
        }
    }

    /**
     * Utility method to determine if this entry and the given entry intersect
     * each other (time bounds overlap each other).
     *
     * @param entry the other entry to check
     * @return true if the entries' time bounds overlap
     */
    public final boolean intersects(Entry<?> entry) {
        return intersects(entry.getStartAsZonedDateTime(), entry.getEndAsZonedDateTime());
    }

    /**
     * Utility method to determine if this entry and the given time interval
     * intersect each other (time bounds overlap each other).
     *
     * @param startTime time interval start
     * @param endTime   time interval end
     * @return true if the entry and the given time interval overlap
     */
    public final boolean intersects(ZonedDateTime startTime, ZonedDateTime endTime) {
        return Util.intersect(startTime, endTime, getStartAsZonedDateTime(), getEndAsZonedDateTime());
    }

    /**
     * Utility method to calculate the duration of the entry. The duration is
     * computed based on the zoned start and end time.
     *
     * @return the duration of the entry
     * @see #getStartAsZonedDateTime()
     * @see #getEndAsZonedDateTime()
     */
    public final Duration getDuration() {
        return Duration.between(getStartAsZonedDateTime(), getEndAsZonedDateTime());
    }

    /**
     * Checks whether the entry will be visible within the given start and end dates. This method
     * takes recurrence into consideration and will return true if any recurrence of this entry
     * will be displayed inside the given time interval.
     *
     * @param startDate the start date of the search interval
     * @param endDate   the end date of the search interval
     * @param zoneId    the time zone
     * @return true if the entry or any of its recurrences is showing
     */
    public final boolean isShowing(LocalDate startDate, LocalDate endDate, ZoneId zoneId) {
        return isShowing(this, startDate, endDate, zoneId);
    }

    private boolean isShowing(Entry<?> entry, LocalDate startDate, LocalDate endDate, ZoneId zoneId) {
        ZonedDateTime st = ZonedDateTime.of(startDate, LocalTime.MIN, zoneId);
        ZonedDateTime et = ZonedDateTime.of(endDate, LocalTime.MAX, zoneId);

        if (entry.isRecurring() || entry.isRecurrence()) {
            return isRecurrenceShowing(entry, st, et, zoneId);
        }

        Interval interval = entry.getInterval();

        return Util.intersect(interval.getStartZonedDateTime(), interval.getEndZonedDateTime(), st, et);
    }

    private final BooleanProperty hidden = new SimpleBooleanProperty(this, "hidden", false);

    public final boolean isHidden() {
        return hidden.get();
    }

    public final BooleanProperty hiddenProperty() {
        return hidden;
    }

    /**
     * An entry can be made explicitly hidden.
     *
     * @param hidden true if the entry should not be visible in the calendar
     */
    public final void setHidden(boolean hidden) {
        this.hidden.set(hidden);
    }

    private boolean isRecurrenceShowing(Entry<?> entry, ZonedDateTime st, ZonedDateTime et, ZoneId zoneId) {
        String recurrenceRule = entry.getRecurrenceRule().replaceFirst("^RRULE:", "");

        LocalDate utilStartDate = entry.getStartDate();

        try {
            LocalDate utilEndDate = et.toLocalDate();

            /*
             * TODO: for performance reasons we should definitely
             * use the advanceTo() call, but unfortunately this
             * collides with the fact that e.g. the DetailedWeekView loads
             * data day by day. So a given day would not show
             * entries that start on the day before but intersect
             * with the given day. We have to find a solution for
             * this.
             */
            // iterator.advanceTo(st.toLocalDate());

            List<LocalDate> dateList = new Recur<LocalDate>(recurrenceRule).getDates(utilStartDate, utilEndDate);

            for (LocalDate repeatingDate : dateList) {
                ZonedDateTime recurrenceStart = ZonedDateTime.of(repeatingDate, LocalTime.MIN, zoneId);
                ZonedDateTime recurrenceEnd = recurrenceStart.plus(entry.getDuration());

                if (Util.intersect(recurrenceStart, recurrenceEnd, st, et)) {
                    return true;
                }
            }
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public final int compareTo(Entry<?> other) {
        if (isFullDay() && other.isFullDay()) {
            return getStartDate().compareTo(other.getStartDate());
        }

        if (isFullDay()) {
            return -1;
        }

        if (other.isFullDay()) {
            return +1;
        }

        LocalDateTime a = LocalDateTime.of(getStartDate(), getStartTime());
        LocalDateTime b = LocalDateTime.of(other.getStartDate(), other.getStartTime());
        int result = a.compareTo(b);
        if (result == 0) {
            String titleA = getTitle() != null ? getTitle() : "";
            String titleB = other.getTitle() != null ? other.getTitle() : "";
            result = titleA.compareTo(titleB);
        }

        return result;
    }

    @Override
    public String toString() {
        return "Entry [title=" + getTitle() + ", id=" + getId() + ", fullDay="
                + isFullDay() + ", startDate=" + getStartDate() + ", endDate="
                + getEndDate() + ", startTime=" + getStartTime() + ", endTime="
                + getEndTime() + ", zoneId=" + getZoneId() + ", recurring = "
                + isRecurring() + ", rrule = " + getRecurrenceRule()
                + ", recurrence = " + isRecurrence() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((getRecurrenceId() == null) ? 0 : getRecurrenceId().hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Entry other = (Entry) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }

        String recId = getRecurrenceId();
        String otherRecId = other.getRecurrenceId();

        if (recId == null) {
            return otherRecId == null;
        }

        return recId.equals(otherRecId);
    }

    private static final String ENTRY_CATEGORY = "Entry";

    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = FXCollections.observableArrayList();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(calendarProperty());
            }

            @Override
            public Class<?> getType() {
                return Calendar.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Calendar";
            }

            @Override
            public String getDescription() {
                return "Calendar";
            }

            @Override
            public Object getValue() {
                return getCalendar();
            }

            @Override
            public void setValue(Object value) {
                setCalendar((Calendar) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(startTimeProperty());
            }

            @Override
            public Class<?> getType() {
                return LocalTime.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Start time";
            }

            @Override
            public String getDescription() {
                return "Start time";
            }

            @Override
            public Object getValue() {
                return getStartTime();
            }

            @Override
            public void setValue(Object value) {
                changeStartTime((LocalTime) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(endTimeProperty());
            }

            @Override
            public Class<?> getType() {
                return LocalTime.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "End time";
            }

            @Override
            public String getDescription() {
                return "End time";
            }

            @Override
            public Object getValue() {
                return getStartTime();
            }

            @Override
            public void setValue(Object value) {
                changeEndTime((LocalTime) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(startDateProperty());
            }

            @Override
            public Class<?> getType() {
                return LocalDate.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Start date";
            }

            @Override
            public String getDescription() {
                return "Start date";
            }

            @Override
            public Object getValue() {
                return getStartDate();
            }

            @Override
            public void setValue(Object value) {
                changeStartDate((LocalDate) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(endDateProperty());
            }

            @Override
            public Class<?> getType() {
                return LocalDate.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "End date";
            }

            @Override
            public String getDescription() {
                return "End date";
            }

            @Override
            public Object getValue() {
                return getEndDate();
            }

            @Override
            public void setValue(Object value) {
                changeEndDate((LocalDate) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(zoneIdProperty());
            }

            @Override
            public Class<?> getType() {
                return ZoneId.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Zone ID";
            }

            @Override
            public String getDescription() {
                return "Zone ID";
            }

            @Override
            public Object getValue() {
                return getZoneId();
            }

            @Override
            public void setValue(Object value) {
                setZoneId((ZoneId) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(titleProperty());
            }

            @Override
            public Class<?> getType() {
                return String.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Title";
            }

            @Override
            public String getDescription() {
                return "Title";
            }

            @Override
            public Object getValue() {
                return getTitle();
            }

            @Override
            public void setValue(Object value) {
                setTitle((String) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(fullDayProperty());
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Full Day";
            }

            @Override
            public String getDescription() {
                return "Full Day";
            }

            @Override
            public Object getValue() {
                return isFullDay();
            }

            @Override
            public void setValue(Object value) {
                setFullDay((boolean) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(locationProperty());
            }

            @Override
            public Class<?> getType() {
                return String.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Location";
            }

            @Override
            public String getDescription() {
                return "Geographic location (free text)";
            }

            @Override
            public Object getValue() {
                return getLocation();
            }

            @Override
            public void setValue(Object value) {
                setLocation((String) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(recurrenceRuleProperty());
            }

            @Override
            public Class<?> getType() {
                return String.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Recurrence Rule";
            }

            @Override
            public String getDescription() {
                return "RRULE";
            }

            @Override
            public Object getValue() {
                return getRecurrenceRule();
            }

            @Override
            public void setValue(Object value) {
                setRecurrenceRule((String) value);
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(minimumDurationProperty());
            }

            @Override
            public Class<?> getType() {
                return Duration.class;
            }

            @Override
            public String getCategory() {
                return ENTRY_CATEGORY;
            }

            @Override
            public String getName() {
                return "Minimum Duration";
            }

            @Override
            public String getDescription() {
                return "Minimum Duration";
            }

            @Override
            public Object getValue() {
                return getMinimumDuration();
            }

            @Override
            public void setValue(Object value) {
                setMinimumDuration((Duration) value);
            }
        });

        return items;
    }
}
