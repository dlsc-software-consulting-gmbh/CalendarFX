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

package com.calendarfx.google.view.popover;

import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.model.Calendar;
import com.calendarfx.view.popover.EntryHeaderView;
import com.calendarfx.view.popover.EntryPropertiesView;
import com.calendarfx.view.popover.PopOverContentPane;
import com.calendarfx.view.popover.PopOverTitledPane;
import javafx.collections.ObservableList;

import static java.util.Objects.requireNonNull;

/**
 * Custom according Popover used to edit the information available in a google
 * entry. This also allows to move the entry from a calendar to another.
 *
 * @author Gabriel Diaz, 07.03.2015.
 */
public class GoogleEntryPopOverContentPane extends PopOverContentPane {

    public GoogleEntryPopOverContentPane(GoogleEntry entry, ObservableList<Calendar> allCalendars) {
        requireNonNull(entry);
        getStylesheets().add(GoogleEntryPopOverContentPane.class.getResource("google-popover.css").toExternalForm());

        EntryHeaderView header = new EntryHeaderView(entry, allCalendars);
        GoogleEntryDetailsView details = new GoogleEntryDetailsView(entry);
        GoogleEntryAttendeesView attendees = new GoogleEntryAttendeesView(entry);
        GoogleEntryGMapsFXView mapView = new GoogleEntryGMapsFXView(entry);

        PopOverTitledPane detailsPane = new PopOverTitledPane("Details", details);
        PopOverTitledPane attendeesPane = new PopOverTitledPane("Attendees", attendees);

        if (Boolean.getBoolean("calendarfx.developer")) {
            EntryPropertiesView properties = new EntryPropertiesView(entry);
            PopOverTitledPane propertiesPane = new PopOverTitledPane("Properties", properties);
            getPanes().addAll(detailsPane, attendeesPane, propertiesPane);
        } else {
            getPanes().addAll(detailsPane, attendeesPane);
        }

        setHeader(header);
        setExpandedPane(detailsPane);
        setFooter(mapView);
    }

}
