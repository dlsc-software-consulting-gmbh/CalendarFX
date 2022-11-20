package com.calendarfx.app;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import java.time.LocalDate;
import java.time.LocalTime;

public class AppStart extends Application{
    @Override
    public void start(Stage primaryStage) {
        CalendarView calCore = new CalendarView();//alldayview
        calCore.setEnableTimeZoneSupport(true);
        calCore.setShowDeveloperConsole(true);
        Canvas testCanvas = new Canvas(150, 150);

        Calendar positive = new Calendar("Positive");
        Calendar mildNegative = new Calendar("Mild Negative");
        Calendar negative = new Calendar("Negative");
        Calendar trigger = new Calendar("Trigger Event");

        positive.setShortName("pos");
        mildNegative.setShortName("mng");
        negative.setShortName("neg");
        trigger.setShortName("trg");

        positive.setStyle(Style.STYLE1);
        mildNegative.setStyle(Style.STYLE2);
        negative.setStyle(Style.STYLE3);
        trigger.setStyle(Style.STYLE4);

        CalendarSource calCoreSource = new CalendarSource("Core");
        calCoreSource.getCalendars().addAll(positive, mildNegative, negative, trigger);

        calCore.getCalendarSources().setAll(calCoreSource);
        calCore.setRequestedTime(LocalTime.now());

        StackPane initialPane = new StackPane();
        initialPane.getChildren().addAll(calCore); // introPane);

        Thread timeUpdate = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calCore.setToday(LocalDate.now());
                        calCore.setTime(LocalTime.now());
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
        timeUpdate.setPriority(Thread.MIN_PRIORITY);
        timeUpdate.setDaemon(true);
        timeUpdate.start();

        Scene initialScene = new Scene(initialPane);
        initialScene.focusOwnerProperty().addListener(it -> System.out.println("focus owner: " + initialScene.getFocusOwner()));
        CSSFX.start(initialScene);

        primaryStage.setTitle("WellTracked");
        primaryStage.setScene(initialScene);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(800);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
