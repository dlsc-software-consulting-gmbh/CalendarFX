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
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;

import java.util.Objects;

/**
 * Applies the result of a drag gesture that ended in a different view than the one it
 * started in. Deliberately free of any UI types so that it can be unit tested without
 * an initialised JavaFX toolkit.
 */
public final class EntryDropSupport {

    /**
     * The outcome of a drop.
     */
    public enum DropResult {

        /**
         * The new interval and the target calendar were applied to the entry.
         */
        APPLIED,

        /**
         * No target calendar could be determined. The entry was left untouched.
         */
        REJECTED_NO_CALENDAR,

        /**
         * The target calendar is read-only. The entry was left untouched.
         */
        REJECTED_READ_ONLY
    }

    private EntryDropSupport() {
    }

    /**
     * Moves the given entry to the given calendar and interval. The entry is left
     * completely untouched when the drop is rejected: applying the new interval while
     * refusing the calendar change would leave the user with a confusing half-outcome.
     *
     * @param entry          the entry that was dragged
     * @param newInterval    the interval determined by the drag gesture
     * @param targetCalendar the calendar resolved for the drop target, may be null
     * @return the outcome of the drop
     */
    public static DropResult applyDrop(Entry<?> entry, Interval newInterval, Calendar targetCalendar) {
        Objects.requireNonNull(entry, "entry can not be null");
        Objects.requireNonNull(newInterval, "new interval can not be null");

        if (targetCalendar == null) {
            return DropResult.REJECTED_NO_CALENDAR;
        }

        if (targetCalendar.isReadOnly()) {
            return DropResult.REJECTED_READ_ONLY;
        }

        /*
         * Order matters. The interval has to be final before the calendar changes,
         * because the views reload in reaction to the calendar change.
         *
         * The move is performed as a removal followed by an addition. Entry.setCalendar
         * only fires ENTRY_CALENDAR_CHANGED on the new calendar, so moving directly from
         * one calendar to another would leave the source calendar - and therefore the
         * view showing it - completely unaware that the entry is gone, which makes a
         * stale copy of the entry stay behind in the source view.
         */
        entry.setInterval(newInterval);
        entry.setCalendar(null);
        entry.setCalendar(targetCalendar);

        return DropResult.APPLIED;
    }
}
