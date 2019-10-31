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
import com.calendarfx.google.view.data.Slice;
import com.calendarfx.google.view.log.ActionType;

import java.time.ZoneId;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Task that loads some entries from google for one single calendar using the period provided by some slices.
 *
 * Created by gdiaz on 6/03/2017.
 */
public final class LoadEntriesBySliceTask extends GoogleTask<List<GoogleEntry>> {

    private final GoogleAccount account;
    private final GoogleCalendar calendar;
    private final GoogleCalendarData entryData;
    private final Slice slice;
    private final ZoneId zoneId;

    public LoadEntriesBySliceTask(GoogleAccount account, GoogleCalendar calendar, GoogleCalendarData entryData, Slice slice, ZoneId zoneId) {
        super();
        this.account = checkNotNull(account);
        this.calendar = checkNotNull(calendar);
        this.entryData = checkNotNull(entryData);
        this.slice = checkNotNull(slice);
        this.zoneId = checkNotNull(zoneId);
        this.logItem.setCalendar(calendar.getName());
        this.logItem.setDescription(getDescription());
    }

    @Override
    public ActionType getAction() {
        return ActionType.LOAD;
    }

    @Override
    public String getDescription() {
        return "Loading " + slice;
    }

    @Override
    protected List<GoogleEntry> call() throws Exception {
        return GoogleConnector.getInstance().getCalendarService(account.getId()).getEntries(calendar, slice.getStart(), slice.getEnd(), zoneId);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        for (GoogleEntry entry : getValue()) {
            if (!entryData.isLoadedEntry(entry)) {
                calendar.addEntry(entry);
                entryData.addLoadedEntry(entry);
            }
        }
        entryData.addLoadedSlice(slice);
    }

}
