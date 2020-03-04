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

package com.calendarfx.resource.app;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Marker;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.ResourceCalendarView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class ResourceCalendarApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        ResourceCalendarView<String> resourceCalendarView = new ResourceCalendarView<>();

        resourceCalendarView.setContextMenuCallback(param -> {
            ContextMenu menu = new ContextMenu();
            MenuItem newMarkerItem = new MenuItem("New Marker");
            newMarkerItem.setOnAction(evt -> {
                Marker marker = new Marker();
                marker.setStyle("-fx-background-color: red;");
                marker.timeProperty().addListener(it -> System.out.println("Marker " + marker.getTitle() + ": new time = " + marker.getTime()));
                marker.setTitle("New Marker");
                marker.setTime(param.getZonedDateTime());
                resourceCalendarView.getMarkers().add(marker);
            });
            menu.getItems().add(newMarkerItem);
            return menu;
        });

        resourceCalendarView.setHeaderFactory(resource -> {
            Label label1 = new Label("IG-TR");
            Label label2 = new Label("29.000");
            Label label3 = new Label("13.200");
            Label label4 = new Label("92 (CPC)");

            label1.setStyle("-fx-font-family: Monospaced; -fx-font-style: bold;");
            label2.setStyle("-fx-font-family: Monospaced; -fx-text-fill: blue; -fx-font-style: bold;");
            label3.setStyle("-fx-font-family: Monospaced; -fx-text-fill: blue; -fx-font-style: bold;");
            label4.setStyle("-fx-font-family: Monospaced; -fx-text-fill: blue; -fx-font-style: bold;");

            label1.setAlignment(Pos.CENTER);
            label2.setAlignment(Pos.CENTER);
            label3.setAlignment(Pos.CENTER);
            label4.setAlignment(Pos.CENTER);

            VBox box = new VBox(5, label1, label2, label3, label4);
            box.setPadding(new Insets(10));
            box.setFillWidth(true);
            return box;
        });

        for (int i = 0; i < 5; i++) {
            CalendarSource source = new CalendarSource("Default");

            Calendar calendar1 = new HelloDayViewCalendar();
            calendar1.setStyle(Style.STYLE1);
            source.getCalendars().add(calendar1);

            Calendar calendar2 = new HelloDayViewCalendar();
            calendar2.setStyle(Style.STYLE2);
            source.getCalendars().add(calendar2);

            Calendar calendar3 = new HelloDayViewCalendar();
            calendar3.setStyle(Style.STYLE3);
            source.getCalendars().add(calendar3);

            String resource = "Resource " + (i + 1);
            resourceCalendarView.getResources().add(resource);

            DayView dayView = resourceCalendarView.getDayView(resource);
            dayView.getCalendarSources().setAll(source);
            dayView.setEnableCurrentTimeMarker(true);
            dayView.setEnableCurrentTimeCircle(i == 0);

            /* PSI:
             * Setting a custom entry view factory will allow you to set icons on your entry
             * views based on state information provided by your model.
             */
            dayView.setEntryViewFactory(entry -> {

                DayEntryView entryView = new DayEntryView(entry);

                /* PSI:
                 * Here you can experiment with the new alignment strategy that allows
                 * applications to have entry views show up on the left, the center, or
                 * the right of a day view with a given width. This is needed to support
                 * "vertical bars".
                 */
                // entryView.setPrefWidth(10);
                // entryView.setAlignmentStrategy(AlignmentStrategy.ALIGN_LEFT);

                /* PSI:
                 * Here you can experiment with the new height layout strategy that allows
                 * applications to have entry views show up with their preferred height instead
                 * of a height determined by their start and end time. This is required to
                 * implement the Event Monitoring Panel.
                 */
                // entryView.setHeightLayoutStrategy(HeightLayoutStrategy.COMPUTE_PREF_SIZE);

                if (Math.random() > .7) {
                    final FontIcon node = new FontIcon(FontAwesome.ERASER);
                    node.setIconColor(Color.RED);
                    node.setIconSize(16);
                    entryView.addNode(Pos.BOTTOM_RIGHT, node);
                }

                if (Math.random() > .9) {
                    final FontIcon node = new FontIcon(FontAwesome.CODE);
                    node.setIconColor(Color.BLUE);
                    node.setIconSize(16);
                    entryView.addNode(Pos.BOTTOM_RIGHT, node);
                }

                if (Math.random() > .7) {
                    final FontIcon node = new FontIcon(FontAwesome.QRCODE);
                    node.setIconColor(Color.MEDIUMPURPLE);
                    node.setIconSize(16);
                    entryView.addNode(Pos.BOTTOM_RIGHT, node);
                }

                if (Math.random() > .7) {
                    final FontIcon node = new FontIcon(FontAwesome.SIGN_IN);
                    node.setIconColor(Color.MEDIUMSPRINGGREEN);
                    node.setIconSize(16);
                    entryView.addNode(Pos.TOP_RIGHT, node);
                }

                return entryView;
            });
        }

        Marker marker1 = new Marker();
        marker1.setTitle("My Marker 1");
        marker1.setTime(ZonedDateTime.now().minusHours(1));
        marker1.setMovable(false);
        resourceCalendarView.getMarkers().add(marker1);

        Marker marker2 = new Marker();
        marker2.setTitle("My Marker 2");
        marker2.setTime(ZonedDateTime.now().plusHours(1));
        marker2.setMovable(false);
        marker2.getStyleClass().add("marker2");
        resourceCalendarView.getMarkers().add(marker2);


        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(resourceCalendarView);

        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        resourceCalendarView.setToday(LocalDate.now());
                        resourceCalendarView.setTime(LocalTime.now());
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

        Scene scene = new Scene(stackPane);
        primaryStage.setTitle("Calendar");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    class HelloDayViewCalendar extends Calendar {

        public HelloDayViewCalendar() {
            createEntries(LocalDate.now().minusDays(2));
            createEntries(LocalDate.now().minusDays(1));
            createEntries(LocalDate.now());
            createEntries(LocalDate.now().plusDays(1));
            createEntries(LocalDate.now().plusDays(2));
        }

        private void createEntries(LocalDate startDate) {
            for (int j = 0; j < 5 + (int) (Math.random() * 4); j++) {
                Entry<?> entry = new Entry<>();
                entry.changeStartDate(startDate);
                entry.changeEndDate(startDate);

                entry.setTitle("Entry " + (j + 1));

                int hour = (int) (Math.random() * 23);
                int durationInHours = Math.max(1, Math.min(24 - hour, (int) (Math.random() * 4)));

                LocalTime startTime = LocalTime.of(hour, 0);
                LocalTime endTime = startTime.plusHours(durationInHours);

                entry.changeStartTime(startTime);
                entry.changeEndTime(endTime);

                entry.setCalendar(this);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
