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

package com.calendarfx.view;

import com.calendarfx.view.DateSelectionModel.SelectionMode;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DateSelectionModelTests {

    @Test
    public void shouldBeEmptySelection() {
        // Given
        DateSelectionModel model = new DateSelectionModel();

        // Then
        assertNotNull(model.getSelectedDates());
        assertThat(model.getSelectedDates(), is(empty()));
        assertTrue(model.isEmpty());
        assertEquals(model.getSelectionMode(), SelectionMode.MULTIPLE_DATES);
        assertThat(model.getLastSelected(), is(nullValue()));
    }

    @Test
    public void shouldSelectOneSingleDate() {
        // Given
        LocalDate now = LocalDate.now();
        DateSelectionModel model = new DateSelectionModel();

        // When
        model.select(now);

        // Then
        assertTrue(model.isSelected(now));
        assertThat(model.getSelectedDates(), contains(now));
        assertEquals(model.getLastSelected(), now);
    }

    @Test
    public void shouldSelectDateAndClearSelection() {
        // Given
        LocalDate now = LocalDate.now();
        DateSelectionModel model = new DateSelectionModel();

        // When
        model.select(now);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates(), contains(now));

        // When
        model.clear();

        // Then
        assertTrue(model.isEmpty());
        assertThat(model.getSelectedDates(), is(empty()));
        assertThat(model.getLastSelected(), is(nullValue()));
    }

    @Test
    public void shouldSelectAndDeselectDate() {
        // Given
        LocalDate now = LocalDate.now();
        DateSelectionModel model = new DateSelectionModel();

        // When
        model.select(now);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates(), contains(now));
        assertTrue(model.isSelected(now));

        // When
        model.deselect(now);

        // Then
        assertTrue(model.isEmpty());
        assertThat(model.getSelectedDates(), is(empty()));
        assertThat(model.getLastSelected(), is(nullValue()));
    }

    @Test
    public void shouldSelectDateRange() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);
        DateSelectionModel model = new DateSelectionModel();
        model.setSelectionMode(SelectionMode.SINGLE_DATE_RANGE);

        // When
        model.selectRange(now, dayAfterTomorrow);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates().size(), is(equalTo(3)));
        assertThat(model.getSelectedDates(), contains(now, tomorrow, dayAfterTomorrow));
        assertThat(model.getLastSelected(), is(equalTo(dayAfterTomorrow)));
        assertTrue(model.isSelected(now));
        assertTrue(model.isSelected(tomorrow));
        assertTrue(model.isSelected(dayAfterTomorrow));
    }

    @Test
    public void shouldSelectMultipleDates() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);
        LocalDate oneYearAfter = dayAfterTomorrow.plusYears(1);
        DateSelectionModel model = new DateSelectionModel();
        model.setSelectionMode(SelectionMode.MULTIPLE_DATES);

        // When
        model.selectRange(now, dayAfterTomorrow);
        model.select(oneYearAfter);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates().size(), is(equalTo(4)));
        assertThat(model.getSelectedDates(), contains(now, tomorrow, dayAfterTomorrow, oneYearAfter));
        assertThat(model.getLastSelected(), is(equalTo(oneYearAfter)));
        assertTrue(model.isSelected(now));
        assertTrue(model.isSelected(tomorrow));
        assertTrue(model.isSelected(dayAfterTomorrow));
        assertTrue(model.isSelected(oneYearAfter));
    }

    @Test
    public void shouldChangeSelectionFromTodayToTomorrow() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        DateSelectionModel model = new DateSelectionModel();
        model.setSelectionMode(SelectionMode.SINGLE_DATE);

        // When
        model.select(now);
        model.select(tomorrow);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates().size(), is(equalTo(1)));
        assertThat(model.getSelectedDates(), contains(tomorrow));
        assertThat(model.getLastSelected(), is(equalTo(tomorrow)));
        assertFalse(model.isSelected(now));
        assertTrue(model.isSelected(tomorrow));
    }

    @Test
    public void shouldSelectOnlySingleDate_AttemptRangeSelection() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);
        DateSelectionModel model = new DateSelectionModel();
        model.setSelectionMode(SelectionMode.SINGLE_DATE);

        // When
        model.selectRange(now, dayAfterTomorrow);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates().size(), is(equalTo(1)));
        assertThat(model.getSelectedDates(), contains(dayAfterTomorrow));
        assertThat(model.getLastSelected(), is(equalTo(dayAfterTomorrow)));
        assertFalse(model.isSelected(now));
        assertFalse(model.isSelected(tomorrow));
        assertTrue(model.isSelected(dayAfterTomorrow));
    }

    @Test
    public void shouldSelectOnlySingleDate_AttemptMultipleSelection() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);
        DateSelectionModel model = new DateSelectionModel();
        model.setSelectionMode(SelectionMode.SINGLE_DATE);

        // When
        model.select(now);
        model.selectUntil(dayAfterTomorrow);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates().size(), is(equalTo(1)));
        assertThat(model.getSelectedDates(), contains(dayAfterTomorrow));
        assertThat(model.getLastSelected(), is(equalTo(dayAfterTomorrow)));
        assertFalse(model.isSelected(now));
        assertFalse(model.isSelected(tomorrow));
        assertTrue(model.isSelected(dayAfterTomorrow));
    }

    @Test
    public void shouldSelectRangeTodayAndTomorrow() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        DateSelectionModel model = new DateSelectionModel();
        model.setSelectionMode(SelectionMode.SINGLE_DATE_RANGE);

        // When
        model.select(now);
        model.select(tomorrow);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates().size(), is(equalTo(2)));
        assertThat(model.getSelectedDates(), contains(now, tomorrow));
        assertThat(model.getLastSelected(), is(equalTo(tomorrow)));
        assertTrue(model.isSelected(now));
        assertTrue(model.isSelected(tomorrow));
    }

    @Test
    public void shouldSelectFromTodayUntilTomorrow() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);
        DateSelectionModel model = new DateSelectionModel();
        model.setSelectionMode(SelectionMode.SINGLE_DATE_RANGE);

        // When
        model.select(now);
        model.selectUntil(dayAfterTomorrow);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates().size(), is(equalTo(3)));
        assertThat(model.getSelectedDates(), contains(now, tomorrow, dayAfterTomorrow));
        assertThat(model.getLastSelected(), is(equalTo(dayAfterTomorrow)));
        assertTrue(model.isSelected(now));
        assertTrue(model.isSelected(tomorrow));
        assertTrue(model.isSelected(dayAfterTomorrow));
    }

    @Test
    public void shouldSelectFromTodayUntilNextTwoWeeks() {
        // Given
        LocalDate now = LocalDate.now();
        LocalDate nextWeek = now.plusWeeks(1);
        LocalDate nextWeekAfterNextWeek = nextWeek.plusWeeks(1);

        List<LocalDate> days = new ArrayList<>();
        LocalDate start = now;
        while (start.isBefore(nextWeekAfterNextWeek) || start.isEqual(nextWeekAfterNextWeek)) {
            days.add(start);
            start = start.plusDays(1);
        }

        DateSelectionModel model = new DateSelectionModel();
        model.setSelectionMode(SelectionMode.SINGLE_DATE_RANGE);

        // When
        model.select(now);
        model.selectUntil(nextWeek);
        model.selectUntil(nextWeekAfterNextWeek);

        // Then
        assertFalse(model.isEmpty());
        assertThat(model.getSelectedDates(), is(not(empty())));
        assertThat(model.getSelectedDates().size(), is(equalTo(days.size())));
        assertThat(model.getLastSelected(), is(equalTo(nextWeekAfterNextWeek)));

        for (LocalDate day : days) {
            assertTrue(model.isSelected(day));
        }
    }
}
