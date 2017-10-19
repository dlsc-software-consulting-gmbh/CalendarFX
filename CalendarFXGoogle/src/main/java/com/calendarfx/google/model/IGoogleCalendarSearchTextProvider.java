package com.calendarfx.google.model;

/**
 * Objects that provides external searching of entries by a given text on a single calendar.
 *
 * Created by gdiaz on 5/05/2017.
 */
public interface IGoogleCalendarSearchTextProvider {

    void search (GoogleCalendar calendar, String searchText);

}
