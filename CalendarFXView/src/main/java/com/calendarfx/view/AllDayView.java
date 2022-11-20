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

import com.calendarfx.model.Entry;
import impl.com.calendarfx.view.AllDayViewSkin;
import impl.com.calendarfx.view.util.Util;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet.Item;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A date control used on top of a {@link DayView} or a {@link DetailedWeekView} for
 * showing "full day" calendar entries. This view can be configured to span a
 * given number of days. One day is sufficient when used with a {@link DayView}
 * and seven days when used with a {@link DetailedWeekView}.
 *
 * <img src="doc-files/all-day-view.png" alt="All Day View">
 *
 * @see Entry#isFullDay()
 */
public class AllDayView extends DateControl implements ZonedDateTimeProvider {

    private static final String ALL_DAY_VIEW = "all-day-view";

    /**
     * Constructs a new view for the given number of days.
     *
     * @param numberOfDays the number of days to be shown by this view
     */
    public AllDayView(int numberOfDays) {
        if (numberOfDays <= 0) {
            throw new IllegalArgumentException("number of days must be larger than zero");
        }

        getStyleClass().add(ALL_DAY_VIEW);
        setNumberOfDays(numberOfDays);

        new CreateAndDeleteHandler(this);


    }

    /**
     * Constructs a new view for seven days.
     */
    public AllDayView() {
        this(7);
    }

    @Override
    public final ZonedDateTime getZonedDateTimeAt(double x, double y, ZoneId zoneId) {
        int day = (int) (x / (getWidth() / getNumberOfDays()));

        LocalDate date = getDate();

        if (isAdjustToFirstDayOfWeek()) {
            date = Util.adjustToFirstDayOfWeek(date, getFirstDayOfWeek());
        }

        date = date.plusDays(day);

        LocalTime time = LocalTime.NOON;
        return ZonedDateTime.of(date, time, zoneId);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AllDayViewSkin(this);
    }

    private StyleableObjectProperty<Insets> extraPadding;

    /**
     * Extra padding to be used inside of the view above and below the full day
     * entries. This is required as the regular padding is already used for
     * other styling purposes.
     *
     * @return insets for extra padding
     */
    public final ObjectProperty<Insets> extraPaddingProperty() {
        if (extraPadding == null) {
            extraPadding = new StyleableObjectProperty<>(new Insets(2, 0, 9, 0)) {

                @Override
                public CssMetaData<AllDayView, Insets> getCssMetaData() {
                    return StyleableProperties.EXTRA_PADDING;
                }

                @Override
                public Object getBean() {
                    return AllDayView.this;
                }

                @Override
                public String getName() {
                    return "extraPadding";
                }
            };
        }



        return extraPadding;
    }

    /**
     * Returns the value of {@link #extraPaddingProperty()}.
     *
     * @return extra padding insets
     */
    public final Insets getExtraPadding() {
        return extraPaddingProperty().get();
    }

    /**
     * Sets the value of {@link #extraPaddingProperty()}.
     *
     * @param padding padding insets
     */
    public final void setExtraPadding(Insets padding) {
        requireNonNull(padding);
        extraPaddingProperty().set(padding);
    }

    private StyleableDoubleProperty rowHeight;

    /**
     * The height for each row shown by the view. This value determines the
     * total height of the view.
     *
     * @return the row height property
     */
    public final DoubleProperty rowHeightProperty() {
        if (rowHeight == null) {
            rowHeight = new StyleableDoubleProperty(20) {

                @Override
                public CssMetaData<AllDayView, Number> getCssMetaData() {
                    return StyleableProperties.ROW_HEIGHT;
                }

                @Override
                public Object getBean() {
                    return AllDayView.this;
                }

                @Override
                public String getName() {
                    return "rowHeight";
                }
            };
        }

        return rowHeight;
    }

    /**
     * Returns the value of {@link #rowHeightProperty()}.
     *
     * @return the row height
     */
    public final double getRowHeight() {
        return rowHeightProperty().get();
    }

    /**
     * Sets the value of the {@link #rowHeightProperty()}.
     *
     * @param height the new row height
     */
    public final void setRowHeight(double height) {
        rowHeightProperty().set(height);
    }

    private StyleableDoubleProperty rowSpacing;

    /**
     * Stores the spacing between rows in the view.
     *
     * @return the spacing between rows in pixels
     */
    public final DoubleProperty rowSpacingProperty() {
        if (rowSpacing == null) {
            rowSpacing = new StyleableDoubleProperty(2) {

                @Override
                public CssMetaData<AllDayView, Number> getCssMetaData() {
                    return StyleableProperties.ROW_SPACING;
                }

                @Override
                public Object getBean() {
                    return AllDayView.this;
                }

                @Override
                public String getName() {
                    return "rowSpacing";
                }
            };
        }

        return rowSpacing;
    }

