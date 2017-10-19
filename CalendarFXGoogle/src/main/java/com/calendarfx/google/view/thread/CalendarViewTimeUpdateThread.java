package com.calendarfx.google.view.thread;

import com.calendarfx.view.CalendarView;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Thread that updates the current tine on the calendar view
 *
 * Created by gdiaz on 4/05/2017.
 */
public class CalendarViewTimeUpdateThread extends Thread {

    private static final int TEN_SECONDS = 10_1000;

    private final CalendarView calendarView;

    public CalendarViewTimeUpdateThread(CalendarView calendarView) {
        super("Google-Calendar-Update Current Time");
        this.calendarView = calendarView;
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            Platform.runLater(() -> {
                calendarView.setToday(LocalDate.now());
                calendarView.setTime(LocalTime.now());
            });

            try {
                sleep(TEN_SECONDS);
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
    }

}
