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

import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;
import javafx.util.Duration;

import static javafx.scene.control.ContentDisplay.TEXT_ONLY;

public class PopOverTitledPane extends TitledPane {

    public PopOverTitledPane(final String title, final Node detailedContent) {
        this(title, null, detailedContent);
    }

    public PopOverTitledPane(final String title, final Node summaryContent,
                             final Node detailedContent) {

        super(title, detailedContent);

        if (title == null) {
            throw new IllegalArgumentException("title can not be null"); //$NON-NLS-1$
        }

        if (detailedContent == null) {
            throw new IllegalArgumentException(
                    "detailed content can not be null"); //$NON-NLS-1$
        }

        setContentDisplay(TEXT_ONLY);
        setGraphic(summaryContent);

        expandedProperty().addListener(
                (value, oldExpanded, newExpanded) -> {
                    if (newExpanded) {
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        detailedContent.setOpacity(0);
                        FadeTransition fadeInContent = new FadeTransition(
                                getFadingDuration());
                        fadeInContent.setFromValue(0);
                        fadeInContent.setToValue(1);
                        fadeInContent.setNode(detailedContent);
                        fadeInContent.play();
                    } else {
                        if (summaryContent != null) {
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            summaryContent.setOpacity(0);
                            FadeTransition fadeInSummary = new FadeTransition(
                                    getFadingDuration());
                            fadeInSummary.setFromValue(0);
                            fadeInSummary.setToValue(1);
                            fadeInSummary.setNode(summaryContent);
                            fadeInSummary.play();
                        }
                    }
                });
    }

    private final ObjectProperty<Duration> fadingDuration = new SimpleObjectProperty<>(
            this, "fadingDuration", Duration.seconds(.2)); //$NON-NLS-1$

    public final ObjectProperty<Duration> fadingDurationProperty() {
        return fadingDuration;
    }

    public final void setFadingDuration(Duration duration) {
        fadingDurationProperty().set(duration);
    }

    public final Duration getFadingDuration() {
        return fadingDurationProperty().get();
    }
}
