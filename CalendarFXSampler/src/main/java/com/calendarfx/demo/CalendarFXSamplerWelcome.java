/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *  Copyright (C) 2006 Google Inc.
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

package com.calendarfx.demo;

import fxsampler.model.WelcomePage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;

public class CalendarFXSamplerWelcome extends WelcomePage {

    public CalendarFXSamplerWelcome() {
        super("CalendarFX", new Label(""));

        Label label = (Label) getContent();
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(50));
        label.setText("Welcome to the CalendarFX sampler. This application allows you to quickly browse through the "
                + "various controls that are available in this framework. In each sample you can play around with the "
                + "properties and controls shown on the right-hand side.");
    }

}