    /**
     * Returns the value of {@link #rowSpacingProperty()}.
     *
     * @return the row spacing in pixels
     */
    public final double getRowSpacing() {
        return rowSpacingProperty().get();
    }

    /**
     * Sets the value of {@link #rowSpacingProperty()}.
     *
     * @param space the space between rows in pixel
     */
    public final void setRowSpacing(double space) {
        if (space < 0) {
            throw new IllegalArgumentException("row spacing can not be smaller than zero");
        }
        rowSpacingProperty().set(space);
    }

    private StyleableDoubleProperty columnSpacing;

    /**
     * Stores the spacing between columns in the view.
     *
     * @return the spacing between columns in pixels
     */
    public final DoubleProperty columnSpacingProperty() {
        if (columnSpacing == null) {
            columnSpacing = new StyleableDoubleProperty(2) {

                @Override
                public CssMetaData<AllDayView, Number> getCssMetaData() {
                    return StyleableProperties.COLUMN_SPACING;
                }

                @Override
                public Object getBean() {
                    return AllDayView.this;
                }

                @Override
                public String getName() {
                    return "columnSpacing";
                }
            };
        }

        return columnSpacing;
    }

    /**
     * Returns the value of {@link #columnSpacingProperty()}.
     *
     * @return the row spacing in pixels
     */
    public final double getColumnSpacing() {
        return columnSpacingProperty().get();
    }

    /**
     * Sets the value of {@link #columnSpacingProperty()}.
     *
     * @param space the space between columns in pixel
     */
    public final void setColumnSpacing(double space) {
        columnSpacingProperty().set(space);
    }

    private final BooleanProperty adjustToFirstDayOfWeek = new SimpleBooleanProperty(this, "adjustToFirstDayOfWeek", true);

    /**
     * A flag used to indicate that the view should always show the first day of
     * the week (e.g. "Monday") at its beginning even if the
     * {@link #dateProperty()} is set to another day (e.g. "Thursday"). The
     * adjustment is normally needed if the view is used in combination with the
     * {@link DetailedWeekView}. It is not needed if the view is used together with the
     * {@link DayView}.
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

    private final IntegerProperty numberOfDays = new SimpleIntegerProperty(this, "numberOfDays");

    /**
     * Stores the number of days that will be shown by this view. This value
     * will be 1 if the view is used in combination with the {@link DayView} and
     * 7 if used together with the {@link DetailedWeekView}.
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

    private final ObjectProperty<Callback<Entry<?>, AllDayEntryView>> entryViewFactory = new SimpleObjectProperty<>(this, "entryViewFactory", AllDayEntryView::new);

    /**
     * A callback used for producing views for entries. The views have to be of
     * type {@link AllDayEntryView}.
     *
     * @return the entry view factory
     */
    public final ObjectProperty<Callback<Entry<?>, AllDayEntryView>> entryViewFactoryProperty() {
        return entryViewFactory;
    }

    /**
     * Returns the value of {@link #entryViewFactoryProperty()}.
     *
     * @return the entry view factory callback
     */
    public final Callback<Entry<?>, AllDayEntryView> getEntryViewFactory() {
        return entryViewFactoryProperty().get();
    }

    /**
     * Sets the value of {@link #entryViewFactoryProperty()}.
     *
     * @param factory the new entry view factory
     */
    public final void setEntryViewFactory(Callback<Entry<?>, AllDayEntryView> factory) {
        requireNonNull(factory);
        entryViewFactoryProperty().set(factory);
    }

    private final ObjectProperty<Callback<AllDayView, Region>> separatorFactory = new SimpleObjectProperty<>(this, "separatorFactory", it -> {
        Region region = new Region();
        region.getStyleClass().add("weekday-separator");
        return region;
    });


    public final Callback<AllDayView, Region> getSeparatorFactory() {
        return separatorFactory.get();
    }

    /**
     * A factory used for creating (optional) vertical separators between the all day view.
     *
     * @return the separator factory
     */
    public final ObjectProperty<Callback<AllDayView, Region>> separatorFactoryProperty() {
        return separatorFactory;
    }

    public final void setSeparatorFactory(Callback<AllDayView, Region> separatorFactory) {
        this.separatorFactory.set(separatorFactory);
    }

