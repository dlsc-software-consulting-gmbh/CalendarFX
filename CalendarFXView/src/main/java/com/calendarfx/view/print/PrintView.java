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

import com.calendarfx.model.CalendarSource;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.util.Util;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.Messages;
import com.calendarfx.view.SourceView;
import impl.com.calendarfx.view.print.PrintViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

import static java.util.Objects.requireNonNull;

/**
 * A print preview pane / dialog for CalendarFX. This view manages a {@link PrintablePage}
 * and binds it to the settingsView / properties that are made available via the {@link SettingsView}.
 * The default style class used by this view is "print-view".
 * <center><img width="100%" src="doc-files/print-view.png"></center>
 */
public class PrintView extends ViewTypeControl {

    private static final String DEFAULT_STYLE = "print-view";

    private final PreviewPane previewPane = new PreviewPane();

    private final SettingsView settingsView = new SettingsView();

    /**
     * Constructs a new print view.
     */
    public PrintView() {
        super();

        getStyleClass().add(DEFAULT_STYLE);

        final PaperView paperView = settingsView.getPaperView();
        final OptionsView optionsView = settingsView.getOptionsView();
        final TimeRangeView timeRangeView = settingsView.getTimeRangeView();
        final SourceView sourceView = settingsView.getSourceView();

        final PrintablePage printablePage = previewPane.getPrintablePage();

        paperView.viewTypeProperty().bindBidirectional(viewTypeProperty());
        timeRangeView.weekFieldsProperty().bind(weekFieldsProperty());
        timeRangeView.todayProperty().bind(todayProperty());
        Util.bindBidirectional(optionsView.showSwimlaneLayoutProperty(), layoutProperty(), LAYOUT_BOOLEAN_CONVERTER);

        printablePage.weekFieldsProperty().bind(weekFieldsProperty());

        printablePage.viewTypeProperty().bind(paperView.viewTypeProperty());
        printablePage.paperProperty().bind(paperView.paperProperty());
        printablePage.marginTypeProperty().bind(paperView.marginTypeProperty());
        printablePage.bottomMarginProperty().bind(paperView.bottomMarginProperty());
        printablePage.leftMarginProperty().bind(paperView.leftMarginProperty());
        printablePage.rightMarginProperty().bind(paperView.rightMarginProperty());
        printablePage.topMarginProperty().bind(paperView.topMarginProperty());

        printablePage.printStartDateProperty().bind(timeRangeView.startDateProperty());
        printablePage.printEndDateProperty().bind(timeRangeView.endDateProperty());
        printablePage.showAllDayEntriesProperty().bind(optionsView.showAllDayEntriesProperty());
        printablePage.showMiniCalendarsProperty().bind(optionsView.showMiniCalendarsProperty());
        printablePage.showCalendarKeysProperty().bind(optionsView.showCalendarKeysProperty());
        printablePage.showTimedEntriesProperty().bind(optionsView.showTimedEntriesProperty());
        printablePage.showEntryDetailsProperty().bind(optionsView.showEntryDetailsProperty());
        printablePage.layoutProperty().bindBidirectional(layoutProperty());

        Bindings.bindContent(sourceView.getCalendarSources(), getCalendarSources());
        Bindings.bindContent(sourceView.getCalendarVisibilityMap(), printablePage.getCalendarVisibilityMap());

        Bindings.bindContent(printablePage.getCalendarSources(), getCalendarSources());
    }

    private final ObservableList<CalendarSource> calendarSources = FXCollections.observableArrayList();

    /**
     * The list of all calendar sources attached to this control.
     *
     * @return the calendar sources
     */
    public final ObservableList<CalendarSource> getCalendarSources() {
        return calendarSources;
    }


    private final ObjectProperty<LocalDate> today = new SimpleObjectProperty<>(this, "today", LocalDate.now()); //$NON-NLS-1$

    /**
     * Stores the date that is considered to represent "today". This property is
     * initialized with {@link LocalDate#now()} but can be any date.
     *
     * @return the date representing "today"
     */
    public final ObjectProperty<LocalDate> todayProperty() {
        return today;
    }

    /**
     * Sets the value of {@link #todayProperty()}.
     *
     * @param date the date representing "today"
     */
    public final void setToday(LocalDate date) {
        requireNonNull(date);
        todayProperty().set(date);
    }

    /**
     * Returns the value of {@link #todayProperty()}.
     *
     * @return the date representing "today"
     */
    public final LocalDate getToday() {
        return todayProperty().get();
    }

    private final ObjectProperty<DateControl.Layout> layout = new SimpleObjectProperty<>(this, "layout", DateControl.Layout.STANDARD); //$NON-NLS-1$

