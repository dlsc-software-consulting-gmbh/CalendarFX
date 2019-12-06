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

import java.util.List;

/**
 * Task that queries all calendars from google and updates the google calendar source.
 *
 * Created by gdiaz on 28/02/2017.
 */
public final class LoadAllCalendarsTask extends GoogleTask<List<GoogleCalendar>> {

    private final GoogleAccount account;

    public LoadAllCalendarsTask(GoogleAccount account) {
        super();
        this.account = account;
    }

    @Override
    public ActionType getAction() {
        return ActionType.LOAD;
    }

    @Override
    public String getDescription() {
        return "Loading all calendars";
    }

    @Override
    protected List<GoogleCalendar> call() throws Exception {
        return GoogleConnector.getInstance().getCalendarService(account.getId()).getCalendars();
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        account.getCalendars().setAll(getValue());
    }
}
