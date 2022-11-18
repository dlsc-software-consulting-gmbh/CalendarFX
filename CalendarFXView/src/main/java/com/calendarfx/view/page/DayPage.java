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

package com.calendarfx.view.page;

import com.calendarfx.view.AgendaView;
import com.calendarfx.view.AllDayView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DetailedDayView;
import com.calendarfx.view.Messages;
import com.calendarfx.view.MonthView;
import com.calendarfx.view.TimeScaleView;
import com.calendarfx.view.YearMonthView;
import com.calendarfx.view.print.ViewType;
import impl.com.calendarfx.view.page.DayPageSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.SegmentedButton;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A composite view focused on displaying calendar information for a single day.
 * The view consists of the page "chrome" inherited from the superclass, a
 * {@link DayView}, an {@link AllDayView}, a {@link TimeScaleView}, a
 * {@link YearMonthView} for picking a new date, and an {@link AgendaView}.
 *
 * <img width="100%" src="doc-files/day-page.png" alt="Day Page">
 */
public class DayPage extends PageBase {

    private final AgendaView agendaView;
    private final YearMonthView yearMonthView;
    private final DetailedDayView detailedDayView;
    private final Node toolBarControls;
    private HBox toolbarControls;

    /**
     * Constructs a new day page.
     */
    public DayPage() {
        super();

        getStyleClass().add("day-page");

        setDateTimeFormatter(
                DateTimeFormatter.ofPattern(Messages.getString("DayPage.DATE_FORMATTER")));

        this.agendaView = new AgendaView();
        this.yearMonthView = new YearMonthView();
        this.detailedDayView = new DetailedDayView();
        this.toolBarControls = createToolBarControls();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DayPageSkin(this);
    }

    @Override
    public Node getToolBarControls() {
        return toolBarControls;
    }

    /**
     * Creates the node used for the page-specific part of the calendar
     * view.
     *
     * @return the toolbar controls specific for this page
     */
    protected Node createToolBarControls() {
        FontIcon listIcon = new FontIcon(FontAwesome.LIST);
        listIcon.getStyleClass().addAll("button-icon");

        ToggleButton agendaOnlyButton = new ToggleButton();
        agendaOnlyButton.setMaxHeight(Double.MAX_VALUE);
        agendaOnlyButton.setGraphic(listIcon);
        agendaOnlyButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        FontIcon calendarIcon = new FontIcon(FontAwesome.CALENDAR);
        calendarIcon.getStyleClass().addAll("button-icon");

        ToggleButton dayOnlyButton = new ToggleButton();
        dayOnlyButton.setMaxHeight(Double.MAX_VALUE);
        dayOnlyButton.setGraphic(calendarIcon);
        dayOnlyButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        FontIcon standardIcon = new FontIcon(FontAwesome.COLUMNS);
        standardIcon.getStyleClass().addAll("button-icon");

        ToggleButton standardButton = new ToggleButton();
        standardButton.setMaxHeight(Double.MAX_VALUE);
        standardButton.setGraphic(standardIcon);
        standardButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        agendaOnlyButton.setOnAction(evt -> setDayPageLayout(DayPageLayout.AGENDA_ONLY));
        dayOnlyButton.setOnAction(evt -> setDayPageLayout(DayPageLayout.DAY_ONLY));
        standardButton.setOnAction(evt -> setDayPageLayout(DayPageLayout.STANDARD));

        SegmentedButton segmentedButton = new SegmentedButton(agendaOnlyButton, standardButton, dayOnlyButton);
        segmentedButton.getStyleClass().add("layout-button");
        segmentedButton.visibleProperty().bind(showDayPageLayoutControlsProperty());
        segmentedButton.setMaxHeight(Double.MAX_VALUE);

        updateDayPageLayoutButtons(agendaOnlyButton, dayOnlyButton, standardButton);
        dayPageLayout.addListener((observable, oldValue, newValue) -> updateDayPageLayoutButtons(agendaOnlyButton, dayOnlyButton, standardButton));

        agendaOnlyButton.setTooltip(new Tooltip(Messages.getString("DayPage.TOOLTIP_MAXIMIZE_AGENDA_LIST")));
        dayOnlyButton.setTooltip(new Tooltip(Messages.getString("DayPage.TOOLTIP_MAXIMIZE_DAY_VIEW")));
        standardButton.setTooltip(new Tooltip(Messages.getString("DayPage.TOOLTIP_STANDARD_LAYOUT")));

        FontIcon layoutIcon = new FontIcon(FontAwesome.TABLE);
        layoutIcon.getStyleClass().addAll("button-icon", "layout-button-icon");

        ToggleButton layoutButton = new ToggleButton();
        layoutButton.setMaxHeight(Double.MAX_VALUE);
        layoutButton.setTooltip(new Tooltip(Messages.getString("DayPage.TOOLTIP_LAYOUT")));
        layoutButton.setId("layout-button");
        layoutButton.setGraphic(layoutIcon);
        layoutButton.setSelected(getLayout().equals(Layout.SWIMLANE));
        layoutButton.setOnAction(evt -> {
            if (layoutButton.isSelected()) {
                setLayout(Layout.SWIMLANE);
            } else {
                setLayout(Layout.STANDARD);
            }
        });

        toolbarControls = new HBox();
        toolbarControls.setSpacing(10);

        //updateToolBarControls(segmentedButton, layoutButton);

        showLayoutButtonProperty().addListener(it -> updateToolBarControls(segmentedButton, layoutButton));

        return toolbarControls;
    }

