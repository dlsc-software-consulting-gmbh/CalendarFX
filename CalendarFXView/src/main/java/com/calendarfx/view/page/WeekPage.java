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

import com.calendarfx.view.AllDayView;
import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.Messages;
import com.calendarfx.view.WeekDayView;
import com.calendarfx.view.WeekTimeScaleView;
import com.calendarfx.view.print.ViewType;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import impl.com.calendarfx.view.page.WeekPageSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.controlsfx.control.PropertySheet;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * A composite view focused on displaying calendar information for several days
 * in a row, normally a week. The view consists of the page "chrome" inherited
 * from the superclass, a {@link WeekDayView} for each day, an
 * {@link AllDayView}, and a {@link WeekTimeScaleView}.
 * <p/>
 * <center><img width="100%" src="doc-files/week-page.png"></center>
 */
public class WeekPage extends PageBase {

    private final DetailedWeekView detailedWeekView;

    private final HBox toolBarControls = new HBox();

    /**
     * Constructs a new week page.
     */
    public WeekPage() {
        getStyleClass().add("week-page"); //$NON-NLS-1$
        setDateTimeFormatter(DateTimeFormatter.ofPattern(Messages.getString("WeekPage.DATE_FORMAT"))); //$NON-NLS-1$

        this.detailedWeekView = new DetailedWeekView();

        ToggleButton layoutButton = new ToggleButton();
        layoutButton.setTooltip(new Tooltip(Messages.getString("WeekPage.TOOLTIP_LAYOUT"))); //$NON-NLS-1$
        layoutButton.setId("layout-button");
        Text layoutIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.TABLE);
        layoutIcon.getStyleClass().addAll("button-icon", "layout-button-icon"); //$NON-NLS-1$ //$NON-NLS-2$
        layoutButton.setGraphic(layoutIcon);
        layoutButton.setSelected(getLayout().equals(Layout.SWIMLANE));
        layoutButton.setOnAction(evt -> {
            if (layoutButton.isSelected()) {
                setLayout(Layout.SWIMLANE);
            } else {
                setLayout(Layout.STANDARD);
            }
        });

        showLayoutButtonProperty().addListener(it -> updateToolBarControls(layoutButton));

        updateToolBarControls(layoutButton);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WeekPageSkin(this);
    }

    @Override
    public Node getToolBarControls() {
        return toolBarControls;
    }

    private void updateToolBarControls(ToggleButton layoutButton) {
        if (isShowLayoutButton()) {
            toolBarControls.getChildren().setAll(layoutButton);
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
     * Returns the week view child control.
     *
     * @return the week view
     */
    public final DetailedWeekView getDetailedWeekView() {
        return detailedWeekView;
    }

    @Override
    public final void goForward() {
        setDate(detailedWeekView.getStartDate().plusDays(
                Math.max(7, detailedWeekView.getNumberOfDays())));
    }

    @Override
    public final void goBack() {
        setDate(detailedWeekView.getStartDate().minusDays(Math.max(7, detailedWeekView.getNumberOfDays())));
    }

    @Override
    public final ViewType getPrintViewType() {
        return ViewType.WEEK_VIEW;
    }

    private final String WEEK_PAGE_CATEGORY = "Week Page"; //$NON-NLS-1$

    @Override
    public ObservableList<PropertySheet.Item> getPropertySheetItems() {
        ObservableList<PropertySheet.Item> items = super.getPropertySheetItems();

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
                return "Layout Button"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Can the user access the button to toggle the layout or not."; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return WEEK_PAGE_CATEGORY;
            }
        });

        return items;
    }
}
