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

package com.calendarfx.google.model;

import com.calendarfx.model.Entry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import impl.com.calendarfx.view.util.Util;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Custom entry representing a google event. This contains all the required
 * information for a single event of google calendar.
 *
 * @author Gabriel Diaz, 07.03.2015.
 */
public class GoogleEntry extends Entry<Event> {

    public GoogleEntry() {
        super();
        reminders.addListener((ListChangeListener<GoogleEntryReminder>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (GoogleEntryReminder r : c.getAddedSubList()) {
                        r.addListener(remindersListener);
                    }
                }

                if (c.wasRemoved()) {
                    for (GoogleEntryReminder r : c.getRemoved()) {
                        r.removeListener(remindersListener);
                    }
                }
            }
        });
        reminders.addListener(remindersListener);
        attendees.addListener((Observable obs) -> {
            if (getCalendar() instanceof GoogleCalendar) {
                getCalendar().fireEvent(new GoogleCalendarEvent(
                        GoogleCalendarEvent.ENTRY_ATTENDEES_CHANGED, (GoogleCalendar) getCalendar(), GoogleEntry.this));
            }
        });
    }

    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(this, "status");

    public final ObjectProperty<Status> statusProperty() {
        return status;
    }

    public final Status getStatus() {
        return statusProperty().get();
    }

    public final void setStatus(Status status) {
        statusProperty().set(status);
    }

    private final BooleanProperty attendeesCanModify = new SimpleBooleanProperty(this, "attendeesCanModify") {
        @Override
        public void set(boolean newValue) {
            boolean oldValue = get();

            if (!Util.equals(oldValue, newValue)) {
                super.set(newValue);

                if (getCalendar() instanceof GoogleCalendar) {
                    getCalendar().fireEvent(new GoogleCalendarEvent(
                            GoogleCalendarEvent.ENTRY_ATTENDEES_CAN_MODIFY_CHANGED,
                            (GoogleCalendar) getCalendar(), GoogleEntry.this));
                }

                if (newValue) {
                    setAttendeesCanInviteOthers(true);
                    setAttendeesCanSeeOthers(true);
                }
            }
        }
    };

    public final BooleanProperty attendeesCanModifyProperty() {
        return attendeesCanModify;
    }

    public final boolean isAttendeesCanModify() {
        return attendeesCanModifyProperty().get();
    }

    public final void setAttendeesCanModify(boolean attendeesCanModify) {
        attendeesCanModifyProperty().set(attendeesCanModify);
    }

    private final BooleanProperty attendeesCanInviteOthers = new SimpleBooleanProperty(
            this, "attendeesCanInviteOthers", true) {

        @Override
        public void set(boolean newValue) {
            boolean oldValue = get();

            if (!Util.equals(oldValue, newValue)) {
                super.set(newValue);

                if (getCalendar() instanceof GoogleCalendar) {
                    getCalendar().fireEvent(new GoogleCalendarEvent(
                            GoogleCalendarEvent.ENTRY_ATTENDEES_CAN_INVITE_CHANGED,
                            (GoogleCalendar) getCalendar(), GoogleEntry.this));
                }
            }
        }
    };

    public final BooleanProperty attendeesCanInviteOthersProperty() {
        return attendeesCanInviteOthers;
    }

    public final boolean isAttendeesCanInviteOthers() {
        return attendeesCanInviteOthersProperty().get();
    }

    public final void setAttendeesCanInviteOthers(
            boolean attendeesCanInviteOthers) {
        attendeesCanInviteOthersProperty().set(attendeesCanInviteOthers);
    }

    private final BooleanProperty attendeesCanSeeOthers = new SimpleBooleanProperty(
            this, "attendeesCanSeeOthers", true) {

        @Override
        public void set(boolean newValue) {
            boolean oldValue = get();

            if (!Util.equals(oldValue, newValue)) {
                super.set(newValue);

                if (getCalendar() instanceof GoogleCalendar) {
                    getCalendar().fireEvent(new GoogleCalendarEvent(
                            GoogleCalendarEvent.ENTRY_ATTENDEES_CAN_SEE_OTHERS_CHANGED,
                            (GoogleCalendar) getCalendar(), GoogleEntry.this));
                }
            }
        }
    };

    public final BooleanProperty attendeesCanSeeOthersProperty() {
        return attendeesCanSeeOthers;
    }

    public final boolean isAttendeesCanSeeOthers() {
        return attendeesCanSeeOthersProperty().get();
    }

    public final void setAttendeesCanSeeOthers(boolean attendeesCanSeeOthers) {
        attendeesCanSeeOthersProperty().set(attendeesCanSeeOthers);
    }

    private final BooleanProperty useDefaultReminder = new SimpleBooleanProperty(this, "useDefaultReminder");

    public final BooleanProperty useDefaultReminderProperty() {
        return useDefaultReminder;
    }

    public final boolean isUseDefaultReminder() {
        return useDefaultReminderProperty().get();
    }

    public final void setUseDefaultReminder(boolean useDefaultReminder) {
        useDefaultReminderProperty().set(useDefaultReminder);
    }

    private final ObservableList<EventAttendee> attendees = FXCollections.observableArrayList();

    public ObservableList<EventAttendee> getAttendees() {
        return attendees;
    }

    private final InvalidationListener remindersListener = obs -> {
        if (getCalendar() instanceof GoogleCalendar) {
            GoogleCalendar calendar = (GoogleCalendar) getCalendar();
            calendar.fireEvent(new GoogleCalendarEvent(GoogleCalendarEvent.ENTRY_REMINDERS_CHANGED, calendar, GoogleEntry.this));
        }
    };

    private final ObservableList<GoogleEntryReminder> reminders = FXCollections.observableArrayList();

    public ObservableList<GoogleEntryReminder> getReminders() {
        return reminders;
    }

    @Override
    public GoogleEntry createRecurrence() {
        GoogleEntry entry = new GoogleEntry();
        entry.setStatus(getStatus());
        entry.setAttendeesCanModify(isAttendeesCanModify());
        entry.setAttendeesCanInviteOthers(isAttendeesCanInviteOthers());
        entry.setAttendeesCanSeeOthers(isAttendeesCanSeeOthers());
        entry.getAttendees().setAll(getAttendees());
        entry.getReminders().setAll(getReminders());
        return entry;
    }

    /**
     * Indicates whether the entry exist in google calendar or not.
     *
     * @return a flag saying whether this entry was already persisted or not.
     */
    public final boolean existsInGoogle() {
        return getUserObject() != null;
    }

    @Override
    public String toString() {
        return "Google Entry: " + getTitle();
    }

    /**
     * Enumeration representing the status of the google entry.
     *
     * @author Gabriel Diaz, 22.03.2015.
     */
    public enum Status {

        CONFIRMED("confirmed"),

        TENTATIVE("tentative"),

        CANCELLED("cancelled");

        private String name;

        Status(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Status fromName(String name) {
            for (Status status : values()) {
                if (status.getName().equals(name)) {
                    return status;
                }
            }

            return null;
        }

    }

}
