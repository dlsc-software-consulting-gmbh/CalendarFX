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

import impl.com.calendarfx.view.TimeScaleViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static java.util.Objects.requireNonNull;

/**
 * A control used for displaying a vertical time scale.
 *
 * <img src="doc-files/time-scale-view.png" alt="Time Scale View">
 */
public class TimeScaleView extends DayViewBase {

    /**
     * Interface for providing additional style properties.
     */
    public interface TimeScaleStyleProvider {
        String getStyle(LocalDateTime time);
    }

    /**
     * Constructs a new scale view.
     */
    public TimeScaleView() {
        getStyleClass().add("time-scale");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimeScaleViewSkin<>(this);
    }

    // DATE FORMATTER

    private final ObjectProperty<DateTimeFormatter> dateFormatter = new SimpleObjectProperty<>(this, "dateFormatter", DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));

    /**
     * Gets the DateTimeFormatter instance, which is used to provide the format
     * on the TimeScale for those labels that are displaying a date.
     * By default it has a value of {@link FormatStyle#SHORT}.
     *
     * @return the date formatter.
     */
    public final ObjectProperty<DateTimeFormatter> dateFormatterProperty() {
        return dateFormatter;
    }

    /**
     * Returns the value of {@link #dateFormatterProperty()}
     *
     * @return the date formatter
     */
    public final DateTimeFormatter getDateFormatter() {
        return dateFormatter.get();
    }

    /**
     * Sets the value of {@link #dateFormatterProperty()}
     *
     * @param formatter the date formatter, not {@code null}
     */
    public final void setDateFormatter(DateTimeFormatter formatter) {
        this.dateFormatter.set(formatter);
    }

    // TIME FORMATTER

    private final ObjectProperty<DateTimeFormatter> timeFormatter = new SimpleObjectProperty<>(this, "timeFormatter", DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));

    /**
     * Gets the DateTimeFormatter instance, which is used to provide the format
     * on the TimeScale for those labels that are displaying the time of day.
     * By default it has a value of {@link FormatStyle#SHORT}.
     *
     * @return the time of day formatter.
     */
    protected ObjectProperty<DateTimeFormatter> timeFormatterProperty() {
        return timeFormatter;
    }

    /**
     * Returns the value of {@link #timeFormatterProperty()}
     *
     * @return the time of day formatter
     */
    public final DateTimeFormatter getTimeFormatter() {
        return timeFormatterProperty().get();
    }

    /**
     * Sets the value of {@link #timeFormatterProperty()}
     *
     * @param formatter the time of day formatter, not {@code null}
     */
    public void setTimeFormatter(DateTimeFormatter formatter) {
        requireNonNull(formatter);
        timeFormatterProperty().set(formatter);
    }

    // STYLE PROVIDERS

    private static final TimeScaleStyleProvider DEFAULT_STYLE_PROVIDER = (time) -> null;

    private final ObjectProperty<TimeScaleStyleProvider> timeStyleProvider = new SimpleObjectProperty<>(this, "timeStyleProvider", DEFAULT_STYLE_PROVIDER);

    private final ObjectProperty<TimeScaleStyleProvider> dateStyleProvider = new SimpleObjectProperty<>(this, "dateStyleProvider", DEFAULT_STYLE_PROVIDER);

    /**
     * Gets the {@link TimeScaleStyleProvider} instance, which is used to provide
     * additional style properties for time labels of TimeScale.
     *
     * @return the style provider
     */
    public ObjectProperty<TimeScaleStyleProvider> timeStyleProviderProperty() {
        return timeStyleProvider;
    }

    /**
     * Returns the value of {@link #timeStyleProviderProperty()}.
     *
     * @return the style provider
     */
    public TimeScaleStyleProvider getTimeStyleProvider() {
        return timeStyleProvider.get();
    }

    /**
     * Sets the value of {@link #timeStyleProviderProperty()}.
     *
     * @param styleProvider time labels style provider, not {@code null}
     */
    public void setTimeStyleProvider(TimeScaleStyleProvider styleProvider) {
        this.timeStyleProvider.set(styleProvider);
    }

    /**
     * Gets the {@link TimeScaleStyleProvider} instance, which is used to provide
     * additional style properties for date labels of TimeScale.
     *
     * @return the style provider
     */
    public ObjectProperty<TimeScaleStyleProvider> dateStyleProviderProperty() {
        return dateStyleProvider;
    }

    /**
     * Returns the value of {@link #dateStyleProviderProperty()}.
     *
     * @return the style provider
     */
    public TimeScaleStyleProvider getDateStyleProvider() {
        return dateStyleProvider.get();
    }

    /**
     * Sets the value of {@link #dateStyleProviderProperty()}.
     *
     * @param styleProvider date labels style provider, not {@code null}
     */
    public void setDateStyleProvider(TimeScaleStyleProvider styleProvider) {
        this.dateStyleProvider.set(styleProvider);
    }
}
