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

package impl.com.calendarfx.google.view;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleCalendarEvent;
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.service.SecurityService;
import com.calendarfx.google.view.task.DeleteEntryTask;
import com.calendarfx.google.view.task.InsertEntryTask;
import com.calendarfx.google.view.task.MoveEntryTask;
import com.calendarfx.google.view.task.UpdateEntryTask;
import com.calendarfx.google.view.thread.GoogleTaskExecutor;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Class in charge of receiving calendar events and synchronize data between the app and google.
 *
 * Created by gdiaz on 06/03/2017.
 */
final class GoogleSyncManager implements EventHandler<CalendarEvent>, ListChangeListener<Calendar> {

    private final Map<GoogleEntry, UpdateEntryTask> updateTasks = new HashMap<>();

    @Override
    public void onChanged(Change<? extends Calendar> c) {
        while (c.next()) {
            for (Calendar calendar : c.getRemoved()) {
                calendar.removeEventHandler(this);
            }

            for (Calendar calendar : c.getAddedSubList()) {
                calendar.addEventHandler(this);
            }
        }
    }

    @Override
    public void handle(CalendarEvent evt) {
        if (SecurityService.getInstance().isLoggedIn()) {
            GoogleAccount account = SecurityService.getInstance().getLoggedAccount();

            if (requiresInsertEntry(evt)) {
                insertEntry(evt, account);
            } else if (requiresDeleteEntry(evt)) {
                deleteEntry(evt, account);
            } else if (requiresUpdateEntry(evt)) {
                updateEntry(evt, account);
            } else if (requiresMoveEntry(evt)) {
                moveEntry(evt, account);
            }
        }
    }

    private void insertEntry(CalendarEvent evt, GoogleAccount account) {
        GoogleEntry entry = (GoogleEntry) evt.getEntry();
        GoogleCalendar calendar = (GoogleCalendar) evt.getCalendar();
        GoogleTaskExecutor.getInstance().execute(new InsertEntryTask(entry, calendar, account));
    }

    private void updateEntry(CalendarEvent evt, GoogleAccount account) {
        GoogleEntry entry = (GoogleEntry) evt.getEntry();
        UpdateEntryTask updateTask = updateTasks.get(entry);
        if (updateTask == null) {
            updateTask = new UpdateEntryTask(entry, account, updateTasks);
            updateTasks.put(entry, updateTask);
            GoogleTaskExecutor.getInstance().execute(updateTask);
        } else {
            updateTask.append(entry);
        }
    }

    private void deleteEntry(CalendarEvent evt, GoogleAccount account) {
        GoogleEntry entry = (GoogleEntry) evt.getEntry();
        GoogleCalendar calendar = (GoogleCalendar) evt.getOldCalendar();
        GoogleTaskExecutor.getInstance().execute(new DeleteEntryTask(entry, calendar, account));
    }

    private void moveEntry(CalendarEvent evt, GoogleAccount account) {
        GoogleEntry entry = (GoogleEntry) evt.getEntry();
        GoogleCalendar from = (GoogleCalendar) evt.getOldCalendar();
        GoogleCalendar to = (GoogleCalendar) evt.getCalendar();
        GoogleTaskExecutor.getInstance().execute(new MoveEntryTask(entry, from, to, account));
    }

    private boolean requiresInsertEntry(CalendarEvent evt) {
        if (evt != null && evt.getEventType().equals(GoogleCalendarEvent.ENTRY_CALENDAR_CHANGED) && evt.getCalendar() != null && evt.getOldCalendar() == null) {
            Entry<?> entry = evt.getEntry();
            if (entry instanceof GoogleEntry && !entry.isRecurrence()) {
                return !((GoogleEntry) entry).existsInGoogle();
            }
        }
        return false;
    }

    private boolean requiresDeleteEntry(CalendarEvent evt) {
        if (evt != null && evt.getEventType().equals(GoogleCalendarEvent.ENTRY_CALENDAR_CHANGED) && evt.getCalendar() == null && evt.getOldCalendar() != null) {
            Entry<?> entry = evt.getEntry();
            if (entry instanceof GoogleEntry && !entry.isRecurrence()) {
                return ((GoogleEntry) entry).existsInGoogle();
            }
        }
        return false;
    }

    private boolean requiresUpdateEntry(CalendarEvent evt) {
        if (evt != null && !evt.getEventType().equals(GoogleCalendarEvent.ENTRY_CALENDAR_CHANGED) && evt.getEventType().getSuperType().equals(GoogleCalendarEvent.ENTRY_CHANGED)) {
            Entry<?> entry = evt.getEntry();
            if (entry instanceof GoogleEntry && !entry.isRecurrence()) {
                return ((GoogleEntry) entry).existsInGoogle();
            }
        }
        return false;
    }

    private boolean requiresMoveEntry(CalendarEvent evt) {
        if (evt != null && evt.getEventType().equals(GoogleCalendarEvent.ENTRY_CALENDAR_CHANGED) && evt.getCalendar() != null && evt.getOldCalendar() != null) {
            Entry<?> entry = evt.getEntry();
            if (entry instanceof GoogleEntry && !entry.isRecurrence()) {
                return ((GoogleEntry) entry).existsInGoogle();
            }
        }
        return false;
    }

}
