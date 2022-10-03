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
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class EntryTest {

    private final Entry<String> entry = new Entry<>();

    private final Calendar calendar = new Calendar();

    @Before
    public void setup() {
        entry.setLocation("Adliswil");
        entry.setUserObject("My User Object");
        entry.setZoneId(ZoneId.of("UTC"));
        entry.setCalendar(calendar);
    }

    @Test
    public void shouldNotHaveStyles() {

        // when
        boolean result = entry.hasStyleClass();

        // then
        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void shouldHaveStylesAfterAdding() {

        // when
        entry.getStyleClass().add("mystyle");

        // then
        assertThat(entry.hasStyleClass(), is(equalTo(true)));
    }

    @Test
    public void shouldNotHaveProperties() {

        // when
        boolean result = entry.hasProperties();

        // then
        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void shouldHaveProperties() {
        // when
        entry.getProperties().put("mykey", "myvalue");

        // then
        assertThat(entry.hasProperties(), is(equalTo(true)));
    }

    @Test
    public void shouldBeEqual() {
        // given
        Entry entryA = new Entry();
        Entry entryB = new Entry();

        entryA.setId("ID");
        entryB.setId("ID");

        // when
        boolean equal = entryA.equals(entryB);

        // then
        assertThat(equal, is(true));
    }

    @Test
    public void shouldBeEqualRecurrences() {
        // given
        Entry<String> entryA = new Entry<>();
        Entry<String> entryB = new Entry<>();

        String id = UUID.randomUUID().toString();

        entryA.setId(id);
        entryB.setId(id);

        String recurrenceId = ZonedDateTime.now().toString();

        entryA.getProperties().put("com.calendarfx.recurrence.id", recurrenceId);
        entryB.getProperties().put("com.calendarfx.recurrence.id", recurrenceId);

        // when
        boolean equal = entryA.equals(entryB);

        // then
        assertThat(equal, is(true));
    }

    @Test
    public void shouldNotBeEqualWithSameRecurrenceIdAndId() {
        // given
        Entry<String> entryA = new Entry<>();
        Entry<String> entryB = new Entry<>();

        String id = UUID.randomUUID().toString();

        entryA.setId(id);
        entryB.setId(id);

        String recurrenceId = ZonedDateTime.now().toString();

        entryA.getProperties().put("com.calendarfx.recurrence.id",
                recurrenceId);
        entryB.getProperties().put("com.calendarfx.recurrence.id",
                recurrenceId);

        // when
        boolean equal = entryA.equals(entryB);

        int entryAHashcode = entryA.hashCode();
        int entryBHashcode = entryB.hashCode();

        Set<Entry<String>> mySet = new HashSet<>();
        mySet.add(entryA);

        // then
        assertThat(entryA == entryB, is(false)); // two different instances
        assertThat(equal, is(true));
        assertThat(entryAHashcode, is(equalTo(entryBHashcode)));
        assertThat(mySet.contains(entryA), is(true));
        assertThat(mySet.contains(entryB), is(true));
    }

    @Test
    public void shouldNotBeEqualWithSameRecurrenceIdButDifferentId() {
        // given
        Entry<String> entryA = new Entry<>();
        Entry<String> entryB = new Entry<>();

        entryA.setId(UUID.randomUUID().toString());
        entryB.setId(UUID.randomUUID().toString());

        String recurrenceId = ZonedDateTime.now().toString();

        entryA.getProperties().put("com.calendarfx.recurrence.id",
                recurrenceId);
        entryB.getProperties().put("com.calendarfx.recurrence.id",
                recurrenceId);

        // when
        boolean equal = entryA.equals(entryB);

        int entryAHashcode = entryA.hashCode();
        int entryBHashcode = entryB.hashCode();

        Set<Entry<String>> mySet = new HashSet<>();
        mySet.add(entryA);

        // then
        assertThat(entryA == entryB, is(false)); // two different instances
        assertThat(equal, is(false));
        assertThat(entryAHashcode, is(not(equalTo(entryBHashcode))));
        assertThat(mySet.contains(entryA), is(true));
        assertThat(mySet.contains(entryB), is(false));
    }

    @Test
    public void shouldMakeRecurrence() {
        // given
        Entry<String> recurrence = new Entry<>();

        // when
        recurrence.getProperties().put("com.calendarfx.recurrence.source",
                entry);

        // then
        assertThat(recurrence.isRecurrence(), is(true));
    }

    @Test
    public void shouldSetRecurrenceId() {
        // given
        Entry<String> recurrence = new Entry<>();

        // when
        recurrence.getProperties().put("com.calendarfx.recurrence.id", "xyz");

        // then
        assertThat(recurrence.getRecurrenceId(), is(equalTo("xyz")));
    }

    @Test
    public void shouldSetRecurrenceRule() {
        // given
        String rule = "RRULE:FREQ=DAILY;";

        // when
        entry.setRecurrenceRule(rule);

        // then
        assertThat(entry.getRecurrenceRule(), is(equalTo(rule)));
        assertThat(entry.getRecurrenceEnd(), is(equalTo(LocalDate.MAX)));
    }

    @Test
    public void shouldReturnRecurrencesDaily() {
        // given
        final String rule = "RRULE:FREQ=DAILY;";
        entry.setRecurrenceRule(rule);

        final int days = 30;
        entry.changeStartDate(LocalDate.now());
        entry.changeEndDate(LocalDate.now());
        entry.changeStartTime(LocalTime.of(10, 0));
        entry.changeEndTime(LocalTime.of(12, 0));

        // when
        Map<LocalDate, List<Entry<?>>> result = calendar.findEntries(
                LocalDate.now(), LocalDate.now().plusDays(days),
                ZoneId.systemDefault());

        // then
        for (int currentDay = 0; currentDay < days; currentDay++) {
            List<Entry<?>> entryList = result
                    .get(LocalDate.now().plusDays(currentDay));

            assertThat(entryList, is(not(empty())));
            assertThat(entryList.size(), is(equalTo(1)));

            Entry<?> recurrence = entryList.get(0);

            assertThat(recurrence.getStartDate(),
                    is(equalTo(entry.getStartDate().plusDays(currentDay))));
            assertThat(recurrence.getEndDate(),
                    is(equalTo(entry.getEndDate().plusDays(currentDay))));
            assertThat(recurrence.getRecurrenceEnd(),
                    is(equalTo(LocalDate.MAX)));
            assertThat(recurrence.getRecurrenceRule(), is(equalTo(rule)));

            assertThatRecurrenceConfiguredLikeSource(recurrence);
        }
    }

    @Test
    public void shouldReturnRecurrencesEveryOtherDayForOneMonth() {
        // given
        final String rule = "RRULE:FREQ=DAILY;INTERVAL=2;";
        entry.setRecurrenceRule(rule);

        final int days = 30;
        entry.changeStartDate(LocalDate.now());
        entry.changeEndDate(LocalDate.now());
        entry.changeStartTime(LocalTime.of(10, 0));
        entry.changeEndTime(LocalTime.of(12, 0));

        // when
        Map<LocalDate, List<Entry<?>>> result = calendar.findEntries(
                LocalDate.now(), LocalDate.now().plusDays(days),
                ZoneId.systemDefault());

        // then
        for (int currentDay = 0; currentDay < days; currentDay++) {
            if (currentDay % 2 == 1) {
                /*
                 * We do not expect results for odd days
                 */
                List<Entry<?>> entryList = result
                        .get(LocalDate.now().plusDays(currentDay));
                assertThat(entryList, is(nullValue()));
            } else {
                List<Entry<?>> entryList = result
                        .get(LocalDate.now().plusDays(currentDay));

                assertThat(entryList, is(not(empty())));
                assertThat(entryList.size(), is(equalTo(1)));

                Entry<?> recurrence = entryList.get(0);

                assertThat(recurrence.getStartDate(),
                        is(equalTo(entry.getStartDate().plusDays(currentDay))));
                assertThat(recurrence.getEndDate(),
                        is(equalTo(entry.getEndDate().plusDays(currentDay))));
                assertThat(recurrence.getRecurrenceEnd(),
                        is(equalTo(LocalDate.MAX)));
                assertThat(recurrence.getRecurrenceRule(), is(equalTo(rule)));

                assertThatRecurrenceConfiguredLikeSource(recurrence);
            }
        }
    }

    @Test
    public void shouldReturnRecurrencesWeekly() {
        // given
        final String rule = "RRULE:FREQ=WEEKLY;";
        entry.setRecurrenceRule(rule);

        final int weeks = 10;
        entry.changeStartDate(LocalDate.now());
        entry.changeEndDate(LocalDate.now());
        entry.changeStartTime(LocalTime.of(10, 0));
        entry.changeEndTime(LocalTime.of(12, 0));

        // when
        Map<LocalDate, List<Entry<?>>> result = calendar.findEntries(
                LocalDate.now(), LocalDate.now().plusWeeks(weeks),
                ZoneId.systemDefault());

        // then
        for (int currentWeek = 0; currentWeek < weeks; currentWeek++) {
            List<Entry<?>> entryList = result
                    .get(LocalDate.now().plusWeeks(currentWeek));

            assertThat(entryList, is(not(empty())));
            assertThat(entryList.size(), is(equalTo(1)));

            Entry<?> recurrence = entryList.get(0);

            assertThat(recurrence.getStartDate(),
                    is(equalTo(entry.getStartDate().plusWeeks(currentWeek))));
            assertThat(recurrence.getEndDate(),
                    is(equalTo(entry.getEndDate().plusWeeks(currentWeek))));
            assertThat(recurrence.getRecurrenceEnd(),
                    is(equalTo(LocalDate.MAX)));
            assertThat(recurrence.getRecurrenceRule(), is(equalTo(rule)));

            assertThatRecurrenceConfiguredLikeSource(recurrence);
        }
    }

    @Test
    public void shouldReturnRecurrencesMonthly() {
        // given
        final String rule = "RRULE:FREQ=MONTHLY;";
        entry.setRecurrenceRule(rule);

        /*
         * We use 2015/1/1 as a fixed date to ensure that we do not run into
         * issues with February having 28 or 29 days.
         */

        final int months = 10;
        entry.changeStartDate(LocalDate.of(2015, 1, 1));
        entry.changeEndDate(LocalDate.of(2015, 1, 1));
        entry.changeStartTime(LocalTime.of(10, 0));
        entry.changeEndTime(LocalTime.of(12, 0));


        // when
        Map<LocalDate, List<Entry<?>>> result = calendar.findEntries(
                LocalDate.of(2015, 1, 1), LocalDate.of(2015, 1, 1).plusMonths(months),
                ZoneId.systemDefault());

        // then
        for (int currentMonth = 0; currentMonth < months; currentMonth++) {
            LocalDate date = LocalDate.of(2015, 1, 1).plusMonths(currentMonth);
            List<Entry<?>> entryList = result
                    .get(date);

            assertThat(entryList, is(not(empty())));
            assertThat(entryList.size(), is(equalTo(1)));

            Entry<?> recurrence = entryList.get(0);

            assertThat(recurrence.getStartDate(),
                    is(equalTo(entry.getStartDate().plusMonths(currentMonth))));
            assertThat(recurrence.getEndDate(),
                    is(equalTo(entry.getEndDate().plusMonths(currentMonth))));
            assertThat(recurrence.getRecurrenceEnd(),
                    is(equalTo(LocalDate.MAX)));
            assertThat(recurrence.getRecurrenceRule(), is(equalTo(rule)));

            assertThatRecurrenceConfiguredLikeSource(recurrence);
        }
    }

    @Test
    public void shouldReturnRecurrencesYearly() {
        // given
        final String rule = "RRULE:FREQ=YEARLY;";
        entry.setRecurrenceRule(rule);

        final int years = 10;
        entry.changeStartDate(LocalDate.now());
        entry.changeEndDate(LocalDate.now());
        entry.changeStartTime(LocalTime.of(10, 0));
        entry.changeEndTime(LocalTime.of(12, 0));

        // when
        Map<LocalDate, List<Entry<?>>> result = calendar.findEntries(
                LocalDate.now(), LocalDate.now().plusYears(years),
                ZoneId.systemDefault());

        // then
        for (int currentYear = 0; currentYear < years; currentYear++) {
            List<Entry<?>> entryList = result
                    .get(LocalDate.now().plusYears(currentYear));

            assertThat(entryList, is(not(empty())));
            assertThat(entryList.size(), is(equalTo(1)));

            Entry<?> recurrence = entryList.get(0);

            assertThat(recurrence.getStartDate(),
                    is(equalTo(entry.getStartDate().plusYears(currentYear))));
            assertThat(recurrence.getEndDate(),
                    is(equalTo(entry.getEndDate().plusYears(currentYear))));
            assertThat(recurrence.getRecurrenceEnd(),
                    is(equalTo(LocalDate.MAX)));
            assertThat(recurrence.getRecurrenceRule(), is(equalTo(rule)));

            assertThatRecurrenceConfiguredLikeSource(recurrence);
        }
    }

    private void assertThatRecurrenceConfiguredLikeSource(Entry<?> recurrence) {
        assertThat(recurrence.getRecurrenceSourceEntry(), is(equalTo(entry)));
        assertThat(recurrence.isRecurrence(), is(true));
        assertThat(recurrence.isRecurring(), is(true));
        assertThat(recurrence.getStartTime(),
                is(equalTo(entry.getStartTime())));
        assertThat(recurrence.getEndTime(), is(equalTo(entry.getEndTime())));
        assertThat(recurrence.getTitle(), is(equalTo(entry.getTitle())));
        assertThat(recurrence.isFullDay(), is(equalTo(entry.isFullDay())));
        assertThat(recurrence.isMultiDay(), is(equalTo(entry.isMultiDay())));
        assertThat(recurrence.getMinimumDuration(),
                is(equalTo(entry.getMinimumDuration())));
        assertThat(recurrence.getLocation(), is(equalTo(entry.getLocation())));
        assertThat(recurrence.getUserObject(),
                is(equalTo(entry.getUserObject())));
        assertThat(recurrence.getZoneId(), is(equalTo(entry.getZoneId())));
    }

    @Test
    public void shouldChangeTitle() {
        // given
        String title = "New Title";

        // when
        entry.setTitle(title);

        // then
        assertThat(entry.getTitle(), is(equalTo(title)));
    }

    @Test
    public void shouldChangeInterval() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(2);

        LocalTime startTime = LocalTime.now();
        LocalTime endTime = LocalTime.now().plusHours(10);

        Interval interval = new Interval(startDate, startTime, endDate, endTime,
                ZoneId.of("UTC"));

        entry.setInterval(interval);

        assertThat(entry.getStartDate(), is(equalTo(startDate)));
        assertThat(entry.getStartTime(), is(equalTo(startTime)));
        assertThat(entry.getEndDate(), is(equalTo(endDate)));
        assertThat(entry.getEndTime(), is(equalTo(endTime)));
        assertThat(entry.getZoneId(), is(equalTo(ZoneId.of("UTC"))));
    }

    @Test
    public void shouldChangeStartTime() {
        // given
        LocalTime time = entry.getStartTime().plusHours(1);

        // when
        entry.changeStartTime(time);

        // then
        assertThat(entry.getStartTime(), is(equalTo(time)));
    }

    @Test
    public void shouldChangeEndTime() {
        // given
        LocalTime time = entry.getEndTime().plusHours(1);

        // when
        entry.changeEndTime(time);

        // then
        assertThat(entry.getEndTime(), is(equalTo(time)));
    }

    @Test
    public void shouldChangeStartDate() {
        // given
        LocalDate date = entry.getStartDate().plusDays(1);

        // when
        entry.changeStartDate(date);

        // then
        assertThat(entry.getStartDate(), is(equalTo(date)));
    }

    @Test
    public void shouldChangeEndDate() {
        // given
        LocalDate date = entry.getEndDate().plusDays(1);

        // when
        entry.changeEndDate(date);

        // then
        assertThat(entry.getEndDate(), is(equalTo(date)));
    }

    @Test
    public void shouldChangeZoneId() {
        // given
        ZoneId zoneId = ZoneId.of("GMT");

        // when
        entry.setZoneId(zoneId);

        // then
        assertThat(entry.getZoneId(), is(equalTo(zoneId)));
    }

    @Test
    public void shouldChangeUserObject() {
        // given
        String userObject = "Hello World";

        // when
        entry.setUserObject(userObject);

        // then
        assertThat(entry.getUserObject(), is(equalTo(userObject)));
    }

    @Test
    public void shouldChangeFullDay() {
        // given
        boolean fullDay = true;

        // when
        entry.setFullDay(fullDay);

        // then
        assertThat(entry.isFullDay(), is(equalTo(TRUE)));
    }

    @Test
    public void shouldChangeCalendar() {
        // given
        Calendar newCalendar = new Calendar();

        // when
        entry.setCalendar(newCalendar);

        // then
        assertThat(entry.getCalendar(), is(equalTo(newCalendar)));
    }

    @Test
    public void shouldReturnMultiDay1() {
        // given
        LocalDate st = LocalDate.now();
        LocalDate et = st.plusDays(1);
        entry.changeStartDate(st);
        entry.changeEndDate(et);

        // when
        boolean multiDay = entry.isMultiDay();

        // then
        assertThat(multiDay, is(true));
    }

    @Test
    public void shouldReturnMultiDay2() {
        // given
        LocalDate st = LocalDate.now().minusDays(4);
        LocalDate et = st.plusDays(4);
        entry.changeStartDate(st);
        entry.changeEndDate(et);

        // when
        boolean multiDay = entry.isMultiDay();

        // then
        assertThat(multiDay, is(true));
    }

    @Test
    public void shouldNotReturnMultiDay() {
        // given
        LocalDate st = LocalDate.now();
        LocalDate et = st;

        entry.changeStartDate(st);
        entry.changeEndDate(et);

        // when
        boolean multiDay = entry.isMultiDay();

        // then
        assertThat(multiDay, is(false));
    }

    @Test
    public void shouldIntersectWithTimeInterval() {
        // given
        LocalDate st = LocalDate.now();
        LocalDate et = LocalDate.now().plusDays(7);

        entry.changeStartDate(st);
        entry.changeEndDate(et);

        ZonedDateTime zst = ZonedDateTime.now().plusDays(2);
        ZonedDateTime zet = ZonedDateTime.now().plusDays(4);

        // when
        boolean intersects = entry.intersects(zst, zet);

        // then
        assertThat(intersects, is(true));
    }

    @Test
    public void shouldNotIntersectWithTimeInterval() {
        // given
        LocalDate st = LocalDate.now();
        LocalDate et = LocalDate.now().plusDays(7);

        entry.changeStartDate(st);
        entry.changeEndDate(et);

        ZonedDateTime zst = ZonedDateTime.now().plusDays(9);
        ZonedDateTime zet = ZonedDateTime.now().plusDays(12);

        // when
        boolean intersects = entry.intersects(zst, zet);

        // then
        assertThat(intersects, is(false));
    }

    @Test
    public void shouldIntersectWithTimeOtherEntry() {
        // given
        LocalDate st = LocalDate.now();
        LocalDate et = LocalDate.now().plusDays(7);

        entry.changeStartDate(st);
        entry.changeEndDate(et);

        Entry<?> entry2 = new Entry<>();

        st = LocalDate.now().plusDays(3);
        et = LocalDate.now().plusDays(5);

        entry2.changeStartDate(st);
        entry2.changeEndDate(et);

        // when
        boolean intersects = entry.intersects(entry2);

        // then
        assertThat(intersects, is(true));

        /*-- reverse operation --*/

        // when
        intersects = entry2.intersects(entry);

        // then
        assertThat(intersects, is(true));
    }

    @Test
    public void shouldNotIntersectWithTimeOtherEntry() {
        // given
        LocalDate st = LocalDate.now();
        LocalDate et = LocalDate.now().plusDays(7);

        entry.changeStartDate(st);
        entry.changeEndDate(et);

        Entry<?> entry2 = new Entry<>();

        st = LocalDate.now().plusDays(9);
        et = LocalDate.now().plusDays(12);

        entry2.changeStartDate(st);
        entry2.changeEndDate(et);

        // when
        boolean intersects = entry.intersects(entry2);

        // then
        assertThat(intersects, is(false));

        /*-- reverse operation --*/

        // when
        intersects = entry2.intersects(entry);

        // then
        assertThat(intersects, is(false));
    }

    @Test
    public void shouldMatchSearchTerm() {
        // given
        String title = "My Title";
        entry.setTitle(title);

        // when
        boolean match = entry.matches("My");

        // then
        assertThat(match, is(true));
    }

    @Test
    public void shouldSortBasedOnStartTime() {
        // given
        String title = "My Title";
        entry.setTitle(title);

        // when
        boolean match = entry.matches("xxx");

        // then
        assertThat(match, is(false));
    }
}
