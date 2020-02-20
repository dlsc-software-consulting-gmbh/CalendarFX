package com.calendarfx.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.ZonedDateTime;

public class Marker {

    public Marker() {
    }

    private final BooleanProperty movable = new SimpleBooleanProperty(this, "movable", true);

    public final boolean isMovable() {
        return movable.get();
    }

    public final BooleanProperty movableProperty() {
        return movable;
    }

    public final void setMovable(boolean movable) {
        this.movable.set(movable);
    }

    private final ObjectProperty<ZonedDateTime> time = new SimpleObjectProperty<>(this, "time", ZonedDateTime.now());

    public ZonedDateTime getTime() {
        return time.get();
    }

    public ObjectProperty<ZonedDateTime> timeProperty() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time.set(time);
    }

    private final StringProperty title = new SimpleStringProperty(this, "title", "Untitled");

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    private final StringProperty style = new SimpleStringProperty(this, "style");

    public final String getStyle() {
        return style.get();
    }

    public final StringProperty styleProperty() {
        return style;
    }

    public final void setStyle(String style) {
        this.style.set(style);
    }

    private final ListProperty<String> styleClass = new SimpleListProperty<>(this, "styleClass", FXCollections.observableArrayList());

    public final ObservableList<String> getStyleClass() {
        return styleClass.get();
    }

    public final ListProperty<String> styleClassProperty() {
        return styleClass;
    }

    public final void setStyleClass(ObservableList<String> styleClass) {
        this.styleClass.set(styleClass);
    }
}
