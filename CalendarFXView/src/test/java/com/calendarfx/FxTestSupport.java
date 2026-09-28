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

package com.calendarfx;

import javafx.application.Application;

import java.lang.reflect.Field;

/**
 * Support class for unit tests that need to instantiate JavaFX controls without
 * running a JavaFX application.
 * <p>
 * The static initializer of {@link javafx.scene.control.Control} installs the default
 * platform user agent stylesheet, which requires a running toolkit. By registering a
 * user agent stylesheet upfront that initializer becomes a no-op and controls can be
 * created in a plain (and headless) JVM.
 */
public final class FxTestSupport {

    private FxTestSupport() {
    }

    /**
     * Makes sure that JavaFX controls can be instantiated without a running toolkit.
     * Calling this method more than once has no effect.
     */
    public static void initializeUserAgentStylesheet() {
        if (Application.getUserAgentStylesheet() != null) {
            return;
        }

        try {
            Field field = Application.class.getDeclaredField("userAgentStylesheet");
            field.setAccessible(true);
            field.set(null, Application.STYLESHEET_MODENA);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            throw new IllegalStateException("unable to register a user agent stylesheet for tests, "
                    + "make sure the surefire argLine contains "
                    + "'--add-opens javafx.graphics/javafx.application=com.calendarfx.view'", ex);
        }
    }
}
