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
