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

import com.calendarfx.view.DateControl.ContextMenuParameter;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import com.calendarfx.view.DayViewBase.HoursLayoutStrategy;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.util.Callback;

import java.text.MessageFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy.HIDE;
import static com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy.SHOW;
import static com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy.SHOW_COMPRESSED;
import static com.calendarfx.view.VirtualGrid.OFF;

/**
 * An implementation of the context menu callback required by
 * {@link DateControl}. Applications can subclass to create their own context
 * menus for the different views.
 *
 * @see DateControl#setContextMenuCallback(Callback)
 */
public class ContextMenuProvider
        implements Callback<ContextMenuParameter, ContextMenu> {

    @Override
    public ContextMenu call(ContextMenuParameter param) {
        DateControl control = param.getDateControl();
        ContextMenu contextMenu = null;

        /*
         * Check for WeekDayView first because it is a specialization of
         * DayView. Otherwise we would always return the context menu of
         * DayView.
         */
        if (control instanceof WeekDayView) {
            contextMenu = getWeekDayViewMenu(param);
        } else if (control instanceof DayView) {
            contextMenu = getDayViewMenu(param);
        } else if (control instanceof AllDayView) {
            contextMenu = getAllDayViewMenu(param);
        }

        if (contextMenu == null || contextMenu.getItems().isEmpty()) {
            return null;
        }

        return contextMenu;
    }

    /**
     * Returns the context menu specific for a single {@link DayView}.
     *
     * @param param
     *            parameter object with the most relevant information for
     *            creating a new context menu
     * @return a context menu for a day view
     */
    protected ContextMenu getDayViewMenu(ContextMenuParameter param) {
        return getDayViewBaseMenu(param);
    }

    /**
     * Returns the context menu specific for a single {@link WeekDayView}. Week
     * day views are used inside a {@link WeekView}.
     *
     * @param param
     *            parameter object with the most relevant information for
     *            creating a new context menu
     * @return a context menu for a week day view
     */
    protected ContextMenu getWeekDayViewMenu(ContextMenuParameter param) {
        ContextMenu contextMenu = getDayViewBaseMenu(param);

        WeekDayView weekDayView = (WeekDayView) param.getDateControl();
        WeekView weekView = weekDayView.getWeekView();
        Menu daysMenu = new Menu(Messages.getString("ContextMenuProvider.SHOW_DAYS")); //$NON-NLS-1$
        int[] days = new int[]{5, 7, 14, 21, 28};
        for (int d : days) {
            String itemText = MessageFormat.format(Messages.getString("ContextMenuProvider.DAYS"), d); //$NON-NLS-1$
            MenuItem item = new MenuItem(itemText);
            item.setOnAction(evt -> weekView.setNumberOfDays(d));
            daysMenu.getItems().add(item);
        }

        contextMenu.getItems().add(daysMenu);

        return contextMenu;
    }

    private ContextMenu getDayViewBaseMenu(ContextMenuParameter param) {
        ContextMenu contextMenu = new ContextMenu();

        DateControl control = param.getDateControl();
        if (control instanceof DayView) {
            DayViewBase dayView = (DayViewBase) control;

            MenuItem newEntry = new MenuItem(Messages.getString("ContextMenuProvider.ADD_NEW_EVENT")); //$NON-NLS-1$
            newEntry.setOnAction(evt -> {
                control.createEntryAt(param.getZonedDateTime());
                contextMenu.hide();
            });
            contextMenu.getItems().add(newEntry);

            /*
             * Only add submenu if view does not use all 24 hours.
             */
            if (!(dayView.getStartTime().equals(LocalTime.MIN)
                    && dayView.getEndTime().equals(LocalTime.MAX))) {
                // Early / late hours menu
                Menu earlyLateHoursMenu = new Menu(Messages.getString("ContextMenuProvider.EARLY_LATE_HOURS")); //$NON-NLS-1$
                RadioMenuItem hideItem = new RadioMenuItem(Messages.getString("ContextMenuProvider.EARLY_LATE_HOURS_HIDE")); //$NON-NLS-1$
                RadioMenuItem showItem = new RadioMenuItem(Messages.getString("ContextMenuProvider.EARLY_LATE_HOURS_SHOW")); //$NON-NLS-1$
                RadioMenuItem showCompressedItem = new RadioMenuItem(
                        Messages.getString("ContextMenuProvider.EARLY_LATE_HOURS_COMPRESSED")); //$NON-NLS-1$
                hideItem.setOnAction(
                        evt -> dayView.setEarlyLateHoursStrategy(HIDE));
                showItem.setOnAction(
                        evt -> dayView.setEarlyLateHoursStrategy(SHOW));
                showCompressedItem.setOnAction(evt -> dayView
                        .setEarlyLateHoursStrategy(SHOW_COMPRESSED));
                switch (dayView.getEarlyLateHoursStrategy()) {
                    case HIDE:
                        hideItem.setSelected(true);
                        break;
                    case SHOW:
                        showItem.setSelected(true);
                        break;
                    case SHOW_COMPRESSED:
                        showCompressedItem.setSelected(true);
                        break;
                    default:
                        break;
                }
                ToggleGroup group = new ToggleGroup();
                group.getToggles().setAll(hideItem, showItem,
                        showCompressedItem);
                earlyLateHoursMenu.getItems().setAll(hideItem, showItem,
                        showCompressedItem);
                contextMenu.getItems().add(earlyLateHoursMenu);

                Menu gridMenu = new Menu(Messages.getString("ContextMenuProvider.GRID")); //$NON-NLS-1$
                MenuItem gridOff = new MenuItem(Messages.getString("ContextMenuProvider.GRID_OFF")); //$NON-NLS-1$
                gridOff.setOnAction(evt -> control.setVirtualGrid(OFF));
                gridMenu.getItems().add(gridOff);
                gridMenu.getItems().add(new SeparatorMenuItem());
                int[] grids = new int[]{5, 10, 15, 30, 60};
                for (int grid : grids) {
                    String itemText = MessageFormat.format(Messages.getString("ContextMenuProvider.MINUTES"), grid); //$NON-NLS-1$
                    String itemTextShort = MessageFormat.format(Messages.getString("ContextMenuProvider.MINUTES_SHORT"), grid); //$NON-NLS-1$
                    MenuItem gridItem = new MenuItem(itemText);
                    gridMenu.getItems().add(gridItem);
                    gridItem.setOnAction(evt -> control
                            .setVirtualGrid(new VirtualGrid(itemText,
                                    itemTextShort, ChronoUnit.MINUTES, grid)));
                }

                contextMenu.getItems().add(gridMenu);

                Menu hoursMenu = new Menu(Messages.getString("ContextMenuProvider.SHOW_HOURS")); //$NON-NLS-1$
                MenuItem hourHeight = new MenuItem();
                Slider slider = new Slider(40, 200, 50);
                slider.setPrefWidth(100);
                slider.setValue(dayView.getHourHeight());
                slider.valueProperty().addListener(it -> {
                    dayView.setHoursLayoutStrategy(
                            HoursLayoutStrategy.FIXED_HOUR_HEIGHT);
                    dayView.setHourHeight(slider.getValue());
                });
                Label sliderWrapper = new Label();
                sliderWrapper.setGraphic(slider);
                sliderWrapper.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                hourHeight.setGraphic(sliderWrapper);
                hoursMenu.getItems().add(hourHeight);
                hoursMenu.getItems().add(new SeparatorMenuItem());
                int[] hours = new int[]{4, 6, 8, 10, 12, 18, 24};
                for (int h : hours) {
                    String labelText = MessageFormat.format(Messages.getString("ContextMenuProvider.HOURS"), h); //$NON-NLS-1$
                    Label wrapper = new Label(labelText);
                    MenuItem item = new MenuItem();
                    item.setGraphic(wrapper);
                    item.setOnAction(evt -> {
                        dayView.setEarlyLateHoursStrategy(
                                EarlyLateHoursStrategy.SHOW);
                        dayView.setHoursLayoutStrategy(
                                HoursLayoutStrategy.FIXED_HOUR_COUNT);
                        dayView.setVisibleHours(h);
                    });
                    hoursMenu.getItems().add(item);
                }
                contextMenu.getItems().add(hoursMenu);
            }
        }

        return contextMenu;
    }

    /**
     * Returns the context menu specific for an {@link AllDayView}.
     *
     * @param param
     *            parameter object with the most relevant information for
     *            creating a new context menu
     * @return a context menu for an all day view
     */
    protected ContextMenu getAllDayViewMenu(ContextMenuParameter param) {
        ContextMenu contextMenu = new ContextMenu();

        DateControl control = param.getDateControl();
        if (control instanceof AllDayView) {
            MenuItem newEntry = new MenuItem(Messages.getString("ContextMenuProvider.ADD_NEW_EVENT")); //$NON-NLS-1$
            newEntry.setOnAction(evt -> {
                control.createEntryAt(param.getZonedDateTime());
                contextMenu.hide();
            });
            contextMenu.getItems().add(newEntry);
        }

        if (contextMenu.getItems().isEmpty()) {
            return null;
        }

        return contextMenu;
    }
}
