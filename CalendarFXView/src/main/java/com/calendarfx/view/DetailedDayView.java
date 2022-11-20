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

import com.calendarfx.view.AgendaView.AgendaEntryCell;
import impl.com.calendarfx.view.DetailedDayViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import org.controlsfx.control.PropertySheet.Item;

import java.util.Optional;

/**
 * The detailed day view is a composite control consisting of a {@link DayView},
 * an {@link AllDayView}, an {@link CalendarHeaderView}, and a
 * {@link TimeScaleView}. The image below shows the standard appearance of the
 * view. The second image shows the same view with the optional agenda view made
 * visible.
 *
 * <img src="doc-files/detailed-day-view.png" alt="Detailed Day View">
 * <img src="doc-files/detailed-day-view-agenda.png" alt="Detailed Day View Agenda">
 *
 */
public class DetailedDayView extends DayViewBase {

    private static final String DEFAULT_STYLE = "detailed-day-view";

    private final AllDayView allDayView;
    private final DayView dayView;
    private final TimeScaleView timeScaleView;
    private final CalendarHeaderView calendarHeader;
    private final AgendaView agendaView;

    /**
     * Constructs a new day view.
     */
    public DetailedDayView() {
        allDayView = new AllDayView(1);
        dayView = new DayView();
        timeScaleView = new TimeScaleView();
        calendarHeader = new CalendarHeaderView();

        agendaView = new AgendaView();
        agendaView.setLookBackPeriodInDays(0);
        agendaView.setLookAheadPeriodInDays(30);
        agendaView.setShowStatusLabel(false);
        agendaView.setCellFactory(view -> new AgendaEntryCell(view, false));

        bind(dayView, true);
        bind(timeScaleView, true);
        bind(allDayView, true);
        bind(agendaView, true);

        calendarHeader.bind(this);

        getStyleClass().add(DEFAULT_STYLE);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DetailedDayViewSkin(this);
    }

    /**
     * Returns the all day view sub control.
     * 
     * @return the all day child node
     */
    public final AllDayView getAllDayView() {
        return allDayView;
    }

    /**
     * Returns the day view sub control.
     * 
     * @return the day view child node
     */
    public final DayView getDayView() {
        return dayView;
    }

    /**
     * Returns the time scale sub control.
     * 
     * @return the time scale child node
     */
    public final TimeScaleView getTimeScaleView() {
        return timeScaleView;
    }

    /**
     * Returns the calendar header sub control.
     * 
     * @return the calendar header child node
     */
    public final CalendarHeaderView getCalendarHeaderView() {
        return calendarHeader;
    }

    /**
     * Returns the agenda view sub control.
     * 
     * @return the agenda view child node
     */
    public final AgendaView getAgendaView() {
        return agendaView;
    }

    // show all day view support

    private final BooleanProperty showAllDayView = new SimpleBooleanProperty(
            this, "showAllDayView", true);

    /**
     * A property used to toggle the visibility of the all day view.
     *
     * @return true if the all day view will be visible
     */
    public final BooleanProperty showAllDayViewProperty() {
        return showAllDayView;
    }

    /**
     * Sets the value of {@link #showAllDayViewProperty()}.
     *
     * @return true if the all day view will be visible
     */
   // public final boolean isShowAllDayView() {
       // return showAllDayViewProperty().get();
    //}
    public final boolean isShowAllDayView() {
        return false;
    }

    /**
     * Sets the value of {@link #showAllDayViewProperty()}.
     *
     * @param show
     *            true if the all day view will be visible
     */
    public final void setShowAllDayView(boolean show) {
        showAllDayViewProperty().set(show);
    }

    // show agenda view support

    private final BooleanProperty showAgendaView = new SimpleBooleanProperty(
            this, "showAgendaView", false);

    /**
     * A property used to toggle the visibility of the agenda view.
     *
     * @return true if the agenda view will be visible
     */
    public final BooleanProperty showAgendaViewProperty() {
        return showAgendaView;
    }

