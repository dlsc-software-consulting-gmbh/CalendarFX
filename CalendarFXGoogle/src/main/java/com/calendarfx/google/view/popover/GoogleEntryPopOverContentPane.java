/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
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
