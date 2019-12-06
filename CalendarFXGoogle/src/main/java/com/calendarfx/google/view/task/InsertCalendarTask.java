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
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.view.log.ActionType;

/**
 * Task that performs an insert operation into google.
 *
 * Created by gdiaz on 06.03.2017.
 */
public final class InsertCalendarTask extends GoogleTask<GoogleCalendar> {

    private final GoogleCalendar calendar;
    private final GoogleAccount account;

    public InsertCalendarTask(GoogleCalendar calendar, GoogleAccount account) {
        this.calendar = calendar;
        this.account = account;
        this.logItem.setDescription(getDescription());
    }

    @Override
    public ActionType getAction() {
        return ActionType.INSERT;
    }

    @Override
    public String getDescription() {
        return "Insert " + calendar;
    }

    @Override
    protected GoogleCalendar call() throws Exception {
        GoogleConnector.getInstance().getCalendarService(account.getId()).insertCalendar(calendar);
        return calendar;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        account.getCalendars().add(calendar);
    }
}