    /**
     * Stores the strategy used by the view to layout the entries of several
     * calendars at once. The standard layout ignores the source calendar of an
     * entry and finds the next available place in the UI that satisfies the
     * time bounds of the entry. The {@link DateControl.Layout#SWIMLANE} strategy allocates
     * a separate column for each calendar and resolves overlapping entry
     * conflicts within that column. Swim lanes are especially useful for
     * resource booking systems (rooms, people, trucks).
     *
     * @return the layout strategy of the view
     */
    public final ObjectProperty<DateControl.Layout> layoutProperty() {
        return layout;
    }

    /**
     * Sets the value of {@link #layoutProperty()}.
     *
     * @param layout the layout
     */
    public final void setLayout(DateControl.Layout layout) {
        requireNonNull(layout);
        layoutProperty().set(layout);
    }

    /**
     * Returns the value of {@link #layoutProperty()}.
     *
     * @return the layout strategy
     */
    public final DateControl.Layout getLayout() {
        return layoutProperty().get();
    }

    private final ObjectProperty<WeekFields> weekFields = new SimpleObjectProperty<>(this, "weekFields", WeekFields.ISO); //$NON-NLS-1$

    /**
     * Week fields are used to determine the first day of a week (e.g. "Monday"
     * in Germany or "Sunday" in the US). It is also used to calculate the week
     * number as the week fields determine how many days are needed in the first
     * week of a year. This property is initialized with {@link WeekFields#ISO}.
     *
     * @return the week fields
     */
    public final ObjectProperty<WeekFields> weekFieldsProperty() {
        return weekFields;
    }

    /**
     * Sets the value of {@link #weekFieldsProperty()}.
     *
     * @param weekFields the new week fields
     */
    public final void setWeekFields(WeekFields weekFields) {
        requireNonNull(weekFields);
        weekFieldsProperty().set(weekFields);
    }

    /**
     * Returns the value of {@link #weekFieldsProperty()}.
     *
     * @return the week fields
     */
    public final WeekFields getWeekFields() {
        return weekFieldsProperty().get();
    }

    /**
     * A convenience method to lookup the first day of the week ("Monday" in
     * Germany, "Sunday" in the US). This method delegates to
     * {@link WeekFields#getFirstDayOfWeek()}.
     *
     * @return the first day of the week
     * @see #weekFieldsProperty()
     */
    public final DayOfWeek getFirstDayOfWeek() {
        return getWeekFields().getFirstDayOfWeek();
    }

    public final void requestStartDate(LocalDate date) {
        settingsView.getTimeRangeView().requestStartDate(date);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PrintViewSkin(this);
    }

    /**
     * Returns the preview pane sub control.
     *
     * @return the preview pane control
     */
    public final PreviewPane getPreviewPane() {
        return previewPane;
    }

    /**
     * Returns the settings view sub control.
     *
     * @return the settings view
     */
    public final SettingsView getSettingsView() {
        return settingsView;
    }

    private final ObjectProperty<EventHandler<ActionEvent>> onContinue = new SimpleObjectProperty<>(this, "onContinue", evt -> doPrint());

    /**
     * Stores an event handler that will be invoked when the user clicks on the "continue" button.
     * The default event handler invokes the {@link #doPrint()} method.
     *
     * @return the event handler used by the "continue" button
     */
    public final ObjectProperty<EventHandler<ActionEvent>> onContinueProperty() {
        return onContinue;
    }

    /**
     * Returns the value of the {@link #onContinueProperty()}.
     *
     * @return the event handler invoked by the "continue" button.
     */
    public final EventHandler<ActionEvent> getOnContinue() {
        return onContinueProperty().get();
    }

    /**
     * Sets the value of the {@link #onContinueProperty()}.
     *
     * @param handler the event handler invoked by the "continue" button.
     */
    public final void setOnContinue(EventHandler<ActionEvent> handler) {
        onContinueProperty().set(handler);
    }

    private final ObjectProperty<EventHandler<ActionEvent>> onCancel = new SimpleObjectProperty<>(this, "onCancel", evt -> hide());

    /**
     * Stores an event handler that will be invoked when the user clicks on the "cancel" button.
     * The default event handler invokes the {@link #hide()} method.
     *
     * @return the event handler used by the "cancel" button
     */
    public final ObjectProperty<EventHandler<ActionEvent>> onCancelProperty() {
        return onCancel;
    }

    /**
     * Returns the value of the {@link #onCancelProperty()}.
     *
     * @return the event handler invoked by the "continue" button.
     */
    public final EventHandler<ActionEvent> getOnCancel() {
        return onCancelProperty().get();
    }

    /**
     * Sets the value of the {@link #onCancelProperty()}.
     *
     * @param handler the event handler invoked by the "cancel" button.
     */
    public final void setOnCancel(EventHandler<ActionEvent> handler) {
        onCancelProperty().set(handler);
    }

    private Stage dialog;

