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

package com.calendarfx.google.model;

import com.google.api.services.calendar.model.EventReminder;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Remind method.
 *
 * Created by gdiaz on 28/04/2017.
 */
public class GoogleEntryReminder {

    public GoogleEntryReminder() {
        super();
    }

    public GoogleEntryReminder(EventReminder reminder) {
        super();
        setMethod(RemindMethod.fromId(reminder.getMethod()));
        setMinutes(reminder.getMinutes());
    }

    public void addListener(InvalidationListener listener) {
        methodProperty().addListener(listener);
        minutesProperty().addListener(listener);
    }

    public void removeListener(InvalidationListener listener) {
        methodProperty().removeListener(listener);
        minutesProperty().addListener(listener);
    }

    private final ObjectProperty<RemindMethod> method = new SimpleObjectProperty<>(this, "method");

    public final ObjectProperty<RemindMethod> methodProperty() {
        return method;
    }

    public final RemindMethod getMethod() {
        return methodProperty().get();
    }

    public final void setMethod(RemindMethod method) {
        methodProperty().set(method);
    }

    private final ObjectProperty<Integer> minutes = new SimpleObjectProperty<>(this, "minutes");

    public final ObjectProperty<Integer> minutesProperty() {
        return minutes;
    }

    public final Integer getMinutes() {
        return minutesProperty().get();
    }

    public final void setMinutes(Integer minutes) {
        minutesProperty().set(minutes);
    }

    /**
     * Enumeration representing the available notification types.
     *
     * @author Gabriel Diaz, 07.03.2015.
     */
    public enum RemindMethod {

        POPUP("popup", "Popup"), EMAIL("email", "Email");

        private String id;
        private String name;

        RemindMethod(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }

        /**
         * Gets a remind method.
         *
         * @param id The id of the remind method.
         * @return The reminder method constant that matches the id.
         */
        public static RemindMethod fromId(String id) {
            if (id != null) {
                for (RemindMethod method : values()) {
                    if (method.id.equals(id)) {
                        return method;
                    }
                }
            }
            return POPUP;
        }
    }

}
