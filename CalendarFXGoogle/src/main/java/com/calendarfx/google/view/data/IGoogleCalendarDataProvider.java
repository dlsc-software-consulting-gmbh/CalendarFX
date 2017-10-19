package com.calendarfx.google.view.data;

import com.calendarfx.google.model.GoogleCalendar;

/**
 * Provider of the google calendar data.
 *
 * Created by gdiaz on 5/05/2017.
 */
public interface IGoogleCalendarDataProvider {

    default GoogleCalendarData getCalendarData (GoogleCalendar calendar) {
        return getCalendarData(calendar, false);
    }

    GoogleCalendarData getCalendarData (GoogleCalendar calendar, boolean create);

    void removeCalendarData(GoogleCalendar calendar);

    void clearData ();
}
