package com.calendarfx.google.view.thread;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.model.GoogleEntryReminder;
import com.calendarfx.google.service.SecurityService;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.google.common.collect.Lists;
import javafx.application.Platform;
import org.controlsfx.control.Notifications;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Thread that shows notifications of entries that are about to happen.
 *
 * Created by gdiaz on 6/05/2017.
 */
public class GoogleNotificationPopupThread extends Thread {

    private static final long ONE_MINUTE = 1000 * 30;

    private final CalendarView calendarView;

    public GoogleNotificationPopupThread (CalendarView calendarView) {
        this.calendarView = calendarView;
        setDaemon(true);
        setName("Google-Notification-Thread");
        setPriority(NORM_PRIORITY);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            try {
                sleep(ONE_MINUTE);
            } catch (InterruptedException e) {
                // Do nothing
            }

            if (SecurityService.getInstance().isLoggedIn()) {
                GoogleAccount account = SecurityService.getInstance().getLoggedAccount();
                LocalDate today = calendarView.getToday();
                LocalDateTime now = calendarView.getToday().atTime(LocalTime.now());

                for (GoogleCalendar calendar : account.getGoogleCalendars()) {
                    Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(today, today, calendarView.getZoneId());
                    for (List<Entry<?>> list : entries.values()) {
                        for (Entry<?> e : list) {
                            if (e instanceof GoogleEntry) {
                                GoogleEntry entry = (GoogleEntry) e;
                                if (isSubjectOfNotification(entry, now)) {
                                    showNotification(entry);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isSubjectOfNotification(GoogleEntry entry, LocalDateTime now) {
        List<GoogleEntryReminder> reminders = Lists.newArrayList();

        reminders.addAll(entry.getReminders());
        if (reminders.isEmpty()) {
            reminders.addAll(((GoogleCalendar) entry.getCalendar()).getDefaultReminders());
        }

        for (GoogleEntryReminder reminder : reminders) {
            if (reminder.getMethod() != GoogleEntryReminder.RemindMethod.POPUP) {
                continue;
            }

            if (reminder.getMinutes() == null || reminder.getMinutes() < 0) {
                continue;
            }

            if (!now.isBefore(entry.getStartAsLocalDateTime())) {
                continue;
            }

            long distanceMinutes = now.until(entry.getStartAsLocalDateTime(), ChronoUnit.MINUTES);
            if (distanceMinutes == reminder.getMinutes()) {
                return true;
            }
        }

        return false;
    }

    private void showNotification (GoogleEntry entry) {
        Platform.runLater(() -> {
            GoogleCalendar calendar = (GoogleCalendar) entry.getCalendar();
            Notifications.create().title(calendar.getName()).text(entry.getTitle()).showInformation();
        });
    }

}
