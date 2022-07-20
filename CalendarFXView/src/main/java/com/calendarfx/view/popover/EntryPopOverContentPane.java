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

import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.Messages;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

import java.util.Objects;

public class EntryPopOverContentPane extends PopOverContentPane {

    private final Entry<?> entry;
    private final DateControl dateControl;
    private final PopOver popOver;

    private final InvalidationListener hideListener = it -> {
        if (getEntry().getCalendar() == null) {
            getPopOver().hide(Duration.ZERO);
        }
    };

    private final WeakInvalidationListener weakHideListener = new WeakInvalidationListener(hideListener);

    private final InvalidationListener fullDayListener = obs -> {
        if (getEntry().isFullDay() && !getPopOver().isDetached()) {
            getPopOver().setDetached(true);
        }
    };

    private final WeakInvalidationListener weakFullDayListener = new WeakInvalidationListener(fullDayListener);

    public EntryPopOverContentPane(PopOver popOver, DateControl dateControl, Entry<?> entry) {
        getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());

        this.popOver = popOver;
        this.dateControl = dateControl;
        this.entry = Objects.requireNonNull(entry);

        EntryDetailsView details = new EntryDetailsView(entry);

        PopOverTitledPane detailsPane = new PopOverTitledPane(Messages.getString("EntryPopOverContentPane.DETAILS"), details);

        EntryHeaderView header = new EntryHeaderView(entry, dateControl.getCalendars());
        setHeader(header);

        if (Boolean.getBoolean("calendarfx.developer")) {
            EntryPropertiesView properties = new EntryPropertiesView(entry);
            PopOverTitledPane propertiesPane = new PopOverTitledPane("Properties", properties);
            getPanes().addAll(detailsPane, propertiesPane);
        } else {
            getPanes().addAll(detailsPane);
        }

        setExpandedPane(detailsPane);

        entry.fullDayProperty().addListener(weakFullDayListener);
        popOver.setOnHidden(evt -> entry.fullDayProperty().removeListener(weakFullDayListener));

        entry.calendarProperty().addListener(weakHideListener);
    }

    public final PopOver getPopOver() {
        return popOver;
    }

    public final DateControl getDateControl() {
        return dateControl;
    }

    public final Entry<?> getEntry() {
        return entry;
    }
}
