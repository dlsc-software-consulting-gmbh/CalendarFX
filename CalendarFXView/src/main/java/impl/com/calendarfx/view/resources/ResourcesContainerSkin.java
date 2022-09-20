package impl.com.calendarfx.view.resources;

import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.DayView;
import com.calendarfx.view.WeekView;
import com.calendarfx.view.resources.Resource;
import com.calendarfx.view.resources.ResourcesView;
import com.calendarfx.view.resources.ResourcesView.Type;
import impl.com.calendarfx.view.DayViewBaseSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class ResourcesContainerSkin<T extends Resource<?>> extends DayViewBaseSkin<ResourcesContainer<T>> {

    private final HBox container = new HBox();

    public ResourcesContainerSkin(ResourcesContainer<T> view) {
        super(view);

        container.getStyleClass().add("container");
        container.setFillHeight(true);

        InvalidationListener updateViewListener = (Observable it) -> updateView();
        view.getResources().addListener(updateViewListener);
        view.numberOfDaysProperty().addListener(updateViewListener);
        view.typeProperty().addListener(updateViewListener);

        updateView();

        getChildren().add(container);
    }

    private void updateView() {
        container.getChildren().clear();
        ResourcesContainer<T> skinnable = getSkinnable();
        if (skinnable.getType().equals(Type.RESOURCES_OVER_DATE)) {
            updateViewResourcesOverDates();
        } else {
            updateViewDatesOverResources();
        }
    }

    private void updateViewDatesOverResources() {
        ResourcesContainer<T> container = getSkinnable();
        ObservableList<T> resources = container.getResources();
        int numberOfDays = container.getNumberOfDays();

        for (int dayIndex = 0; dayIndex < numberOfDays; dayIndex++) {

            HBox resourcesBox = new HBox();

            for (int resourceIndex = 0; resourceIndex < resources.size(); resourceIndex++) {
                T resource = resources.get(resourceIndex);

                DayView dayView = container.getDayViewFactory().call(resource);

                dayView.getStyleClass().removeAll("only", "first", "middle", "last");

                if (resources.size() == 1) {
                    dayView.getStyleClass().add("only");
                } else {
                    if (resourceIndex == 0) {
                        dayView.getStyleClass().add("first");
                    } else if (resourceIndex == resources.size() - 1) {
                        dayView.getStyleClass().add("last");
                    } else {
                        dayView.getStyleClass().add("middle");
                    }
                }

                // bind day view to container but remove bindings that interfere
                container.bind(dayView, true);

                // unbind what is not needed
                Bindings.unbindBidirectional(container.defaultCalendarProviderProperty(), dayView.defaultCalendarProviderProperty());
                Bindings.unbindBidirectional(container.draggedEntryProperty(), dayView.draggedEntryProperty());
                Bindings.unbindBidirectional(container.enableCurrentTimeMarkerProperty(), dayView.enableCurrentTimeMarkerProperty());
                Bindings.unbindBidirectional(container.enableCurrentTimeCircleProperty(), dayView.enableCurrentTimeCircleProperty());
                Bindings.unbindBidirectional(container.availabilityCalendarProperty(), dayView.availabilityCalendarProperty());
                Bindings.unbindBidirectional(container.lassoStartProperty(), dayView.lassoStartProperty());
                Bindings.unbindBidirectional(container.lassoEndProperty(), dayView.lassoEndProperty());
                Bindings.unbindBidirectional(container.onLassoFinishedProperty(), dayView.onLassoFinishedProperty());
                Bindings.unbindContentBidirectional(container.getCalendarSources(), dayView.getCalendarSources());

                dayView.setEnableCurrentTimeMarker(true);
                dayView.setEnableCurrentTimeCircle(dayIndex == 0 && resourceIndex == 0);
                dayView.setAvailabilityCalendar(resource.getAvailabilityCalendar());
                dayView.installDefaultLassoFinishedBehaviour();

                CalendarSource calendarSource = createCalendarSource(resource);
                dayView.getCalendarSources().setAll(calendarSource);
                dayView.setDefaultCalendarProvider(control -> calendarSource.getCalendars().get(0));

                dayView.setPrefWidth(0); // so they all end up with the same percentage width
                HBox.setHgrow(dayView, Priority.ALWAYS);
                resourcesBox.getChildren().add(dayView);

                if (resourceIndex < resources.size() - 1) {
                    Region separator = new Region();
                    separator.getStyleClass().add("weekday-separator"); // not really separating weekdays, but we want it to look the same
                    resourcesBox.getChildren().add(separator);
                    HBox.setHgrow(separator, Priority.NEVER);
                }
            }

            this.container.getChildren().add(resourcesBox);

            if (dayIndex < numberOfDays - 1) {
                Callback<ResourcesView<T>, Region> separatorFactory = container.getSeparatorFactory();
                if (separatorFactory != null) {
                    Region separator = separatorFactory.call(container.getResourcesView());
                    if (separator != null) {
                        this.container.getChildren().add(separator);
                        HBox.setHgrow(separator, Priority.NEVER);
                    }
                }
            }
            HBox.setHgrow(resourcesBox, Priority.ALWAYS);
        }
    }

    private void updateViewResourcesOverDates() {
        ResourcesContainer<T> container = getSkinnable();
        ObservableList<T> resources = container.getResources();
        for (int i = 0; i < resources.size(); i++) {
            T resource = resources.get(i);

            WeekView weekView = container.getWeekViewFactory().call(resource);

            weekView.getStyleClass().removeAll("only", "first", "middle", "last");

            if (resources.size() == 1) {
                weekView.getStyleClass().add("only");
            } else {
                if (i == 0) {
                    weekView.getStyleClass().add("first");
                } else if (i == resources.size() - 1) {
                    weekView.getStyleClass().add("last");
                } else {
                    weekView.getStyleClass().add("middle");
                }
            }

            weekView.setPrefWidth(0); // so they all end up with the same percentage width

            // bind day view to container but remove bindings that interfere
            container.bind(weekView, true);

            // rebind "adjust"
            weekView.adjustToFirstDayOfWeekProperty().bind(container.adjustToFirstDayOfWeekProperty());

            // unbind what is not needed
            Bindings.unbindBidirectional(container.defaultCalendarProviderProperty(), weekView.defaultCalendarProviderProperty());
            Bindings.unbindBidirectional(container.draggedEntryProperty(), weekView.draggedEntryProperty());
            Bindings.unbindBidirectional(container.enableCurrentTimeMarkerProperty(), weekView.enableCurrentTimeMarkerProperty());
            Bindings.unbindBidirectional(container.enableCurrentTimeCircleProperty(), weekView.enableCurrentTimeCircleProperty());
            Bindings.unbindBidirectional(container.availabilityCalendarProperty(), weekView.availabilityCalendarProperty());
            Bindings.unbindBidirectional(container.lassoStartProperty(), weekView.lassoStartProperty());
            Bindings.unbindBidirectional(container.lassoEndProperty(), weekView.lassoEndProperty());
            Bindings.unbindBidirectional(container.onLassoFinishedProperty(), weekView.onLassoFinishedProperty());
            Bindings.unbindContentBidirectional(container.getCalendarSources(), weekView.getCalendarSources());

            weekView.setEnableCurrentTimeCircle(i == 0);
            weekView.setEnableCurrentTimeMarker(true);

            weekView.setAvailabilityCalendar(resource.getAvailabilityCalendar());
            weekView.installDefaultLassoFinishedBehaviour();
            weekView.numberOfDaysProperty().bind(container.numberOfDaysProperty());

            container.numberOfDaysProperty().addListener(it -> System.out.println("number of days (multi resources): " + weekView.getNumberOfDays()));
            weekView.numberOfDaysProperty().addListener(it -> System.out.println("number of days: " + weekView.getNumberOfDays()));

            CalendarSource calendarSource = createCalendarSource(resource);
            weekView.getCalendarSources().setAll(calendarSource);
            weekView.setDefaultCalendarProvider(control -> calendarSource.getCalendars().get(0));

            this.container.getChildren().add(weekView);

            if (i < resources.size() - 1) {
                Callback<ResourcesView<T>, Region> separatorFactory = container.getSeparatorFactory();
                if (separatorFactory != null) {
                    Region separator = separatorFactory.call(container.getResourcesView());
                    if (separator != null) {
                        this.container.getChildren().add(separator);
                        HBox.setHgrow(separator, Priority.NEVER);
                    }
                }
            }
            HBox.setHgrow(weekView, Priority.ALWAYS);
        }
    }

    private CalendarSource createCalendarSource(T resource) {
        CalendarSource source = new CalendarSource(resource.getUserObject().toString());
        source.setName(resource.getUserObject().toString());
        source.getCalendars().setAll(resource.getCalendar());
        return source;
    }
}