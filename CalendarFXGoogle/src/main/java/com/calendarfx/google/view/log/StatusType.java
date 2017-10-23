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

package com.calendarfx.google.view.log;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.paint.Color;

/**
 * Status of the google task.
 * <p>
 * Created by gdiaz on 28/02/2017.
 */
public enum StatusType {

    SUCCEEDED {
        @Override
        public String getDisplayName() {
            return "Succeeded";
        }

        @Override
        public FontAwesomeIcon getIcon() {
            return FontAwesomeIcon.CHECK;
        }

        @Override
        public Color getColor() {
            return Color.GREEN;
        }
    },

    PENDING {
        @Override
        public String getDisplayName() {
            return "Pending";
        }

        @Override
        public FontAwesomeIcon getIcon() {
            return FontAwesomeIcon.EXCLAMATION_TRIANGLE;
        }

        @Override
        public Color getColor() {
            return Color.ORANGE;
        }
    },

    FAILED {
        @Override
        public String getDisplayName() {
            return "Failed";
        }

        @Override
        public FontAwesomeIcon getIcon() {
            return FontAwesomeIcon.CLOSE;
        }

        @Override
        public Color getColor() {
            return Color.RED;
        }
    },

    CANCELLED {
        @Override
        public String getDisplayName() {
            return "Cancelled";
        }

        @Override
        public FontAwesomeIcon getIcon() {
            return FontAwesomeIcon.EXCLAMATION_CIRCLE;
        }

        @Override
        public Color getColor() {
            return Color.BLUE;
        }
    };

    public abstract String getDisplayName();

    public abstract FontAwesomeIcon getIcon();

    public abstract Color getColor();

    public FontAwesomeIconView createView() {
        FontAwesomeIconView view = new FontAwesomeIconView(getIcon());
        view.setFill(getColor());
        return view;
    }
}