    /**
     * Creates an application-modal dialog and shows it after adding the print view to it.
     *
     * @param owner the owner window of the dialog
     */
    public final void show(Window owner) {
        if (dialog != null) {
            dialog.show();
        } else {
            Scene scene = new Scene(this);
            dialog = new Stage();
            dialog.initOwner(owner);
            dialog.setScene(scene);
            dialog.sizeToScene();
            dialog.centerOnScreen();
            dialog.setTitle(Messages.getString("PrintView.TITLE_LABEL"));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.show();
        }
    }

    /**
     * Hides the dialog.
     */
    public final void hide() {
        if (dialog != null) {
            dialog.hide();
        }
    }

    /**
     * Performs the actual printing of the calendars.
     */
    protected final void doPrint() {
        hide();

        PrintablePage pageInView = previewPane.getPrintablePage();
        PrintablePage pageToPrint = new PrintablePage();

        try {
            pageInView.bindPage(pageToPrint);

            Printer printer = Printer.getDefaultPrinter();

            LoggingDomain.PRINTING.fine("printer = " + printer);

            PageLayout layout = null;

            final Paper paper = pageInView.getPaper();
            final PageOrientation pageOrientation = pageInView.getViewType().getPageOrientation();
            final PaperView.MarginType marginType = pageInView.getMarginType();

            LoggingDomain.PRINTING.fine("paper = " + paper);
            LoggingDomain.PRINTING.fine("pageOrientation = " + pageOrientation);
            LoggingDomain.PRINTING.fine("marginType = " + marginType);
            LoggingDomain.PRINTING.fine("custom margins = left: " + pageInView.getLeftMargin() + ", right: " + pageInView.getRightMargin() + ", top: " + pageInView.getTopMargin() + ", bottom: " + pageInView.getBottomMargin());

            switch (marginType) {
                case DEFAULT:
                    layout = printer.createPageLayout(
                            paper,
                            pageOrientation,
                            Printer.MarginType.DEFAULT);
                    break;
                case MINIMUM:
                    layout = printer.createPageLayout(
                            paper,
                            pageOrientation,
                            Printer.MarginType.HARDWARE_MINIMUM);
                    break;
                case CUSTOM:
                    layout = printer.createPageLayout(
                            paper,
                            pageOrientation,
                            pageInView.getLeftMargin(),
                            pageInView.getRightMargin(),
                            pageInView.getTopMargin(),
                            pageInView.getBottomMargin());
                    break;
            }


            // sizes of print page and physical page
            double pageWidth = pageToPrint.prefWidth(-1);
            double pageHeight = pageToPrint.prefHeight(-1);

            double printableWidth = layout.getPrintableWidth();
            double printableHeight = layout.getPrintableHeight();

            // scaling
            double scaleX = printableWidth / pageWidth;
            double scaleY = printableHeight / pageHeight;
            double scale = Math.min(scaleX, scaleY);

            LoggingDomain.PRINTING.fine("pageWidth / pageHeight = " + pageWidth + " / " + pageHeight);
            LoggingDomain.PRINTING.fine("printableWidth / printableHeight = " + printableWidth + " / " + printableHeight);
            LoggingDomain.PRINTING.fine("scaleX / scaleY = " + scaleX + " / " + scaleY);
            LoggingDomain.PRINTING.fine("scale = " + scale);

            pageToPrint.applyCss();
            pageToPrint.layout();
            pageToPrint.getTransforms().add(new Scale(scale, scale));

            // transformation = center
            final double translateX = (printableWidth - (pageWidth * scale)) / 2;
            final double translateY = (printableHeight - (pageHeight * scale)) / 2;

            LoggingDomain.PRINTING.fine("translateX / translateY = " + translateX + " / " + translateY);

            pageToPrint.setTranslateX(translateX);
            pageToPrint.setTranslateY(translateY);

            PrinterJob job = PrinterJob.createPrinterJob(printer);
            JobSettings settings = job.getJobSettings();
            settings.setJobName(Messages.getString("PrintView.TITLE_LABEL"));
            settings.setPageLayout(layout);

            if (job.showPrintDialog(getScene().getWindow())) {
                do {
                    boolean success = job.printPage(pageToPrint);
                    if (!success) {
                        break;
                    }
                }
                while (pageToPrint.next());

                job.endJob();
            }
        } finally {
            pageInView.unbindPage(pageToPrint);
        }
    }

    private static final Util.Converter<Boolean, DateControl.Layout> LAYOUT_BOOLEAN_CONVERTER = new Util.Converter<Boolean, DateControl.Layout>() {

        @Override
        public Boolean toLeft(DateControl.Layout right) {
            return right == DateControl.Layout.SWIMLANE;
        }

        @Override
        public DateControl.Layout toRight(Boolean left) {
            return Boolean.TRUE.equals(left) ? DateControl.Layout.SWIMLANE : DateControl.Layout.STANDARD;
        }
    };

}
