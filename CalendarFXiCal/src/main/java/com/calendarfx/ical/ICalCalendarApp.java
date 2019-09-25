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

package com.calendarfx.ical;

import com.calendarfx.ical.model.ICalCalendar;
import com.calendarfx.ical.view.ICalWebSourceFactory;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.LoadEvent;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.CalendarView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import org.controlsfx.dialog.ProgressDialog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ICalCalendarApp extends Application {

    private static final ExecutorService executor = Executors
            .newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable, "ICalCalendar Load Thread");
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });

    @Override
    public void start(Stage primaryStage) {
        LoggingDomain.CONFIG.info("Java version: " + System.getProperty("java.version"));

        System.setProperty("ical4j.unfolding.relaxed", "true");
        System.setProperty("ical4j.parsing.relaxed", "true");
        System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());

        CalendarView calendarView = new CalendarView();
        calendarView.setToday(LocalDate.now());
        calendarView.setTime(LocalTime.now());
        calendarView.addEventFilter(LoadEvent.LOAD, evt -> {

            /*
             * Run in background thread. We do not want to block the UI.
             */
            executor.submit(() -> {
                for (CalendarSource source : evt.getCalendarSources()) {
                    for (Calendar calendar : source.getCalendars()) {
                        if (calendar instanceof ICalCalendar) {
                            ICalCalendar account = (ICalCalendar) calendar;
                            account.load(evt);
                        }
                    }
                }
            });
        });

        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
                    });

                    try {
                        // update every 10 seconds
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();

        calendarView.setRequestedTime(LocalTime.now());
        calendarView.setTraysAnimated(false);
        calendarView.setCalendarSourceFactory(new ICalWebSourceFactory(primaryStage));
        calendarView.getCalendarSources().setAll(ICalRepository.familyCalendars, ICalRepository.communityCalendars);

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                ICalRepository.workDoneProperty.addListener(it -> updateProgress(ICalRepository.workDoneProperty.get(), ICalRepository.totalWorkProperty.get()));
                ICalRepository.totalWorkProperty.addListener(it -> updateProgress(ICalRepository.workDoneProperty.get(), ICalRepository.totalWorkProperty.get()));
                ICalRepository.messageProperty.addListener(it -> updateMessage(ICalRepository.messageProperty.get()));
                ICalRepository.loadSources();
                return null;
            }
        };

        ImageView logo = new ImageView(ICalCalendarApp.class.getResource("ical.png").toExternalForm());
        logo.setFitWidth(64);
        logo.setPreserveRatio(true);

        ProgressDialog progressDialog = new ProgressDialog(task);
        progressDialog.setGraphic(logo);
        progressDialog.initModality(Modality.NONE);
        progressDialog.initStyle(StageStyle.UTILITY);
        progressDialog.initOwner(primaryStage.getOwner());
        progressDialog.setTitle("Progress");
        progressDialog.setHeaderText("Importing Online Calendars");
        progressDialog.setContentText("The application is now downloading several calendars from the web.");
        progressDialog.getDialogPane().setPrefWidth(500);
        progressDialog.getDialogPane().getStylesheets().clear();
        progressDialog.getDialogPane().getStylesheets().add(ICalCalendarApp.class.getResource("dialog.css").toExternalForm());

        executor.submit(task);

        Scene scene = new Scene(calendarView);

        primaryStage.setTitle("iCalendar");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1400);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