    private static class StyleableProperties {

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private static final CssMetaData<AllDayView, Number> ROW_HEIGHT = new CssMetaData<AllDayView, Number>(
                "-fx-row-height", StyleConverter.getSizeConverter(), 20d) {

            @Override
            public Double getInitialValue(AllDayView node) {
                return node.getRowHeight();
            }

            @Override
            public boolean isSettable(AllDayView n) {
                return n.rowHeight == null || !n.rowHeight.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Number> getStyleableProperty(AllDayView n) {
                return (StyleableProperty<Number>) n.rowHeightProperty();
            }
        };

        private static final CssMetaData<AllDayView, Number> ROW_SPACING = new CssMetaData<AllDayView, Number>(
                "-fx-row-spacing", StyleConverter.getSizeConverter(), 2d) {

            @Override
            public Double getInitialValue(AllDayView node) {
                return node.getRowSpacing();
            }

            @Override
            public boolean isSettable(AllDayView n) {
                return n.rowSpacing == null || !n.rowSpacing.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Number> getStyleableProperty(AllDayView n) {
                return (StyleableProperty<Number>) n.rowSpacingProperty();
            }
        };

        private static final CssMetaData<AllDayView, Number> COLUMN_SPACING = new CssMetaData<AllDayView, Number>(
                "-fx-column-spacing", StyleConverter.getSizeConverter(), 2d) {

            @Override
            public Double getInitialValue(AllDayView node) {
                return node.getColumnSpacing();
            }

            @Override
            public boolean isSettable(AllDayView n) {
                return n.columnSpacing == null || !n.columnSpacing.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Number> getStyleableProperty(AllDayView n) {
                return (StyleableProperty<Number>) n.columnSpacingProperty();
            }
        };

        private static final CssMetaData<AllDayView, Insets> EXTRA_PADDING = new CssMetaData<AllDayView, Insets>(
                "-fx-extra-padding", StyleConverter.getInsetsConverter(),
                Insets.EMPTY) {

            @Override
            public Insets getInitialValue(AllDayView node) {
                return node.getExtraPadding();
            }

            @Override
            public boolean isSettable(AllDayView n) {
                return n.extraPadding == null || !n.extraPadding.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Insets> getStyleableProperty(AllDayView n) {
                return (StyleableProperty<Insets>) n.extraPaddingProperty();
            }
        };

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Region.getClassCssMetaData());

            styleables.add(ROW_HEIGHT);
            styleables.add(ROW_SPACING);
            styleables.add(COLUMN_SPACING);
            styleables.add(EXTRA_PADDING);

            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public final List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    private static final String ALL_DAY_VIEW_CATEGORY = "All Day View";

    @Override
    public ObservableList<Item> getPropertySheetItems() {
        ObservableList<Item> items = super.getPropertySheetItems();

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(numberOfDaysProperty());
            }

            @Override
            public void setValue(Object value) {
                setNumberOfDays((Integer) value);
            }

            @Override
            public Object getValue() {
                return getNumberOfDays();
            }

            @Override
            public Class<?> getType() {
                return Integer.class;
            }

            @Override
            public String getName() {
                return "Number Of Days";
            }

            @Override
            public String getDescription() {
                return "Determines how many days will be covered by this control";
            }

            @Override
            public String getCategory() {
                return ALL_DAY_VIEW_CATEGORY;
            }
        });

        // column spacing

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(columnSpacingProperty());
            }

            @Override
            public void setValue(Object value) {
                setColumnSpacing((double) value);
            }

            @Override
            public Object getValue() {
                return getColumnSpacing();
            }

            @Override
            public Class<?> getType() {
                return Double.class;
            }

            @Override
            public String getName() {
                return "Column Spacing";
            }

            @Override
            public String getDescription() {
                return "The gap between the days / columns";
            }

            @Override
            public String getCategory() {
                return ALL_DAY_VIEW_CATEGORY;
            }
        });

        // row height

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(rowHeightProperty());
            }

            @Override
            public void setValue(Object value) {
                setRowHeight((double) value);
            }

            @Override
            public Object getValue() {
                return getRowHeight();
            }

            @Override
            public Class<?> getType() {
                return Double.class;
            }

            @Override
            public String getName() {
                return "Row Height";
            }

            @Override
            public String getDescription() {
                return "The height of each row in the control";
            }

            @Override
            public String getCategory() {
                return ALL_DAY_VIEW_CATEGORY;
            }
        });

        // row spacing

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(rowSpacingProperty());
            }

            @Override
            public void setValue(Object value) {
                setRowSpacing((double) value);
            }

            @Override
            public Object getValue() {
                return getRowSpacing();
            }

            @Override
            public Class<?> getType() {
                return Double.class;
            }

            @Override
            public String getName() {
                return "Row Spacing";
            }

            @Override
            public String getDescription() {
                return "The gap between the rows";
            }

            @Override
            public String getCategory() {
                return ALL_DAY_VIEW_CATEGORY;
            }
        });

        // extra padding

        items.add(new Item() {

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(extraPaddingProperty());
            }

            @Override
            public void setValue(Object value) {
                setExtraPadding((Insets) value);
            }

            @Override
            public Object getValue() {
                return getExtraPadding();
            }

            @Override
            public Class<?> getType() {
                return Insets.class;
            }

            @Override
            public String getName() {
                return "Extra Padding";
            }

            @Override
            public String getDescription() {
                return "Additional padding inside the control";
            }

            @Override
            public String getCategory() {
                return ALL_DAY_VIEW_CATEGORY;
            }
        });

        return items;
    }
}
