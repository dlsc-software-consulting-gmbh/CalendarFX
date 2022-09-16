package impl.com.calendarfx.view.resources;

import com.calendarfx.view.DayViewBase;
import com.calendarfx.view.WeekView;
import com.calendarfx.view.resources.Resource;
import com.calendarfx.view.resources.ResourcesView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class ResourcesContainer<T extends Resource<?>> extends DayViewBase {

    public ResourcesContainer(ResourcesView<T> view) {
        getStyleClass().add("resources-view-container");
        setShowToday(false);

        numberOfDaysProperty().bind(view.numberOfDaysProperty());
        resourcesProperty().bind(view.resourcesProperty());
        separatorFactoryProperty().bind(view.separatorFactoryProperty());
        resourcesProperty().bind(view.resourcesProperty());
        numberOfDaysProperty().bind(view.numberOfDaysProperty());
        weekViewFactoryProperty().bind(view.weekViewFactoryProperty());
        adjustToFirstDayOfWeekProperty().bind(view.adjustToFirstDayOfWeekProperty());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResourcesContainerSkin<>(this);
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

    private final ListProperty<T> resources = new SimpleListProperty<>(this, "resources", FXCollections.observableArrayList());

    public final ObservableList<T> getResources() {
        return resources.get();
    }

    public final ListProperty<T> resourcesProperty() {
        return resources;
    }

    public final void setResources(ObservableList<T> resources) {
        this.resources.set(resources);
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

    private final ObjectProperty<Callback<T, Region>> separatorFactory = new SimpleObjectProperty<>(this, "separatorFactory");

    public final Callback<T, Region> getSeparatorFactory() {
        return separatorFactory.get();
    }

    /**
     * A factory used for creating the vertical separators between the resources.
     *
     * @return the resource separator factory
     */
    public final ObjectProperty<Callback<T, Region>> separatorFactoryProperty() {
        return separatorFactory;
    }

    public final void setSeparatorFactory(Callback<T, Region> separatorFactory) {
        this.separatorFactory.set(separatorFactory);
    }
}

