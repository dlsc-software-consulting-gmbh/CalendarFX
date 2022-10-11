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

package com.calendarfx.view;

import com.calendarfx.model.Resource;
import impl.com.calendarfx.view.ResourcesViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Consumer;

import static com.calendarfx.view.RequestEvent.REQUEST_ENTRY;

/**
 * The detailed day view is a composite control consisting of a {@link DayView},
 * an {@link AllDayView}, an {@link CalendarHeaderView}, and a
 * {@link TimeScaleView}. The image below shows the standard appearance of the
 * view. The second image shows the same view with the optional agenda view made
 * visible.
 *
 * <img src="doc-files/detailed-day-view.png" alt="Detailed Day View">
 * <img src="doc-files/detailed-day-view-agenda.png" alt="Detailed Day View Agenda">
 *
 */
public class ResourcesView<T extends Resource<?>> extends DayViewBase {

    private static final String DEFAULT_STYLE = "resources-view";


    /**
     * Constructs a new day view.
     */
    public ResourcesView() {
        getStyleClass().add(DEFAULT_STYLE);
        setShowToday(false);
        setGridType(GridType.CUSTOM);

        // calling "editEntry" with "false" flag because we do not want to change the start date of the view
        addEventHandler(REQUEST_ENTRY, evt -> maybeRunAndConsume(evt, e -> editEntry(evt.getEntry(), false)));

        VirtualGrid grid = new VirtualGrid("Editing Grid", "Editing Grid", ChronoUnit.MINUTES, 20);

        setVirtualGrid(grid);
        setAvailabilityGrid(grid);
        setGridLines(grid);

        Label weekNumberLabel = new Label();
        weekNumberLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        weekNumberLabel.getStyleClass().add("week-number-label");
        weekNumberLabel.setAlignment(Pos.CENTER);
        weekNumberLabel.textProperty().bind(Bindings.createStringBinding(() -> DateTimeFormatter.ofPattern("w").format(getDate()), dateProperty()));
        setUpperLeftCorner(weekNumberLabel);

        InvalidationListener adjustListener = it -> maybeAdjustToFirstDayOfWeek();
        dateProperty().addListener(adjustListener);
        adjustToFirstDayOfWeekProperty().addListener(adjustListener);
        maybeAdjustToFirstDayOfWeek();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResourcesViewSkin(this);
    }

    private void maybeAdjustToFirstDayOfWeek() {
        if (isAdjustToFirstDayOfWeek()) {
            setDate(getDate().with(TemporalAdjusters.previousOrSame(getFirstDayOfWeek())));
        }
    }

    /**
     * A list of possible visualization types that the resources view can support.
     */
    public enum Type {
        /**
         * Display the resources at the very top and then one or more days below each
         * resource.
         */
        RESOURCES_OVER_DATES,

        /**
         * Display the dates at the very top and then one or more resources below each
         * date.
         */
        DATES_OVER_RESOURCES
    }

    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(this, "type", Type.RESOURCES_OVER_DATES);

    public final Type getType() {
        return type.get();
    }

    /**
     * Determines the visualization type: resoruces over dates or dates over resources.
     *
     * @return the visualization type
     */
    public final ObjectProperty<Type> typeProperty() {
        return type;
    }

    public final void setType(Type type) {
        this.type.set(type);
    }

    private void maybeRunAndConsume(RequestEvent evt, Consumer<RequestEvent> consumer) {
        if (!evt.isConsumed()) {
            consumer.accept(evt);
            evt.consume();
        }
    }

    private final BooleanProperty adjustToFirstDayOfWeek = new SimpleBooleanProperty(this, "adjustToFirstDayOfWeek", true);

    /**
     * A flag used to indicate that the view should always show the first day of
     * the week (e.g. "Monday") at its beginning even if the
     * {@link #dateProperty()} is set to another day (e.g. "Thursday").
     *
     * @return true if the view always shows the first day of the week
     */
    public final BooleanProperty adjustToFirstDayOfWeekProperty() {
        return adjustToFirstDayOfWeek;
    }

    /**
     * Returns the value of {@link #adjustToFirstDayOfWeekProperty()}.
     *
     * @return true if the view always shows the first day of the week
     */
    public final boolean isAdjustToFirstDayOfWeek() {
        return adjustToFirstDayOfWeekProperty().get();
    }

    /**
     * Sets the value of {@link #adjustToFirstDayOfWeekProperty()}.
     *
     * @param adjust if true the view will always show the first day of the week
     */
    public final void setAdjustToFirstDayOfWeek(boolean adjust) {
        adjustToFirstDayOfWeekProperty().set(adjust);
    }

    private final ObjectProperty<Callback<T, AllDayView>> allDayViewFactory = new SimpleObjectProperty<>(this, "allDayViewFactory", it-> new AllDayView());

