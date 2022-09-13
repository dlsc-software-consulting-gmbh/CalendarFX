package com.calendarfx.view.resources;

import com.calendarfx.view.DayViewBase;
import impl.com.calendarfx.view.resources.MultiResourceDayViewContainerSkin;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;

public class MultiResourceDayViewContainer<T extends Resource<?>> extends DayViewBase {

    public MultiResourceDayViewContainer() {
        getStyleClass().add("multi-resource-day-view-container");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MultiResourceDayViewContainerSkin<>(this);
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
}

