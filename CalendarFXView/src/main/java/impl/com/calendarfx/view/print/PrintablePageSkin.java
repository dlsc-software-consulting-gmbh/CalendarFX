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

package impl.com.calendarfx.view.print;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.calendarfx.view.Messages;
import com.calendarfx.view.SourceGridView;
import com.calendarfx.view.YearMonthView;
import com.calendarfx.view.print.PrintablePage;
import com.calendarfx.view.print.ViewType;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PrintablePageSkin extends SkinBase<PrintablePage> {

    private final YearMonthView calendarOne = new YearMonthView();
    private final YearMonthView calendarTwo = new YearMonthView();

    public PrintablePageSkin(PrintablePage control) {
        super(control);

        calendarOne.setShowTodayButton(false);
        calendarOne.setShowMonthArrows(false);
        calendarOne.setShowYearArrows(false);
        calendarOne.setShowWeekNumbers(false);
        calendarOne.setShowToday(false);
        calendarOne.weekFieldsProperty().bind(control.weekFieldsProperty());
        calendarOne.visibleProperty().bind(control.showMiniCalendarsProperty());
        calendarOne.dateProperty().bind(control.pageStartDateProperty());
        calendarOne.managedProperty().bind(calendarOne.visibleProperty());

        calendarTwo.setShowTodayButton(false);
        calendarTwo.setShowMonthArrows(false);
        calendarTwo.setShowYearArrows(false);
        calendarTwo.setShowWeekNumbers(false);
        calendarTwo.setShowToday(false);
        calendarTwo.weekFieldsProperty().bind(control.weekFieldsProperty());
        calendarTwo.dateProperty()
                .bind(Bindings.createObjectBinding(
                        () -> calendarOne.getDate().plusMonths(1),
                        calendarOne.dateProperty()));
        calendarTwo.visibleProperty().bind(control.showMiniCalendarsProperty());
        calendarTwo.managedProperty().bind(calendarTwo.visibleProperty());

        SourceGridView sourceView = new SourceGridView();
        sourceView.visibleProperty().bind(control.showCalendarKeysProperty());
        sourceView.managedProperty().bind(sourceView.visibleProperty());
        sourceView.bind(control);

        PrintPagePeriodFormatter formatter = new PrintPagePeriodFormatter(
                control);
        
        Label periodLabel = new Label();
        periodLabel.textProperty().bind(formatter.textProperty());
        periodLabel.getStyleClass().add("period-label");

        VBox titleSection = new VBox();
        titleSection.getChildren().addAll(periodLabel, sourceView);
        titleSection.getStyleClass().add("title-section");

        HBox calendarsBox = new HBox(calendarOne, calendarTwo);
        calendarsBox.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
        calendarsBox.getStyleClass().add("mini-calendars");

        BorderPane header = new BorderPane();
        header.setCenter(titleSection);
        header.setRight(calendarsBox);
        header.getStyleClass().add("header");

        BorderPane container = new BorderPane();
        container.setTop(header);
        container.centerProperty().bind(control.viewProperty());
        container.getStyleClass().add("container");
        getChildren().add(container);

        InvalidationListener selectedDatesListener = obs -> updateSelectedDates();
        control.pageStartDateProperty().addListener(selectedDatesListener);
        control.pageEndDateProperty().addListener(selectedDatesListener);
        updateSelectedDates();

        Region glassPane = new Region();
        glassPane.prefWidthProperty().bind(getSkinnable().widthProperty());
        glassPane.prefHeightProperty().bind(getSkinnable().heightProperty());
        glassPane.getStyleClass().add("glasspane");
        glassPane.setMouseTransparent(false);
        getChildren().add(glassPane);
    }

    private void updateSelectedDates() {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate start = getSkinnable().getPageStartDate();

        do {
            dates.add(start);
            start = start.plusDays(1);
        } while (start.isBefore(getSkinnable().getPageEndDate())
                || start.isEqual(getSkinnable().getPageEndDate()));

        calendarOne.getSelectedDates().clear();
        calendarOne.getSelectedDates().addAll(dates);

        calendarTwo.getSelectedDates().clear();
        calendarTwo.getSelectedDates().addAll(dates);
    }

    private static final class PrintPagePeriodFormatter
            implements InvalidationListener {

        private final PrintablePage page;
        private final ObjectProperty<Map<ViewType, DateTimeFormatter>> formatterMapProperty = new SimpleObjectProperty<>(
                this, "formatterMapProperty");

        private PrintPagePeriodFormatter(PrintablePage page) {
            this.page = page;
            page.viewTypeProperty().addListener(this);
            page.pageStartDateProperty().addListener(this);
            page.pageEndDateProperty().addListener(this);
            formatterMapProperty.bind(page.formatterMapProperty());
            format();
        }

        private final ReadOnlyStringWrapper text = new ReadOnlyStringWrapper(
                this, "text");

        public final ReadOnlyStringProperty textProperty() {
            return text.getReadOnlyProperty();
        }

        private void setText(String text) {
            this.text.set(text);
        }

        private void format() {
            DateTimeFormatter formatter = getFormatterMap()
                    .get(page.getViewType());
            if (formatter == null) {
                formatter = page.getViewType().getDateTimeFormatter();
            }

            switch (page.getViewType()) {
            case DAY_VIEW:
            case MONTH_VIEW:
                setText(formatter.format(page.getPageStartDate()));
                break;

            case WEEK_VIEW:
                StringBuilder sb = new StringBuilder();
                sb.append(formatter.format(page.getPageStartDate()));
                sb.append(" ")
                        .append(Messages.getString("PrintViewType.TO_LABEL"))
                        .append(" ");
                sb.append(formatter.format(page.getPageEndDate()));
                setText(sb.toString());
                break;
            default:
                setText("");
                break;
            }
        }

        @Override
        public void invalidated(Observable observable) {
            format();
        }

        public ObjectProperty<Map<ViewType, DateTimeFormatter>> dateTimeFormatterMapProperty() {
            return formatterMapProperty;
        }

        private Map<ViewType, DateTimeFormatter> getFormatterMap() {
            if (dateTimeFormatterMapProperty().get() == null) {
                formatterMapProperty.set(new HashMap<>());
            }
            return dateTimeFormatterMapProperty().get();
        }

    }

}
