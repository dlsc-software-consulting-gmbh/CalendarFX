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

package com.calendarfx.view.print;

import impl.com.calendarfx.view.print.OptionsViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

/**
 * A control that allows to toggle a couple of options inside the print preview dialog.
 * The default style class of this view is "options-view".
 * <p>
 * <center><img src="doc-files/options-view.png"></center>
 */
public class OptionsView extends ViewTypeControl {

    private static final String DEFAULT_STYLE_CLASS = "options-view";

    /**
     * Constructs a new view.
     */
    public OptionsView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new OptionsViewSkin(this);
    }

    private final BooleanProperty showAllDayEntries = new SimpleBooleanProperty(this, "showAllDayEntries", true);

    public final BooleanProperty showAllDayEntriesProperty() {
        return showAllDayEntries;
    }

    public final boolean isShowAllDayEntries() {
        return showAllDayEntriesProperty().get();
    }

    public final void setShowAllDayEntries(boolean showAllDayEntries) {
        showAllDayEntriesProperty().set(showAllDayEntries);
    }

    private final BooleanProperty showEntryDetails = new SimpleBooleanProperty(this, "showEntryDetails", true);

    public final BooleanProperty showEntryDetailsProperty() {
        return showEntryDetails;
    }

    public final boolean isShowEntryDetails() {
        return showEntryDetailsProperty().get();
    }

    public final void setShowEntryDetails(boolean showEntryDetails) {
        showEntryDetailsProperty().set(showEntryDetails);
    }

    private final BooleanProperty showTimedEntries = new SimpleBooleanProperty(this, "showTimedEntries", true);

    public final BooleanProperty showTimedEntriesProperty() {
        return showTimedEntries;
    }

    public final boolean isShowTimedEntries() {
        return showTimedEntriesProperty().get();
    }

    public final void setShowTimedEntries(boolean showTimedEntries) {
        showTimedEntriesProperty().set(showTimedEntries);
    }

    private final BooleanProperty showMiniCalendars = new SimpleBooleanProperty(this, "showMiniCalendars", true);

    public final BooleanProperty showMiniCalendarsProperty() {
        return showMiniCalendars;
    }

    public final boolean isShowMiniCalendars() {
        return showMiniCalendarsProperty().get();
    }

    public final void setShowMiniCalendars(boolean showMiniCalendars) {
        showMiniCalendarsProperty().set(showMiniCalendars);
    }

    private final BooleanProperty showCalendarKeys = new SimpleBooleanProperty(this, "showCalendarKeys", true);

    public final BooleanProperty showCalendarKeysProperty() {
        return showCalendarKeys;
    }

    public final boolean isShowCalendarKeys() {
        return showCalendarKeysProperty().get();
    }

    public final void setShowCalendarKeys(boolean showCalendarKeys) {
        showCalendarKeysProperty().set(showCalendarKeys);
    }

    private final BooleanProperty showSwimlaneLayout = new SimpleBooleanProperty(this, "showSwimlaneLayout", true);

    public final BooleanProperty showSwimlaneLayoutProperty() {
        return showSwimlaneLayout;
    }

    public final boolean isShowSwimlaneLayout() {
        return showSwimlaneLayoutProperty().get();
    }

    public final void setShowSwimlaneLayout(boolean showSwimlaneLayout) {
        showSwimlaneLayoutProperty().set(showSwimlaneLayout);
    }

    private static final String PRINT_OPTIONS_CATEGORY = "Options View";

    @Override
    public ObservableList<PropertySheet.Item> getPropertySheetItems() {
        ObservableList<PropertySheet.Item> items = super.getPropertySheetItems();

        items.add(new PropertySheet.Item() {
            @Override
            public void setValue(Object value) {
                setShowAllDayEntries((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowAllDayEntries();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showAllDayEntriesProperty());
            }

            @Override
            public String getName() {
                return "All Day Entries";
            }

            @Override
            public String getDescription() {
                return "All Day Entries";
            }

            @Override
            public String getCategory() {
                return PRINT_OPTIONS_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public void setValue(Object value) {
                setShowCalendarKeys((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowCalendarKeys();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showCalendarKeysProperty());
            }

            @Override
            public String getName() {
                return "Calendar Keys";
            }

            @Override
            public String getDescription() {
                return "Show calendar keys";
            }

            @Override
            public String getCategory() {
                return PRINT_OPTIONS_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public void setValue(Object value) {
                setShowEntryDetails((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowEntryDetails();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showEntryDetailsProperty());
            }

            @Override
            public String getName() {
                return "Entry Details";
            }

            @Override
            public String getDescription() {
                return "Show entry details";
            }

            @Override
            public String getCategory() {
                return PRINT_OPTIONS_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public void setValue(Object value) {
                setShowMiniCalendars((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowMiniCalendars();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showMiniCalendarsProperty());
            }

            @Override
            public String getName() {
                return "Mini Calendars";
            }

            @Override
            public String getDescription() {
                return "Show mini calendars";
            }

            @Override
            public String getCategory() {
                return PRINT_OPTIONS_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public void setValue(Object value) {
                setShowSwimlaneLayout((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowSwimlaneLayout();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showSwimlaneLayoutProperty());
            }

            @Override
            public String getName() {
                return "Swimlanes Layout";
            }

            @Override
            public String getDescription() {
                return "Show swimlane layout";
            }

            @Override
            public String getCategory() {
                return PRINT_OPTIONS_CATEGORY;
            }
        });

        items.add(new PropertySheet.Item() {
            @Override
            public void setValue(Object value) {
                setShowTimedEntries((boolean) value);
            }

            @Override
            public Object getValue() {
                return isShowTimedEntries();
            }

            @Override
            public Class<?> getType() {
                return Boolean.class;
            }

            @Override
            public Optional<ObservableValue<?>> getObservableValue() {
                return Optional.of(showTimedEntriesProperty());
            }

            @Override
            public String getName() {
                return "Timed Entries";
            }

            @Override
            public String getDescription() {
                return "Show timed entries";
            }

            @Override
            public String getCategory() {
                return PRINT_OPTIONS_CATEGORY;
            }
        });

        return items;
    }
}