    private void updateDayPageLayoutButtons(ToggleButton agendaOnlyButton,ToggleButton dayOnlyButton,ToggleButton standardButton) {
        switch (dayPageLayout.get()) {
            case AGENDA_ONLY:
                agendaOnlyButton.setSelected(true);
                break;
            case DAY_ONLY:
                dayOnlyButton.setSelected(true);
                break;
            case STANDARD:
                standardButton.setSelected(true);
                break;
            default:
                break;
        }
    }

    private void updateToolBarControls(SegmentedButton segmentedButton, ToggleButton layoutButton) {
        if (isShowLayoutButton()) {
            toolbarControls.getChildren().setAll(layoutButton, segmentedButton);
        } else {
            toolbarControls.getChildren().setAll(segmentedButton);
        }
    }

    private final BooleanProperty showLayoutButton = new SimpleBooleanProperty(this, "showLayoutButton", true);

    /**
     * Controls whether the "layout" button (to toggle between standard and
     * swimlane layout) in the upper left corner of the control will be shown to
     * the user or not.
     *
     * @return true if the "print" button will be accessible by the user
     */
    public final BooleanProperty showLayoutButtonProperty() {
        return showLayoutButton;
    }

    /**
     * Returns the value of {@link #showLayoutButtonProperty()}.
     *
     * @return true if the "layout" button will be accessible by the user
     */
    public final boolean isShowLayoutButton() {
        return showLayoutButton.get();
    }

    /**
     * Sets the value of {@link #showLayoutButtonProperty()}.
     *
     * @param show if true the "layout" button will be accessible by the user
     */
    public final void setShowLayoutButton(boolean show) {
        showLayoutButton.set(show);
    }

    /**
     * An enumerator used for telling the {@link DayPage} view to show more or
     * less content.
     */
    public enum DayPageLayout {

        /**
         * Specifies that the view will display a {@link DayView}, a
         * {@link MonthView}, and an {@link AgendaView}.
         */
        STANDARD,

        /**
         * Only displays an {@link AgendaView}.
         */
        AGENDA_ONLY,

        /**
         * Only displays the {@link DayView}.
         */
        DAY_ONLY
    }

    private final ObjectProperty<DayPageLayout> dayPageLayout = new SimpleObjectProperty<>(
            this, "dayPageLayout", DayPageLayout.STANDARD);

