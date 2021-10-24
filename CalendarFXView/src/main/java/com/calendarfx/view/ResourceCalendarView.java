package com.calendarfx.view;

import com.calendarfx.model.Marker;
import impl.com.calendarfx.view.ResourceCalendarViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.util.Callback;

public class ResourceCalendarView<T> extends DayViewBase {

    public ResourceCalendarView() {
        getStyleClass().add("resource-calendar-view");

        setEnableCurrentTimeMarker(false);
        setScrollingEnabled(true);
        setHourHeight(40);
        setHoursLayoutStrategy(DayViewBase.HoursLayoutStrategy.FIXED_HOUR_HEIGHT);

        ListChangeListener<? super T> l = change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(resource -> {
                        DayView dayView = new DayView();
                        bind(dayView, true);
                        partialUnbinding(dayView);
                        dayViewMap.put(resource, dayView);
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(dayViewMap::remove);
                }
            }
        };

        resources.addListener(l);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResourceCalendarViewSkin(this);
    }

    private final BooleanProperty showTimeScale = new SimpleBooleanProperty(this, "showTimeScale", true);

    /**
     * Controls whether the time scale shold be shown on the left-hand side or not.
     *
     * @return true if the scale should be shown
     */
    public BooleanProperty showTimeScaleProperty() {
        return showTimeScale;
    }

    public boolean isShowTimeScale() {
        return showTimeScale.get();
    }

    public void setShowTimeScale(boolean showTimeScale) {
        this.showTimeScale.set(showTimeScale);
    }

    private final ListProperty<Marker> markers = new SimpleListProperty<>(this, "markers", FXCollections.observableArrayList());

    public final ObservableList<Marker> getMarkers() {
        return markers.get();
    }

    public final ListProperty<Marker> markersProperty() {
        return markers;
    }

    public final void setMarkers(ObservableList<Marker> markers) {
        this.markers.set(markers);
    }

    private void partialUnbinding(DayView otherControl) {
        Bindings.unbindBidirectional(otherControl.draggedEntryProperty(), draggedEntryProperty());
        Bindings.unbindContentBidirectional(otherControl.getCalendarVisibilityMap(), getCalendarVisibilityMap());
        Bindings.unbindContentBidirectional(otherControl.getCalendarSources(), getCalendarSources());
    }

    private final BooleanProperty showScrollBar = new SimpleBooleanProperty(this, "showScrollBar", true);

    public final boolean isShowScrollBar() {
        return showScrollBar.get();
    }

    public final BooleanProperty showScrollBarProperty() {
        return showScrollBar;
    }

    public final void setShowScrollBar(boolean showScrollBar) {
        this.showScrollBar.set(showScrollBar);
    }

    private final ObjectProperty<Callback<T, Node>> headerFactory = new SimpleObjectProperty<>(this, "headerFactory", it-> new Label("Header"));

    public final Callback<T, Node> getHeaderFactory() {
        return headerFactory.get();
    }

    public final ObjectProperty<Callback<T, Node>> headerFactoryProperty() {
        return headerFactory;
    }

    public final void setHeaderFactory(Callback<T, Node> headerFactory) {
        this.headerFactory.set(headerFactory);
    }

    private final ReadOnlyMapProperty<T, DayView> dayViewMap = new ReadOnlyMapWrapper<>(this, "dayViewMap", FXCollections.observableHashMap());

    public final ObservableMap getDayViewMap() {
        return dayViewMap.get();
    }

    public final ReadOnlyMapProperty<T, DayView> dayViewMapProperty() {
        return dayViewMap;
    }

    public final DayView getDayView(T resource) {
        return dayViewMap.get(resource);
    }

    private final ListProperty<T> resources = new SimpleListProperty<>(this, "resources", FXCollections.observableArrayList());

    public ObservableList<T> getResources() {
        return resources.get();
    }

    public ListProperty<T> resourcesProperty() {
        return resources;
    }

    public void setResources(ObservableList<T> resources) {
        this.resources.set(resources);
    }
}
