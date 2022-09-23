package impl.com.calendarfx.view.resources;

import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.WeekView;
import com.calendarfx.view.resources.Resource;
import com.calendarfx.view.resources.ResourcesView;
import com.calendarfx.view.resources.ResourcesView.Type;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class ResourcesContainer<T extends Resource<?>> extends DayViewBase {

    private final ResourcesView<T> resourcesView;

    public ResourcesContainer(ResourcesView<T> view) {
        this.resourcesView = view;

        getStyleClass().add("resources-view-container");
        setShowToday(false);

        Bindings.bindContentBidirectional(getResources(), view.getResources());

        numberOfDaysProperty().bind(view.numberOfDaysProperty());
        smallSeparatorFactoryProperty().bind(view.smallSeparatorFactoryProperty());
        largeSeparatorFactoryProperty().bind(view.largeSeparatorFactoryProperty());
        numberOfDaysProperty().bind(view.numberOfDaysProperty());
        weekViewFactoryProperty().bind(view.weekViewFactoryProperty());
        dayViewFactoryProperty().bind(view.dayViewFactoryProperty());
        adjustToFirstDayOfWeekProperty().bind(view.adjustToFirstDayOfWeekProperty());
        typeProperty().bind(view.typeProperty());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResourcesContainerSkin<>(this);
    }

    public final ResourcesView<T> getResourcesView() {
        return resourcesView;
    }

    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(this, "type", Type.RESOURCES_OVER_DATE);

    public final Type getType() {
        return type.get();
    }

    /**
     * Determines the visualization type: resoruces over dates or dates over resources.
     *
     * @return the visualization type
     */
    public final ObjectProperty<Type> typeProperty() {
        return type;
    }

    public final void setType(Type type) {
        this.type.set(type);
    }

    private final BooleanProperty adjustToFirstDayOfWeek = new SimpleBooleanProperty(this, "adjustToFirstDayOfWeek", true);

    /**
     * A flag used to indicate that the view should always show the first day of
     * the week (e.g. "Monday") at its beginning even if the
     * {@link #dateProperty()} is set to another day (e.g. "Thursday").
     *
     * @return true if the view always shows the first day of the week
     */
    public final BooleanProperty adjustToFirstDayOfWeekProperty() {
        return adjustToFirstDayOfWeek;
    }

    /**
     * Returns the value of {@link #adjustToFirstDayOfWeekProperty()}.
     *
     * @return true if the view always shows the first day of the week
     */
    public final boolean isAdjustToFirstDayOfWeek() {
        return adjustToFirstDayOfWeekProperty().get();
    }

    /**
     * Sets the value of {@link #adjustToFirstDayOfWeekProperty()}.
     *
     * @param adjust if true the view will always show the first day of the week
     */
    public final void setAdjustToFirstDayOfWeek(boolean adjust) {
        adjustToFirstDayOfWeekProperty().set(adjust);
    }

    private final ObservableList<T> resources = FXCollections.observableArrayList();

    /**
     * The resources to be shown in this view.
     *
     * @return the list of resources
     */
    public final ObservableList<T> getResources() {
        return resources;
    }

    private final IntegerProperty numberOfDays = new SimpleIntegerProperty(this, "numberOfDays", 7);

    /**
     * Stores the number of days that will be shown by this view.
     *
     * @return the number of days shown by the view
     */
    public final IntegerProperty numberOfDaysProperty() {
        return numberOfDays;
    }

    /**
     * Returns the value of {@link #numberOfDaysProperty()}.
     *
     * @return the number of days shown by the view
     */
    public final int getNumberOfDays() {
        return numberOfDaysProperty().get();
    }

    /**
     * Sets the value of {@link #numberOfDaysProperty()}.
     *
     * @param number the new number of days shown by the view
     */
    public final void setNumberOfDays(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("invalid number of days, must be larger than 0 but was " + number);
        }

        numberOfDaysProperty().set(number);
    }

    private final ObjectProperty<Callback<T, WeekView>> weekViewFactory = new SimpleObjectProperty<>(this, "weekViewFactory");

    public final Callback<T, WeekView> getWeekViewFactory() {
        return weekViewFactory.get();
    }

    /**
     * A factory used for creating a new {@link WeekView} instance for each resource
     * shown in the view.
     *
     * @return a factory for resource week views
     */
    public final ObjectProperty<Callback<T, WeekView>> weekViewFactoryProperty() {
        return weekViewFactory;
    }

    public void setWeekViewFactory(Callback<T, WeekView> weekViewFactory) {
        this.weekViewFactory.set(weekViewFactory);
    }

    private final ObjectProperty<Callback<T, DayView>> dayViewFactory = new SimpleObjectProperty<>(this, "dayViewFactory");

    public final Callback<T, DayView> getDayViewFactory() {
        return dayViewFactory.get();
    }

    /**
     * A factory used for creating a new {@link DayView} instance for a resource day
     * shown in the view.
     *
     * @return a factory for resource day views
     */
    public final ObjectProperty<Callback<T, DayView>> dayViewFactoryProperty() {
        return dayViewFactory;
    }

    public void setDayViewFactory(Callback<T, DayView> dayViewFactory) {
        this.dayViewFactory.set(dayViewFactory);
    }

    private final ObjectProperty<Callback<ResourcesView<T>, Region>> smallSeparatorFactory = new SimpleObjectProperty<>(this, "smallSeparatorFactory", it-> {
        Region region = new Region();
        region.getStyleClass().add("small-separator");
        return region;
    });

    public final Callback<ResourcesView<T>, Region> getSmallSeparatorFactory() {
        return smallSeparatorFactory.get();
    }

    public final ObjectProperty<Callback<ResourcesView<T>, Region>> smallSeparatorFactoryProperty() {
        return smallSeparatorFactory;
    }

    public final void setSmallSeparatorFactory(Callback<ResourcesView<T>, Region> smallSeparatorFactory) {
        this.smallSeparatorFactory.set(smallSeparatorFactory);
    }

    private final ObjectProperty<Callback<ResourcesView<T>, Region>> largeSeparatorFactory = new SimpleObjectProperty<>(this, "largeSeparatorFactory");

    public final Callback<ResourcesView<T>, Region> getLargeSeparatorFactory() {
        return largeSeparatorFactory.get();
    }

    /**
     * A factory used for creating the vertical separators between the resources.
     *
     * @return the resource separator factory
     */
    public final ObjectProperty<Callback<ResourcesView<T>, Region>> largeSeparatorFactoryProperty() {
        return largeSeparatorFactory;
    }

    public final void setLargeSeparatorFactory(Callback<ResourcesView<T>, Region> largeSeparatorFactory) {
        this.largeSeparatorFactory.set(largeSeparatorFactory);
    }
}

