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

package com.calendarfx.demo.print;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.print.PaperView;
import javafx.scene.Node;

public class HelloPaperView extends CalendarFXSample {

    @Override
    protected Node createControl() {
        return new PaperView();
    }

    @Override
    protected Class<?> getJavaDocClass() {
        return PaperView.class;
    }

    @Override
    public String getSampleName() {
        return "Paper View";
    }

    @Override
    public String getSampleDescription() {
        return "This control allows to select the view that is going to be printed and configure the paper type and print margins.";
    }

    public static void main(String[] args) {
        launch(args);
    }

}
