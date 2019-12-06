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
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.view.log.ActionType;

import java.util.Map;

/**
 * Task that updates one entry in google.
 *
 * Created by gdiaz on 12/03/2017.
 */
public final class UpdateEntryTask extends GoogleTask<GoogleEntry> {

    private static final long TWO_SECONDS = 2 * 1000;

    private GoogleEntry entry;
    private final GoogleAccount account;
    private final Map<GoogleEntry, UpdateEntryTask> updateTasks;

    public UpdateEntryTask(GoogleEntry entry, GoogleAccount account, Map<GoogleEntry, UpdateEntryTask> updateTasks) {
        this.entry = entry;
        this.account = account;
        this.updateTasks = updateTasks;
        this.logItem.setCalendar(entry.getCalendar().getName());
        this.logItem.setDescription(getDescription());
    }

    public void append(GoogleEntry newVersion) {
        assert (entry.equals(newVersion));
        this.entry = newVersion;
        this.logItem.setDescription(getDescription());
    }

    @Override
    public ActionType getAction() {
        return ActionType.UPDATE;
    }

    @Override
    public String getDescription() {
        return "Update " + entry;
    }

    @Override
    protected GoogleEntry call() throws Exception {
        Thread.sleep(TWO_SECONDS);
        return GoogleConnector.getInstance().getCalendarService(account.getId()).updateEntry(entry);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateTasks.remove(entry);
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateTasks.remove(entry);
    }

    @Override
    protected void failed() {
        super.failed();
        updateTasks.remove(entry);
    }
}
