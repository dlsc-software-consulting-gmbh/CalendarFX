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
import com.calendarfx.google.view.log.ActionType;

/**
 * Moves an entry from one calendar to another.
 *
 * Created by gdiaz on 19/03/2017.
 */
public final class MoveEntryTask extends GoogleTask<GoogleEntry> {

    private final GoogleEntry entry;
    private final GoogleCalendar from;
    private final GoogleCalendar to;
    private final GoogleAccount account;

    public MoveEntryTask(GoogleEntry entry, GoogleCalendar from, GoogleCalendar to, GoogleAccount account) {
        this.entry = entry;
        this.from = from;
        this.to = to;
        this.account = account;
        this.logItem.setCalendar(from.getName());
        this.logItem.setDescription(getDescription());
    }

    @Override
    public ActionType getAction() {
        return ActionType.MOVE;
    }

    @Override
    public String getDescription() {
        return "Moving " + entry + " to " + to;
    }

    @Override
    protected GoogleEntry call() throws Exception {
        return GoogleConnector.getInstance().getCalendarService(account.getId()).moveEntry(entry, from, to);
    }
}
