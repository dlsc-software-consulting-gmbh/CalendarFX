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

package com.calendarfx.view.popover;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import javafx.scene.control.Label;
import org.controlsfx.control.PopOver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A popover that shows the calendar entries for a given date.
 * <p/>
 * <center><img src="doc-files/date-popover.png"></center>
 */
public class DatePopOver extends PopOver {

    private DateControl dateControl;
    private LocalDate date;

    /**
     * Constructs a new popover for the given date.
     *
     * @param control the date control where the popover is being used
     * @param date the date for which to show the entries
     */
    public DatePopOver(DateControl control, LocalDate date) {
        this.dateControl = requireNonNull(control);
        this.date = requireNonNull(date);
        EntriesPane entriesPane = new EntriesPane();

        getRoot().getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());
        getRoot().getStyleClass().add("root");

        List<Entry<?>> entries = findEntries();
        if (entries == null || entries.isEmpty()) {
            Label label = new Label();
            label.setText("No Entries"); //$NON-NLS-1$
            label.getStyleClass().add("no-entries-label"); //$NON-NLS-1$
            setContentNode(label);
        } else {
            entriesPane.getEntries().setAll(entries);
            setContentNode(entriesPane);
        }

        getStyleClass().add("date-popover"); //$NON-NLS-1$
        setArrowIndent(4);
        setDetachable(false);
        setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
        setCornerRadius(4);
    }

    /**
     * Returns the date control where the popover is being used.
     *
     * @return the date control
     */
    public final DateControl getDateControl() {
        return dateControl;
    }

    /**
     * Returns the date where the popover is being used.
     *
     * @return the date
     */
    public final LocalDate getDate() {
        return date;
    }

    private List<Entry<?>> findEntries() {
        List<Entry<?>> result = new ArrayList<>();
        for (CalendarSource source : dateControl.getCalendarSources()) {
            for (Calendar calendar : source.getCalendars()) {
                try {
                    Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(date, date, dateControl.getZoneId());
                    List<Entry<?>> entriesList = entriesMap.get(date);
                    if (entriesList != null) {
                        for (Entry<?> entry : entriesList) {
                            result.add(entry);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Collections.sort(result);
        return result;
    }
}
