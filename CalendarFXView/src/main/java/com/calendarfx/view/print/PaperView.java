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

import impl.com.calendarfx.view.print.PaperViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterAttributes;
import javafx.scene.control.Skin;

import java.util.Objects;
import java.util.Set;

/**
 * A control for specifying the paper size, the view type (day, week, month),
 * and the print margins. The default style class of this view is "paper-view".
 *
 * <center><img src="doc-files/paper-view.png"></center>
 */
public class PaperView extends ViewTypeControl {

    private static final String DEFAULT_STYLE = "paper-view";

    /**
     * Possible print margin types: default, minimum, custom.
     */
    public enum MarginType {

        /**
         * Used to request that the minimum margins supported by the printer
         * shall be used for printing.
         */
        MINIMUM(Printer.MarginType.HARDWARE_MINIMUM),

        /**
         * Used to request that the default margins of the printer
         * shall be used for printing.
         */
        DEFAULT(Printer.MarginType.DEFAULT),

        /**
         * Used to request that custom margins shall be used for printing.
         */
        CUSTOM(null);

        Printer.MarginType type;

        MarginType(Printer.MarginType type) {
            this.type = type;
        }

        public Printer.MarginType getPrinterMarginType() {
            return type;
        }
    }

    /**
     * Constructs a new paper setup control.
     */
    public PaperView() {
        super();
        getStyleClass().add(DEFAULT_STYLE);

        Printer defaultPrinter = Printer.getDefaultPrinter();
        if (defaultPrinter != null) {
            PrinterAttributes printerAttributes = defaultPrinter.getPrinterAttributes();
            if (printerAttributes != null) {
                Set<Paper> supportedPapers = printerAttributes.getSupportedPapers();
                if (supportedPapers != null) {
                    getAvailablePapers().setAll(supportedPapers);
                }
            }
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PaperViewSkin(this);
    }

    // paper support

    private final ObjectProperty<Paper> paper = new SimpleObjectProperty<>(this, "paper", Paper.A4);

    /**
     * A property used to store the currently selected paper size.
     *
     * @return the paper
     */
    public final ObjectProperty<Paper> paperProperty() {
        return paper;
    }

    /**
     * Returns the value of {@link #paperProperty()}.
     *
     * @return the paper
     */
    public final Paper getPaper() {
        return paperProperty().get();
    }

    /**
     * Sets the value of {@link #paperProperty()}.
     *
     * @param paper the paper
     */
    public final void setPaper(Paper paper) {
        paperProperty().set(paper);
    }

    // available papers support

    private final ObservableList<Paper> availablePapers = FXCollections.observableArrayList();

    /**
     * Returns the available paper sizes.
     *
     * @return the paper sizes
     */
    public final ObservableList<Paper> getAvailablePapers() {
        return availablePapers;
    }

    // margin type support

    private final ObjectProperty<MarginType> marginType = new SimpleObjectProperty<MarginType>(this, "marginType", MarginType.DEFAULT) {
        @Override
        public void set(MarginType newValue) {
            super.set(Objects.requireNonNull(newValue));
        }
    };

    /**
     * A property used to store the currently requested margin type (custom, minimum, or default).
     *
     * @return the requested margin types
     */
    public final ObjectProperty<MarginType> marginTypeProperty() {
        return marginType;
    }

    /**
     * Returns the value of {@link #marginTypeProperty()}.
     *
     * @return the margin type (custom, default, minimum)
     */
    public final MarginType getMarginType() {
        return marginTypeProperty().get();
    }

    /**
     * Sets the value of {@link #marginTypeProperty()}.
     *
     * @param type the margin type (custom, default, minimum)
     */
    public final void setMarginType(MarginType type) {
        marginTypeProperty().set(type);
    }

    // top margin support

    private final DoubleProperty topMargin = new SimpleDoubleProperty(this, "topMargin") {
        @Override
        public void set(double newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("The margin is invalid: " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * Stores the top print margin value.
     *
     * @return the top margin
     */
    public final DoubleProperty topMarginProperty() {
        return topMargin;
    }

    /**
     * Returns the value of the {@link #topMarginProperty()}.
     *
     * @return the top margin
     */
    public final double getTopMargin() {
        return topMarginProperty().get();
    }

    /**
     * Sets the value of the {@link #topMarginProperty()}.
     *
     * @param margin the top margin
     */
    public final void setTopMargin(double margin) {
        topMarginProperty().set(margin);
    }

    // right margin support

    private final DoubleProperty rightMargin = new SimpleDoubleProperty(this, "rightMargin") {
        @Override
        public void set(double newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("The margin is invalid: " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * Stores the right print margin value.
     *
     * @return the right margin
     */
    public final DoubleProperty rightMarginProperty() {
        return rightMargin;
    }

    /**
     * Returns the value of the {@link #rightMarginProperty()}.
     *
     * @return the right margin
     */
    public final double getRightMargin() {
        return rightMarginProperty().get();
    }

    /**
     * Sets the value of the {@link #rightMarginProperty()}.
     *
     * @param margin the right margin
     */
    public final void setRightMargin(double margin) {
        rightMarginProperty().set(margin);
    }

    // bottom margin support

    private final DoubleProperty bottomMargin = new SimpleDoubleProperty(this, "bottomMargin") {
        @Override
        public void set(double newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("The margin is invalid: " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * Stores the bottom print margin value.
     *
     * @return the bottom margin
     */
    public final DoubleProperty bottomMarginProperty() {
        return bottomMargin;
    }

    /**
     * Returns the value of the {@link #bottomMarginProperty()}.
     *
     * @return the bottom margin
     */
    public final double getBottomMargin() {
        return bottomMarginProperty().get();
    }

    /**
     * Sets the value of the {@link #bottomMarginProperty()}.
     *
     * @param margin the bottom margin
     */
    public final void setBottomMargin(double margin) {
        bottomMarginProperty().set(margin);
    }

    // left margin support

    private final DoubleProperty leftMargin = new SimpleDoubleProperty(this, "leftMargin") {
        @Override
        public void set(double newValue) {
            if (newValue < 0) {
                throw new IllegalArgumentException("The margin is invalid: " + newValue);
            }
            super.set(newValue);
        }
    };

    /**
     * Stores the left print margin value.
     *
     * @return the left margin
     */
    public final DoubleProperty leftMarginProperty() {
        return leftMargin;
    }

    /**
     * Returns the value of the {@link #leftMarginProperty()}.
     *
     * @return the left margin
     */
    public final double getLeftMargin() {
        return leftMarginProperty().get();
    }

    /**
     * Sets the value of the {@link #leftMarginProperty()}.
     *
     * @param margin the left margin
     */
    public final void setLeftMargin(double margin) {
        leftMarginProperty().set(margin);
    }
}
