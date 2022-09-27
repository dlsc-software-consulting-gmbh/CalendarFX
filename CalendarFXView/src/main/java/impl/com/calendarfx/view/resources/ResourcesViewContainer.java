package impl.com.calendarfx.view.resources;

import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.resources.Resource;
import com.calendarfx.view.resources.ResourcesView;
import javafx.scene.control.Skin;

public class ResourcesViewContainer<T extends Resource<?>> extends DayViewBase {

    private final ResourcesView<T> resourcesView;

    public ResourcesViewContainer(ResourcesView<T> view) {
        this.resourcesView = view;
        getStyleClass().add("resources-view-container");
        setShowToday(false);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResourcesViewContainerSkin<>(this);
    }

    public final ResourcesView<T> getResourcesView() {
        return resourcesView;
    }
}

