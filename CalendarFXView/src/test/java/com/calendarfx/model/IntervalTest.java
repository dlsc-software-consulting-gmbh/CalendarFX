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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class IntervalTest {

    private Interval interval;

    @Before
    public void setup() {
        interval = new Interval(LocalDate.now(), LocalTime.MIN, LocalDate.now(), LocalTime.MAX);
    }

    @Test
    public void shouldBeEqual() {
        // given
        Interval interval1 = new Interval(LocalDate.of(2017, 1, 14), LocalTime.of(10, 00), LocalDate.of(2017, 1, 15), LocalTime.of(23, 00));
        Interval interval2 = new Interval(LocalDate.of(2017, 1, 14), LocalTime.of(10, 00), LocalDate.of(2017, 1, 15), LocalTime.of(23, 00));

        // when
        boolean equal = interval1.equals(interval2);

        // then
        assertThat(equal, is(true));
    }

    @Test
    public void shouldNotBeEqualBecauseOfDifferentLocalDate() {
        // given
        Interval interval1 = new Interval(LocalDate.of(2017, 1, 14), LocalTime.of(10, 00), LocalDate.of(2017, 1, 15), LocalTime.of(23, 00));
        Interval interval2 = new Interval(LocalDate.of(2017, 1, 14), LocalTime.of(10, 00), LocalDate.of(2017, 1, 16), LocalTime.of(23, 00));

        // when
        boolean equal = interval1.equals(interval2);

        // then
        assertThat(equal, is(false));
    }

    @Test
    public void shouldNotBeEqualBecauseOfDifferentLocalTime() {
        // given
        Interval interval1 = new Interval(LocalDate.of(2017, 1, 14), LocalTime.of(10, 00), LocalDate.of(2017, 1, 15), LocalTime.of(21, 00));
        Interval interval2 = new Interval(LocalDate.of(2017, 1, 14), LocalTime.of(10, 00), LocalDate.of(2017, 1, 15), LocalTime.of(23, 00));

        // when
        boolean equal = interval1.equals(interval2);

        // then
        assertThat(equal, is(false));
    }

    @Test
    public void shouldBeInitializedProperly() {
        assertThat(interval.getStartDate(), is(equalTo(LocalDate.now())));
        assertThat(interval.getEndDate(), is(equalTo(LocalDate.now())));

        // we can not assert the end times as LocalTime.now() wil be different
        // now
    }

    @Test
    public void shouldChangeStartDate() {
        // given
        LocalDate newDate = LocalDate.now().minusDays(20);

        // when
        interval = interval.withStartDate(newDate);

        // then
        assertThat(interval.getStartDate(), is(equalTo(newDate)));
    }

    @Test
    public void shouldChangeEndDate() {
        // given
        LocalDate newDate = LocalDate.now().plusDays(20);

        // when
        interval = interval.withEndDate(newDate);

        // then
        assertThat(interval.getEndDate(), is(equalTo(newDate)));
    }

    @Test
    public void shouldChangeStartTime() {
        // given
        LocalTime newTime = LocalTime.of(10, 15);

        // when
        interval = interval.withStartTime(newTime);

        // then
        assertThat(interval.getStartTime(), is(equalTo(newTime)));
    }

    @Test
    public void shouldChangeEndTime() {
        // given
        LocalTime newTime = LocalTime.of(10, 15);

        // when
        interval = interval.withEndTime(newTime);

        // then
        assertThat(interval.getEndTime(), is(equalTo(newTime)));
    }

    @Test
    public void shouldChangeDuration() {
        // given
        Duration newDuration = Duration.ofMinutes(30);

        // when
        interval = interval.withDuration(newDuration);

        // then
        assertThat(interval.getDuration(), is(equalTo(newDuration)));
        assertThat(interval.getEndTime(), is(equalTo(interval.getStartTime().plus(newDuration))));
    }

    @Test
    public void shouldChangeZoneId() {
        // given
        ZoneId newZoneId = ZoneId.of("UTC");

        // when
        interval = interval.withZoneId(newZoneId);

        // then
        assertThat(interval.getZoneId(), is(equalTo(newZoneId)));
    }

    @Test
    public void shouldChangeAll() {
        // given
        LocalDate newStartDate = LocalDate.now().minusMonths(1);
        LocalDate newEndDate = LocalDate.now().plusMonths(2);
        LocalTime newStartTime = LocalTime.of(10, 15);
        LocalTime newEndTime = LocalTime.of(10, 15);
        ZoneId newZoneId = ZoneId.of("UTC");

        // when
        interval = interval.withStartDate(newStartDate).withEndDate(newEndDate)
                .withStartTime(newStartTime).withEndTime(newEndTime).withZoneId(newZoneId);

        // then
        assertThat(interval.getStartDate(), is(equalTo(newStartDate)));
        assertThat(interval.getStartTime(), is(equalTo(newStartTime)));
        assertThat(interval.getEndDate(), is(equalTo(newEndDate)));
        assertThat(interval.getEndTime(), is(equalTo(newEndTime)));
        assertThat(interval.getZoneId(), is(equalTo(newZoneId)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailBecauseOfBadDates() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        // when
        new Interval(startDate, LocalTime.now(), endDate, LocalTime.now(), ZoneId.systemDefault());

        // then
        // throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailBecauseOfBadTimes() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate;

        // when
        new Interval(startDate, LocalTime.now(), endDate, LocalTime.now().minusHours(1), ZoneId.systemDefault());

        // then
        // throw exception
    }
}
