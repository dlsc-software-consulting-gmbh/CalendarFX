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

package com.calendarfx.view.print;

import com.calendarfx.view.CalendarFXControl;
import com.calendarfx.view.SourceView;
import impl.com.calendarfx.view.print.SettingsViewSkin;
import javafx.scene.control.Skin;

/**
 * The right-hand side of the {@link PrintView}. This view combines several controls
 * found in the print package. The view contains the following sub-controls in this
 * order from top to bottom.
 * <ul>
 *     <li>PaperView - for setting the paper size, the view type (day, week, month) and to specify print margins.</li>
 *     <li>TimeRangeView - for specifying the time range that shall be printed (e.g. "today", "tomorrow", "next 5 months", etc...</li>
 *     <li>SourceView - for displaying the calendars that are used for printing. Calendar visibility can be toggled.</li>
 *     <li>OptionsView - for enabling / disabling various visualizations (e.g. swimlane layout).</li>
 * </ul>
 * The default style of this control is "print-settings-view".
 *
 * <center><img src="doc-files/settings-view.png"></center>
 */
public class SettingsView extends CalendarFXControl {

    public static final String DEFAULT_STYLE = "print-settings-view";

    private final PaperView paperView;
    private final TimeRangeView timeRangeView;
    private final SourceView sourceView;
    private final OptionsView optionsView;

    public SettingsView() {
        super();

        getStyleClass().add(DEFAULT_STYLE);

        paperView = new PaperView();
        timeRangeView = new TimeRangeView();
        sourceView = new SourceView();
        optionsView = new OptionsView();

        timeRangeView.viewTypeProperty().bind(paperView.viewTypeProperty());
        optionsView.viewTypeProperty().bind(paperView.viewTypeProperty());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SettingsViewSkin(this);
    }

    public final PaperView getPaperView() {
        return paperView;
    }

    public final TimeRangeView getTimeRangeView() {
        return timeRangeView;
    }

    public final OptionsView getOptionsView() {
        return optionsView;
    }

    public final SourceView getSourceView() {
        return sourceView;
    }
}
