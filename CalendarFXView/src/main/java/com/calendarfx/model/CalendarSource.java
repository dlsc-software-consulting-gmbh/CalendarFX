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

import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.SourceView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

import static com.calendarfx.util.LoggingDomain.MODEL;
import static java.util.logging.Level.FINE;

/**
 * A calendar source is a collection of calendars. It often represents a user
 * account with some calendar service, e.g. Google Calendar or Apple me.com. The
 * image below shows an example: a calendar source called "Work" with calendars
 * "Meetings, Training, Customers, Holidays".
 * <p/>
 *
 * <img src="doc-files/calendar-source.png">
 * <p/>
 *
 * Calendar sources can be shown to the user via the {@link SourceView} control.
 */
public class CalendarSource {

    /**
     * Constructs a new untitled calendar source.
     */
    public CalendarSource() {
        if (MODEL.isLoggable(FINE)) {
            getCalendars().addListener(
                    (Change<? extends Calendar> change) -> {
                        while (change.next()) {
                            if (change.wasAdded()) {
                                for (Calendar calendar : change.getAddedSubList()) {
                                    LoggingDomain.MODEL.fine("added calendar " + calendar.getName() + " to source " //$NON-NLS-1$ //$NON-NLS-2$
                                            + getName());
                                }
                            } else if (change.wasRemoved()) {
                                for (Calendar calendar : change.getRemoved()) {
                                    MODEL.fine("removed calendar " + calendar.getName() + " from source " + getName()); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Constructs a new calendar source with the given name.
     *
     * @param name
     *            the name of the calendar source, e.g. "Google", "Apple"
     */
    public CalendarSource(String name) {
        this();
        setName(name);
    }

    private final StringProperty name = new SimpleStringProperty(this, "name", "Untitled"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * The property used to store the name of the calendar source.
     *
     * @return the name property
     */
    public final StringProperty nameProperty() {
        return name;
    }

    /**
     * Sets the value of {@link #nameProperty()}.
     *
     * @param name
     *            the new name for the calendar source
     */
    public final void setName(String name) {
        MODEL.fine("changing name to " + name); //$NON-NLS-1$
        nameProperty().set(name);
    }

    /**
     * Returns the value fo {@link #nameProperty()}.
     *
     * @return the calendar source name
     */
    public final String getName() {
        return nameProperty().get();
    }

    private final ObservableList<Calendar> calendars = FXCollections.observableArrayList();

    /**
     * Returns the list of calendars that belong to this calendar source.
     * Example: the calendar source is "Google" and calendars might be "Work",
     * "Home", "Sport", "Children".
     *
     * @return the calendars owned by this calendar source
     */
    public final ObservableList<Calendar> getCalendars() {
        return calendars;
    }

    @Override
    public String toString() {
        return "CalendarSource [name=" + getName() + ", calendars=" + calendars + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
