/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
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
