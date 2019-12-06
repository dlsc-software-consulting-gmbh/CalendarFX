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
import com.calendarfx.google.model.IGoogleCalendarSearchTextProvider;
import com.calendarfx.google.service.SecurityService;
import com.calendarfx.google.view.data.GoogleCalendarData;
import com.calendarfx.google.view.data.IGoogleCalendarDataProvider;
import com.calendarfx.google.view.task.LoadEntriesByTextTask;
import com.calendarfx.google.view.thread.GoogleTaskExecutor;
import com.calendarfx.model.Calendar;
import javafx.collections.ListChangeListener;

/**
 * Default implementation of the provider that searches entries in google synchronously.
 *
 * Created by gdiaz on 5/05/2017.
 */
final class GoogleCalendarSearchTextManager implements IGoogleCalendarSearchTextProvider, ListChangeListener<Calendar> {

    private final IGoogleCalendarDataProvider provider;

    GoogleCalendarSearchTextManager(IGoogleCalendarDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void search(GoogleCalendar calendar, String text) {
        if (SecurityService.getInstance().isLoggedIn()) {
            GoogleCalendarData data = provider.getCalendarData(calendar, true);
            if (!data.isLoadedSearchText(text)) {
                GoogleAccount account = SecurityService.getInstance().getLoggedAccount();
                LoadEntriesByTextTask task = new LoadEntriesByTextTask(text, calendar, data, account);
                GoogleTaskExecutor.getInstance().executeImmediate(task);
            }
        }
    }

    @Override
    public void onChanged(Change<? extends Calendar> c) {
        while (c.next()) {
            for (Calendar calendar : c.getRemoved()) {
                if (calendar instanceof GoogleCalendar) {
                    ((GoogleCalendar) calendar).setSearchTextProvider(null);
                }
            }

            for (Calendar calendar : c.getAddedSubList()) {
                if (calendar instanceof GoogleCalendar) {
                    ((GoogleCalendar) calendar).setSearchTextProvider(this);
                }
            }
        }
    }
}
