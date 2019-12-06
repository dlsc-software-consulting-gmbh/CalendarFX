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
import com.calendarfx.google.service.SecurityService;
import com.calendarfx.google.view.data.GoogleCalendarData;
import com.calendarfx.google.view.data.IGoogleCalendarDataProvider;
import com.calendarfx.google.view.data.Slice;
import com.calendarfx.google.view.task.LoadEntriesBySliceTask;
import com.calendarfx.google.view.thread.GoogleTaskExecutor;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.LoadEvent;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader class for calendars and entries.
 *
 * Created by gdiaz on 27/02/2017.
 */
final class GoogleCalendarDataManager implements IGoogleCalendarDataProvider, ListChangeListener<Calendar>, EventHandler<LoadEvent> {

    private final Map<GoogleCalendar, GoogleCalendarData> calendarsData = new HashMap<>();

    GoogleCalendarDataManager() {
        super();
    }

    @Override
    public GoogleCalendarData getCalendarData(GoogleCalendar calendar, boolean create) {
        GoogleCalendarData data = calendarsData.get(calendar);
        if (data == null && create) {
            data = new GoogleCalendarData();
            calendarsData.put(calendar, data);
        }
        return data;
    }

    @Override
    public void removeCalendarData(GoogleCalendar calendar) {
        calendarsData.remove(calendar);
    }

    @Override
    public void clearData() {
        calendarsData.clear();
    }

    @Override
    public void onChanged(Change<? extends Calendar> c) {
        List<GoogleCalendar> removed = new ArrayList<>();

        while (c.next()) {
            for (Calendar calendar : c.getRemoved()) {
                if (calendar instanceof GoogleCalendar) {
                    removed.add((GoogleCalendar) calendar);
                }
            }

            for (Calendar calendar : c.getAddedSubList()) {
                if (calendar instanceof GoogleCalendar) {
                    removed.remove(calendar);
                }
            }
        }

        for (GoogleCalendar calendar : removed) {
            removeCalendarData(calendar);
        }
    }

    @Override
    public void handle(LoadEvent evt) {
        if (SecurityService.getInstance().isLoggedIn()) {
            List<Slice> slices = Slice.split(evt.getStartDate(), evt.getEndDate());

            if (!slices.isEmpty()) {
                ZoneId zoneId = evt.getZoneId();
                GoogleAccount account = SecurityService.getInstance().getLoggedAccount();

                for (GoogleCalendar cal : account.getGoogleCalendars()) {
                    GoogleCalendarData data = getCalendarData(cal, true);
                    List<Slice> unloaded = data.getUnloadedSlices(slices);
                    if (!unloaded.isEmpty()) {
                        data.addInProgressSlices(unloaded);
                        for (Slice us : unloaded) {
                            LoadEntriesBySliceTask task = new LoadEntriesBySliceTask(account, cal, data, us, zoneId);
                            GoogleTaskExecutor.getInstance().execute(task);
                        }
                    }
                }
            }
        }
    }

}
