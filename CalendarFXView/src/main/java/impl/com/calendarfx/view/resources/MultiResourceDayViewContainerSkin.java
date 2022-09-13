package impl.com.calendarfx.view.resources;

import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.DayView;
import com.calendarfx.view.resources.MultiResourceDayViewContainer;
import com.calendarfx.view.resources.Resource;
import impl.com.calendarfx.view.DayViewBaseSkin;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class MultiResourceDayViewContainerSkin<T extends Resource<?>> extends DayViewBaseSkin<MultiResourceDayViewContainer<T>> {

    private final HBox container = new HBox();

    public MultiResourceDayViewContainerSkin(MultiResourceDayViewContainer<T> view) {
        super(view);
        container.getStyleClass().add("inner-container");
        view.getResources().addListener((Observable it) -> updateView());
        updateView();
        getChildren().add(container);
    }

    private void updateView() {
        container.getChildren().clear();

        MultiResourceDayViewContainer<T> multiResourceDayViewContainer = getSkinnable();
        ObservableList<T> resources = multiResourceDayViewContainer.getResources();
        for (int i = 0; i < resources.size(); i++) {
            DayView dayView = new DayView();

            // bind day view to container but remove bindings that interfere
            multiResourceDayViewContainer.bind(dayView, true);
            Bindings.unbindBidirectional(multiResourceDayViewContainer.defaultCalendarProviderProperty(), dayView.defaultCalendarProviderProperty());
            Bindings.unbindBidirectional(multiResourceDayViewContainer.draggedEntryProperty(), dayView.draggedEntryProperty());
            Bindings.unbindBidirectional(multiResourceDayViewContainer.enableCurrentTimeMarkerProperty(), dayView.enableCurrentTimeMarkerProperty());
            Bindings.unbindBidirectional(multiResourceDayViewContainer.enableCurrentTimeCircleProperty(), dayView.enableCurrentTimeCircleProperty());
            Bindings.unbindBidirectional(multiResourceDayViewContainer.availabilityCalendarProperty(), dayView.availabilityCalendarProperty());
            Bindings.unbindBidirectional(multiResourceDayViewContainer.lassoStartProperty(), dayView.lassoStartProperty());
            Bindings.unbindBidirectional(multiResourceDayViewContainer.lassoEndProperty(), dayView.lassoEndProperty());
            Bindings.unbindBidirectional(multiResourceDayViewContainer.onLassoFinishedProperty(), dayView.onLassoFinishedProperty());
            Bindings.unbindContentBidirectional(multiResourceDayViewContainer.getCalendarSources(), dayView.getCalendarSources());

            dayView.setEnableCurrentTimeCircle(i == 0);
            dayView.setEnableCurrentTimeMarker(true);

            T resource = resources.get(i);

            dayView.setAvailabilityCalendar(resource.getAvailabilityCalendar());
            dayView.installDefaultLassoFinishedBehaviour();

            CalendarSource calendarSource = createCalendarSource(resource);
            dayView.getCalendarSources().setAll(calendarSource);
            dayView.setDefaultCalendarProvider(control -> calendarSource.getCalendars().get(0));

            container.getChildren().add(dayView);
            HBox.setHgrow(dayView, Priority.ALWAYS);
        }
    }

    private CalendarSource createCalendarSource(T resource) {
        CalendarSource source = new CalendarSource(resource.getUserObject().toString());
        source.setName(resource.getUserObject().toString());
        source.getCalendars().setAll(resource.getCalendar());
        return source;
    }
}