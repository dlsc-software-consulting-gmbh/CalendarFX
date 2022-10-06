package impl.com.calendarfx.view;

import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.DayView;
import com.calendarfx.view.WeekView;
import com.calendarfx.model.Resource;
import com.calendarfx.view.ResourcesView;
import com.calendarfx.view.ResourcesView.Type;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class ResourcesViewContainerSkin<T extends Resource<?>> extends DayViewBaseSkin<ResourcesViewContainer<T>> {

    private final HBox box = new HBox();

    private final ResourcesView<T> resourcesView;

    public ResourcesViewContainerSkin(ResourcesViewContainer<T> view) {
        super(view);

        box.getStyleClass().add("container");
        box.setFillHeight(true);

        resourcesView = view.getResourcesView();

        InvalidationListener updateViewListener = (Observable it) -> updateView();
        resourcesView.getResources().addListener(updateViewListener);
        resourcesView.numberOfDaysProperty().addListener(updateViewListener);
        resourcesView.typeProperty().addListener(updateViewListener);

        updateView();

        getChildren().add(box);
    }

    private void updateView() {
        box.getChildren().clear();

        ResourcesViewContainer<T> resourcesContainer = getSkinnable();
        resourcesContainer.unbindAll();

        if (resourcesView.getType().equals(Type.RESOURCES_OVER_DATES)) {
            updateViewResourcesOverDates();
        } else {
            updateViewDatesOverResources();
        }
    }

    private void updateViewDatesOverResources() {
        ResourcesViewContainer<T> container = getSkinnable();
        ObservableList<T> resources = resourcesView.getResources();
        int numberOfDays = resourcesView.getNumberOfDays();

        for (int dayIndex = 0; dayIndex < numberOfDays; dayIndex++) {

            HBox resourcesBox = new HBox();

            for (int resourceIndex = 0; resourceIndex < resources.size(); resourceIndex++) {
                T resource = resources.get(resourceIndex);

                DayView dayView = resourcesView.getDayViewFactory().call(resource);

                final int additionalDays = dayIndex;

                dayView.dateProperty().bind(Bindings.createObjectBinding(() -> container.getDate().plusDays(additionalDays), container.dateProperty()));

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
                container.bind(dayView, false);

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

            this.box.getChildren().add(resourcesBox);

            if (dayIndex < numberOfDays - 1) {
                Callback<ResourcesView<T>, Region> separatorFactory = resourcesView.getLargeSeparatorFactory();
                if (separatorFactory != null) {
                    Region separator = separatorFactory.call(container.getResourcesView());
                    if (separator != null) {
                        this.box.getChildren().add(separator);
                        HBox.setHgrow(separator, Priority.NEVER);
                    }
                }
            }
            HBox.setHgrow(resourcesBox, Priority.ALWAYS);
        }
    }

    private void updateViewResourcesOverDates() {
        ResourcesViewContainer<T> container = getSkinnable();
        ObservableList<T> resources = resourcesView.getResources();
        for (int i = 0; i < resources.size(); i++) {
            T resource = resources.get(i);

            WeekView weekView = resourcesView.getWeekViewFactory().call(resource);

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

            // bind additionally "adjust"
            weekView.adjustToFirstDayOfWeekProperty().bind(resourcesView.adjustToFirstDayOfWeekProperty());

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

            weekView.numberOfDaysProperty().bindBidirectional(resourcesView.numberOfDaysProperty());

            CalendarSource calendarSource = createCalendarSource(resource);
            weekView.getCalendarSources().setAll(calendarSource);
            weekView.setDefaultCalendarProvider(control -> calendarSource.getCalendars().get(0));

            this.box.getChildren().add(weekView);

            if (i < resources.size() - 1) {
                Callback<ResourcesView<T>, Region> separatorFactory = resourcesView.getLargeSeparatorFactory();
                if (separatorFactory != null) {
                    Region separator = separatorFactory.call(container.getResourcesView());
                    if (separator != null) {
                        this.box.getChildren().add(separator);
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