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

package impl.com.calendarfx.view.page;

import com.calendarfx.view.AgendaView;
import com.calendarfx.view.AllDayView;
import com.calendarfx.view.DetailedDayView;
import com.calendarfx.view.YearMonthView;
import com.calendarfx.view.page.DayPage;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

import java.time.LocalDate;

public class DayPageSkin extends PageBaseSkin<DayPage> {

    private Label todayLabel;

    private YearMonthView yearMonthView;

    private Node leftSide;

    private Node rightSide;

    private ColumnConstraints leftColumn;

    private ColumnConstraints rightColumn;

    private GridPane gridPane;

    public DayPageSkin(DayPage view) {
        super(view);

        yearMonthView.getSelectedDates().addListener((Observable evt) -> {
            if (yearMonthView.getSelectedDates().size() == 1) {
                LocalDate date = yearMonthView.getSelectedDates().iterator().next();
                getSkinnable().setDate(date);
            }
        });

        updateView();

        view.dateProperty().addListener(evt -> updateView());

        view.dayPageLayoutProperty().addListener(it -> updateLayout());
        updateLayout();
    }

    private void updateView() {
        LocalDate date = getSkinnable().getDate();
        todayLabel.setText(Long.toString(date.getDayOfMonth()));

        yearMonthView.getSelectedDates().clear();
        yearMonthView.getSelectedDates().add(date);
    }

    @Override
    protected Node createContent() {
        leftSide = createLeftHandSide();
        rightSide = createRightHandSide();

        leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(50);
        leftColumn.setMinWidth(Region.USE_COMPUTED_SIZE);
        leftColumn.setPrefWidth(Region.USE_COMPUTED_SIZE);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        leftColumn.setFillWidth(true);

        rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(50);
        rightColumn.setMinWidth(Region.USE_COMPUTED_SIZE);
        rightColumn.setPrefWidth(Region.USE_COMPUTED_SIZE);
        rightColumn.setMaxWidth(Double.MAX_VALUE);
        rightColumn.setFillWidth(true);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(100);
        rowConstraints.setFillHeight(true);

        // no need to assign a style class, will be auto-assigned by superclass ("content")
        gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.getColumnConstraints().addAll(leftColumn, rightColumn);
        gridPane.getRowConstraints().addAll(rowConstraints);

        gridPane.add(leftSide, 0, 0);
        gridPane.add(rightSide, 1, 0);

        getSkinnable().widthProperty().addListener(it -> updateLayout());

        return gridPane;
    }

    private void updateLayout() {
        final DayPage page = getSkinnable();

        switch (page.getDayPageLayout()) {
            case STANDARD:
                final Insets insets = page.getInsets();
                if (page.getWidth() - insets.getLeft() - insets.getRight() < leftSide.prefWidth(-1) + rightSide.prefWidth(-1)) {
                    leftSide.setVisible(false);
                    rightSide.setVisible(true);
                    leftColumn.setPercentWidth(0);
                    rightColumn.setPercentWidth(100);
                } else {
                    leftSide.setVisible(true);
                    rightSide.setVisible(true);
                    leftColumn.setPercentWidth(50);
                    rightColumn.setPercentWidth(50);
                }
                gridPane.setHgap(20);
                break;
            case AGENDA_ONLY:
                leftSide.setVisible(true);
                rightSide.setVisible(false);
                leftColumn.setPercentWidth(100);
                rightColumn.setPercentWidth(0);
                gridPane.setHgap(0);
                break;
            case DAY_ONLY:
                leftSide.setVisible(false);
                rightSide.setVisible(true);
                leftColumn.setPercentWidth(0);
                rightColumn.setPercentWidth(100);
                gridPane.setHgap(0);
                break;
        }
    }

    protected Node createLeftHandSide() {
        DayPage dayPage = getSkinnable();

        // today label
        todayLabel = new Label();
        todayLabel.getStyleClass().add("today-label");
        todayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        todayLabel.setAlignment(Pos.TOP_LEFT);
        todayLabel.setTextOverrun(OverrunStyle.CLIP);
        todayLabel.setMinWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(todayLabel, Priority.ALWAYS);

        // year month view
        yearMonthView = dayPage.getYearMonthView();
        yearMonthView.setSelectionMode(SelectionMode.SINGLE);
        yearMonthView.setShowMonth(false);
        yearMonthView.setShowYear(false);
        yearMonthView.setShowTodayButton(false);
        HBox.setHgrow(yearMonthView, Priority.NEVER);
        getSkinnable().bind(yearMonthView, true);

        HBox header = new HBox(10);
        header.setFillHeight(true);
        header.getChildren().addAll(todayLabel, yearMonthView);
        header.getStyleClass().add("header");

        AgendaView agendaView = dayPage.getAgendaView();
        getSkinnable().bind(agendaView, false);
        agendaView.dateProperty().bind(dayPage.todayProperty());

        HBox.setMargin(agendaView, new Insets(10, 0, 0, 0));

        BorderPane leftHandSide = new BorderPane();
        leftHandSide.getStyleClass().add("left-side");
        leftHandSide.setTop(header);
        leftHandSide.setCenter(agendaView);
        HBox.setHgrow(leftHandSide, Priority.ALWAYS);
        return leftHandSide;
    }

    protected Node createRightHandSide() {
        DayPage dayPage = getSkinnable();

        // the day view
        DetailedDayView dayView = dayPage.getDetailedDayView();

        getSkinnable().bind(dayView, true);

        AllDayView allDayView = dayView.getAllDayView();
        allDayView.showTodayProperty().unbindBidirectional(dayView.showTodayProperty()); // we need control over this

        return dayView;
    }
}
