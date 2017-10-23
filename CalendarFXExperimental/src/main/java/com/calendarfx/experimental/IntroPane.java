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

package com.calendarfx.experimental;

import com.calendarfx.view.CalendarFXControl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Skin;

import java.util.Objects;

/**
 * Created by dirk on 18/01/17.
 */
public class IntroPane extends CalendarFXControl {

    public IntroPane() {
        getStylesheets().add(IntroPane.class.getResource("intro.css").toExternalForm());

        getStyleClass().add("intro-pane");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new IntroPaneSkin(this);
    }

    private final ObservableList<IntroTarget> targets = FXCollections.observableArrayList();

    public final ObservableList<IntroTarget> getTargets() {
        return targets;
    }

    public static class IntroTarget {

        private final Parent parent;
        private final String id;

        public IntroTarget(Parent parent, String id) {
            this.parent = Objects.requireNonNull(parent);
            this.id = Objects.requireNonNull(id);
        }

        public final Parent getParent() {
            return parent;
        }

        public final String getId() {
            return id;
        }
    }
}
