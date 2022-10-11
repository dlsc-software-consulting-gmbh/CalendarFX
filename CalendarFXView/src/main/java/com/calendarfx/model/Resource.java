package com.calendarfx.model;

import com.calendarfx.view.DateControl;
import com.calendarfx.view.Messages;
import com.calendarfx.view.ResourcesView;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

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

    private final InvalidationListener updateCalendarListListener = (Observable it) -> updateCalendarList();

    private final WeakInvalidationListener weakUpdateCalendarListListener = new WeakInvalidationListener(updateCalendarListListener);

    public Resource() {
        getCalendarSources().addListener(weakUpdateCalendarListListener);

        /*
         * Every resource is initially populated with a default source and calendar.
         * We borrow the i18n strings from DateControl.
         */
        Calendar defaultCalendar = new Calendar(Messages.getString("DateControl.DEFAULT_CALENDAR_NAME"));
        defaultCalendar.setUserObject(this);

        CalendarSource defaultCalendarSource = new CalendarSource(Messages.getString("DateControl.DEFAULT_CALENDAR_SOURCE_NAME"));
        defaultCalendarSource.getCalendars().add(defaultCalendar);
        getCalendarSources().add(defaultCalendarSource);
    }

    public Resource(T userObject) {
        this();
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

    private final ObservableList<CalendarSource> calendarSources = FXCollections.observableArrayList();

    /**
     * The list of all calendar sources attached to this resource.
     *
     * @return the calendar sources
     * @see DateControl#getCalendarSources()
     */
    public final ObservableList<CalendarSource> getCalendarSources() {
        return calendarSources;
    }

    private final ReadOnlyListWrapper<Calendar> calendars = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    /**
     * A list that contains all calendars found in all calendar sources
     * currently attached to this resource. This is a convenience list that
     * "flattens" the two level structure of sources and their calendars. It is
     * a read-only list because calendars can not be added directly to a resource.
     * Instead, they are added to calendar sources and those sources are
     * then added to the control.
     *
     * @return the list of all calendars available for this resource
     * @see #getCalendarSources()
     */
    public final ReadOnlyListProperty<Calendar> calendarsProperty() {
        return calendars.getReadOnlyProperty();
    }

    private final ObservableList<Calendar> unmodifiableCalendars = FXCollections.unmodifiableObservableList(calendars.get());

    /**
     * Returns the value of {@link #calendarsProperty()}.
     *
     * @return the list of all calendars available for this control
     */
    public final ObservableList<Calendar> getCalendars() {
        return unmodifiableCalendars;
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

    private void updateCalendarList() {
        List<Calendar> removedCalendars = new ArrayList<>(calendars);
        List<Calendar> newCalendars = new ArrayList<>();
        for (CalendarSource source : getCalendarSources()) {
            for (Calendar calendar : source.getCalendars()) {
                if (calendars.contains(calendar)) {
                    removedCalendars.remove(calendar);
                } else {
                    newCalendars.add(calendar);
                }
            }
            source.getCalendars().removeListener(weakUpdateCalendarListListener);
            source.getCalendars().addListener(weakUpdateCalendarListListener);
        }

        calendars.addAll(newCalendars);
        calendars.removeAll(removedCalendars);
    }

    @Override
    public String toString() {
        if (getUserObject() != null) {
            return getUserObject().toString();
        }

        return super.toString();
    }
}
