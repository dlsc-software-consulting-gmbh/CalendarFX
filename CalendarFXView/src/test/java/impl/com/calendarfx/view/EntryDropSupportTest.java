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

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import impl.com.calendarfx.view.EntryDropSupport.DropResult;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

public class EntryDropSupportTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 28);

    private final Calendar<String> sourceCalendar = new Calendar<>("Source");

    private final Calendar<String> targetCalendar = new Calendar<>("Target");

    private final Entry<String> entry = new Entry<>();

    private Interval originalInterval;

    private final Interval newInterval = new Interval(DATE, LocalTime.of(14, 0), DATE, LocalTime.of(15, 0));

    @Before
    public void setup() {
        entry.setZoneId(ZoneId.of("UTC"));
        entry.setInterval(new Interval(DATE, LocalTime.of(9, 0), DATE, LocalTime.of(10, 0)));
        entry.setCalendar(sourceCalendar);
        originalInterval = entry.getInterval();
    }

    @Test
    public void shouldRejectDropWhenTargetCalendarIsNull() {

        // when
        DropResult result = EntryDropSupport.applyDrop(entry, newInterval, null);

        // then
        assertThat(result, is(DropResult.REJECTED_NO_CALENDAR));
        assertThat(entry.getInterval(), is(equalTo(originalInterval)));
        assertThat(entry.getCalendar(), is(sameInstance(sourceCalendar)));
    }

    @Test
    public void shouldRejectDropWhenTargetCalendarIsReadOnly() {

        // given
        targetCalendar.setReadOnly(true);

        // when
        DropResult result = EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then
        assertThat(result, is(DropResult.REJECTED_READ_ONLY));
        assertThat(entry.getInterval(), is(equalTo(originalInterval)));
        assertThat(entry.getCalendar(), is(sameInstance(sourceCalendar)));
    }

    @Test
    public void shouldApplyIntervalAndCalendarWhenTargetCalendarIsWritable() {

        // when
        DropResult result = EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then
        assertThat(result, is(DropResult.APPLIED));
        assertThat(entry.getInterval(), is(equalTo(newInterval)));
        assertThat(entry.getCalendar(), is(sameInstance(targetCalendar)));
    }

    @Test
    public void shouldNotifyTheSourceCalendarThatTheEntryLeft() {

        // given
        AtomicReference<CalendarEvent> removalEvent = new AtomicReference<>();
        sourceCalendar.addEventHandler(evt -> {
            if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED) && evt.isEntryRemoved()) {
                removalEvent.set(evt);
            }
        });

        // when
        EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then the source view has to be told, otherwise it keeps showing a stale copy
        assertThat(removalEvent.get(), is(notNullValue()));
        assertThat(removalEvent.get().getEntry(), is(sameInstance(entry)));
    }

    @Test
    public void shouldNotifyTheTargetCalendarThatTheEntryArrived() {

        // given
        AtomicReference<CalendarEvent> additionEvent = new AtomicReference<>();
        targetCalendar.addEventHandler(evt -> {
            if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED) && evt.isEntryAdded()) {
                additionEvent.set(evt);
            }
        });

        // when
        EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then
        assertThat(additionEvent.get(), is(notNullValue()));
        assertThat(additionEvent.get().getEntry(), is(sameInstance(entry)));
    }

    @Test
    public void shouldRemoveTheEntryFromTheSourceCalendar() {

        // when
        EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then
        assertThat(sourceCalendar.findEntries(DATE, DATE, ZoneId.of("UTC")).isEmpty(), is(true));
        assertThat(targetCalendar.findEntries(DATE, DATE, ZoneId.of("UTC")).get(DATE), hasItem(entry));
    }

    @Test
    public void shouldApplyIntervalBeforeCalendar() {

        // given
        AtomicReference<Interval> intervalSeenByCalendarChange = new AtomicReference<>();
        targetCalendar.addEventHandler(evt -> {
            if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED)) {
                intervalSeenByCalendarChange.set(evt.getEntry().getInterval());
            }
        });

        // when
        EntryDropSupport.applyDrop(entry, newInterval, targetCalendar);

        // then
        assertThat(intervalSeenByCalendarChange.get(), is(equalTo(newInterval)));
    }
}
