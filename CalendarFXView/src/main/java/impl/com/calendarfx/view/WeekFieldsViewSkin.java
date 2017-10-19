/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.view.WeekFieldsView;
import javafx.beans.InvalidationListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;

import java.time.DayOfWeek;
import java.time.temporal.WeekFields;

public class WeekFieldsViewSkin extends SkinBase<WeekFieldsView> {

    private ComboBox<DayOfWeek> dayOfWeekComboBox;
    private ComboBox<Integer> minimalDaysInFirstWeekComboBox;

    public WeekFieldsViewSkin(final WeekFieldsView view) {
        super(view);

        dayOfWeekComboBox = new ComboBox<>();
        dayOfWeekComboBox.getItems().addAll(DayOfWeek.values());
        dayOfWeekComboBox.setValue(view.getFirstDayOfWeek());

        minimalDaysInFirstWeekComboBox = new ComboBox<>();
        minimalDaysInFirstWeekComboBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7);
        minimalDaysInFirstWeekComboBox.setValue(view.getMinimalDaysInFirstWeek());

        GridPane pane = new GridPane();
        pane.getStyleClass().add("content");
        pane.add(new Label("First day:"), 0, 0);
        pane.add(new Label("Minimum days:"), 0, 1);
        pane.add(dayOfWeekComboBox, 1, 0);
        pane.add(minimalDaysInFirstWeekComboBox, 1, 1);

        getChildren().add(pane);

        // listeners

        InvalidationListener updateListener = it -> updateValues();
        dayOfWeekComboBox.valueProperty().addListener(updateListener);
        minimalDaysInFirstWeekComboBox.valueProperty().addListener(updateListener);

        view.weekFieldsProperty().addListener(it -> {
            WeekFields fields = view.getWeekFields();
            dayOfWeekComboBox.setValue(fields.getFirstDayOfWeek());
            minimalDaysInFirstWeekComboBox.setValue(fields.getMinimalDaysInFirstWeek());
        });
    }

    private void updateValues() {
        DayOfWeek dayOfWeek = dayOfWeekComboBox.getValue();
        Integer minimumNumberOfDays = minimalDaysInFirstWeekComboBox.getValue();
        getSkinnable().setWeekFields(WeekFields.of(dayOfWeek, minimumNumberOfDays));
    }
}
