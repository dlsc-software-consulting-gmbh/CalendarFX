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

package com.calendarfx.google.view.task;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.view.data.GoogleCalendarData;
import com.calendarfx.google.view.log.ActionType;

import java.util.List;

/**
 * Search entries by text task.
 *
 * Created by gdiaz on 21/03/2017.
 */
public final class LoadEntriesByTextTask extends GoogleTask<List<GoogleEntry>> {

    private final String searchText;
    private final GoogleCalendar calendar;
    private final GoogleCalendarData data;
    private final GoogleAccount account;

    public LoadEntriesByTextTask(String searchText, GoogleCalendar calendar, GoogleCalendarData data, GoogleAccount account) {
        this.searchText = searchText;
        this.calendar = calendar;
        this.data = data;
        this.account = account;
        this.logItem.setCalendar(calendar.getName());
        this.logItem.setDescription(getDescription());
    }

    @Override
    public ActionType getAction() {
        return ActionType.LOAD;
    }

    @Override
    public String getDescription() {
        return "Loading \"" + searchText + "\"";
    }

    @Override
    protected List<GoogleEntry> call() throws Exception {
        return GoogleConnector.getInstance().getCalendarService(account.getId()).getEntries(calendar, searchText);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        for (GoogleEntry entry : getValue()) {
            if (!data.isLoadedEntry(entry)) {
                calendar.addEntry(entry);
                data.addLoadedEntry(entry);
            }
        }
        data.addLoadedSearchText(searchText);
    }
}
