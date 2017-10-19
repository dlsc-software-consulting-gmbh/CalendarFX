package impl.com.calendarfx.google.view;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.IGoogleCalendarSearchTextProvider;
import com.calendarfx.google.service.SecurityService;
import com.calendarfx.google.view.data.GoogleCalendarData;
import com.calendarfx.google.view.data.IGoogleCalendarDataProvider;
import com.calendarfx.google.view.task.LoadEntriesByTextTask;
import com.calendarfx.google.view.thread.GoogleTaskExecutor;
import com.calendarfx.model.Calendar;
import javafx.collections.ListChangeListener;

/**
 * Default implementation of the provider that searches entries in google synchronously.
 *
 * Created by gdiaz on 5/05/2017.
 */
final class GoogleCalendarSearchTextManager implements IGoogleCalendarSearchTextProvider, ListChangeListener<Calendar> {

    private final IGoogleCalendarDataProvider provider;

    GoogleCalendarSearchTextManager(IGoogleCalendarDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void search(GoogleCalendar calendar, String text) {
        if (SecurityService.getInstance().isLoggedIn()) {
            GoogleCalendarData data = provider.getCalendarData(calendar, true);
            if (!data.isLoadedSearchText(text)) {
                GoogleAccount account = SecurityService.getInstance().getLoggedAccount();
                LoadEntriesByTextTask task = new LoadEntriesByTextTask(text, calendar, data, account);
                GoogleTaskExecutor.getInstance().executeImmediate(task);
            }
        }
    }

    @Override
    public void onChanged(Change<? extends Calendar> c) {
        while (c.next()) {
            for (Calendar calendar : c.getRemoved()) {
                if (calendar instanceof GoogleCalendar) {
                    ((GoogleCalendar) calendar).setSearchTextProvider(null);
                }
            }

            for (Calendar calendar : c.getAddedSubList()) {
                if (calendar instanceof GoogleCalendar) {
                    ((GoogleCalendar) calendar).setSearchTextProvider(this);
                }
            }
        }
    }
}
