/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view.popover;

import com.calendarfx.model.Entry;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

/**
 * Created by lemmi on 20.03.17.
 */
public class EntryPropertiesView extends EntryPopOverPane {

    public EntryPropertiesView(Entry<?> entry) {
        PropertySheet propertySheet = new PropertySheet();
        propertySheet.getItems().setAll(BeanPropertyUtils.getProperties(entry));
        getChildren().add(propertySheet);
        setMaxHeight(400);
    }
}