    public final Callback<T, AllDayView> getAllDayViewFactory() {
        return allDayViewFactory.get();
    }

    /**
     * A factory used for creating a new {@link AllDayView} instance for each resource
     * shown in the view.
     *
     * @return a factory for all day views
     */
    public final ObjectProperty<Callback<T, AllDayView>> allDayViewFactoryProperty() {
        return allDayViewFactory;
    }

    public final void setAllDayViewFactory(Callback<T, AllDayView> allDayViewFactory) {
        this.allDayViewFactory.set(allDayViewFactory);
    }

    private final ObjectProperty<Callback<T, WeekDayHeaderView>> weekDayHeaderViewFactory = new SimpleObjectProperty<>(this, "weekDayHeaderViewFactory", it-> new WeekDayHeaderView());

    public final Callback<T, WeekDayHeaderView> getWeekDayHeaderViewFactory() {
        return weekDayHeaderViewFactory.get();
    }

    /**
     * A factory used for creating a new {@link WeekDayHeaderView} instance for each resource
     * shown in the view.
     *
     * @return a factory for week day header views
     */
    public final ObjectProperty<Callback<T, WeekDayHeaderView>> weekDayHeaderViewFactoryProperty() {
        return weekDayHeaderViewFactory;
    }

    public final void setWeekDayHeaderViewFactory(Callback<T, WeekDayHeaderView> factory) {
        this.weekDayHeaderViewFactory.set(factory);
    }

    private final IntegerProperty numberOfDays = new SimpleIntegerProperty(this, "numberOfDays", 7);

    /**
     * Stores the number of days that will be shown by this view.
     *
     * @return the number of days shown by the view
     */
    public final IntegerProperty numberOfDaysProperty() {
        return numberOfDays;
    }

    /**
     * Returns the value of {@link #numberOfDaysProperty()}.
     *
     * @return the number of days shown by the view
     */
    public final int getNumberOfDays() {
        return numberOfDaysProperty().get();
    }

    /**
     * Sets the value of {@link #numberOfDaysProperty()}.
     *
     * @param number the new number of days shown by the view
     */
    public final void setNumberOfDays(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("invalid number of days, must be larger than 0 but was " + number);
        }

