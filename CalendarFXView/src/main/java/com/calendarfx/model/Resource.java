package com.calendarfx.model;

import com.calendarfx.view.DateControl;
import com.calendarfx.view.ResourcesView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A resource represents a person or a machine. Resources can be edited via
 * the {@link ResourcesView}. A typical use case would be the allocation of
 * personnel in a hairdresser salon. A resource might be available or not.
 * This can be expressed via the {@link #availabilityCalendarProperty()}.
 *
 * @param <T> the type of the wrapped / referenced business object (person or machine).
 *
 * @see ResourcesView#getResources()
 * @see DateControl#editAvailabilityProperty()
 * @see DateControl#availabilityGridProperty()
 * @see DateControl#availabilityFillProperty()
 */
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

    /**
     * An (optional) user object.
     *
     * @return the user object (e.g. the person or the calendar data source).
     */
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

    /**
     * A resource can be "booked" or "allocated to tasks". Those bookings / allocations are stored
     * in this calendar.
     *
     * @return the resource calendar with the resource's bookings
     */
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

    /**
     * A resource might be available or not. This can be determined via the "availability"
     * calendar.
     *
     * @return the resource's availability calendar
     *
     * @see DateControl#editAvailabilityProperty()
     * @see DateControl#availabilityGridProperty()
     * @see DateControl#availabilityFillProperty()
     */
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
