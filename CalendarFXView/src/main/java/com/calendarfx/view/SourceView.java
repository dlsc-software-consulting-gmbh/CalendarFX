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
import impl.com.calendarfx.view.SourceViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;

/**
 * A view specialized in showing a list of {@link CalendarSource} instances.
 * This list can be used to toggle the visibility of calendars.
 * <p/>
 * <img src="doc-files/source-view.png">
 * <p/>
 */
public class SourceView extends CalendarFXControl {

    private static final String DEFAULT_STYLE_CLASS = "source-view";

    /**
     * Constructs a new source view.
     */
    public SourceView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        createContextMenu();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SourceViewSkin(this);
    }

    public final void bind(DateControl dateControl) {
        Bindings.bindContentBidirectional(calendarSources, dateControl.getCalendarSources());
        Bindings.bindContentBidirectional(calendarVisibilityMap, dateControl.getCalendarVisibilityMap());
    }

    public final void unbind(DateControl dateControl) {
        Bindings.unbindContentBidirectional(calendarSources, dateControl.getCalendarSources());
        Bindings.unbindContentBidirectional(calendarVisibilityMap, dateControl.getCalendarVisibilityMap());
    }

    private final ObservableList<CalendarSource> calendarSources = FXCollections
            .observableArrayList();

    /**
     * The list of calendar sources shown by the view.
     *
     * @return the calendar sources
     */
    public final ObservableList<CalendarSource> getCalendarSources() {
        return calendarSources;
    }

    private final ObservableMap<Calendar, BooleanProperty> calendarVisibilityMap = FXCollections.observableHashMap();

    public final ObservableMap<Calendar, BooleanProperty> getCalendarVisibilityMap() {
        return calendarVisibilityMap;
    }

    public final BooleanProperty getCalendarVisibilityProperty(Calendar calendar) {
        return calendarVisibilityMap.computeIfAbsent(calendar, cal -> new SimpleBooleanProperty(SourceView.this, "visible", true));
    }

    public final boolean isCalendarVisible(Calendar calendar) {
        BooleanProperty prop = getCalendarVisibilityProperty(calendar);
        return prop.get();
    }

    public final void setCalendarVisibility(Calendar calendar, boolean visible) {
        BooleanProperty prop = getCalendarVisibilityProperty(calendar);
        prop.set(visible);
    }

    private void createContextMenu() {
        MenuItem disableAll = new MenuItem();
        disableAll.setText(Messages.getString("SourceView.DISABLE_ALL"));
        disableAll.setOnAction(evt -> {
            for (CalendarSource source : getCalendarSources()) {
                for (Calendar calendar : source.getCalendars()) {
                    setCalendarVisibility(calendar, false);
                }
            }
        });

        MenuItem enableAll = new MenuItem();
        enableAll.setText(Messages.getString("SourceView.ENABLE_ALL"));
        enableAll.setOnAction(evt -> {
            for (CalendarSource source : getCalendarSources()) {
                for (Calendar calendar : source.getCalendars()) {
                    setCalendarVisibility(calendar, true);
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(disableAll, enableAll);
        setContextMenu(contextMenu);
    }
}