        numberOfDaysProperty().set(number);
    }

    private final ObjectProperty<Callback<T, Node>> resourceHeaderFactory = new SimpleObjectProperty<>(this,"headerFactory", resource -> {
        Label label = new Label(resource.toString());
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("resource-header");
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return label;
    });

    public final Callback<T, Node> getResourceHeaderFactory() {
        return resourceHeaderFactory.get();
    }

    public final ObjectProperty<Callback<T, Node>> resourceHeaderFactoryProperty() {
        return resourceHeaderFactory;
    }

    public final void setResourceHeaderFactory(Callback<T, Node> resourceHeaderFactory) {
        this.resourceHeaderFactory.set(resourceHeaderFactory);
    }

    private final ObservableList<T> resources = FXCollections.observableArrayList();

    /**
     * The resources to be shown in this view.
     *
     * @return the list of resources
     */
    public final ObservableList<T> getResources() {
        return resources;
    }

    // show all day view support

    private final BooleanProperty showAllDayView = new SimpleBooleanProperty(this, "showAllDayView", true);

    /**
     * A property used to toggle the visibility of the all day view.
     *
     * @return true if the all day view will be visible
     */
    public final BooleanProperty showAllDayViewProperty() {
        return showAllDayView;
    }

    /**
     * Sets the value of {@link #showAllDayViewProperty()}.
     *
     * @return true if the all day view will be visible
     */
    public final boolean isShowAllDayView() {
        return showAllDayViewProperty().get();
    }

    /**
     * Sets the value of {@link #showAllDayViewProperty()}.
     *
     * @param show
     *            true if the all day view will be visible
     */
    public final void setShowAllDayView(boolean show) {
        showAllDayViewProperty().set(show);
    }

    // show timescale view support

    private final BooleanProperty showTimeScaleView = new SimpleBooleanProperty(this, "showTimeScaleView", true);

    /**
     * A property used to toggle the visibility of the time scale view.
     *
     * @return true if the timescale view will be visible
     */
    public final BooleanProperty showTimeScaleViewProperty() {
        return showTimeScaleView;
    }

    /**
     * Returns the value of {@link #showTimeScaleViewProperty()}.
     *
     * @return true if the timescale view will be visible
     */
    public final boolean isShowTimeScaleView() {
        return showTimeScaleViewProperty().get();
    }

    /**
     * Sets the value of {@link #showTimeScaleViewProperty()}.
     *
     * @param show
     *            if true the timescale view will be visible
     */
    public final void setShowTimeScaleView(boolean show) {
        showTimeScaleViewProperty().set(show);
    }

    // show scrollbar support

    private final BooleanProperty showScrollBar = new SimpleBooleanProperty(this, "showScrollBar", true);

    /**
     * A property used to control the visibility of the vertial scrollbar.
     *
     * @return true if the scrollbar should be shown to the user
     */
    public final BooleanProperty showScrollBarProperty() {
        return showScrollBar;
    }

    /**
     * Sets the value of {@link #showScrollBarProperty()}.
     *
     * @param showScrollBar
     *            if true the scrollbar will be visible
     */
    public final void setShowScrollBar(boolean showScrollBar) {
        this.showScrollBar.set(showScrollBar);
    }

    /**
     * Returns the value of {@link #showScrollBarProperty()}.
     *
     * @return true if the scrollbar will be visible
     */
    public final boolean isShowScrollBar() {
        return showScrollBar.get();
    }

    private final ObjectProperty<Callback<T, WeekView>> weekViewFactory = new SimpleObjectProperty<>(this, "weekViewFactory", resource -> new WeekView());

    public final Callback<T, WeekView> getWeekViewFactory() {
        return weekViewFactory.get();
    }

    /**
     * A factory used for creating a new {@link WeekView} instance for each resource
     * shown in the view.
     *
     * @return a factory for resource week views
     */
    public final ObjectProperty<Callback<T, WeekView>> weekViewFactoryProperty() {
        return weekViewFactory;
    }

    public void setWeekViewFactory(Callback<T, WeekView> weekViewFactory) {
        this.weekViewFactory.set(weekViewFactory);
    }

    private final ObjectProperty<Callback<T, DayView>> dayViewFactory = new SimpleObjectProperty<>(this, "dayViewFactory", resource -> new DayView());

    public final Callback<T, DayView> getDayViewFactory() {
        return dayViewFactory.get();
    }

    /**
     * A factory used for creating a new {@link DayView} instance for a resource day
     * shown in the view.
     *
     * @return a factory for resource day views
     */
    public final ObjectProperty<Callback<T, DayView>> dayViewFactoryProperty() {
        return dayViewFactory;
    }

    public void setDayViewFactory(Callback<T, DayView> dayViewFactory) {
        this.dayViewFactory.set(dayViewFactory);
    }

    private final ObjectProperty<Node> upperLeftCorner = new SimpleObjectProperty<>(this, "upperLeftCorner");

    public final Node getUpperLeftCorner() {
        return upperLeftCorner.get();
    }

    /**
     * Specifies a node that will be placed in the upper left corner of the view.
     *
     * @return the upper left corner node
     */
    public final ObjectProperty<Node> upperLeftCornerProperty() {
        return upperLeftCorner;
    }

    public final void setUpperLeftCorner(Node upperLeftCorner) {
        this.upperLeftCorner.set(upperLeftCorner);
    }

    private final ObjectProperty<Node> upperRightCorner = new SimpleObjectProperty<>(this, "upperRightCorner", new Region());

    public final Node getUpperRightCorner() {
        return upperRightCorner.get();
    }

    /**
     * Specifies a node that will be placed in the upper right corner of the view.
     *
     * @return the upper right corner node
     */
    public final ObjectProperty<Node> upperRightCornerProperty() {
        return upperRightCorner;
    }

    public final void setUpperRightCorner(Node upperRightCorner) {
        this.upperRightCorner.set(upperRightCorner);
    }

    private final ObjectProperty<Callback<ResourcesView<T>, Region>> smallSeparatorFactory = new SimpleObjectProperty<>(this, "smallSeparatorFactory", it-> {
        Region region = new Region();
        region.getStyleClass().add("small-separator");
        return region;
    });

    public final Callback<ResourcesView<T>, Region> getSmallSeparatorFactory() {
        return smallSeparatorFactory.get();
    }

    public final ObjectProperty<Callback<ResourcesView<T>, Region>> smallSeparatorFactoryProperty() {
        return smallSeparatorFactory;
    }

    public final void setSmallSeparatorFactory(Callback<ResourcesView<T>, Region> smallSeparatorFactory) {
        this.smallSeparatorFactory.set(smallSeparatorFactory);
    }

    private final ObjectProperty<Callback<ResourcesView<T>, Region>> largeSeparatorFactory = new SimpleObjectProperty<>(this, "largeSeparatorFactory", it-> {
        Region region = new Region();
        region.getStyleClass().add("large-separator");
        return region;
    });


    public final Callback<ResourcesView<T>, Region> getLargeSeparatorFactory() {
        return largeSeparatorFactory.get();
    }

    /**
     * A factory used for creating the vertical separators between the resources.
     *
     * @return the resource separator factory
     */
    public final ObjectProperty<Callback<ResourcesView<T>, Region>> largeSeparatorFactoryProperty() {
        return largeSeparatorFactory;
    }

    public final void setLargeSeparatorFactory(Callback<ResourcesView<T>, Region> largeSeparatorFactory) {
        this.largeSeparatorFactory.set(largeSeparatorFactory);
    }
}