    /**
     * Sets the value of {@link #showAgendaViewProperty()}.
     *
     * @return true if the agenda view will be visible
     */
    public final boolean isShowAgendaView() {
        return showAgendaViewProperty().get();
    }

    /**
     * Sets the value of {@link #showAgendaViewProperty()}.
     *
     * @param show
     *            if true the agenda view will be visible
     */
    public final void setShowAgendaView(boolean show) {
        showAgendaViewProperty().set(show);
    }

    // show time scale view support

    private final BooleanProperty showTimeScaleView = new SimpleBooleanProperty(
            this, "showTimeScaleView", true);

    /**
     * A property used to toggle the visibility of the time scale view.
     *
     * @return true if the time scale view will be visible
     */
    public final BooleanProperty showTimeScaleViewProperty() {
        return showTimeScaleView;
    }

    /**
     * Returns the value of {@link #showTimeScaleViewProperty()}.
     *
     * @return true if the time scale view will be visible
     */
    public final boolean isShowTimeScaleView() {
        return showTimeScaleViewProperty().get();
    }

    /**
     * Sets the value of {@link #showTimeScaleViewProperty()}.
     *
     * @param show
     *            if true the time scale view will be visible
     */
    public final void setShowTimeScaleView(boolean show) {
        showTimeScaleViewProperty().set(show);
    }

    // show scrollbar support

    private final BooleanProperty showScrollBar = new SimpleBooleanProperty(
            this, "showScrollBar", true);

    /**
     * A property used to control the visibility of the vertial scrollbar.
     *
     * @return true if the scrollbar should be shown to the user
     */
    public final BooleanProperty showScrollBarProperty() {
        return showScrollBar;
    }

    /**
     * Sets the value of {@link #showScrollBarProperty()}.
     *
     * @param showScrollBar
     *            if true the scrollbar will be visible
     */
    public final void setShowScrollBar(boolean showScrollBar) {
        this.showScrollBar.set(showScrollBar);
    }

    /**
     * Returns the value of {@link #showScrollBarProperty()}.
     *
     * @return true if the scrollbar will be visible
     */
    public final boolean isShowScrollBar() {
        return showScrollBar.get();
    }

    private static final String DETAILED_DAY_VIEW_CATEGORY = "Detailed Day View";

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {
            @Override
            public void setValue(Object value) {
                setShowAllDayView((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowAllDayView();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showAllDayViewProperty());
            }

            @Override
            public String getName() {
                return "Show All Day View";
            }

            @Override
            public String getDescription() {
                return "Show All Day View";
            }

            @Override
            public String getCategory() {
                return DETAILED_DAY_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {
            @Override
            public void setValue(Object value) {
                setShowTimeScaleView((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowTimeScaleView();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showTimeScaleViewProperty());
            }

            @Override
            public String getName() {
                return "Show Time Scale View";
            }

            @Override
            public String getDescription() {
                return "Show Time Scale View";
            }

            @Override
            public String getCategory() {
                return DETAILED_DAY_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {
            @Override
            public void setValue(Object value) {
                setShowAgendaView((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowAgendaView();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showAgendaViewProperty());
            }

            @Override
            public String getName() {
                return "Show Agenda View";
            }

            @Override
            public String getDescription() {
                return "Show Agenda View";
            }

            @Override
            public String getCategory() {
                return DETAILED_DAY_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {
            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getCategory() {
                return DETAILED_DAY_VIEW_CATEGORY;
            }

            @Override
            public String getName() {
                return "Show ScrollBar";
            }

            @Override
            public String getDescription() {
                return "Show ScrollBar";
            }

            @Override
            public Object getValue() {
                return isShowScrollBar();
            }

            @Override
            public void setValue(Object value) {
                setShowScrollBar((boolean) value);
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showScrollBarProperty());
            }
        });

        return items;
    }

}
