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

import java.time.ZonedDateTime;

/**
 * An interface that needs to be implemented by those date controls that want to
 * support the creation of new entries with a double click. For this to work
 * they need to supply the time at the given click location.
 */
public interface ZonedDateTimeProvider {

    /**
     * Returns the time at the given location.
     *
     * @param x
     *            the x coordinate of the input event
     * @param y
     *            the y coordinate of the input event
     * @return the time at the given location
     */
    ZonedDateTime getZonedDateTimeAt(double x, double y);
}
