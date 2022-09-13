/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *  Copyright (C) 2006 Google Inc.
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

package com.calendarfx.demo.views.resources;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.resources.DetailedResourcesDayView;
import com.calendarfx.view.resources.MultiResourceDayViewContainer;
import com.calendarfx.view.resources.Resource;

import java.time.LocalDate;
import java.time.LocalTime;

public class HelloMultiDayViewContainer extends CalendarFXDateControlSample {

    private MultiResourceDayViewContainer<Resource<String>> multiDayViewContainer;

    @Override
    public String getSampleName() {
        return "Multi Day View Container";
    }

    @Override
    public String getSampleDescription() {
        return "A specialized container for showing multiple DayView instances, one for each item added to it.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return DetailedResourcesDayView.class;
    }

    @Override
    protected DateControl createControl() {
        multiDayViewContainer = new MultiResourceDayViewContainer<>();
        multiDayViewContainer.getResources().addAll(create("Dirk"), create("Katja"), create("Philip"), create("Jule"), create("Armin"));
        multiDayViewContainer.setPrefHeight(800);
        return multiDayViewContainer;
    }

    private Resource<String> create(String name) {
        Resource<String> resource = new Resource(name);
        resource.getCalendar().addEventHandler(evt -> System.out.println(evt));
        resource.getAvailabilityCalendar().addEventHandler(evt -> System.out.println(evt));
        return resource;
    }

    class HelloCalendar extends Calendar {

        public HelloCalendar() {
            LocalDate date = LocalDate.now();

            for (int i = 1; i < 3; i++) {

                Entry<?> entry = new Entry<>();
                entry.changeStartDate(date);
                entry.changeEndDate(date);

                entry.setTitle("Entry " + i);

                int hour = (int) (Math.random() * 23);
                int durationInHours = Math.min(24 - hour,
                        (int) (Math.random() * 4));

                LocalTime startTime = LocalTime.of(hour, 0);
                LocalTime endTime = startTime.plusHours(durationInHours);

                entry.changeStartTime(startTime);
                entry.changeEndTime(endTime);

                if (Math.random() < .1) {
                    entry.setFullDay(true);
                    entry.setTitle("Full Day Entry");
                }

                entry.setCalendar(this);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
