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

package com.calendarfx.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class CalendarTest {

    private final Calendar calendar = new Calendar();
    private Entry<String> recurrenceSourceEntry;
    private Entry<String> recurrence;

    @Before
    public void setup() {
        LocalDate today = LocalDate.now();
        recurrenceSourceEntry = new Entry<>("Recurrence Source");
        recurrenceSourceEntry.setInterval(recurrenceSourceEntry.getInterval().withEndDate(LocalDate.now().plusDays(30)));
        recurrenceSourceEntry.setRecurrenceRule("RRULE:FREQ=DAILY;");
        recurrenceSourceEntry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(today, today, ZoneId.systemDefault());
        recurrence = (Entry<String>) entries.get(today).get(0);
    }

    @Test
    public void shouldUpdateRecurrenceSourceTitle() {
        // given
        String newTitle = "New Title";

        // when
        recurrence.setTitle(newTitle);

        // then
        assertThat(recurrenceSourceEntry.getTitle(), is(equalTo(newTitle)));
    }

    @Test
    public void shouldUpdateRecurrenceSourceRecurrenceRule() {
        // given
        String newRule = "RRULE:FREQ=WEEKLY;";

        // when
        recurrence.setRecurrenceRule(newRule);

        // then
        assertThat(recurrenceSourceEntry.getRecurrenceRule(),
                is(equalTo(newRule)));
    }

    @Test
    public void shouldUpdateRecurrenceSourceInterval() {
        // given
        Interval oldInterval = recurrenceSourceEntry.getInterval();
        Interval newInterval = new Interval(LocalDate.now().plusDays(1),
                LocalTime.of(10, 00), LocalDate.now().plusDays(1),
                LocalTime.of(13, 45), ZoneId.of("UTC"));

        // when
        recurrence.setInterval(newInterval);

        // then
        assertThat(newInterval, is(not(equalTo(oldInterval))));
    }

    @Test
    public void shouldUpdateRecurrenceSourceCalendar() {
        // given
        Calendar newCalendar = new Calendar();

        // when
        recurrence.setCalendar(newCalendar);

        // then
        assertThat(recurrenceSourceEntry.getCalendar(),
                is(equalTo(newCalendar)));
    }

    @Test
    public void shouldUpdateRecurrenceSourceFullDay() {
        // given
        recurrenceSourceEntry.setFullDay(false);

        // when
        recurrence.setFullDay(true);

        // then
        assertThat(recurrenceSourceEntry.isFullDay(), is(true));
    }

    @Test
    public void shouldUpdateRecurrenceSourceLocation() {
        // given
        String newLocation = "New York";

        // when
        recurrence.setLocation(newLocation);

        // then
        assertThat(recurrenceSourceEntry.getLocation(), is(equalTo(newLocation)));
    }

    @Test
    public void shouldUpdateRecurrenceSourceUserObject() {
        // given
        String newUserObject = "My User Object";

        // when
        recurrence.setUserObject(newUserObject);

        // then
        assertThat(recurrenceSourceEntry.getUserObject(), is(equalTo(newUserObject)));
    }

    @Test
    public void shouldReturnEntriesForSearch() throws Exception {
        // given
        Entry<?> entry1 = new Entry<>("xxxAAA");
        Entry<?> entry2 = new Entry<>("AAAxXx");
        Entry<?> entry3 = new Entry<>("AAxXXAAA");
        Entry<?> entryA = new Entry<>("ttTAAA");
        Entry<?> entryB = new Entry<>("AAATTt");
        Entry<?> entryC = new Entry<>("AAAtttAAA");

        entry1.setCalendar(calendar);
        entryA.setCalendar(calendar);
        entry2.setCalendar(calendar);
        entryB.setCalendar(calendar);
        entry3.setCalendar(calendar);
        entryC.setCalendar(calendar);

        // when
        List<Entry<?>> entries = calendar.findEntries("xxx");

        // then
        assertThat(entries.size(), is(equalTo(3)));
        assertThat(entries.contains(entry1), is(true));
        assertThat(entries.contains(entry2), is(true));
        assertThat(entries.contains(entry3), is(true));
        assertThat(entries.contains(entryA), is(false));
        assertThat(entries.contains(entryB), is(false));
        assertThat(entries.contains(entryC), is(false));
    }

    @Test
    public void shouldReturnEntriesForTimeInterval() throws Exception {
        // given
        Entry<?> entry1 = new Entry<>();
        Entry<?> entry2 = new Entry<>();
        Entry<?> entry3 = new Entry<>();
        Entry<?> entryA = new Entry<>();
        Entry<?> entryB = new Entry<>();
        Entry<?> entryC = new Entry<>();

        entry1.changeStartDate(LocalDate.now());
        entry1.changeEndDate(LocalDate.now().plusDays(10));

        entry2.changeStartDate(LocalDate.now().plusDays(7));
        entry2.changeEndDate(LocalDate.now().plusDays(9));

        entry3.changeStartDate(LocalDate.now().plusDays(4));
        entry3.changeEndDate(LocalDate.now().plusDays(6));

        entryA.changeStartDate(LocalDate.now().minusDays(7));
        entryA.changeEndDate(LocalDate.now().minusDays(10));

        entryB.changeStartDate(LocalDate.now().minusDays(7));
        entryB.changeEndDate(LocalDate.now().minusDays(9));

        entryC.changeStartDate(LocalDate.now().plusDays(40));
        entryC.changeEndDate(LocalDate.now().plusDays(60));

        entry1.setCalendar(calendar);
        entryA.setCalendar(calendar);
        entry2.setCalendar(calendar);
        entryB.setCalendar(calendar);
        entry3.setCalendar(calendar);
        entryC.setCalendar(calendar);

        // when
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(
                LocalDate.now(), LocalDate.now().plusDays(10),
                ZoneId.systemDefault());

        // then
        assertThat(entries.keySet().size(), is(equalTo(11)));

        // entry1 is 11 days long: 0, 1, 2, ..., 10
        for (int i = 0; i <= 10; i++) {
            assertThat(
                    entries.get(LocalDate.now().plusDays(i)).contains(entry1),
                    is(true));

            // A, B, C do not show up on any of these days
            assertThat(
                    entries.get(LocalDate.now().plusDays(i)).contains(entryA),
                    is(false));
            assertThat(
                    entries.get(LocalDate.now().plusDays(i)).contains(entryB),
                    is(false));
            assertThat(
                    entries.get(LocalDate.now().plusDays(i)).contains(entryC),
                    is(false));
        }

        // entry2 is 3 days long: 7, 8, 9
        for (int i = 7; i <= 9; i++) {
            assertThat(
                    entries.get(LocalDate.now().plusDays(i)).contains(entry2),
                    is(true));
        }

        // entry3 is 3 days long: 4, 5, 6
        for (int i = 4; i <= 6; i++) {
            assertThat(
                    entries.get(LocalDate.now().plusDays(i)).contains(entry3),
                    is(true));
        }
    }

    @Test
    public void shouldBeShowing() {
        // when
        boolean showing = recurrenceSourceEntry.isShowing(LocalDate.now(), LocalDate.now().plusDays(1), ZoneId.systemDefault());

        // then
        assertThat(showing, is(true));
    }

    @Test
    public void shouldNotBeShowing() {
        // when
        boolean showing = recurrenceSourceEntry.isShowing(LocalDate.now().minusDays(100), LocalDate.now().minusDays(90), ZoneId.systemDefault());

        // then
        assertThat(showing, is(false));
    }
}
