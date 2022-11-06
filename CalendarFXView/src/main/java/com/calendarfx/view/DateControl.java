/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.util.ViewHelper;
import com.calendarfx.util.WeakList;
import com.calendarfx.view.page.DayPage;
import com.calendarfx.view.popover.DatePopOver;
import com.calendarfx.view.popover.EntryPopOverContentPane;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.control.PropertySheet.Item;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Objects.requireNonNull;
import static javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED;

/**
 * The superclass for all controls that are showing calendar information. This
 * class is responsible for:
 *
 * <ul>
 * <li>Binding to other d
 * ate controls</li>
 * <li>Providing the current date, "today", first day of week</li>
 * <li>Creating sources, calendars, entries</li>
 * <li>Context menu</li>
 * <li>Showing details for a given date or entry</li>
 * <li>Providing a virtual grid for editing</li>
 * <li>Selection handling</li>
 * <li>Printing</li>
 * </ul>
 * <h2>Binding</h2> Date controls are bound to each other to create complex date
 * controls like the {@link CalendarView}. When date controls are bound to each
 * other via the {@link #bind(DateControl, boolean)} method then most of their
 * properties will be bound to each other. This not only includes date and time
 * zone properties but also all the factory and detail callbacks. This allows an
 * application to create a complex calendar control and to configure only that
 * control without worrying about the date controls that are nested inside
 * it. The children will all "inherit" their settings from the parent control.
 *
 * <h2>Current Date, Today, First Day of Week</h2> The {@link #dateProperty()}
 * is used to store the date that the control has to display. For the
 * {@link DayView} this would mean that it has to show exactly that date. The
 * {@link DetailedWeekView} would only have to guarantee that it shows the week that
 * contains this date. For this the {@link DetailedWeekView} uses the
 * {@link #getFirstDayOfWeek()} method that looks up its value from the week
 * fields stored in the {@link #weekFieldsProperty()}. The
 * {@link #todayProperty()} is mainly used for highlighting today's date in the
 * view (e.g. a red background).
 *
 * <h2>Creating Sources, Calendars, Entries</h2> The date control uses various
 * factories to create new sources, calendars, and entries. Each factory has to
 * implement the {@link Callback} interface. The factories will be invoked when
 * the application calls {@link #createCalendarSource()} or
 * {@link #createEntryAt(ZonedDateTime)}.
 *
 * <h2>Context Menu</h2> Date controls can either set a context menu explicitly
 * via {@link #setContextMenu(ContextMenu)} or by providing a callback that gets
 * invoked every time the context menu event is received (see
 * {@link #setContextMenuCallback(Callback)}). If a context menu has been set
 * explicitly then the callback will never be called again.
 *
 * <h2>Details for Entries and Dates</h2> When clicking on an entry or a date
 * the user wants to see details regarding the entry or the date. Callbacks for
 * this can be registered via {@link #setEntryDetailsCallback(Callback)} and
 * {@link #setDateDetailsCallback(Callback)}. The callbacks can decide which
 * kind of user interface they want to show to the user. The default
 * implementation for both callbacks is a {@link PopOver} control from the
 * <a href="http://controlsfx.org">ControlsFX</a> project.
 *
 * <h2>Selection Handling</h2> Date controls use a very simple selection
 * concept. All selected entries are stored inside an observable list (see
 * {@link #getSelections()}). The controls support single and multiple
 * selections (see {@link #setSelectionMode(SelectionMode)}). Due to the binding
 * approach it does not matter in which child date control an entry gets
 * selected. All controls will always know which entries are selected.
 *
 * <h2>Virtual Grid</h2> A virtual grid is used for editing. It allows the start
 * and end times of entries to snap to "virtual" grid lines. The grid can be
 * used to make the times always snap to 5, 10, 15, 30 minutes for example. This
 * makes it easier to align entries to each other and covers the most common use
 * cases. More precise times can always be set in the details.
 */
public abstract class DateControl extends CalendarFXControl {

    private int entryCounter = 1;

    private Boolean usesOwnContextMenu;

    private final InvalidationListener updateCalendarListListener = (Observable it) -> updateCalendarList();

    private final WeakInvalidationListener weakUpdateCalendarListListener = new WeakInvalidationListener(updateCalendarListListener);

    /**
     * Constructs a new date control and initializes all factories and callbacks
     * with default implementations.
     */
    protected DateControl() {
        setOnMouseClicked(evt -> requestFocus());

        setUsagePolicy(count -> {
            if (count < 0) {
                throw new IllegalArgumentException("usage count can not be smaller than zero, but was " + count);
            }

            switch (count) {
                case 0:
                    return Usage.NONE;
                case 1:
                    return Usage.VERY_LOW;
                case 2:
                    return Usage.LOW;
                case 3:
                    return Usage.MEDIUM;
                case 4:
                    return Usage.HIGH;
                case 5:
                default:
                    return Usage.VERY_HIGH;
            }
        });

        getWeekendDays().add(SATURDAY);
        getWeekendDays().add(SUNDAY);

        /*
         * Every date control is initially populated with a default source and
         * calendar.
         */
        CalendarSource defaultCalendarSource = new CalendarSource(Messages.getString("DateControl.DEFAULT_CALENDAR_SOURCE_NAME"));
        Calendar defaultCalendar = new Calendar(Messages.getString("DateControl.DEFAULT_CALENDAR_NAME"));
        defaultCalendarSource.getCalendars().add(defaultCalendar);
        getCalendarSources().add(defaultCalendarSource);

        getCalendarSources().addListener(weakUpdateCalendarListListener);

        /*
         * The popover content callback creates a content node that will make
         * out the content of the popover used to display entry details.
         */
        setEntryDetailsPopOverContentCallback(param -> new EntryPopOverContentPane(param.getPopOver(), param.getDateControl(), param.getEntry()));

        /*
         * The default calendar provider returns the first calendar which is visible and not read-only.
         */
        setDefaultCalendarProvider(control -> {
            List<CalendarSource> sources = getCalendarSources();
            for (CalendarSource s : sources) {
                List<? extends Calendar> calendars = s.getCalendars();
                if (calendars != null && !calendars.isEmpty()) {
                    for (Calendar c : calendars) {
                        if (!c.isReadOnly() && isCalendarVisible(c)) {
                            return c;
                        }
                    }
                }
            }

            return null;
        });

        setEntryFactory(param -> {
            DateControl control = param.getDateControl();

            VirtualGrid grid = control.getVirtualGrid();
            ZonedDateTime time = param.getZonedDateTime();
            DayOfWeek firstDayOfWeek = getFirstDayOfWeek();
            ZonedDateTime lowerTime = grid.adjustTime(time, false, firstDayOfWeek);
            ZonedDateTime upperTime = grid.adjustTime(time, true, firstDayOfWeek);

            if (Duration.between(time, lowerTime).abs().minus(Duration.between(time, upperTime).abs()).isNegative()) {
                time = lowerTime;
            } else {
                time = upperTime;
            }

            Entry<Object> entry = new Entry<>(MessageFormat.format(Messages.getString("DateControl.DEFAULT_ENTRY_TITLE"), entryCounter++));
            Interval interval = new Interval(time.toLocalDateTime(), time.toLocalDateTime().plusHours(1), time.getZone());
            entry.setInterval(interval);

            if (control instanceof AllDayView) {
                entry.setFullDay(true);
            }

            return entry;
        });

        setEntryDetailsCallback(param -> {
            InputEvent evt = param.getInputEvent();
            if (evt instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent) evt;
                if (mouseEvent.getClickCount() == 2) {
                    showEntryDetails(param.getEntry(), param.getNode(), param.getOwner(), param.getScreenY());
                    return true;
                }
            } else {
                showEntryDetails(param.getEntry(), param.getNode(), param.getOwner(), param.getScreenY());
                return true;
            }

            return false;
        });

