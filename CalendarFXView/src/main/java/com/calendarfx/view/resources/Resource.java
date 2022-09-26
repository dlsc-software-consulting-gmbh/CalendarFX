package com.calendarfx.view.resources;

import com.calendarfx.model.Calendar;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Resource<T> {

    public Resource() {
    }

    public Resource(T userObject) {
        setUserObject(userObject);
    }

    private final ObjectProperty<T> userObject = new SimpleObjectProperty<>(this, "userObject");

    public final T getUserObject() {
        return userObject.get();
    }

    public final ObjectProperty<T> userObjectProperty() {
        return userObject;
    }

    public final void setUserObject(T userObject) {
        this.userObject.set(userObject);
    }

    public final ObjectProperty<Calendar> calendar = new SimpleObjectProperty<>(this, "calendar", new Calendar());

    public final Calendar getCalendar() {
        return calendar.get();
    }

    public final ObjectProperty<Calendar> calendarProperty() {
        return calendar;
    }

    public final void setCalendar(Calendar calendar) {
        this.calendar.set(calendar);
    }

    public final ObjectProperty<Calendar> availabilityCalendar = new SimpleObjectProperty<>(this, "availabilityCalendar", new Calendar());

    public Calendar getAvailabilityCalendar() {
        return availabilityCalendar.get();
    }

    public ObjectProperty<Calendar> availabilityCalendarProperty() {
        return availabilityCalendar;
    }

    public void setAvailabilityCalendar(Calendar availabilityCalendar) {
        this.availabilityCalendar.set(availabilityCalendar);
    }

    @Override
    public String toString() {
        if (getUserObject() != null) {
            return getUserObject().toString();
        }

        return super.toString();
    }
}
