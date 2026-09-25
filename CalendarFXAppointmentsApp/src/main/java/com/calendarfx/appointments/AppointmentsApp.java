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

package com.calendarfx.appointments;

import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.model.Resource;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import com.calendarfx.view.DayViewBase.GridType;
import com.calendarfx.view.DayViewBase.HoursLayoutStrategy;
import com.calendarfx.view.ResourcesView;
import com.calendarfx.view.ResourcesView.Type;
import com.calendarfx.view.VirtualGrid;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * A sample application showing how the {@link ResourcesView} can be used to
 * build an appointment calendar for a hairdresser salon. The application shows
 * either several stylists next to each other for a single day, or a single
 * stylist for an entire week.
 */
public class AppointmentsApp extends Application {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy");

    private static final DateTimeFormatter WEEK_START_FORMATTER = DateTimeFormatter.ofPattern("d");

    private final List<Resource<Stylist>> stylists = SalonData.createStylists();

    private final ResourcesView<Resource<Stylist>> resourcesView = new ResourcesView<>();

    @Override
    public void start(Stage primaryStage) {
        configureView();

        BorderPane container = new BorderPane();
        container.getStyleClass().add("container");
        container.setTop(createToolBar());
        container.setCenter(resourcesView);

        Scene scene = new Scene(container);
        scene.getStylesheets().add(Objects.requireNonNull(AppointmentsApp.class.getResource("appointments.css")).toExternalForm());

        primaryStage.setTitle("Appointments");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1100);
        primaryStage.setHeight(850);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void configureView() {
        resourcesView.setType(Type.RESOURCES_OVER_DATES);
        resourcesView.setNumberOfDays(1);
        resourcesView.setScrollingEnabled(false);
        resourcesView.setShowAllDayView(false);
        resourcesView.setShowDetailsUponEntryCreation(false);
        resourcesView.setCreateEntryClickCount(1);

        // only show the hours during which the salon is open
        resourcesView.setStartTime(SalonData.OPENING_TIME);
        resourcesView.setEndTime(SalonData.CLOSING_TIME);
        resourcesView.setEarlyLateHoursStrategy(EarlyLateHoursStrategy.HIDE);
        resourcesView.setHoursLayoutStrategy(HoursLayoutStrategy.FIXED_HOUR_HEIGHT);
        resourcesView.setHourHeight(60);

        // appointments are scheduled in steps of 15 minutes
        VirtualGrid grid = new VirtualGrid("15 Minutes", "15 Min", ChronoUnit.MINUTES, 15);
        resourcesView.setGridType(GridType.CUSTOM);
        resourcesView.setGridLines(grid);
        resourcesView.setVirtualGrid(grid);
        resourcesView.setAvailabilityGrid(grid);

        resourcesView.setResourceHeaderFactory(this::createResourceHeader);
        resourcesView.setEntryFactory(param -> {
            ZonedDateTime time = grid.adjustTime(param.getZonedDateTime(), false, param.getDateControl().getFirstDayOfWeek());

            Entry<Object> entry = new Entry<>("New Appointment");
            entry.setInterval(new Interval(time.toLocalDateTime(), time.toLocalDateTime().plusMinutes(45), time.getZone()));
            return entry;
        });

        resourcesView.getResources().setAll(stylists);
    }

    private Label createResourceHeader(Resource<Stylist> resource) {
        Stylist stylist = resource.getUserObject();

        Label label = new Label(stylist.name());
        label.setAlignment(Pos.CENTER);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.getStyleClass().addAll("resource-header", stylist.style() + "-resource-header");
        return label;
    }

    private HBox createToolBar() {
        Label titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(Bindings.createStringBinding(this::createTitle, resourcesView.dateProperty(), resourcesView.numberOfDaysProperty()));

        ToggleButton scheduleButton = new ToggleButton("Change schedule");
        scheduleButton.selectedProperty().bindBidirectional(resourcesView.editAvailabilityProperty());

        Button previousButton = new Button("<");
        previousButton.setOnAction(evt -> resourcesView.setDate(resourcesView.getDate().minusDays(resourcesView.getNumberOfDays())));

        Button todayButton = new Button("Today");
        todayButton.setOnAction(evt -> resourcesView.setDate(LocalDate.now()));

        Button nextButton = new Button(">");
        nextButton.setOnAction(evt -> resourcesView.setDate(resourcesView.getDate().plusDays(resourcesView.getNumberOfDays())));

        ChoiceBox<Resource<Stylist>> stylistBox = new ChoiceBox<>();
        stylistBox.getItems().setAll(stylists);
        stylistBox.setValue(stylists.get(0));

        ToggleGroup viewGroup = new ToggleGroup();

        ToggleButton dayButton = new ToggleButton("Day view");
        dayButton.setToggleGroup(viewGroup);
        dayButton.setSelected(true);

        ToggleButton weekButton = new ToggleButton("Week view");
        weekButton.setToggleGroup(viewGroup);

        // the choice box is only needed for the week view, which shows a single stylist
        stylistBox.visibleProperty().bind(weekButton.selectedProperty());
        stylistBox.managedProperty().bind(weekButton.selectedProperty());

        Runnable updateView = () -> {
            if (weekButton.isSelected()) {
                resourcesView.setNumberOfDays(7);
                resourcesView.setAdjustToFirstDayOfWeek(true);
                resourcesView.getResources().setAll(stylistBox.getValue());
            } else {
                resourcesView.setAdjustToFirstDayOfWeek(false);
                resourcesView.setNumberOfDays(1);
                resourcesView.getResources().setAll(stylists);
            }
        };

        // the toggle group must never end up with no selection at all
        viewGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            } else {
                updateView.run();
            }
        });

        stylistBox.valueProperty().addListener(it -> updateView.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navigationBox = new HBox(previousButton, todayButton, nextButton);
        navigationBox.getStyleClass().add("navigation-box");

        HBox viewBox = new HBox(dayButton, weekButton);
        viewBox.getStyleClass().add("view-box");

        HBox toolBar = new HBox(titleLabel, spacer, scheduleButton, navigationBox, stylistBox, viewBox);
        toolBar.getStyleClass().add("tool-bar");
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(10));
        return toolBar;
    }

    private String createTitle() {
        LocalDate startDate = resourcesView.getDate();

        if (resourcesView.getNumberOfDays() == 1) {
            return DAY_FORMATTER.format(startDate);
        }

        LocalDate endDate = startDate.plusDays(resourcesView.getNumberOfDays() - 1L);
        return WEEK_START_FORMATTER.format(startDate) + " - " + DAY_FORMATTER.format(endDate);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