        setDateDetailsCallback(param -> {
            InputEvent evt = param.getInputEvent();
            if (evt == null) {
                showDateDetails(param.getOwner(), param.getLocalDate());
                return true;
            } else if (evt instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent) evt;
                if (mouseEvent.getClickCount() == 1) {
                    showDateDetails(param.getOwner(), param.getLocalDate());
                    return true;
                }
            }

            return false;
        });

        setContextMenuCallback(new ContextMenuProvider());

        setEntryContextMenuCallback(param -> {
            EntryViewBase<?> entryView = param.getEntryView();
            Entry<?> entry = entryView.getEntry();

            ContextMenu contextMenu = new ContextMenu();

            /*
             * Show dialog / popover with entry details.
             */
            MenuItem informationItem = new MenuItem(Messages.getString("DateControl.MENU_ITEM_INFORMATION"));
            informationItem.setOnAction(evt -> {
                Callback<EntryDetailsParameter, Boolean> detailsCallback = getEntryDetailsCallback();
                if (detailsCallback != null) {
                    ContextMenuEvent ctxEvent = param.getContextMenuEvent();
                    EntryDetailsParameter entryDetailsParam = new EntryDetailsParameter(ctxEvent, DateControl.this, entryView.getEntry(), entryView, entryView, ctxEvent.getScreenX(), ctxEvent.getScreenY());
                    detailsCallback.call(entryDetailsParam);
                }
            });
            contextMenu.getItems().add(informationItem);

            String stylesheet = CalendarView.class.getResource("calendar.css").toExternalForm();

            /*
             * Assign entry to different calendars.
             */
            Menu calendarMenu = new Menu(Messages.getString("DateControl.MENU_CALENDAR"));
            for (Calendar calendar : getCalendars()) {
                RadioMenuItem calendarItem = new RadioMenuItem(calendar.getName());
                calendarItem.setOnAction(evt -> entry.setCalendar(calendar));
                calendarItem.setDisable(calendar.isReadOnly());
                calendarItem.setSelected(calendar.equals(param.getCalendar()));
                calendarMenu.getItems().add(calendarItem);

                StackPane graphic = new StackPane();
                graphic.getStylesheets().add(stylesheet);

                /*
                 * Icon has to be wrapped in a stackpane so that a stylesheet
                 * can be added to it.
                 */
                Rectangle icon = new Rectangle(10, 10);
                icon.setArcHeight(2);
                icon.setArcWidth(2);
                icon.getStyleClass().setAll(calendar.getStyle() + "-icon");
                graphic.getChildren().add(icon);

                calendarItem.setGraphic(graphic);
            }

            calendarMenu.setDisable(param.getCalendar().isReadOnly());
            contextMenu.getItems().add(calendarMenu);

            if (getEntryEditPolicy().call(new EntryEditParameter(this, entry, EditOperation.DELETE))) {
                /*
                 * Delete calendar entry.
                 */
                MenuItem delete = new MenuItem(Messages.getString("DateControl.MENU_ITEM_DELETE"));
                contextMenu.getItems().add(delete);
                delete.setDisable(param.getCalendar().isReadOnly());
                delete.setOnAction(evt -> {
                    Calendar calendar = entry.getCalendar();
                    if (!calendar.isReadOnly()) {
                        if (entry.isRecurrence()) {
                            Entry<?> recurrenceSourceEntry = entry.getRecurrenceSourceEntry();
                            if (recurrenceSourceEntry != null) {
                                recurrenceSourceEntry.removeFromCalendar();
                            }
                        } else {
                            entry.removeFromCalendar();
                        }
                    }
                });
            }

            return contextMenu;
        });

        setCalendarSourceFactory(param -> {
            CalendarSource source = new CalendarSource(Messages.getString("DateControl.DEFAULT_NEW_CALENDAR_SOURCE"));
            Calendar calendar = new Calendar(Messages.getString("DateControl.DEFAULT_NEW_CALENDAR"));
            calendar.setShortName(Messages.getString("DateControl.DEFAULT_NEW_CALENDAR").substring(0, 1));
            source.getCalendars().add(calendar);
            return source;
        });

        addEventHandler(CONTEXT_MENU_REQUESTED, evt -> {

            /*
             * If a context menu was specified by calling setContextMenu() then
             * we will not use the callback to produce one.
             */
            if (null == usesOwnContextMenu) {
                usesOwnContextMenu = getContextMenu() != null;
            }

            if (!usesOwnContextMenu) {
                Callback<ContextMenuParameter, ContextMenu> callback = getContextMenuCallback();
                if (callback != null) {
                    Callback<DateControl, Calendar> calendarProvider = getDefaultCalendarProvider();
                    Calendar calendar = calendarProvider.call(DateControl.this);
                    ZonedDateTime time = ZonedDateTime.now();
                    if (DateControl.this instanceof ZonedDateTimeProvider) {
                        ZonedDateTimeProvider provider = (ZonedDateTimeProvider) DateControl.this;
                        time = provider.getZonedDateTimeAt(evt.getX(), evt.getY(), getZoneId());
                    }
                    ContextMenuParameter param = new ContextMenuParameter(evt, DateControl.this, calendar, time);
                    ContextMenu menu = callback.call(param);
                    if (menu != null) {
                        menu.show(getScene().getWindow(), evt.getScreenX(), evt.getScreenY());
                    }

                    evt.consume();
                }
            }
        });

        getAvailableZoneIds().add(ZoneId.of("Europe/Zurich"));
        getAvailableZoneIds().add(ZoneId.of("Europe/Helsinki"));
        getAvailableZoneIds().add(ZoneId.of("Europe/London"));
        getAvailableZoneIds().add(ZoneId.of("US/Eastern"));
        getAvailableZoneIds().add(ZoneId.of("US/Central"));
        getAvailableZoneIds().add(ZoneId.of("US/Pacific"));

        createEntryClickCountProperty().addListener(it -> {
            int createEntryClickCount = getCreateEntryClickCount();
            if (createEntryClickCount <= 0 || createEntryClickCount > 3) {
                throw new IllegalArgumentException("the click count for creating new entries must be between 1 and 3 but was " + createEntryClickCount);
            }
        });
    }

    private final ObservableMap<Calendar, BooleanProperty> calendarVisibilityMap = FXCollections.observableHashMap();

    public final ObservableMap<Calendar, BooleanProperty> getCalendarVisibilityMap() {
        return calendarVisibilityMap;
    }

    public final BooleanProperty getCalendarVisibilityProperty(Calendar calendar) {
        return calendarVisibilityMap.computeIfAbsent(calendar, cal -> new SimpleBooleanProperty(DateControl.this, "visible", true));
    }

    public final boolean isCalendarVisible(Calendar calendar) {
        BooleanProperty prop = getCalendarVisibilityProperty(calendar);
        return prop.get();
    }

    public final void setCalendarVisibility(Calendar calendar, boolean visible) {
        BooleanProperty prop = getCalendarVisibilityProperty(calendar);
        prop.set(visible);
    }

    public enum Layer {

        /**
         * Base (and default) presentation layer for entry views.
         */
        BASE,

        /**
         * Top presentation layer for entry views. Presented view entries will be visible above base layer.
         * Uses simple layout system, which does not support resolving of overlapping entries.
         */
        TOP
    }

    private final ObservableSet<Layer> visibleLayers = FXCollections.observableSet(Layer.BASE, Layer.TOP);

    /**
     * Collection of layers which should be visible/presented.
     */
    public final ObservableSet<Layer> visibleLayersProperty() {
        return visibleLayers;
    }

    /**
     * Requests that the date control should reload its data and recreate its
     * entry views. Normally applications do not have to call this method. It is
     * more like a backdoor for client / server applications where the server is
     * unable to push changes to the client. In this case the client must
     * frequently trigger an explicit refresh.
     */
    public final void refreshData() {
        getProperties().put("refresh.data", true);

        getBoundDateControls().forEach(DateControl::refreshData);
    }


    /**
     * Creates a new calendar source that will be added to the list of calendar
     * sources of this date control. The method delegates the actual creation of
     * the calendar source to a factory, which can be specified by calling
     * {@link #setCalendarSourceFactory(Callback)}.
     *
     * @see #setCalendarSourceFactory(Callback)
     */
    public final void createCalendarSource() {
        Callback<CreateCalendarSourceParameter, CalendarSource> factory = getCalendarSourceFactory();
        if (factory != null) {
            CreateCalendarSourceParameter param = new CreateCalendarSourceParameter(this);
            CalendarSource calendarSource = factory.call(param);
            if (calendarSource != null && !getCalendarSources().contains(calendarSource)) {
                getCalendarSources().add(calendarSource);
            }
        }
    }

    // dragged entry support
    private final ObjectProperty<DraggedEntry> draggedEntry = new SimpleObjectProperty<>(this, "draggedEntry");

    /**
     * Stores a {@link DraggedEntry} instance, which serves as a wrapper around
     * the actual entry that is currently being edited by the user. The
     * framework creates this wrapper when the user starts a drag and adds it to
     * the date control. This allows the framework to show the entry at its old
     * and new location at the same time. It also ensures that the calendar does
     * not fire any events before the user has committed the entry to a new
     * location.
     *
     * @return the dragged entry
     */
    public final ObjectProperty<DraggedEntry> draggedEntryProperty() {
        return draggedEntry;
    }

    /**
     * Returns the value of {@link #draggedEntryProperty()}.
     *
     * @return the dragged entry
     */
    public final DraggedEntry getDraggedEntry() {
        return draggedEntry.get();
    }

    /**
     * Sets the value of {@link #draggedEntryProperty()}.
     *
     * @param entry the dragged entry
     */
    public final void setDraggedEntry(DraggedEntry entry) {
        draggedEntryProperty().set(entry);
    }

    /**
     * Creates a new entry at the given time. The method delegates the actual
     * instance creation to the entry factory (see
     * {@link #entryFactoryProperty()}). The factory receives a parameter object
     * that contains the default calendar where the entry can be added, however
     * the factory can choose to add the entry to any calendar it likes. Please
     * note that the time passed to the factory will be adjusted based on the
     * current virtual grid settings (see {@link #virtualGridProperty()}).
     *
     * @param time the time point where the entry will be created (the entry start
     *             time)
     * @return the new calendar entry or null if no entry could be created
     * @see #setEntryFactory(Callback)
     * @see #setVirtualGrid(VirtualGrid)
     */
    public final Entry<?> createEntryAt(ZonedDateTime time) {
        return createEntryAt(time, null, false);
    }

    /**
     * Creates a new entry at the given time. The method delegates the actual
     * instance creation to the entry factory (see
     * {@link #entryFactoryProperty()}). The factory receives a parameter object
     * that contains the calendar where the entry can be added, however the
     * factory can choose to add the entry to any calendar it likes. Please note
     * that the time passed to the factory will be adjusted based on the current
     * virtual grid settings (see {@link #virtualGridProperty()}).
     *
     * @param time     the time point where the entry will be created (the entry start
     *                 time)
     * @param calendar the calendar to which the new entry will be added (if null the
     *                 default calendar provider will be invoked)
     * @return the new calendar entry or null if no entry could be created
     * @see #setEntryFactory(Callback)
     * @see #setVirtualGrid(VirtualGrid)
     */
    public final Entry<?> createEntryAt(ZonedDateTime time, Calendar calendar) {
        return createEntryAt(time, calendar, false);
    }

    /**
     * Creates a new entry at the given time. The method delegates the actual
     * instance creation to the entry factory (see
     * {@link #entryFactoryProperty()}). The factory receives a parameter object
     * that contains the calendar where the entry can be added, however the
     * factory can choose to add the entry to any calendar it likes. Please note
     * that the time passed to the factory will be adjusted based on the current
     * virtual grid settings (see {@link #virtualGridProperty()}).
     *
     * @param time            the time point where the entry will be created (the entry start
     *                        time)
     * @param calendar        the calendar to which the new entry will be added (if null the
     *                        default calendar provider will be invoked)
     * @param initiallyHidden entry will be invisible until the application calls {@link Entry#setHidden(boolean)}.
     * @return the new calendar entry or null if no entry could be created
     * @see #setEntryFactory(Callback)
     * @see #setVirtualGrid(VirtualGrid)
     */
    public final Entry<?> createEntryAt(ZonedDateTime time, Calendar calendar, boolean initiallyHidden) {
        requireNonNull(time);
        VirtualGrid grid = getVirtualGrid();
        if (grid != null) {
            ZonedDateTime timeA = grid.adjustTime(time, false, getFirstDayOfWeek());
            ZonedDateTime timeB = grid.adjustTime(time, true, getFirstDayOfWeek());
            if (Duration.between(time, timeA).abs().minus(Duration.between(time, timeB).abs()).isNegative()) {
                time = timeA;
            } else {
                time = timeB;
            }
        }

        if (calendar == null) {
            Callback<DateControl, Calendar> defaultCalendarProvider = getDefaultCalendarProvider();
            calendar = defaultCalendarProvider.call(this);
        }

        if (calendar != null) {
            if (calendar.isReadOnly()) {
                return null;
            }

            /*
             * We have to ensure that the calendar is visible, otherwise the new
             * entry would not be shown to the user.
             */
            setCalendarVisibility(calendar, true);

            CreateEntryParameter param = new CreateEntryParameter(this, calendar, time);
            Callback<CreateEntryParameter, Entry<?>> factory = getEntryFactory();
            Entry<?> entry = factory.call(param);

            System.out.println(calendar);

            /*
             * This is OK. The factory can return NULL. In this case we
             * assume that the application does not allow to create an entry
             * at the given location.
             */
            if (entry != null) {
                entry.setHidden(initiallyHidden);
                entry.setCalendar(calendar);
            }

            return entry;

        } else {

            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(this.getScene().getWindow());
            alert.initModality(Modality.WINDOW_MODAL);
            alert.setTitle(Messages.getString("DateControl.TITLE_CALENDAR_PROBLEM"));
            alert.setHeaderText(Messages.getString("DateControl.HEADER_TEXT_UNABLE_TO_CREATE_NEW_ENTRY"));
            String newLine = System.getProperty("line.separator");
            alert.setContentText(MessageFormat.format(Messages.getString("DateControl.CONTENT_TEXT_UNABLE_TO_CREATE_NEW_ENTRY"), newLine));
            alert.show();

        }

        return null;
    }

    /**
     * Returns the calendar shown at the given location. This method returns an
     * optional value. Calling this method might or might not make sense,
     * depending on the type of control and the current layout (see
     * {@link #layoutProperty()}).
     *
     * @param x the x-coordinate to check
     * @param y the y-coordinate to check
     * @return the calendar at the given location
     */
    public Optional<Calendar> getCalendarAt(double x, double y) {
        return Optional.empty();
    }

    /**
     * Adjusts the current view / page in such a way that the given entry
     * becomes visible.
     *
     * @param entry the entry to show
     */
    public final void showEntry(Entry<?> entry) {
        requireNonNull(entry);
        doShowEntry(entry, false, true);
    }

    /**
     * Adjusts the current view / page in such a way that the given entry
     * becomes visible and brings up the detail editor / UI for the entry
     * (default is a popover).
     *
     * @param entry the entry to show
     */
    public final void editEntry(Entry<?> entry) {
        requireNonNull(entry);
        doShowEntry(entry, true, true);
    }

    /**
     * Adjusts the current view / page in such a way that the given entry
     * becomes visible and brings up the detail editor / UI for the entry
     * (default is a popover).
     *
     * @param entry      the entry to show
     * @param changeDate change the date of the control to the entry's start date
     */
    public final void editEntry(Entry<?> entry, boolean changeDate) {
        requireNonNull(entry);
        doShowEntry(entry, true, changeDate);
    }

    private void doShowEntry(Entry<?> entry, boolean startEditing, boolean changeDate) {
        layout(); // important so that entry view bounds can be found

        if (changeDate) {
            setDate(entry.getStartDate());
        }

        Platform.runLater(() -> {
            // do not scroll time when a location is already given
            // a location is usually given when the user created a new entry via dragging
            if (!entry.isFullDay()) {
                // wiggle the requested time property
                setRequestedTime(null);
                setRequestedTime(entry.getStartTime());
            }

            Platform.runLater(() -> {
                if (startEditing) {

                    /*
                     * The UI first needs to update itself so that the matching entry
                     * view can be found.
                     */
                    Platform.runLater(() -> doEditEntry(entry));
                } else {
                    Platform.runLater(() -> doBounceEntry(entry));
                }
            });
        });
    }

    private void doEditEntry(Entry<?> entry) {
        EntryViewBase<?> entryView = findEntryView(entry);

        Platform.runLater(() -> {
            if (entryView != null) {
                entryView.bounce();

                Point2D location = entryView.localToScreen(0, 0);

                Callback<EntryDetailsParameter, Boolean> callback = getEntryDetailsCallback();
                EntryDetailsParameter param = new EntryDetailsParameter(null, this, entry, entryView, entryView, location.getX(), location.getY());
                callback.call(param);
            }
        });
    }

    private void doBounceEntry(Entry<?> entry) {
        EntryViewBase<?> entryView = findEntryView(entry);

        if (entryView != null) {
            entryView.bounce();
        }
    }

    private PopOver entryPopOver;

    private void showEntryDetails(Entry<?> entry, Node node, Node owner, double screenY) {
        Callback<EntryDetailsPopOverContentParameter, Node> contentCallback = getEntryDetailsPopOverContentCallback();
        if (contentCallback == null) {
            throw new IllegalStateException("No content callback found for entry popover");
        }

        if (entryPopOver == null || entryPopOver.isDetached()) {
            entryPopOver = new PopOver();
            entryPopOver.setAnimated(false); // important, otherwise too many side effects
        }

        EntryDetailsPopOverContentParameter param = new EntryDetailsPopOverContentParameter(entryPopOver, this, owner, entry);
        Node content = contentCallback.call(param);

        if (content == null) {
            content = new Label(Messages.getString("DateControl.NO_CONTENT"));
        }

        entryPopOver.setContentNode(content);

        ArrowLocation location = ViewHelper.findPopOverArrowLocation(node);
        if (location == null) {
            location = ArrowLocation.TOP_LEFT;
        }

        entryPopOver.setArrowLocation(location);

        Point2D position = ViewHelper.findPopOverArrowPosition(node, screenY, entryPopOver.getArrowSize(), location);

        entryPopOver.show(owner, position.getX(), position.getY());
    }

    /**
     * Creates a new {@link DatePopOver} and shows it attached to the given
     * owner node.
     *
     * @param owner the owner node
     * @param date  the date for which to display more detail
     */
    public void showDateDetails(Node owner, LocalDate date) {
        PopOver datePopOver = new DatePopOver(this, date);
        datePopOver.show(owner);
    }

    private abstract static class ContextMenuParameterBase {

        private final DateControl dateControl;
        private final ContextMenuEvent contextMenuEvent;

        public ContextMenuParameterBase(ContextMenuEvent contextMenuEvent, DateControl dateControl) {
            this.contextMenuEvent = requireNonNull(contextMenuEvent);
            this.dateControl = requireNonNull(dateControl);
        }

        public ContextMenuEvent getContextMenuEvent() {
            return contextMenuEvent;
        }

        public DateControl getDateControl() {
            return dateControl;
        }
    }

    /**
     * The parameter object passed to the entry factory. It contains the most
     * important parameters for creating a new entry: the requesting date
     * control, the time point where the user performed a double click and the default
     * calendar.
     *
     * @see DateControl#entryFactoryProperty()
     * @see DateControl#defaultCalendarProviderProperty()
     * @see DateControl#createEntryAt(ZonedDateTime)
     */
    public static final class CreateEntryParameter {

        private final Calendar calendar;
        private final ZonedDateTime zonedDateTime;
        private final DateControl control;

        /**
         * Constructs a new parameter object.
         *
         * @param control  the control where the user / the application wants to
         *                 create a new entry
         * @param calendar the default calendar
         * @param time     the time selected by the user in the date control
         */
        public CreateEntryParameter(DateControl control, Calendar calendar, ZonedDateTime time) {
            this.control = requireNonNull(control);
            this.calendar = requireNonNull(calendar);
            this.zonedDateTime = requireNonNull(time);
        }

        /**
         * Returns the calendar to which the entry will be added. Applications can add the new entry to
         * this calendar by calling {@link Entry#setCalendar(Calendar)} or they can choose any other calendar.
         *
         * @return the calendar
         */
        public Calendar getCalendar() {
            return calendar;
        }

        /**
         * The time selected by the user.
         *
         * @return the start time for the new entry
         */
        public ZonedDateTime getZonedDateTime() {
            return zonedDateTime;
        }

        /**
         * The date control where the user performed the double click.
         *
         * @return the date control where the event happened
         */
        public DateControl getDateControl() {
            return control;
        }

        @Override
        public String toString() {
            return "CreateEntryParameter [calendar=" + calendar
                    + ", zonedDateTime=" + zonedDateTime + "]";
        }
    }

    private final ObjectProperty<Callback<CreateEntryParameter, Entry<?>>> entryFactory = new SimpleObjectProperty<>(this, "entryFactory");

    /**
     * A factory for creating new entries when the user double clicks inside the
     * date control or when the application calls
     * {@link #createEntryAt(ZonedDateTime)}. The factory can return NULL to
     * indicate that no entry can be created at the given location.
     *
     * <h2>Code Example</h2>
     * <p>
     * The code below shows the default entry factory that is set on every date
     * control.
     * <pre>
     * setEntryFactory(param -&gt; {
     * 	DateControl control = param.getControl();
     * 	VirtualGrid grid = control.getVirtualGrid();
     * 	ZonedDateTime time = param.getZonedDateTime();
     * 	DayOfWeek firstDayOfWeek = getFirstDayOfWeek();
     *
     * 	ZonedDateTime lowerTime = grid.adjustTime(time, false, firstDayOfWeek);
     * 	ZonedDateTime upperTime = grid.adjustTime(time, true, firstDayOfWeek);
     *
     * 	if (Duration.between(time, lowerTime).abs().minus(Duration.between(time, upperTime).abs()).isNegative()) {
     * 		time = lowerTime;
     *    } else {
     * 		time = upperTime;
     *    }
     *
     * 	Entry&lt;Object&gt; entry = new Entry&lt;&gt;(&quot;New Entry&quot;);
     * 	entry.changeStartDate(time.toLocalDate());
     * 	entry.changeStartTime(time.toLocalTime());
     * 	entry.changeEndDate(entry.getStartDate());
     * 	entry.changeEndTime(entry.getStartTime().plusHours(1));
     *
     * 	if (control instanceof AllDayView) {
     * 		entry.setFullDay(true);
     *    }
     *
     * 	return entry;
     * });
     * </pre>
     *
     * @return the entry factory callback
     */
    public final ObjectProperty<Callback<CreateEntryParameter, Entry<?>>> entryFactoryProperty() {
        return entryFactory;
    }

    /**
     * Returns the value of {@link #entryFactoryProperty()}.
     *
     * @return the factory used for creating a new entry
     */
    public final Callback<CreateEntryParameter, Entry<?>> getEntryFactory() {
        return entryFactoryProperty().get();
    }

    /**
     * Sets the value of {@link #entryFactoryProperty()}.
     *
     * @param factory the factory used for creating a new entry
     */
    public final void setEntryFactory(Callback<CreateEntryParameter, Entry<?>> factory) {
        Objects.requireNonNull(factory);
        entryFactoryProperty().set(factory);
    }

    /*
     * Calendar source callback.
     */

    /**
     * The parameter object passed to the calendar source factory.
     *
     * @see DateControl#setCalendarSourceFactory(Callback)
     */
    public static final class CreateCalendarSourceParameter {

        private final DateControl dateControl;

        /**
         * Constructs a new parameter object.
         *
         * @param dateControl the control where the source will be added
         */
        public CreateCalendarSourceParameter(DateControl dateControl) {
            this.dateControl = requireNonNull(dateControl);
        }

        /**
         * The control where the source will be added.
         *
         * @return the date control
         */
        public DateControl getDateControl() {
            return dateControl;
        }
    }

    private final ObjectProperty<Callback<CreateCalendarSourceParameter, CalendarSource>> calendarSourceFactory = new SimpleObjectProperty<>(this, "calendarSourceFactory");

    /**
     * A factory for creating a new calendar source, e.g. a new Google calendar
     * account.
     *
     * <h2>Code Example</h2> The code below shows the default implementation of
     * this factory. Applications can choose to bring up a full-featured user
     * interface / dialog to specify the exact location of the source (either
     * locally or over a network). A local calendar source might read its data
     * from an XML file while a remote source could load data from a web
     * service.
     *
     * <pre>
     * setCalendarSourceFactory(param -&gt; {
     * 	CalendarSource source = new CalendarSource(&quot;Calendar Source&quot;);
     * 	Calendar calendar = new Calendar(&quot;Calendar&quot;);
     * 	source.getCalendars().add(calendar);
     * 	return source;
     * });
     * </pre>
     * <p>
     * The factory can be invoked by calling {@link #createCalendarSource()}.
     *
     * @return the calendar source factory
     * @see #createCalendarSource()
     */
    public final ObjectProperty<Callback<CreateCalendarSourceParameter, CalendarSource>> calendarSourceFactoryProperty() {
        return calendarSourceFactory;
    }

    /**
     * Returns the value of {@link #calendarSourceFactoryProperty()}.
     *
     * @return the calendar source factory
     */
    public final Callback<CreateCalendarSourceParameter, CalendarSource> getCalendarSourceFactory() {
        return calendarSourceFactoryProperty().get();
    }

    /**
     * Sets the value of {@link #calendarSourceFactoryProperty()}.
     *
     * @param callback the callback used for creating a new calendar source
     */
    public final void setCalendarSourceFactory(Callback<CreateCalendarSourceParameter, CalendarSource> callback) {
        calendarSourceFactoryProperty().set(callback);
    }

    /*
     * Context menu callback for entries.
     */

    /**
     * The parameter object passed to the context menu callback for entries.
     *
     * @see DateControl#entryContextMenuCallbackProperty()
     */
    public static final class EntryContextMenuParameter extends ContextMenuParameterBase {

        private final EntryViewBase<?> entryView;

        /**
         * Constructs a new context menu parameter object.
         *
         * @param evt       the event that triggered the context menu
         * @param control   the date control where the event occurred
         * @param entryView the entry view for which the context menu will be created
         */
        public EntryContextMenuParameter(ContextMenuEvent evt, DateControl control, EntryViewBase<?> entryView) {
            super(evt, control);
            this.entryView = requireNonNull(entryView);
        }

        /**
         * The entry view for which the context menu will be shown.
         *
         * @return the entry view
         */
        public EntryViewBase<?> getEntryView() {
            return entryView;
        }

        /**
         * Convenience method to easily look up the entry for which the view was
         * created.
         *
         * @return the calendar entry
         */
        public Entry<?> getEntry() {
            return entryView.getEntry();
        }

        /**
         * Convenience method to easily look up the calendar of the entry for
         * which the view was created.
         *
         * @return the calendar
         */
        public Calendar getCalendar() {
            return getEntry().getCalendar();
        }

        @Override
        public String toString() {
            return "EntryContextMenuParameter [entry=" + entryView
                    + ", dateControl =" + getDateControl() + "]";
        }
    }

    // entry edit support

    /**
     * Possible edit operations on an entry. This enum will be used as parameter of the
     * callback set with {@link DateControl#setEntryEditPolicy}.
     *
     * @see #setEntryEditPolicy(Callback)
     */
    public enum EditOperation {

        /**
         * Checked if the start of an entry can be changed.
         */
        CHANGE_START,

        /**
         * Checked if the end of an entry can be changed.
         */
        CHANGE_END,

        /**
         * Checked if entry can be moved around, hence changing start and end time at
         * the same time.
         */
        MOVE,

        /**
         * Checked if an entry can be deleted.
         */
        DELETE
    }

    /**
     * Class used for parameter of {@link DateControl#entryEditPolicy}
     * functional interface.
     */
    public static final class EntryEditParameter {

        /**
         * The date control the entity is associated with.
         */
        private final DateControl dateControl;

        /**
         * The entity the operation is operated on.
         */
        private final Entry<?> entry;

        /**
         * The operation.
         */
        private final DateControl.EditOperation editOperation;

        public EntryEditParameter(DateControl dateControl, Entry<?> entry, EditOperation editOperation) {
            this.dateControl = Objects.requireNonNull(dateControl);
            this.entry = Objects.requireNonNull(entry);
            this.editOperation = Objects.requireNonNull(editOperation);
        }

        /**
         * The {@link DateControl} which is asking for a specific {@link DateControl.EditOperation} permission.
         *
         * @return The date control.
         */
        public DateControl getDateControl() {
            return dateControl;
        }

        /**
         * The entry where the {@link com.calendarfx.view.DateControl.EditOperation} should be applied.
         *
         * @return The entry.
         */
        public Entry<?> getEntry() {
            return entry;
        }

        /**
         * The actual edit operation.
         *
         * @return The edit operation.
         */
        public EditOperation getEditOperation() {
            return editOperation;
        }

        @Override
        public String toString() {
            return "EntryEditParameter{" +
                    "dateControl=" + dateControl +
                    ", entry=" + entry +
                    ", editOperation=" + editOperation +
                    '}';
        }
    }

    private final ObjectProperty<Callback<EntryEditParameter, Boolean>> entryEditPolicy = new SimpleObjectProperty<>(action -> true);

    /**
     * A property that stores a callback used for editing entries. If an edit operation will be executed
     * on an entry then the callback will be invoked to determine if the operation is allowed. By default,
     * all operations listed inside {@link EditOperation} are allowed.
     *
     * @return the property
     * @see EditOperation
     */
    public final ObjectProperty<Callback<EntryEditParameter, Boolean>> entryEditPolicyProperty() {
        return entryEditPolicy;
    }

    /**
     * Returns the value of {@link #entryEditPolicy}.
     *
     * @return The entry edit policy callback
     * @see EditOperation
     */
    public final Callback<EntryEditParameter, Boolean> getEntryEditPolicy() {
        return entryEditPolicy.get();
    }

    /**
     * Sets the value of {@link #entryEditPolicy}.
     *
     * @param policy the entry edit policy callback
     * @see EditOperation
     */
    public final void setEntryEditPolicy(Callback<EntryEditParameter, Boolean> policy) {
        Objects.requireNonNull(policy, "The edit entry policy can not be null");
        this.entryEditPolicy.set(policy);
    }

    private final ObjectProperty<Callback<EntryContextMenuParameter, ContextMenu>> entryContextMenuCallback = new SimpleObjectProperty<>(this, "entryFactory");

    /**
     * A callback used for dynamically creating a context menu for a given
     * entry view.
     *
     * <h2>Code Example</h2> The code below shows the default implementation of
     * this callback.
     * <pre>
     * setEntryContextMenuCallback(param -&gt; {
     * 	EntryViewBase&lt;?&gt; entryView = param.getEntryView();
     * 	Entry&lt;?&gt; entry = entryView.getEntry();
     *
     * 	ContextMenu contextMenu = new ContextMenu();
     *
     * 	MenuItem informationItem = new MenuItem(&quot;Information&quot;);
     * 	informationItem.setOnAction(evt -&gt; {
     * 		Callback&lt;EntryDetailsParameter, Boolean&gt; detailsCallback = getEntryDetailsCallback();
     * 		if (detailsCallback != null) {
     * 			ContextMenuEvent ctxEvent = param.getContextMenuEvent();
     * 			EntryDetailsParameter entryDetailsParam = new EntryDetailsParameter(ctxEvent, DateControl.this, entryView, this, ctxEvent.getScreenX(), ctxEvent.getScreenY());
     * 			detailsCallback.call(entryDetailsParam);
     *        }
     *    });
     * 	contextMenu.getItems().add(informationItem);
     *
     * 	Menu calendarMenu = new Menu(&quot;Calendar&quot;);
     * 	for (Calendar calendar : getCalendars()) {
     * 		MenuItem calendarItem = new MenuItem(calendar.getName());
     * 		calendarItem.setOnAction(evt -&gt; entry.setCalendar(calendar));
     * 		calendarMenu.getItems().add(calendarItem);
     *    }
     * 	contextMenu.getItems().add(calendarMenu);
     *
     * 	return contextMenu;
     * });
     * </pre>
     *
     * @return the property used for storing the callback
     */
    public final ObjectProperty<Callback<EntryContextMenuParameter, ContextMenu>> entryContextMenuCallbackProperty() {
        return entryContextMenuCallback;
    }

    /**
     * Returns the value of {@link #entryContextMenuCallbackProperty()}.
     *
     * @return the callback for creating a context menu for a given calendar
     * entry
     */
    public final Callback<EntryContextMenuParameter, ContextMenu> getEntryContextMenuCallback() {
        return entryContextMenuCallbackProperty().get();
    }

    /**
     * Sets the value of {@link #entryContextMenuCallbackProperty()}.
     *
     * @param callback the callback used for creating a context menu for a calendar
     *                 entry
     */
    public final void setEntryContextMenuCallback(Callback<EntryContextMenuParameter, ContextMenu> callback) {
        entryContextMenuCallbackProperty().set(callback);
    }

    /*
     * Context menu callback.
     */

    /**
     * The parameter object passed to the context menu callback.
     *
     * @see DateControl#contextMenuCallbackProperty()
     */
    public static final class ContextMenuParameter extends ContextMenuParameterBase {

        private final Calendar calendar;
        private final ZonedDateTime zonedDateTime;

        /**
         * Constructs a new parameter object.
         *
         * @param evt         the event that triggered the context menu
         * @param dateControl the date control where the event occurred
         * @param calendar    the (default) calendar where newly created entries should
         *                    be added (can be null if no editable calendar was found)
         * @param time        the time point where the mouse click occurred
         */
        public ContextMenuParameter(ContextMenuEvent evt, DateControl dateControl, Calendar calendar, ZonedDateTime time) {
            super(evt, dateControl);
            this.calendar = calendar;
            this.zonedDateTime = time;
        }

        /**
         * The (default) calendar where newly created entries should be added.
         * Only relevant if the context menu is actually used for creating new
         * entries. This can be different from application to application.
         *
         * @return the (default) calendar for adding new entries
         */
        public Calendar getCalendar() {
            return calendar;
        }

        /**
         * The time point where the mouse click occurred.
         *
         * @return the time shown at the mouse click location
         */
        public ZonedDateTime getZonedDateTime() {
            return zonedDateTime;
        }

        @Override
        public String toString() {
            return "ContextMenuParameter [calendar=" + calendar + ", zonedDateTime=" + zonedDateTime + "]";
        }
    }

    private final ObjectProperty<Callback<ContextMenuParameter, ContextMenu>> contextMenuCallback = new SimpleObjectProperty<>(this, "contextMenuCallback");

    /**
     * The context menu callback that will be invoked when the user triggers the
     * context menu by clicking in an area without an entry view. Using a
     * callback allows the application to create context menus with different
     * content, depending on the current state of the application and the
     * location of the click.
     *
     * <h2>Code Example</h2>
     * <p>
     * The code below shows a part of the default implementation:
     * <pre>
     * setContextMenuCallback(param -&gt; {
     * 	ContextMenu menu = new ContextMenu();
     * 	MenuItem newEntryItem = new MenuItem(&quot;Add New Event&quot;);
     * 	newEntryItem.setOnAction(evt -&gt; {
     * 		createEntryAt(param.getZonedDateTime());
     *    });
     * 	menu.getItems().add(newEntry);
     * 	return menu;
     * });
     * </pre>
     *
     * @return the context menu callback
     */
    public final ObjectProperty<Callback<ContextMenuParameter, ContextMenu>> contextMenuCallbackProperty() {
        return contextMenuCallback;
    }

    /**
     * Returns the value of {@link #contextMenuCallbackProperty()}.
     *
     * @return the context menu callback
     */
    public final Callback<ContextMenuParameter, ContextMenu> getContextMenuCallback() {
        return contextMenuCallbackProperty().get();
    }

    /**
     * Sets the value of {@link #contextMenuCallbackProperty()}.
     *
     * @param callback the context menu callback
     */
    public final void setContextMenuCallback(Callback<ContextMenuParameter, ContextMenu> callback) {
        contextMenuCallbackProperty().set(callback);
    }

    /*
     * Default calendar provider callback.
     */
    private final ObjectProperty<Callback<DateControl, Calendar>> defaultCalendarProvider = new SimpleObjectProperty<>(this, "defaultCalendarProvider");

    /**
     * The default calendar provider is responsible for returning a calendar
     * that can be used to add a new entry. This way the user can add new
     * entries by simply double-clicking inside the view without the need of
     * first showing a calendar selection UI. This can be changed by setting a
     * callback that prompts the user with a dialog.
     *
     * <h2>Code Example</h2>
     * <p>
     * The code shown below is the default implementation of this provider. It
     * returns the first calendar of the first source. If no source is available
     * it will return null.
     * <pre>
     * setDefaultCalendarProvider(control -&gt; {
     * 	List&lt;CalendarSource&gt; sources = getCalendarSources();
     * 	if (sources != null &amp;&amp; !sources.isEmpty()) {
     * 		CalendarSource s = sources.get(0);
     * 		List&lt;? extends Calendar&gt; calendars = s.getCalendars();
     * 		if (calendars != null &amp;&amp; !calendars.isEmpty()) {
     * 			return calendars.get(0);
     *        }
     *    }
     *
     * 	return null;
     * });
     * </pre>
     *
     * @return the default calendar provider callback
     */
    public final ObjectProperty<Callback<DateControl, Calendar>> defaultCalendarProviderProperty() {
        return defaultCalendarProvider;
    }

    /**
     * Returns the value of {@link #defaultCalendarProviderProperty()}.
     *
     * @return the default calendar provider
     */
    public final Callback<DateControl, Calendar> getDefaultCalendarProvider() {
        return defaultCalendarProviderProperty().get();
    }

    /**
     * Sets the value of {@link #defaultCalendarProviderProperty()}.
     *
     * @param provider the default calendar provider
     */
    public final void setDefaultCalendarProvider(Callback<DateControl, Calendar> provider) {
        requireNonNull(provider);
        defaultCalendarProviderProperty().set(provider);
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

    private abstract static class DetailsParameter {

        private final InputEvent inputEvent;
        private final DateControl dateControl;
        private final Node owner;
        private final double screenX;
        private final double screenY;
        private final Node node;

        /**
         * Constructs a new parameter object.
         *
         * @param inputEvent the input event that triggered the need for showing entry
         *                   details (e.g. a mouse double click, or a context menu item
         *                   selection)
         * @param control    the control where the event occurred
         * @param owner      a node that can be used as an owner for the dialog or
         *                   popover
         * @param screenX    the screen location where the event occurred
         * @param screenY    the screen location where the event occurred
         */
        public DetailsParameter(InputEvent inputEvent, DateControl control, Node node, Node owner, double screenX, double screenY) {
            this.inputEvent = inputEvent;
            this.dateControl = requireNonNull(control);
            this.node = requireNonNull(node);
            this.owner = requireNonNull(owner);
            this.screenX = screenX;
            this.screenY = screenY;
        }

        /**
         * Returns the node that should be used as the owner of a dialog /
         * popover. We should not use the entry view as the owner of a dialog /
         * popover because views come and go. We need something that lives
         * longer. A good candidate will be the root node of the scene.
         *
         * @return an owner node for the detail dialog / popover
         */
        public Node getOwner() {
            return owner;
        }

        /**
         * Returns the node that will be used for calculating the position
         * of the detail dialog / popover. Popovers will point an arrow at the
         * given node.
         *
         * @return the annotated node
         */
        public Node getNode() {
            return node;
        }

        /**
         * The screen X location where the event occurred.
         *
         * @return the screen x location of the event
         */
        public double getScreenX() {
            return screenX;
        }

        /**
         * The screen Y location where the event occurred.
         *
         * @return the screen y location of the event
         */
        public double getScreenY() {
            return screenY;
        }

        /**
         * The input event that triggered the need for showing entry details
         * (e.g. a mouse double click or a context menu item selection).
         *
         * @return the input event
         */
        public InputEvent getInputEvent() {
            return inputEvent;
        }

        /**
         * The date control where the event occurred.
         *
         * @return the date control
         */
        public DateControl getDateControl() {
            return dateControl;
        }
    }

    /**
     * The parameter object passed to the entry details callback.
     *
     * @see DateControl#entryDetailsCallbackProperty()
     */
    public final static class EntryDetailsParameter extends DetailsParameter {

        private final Entry<?> entry;

        /**
         * Constructs a new parameter object.
         *
         * @param inputEvent the input event that triggered the need for showing entry
         *                   details (e.g. a mouse double click, or a context menu item
         *                   selection)
         * @param control    the control where the event occurred
         * @param entry      the entry for which details are requested
         * @param node       the node to which the popover will be placed relative too (when using popovers)
         * @param owner      a node that can be used as an owner for the dialog or
         *                   popover
         * @param screenX    the screen location where the event occurred
         * @param screenY    the screen location where the event occurred
         */
        public EntryDetailsParameter(InputEvent inputEvent, DateControl control, Entry<?> entry, Node node, Node owner, double screenX, double screenY) {
            super(inputEvent, control, node, owner, screenX, screenY);
            this.entry = entry;
        }

        /**
         * The entry for which details are requested.
         *
         * @return the entry view
         */
        public Entry<?> getEntry() {
            return entry;
        }
    }

    /**
     * The parameter object passed to the date details callback.
     *
     * @see DateControl#dateDetailsCallbackProperty()
     */
    public final static class DateDetailsParameter extends DetailsParameter {

        private final LocalDate localDate;

        /**
         * Constructs a new parameter object.
         *
         * @param inputEvent the input event that triggered the need for showing entry
         *                   details (e.g. a mouse double click, or a context menu item
         *                   selection)
         * @param control    the control where the event occurred
         * @param date       the date for which details are required
         * @param node       the annotated node (popover will point at it with an arrow)
         * @param owner      a node that can be used as an owner for the dialog or popover
         * @param screenX    the screen location where the event occurred
         * @param screenY    the screen location where the event occurred
         */
        public DateDetailsParameter(InputEvent inputEvent, DateControl control, Node node, Node owner, LocalDate date, double screenX, double screenY) {
            super(inputEvent, control, node, owner, screenX, screenY);
            this.localDate = requireNonNull(date);
        }

        /**
         * The date for which details are required.
         *
         * @return the date
         */
        public LocalDate getLocalDate() {
            return localDate;
        }
    }

    private final ObjectProperty<Callback<DateDetailsParameter, Boolean>> dateDetailsCallback = new SimpleObjectProperty<>(this, "dateDetailsCallback");

    /**
     * A callback used for showing the details of a given date. The default
     * implementation of this callback displays a small {@link PopOver} but
     * applications might as well display a large dialog where the user can
     * freely edit the date.
     *
     * <h2>Code Example</h2> The code below shows the default implementation
     * used by all date controls. It delegates to a private method that shows
     * the popover.
     *
     * <pre>
     * setDateDetailsCallback(param -&gt; {
     * 	InputEvent evt = param.getInputEvent();
     * 	if (evt instanceof MouseEvent) {
     * 		MouseEvent mouseEvent = (MouseEvent) evt;
     * 		if (mouseEvent.getClickCount() == 1) {
     * 			showDateDetails(param.getOwner(), param.getLocalDate());
     * 			return true;
     *        }
     *    }
     *
     * 	return false;
     * });
     * </pre>
     *
     * @return the callback for showing details for a given date
     */
    public final ObjectProperty<Callback<DateDetailsParameter, Boolean>> dateDetailsCallbackProperty() {
        return dateDetailsCallback;
    }

    /**
     * Sets the value of {@link #dateDetailsCallbackProperty()}.
     *
     * @param callback the date details callback
     */
    public final void setDateDetailsCallback(Callback<DateDetailsParameter, Boolean> callback) {
        requireNonNull(callback);
        dateDetailsCallbackProperty().set(callback);
    }

    /**
     * Returns the value of {@link #dateDetailsCallbackProperty()}.
     *
     * @return the date details callback
     */
    public final Callback<DateDetailsParameter, Boolean> getDateDetailsCallback() {
        return dateDetailsCallbackProperty().get();
    }

    private final ObjectProperty<Callback<EntryDetailsParameter, Boolean>> entryDetailsCallback = new SimpleObjectProperty<>(this, "entryDetailsCallback");

    /**
     * A callback used for showing the details of a given entry. The default
     * implementation of this callback displays a small {@link PopOver} but
     * applications might as well display a large dialog where the user can
     * freely edit the entry.
     *
     * <h2>Code Example</h2> The code below shows the default implementation
     * used by all date controls. It delegates to a private method that shows
     * the popover.
     *
     * <pre>
     * setEntryDetailsCallback(param -&gt; {
     * 	InputEvent evt = param.getInputEvent();
     * 	if (evt instanceof MouseEvent) {
     * 		MouseEvent mouseEvent = (MouseEvent) evt;
     * 		if (mouseEvent.getClickCount() == 2) {
     * 			showEntryDetails(param.getEntryView(), param.getOwner(), param.getScreenX(), param.getScreenY());
     * 			return true;
     *        }
     *    } else {
     * 		showEntryDetails(param.getEntryView(), param.getOwner(), param.getScreenX(), param.getScreenY());
     * 		return true;
     *    }
     *
     * 	return false;
     * });
     * </pre>
     *
     * @return the callback used for showing details for a given entry
     */
    public final ObjectProperty<Callback<EntryDetailsParameter, Boolean>> entryDetailsCallbackProperty() {
        return entryDetailsCallback;
    }

    /**
     * Sets the value of {@link #entryDetailsCallbackProperty()}.
     *
     * @param callback the entry details callback
     */
    public final void setEntryDetailsCallback(Callback<EntryDetailsParameter, Boolean> callback) {
        requireNonNull(callback);
        entryDetailsCallbackProperty().set(callback);
    }

    /**
     * Returns the value of {@link #entryDetailsCallbackProperty()}.
     *
     * @return the entry details callback
     */
    public final Callback<EntryDetailsParameter, Boolean> getEntryDetailsCallback() {
        return entryDetailsCallbackProperty().get();
    }

    ////////////

    /**
     * The parameter object passed to the entry details popover content
     * callback.
     *
     * @see DateControl#entryDetailsPopOverContentCallbackProperty()
     */
    public final static class EntryDetailsPopOverContentParameter {

        private final DateControl dateControl;

        private final Node node;

        private final Entry<?> entry;

        private final PopOver popOver;

        /**
         * Constructs a new parameter object.
         *
         * @param popOver the popover for which details will be created
         * @param control the control where the event occurred
         * @param node    the node where the event occurred
         * @param entry   the entry for which details will be shown
         */
        public EntryDetailsPopOverContentParameter(PopOver popOver, DateControl control, Node node, Entry<?> entry) {
            this.popOver = requireNonNull(popOver);
            this.dateControl = requireNonNull(control);
            this.node = requireNonNull(node);
            this.entry = requireNonNull(entry);
        }

        /**
         * Returns the popover in which the content will be shown.
         *
         * @return the popover
         */
        public PopOver getPopOver() {
            return popOver;
        }

        /**
         * The date control where the popover was requested.
         *
         * @return the date control
         */
        public DateControl getDateControl() {
            return dateControl;
        }

        /**
         * The node for which the popover was requested.
         *
         * @return the node
         */
        public Node getNode() {
            return node;
        }

        /**
         * The entry for which the popover was requested.
         *
         * @return the entry
         */
        public Entry<?> getEntry() {
            return entry;
        }
    }

    private final ObjectProperty<Callback<EntryDetailsPopOverContentParameter, Node>> entryDetailsPopoverContentCallback = new SimpleObjectProperty<>(this, "entryDetailsPopoverContentCallback");

    /**
     * Stores a callback for creating the content of the popover.
     *
     * @return the popover content callback
     */
    public final ObjectProperty<Callback<EntryDetailsPopOverContentParameter, Node>> entryDetailsPopOverContentCallbackProperty() {
        return entryDetailsPopoverContentCallback;
    }

    /**
     * Sets the value of {@link #entryDetailsPopOverContentCallbackProperty()}.
     *
     * @param callback the entry details popover content callback
     */
    public final void setEntryDetailsPopOverContentCallback(Callback<EntryDetailsPopOverContentParameter, Node> callback) {
        requireNonNull(callback);
        entryDetailsPopOverContentCallbackProperty().set(callback);
    }

    /**
     * Returns the value of
     * {@link #entryDetailsPopOverContentCallbackProperty()}.
     *
     * @return the entry details popover content callback
     */
    public final Callback<EntryDetailsPopOverContentParameter, Node> getEntryDetailsPopOverContentCallback() {
        return entryDetailsPopOverContentCallbackProperty().get();
    }

    ///////////

    private final ObjectProperty<LocalDate> today = new SimpleObjectProperty<>(this, "today", LocalDate.now());

    /**
     * Stores the date that is considered to represent "today". This property is
     * initialized with {@link LocalDate#now()} but can be any date.
     *
     * @return the date representing "today"
     */
    public final ObjectProperty<LocalDate> todayProperty() {
        return today;
    }

    /**
     * Sets the value of {@link #todayProperty()}.
     *
     * @param date the date representing "today"
     */
    public final void setToday(LocalDate date) {
        requireNonNull(date);
        todayProperty().set(date);
    }

    /**
     * Returns the value of {@link #todayProperty()}.
     *
     * @return the date representing "today"
     */
    public final LocalDate getToday() {
        return todayProperty().get();
    }

    private final BooleanProperty showToday = new SimpleBooleanProperty(this, "showToday", true);

    /**
     * A flag used to indicate that the view will mark the area that represents
     * the value of {@link #todayProperty()}. By default, this area will be
     * filled with a different color (red) than the rest (white).
     * <img src="doc-files/all-day-view-today.png" alt="All Day View Today">
     *
     * @return true if today will be shown differently
     */
    public final BooleanProperty showTodayProperty() {
        return showToday;
    }

    /**
     * Returns the value of {@link #showTodayProperty()}.
     *
     * @return true if today will be highlighted visually
     */
    public final boolean isShowToday() {
        return showTodayProperty().get();
    }

    /**
     * Sets the value of {@link #showTodayProperty()}.
     *
     * @param show if true today will be highlighted visually
     */
    public final void setShowToday(boolean show) {
        showTodayProperty().set(show);
    }

    private final BooleanProperty showNoonMarker = new SimpleBooleanProperty(this, "showNoonMarker", true);

    public final boolean isShowNoonMarker() {
        return showNoonMarker.get();
    }

    /**
     * A property used to indicate whether the day view should mark noon with a special
     * marker.
     *
     * @return true if noon will be marked in a special way
     */
    public final BooleanProperty showNoonMarkerProperty() {
        return showNoonMarker;
    }

    public final void setShowNoonMarker(boolean showNoonMarker) {
        this.showNoonMarker.set(showNoonMarker);
    }

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date", LocalDate.now());

    /**
     * The date that needs to be shown by the date control. This property is
     * initialized with {@link LocalDate#now()}.
     *
     * @return the date shown by the control
     */
    public final ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    /**
     * Sets the value of {@link #dateProperty()}.
     *
     * @param date the date shown by the control
     */
    public final void setDate(LocalDate date) {
        requireNonNull(date);
        dateProperty().set(date);
    }

    /**
     * Returns the value of {@link #dateProperty()}.
     *
     * @return the date shown by the control
     */
    public final LocalDate getDate() {
        return dateProperty().get();
    }


    private final BooleanProperty enableTimeZoneSupport = new SimpleBooleanProperty(this, "enableTimeZoneSupport", false);

    public final boolean isEnableTimeZoneSupport() {
        return enableTimeZoneSupport.get();
    }

    /**
     * Enables or disables user options to work with different time zones.
     *
     * @return true if time zone support is enabled
     * @see DateControl#zoneIdProperty()
     * @see Entry#zoneIdProperty()
     * @see Interval#getZoneId()
     */
    public final BooleanProperty enableTimeZoneSupportProperty() {
        return enableTimeZoneSupport;
    }

    public final void setEnableTimeZoneSupport(boolean enableTimeZoneSupport) {
        this.enableTimeZoneSupport.set(enableTimeZoneSupport);
    }

    private final ObjectProperty<ZoneId> zoneId = new SimpleObjectProperty<>(this, "zoneId", ZoneId.systemDefault());

    /**
     * The time zone used by the date control. Entries and date controls might
     * use different time zones resulting in different layout of entry views.
     * <p>
     * #see {@link Entry#zoneIdProperty()}
     *
     * @return the time zone used by the date control for calculating entry view
     * layouts
     */
    public final ObjectProperty<ZoneId> zoneIdProperty() {
        return zoneId;
    }

    /**
     * Sets the value of {@link #zoneIdProperty()}.
     *
     * @param zoneId the time zone
     */
    public final void setZoneId(ZoneId zoneId) {
        requireNonNull(zoneId);
        zoneIdProperty().set(zoneId);
    }

    /**
     * Returns the value of {@link #zoneIdProperty()}.
     *
     * @return the time zone
     */
    public final ZoneId getZoneId() {
        return zoneIdProperty().get();
    }

    private final ObjectProperty<LocalTime> time = new SimpleObjectProperty<>(this, "time", LocalTime.now());

    /**
     * Stores a time that can be visualized, e.g. the thin line in
     * {@link DayView} representing the current time.
     *
     * @return the current time
     */
    public final ObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    /**
     * Sets the value of {@link #timeProperty()}.
     *
     * @param time the current time
     */
    public final void setTime(LocalTime time) {
        requireNonNull(time);
        timeProperty().set(time);
    }

    /**
     * Returns the value of {@link #timeProperty()}.
     *
     * @return the current time
     */
    public final LocalTime getTime() {
        return timeProperty().get();
    }

    /**
     * Returns the zoned date time version of the current time.
     *
     * @return the zoned date time version of the current time property
     * @see #getTime()
     */
    public final ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.of(getDate(), getTime(), getZoneId());
    }

    private final ObjectProperty<LocalTime> startTime = new SimpleObjectProperty<>(this, "startTime", LocalTime.of(6, 0));

    /**
     * A start time used to limit the time interval shown by the control. The
     * {@link DayView} uses this property and the {@link #endTimeProperty()} to
     * support the concept of "early" and "late" hours. These hours can be
     * hidden if required.
     *
     * @return the start time
     */
    public final ObjectProperty<LocalTime> startTimeProperty() {
        return startTime;
    }

    /**
     * Returns the value of {@link #startTimeProperty()}.
     *
     * @return the start time
     */
    public final LocalTime getStartTime() {
        return startTimeProperty().get();
    }

    /**
     * Sets the value of {@link #startTimeProperty()}.
     *
     * @param time the start time
     */
    public final void setStartTime(LocalTime time) {
        startTimeProperty().set(time);
    }

    private final ObjectProperty<LocalTime> endTime = new SimpleObjectProperty<>(this, "endTime", LocalTime.of(22, 0));

    /**
     * An end time used to limit the time interval shown by the control. The
     * {@link DayView} uses this property and the {@link #startTimeProperty()}
     * to support the concept of "early" and "late" hours. These hours can be
     * hidden if required.
     *
     * @return the end time
     */
    public final ObjectProperty<LocalTime> endTimeProperty() {
        return endTime;
    }

    /**
     * Returns the value of {@link #endTimeProperty()}.
     *
     * @return the end time
     */
    public final LocalTime getEndTime() {
        return endTimeProperty().get();
    }

    /**
     * Sets the value of {@link #endTimeProperty()}.
     *
     * @param time the end time
     */
    public final void setEndTime(LocalTime time) {
        endTimeProperty().set(time);
    }

    private final ObjectProperty<WeekFields> weekFields = new SimpleObjectProperty<>(this, "weekFields", WeekFields.of(Locale.getDefault()));

    /**
     * Week fields are used to determine the first day of a week (e.g. "Monday"
     * in Germany or "Sunday" in the US). It is also used to calculate the week
     * number as the week fields determine how many days are needed in the first
     * week of a year. This property is initialized with {@link WeekFields#ISO}.
     *
     * @return the week fields
     */
    public final ObjectProperty<WeekFields> weekFieldsProperty() {
        return weekFields;
    }

    /**
     * Sets the value of {@link #weekFieldsProperty()}.
     *
     * @param weekFields the new week fields
     */
    public final void setWeekFields(WeekFields weekFields) {
        requireNonNull(weekFields);
        weekFieldsProperty().set(weekFields);
    }

    /**
     * Returns the value of {@link #weekFieldsProperty()}.
     *
     * @return the week fields
     */
    public final WeekFields getWeekFields() {
        return weekFieldsProperty().get();
    }

    /**
     * A convenience method to look up the first day of the week ("Monday" in
     * Germany, "Sunday" in the US). This method delegates to
     * {@link WeekFields#getFirstDayOfWeek()}.
     *
     * @return the first day of the week
     * @see #weekFieldsProperty()
     */
    public final DayOfWeek getFirstDayOfWeek() {
        return getWeekFields().getFirstDayOfWeek();
    }

    private final ReadOnlyListWrapper<Calendar> calendars = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    /**
     * A list that contains all calendars found in all calendar sources
     * currently attached to this date control. This is a convenience list that
     * "flattens" the two level structure of sources and their calendars. It is
     * a read-only list because calendars can not be added directly to a date
     * control. Instead, they are added to calendar sources and those sources are
     * then added to the control.
     *
     * @return the list of all calendars shown by this control
     * @see #getCalendarSources()
     */
    public final ReadOnlyListProperty<Calendar> calendarsProperty() {
        return calendars.getReadOnlyProperty();
    }

    private final ObservableList<Calendar> unmodifiableCalendars = FXCollections.unmodifiableObservableList(calendars.get());

    /**
     * Returns the value of {@link #calendarsProperty()}.
     *
     * @return the list of all calendars shown by this control
     */
    public final ObservableList<Calendar> getCalendars() {
        return unmodifiableCalendars;
    }

    private final ObservableList<CalendarSource> calendarSources = FXCollections.observableArrayList();

    /**
     * The list of all calendar sources attached to this control. The calendars
     * found in all sources are also added to the read-only list that can be
     * retrieved by calling {@link #getCalendars()}.
     *
     * @return the calendar sources
     * @see #calendarSourceFactoryProperty()
     */
    public final ObservableList<CalendarSource> getCalendarSources() {
        return calendarSources;
    }

    private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(this, "selectionMode", SelectionMode.MULTIPLE);

    /**
     * Stores the selection mode. All date controls support single and multiple
     * selections.
     *
     * @return the selection mode
     * @see SelectionMode
     */
    public final ObjectProperty<SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /**
     * Sets the value of {@link #selectionModeProperty()}.
     *
     * @param mode the selection mode (single, multiple)
     */
    public final void setSelectionMode(SelectionMode mode) {
        requireNonNull(mode);
        selectionModeProperty().set(mode);
    }

    /**
     * Returns the value of {@link #selectionModeProperty()}.
     *
     * @return the selection mode (single, multiple)
     */
    public final SelectionMode getSelectionMode() {
        return selectionModeProperty().get();
    }

    private final ObservableSet<Entry<?>> selections = FXCollections.observableSet();

    /**
     * Stores the currently selected entries.
     *
     * @return the set of currently selected entries
     */
    public final ObservableSet<Entry<?>> getSelections() {
        return selections;
    }

    /**
     * Adds the given entry to the set of currently selected entries.
     *
     * @param entry the selected entries
     * @see #deselect(Entry)
     * @see #getSelections()
     */
    public final void select(Entry<?> entry) {
        requireNonNull(entry);
        selections.add(entry);
    }

    /**
     * Removes the given entry from the set of currently selected entries.
     *
     * @param entry the selected entries
     * @see #select(Entry)
     * @see #getSelections()
     */
    public final void deselect(Entry<?> entry) {
        requireNonNull(entry);
        selections.remove(entry);
    }

    /**
     * Clears the current selection of entries.
     */
    public final void clearSelection() {
        getSelections().clear();
    }

    private final ObjectProperty<VirtualGrid> virtualGrid = new SimpleObjectProperty<>(this, "virtualGrid", new VirtualGrid(Messages.getString("DateControl.DEFAULT_VIRTUAL_GRID_NAME"), Messages.getString("DateControl.DEFAULT_VIRTUAL_GRID_SHORT_NAME"), ChronoUnit.MINUTES, 15));

    /**
     * A virtual grid used for snapping to invisible grid lines while editing
     * calendar entries. Using a virtual grid makes it easier to edit entries so
     * that they all start at exactly the same time. The default grid is set to
     * "5 Minutes". {@link VirtualGrid#OFF} can be used to completely disable
     * the grid.
     *
     * @return the virtual grid
     */
    public final ObjectProperty<VirtualGrid> virtualGridProperty() {
        return virtualGrid;
    }

    /**
     * Returns the value of {@link #virtualGridProperty()}.
     *
     * @return the currently active grid
     */
    public final VirtualGrid getVirtualGrid() {
        return virtualGridProperty().get();
    }

    /**
     * Sets the value of {@link #virtualGridProperty()}.
     *
     * @param grid the grid
     */
    public final void setVirtualGrid(VirtualGrid grid) {
        requireNonNull(grid);
        virtualGridProperty().set(grid);
    }

    private final ObjectProperty<LocalTime> requestedTime = new SimpleObjectProperty<>(this, "requestedTime", LocalTime.now());

    /**
     * Stores the time that the application wants to show in its date controls
     * when the UI opens up. Most applications will normally set this time to
     * {@link LocalTime#now()}.
     *
     * @return the requested time
     */
    public final ObjectProperty<LocalTime> requestedTimeProperty() {
        return requestedTime;
    }

    /**
     * Sets the value of {@link #requestedTimeProperty()}.
     *
     * @param time the requested time
     */
    public final void setRequestedTime(LocalTime time) {
        requestedTimeProperty().set(time);
    }

    /**
     * Returns the value of {@link #requestedTimeProperty()}.
     *
     * @return the requested time
     */
    public final LocalTime getRequestedTime() {
        return requestedTimeProperty().get();
    }

    /**
     * Supported layout strategies by the {@link DayView}.
     */
    public enum Layout {

        /**
         * The standard layout lays out calendar entries in the most efficient
         * way without distinguishing between different calendars. This is the
         * layout found in most calendar software.
         */
        STANDARD,

        /**
         * The swimlane layout creates virtual columns, one for each calendar.
         * The entries of the calendars are shown in their own column. This
         * layout strategy is often found in resource booking systems (e.g. one
         * calendar per room / per person / per truck).
         */
        SWIMLANE
    }

    private final ObjectProperty<Layout> layout = new SimpleObjectProperty<>(this, "layout", Layout.STANDARD);

    /**
     * Stores the strategy used by the view to lay out the entries of several
     * calendars at once. The standard layout ignores the source calendar of an
     * entry and finds the next available place in the UI that satisfies the
     * time bounds of the entry. The {@link Layout#SWIMLANE} strategy allocates
     * a separate column for each calendar and resolves overlapping entry
     * conflicts within that column. Swim lanes are especially useful for
     * resource booking systems (rooms, people, trucks).
     *
     * @return the layout strategy of the view
     */
    public final ObjectProperty<Layout> layoutProperty() {
        return layout;
    }

    /**
     * Sets the value of {@link #layoutProperty()}.
     *
     * @param layout the layout
     */
    public final void setLayout(Layout layout) {
        requireNonNull(layout);
        layoutProperty().set(layout);
    }

    /**
     * Returns the value of {@link #layoutProperty()}.
     *
     * @return the layout strategy
     */
    public final Layout getLayout() {
        return layoutProperty().get();
    }

    private final ObservableSet<DayOfWeek> weekendDays = FXCollections.observableSet();

    /**
     * Returns the days of the week that are considered to be weekend days, for
     * example Saturday and Sunday, or Friday and Saturday.
     *
     * @return the weekend days
     */
    public ObservableSet<DayOfWeek> getWeekendDays() {
        return weekendDays;
    }

    /**
     * Makes the control go forward in time by adding one or more days to the
     * current date. Subclasses override this method to adjust it to their
     * needs, e.g. the {@link DetailedWeekView} adds the number of days found in
     * {@link DetailedWeekView#getNumberOfDays()}.
     *
     * @see #dateProperty()
     */
    public void goForward() {
        setDate(getDate().plusDays(1));
    }

    /**
     * Makes the control go forward in time by removing one or more days from
     * the current date. Subclasses override this method to adjust it to their
     * needs, e.g. the {@link DetailedWeekView} removes the number of days found in
     * {@link DetailedWeekView#getNumberOfDays()}.
     *
     * @see #dateProperty()
     */
    public void goBack() {
        setDate(getDate().minusDays(1));
    }

    /**
     * Makes the control go to "today".
     *
     * @see #dateProperty()
     * @see #todayProperty()
     */
    public void goToday() {
        setDate(getToday());
    }

    /**
     * Finds the first view that represents the given entry.
     *
     * @param entry the entry
     * @return the view
     */
    public final EntryViewBase<?> findEntryView(Entry<?> entry) {
        requireNonNull(entry);
        return doFindEntryView(this, entry);
    }

    private EntryViewBase<?> doFindEntryView(Parent parent, Entry<?> entry) {
        EntryViewBase<?> result = null;

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof EntryViewBase) {
                EntryViewBase<?> base = (EntryViewBase<?>) node;
                if (base.getEntry().equals(entry)) {
                    result = base;
                    break;
                }
            } else if (node instanceof Parent) {
                result = doFindEntryView((Parent) node, entry);
                if (result != null) {
                    break;
                }
            }
        }

        return result;
    }

    private final WeakList<DateControl> boundDateControls = new WeakList<>();

    /**
     * Returns all data controls that are bound to this control.
     *
     * @return the bound date controls / sub controls / children controls
     */
    public final WeakList<DateControl> getBoundDateControls() {
        return boundDateControls;
    }

    /**
     * Unbinds all bound date controls.
     *
     * @see #bind(DateControl, boolean)
     * @see #unbind(DateControl)
     */
    public final void unbindAll() {
        WeakList<DateControl> controls = getBoundDateControls();
        for (DateControl next : controls) {
            unbind(next);
        }
    }

    // hyperlink support
    private final BooleanProperty enableHyperlinks = new SimpleBooleanProperty(this, "enableHyperlinks", true);

    /**
     * A property used to control whether the control allows the user to click on it or an element
     * inside it in order to "jump" to another screen with more detail. Example: in the {@link CalendarView}
     * the user can click on the "day of month" label of a cell inside the {@link MonthSheetView} in
     * order to switch to the {@link DayPage} where the user will see all entries scheduled for that day.
     *
     * @return true if the support for hyperlinks is enabled
     */
    public final BooleanProperty enableHyperlinksProperty() {
        return enableHyperlinks;
    }

    /**
     * Sets the value of the {@link #enableHyperlinksProperty()}.
     *
     * @param enable if true the hyperlink support will be enabled
     */
    public final void setEnableHyperlinks(boolean enable) {
        this.enableHyperlinks.set(enable);
    }

    /**
     * Returns the value of the {@link #enableHyperlinksProperty()}.
     *
     * @return true if the hyperlink support is enabled
     */
    public final boolean isEnableHyperlinks() {
        return enableHyperlinks.get();
    }

    private final BooleanProperty editAvailability = new SimpleBooleanProperty(this, "editAvailability");

    public boolean isEditAvailability() {
        return editAvailability.get();
    }

    /**
     * A flag used to signal whether the user is allowed to modify the availability calendar. If he is
     * allowed then the user can click on "time slots" based on the {@link #availabilityGridProperty()}.
     *
     * @return true if the user can edit availability
     */
    public BooleanProperty editAvailabilityProperty() {
        return editAvailability;
    }

    public void setEditAvailability(boolean editAvailability) {
        this.editAvailability.set(editAvailability);
    }

    private final ObjectProperty<Calendar> availabilityCalendar = new SimpleObjectProperty<>(this, "availabilityCalendar", new Calendar());

    public final Calendar getAvailabilityCalendar() {
        return availabilityCalendar.get();
    }

    /**
     * A background calendar used to display "availability" of a resource.
     *
     * @return the background calendar used for this view
     */
    public final ObjectProperty<Calendar> availabilityCalendarProperty() {
        return availabilityCalendar;
    }

    public final void setAvailabilityCalendar(Calendar calendar) {
        this.availabilityCalendar.set(calendar);
    }

    private final ObjectProperty<VirtualGrid> availabilityGrid = new SimpleObjectProperty<>(this, "availabilityGrid", new VirtualGrid("30 Minutes", "30 Minutes", ChronoUnit.MINUTES, 30));

    public final VirtualGrid getAvailabilityGrid() {
        return availabilityGrid.get();
    }

    /**
     * Defines a virtual grid to be used for working with the availability calendar.
     *
     * @return the availability calendar grid size
     * @see #availabilityCalendarProperty()
     */
    public final ObjectProperty<VirtualGrid> availabilityGridProperty() {
        return availabilityGrid;
    }

    public final void setAvailabilityGrid(VirtualGrid availabilityGrid) {
        this.availabilityGrid.set(availabilityGrid);
    }

    private final ObjectProperty<Paint> availabilityFill = new SimpleObjectProperty<>(this, "availabilityFill", Color.rgb(0, 0, 0, .1));

    public final Paint getAvailabilityFill() {
        return availabilityFill.get();
    }

    /**
     * The color used for drawing the availability background information behind the calendar
     * entries.
     *
     * @return the color used for filling "unavailable" time slots
     */
    public final ObjectProperty<Paint> availabilityFillProperty() {
        return availabilityFill;
    }

    public final void setAvailabilityFill(Paint availabilityFill) {
        this.availabilityFill.set(availabilityFill);
    }

    private final BooleanProperty showDetailsUponEntryCreation = new SimpleBooleanProperty(this, "showDetailsUponEntryCreation", true);

    public final boolean isShowDetailsUponEntryCreation() {
        return showDetailsUponEntryCreation.get();
    }

    /**
     * Determines if the {@link #entryDetailsCallbackProperty()} will be used to display
     * a dialog for a newly added calendar entry.
     *
     * @return true if the date control shows a dialog immediately after a new entry was added by the user
     */
    public final BooleanProperty showDetailsUponEntryCreationProperty() {
        return showDetailsUponEntryCreation;
    }

    public final void setShowDetailsUponEntryCreation(boolean showDetailsUponEntryCreation) {
        this.showDetailsUponEntryCreation.set(showDetailsUponEntryCreation);
    }

    /**
     * Binds several properties of the given date control to the same properties
     * of this control. This kind of binding is needed to create UIs with nested
     * date controls. The {@link CalendarView} for example consists of several
     * pages. Each page is a date control that consists of several other date
     * controls. The {@link DayPage} consists of the {@link AgendaView}, a
     * single {@link DayView}, and a {@link YearMonthView} . All of these
     * controls are bound to each other so that the application can simply
     * change properties on the {@link CalendarView} without worrying about the
     * nested controls.
     *
     * @param otherControl the control that will be bound to this control
     * @param bindDate     determines if the date property will also be bound
     */
    public final void bind(DateControl otherControl, boolean bindDate) {
        requireNonNull(otherControl);
        boundDateControls.add(otherControl);

        // bind collections
        Bindings.bindContentBidirectional(otherControl.getCalendarVisibilityMap(), getCalendarVisibilityMap());
        Bindings.bindContentBidirectional(otherControl.getCalendarSources(), getCalendarSources());
        Bindings.bindContentBidirectional(otherControl.getSelections(), getSelections());
        Bindings.bindContentBidirectional(otherControl.getWeekendDays(), getWeekendDays());
        Bindings.bindContentBidirectional(otherControl.getAvailableZoneIds(), getAvailableZoneIds());

        // bind properties
        Bindings.bindBidirectional(otherControl.suspendUpdatesProperty(), suspendUpdatesProperty());
        Bindings.bindBidirectional(otherControl.entryFactoryProperty(), entryFactoryProperty());
        Bindings.bindBidirectional(otherControl.defaultCalendarProviderProperty(), defaultCalendarProviderProperty());
        Bindings.bindBidirectional(otherControl.virtualGridProperty(), virtualGridProperty());
        Bindings.bindBidirectional(otherControl.draggedEntryProperty(), draggedEntryProperty());
        Bindings.bindBidirectional(otherControl.requestedTimeProperty(), requestedTimeProperty());

        Bindings.bindBidirectional(otherControl.selectionModeProperty(), selectionModeProperty());
        Bindings.bindBidirectional(otherControl.weekFieldsProperty(), weekFieldsProperty());
        Bindings.bindBidirectional(otherControl.layoutProperty(), layoutProperty());
        if (bindDate) {
            Bindings.bindBidirectional(otherControl.dateProperty(), dateProperty());
        }

        Bindings.bindBidirectional(otherControl.todayProperty(), todayProperty());
        Bindings.bindBidirectional(otherControl.zoneIdProperty(), zoneIdProperty());

        Bindings.bindBidirectional(otherControl.startTimeProperty(), startTimeProperty());
        Bindings.bindBidirectional(otherControl.endTimeProperty(), endTimeProperty());
        Bindings.bindBidirectional(otherControl.timeProperty(), timeProperty());
        Bindings.bindBidirectional(otherControl.usagePolicyProperty(), usagePolicyProperty());
        Bindings.bindBidirectional(otherControl.enableTimeZoneSupportProperty(), enableTimeZoneSupportProperty());
        Bindings.bindBidirectional(otherControl.showDetailsUponEntryCreationProperty(), showDetailsUponEntryCreationProperty());
        Bindings.bindBidirectional(otherControl.showNoonMarkerProperty(), showNoonMarkerProperty());
        Bindings.bindBidirectional(otherControl.showTodayProperty(), showTodayProperty());

        Bindings.bindBidirectional(otherControl.editAvailabilityProperty(), editAvailabilityProperty());
        Bindings.bindBidirectional(otherControl.availabilityCalendarProperty(), availabilityCalendarProperty());
        Bindings.bindBidirectional(otherControl.availabilityGridProperty(), availabilityGridProperty());
        Bindings.bindBidirectional(otherControl.availabilityFillProperty(), availabilityFillProperty());
        Bindings.bindBidirectional(otherControl.createEntryClickCountProperty(), createEntryClickCountProperty());

        // bind callbacks
        Bindings.bindBidirectional(otherControl.entryDetailsCallbackProperty(), entryDetailsCallbackProperty());
        Bindings.bindBidirectional(otherControl.dateDetailsCallbackProperty(), dateDetailsCallbackProperty());
        Bindings.bindBidirectional(otherControl.contextMenuCallbackProperty(), contextMenuCallbackProperty());
        Bindings.bindBidirectional(otherControl.entryContextMenuCallbackProperty(), entryContextMenuCallbackProperty());
        Bindings.bindBidirectional(otherControl.calendarSourceFactoryProperty(), calendarSourceFactoryProperty());
        Bindings.bindBidirectional(otherControl.entryDetailsPopOverContentCallbackProperty(), entryDetailsPopOverContentCallbackProperty());
        Bindings.bindBidirectional(otherControl.entryEditPolicyProperty(), entryEditPolicyProperty());
    }

    /**
     * Unbinds the given control from this control. Unbinding is done for all
     * properties and observable lists that have previously been bound by the
     * {@link #bind(DateControl, boolean)} method.
     *
     * @param otherControl the control to unbind
     */
    public final void unbind(DateControl otherControl) {
        requireNonNull(otherControl);

        // unbind collections
        Bindings.unbindContentBidirectional(otherControl.getCalendarVisibilityMap(), getCalendarVisibilityMap());
        Bindings.unbindContentBidirectional(otherControl.getCalendarSources(), getCalendarSources());
        Bindings.unbindContentBidirectional(otherControl.getSelections(), getSelections());
        Bindings.unbindContentBidirectional(otherControl.getWeekendDays(), getWeekendDays());
        Bindings.unbindContentBidirectional(otherControl.getAvailableZoneIds(), getAvailableZoneIds());

        // unbind properties
        Bindings.unbindBidirectional(otherControl.suspendUpdatesProperty(), suspendUpdatesProperty());
        Bindings.unbindBidirectional(otherControl.entryFactoryProperty(), entryFactoryProperty());
        Bindings.unbindBidirectional(otherControl.defaultCalendarProviderProperty(), defaultCalendarProviderProperty());
        Bindings.unbindBidirectional(otherControl.virtualGridProperty(), virtualGridProperty());
        Bindings.unbindBidirectional(otherControl.draggedEntryProperty(), draggedEntryProperty());
        Bindings.unbindBidirectional(otherControl.requestedTimeProperty(), requestedTimeProperty());

        Bindings.unbindBidirectional(otherControl.selectionModeProperty(), selectionModeProperty());
        Bindings.unbindBidirectional(otherControl.weekFieldsProperty(), weekFieldsProperty());
        Bindings.unbindBidirectional(otherControl.layoutProperty(), layoutProperty());
        Bindings.unbindBidirectional(otherControl.dateProperty(), dateProperty());

        Bindings.unbindBidirectional(otherControl.todayProperty(), todayProperty());
        Bindings.unbindBidirectional(otherControl.zoneIdProperty(), zoneIdProperty());

        Bindings.unbindBidirectional(otherControl.startTimeProperty(), startTimeProperty());
        Bindings.unbindBidirectional(otherControl.endTimeProperty(), endTimeProperty());
        Bindings.unbindBidirectional(otherControl.timeProperty(), timeProperty());
        Bindings.unbindBidirectional(otherControl.usagePolicyProperty(), usagePolicyProperty());
        Bindings.unbindBidirectional(otherControl.enableTimeZoneSupportProperty(), enableTimeZoneSupportProperty());
        Bindings.unbindBidirectional(otherControl.showDetailsUponEntryCreationProperty(), showDetailsUponEntryCreationProperty());
        Bindings.unbindBidirectional(otherControl.showNoonMarkerProperty(), showNoonMarkerProperty());
        Bindings.unbindBidirectional(otherControl.showTodayProperty(), showTodayProperty());

        Bindings.unbindBidirectional(otherControl.editAvailabilityProperty(), editAvailabilityProperty());
        Bindings.unbindBidirectional(otherControl.availabilityCalendarProperty(), availabilityCalendarProperty());
        Bindings.unbindBidirectional(otherControl.availabilityGridProperty(), availabilityGridProperty());
        Bindings.unbindBidirectional(otherControl.availabilityFillProperty(), availabilityFillProperty());
        Bindings.unbindBidirectional(otherControl.createEntryClickCountProperty(), createEntryClickCountProperty());

        // unbind callbacks
        Bindings.unbindBidirectional(otherControl.entryDetailsCallbackProperty(), entryDetailsCallbackProperty());
        Bindings.unbindBidirectional(otherControl.dateDetailsCallbackProperty(), dateDetailsCallbackProperty());
        Bindings.unbindBidirectional(otherControl.contextMenuCallbackProperty(), contextMenuCallbackProperty());
        Bindings.unbindBidirectional(otherControl.entryContextMenuCallbackProperty(), entryContextMenuCallbackProperty());
        Bindings.unbindBidirectional(otherControl.calendarSourceFactoryProperty(), calendarSourceFactoryProperty());
        Bindings.unbindBidirectional(otherControl.entryDetailsPopOverContentCallbackProperty(), entryDetailsPopOverContentCallbackProperty());
        Bindings.unbindBidirectional(otherControl.entryEditPolicyProperty(), entryEditPolicyProperty());
    }

    private final BooleanProperty suspendUpdates = new SimpleBooleanProperty(this, "suspendUpdates", false);

    /**
     * A property that will suspend all updates to the view based on model changes. This feature
     * comes in handy when performing large batch updates with many adds and / or removes of calendar
     * entries. When this property is set to true the view will not add / remove / update any entry
     * views. Once it is set back to false a single refresh will be executed.
     *
     * @return true if updates are suspended
     */
    public final BooleanProperty suspendUpdatesProperty() {
        return suspendUpdates;
    }

    /**
     * Returns the value of {@link #suspendUpdatesProperty()}.
     *
     * @return true if updates are suspended
     */
    public final boolean isSuspendUpdates() {
        return suspendUpdates.get();
    }

    /**
     * Sets the value of {@link #suspendUpdatesProperty()}.
     *
     * @param suspend if true updates are suspended
     */
    public final void setSuspendUpdates(boolean suspend) {
        this.suspendUpdates.set(suspend);
    }

    // usage policy support

    public enum Usage {
        NONE,
        VERY_LOW,
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }

    public final ObjectProperty<Callback<Integer, Usage>> usagePolicy = new SimpleObjectProperty<>(this, "usagePolicy");

    /**
     * A property used to store a policy that will be used to determine if a given number of entries
     * indicates a low or high usage of that day. This policy is used by the {@link YearMonthView} to color
     * the background of each day based on the "usage" of that day. The default implementation of this policy
     * returns "none" for 0 entries, "very low" for 1 entry, "low" for 2 entries, "medium" for 3 entries,
     * "high" for 4 entries, and "very high" for 5 entries or more.
     *
     * @return the usage policy
     */
    public final ObjectProperty<Callback<Integer, Usage>> usagePolicyProperty() {
        return usagePolicy;
    }

    /**
     * Sets the value of {@link #usagePolicyProperty()}.
     *
     * @param policy the new usage policy
     */
    public final void setUsagePolicy(Callback<Integer, Usage> policy) {
        Objects.requireNonNull(policy);
        this.usagePolicy.set(policy);
    }

    /**
     * Returns the value of {@link #usagePolicyProperty()}.
     *
     * @return the new usage policy
     */
    public final Callback<Integer, Usage> getUsagePolicy() {
        return usagePolicy.get();
    }

    private final ObservableList<ZoneId> availableZoneIds = FXCollections.observableArrayList();

    /**
     * A list of time zones / time zone IDs that will be available to the user
     * in the date controls. This list is pre-populated with a small subset of
     * all available Zone IDs.
     *
     * @return the list of available time zone IDs
     */
    public final ObservableList<ZoneId> getAvailableZoneIds() {
        return availableZoneIds;
    }

    private final IntegerProperty createEntryClickCount = new SimpleIntegerProperty(this, "createEntryClickCount", 2);

    public int getCreateEntryClickCount() {
        return createEntryClickCount.get();
    }

    /**
     * An integer that determines how many times the user has to perform a primary
     * mouse button click in order to create a new entry.
     *
     * @return the number of mouse clicks required for creating a new entry
     */
    public IntegerProperty createEntryClickCountProperty() {
        return createEntryClickCount;
    }

    public void setCreateEntryClickCount(int createEntryClickCount) {
        this.createEntryClickCount.set(createEntryClickCount);
    }

    private static final String DATE_CONTROL_CATEGORY = "Date Control";

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(dateProperty());
            }

            @Override
            public void setValue(Object value) {
                setDate((LocalDate) value);
            }

            @Override
            public Object getValue() {
                return getDate();
            }

            @Override
            public Class<?> getType() {
                return LocalDate.class;
            }

            @Override
            public String getName() {
                return "Date";
            }

            @Override
            public String getDescription() {
                return "Date";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(layoutProperty());
            }

            @Override
            public void setValue(Object value) {
                setLayout((Layout) value);
            }

            @Override
            public Object getValue() {
                return getLayout();
            }

            @Override
            public Class<?> getType() {
                return Layout.class;
            }

            @Override
            public String getName() {
                return "Layout";
            }

            @Override
            public String getDescription() {
                return "Layout";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(selectionModeProperty());
            }

            @Override
            public void setValue(Object value) {
                setSelectionMode((SelectionMode) value);
            }

            @Override
            public Object getValue() {
                return getSelectionMode();
            }

            @Override
            public Class<?> getType() {
                return SelectionMode.class;
            }

            @Override
            public String getName() {
                return "Selection Mode";
            }

            @Override
            public String getDescription() {
                return "Selection Mode";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(todayProperty());
            }

            @Override
            public void setValue(Object value) {
                setToday((LocalDate) value);
            }

            @Override
            public Object getValue() {
                return getToday();
            }

            @Override
            public Class<?> getType() {
                return LocalDate.class;
            }

            @Override
            public String getName() {
                return "Today";
            }

            @Override
            public String getDescription() {
                return "Today";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showTodayProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowToday((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowToday();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Today";
            }

            @Override
            public String getDescription() {
                return "Highlight today";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(zoneIdProperty());
            }

            @Override
            public void setValue(Object value) {
                setZoneId((ZoneId) value);
            }

            @Override
            public Object getValue() {
                return getZoneId();
            }

            @Override
            public Class<?> getType() {
                return ZoneId.class;
            }

            @Override
            public String getName() {
                return "Timezone";
            }

            @Override
            public String getDescription() {
                return "Timezone";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(weekFieldsProperty());
            }

            @Override
            public void setValue(Object value) {
                setWeekFields((WeekFields) value);
            }

            @Override
            public Object getValue() {
                return getWeekFields();
            }

            @Override
            public Class<?> getType() {
                return WeekFields.class;
            }

            @Override
            public String getName() {
                return "Week Fields";
            }

            @Override
            public String getDescription() {
                return "Week Fields (calendar standard)";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(timeProperty());
            }

            @Override
            public void setValue(Object value) {
                setTime((LocalTime) value);
            }

            @Override
            public Object getValue() {
                return getTime();
            }

            @Override
            public Class<?> getType() {
                return LocalTime.class;
            }

            @Override
            public String getName() {
                return "Time";
            }

            @Override
            public String getDescription() {
                return "Time";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(startTimeProperty());
            }

            @Override
            public void setValue(Object value) {
                setStartTime((LocalTime) value);
            }

            @Override
            public Object getValue() {
                return getStartTime();
            }

            @Override
            public Class<?> getType() {
                return LocalTime.class;
            }

            @Override
            public String getName() {
                return "Start Time";
            }

            @Override
            public String getDescription() {
                return "The first visible time at the top.";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(endTimeProperty());
            }

            @Override
            public void setValue(Object value) {
                setEndTime((LocalTime) value);
            }

            @Override
            public Object getValue() {
                return getEndTime();
            }

            @Override
            public Class<?> getType() {
                return LocalTime.class;
            }

            @Override
            public String getName() {
                return "End Time";
            }

            @Override
            public String getDescription() {
                return "The last visible time at the bottom.";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(enableHyperlinksProperty());
            }

            @Override
            public void setValue(Object value) {
                setEnableHyperlinks((boolean) value);
            }

            @Override
            public Object getValue() {
                return isEnableHyperlinks();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Hyperlinks";
            }

            @Override
            public String getDescription() {
                return "Hyperlinks enabled / disabled";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {
            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.empty();
            }

            @Override
            public void setValue(Object value) {
                if ((Boolean) value) {
                    visibleLayersProperty().add(Layer.BASE);
                } else {
                    visibleLayersProperty().remove(Layer.BASE);
                }
            }

            @Override
            public Object getValue() {
                return visibleLayersProperty().contains(Layer.BASE);
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Base Layer";
            }

            @Override
            public String getDescription() {
                return "Base Layer visible / hidden";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {
            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.empty();
            }

            @Override
            public void setValue(Object value) {
                if ((Boolean) value) {
                    visibleLayersProperty().add(Layer.TOP);
                } else {
                    visibleLayersProperty().remove(Layer.TOP);
                }
            }

            @Override
            public Object getValue() {
                return visibleLayersProperty().contains(Layer.TOP);
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Top Layer";
            }

            @Override
            public String getDescription() {
                return "Top Layer visible / hidden";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showNoonMarkerProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowNoonMarker((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowNoonMarker();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Show Noon Marker";
            }

            @Override
            public String getDescription() {
                return "Show / hide markers for noon.";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(editAvailabilityProperty());
            }

            @Override
            public void setValue(Object value) {
                setEditAvailability((boolean) value);
            }

            @Override
            public Object getValue() {
                return isEditAvailability();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Edit Availability";
            }

            @Override
            public String getDescription() {
                return "Allow editing of the availability calendar";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(enableTimeZoneSupportProperty());
            }

            @Override
            public void setValue(Object value) {
                setEnableTimeZoneSupport((boolean) value);
            }

            @Override
            public Object getValue() {
                return isEnableTimeZoneSupport();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Enable Timezone Support";
            }

            @Override
            public String getDescription() {
                return "Provide UI controls to work with timezones.";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showDetailsUponEntryCreationProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowDetailsUponEntryCreation((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowDetailsUponEntryCreation();
            }

            @Override
            public Class<?> getType() {
                return boolean.class;
            }

            @Override
            public String getName() {
                return "Show Details upon Creation";
            }

            @Override
            public String getDescription() {
                return "Show the details dialog immediately after creating a new entry";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(createEntryClickCountProperty());
            }

            @Override
            public void setValue(Object value) {
                setCreateEntryClickCount((Integer) value);
            }

            @Override
            public Object getValue() {
                return getCreateEntryClickCount();
            }

            @Override
            public Class<?> getType() {
                return Integer.class;
            }

            @Override
            public String getName() {
                return "Create Entry Click Count";
            }

            @Override
            public String getDescription() {
                return "Set the number of clicks needed to create a new entry.";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(availabilityFillProperty());
            }

            @Override
            public void setValue(Object value) {
                setAvailabilityFill((Paint) value);
            }

            @Override
            public Object getValue() {
                return getAvailabilityFill();
            }

            @Override
            public Class<?> getType() {
                return Paint.class;
            }

            @Override
            public String getName() {
                return "Availability Fill Color";
            }

            @Override
            public String getDescription() {
                return "Availability Fill Color";
            }

            @Override
            public String getCategory() {
                return DATE_CONTROL_CATEGORY;
            }
        });

        return items;
    }
}
