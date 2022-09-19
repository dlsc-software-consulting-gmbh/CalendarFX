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
                Callback<T, Region> separatorFactory = container.getSeparatorFactory();
                if (separatorFactory != null) {
                    Region separator = separatorFactory.call(resource);
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