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

package com.calendarfx.app;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;
import impl.com.calendarfx.view.CalendarViewSkin;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * This class is the primary initializing GUI class.
 */



public class CalendarApp extends Application {

    DateTimeFormatter timeFormatter;
    Calendar personalSched;
    Calendar groupSched;


    @Override
    public void start(Stage primaryStage) throws Exception {


        timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        CalendarView calendarView = new CalendarView();


        calendarView.showWeekPage();
        calendarView.setShowPageSwitcher(false);







        manager = new AppManager(this);


        personalSched = new Calendar("Personal Schedule");
        groupSched = new Calendar("Group Schedule");


        personalSched.setStyle(Style.STYLE1);
        groupSched.setStyle(Style.STYLE2);

        CalendarSource myCalendarSource = new CalendarSource("My Schedules");

        calendarView.getCalendarSources().addAll(myCalendarSource);

        calendarView.setRequestedTime(LocalTime.now());

        Button newCustomEvent = createButton("Add Custom Event");
        Button resetSchedule = createButton("Reset Schedule");
        Button loadSchedule = createButton("Load Schedule");
        Button uploadSchedule = createButton("Upload Schedule");
        Button downloadGroup = createButton("Download Group Schedule");
        Button exitProgram = createButton("Exit");

        Method buildBarBoxField = CalendarViewSkin.class.getDeclaredMethod("buildLeftToolBarBox");



        exitProgram.setOnAction(e -> {
            Platform.exit();
            System.exit(1);});

        uploadSchedule.setOnAction(event -> {manager.uploadSchedule(manager.userCourseList);});

        downloadGroup.setOnAction(event -> {manager.syncSchedule();
            myCalendarSource.getCalendars().removeAll(personalSched);
            myCalendarSource.getCalendars().addAll(groupSched);
        });

        try{loadSchedule.setOnAction(event -> {selectFileLoad();
            myCalendarSource.getCalendars().addAll(personalSched);
        });} catch (Exception e) { e.printStackTrace();}

        try{ resetSchedule.setOnAction(event -> {manager.resetState();
            myCalendarSource.getCalendars().removeAll(groupSched, personalSched);
        }); } catch (NullPointerException e) {}

        //addCalendarButton.setOnAction(event -> {newEvent();});//TODO change the button assignment






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
            };
        };

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();

        Scene scene = new Scene(calendarView);
        primaryStage.setTitle("Meet.Me");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();



    }

    public static void main(String[] args) {
        launch(args);
    }

    AppManager manager;




    public void displayCourse(Course course, String decider){
        String recurrenceRule = "RRULE:FREQ=WEEKLY;BYDAY=";


        Interval testInterval = new Interval(LocalDate.parse("2018-09-20"), LocalTime.parse(String.valueOf(course.startTime),timeFormatter), LocalDate.parse("2018-09-20"), LocalTime.parse(String.valueOf(course.endTime), timeFormatter));
        Entry<String> courseEvent = new Entry<>(course.courseID, testInterval);

        if (course.courseDays.contains("Monday")){
            recurrenceRule += " MO,";
        }
        if (course.courseDays.contains("Tuesday")){
            recurrenceRule += " TU,";

        }
        if (course.courseDays.contains("Wednesday")){
            recurrenceRule += " WE,";

        }
        if (course.courseDays.contains("Thursday")){
            recurrenceRule += " TH,";

        }
        if (course.courseDays.contains("Friday")){
            recurrenceRule += " FR";

        }


        courseEvent.setRecurrenceRule(recurrenceRule);


        if (decider == "personal"){
            personalSched.addEntries(courseEvent);
        } else if (decider == "group"){
            groupSched.addEntries(courseEvent);
        }

    }

    /**
     * This method creates the file selection window and passes it to the AppManager.
     */

    public void selectFileLoad() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Schedule PDF File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Portable Document Format", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            manager.loadSchedule(selectedFile);
        }
    }

    /**
     * This is an unused method for showing an error dialog if the user tries to initialize the schedule parser twice.
     */

    public void showSetupError() { //TODO Use this

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Illegal Action");
        alert.setHeaderText("Your Account is already set up!");
        alert.setContentText("Please reset your information before uploading a new schedule.");

        alert.showAndWait();

    }

    /**
     * This is a generic method constructor for a button.
     * @param buttonName The text to be displayed in the button.
     * @return The method returns the created button.
     */

    public Button createButton (String buttonName) {

        Button button = new Button(buttonName);
        button.setFont(new Font ("Arial", 16));

        return button;
    }

    /**
     * This method is called from the GUI to open the dialog menu to select the parameters of a new custom event.  The custom event is then added to the list of courses for the given user.
     */

    public void newEvent(){  //TODO Fix it to add the event to the schedule
        GridPane customPane = new GridPane();
        customPane.setVgap(10);
        Stage customStage = new Stage();
        customStage.setTitle("Create a Custom Event");
        customStage.setScene(new Scene(customPane, 450,450));

        customPane.setPadding(new Insets(10,10,10,10));

        Label courseLabel = new Label("Event Name:");
        Label startTimeLabel = new Label("Start Time:");
        Label endTimeLabel = new Label("End Time:");
        Label daysLabel = new Label("Days:");

        GridPane.setConstraints(courseLabel, 2, 1);
        GridPane.setConstraints(startTimeLabel, 2, 2);
        GridPane.setConstraints(endTimeLabel, 2, 3);
        GridPane.setConstraints(daysLabel, 2, 4);

        TextField courseBox = new TextField("e.g. Baseball Practice, Sleep, ...");
        TextField startTimeBox = new TextField("e.g. 9:00 AM, 1:00 PM, ...");
        TextField endTimeBox = new TextField("e.g. 9:00 AM, 1:00 PM, ...");

        GridPane.setConstraints(courseBox, 4, 1);
        GridPane.setConstraints(startTimeBox, 4, 2);
        GridPane.setConstraints(endTimeBox, 4, 3);

        CheckBox monCheck = new CheckBox("Monday");
        CheckBox tueCheck = new CheckBox("Tuesday");
        CheckBox wedCheck = new CheckBox("Wednesday");
        CheckBox thuCheck = new CheckBox("Thursday");
        CheckBox friCheck = new CheckBox("Friday");
        CheckBox satCheck = new CheckBox("Saturday");
        CheckBox sunCheck = new CheckBox("Sunday");

        GridPane.setConstraints(monCheck, 4, 4);
        GridPane.setConstraints(tueCheck, 4, 5);
        GridPane.setConstraints(wedCheck, 4, 6);
        GridPane.setConstraints(thuCheck, 4, 7);
        GridPane.setConstraints(friCheck, 4, 8);
        GridPane.setConstraints(satCheck, 4, 9);
        GridPane.setConstraints(sunCheck, 4, 10);

        ArrayList<CheckBox> checkBoxes = new ArrayList<>();
        checkBoxes.add(monCheck);
        checkBoxes.add(tueCheck);
        checkBoxes.add(wedCheck);
        checkBoxes.add(thuCheck);
        checkBoxes.add(friCheck);
        checkBoxes.add(satCheck);
        checkBoxes.add(sunCheck);

        Button confirmButton = new Button("Submit");

        GridPane.setConstraints(confirmButton, 4, 12);

        customPane.getChildren().addAll(courseLabel, startTimeLabel, endTimeLabel, daysLabel);
        customPane.getChildren().addAll(courseBox, startTimeBox, endTimeBox, monCheck, tueCheck, wedCheck, thuCheck, friCheck, satCheck, sunCheck, confirmButton);

        customStage.show();


        /**
         * This handle opens a dialog window for the user to input the information of their custom event and creates it as a course object within the list of the user's courses.
         */
        confirmButton.setOnAction(event -> {
            ArrayList<String> days = new ArrayList<>();
            String name;
            String startTime;
            String endTime;

            //TODO This is pretty bad

            if(monCheck.isSelected()){ days.add("Monday"); }
            if(tueCheck.isSelected()){ days.add("Tuesday"); }
            if(wedCheck.isSelected()){ days.add("Wednesday"); }
            if(thuCheck.isSelected()){ days.add("Thursday"); }
            if(friCheck.isSelected()){ days.add("Friday"); }
            if(satCheck.isSelected()){ days.add("Saturday"); }
            if(sunCheck.isSelected()){ days.add("Sunday"); }

            name = courseBox.getText();
            startTime = startTimeBox.getText();
            endTime = endTimeBox.getText();

            Course customEvent = new Course(name, startTime, endTime, days);

            manager.userCourseList.add(customEvent);

            System.out.println("A custom event named " + customEvent.courseID + " was created and will take place between " + customEvent.Time.get(0) + " and " + customEvent.Time.get(1));

            customStage.hide();

        });


    }





}