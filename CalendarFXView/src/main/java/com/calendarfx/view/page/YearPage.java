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

import com.calendarfx.view.Messages;
import com.calendarfx.view.MonthSheetView;
import com.calendarfx.view.MonthSheetView.ClickBehaviour;
import com.calendarfx.view.YearView;
import com.calendarfx.view.print.ViewType;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import impl.com.calendarfx.view.page.YearPageSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import org.controlsfx.control.PropertySheet;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * A composite view focused on displaying calendar information for a single
 * year. The view consists of the page "chrome" inherited from the superclass
 * and a view of type {@link YearView}. Alternatively the page can
 * also display months in columns via the {@link MonthSheetView} control. The
 * application can switch between these two views by calling {@link #setDisplayMode(DisplayMode)}.
 *
 * <h3>YearView</h3>
 * <center><img width="100%" src="doc-files/year-page.png"></center>
 * <h3>MonthSheetView</h3>
 * <center><img width="100%" src="doc-files/year-page-2.png"></center>
 */
public class YearPage extends PageBase {

    private YearView yearView;
    private MonthSheetView monthSheetView;
    private ToggleButton displayModeButton;

    /**
     * Constructs a new year page.
     */
    public YearPage() {
        getStyleClass().add("year-page"); //$NON-NLS-1$

        this.yearView = new YearView();

        this.monthSheetView = new MonthSheetView();
        this.monthSheetView.setCellFactory(param -> new MonthSheetView.DetailedDateCell(param.getView(), param.getDate()));
        this.monthSheetView.setClickBehaviour(ClickBehaviour.SHOW_DETAILS);

        bind(yearView, true);
        bind(monthSheetView, true);

        Bindings.bindBidirectional(monthSheetView.showTodayProperty(), showTodayProperty());

        setDateTimeFormatter(DateTimeFormatter.ofPattern(Messages.getString("YearPage.DATE_FORMAT"))); //$NON-NLS-1$

        displayModeProperty().addListener(it -> updateDisplayModeIcon());

        displayModeButton = new ToggleButton();
        displayModeButton.setId("display-mode-button");
        displayModeButton.setTooltip(new Tooltip(Messages.getString("YearPage.TOOLTIP_DISPLAY_MODE")));
        displayModeButton.setSelected(getDisplayMode().equals(DisplayMode.COLUMNS));
        displayModeButton.selectedProperty().addListener(it -> {
            if (displayModeButton.isSelected()) {
                setDisplayMode(DisplayMode.COLUMNS);
            } else {
                setDisplayMode(DisplayMode.GRID);
            }
        });

        displayModeProperty().addListener(it -> displayModeButton.setSelected(getDisplayMode().equals(DisplayMode.COLUMNS)));

        updateDisplayModeIcon();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearPageSkin(this);
    }

    @Override
    public Node getToolBarControls() {
        return displayModeButton;
    }

    /*
     * Sets the graphic node on the display mode button based on the current display
     * mode (column or grid).
     */
    private void updateDisplayModeIcon() {
        FontAwesomeIcon icon = FontAwesomeIcon.CALENDAR;
        if (getDisplayMode().equals(DisplayMode.GRID)) {
            icon = FontAwesomeIcon.CALENDAR_ALT;
        }

        final Text graphic = FontAwesomeIconFactory.get().createIcon(icon);
        graphic.getStyleClass().addAll("button-icon", "display-mode-icon");
        displayModeButton.setGraphic(graphic);
    }

    /**
     * An enum used for setting the display mode of the {@link YearPage}. The
     * page can display the 12 months of a year in a grid or a column layout.
     *
     * @see #displayModeProperty()
     */
    public enum DisplayMode {
        GRID,
        COLUMNS
    }

    private final ObjectProperty<DisplayMode> displayMode = new SimpleObjectProperty<>(this, "displayMode", DisplayMode.GRID);

    /**
     * A property used to control whether the page should display the year in a grid format (with
     * each cell containing a single month) or in a column format (each column representing one
     * month).
     *
     * @return the display mode
     */
    public final ObjectProperty<DisplayMode> displayModeProperty() {
        return displayMode;
    }

    /**
     * Returns the value of {@link #displayModeProperty()}.
     *
     * @return the current display mode
     */
    public final DisplayMode getDisplayMode() {
        return displayMode.get();
    }

    /**
     * Sets the value of {@link #displayModeProperty()}.
     *
     * @param mode the display mode
     */
    public final void setDisplayMode(DisplayMode mode) {
        this.displayMode.set(mode);
    }

    /**
     * Returns the {@link MonthSheetView} used by the page to display months
     * in columns.
     *
     * @return the month sheet view
     */
    public final MonthSheetView getMonthSheetView() {
        return monthSheetView;
    }


    /**
     * Returns the {@link YearView} used by the page to display months
     * in a 4x3 grid layout.
     *
     * @return the year view
     */
    public final YearView getYearView() {
        return yearView;
    }

    @Override
    public final void goForward() {
        setDate(getDate().plusYears(1));
    }

    @Override
    public final void goBack() {
        setDate(getDate().minusYears(1));
    }

    @Override
    public final ViewType getPrintViewType() {
        /*
        We currently do not support printing years, hence we return MONTH_VIEW.
         */
        return ViewType.MONTH_VIEW;
    }

    private final String YEAR_PAGE_CATEGORY = "Year Page"; //$NON-NLS-1$

    @Override
    public ObservableList<PropertySheet.Item> getPropertySheetItems() {
        ObservableList<PropertySheet.Item> items = super.getPropertySheetItems();

        items.add(new PropertySheet.Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(displayModeProperty());
            }

            @Override
            public void setValue(Object value) {
                setDisplayMode((DisplayMode) value);
            }

            @Override
            public Object getValue() {
                return getDisplayMode();
            }

            @Override
            public Class<?> getType() {
                return DisplayMode.class;
            }

            @Override
            public String getName() {
                return "Display Mode"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Grid or Column Layout"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return YEAR_PAGE_CATEGORY;
            }
        });

        return items;
    }
}
