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

package com.calendarfx.view.resources;

import com.calendarfx.view.AllDayView;
import com.calendarfx.view.CalendarHeaderView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.TimeScaleView;
import impl.com.calendarfx.view.resources.DetailedResourcesDayViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.util.Callback;

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
public class DetailedResourcesDayView<T extends Resource<?>> extends DayViewBase {

    private static final String DEFAULT_STYLE = "detailed-resources-day-view";

    /**
     * Constructs a new day view.
     */
    public DetailedResourcesDayView() {
        getStyleClass().add(DEFAULT_STYLE);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DetailedResourcesDayViewSkin(this);
    }

    private final ObjectProperty<Callback<T, Node>> resourceHeaderFactory = new SimpleObjectProperty<>(this,"headerFactory", resource -> {
        Label label = new Label(resource.toString());
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("resource-header-label");
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

    private final ListProperty<T> resources = new SimpleListProperty<>(this, "resources", FXCollections.observableArrayList());

    public final ObservableList<T> getResources() {
        return resources.get();
    }

    public final ListProperty<T> resourcesProperty() {
        return resources;
    }

    public final void setResources(ObservableList<T> resources) {
        this.resources.set(resources);
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
}
