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

package com.calendarfx.appointments;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Resource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Creates the demo data of the appointments application: the stylists working
 * in the salon, their working hours, and their customer appointments.
 */
public final class SalonData {

    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);

    public static final LocalTime CLOSING_TIME = LocalTime.of(19, 0);

    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);

    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);

    private static final int DATA_GENERATION_SEED = 11011;

    private static final String[] CUSTOMERS = {"Anna Holm", "Anne Carlson", "Helen Brown", "Hans", "Fred", "Adam Berg", "Matthew", "Sebastian", "George Larson", "Carin Olson", "Justine", "Lars K"};

    private static final String[] TREATMENTS = {"Long hair", "Short hair", "Cut & color", "Eyebrow tinting", "Lash lift", "Manicure & pedicure", "Kids hair"};

    private static final int[] DURATIONS = {30, 45, 60, 75, 90};

    private SalonData() {
    }

    /**
     * Creates the stylists of the salon, each one with its working hours and a
     * calendar filled with customer appointments.
     *
     * @return the list of stylists working in the salon
     */
    public static List<Resource<Stylist>> createStylists() {
        Random random = new Random(DATA_GENERATION_SEED);

        List<Resource<Stylist>> result = new ArrayList<>();
        result.add(createStylist(new Stylist("Amy Fowler", "amy"), random));
        result.add(createStylist(new Stylist("Howard", "howard"), random));
        result.add(createStylist(new Stylist("Bernadette", "bernadette"), random));
        return result;
    }

    private static Resource<Stylist> createStylist(Stylist stylist, Random random) {
        Resource<Stylist> resource = new Resource<>(stylist);

        Calendar availability = resource.getAvailabilityCalendar();
        availability.setName("Working hours of " + stylist.name());
        fillWorkingHours(availability);

        Calendar appointments = resource.getCalendars().get(0);
        appointments.setName("Appointments of " + stylist.name());
        appointments.setShortName(stylist.name());
        appointments.setStyle(stylist.style());
        fillAppointments(appointments, random);

        return resource;
    }

    /**
     * The availability calendar stores the times when a stylist is *not*
     * available: before the salon opens, during lunch, and after it closes.
     */
    private static void fillWorkingHours(Calendar calendar) {
        forEachDayOfDemoPeriod(date -> {
            addEntry(calendar, "Closed", date, LocalTime.MIN, OPENING_TIME);
            addEntry(calendar, "Lunch", date, LUNCH_START, LUNCH_END);
            addEntry(calendar, "Closed", date, CLOSING_TIME, LocalTime.MAX);

            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                addEntry(calendar, "Closed", date, LocalTime.MIN, LocalTime.MAX);
            }
        });
    }

    private static void fillAppointments(Calendar calendar, Random random) {
        forEachDayOfDemoPeriod(date -> {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return;
            }

            LocalTime time = OPENING_TIME.plusMinutes(60L + 15 * random.nextInt(8));

            while (time.isBefore(CLOSING_TIME.minusHours(1))) {
                int duration = DURATIONS[random.nextInt(DURATIONS.length)];

                if (time.isBefore(LUNCH_END) && time.plusMinutes(duration).isAfter(LUNCH_START)) {
                    time = LUNCH_END;
                }

                String customer = CUSTOMERS[random.nextInt(CUSTOMERS.length)];
                String treatment = TREATMENTS[random.nextInt(TREATMENTS.length)];

                addEntry(calendar, customer + " - " + treatment, date, time, time.plusMinutes(duration));

                // leave a gap of 0 to 75 minutes before the next appointment
                time = time.plusMinutes(duration + 15L * random.nextInt(6));
            }
        });
    }

    private static void forEachDayOfDemoPeriod(Consumer<LocalDate> consumer) {
        LocalDate date = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        for (int i = 0; i < 28; i++) {
            consumer.accept(date);
            date = date.plusDays(1);
        }
    }

    private static void addEntry(Calendar calendar, String title, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Entry<?> entry = new Entry<>(title);
        entry.setInterval(date, startTime, date, endTime);
        calendar.addEntry(entry);
    }
}
