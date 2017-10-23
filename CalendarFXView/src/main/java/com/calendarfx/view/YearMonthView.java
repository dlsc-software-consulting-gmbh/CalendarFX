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

import impl.com.calendarfx.view.YearMonthViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet.Item;

import java.time.LocalDate;
import java.util.Optional;

import static com.calendarfx.view.YearMonthView.ClickBehaviour.PERFORM_SELECTION;
import static java.lang.Double.MAX_VALUE;
import static java.util.Objects.requireNonNull;
import static javafx.geometry.Pos.CENTER;

/**
 * Displays a given month in a given year. The view can be configured in many
 * ways:
 * <ul>
 * <li>Show / hide the name of the month</li>
 * <li>Show / hide the year</li>
 * <li>Show / hide arrow buttons for changing the month</li>
 * <li>Show / hide arrow buttons for changing the year</li>
 * <li>Show / hide today</li>
 * <li>Show / hide a button for going to today</li>
 * <li>Show / hide usage colors</li>
 * </ul>
 * Additionally the application can choose from two different behaviours when
 * the user clicks on a date:
 * <ol>
 * <li>Perform a selection / select the date</li>
 * <li>Show details of the date (by default shows a popover with all entries on
 * that date)</li>
 * </ol>
 * The image below shows the visual apperance of this control:
 * <p/>
 * <center><img src="doc-files/date-picker.png"></center>
 * <p/>
 */
public class YearMonthView extends MonthViewBase {

    /**
     * Constructs a new view.
     */
    public YearMonthView() {
        getStyleClass().add("year-month-view"); //$NON-NLS-1$

        setCellFactory(view -> new DateCell());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearMonthViewSkin(this);
    }

    /**
     * The base date cell implementation for month views.
     *
     * @see #setCellFactory(Callback)
     */
    public static class DateCell extends Label {

        private LocalDate date;

        public DateCell() {
            setFocusTraversable(false);
            setMaxSize(MAX_VALUE, MAX_VALUE);
            setAlignment(CENTER);
        }

        public final void setDate(LocalDate date) {
            this.date = date;
            update(date);
        }

        public final LocalDate getDate() {
            return date;
        }

        protected void update(LocalDate date) {
            setText(Integer.toString(date.getDayOfMonth()));
        }
    }

    private final ObjectProperty<Callback<YearMonthView, DateCell>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory"); //$NON-NLS-1$

    /**
     * A factory for creating alternative content for the month view. The image
     * below shows the {@link YearMonthView} once with the default factory and
     * once with an alternative factory that creates checkboxes.
     * <p/>
     * <center><img src="doc-files/month-cell-factory.png"></center>
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<YearMonthView, DateCell>> cellFactoryProperty() {
        return cellFactory;
    }

    /**
     * Sets the value of {@link #cellFactoryProperty()}.
     *
     * @param factory
     *            the cell factory
     */
    public final void setCellFactory(Callback<YearMonthView, DateCell> factory) {
        requireNonNull(factory);
        cellFactoryProperty().set(factory);
    }

    /**
     * Returns the value of {@link #cellFactoryProperty()}.
     *
     * @return the cell factory
     */
    public final Callback<YearMonthView, DateCell> getCellFactory() {
        return cellFactoryProperty().get();
    }

    private final BooleanProperty showMonth = new SimpleBooleanProperty(this,
            "showMonth", true); //$NON-NLS-1$

    /**
     * Show or hide the name of the month.
     *
     * @return true if the month name will be shown
     */
    public final BooleanProperty showMonthProperty() {
        return showMonth;
    }

    /**
     * Sets the value of {@link #showMonthProperty()}.
     *
     * @param show
     *            if true the month will be shown
     */
    public final void setShowMonth(boolean show) {
        showMonthProperty().set(show);
    }

    /**
     * Returns the value of {@link #showMonthProperty()}.
     *
     * @return true if the month name will be shown
     */
    public final boolean isShowMonth() {
        return showMonthProperty().get();
    }

    private final BooleanProperty showYear = new SimpleBooleanProperty(this,
            "showYear", true); //$NON-NLS-1$

    /**
     * Show or hide the year.
     *
     * @return true if the year will be shown
     */
    public final BooleanProperty showYearProperty() {
        return showYear;
    }

    /**
     * Sets the value of {@link #showYearProperty()}.
     *
     * @param show
     *            if true the year will be shown
     */
    public final void setShowYear(boolean show) {
        showYearProperty().set(show);
    }

    /**
     * Returns the value of {@link #showYearProperty()}.
     *
     * @return true if the year will be shown
     */
    public final boolean isShowYear() {
        return showYearProperty().get();
    }

    private final BooleanProperty showTodayButton = new SimpleBooleanProperty(
            this, "showTodayButton", true); //$NON-NLS-1$

    /**
     * Show or hide a button to quickly go to today's date.
     *
     * @return true if the button will be shown
     */
    public final BooleanProperty showTodayButtonProperty() {
        return showTodayButton;
    }

    /**
     * Sets the value of the {@link #showTodayButtonProperty()}.
     *
     * @param show
     *            if true will show the button
     */
    public final void setShowTodayButton(boolean show) {
        showTodayButtonProperty().set(show);
    }

    /**
     * Returns the value of the {@link #showTodayButtonProperty()}.
     *
     * @return true if the button is shown
     */
    public final boolean isShowTodayButton() {
        return showTodayButtonProperty().get();
    }

