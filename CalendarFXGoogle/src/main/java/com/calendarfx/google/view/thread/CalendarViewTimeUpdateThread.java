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

package com.calendarfx.google.view.thread;

import com.calendarfx.view.CalendarView;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Thread that updates the current tine on the calendar view
 *
 * Created by gdiaz on 4/05/2017.
 */
public class CalendarViewTimeUpdateThread extends Thread {

    private static final int TEN_SECONDS = 10000;

    private final CalendarView calendarView;

    public CalendarViewTimeUpdateThread(CalendarView calendarView) {
        super("Google-Calendar-Update Current Time");
        this.calendarView = calendarView;
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            Platform.runLater(() -> {
                calendarView.setToday(LocalDate.now());
                calendarView.setTime(LocalTime.now());
            });

            try {
                sleep(TEN_SECONDS);
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
    }

}
