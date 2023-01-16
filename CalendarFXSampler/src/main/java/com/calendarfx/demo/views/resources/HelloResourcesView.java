/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *  Copyright (C) 2006 Google Inc.
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

package com.calendarfx.demo.views.resources;

import com.calendarfx.demo.CalendarFXDateControlSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Resource;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DateControl.Layout;
import com.calendarfx.view.DayViewBase.AvailabilityEditingEntryBehaviour;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import com.calendarfx.view.DayViewBase.GridType;
import com.calendarfx.view.ResourcesView;
import com.calendarfx.view.ResourcesView.Type;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class HelloResourcesView extends CalendarFXDateControlSample {

    private ResourcesView resourcesView;

    public static final int DATA_GENERATION_SEED = 11011;

    final Random random = new Random(DATA_GENERATION_SEED);

    @Override
    public String getSampleName() {
        return "ResourcesView";
    }

    @Override
    public String getSampleDescription() {
        return "The resources view is used to display allocations of resources, e.g. people working at a hairdresser and their customer appointments.";
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return ResourcesView.class;
    }

    @Override
    protected boolean isSupportingDeveloperConsole() {
        return false;
    }

    @Override
    public Node getControlPanel() {
        ToggleButton availabilityButton = new ToggleButton("Edit Schedule");
        availabilityButton.selectedProperty().bindBidirectional(resourcesView.editAvailabilityProperty());

        DatePicker datePicker = new DatePicker();
        datePicker.valueProperty().bindBidirectional(resourcesView.dateProperty());

        ChoiceBox<Integer> daysBox = new ChoiceBox<>();
        daysBox.getItems().setAll(1, 2, 3, 4, 5, 7, 10, 14);
        daysBox.setValue(resourcesView.getNumberOfDays());
        daysBox.valueProperty().addListener(it -> resourcesView.setNumberOfDays(daysBox.getValue()));

        ChoiceBox<Integer> numberOfResourcesBox = new ChoiceBox<>();
        numberOfResourcesBox.getItems().setAll(1, 2, 3, 4, 5);
        numberOfResourcesBox.setValue(resourcesView.getResources().size());
        numberOfResourcesBox.valueProperty().addListener(it -> resourcesView.getResources().setAll(createResources(numberOfResourcesBox.getValue())));

        Button memoryTestButton = new Button("Test Heap");
        memoryTestButton.setOnAction(evt -> {
            Thread thread = new Thread(() -> {
                Runtime r = Runtime.getRuntime();
                int counter = 0;
                while (true) {
                    counter++;
                    final int fc = counter;

                    Platform.runLater(() -> {
                        List<Resource<String>> resources = createResources(5);
                        resourcesView.getResources().setAll(resources);

                        resources.forEach(resource -> {
                            CalendarSource source = new CalendarSource("Default");

                            HelloDayViewCalendar calendar1 = new HelloDayViewCalendar(random.nextLong());
                            calendar1.generateBaseEntries();
                            calendar1.setStyle(Style.STYLE1);
                            source.getCalendars().add(calendar1);

                            HelloDayViewCalendar calendar2 = new HelloDayViewCalendar(random.nextLong());
                            calendar2.generateBaseEntries();
                            calendar2.setStyle(Style.STYLE2);
                            source.getCalendars().add(calendar2);

                            HelloDayViewCalendar calendar3 = new HelloDayViewCalendar(random.nextLong());
                            calendar3.generateBaseEntries();
                            calendar3.setStyle(Style.STYLE3);
                            source.getCalendars().add(calendar3);

                            HelloDayViewCalendar calendar4 = new HelloDayViewCalendar(random.nextLong());
                            calendar4.generateTopEntries();
                            calendar4.setStyle(Style.STYLE4);
                            source.getCalendars().add(calendar4);

                            resource.getCalendarSources().setAll(source);
                        });

                        System.out.println(fc + ": free: " + (r.freeMemory() / 1_000) + " kb");
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.setName("Memory Test Thread");
            thread.setDaemon(true);
            thread.start();
        });

        ChoiceBox<Integer> clicksBox = new ChoiceBox<>();
        clicksBox.getItems().setAll(1, 2, 3);
        clicksBox.setValue(resourcesView.getCreateEntryClickCount());
        clicksBox.valueProperty().addListener(it -> resourcesView.setCreateEntryClickCount(clicksBox.getValue()));

        ChoiceBox<AvailabilityEditingEntryBehaviour> behaviourBox = new ChoiceBox<>();
        behaviourBox.getItems().setAll(AvailabilityEditingEntryBehaviour.values());
        behaviourBox.valueProperty().bindBidirectional(resourcesView.entryViewAvailabilityEditingBehaviourProperty());

        ChoiceBox<Type> typeBox = new ChoiceBox<>();
        typeBox.getItems().setAll(Type.values());
        typeBox.valueProperty().bindBidirectional(resourcesView.typeProperty());
        typeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Type object) {
                if (object != null) {
                    if (object.equals(Type.RESOURCES_OVER_DATES)) {
                        return "Resources over date";
                    } else if (object.equals(Type.DATES_OVER_RESOURCES)) {
                        return "Date over resources";
                    } else {
                        return "unknown view type: " + object.name();
                    }
                }
                return "";
            }

            @Override
            public Type fromString(String string) {
                return null;
            }
        });

        ChoiceBox<Layout> layoutBox = new ChoiceBox<>();
        layoutBox.getItems().setAll(Layout.values());
        layoutBox.valueProperty().bindBidirectional(resourcesView.layoutProperty());
        layoutBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Layout object) {
                if (object != null) {
                    if (object.equals(Layout.SWIMLANE)) {
                        return "Swim Lanes";
                    } else if (object.equals(Layout.STANDARD)) {
                        return "Standard";
                    } else {
                        return "unknown layout type: " + object.name();
                    }
                }
                return "";
            }

            @Override
            public Layout fromString(String string) {
                return null;
            }
        });

        ChoiceBox<GridType> gridTypeBox = new ChoiceBox<>();
        gridTypeBox.getItems().setAll(GridType.values());
        gridTypeBox.valueProperty().bindBidirectional(resourcesView.gridTypeProperty());

        CheckBox infiniteScrolling = new CheckBox("Infinite scrolling");
        infiniteScrolling.selectedProperty().bindBidirectional(resourcesView.scrollingEnabledProperty());
        infiniteScrolling.setDisable(true);

        CheckBox adjustBox = new CheckBox("Adjust first day of week");
        adjustBox.selectedProperty().bindBidirectional(resourcesView.adjustToFirstDayOfWeekProperty());

        CheckBox scrollbarBox = new CheckBox("Show scrollbar");
        scrollbarBox.selectedProperty().bindBidirectional(resourcesView.showScrollBarProperty());

        CheckBox timescaleBox = new CheckBox("Show timescale");
        timescaleBox.selectedProperty().bindBidirectional(resourcesView.showTimeScaleViewProperty());

        CheckBox allDayBox = new CheckBox("Show all day events");
        allDayBox.selectedProperty().bindBidirectional(resourcesView.showAllDayViewProperty());

        CheckBox detailsBox = new CheckBox("Show details upon creation");
        detailsBox.selectedProperty().bindBidirectional(resourcesView.showDetailsUponEntryCreationProperty());

        CheckBox flipBox = new CheckBox("Enable start / end flip over");
        flipBox.selectedProperty().bindBidirectional(resourcesView.enableStartAndEndTimesFlipProperty());

        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.valueProperty().bindBidirectional(resourcesView.entryViewAvailabilityEditingOpacityProperty());

        return new VBox(10, availabilityButton, new Label("View type"), typeBox, layoutBox, datePicker, infiniteScrolling, adjustBox, memoryTestButton, new Label("Number of resources"), numberOfResourcesBox, new Label("Number of days"), daysBox, new Label("Clicks to create"), clicksBox,
                new Label("Availability Behaviour"), behaviourBox, new Label("Availability Opacity"), slider, new Label("Grid Type"), gridTypeBox, scrollbarBox, timescaleBox, allDayBox, detailsBox, flipBox);
    }

    @Override
    protected DateControl createControl() {
        resourcesView = new ResourcesView();
        resourcesView.setScrollingEnabled(false);
        resourcesView.setType(Type.DATES_OVER_RESOURCES);
        resourcesView.setNumberOfDays(5);
        resourcesView.setCreateEntryClickCount(1);
        resourcesView.setGridType(GridType.CUSTOM);
        resourcesView.setEarlyLateHoursStrategy(EarlyLateHoursStrategy.HIDE);
        resourcesView.getResources().setAll(createResources(3));
        resourcesView.setShowDetailsUponEntryCreation(false);

        return resourcesView;
    }

    private List<Resource<String>> createResources(int count) {
        ObservableList<Resource<String>> result = FXCollections.observableArrayList();
        switch (count) {
            case 1:
                result.addAll(create("Dirk", Style.STYLE1));
                break;
            case 2:
                result.addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2));
                break;
            case 3:
                result.addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2), create("Philip", Style.STYLE3));
                break;
            case 4:
                result.addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2), create("Philip", Style.STYLE3), create("Jule", Style.STYLE4));
                break;
            case 5:
                result.addAll(create("Dirk", Style.STYLE1), create("Katja", Style.STYLE2), create("Philip", Style.STYLE3), create("Jule", Style.STYLE4), create("Armin", Style.STYLE5));
                break;
        }

        return result;
    }

    private Resource<String> create(String name, Style style) {
        Resource<String> resource = new Resource(name);
        resource.getAvailabilityCalendar().setName("Availability of " + name);
//        resource.getCalendars().get(0).setStyle(style);
//        resource.getCalendars().get(0).setUserObject(resource);
//        resource.getCalendarSources().get(0).getCalendars().add(new Calendar("Second", resource));
        fillAvailabilities(resource.getAvailabilityCalendar());
        return resource;
    }

    private void fillAvailabilities(Calendar calendar) {
        LocalDate date = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int i = 0; i < 14; i++) {
            // fourteen days is enough for this demo
            Entry morning = new Entry("Morning");
            morning.setInterval(date, LocalTime.MIN, date, LocalTime.of(8, 0));
            calendar.addEntry(morning);
            Entry noon = new Entry("Noon");
            noon.setInterval(date, LocalTime.of(12, 0), date, LocalTime.of(13, 0));
            calendar.addEntry(noon);
            Entry evening = new Entry("Evening");
            evening.setInterval(date, LocalTime.of(18, 0), date, LocalTime.MAX);
            calendar.addEntry(evening);
            date = date.plusDays(1);
        }
    }

    class HelloDayViewCalendar extends Calendar {

        final Random dataRandom = new Random();

        public HelloDayViewCalendar(long dataSeed) {
            dataRandom.setSeed(dataSeed);
        }

        public void generateBaseEntries() {
            createEntries(LocalDate.now(), Entry::new);
            createEntries(LocalDate.now().plusDays(1), Entry::new);
            createEntries(LocalDate.now().plusDays(2), Entry::new);
            createEntries(LocalDate.now().plusDays(3), Entry::new);
            createEntries(LocalDate.now().plusDays(4), Entry::new);
        }

        public void generateTopEntries() {
            createEntries(LocalDate.now(), Entry::new);
        }

        private <T extends Entry<?>> void createEntries(LocalDate startDate, Supplier<T> entryProducer) {
            for (int j = 0; j < 5 + (int) (dataRandom.nextDouble() * 4); j++) {
                T entry = entryProducer.get();
                entry.changeStartDate(startDate);
                entry.changeEndDate(startDate);

                String s = entry.getClass().getSimpleName();
                entry.setTitle(s + (j + 1));

                int hour = (int) (dataRandom.nextDouble() * 23);
                int durationInHours = Math.max(1, Math.min(24 - hour, (int) (dataRandom.nextDouble() * 4)));

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