    private final BooleanProperty showMonthArrows = new SimpleBooleanProperty(
            this, "showMonthArrows", true); //$NON-NLS-1$

    /**
     * Shows or hides the arrows to change the month.
     *
     * @return true if the arrows will be shown
     */
    public final BooleanProperty showMonthArrowsProperty() {
        return showMonthArrows;
    }

    /**
     * Sets the value of the {@link #showMonthArrowsProperty()}.
     *
     * @param show
     *            if true will show the arrows
     */
    public final void setShowMonthArrows(boolean show) {
        showMonthArrowsProperty().set(show);
    }

    /**
     * Returns the value of the {@link #showMonthArrowsProperty()}.
     *
     * @return true if the arrows will be shown
     */
    public final boolean isShowMonthArrows() {
        return showMonthArrowsProperty().get();
    }

    private final BooleanProperty showYearArrows = new SimpleBooleanProperty(
            this, "showYearArrows", true); //$NON-NLS-1$

    /**
     * Shows or hides the arrows to change the year.
     *
     * @return true if the arrows will be shown
     */
    public final BooleanProperty showYearArrowsProperty() {
        return showYearArrows;
    }

    /**
     * Sets the value of the {@link #showYearArrowsProperty()}.
     *
     * @param show
     *            if true will show the arrows
     */
    public final void setShowYearArrows(boolean show) {
        showYearArrowsProperty().set(show);
    }

    /**
     * Returns the value of the {@link #showYearArrowsProperty()}.
     *
     * @return true if the arrows will be shown
     */
    public final boolean isShowYearArrows() {
        return showYearArrowsProperty().get();
    }

    private final BooleanProperty showUsageColors = new SimpleBooleanProperty(
            this, "showUsageColors", false); //$NON-NLS-1$

    /**
     * Show or hide usage colors that are based on the number of entries on a
     * given date. The image below shows those colors in action:
     * <p/>
     * <center><img src="doc-files/usage-colors.png"></center>
     *
     * @return true if the usage colors will be shown
     */
    public final BooleanProperty showUsageColorsProperty() {
        return showUsageColors;
    }

    /**
     * Sets the value of {@link #showUsageColorsProperty()}.
     *
     * @param show
     *            if true will show the colors
     */
    public final void setShowUsageColors(boolean show) {
        showUsageColorsProperty().set(show);
    }

    /**
     * Returns the value of the {@link #showUsageColorsProperty()}.
     *
     * @return true if the colors will be shown
     */
    public final boolean isShowUsageColors() {
        return showUsageColorsProperty().get();
    }

    /**
     * An enumerator to control the behaviour of the control when the user
     * clicks on a date.
     *
     * @see YearMonthView#clickBehaviourProperty()
     */
    public enum ClickBehaviour {

        /**
         * A value used to make the control select the date on which the user clicked.
         */
        PERFORM_SELECTION,

        /**
         * A value used to make the control show some kind of dialog or popover to show
         * details about the clicked date.
         */
        SHOW_DETAILS,

        /**
         * Do nothing when the user clicks on a date.
         */
        NONE
    }

    private final ObjectProperty<ClickBehaviour> clickBehaviour = new SimpleObjectProperty<>(this, "clickBehaviour", PERFORM_SELECTION); //$NON-NLS-1$

    /**
     * The behaviour used when the user clicks on a date.
     *
     * @return the click behaviour
     */
    public final ObjectProperty<ClickBehaviour> clickBehaviourProperty() {
        return clickBehaviour;
    }

    /**
     * Sets the value of {@link #clickBehaviourProperty()}.
     *
     * @param behaviour the click behaviour
     */
    public final void setClickBehaviour(ClickBehaviour behaviour) {
        requireNonNull(behaviour);
        clickBehaviourProperty().set(behaviour);
    }

    /**
     * Returns the value of {@link #clickBehaviourProperty()}.
     *
     * @return the click behaviour
     */
    public final ClickBehaviour getClickBehaviour() {
        return clickBehaviourProperty().get();
    }

    private static final String MONTH_VIEW_CATEGORY = "Month View"; //$NON-NLS-1$

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {
            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(clickBehaviourProperty());
            }

            @Override
            public void setValue(Object value) {
                setClickBehaviour((ClickBehaviour) value);
            }

            @Override
            public Object getValue() {
                return getClickBehaviour();
            }

            @Override
            public Class<?> getType() {
                return ClickBehaviour.class;
            }

            @Override
            public String getName() {
                return "Click Behaviour"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Click behaviour"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showMonthProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowMonth((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowMonth();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Month"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Show or hide the name of the month"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showTodayButtonProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowTodayButton((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowTodayButton();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Today Button"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Show or hide the 'today' button"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showUsageColorsProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowUsageColors((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowUsageColors();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Usage Colors"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Show Usage Colors"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showYearProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowYear((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowYear();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Year"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Show or hide the year"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showYearArrowsProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowYearArrows((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowYearArrows();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Year Arrows"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Show or hide the year adjuster arrows"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_VIEW_CATEGORY;
            }
        });

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showMonthArrowsProperty());
            }

            @Override
            public void setValue(Object value) {
                setShowMonthArrows((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowMonthArrows();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public String getName() {
                return "Show Month Arrows"; //$NON-NLS-1$
            }

            @Override
            public String getDescription() {
                return "Show or hide the month adjustment arrows"; //$NON-NLS-1$
            }

            @Override
            public String getCategory() {
                return MONTH_VIEW_CATEGORY;
            }
        });

        return items;
    }
}