    /**
     * Stores the currently requested layout for the {@link DayPage}. The layout
     * determines if the page will show only the {@link DayView} or a more
     * complext UI with the additional {@link MonthView} for picking a date, and
     * an {@link AgendaView} with the calendar entries for the next couple of
     * weeks.
     *
     * @return the day page layout
     */
    public final ObjectProperty<DayPageLayout> dayPageLayoutProperty() {
        return dayPageLayout;
    }

    /**
     * Sets the value of {@link #dayPageLayoutProperty()}.
     *
     * @param layout
     *            the layout
     */
    public final void setDayPageLayout(DayPageLayout layout) {
        requireNonNull(layout);
        dayPageLayoutProperty().set(layout);
    }

    /**
     * Returns the value of {@link #dayPageLayoutProperty()}.
     *
     * @return the layout
     */
    public final DayPageLayout getDayPageLayout() {
        return dayPageLayoutProperty().get();
    }

    private final BooleanProperty showDayPageLayoutControls = new SimpleBooleanProperty(
            this, "showDayPageLayoutControls", true);

    /**
     * Determines if the controls for switching between different layouts of
     * this view will be shown to the user or not.
     *
     * @return true if the controls will be shown
     */
    public final BooleanProperty showDayPageLayoutControlsProperty() {
        return showDayPageLayoutControls;
    }

    /**
     * Returns the value of {@link #showDayPageLayoutControlsProperty()}.
     *
     * @return true if the controls will be shown
     */
    public final boolean isShowDayPageLayoutControls() {
        return showDayPageLayoutControlsProperty().get();
    }

    /**
     * Sets the value of {@link #showDayPageLayoutControlsProperty()}.
     *
     * @param show
     *            if true the controls will be shown
     */
    public final void setShowDayPageLayoutControls(boolean show) {
        showDayPageLayoutControlsProperty().set(show);
    }

    /**
     * Returns the agenda view child control.
     *
     * @return the agenda view
     */
    public final AgendaView getAgendaView() {
        return agendaView;
    }

    /**
     * Returns the date picker child control.
     *
     * @return the date picker view
     */
    public final YearMonthView getYearMonthView() {
        return yearMonthView;
    }

    /**
     * Returns the day view.
     *
     * @return the day view
     */
    public final DetailedDayView getDetailedDayView() {
        return detailedDayView;
    }

    @Override
    public final void goForward() {
        setDate(getDate().plusDays(1));
    }

    @Override
    public final void goBack() {
        setDate(getDate().minusDays(1));
    }

    @Override
    public final ViewType getPrintViewType() {
        return ViewType.DAY_VIEW;
    }

    private static final String DAY_PAGE_CATEGORY = "Day Page";

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(dayPageLayoutProperty());
            }

            @Override
            public void setValue(Object value) {
                setDayPageLayout((DayPageLayout) value);
            }

            @Override
            public Object getValue() {
                return getDayPageLayout();
            }

            @Override
            public Class<?> getType() {
                return DayPageLayout.class;
            }

            @Override
            public String getName() {
                return "Day Page Layout";
            }

            @Override
            public String getDescription() {
                return "Layout of the day page";
            }

            @Override
            public String getCategory() {
                return DAY_PAGE_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showDayPageLayoutControlsProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowDayPageLayoutControls((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowDayPageLayoutControls();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Day Page Layout Controls";
            }

            @Override
            public String getDescription() {
                return "Show Day Page Layout Controls";
            }

            @Override
            public String getCategory() {
                return DAY_PAGE_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showLayoutButtonProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowLayoutButton((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowLayoutButton();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Layout Button";
            }

            @Override
            public String getDescription() {
                return "Can the user access the button to toggle the layout or not.";
            }

            @Override
            public String getCategory() {
                return DAY_PAGE_CATEGORY;
            }
        });

        return items;
    }
}