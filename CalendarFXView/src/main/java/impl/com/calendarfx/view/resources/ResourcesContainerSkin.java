package impl.com.calendarfx.view.resources;

import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.WeekView;
import com.calendarfx.view.resources.Resource;
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
        updateView();

        getChildren().add(container);
    }

    private void updateView() {
        container.getChildren().clear();

        ResourcesContainer<T> resourcesViewContainer = getSkinnable();
        ObservableList<T> resources = resourcesViewContainer.getResources();
        for (int i = 0; i < resources.size(); i++) {
            T resource = resources.get(i);

            WeekView weekView = resourcesViewContainer.getWeekViewFactory().call(resource);

            weekView.setPrefWidth(0); // so they all end up with the same percentage width

            // bind day view to container but remove bindings that interfere
            resourcesViewContainer.bind(weekView, true);
            Bindings.unbindBidirectional(resourcesViewContainer.defaultCalendarProviderProperty(), weekView.defaultCalendarProviderProperty());
            Bindings.unbindBidirectional(resourcesViewContainer.draggedEntryProperty(), weekView.draggedEntryProperty());
            Bindings.unbindBidirectional(resourcesViewContainer.enableCurrentTimeMarkerProperty(), weekView.enableCurrentTimeMarkerProperty());
            Bindings.unbindBidirectional(resourcesViewContainer.enableCurrentTimeCircleProperty(), weekView.enableCurrentTimeCircleProperty());
            Bindings.unbindBidirectional(resourcesViewContainer.availabilityCalendarProperty(), weekView.availabilityCalendarProperty());
            Bindings.unbindBidirectional(resourcesViewContainer.lassoStartProperty(), weekView.lassoStartProperty());
            Bindings.unbindBidirectional(resourcesViewContainer.lassoEndProperty(), weekView.lassoEndProperty());
            Bindings.unbindBidirectional(resourcesViewContainer.onLassoFinishedProperty(), weekView.onLassoFinishedProperty());
            Bindings.unbindContentBidirectional(resourcesViewContainer.getCalendarSources(), weekView.getCalendarSources());

            weekView.setEnableCurrentTimeCircle(i == 0);
            weekView.setEnableCurrentTimeMarker(true);

            weekView.setAvailabilityCalendar(resource.getAvailabilityCalendar());
            weekView.installDefaultLassoFinishedBehaviour();
            weekView.numberOfDaysProperty().bind(resourcesViewContainer.numberOfDaysProperty());

            resourcesViewContainer.numberOfDaysProperty().addListener(it -> System.out.println("number of days (multi resources): " + weekView.getNumberOfDays()));
            weekView.numberOfDaysProperty().addListener(it -> System.out.println("number of days: " + weekView.getNumberOfDays()));

            CalendarSource calendarSource = createCalendarSource(resource);
            weekView.getCalendarSources().setAll(calendarSource);
            weekView.setDefaultCalendarProvider(control -> calendarSource.getCalendars().get(0));

            container.getChildren().add(weekView);

            if (i < resources.size() - 1) {
                Callback<T, Region> separatorFactory = resourcesViewContainer.getSeparatorFactory();
                if (separatorFactory != null) {
                    Region separator = separatorFactory.call(resource);
                    if (separator != null) {
                        container.getChildren().add(separator);
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