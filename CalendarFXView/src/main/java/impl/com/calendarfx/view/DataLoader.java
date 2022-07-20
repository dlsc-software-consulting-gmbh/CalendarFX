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
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.LoadEvent;
import com.calendarfx.util.LoggingDomain;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class DataLoader {

    private final LoadDataSettingsProvider provider;

    public DataLoader(LoadDataSettingsProvider provider) {
        this.provider = requireNonNull(provider);
    }

    public void loadEntries(Map<LocalDate, List<Entry<?>>> result) {

        long time = System.currentTimeMillis();

        LocalDate startDate = provider.getLoadStartDate();
        LocalDate endDate = provider.getLoadEndDate();

        ZoneId zoneId = provider.getZoneId();

        for (CalendarSource source : provider.getCalendarSources()) {

            for (Calendar calendar : source.getCalendars()
                    .stream()
                    .filter(c -> provider.isCalendarVisible(c))
                    .collect(Collectors.toList())) {

                try {
                    Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, endDate, zoneId);

                    for (LocalDate date : entries.keySet()) {

                        List<Entry<?>> list = result.computeIfAbsent(date, it -> new ArrayList<>());
                        List<Entry<?>> dateEntries = entries.get(date);
                        if (dateEntries != null) {
                            for (Entry<?> entry : dateEntries) {
                                if (entry == null) {
                                    if (LoggingDomain.MODEL.isLoggable(Level.SEVERE)) {
                                        LoggingDomain.MODEL.severe("the calendar " + calendar.getName() + " (source " + source.getName() + ") returned a NULL entry");
                                    }
                                } else {
                                    list.add(entry);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (List<Entry<?>> entries : result.values()) {
            Collections.sort(entries);
        }

        LoggingDomain.PERFORMANCE.fine("data load time: " + (System.currentTimeMillis() - time) + ", view = " + provider.getClass().getSimpleName());

        provider.getControl().fireEvent(new LoadEvent(LoadEvent.LOAD, provider.getLoaderName(), provider.getCalendarSources(), startDate, endDate, zoneId));
    }
}