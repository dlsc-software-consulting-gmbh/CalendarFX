package com.calendarfx.google.view.thread;

import com.calendarfx.google.service.SecurityService;
import com.calendarfx.google.view.data.IGoogleCalendarDataProvider;
import com.calendarfx.google.view.task.RefreshCalendarsTask;
import com.google.common.base.Preconditions;

/**
 * Thread that performs the automatic refreshing.
 *
 * Created by gdiaz on 5/05/2017.
 */
public class GoogleAutoRefreshThread extends Thread {

    private final Object LOCK = new Object();

    private final IGoogleCalendarDataProvider provider;
    private RefreshCalendarsTask task;
    private long delay;

    public GoogleAutoRefreshThread (IGoogleCalendarDataProvider provider) {
        this.provider = provider;
        this.delay = RefreshInterval.EVERY_5_MINUTES.getTime();
        setName("Google-Calendar-Auto refresh Thread");
        setPriority(NORM_PRIORITY);
        setDaemon(true);
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        Preconditions.checkArgument(delay >= 0);
        synchronized (LOCK) {
            this.delay = delay;
        }
    }

    public void restart () {
        synchronized (LOCK) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
        interrupt();
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run () {
        while (true) {
            try {
                long wait;

                synchronized (LOCK) {
                    wait = delay;
                }

                if (wait > 0) {
                    sleep(wait);
                }
            }
            catch (InterruptedException e) {
                // Do nothing
            }

            synchronized (LOCK) {
                if (delay > 0 && SecurityService.getInstance().isLoggedIn()) {
                    task = new RefreshCalendarsTask(SecurityService.getInstance().getLoggedAccount(), provider);
                    GoogleTaskExecutor.getInstance().execute(task);
                }
            }
        }
    }

    /**
     * Enum used to determine the internals of refreshing.
     */
    public enum RefreshInterval {

        NEVER(0, "Never"),

        EVERY_MINUTE(1000 * 60, "Every Minute"),

        EVERY_2_MINUTES(1000 * 60 * 2, "Every 2 Minutes"),

        EVERY_5_MINUTES(1000 * 60 * 5, "Every 5 Minutes"),

        EVERY_10_MINUTES(1000 * 60 * 10, "Every 10 Minutes"),

        EVERY_30_MINUTES(1000 * 60 * 30, "Every 30 Minutes");

        private long time;
        private String name;

        RefreshInterval (long time, String name) {
            this.time = time;
            this.name = name;
        }

        public long getTime() {
            return time;
        }

        public String getName() {
            return name;
        }

    }

}
