/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.print;

import com.calendarfx.view.Messages;
import com.calendarfx.view.print.TimeRangeField;
import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

public class TimeRangeFieldSkin extends SkinBase<TimeRangeField> {

    private final DatePicker datePicker;
    private final Spinner<Integer> weekNumberSpinner;
    private final Spinner<Integer> monthYearSpinner;
    private final Spinner<Integer> afterUnitsSpinner;
    private final Label afterUnitsLabel;

    private final IntegerSpinnerValueFactory weekValueFactory;
    private final IntegerSpinnerValueFactory monthYearValueFactory;
    private final IntegerSpinnerValueFactory afterUnitsValueFactory;

    public TimeRangeFieldSkin(TimeRangeField control) {
        super(control);

        ComboBox<TimeRangeField.TimeRangeFieldValue> valuesComboBox = new ComboBox<>();
        valuesComboBox.setConverter(new TimeRangeFieldValueStringConverter());
        valuesComboBox.setItems(control.getValues());
        valuesComboBox.valueProperty().bindBidirectional(control.valueProperty());
        valuesComboBox.setVisibleRowCount(5);

        datePicker = new DatePicker();
        datePicker.getEditor().setPrefColumnCount(6);
        datePicker.valueProperty().bindBidirectional(control.onDateProperty());
        datePicker.managedProperty().bind(datePicker.visibleProperty());
        datePicker.setEditable(false);

        weekValueFactory = new IntegerSpinnerValueFactory(1, 52);
        weekValueFactory.valueProperty().addListener(obs -> control.setOnWeekNumber(weekValueFactory.getValue()));
        control.onWeekNumberProperty().addListener(obs -> {
            if (control.getOnWeekNumber() != null) {
                weekValueFactory.setValue(control.getOnWeekNumber());
            }
        });
        weekNumberSpinner = new Spinner<>();
        weekNumberSpinner.setValueFactory(weekValueFactory);
        weekNumberSpinner.managedProperty().bind(weekNumberSpinner.visibleProperty());
        weekNumberSpinner.setPrefWidth(70);

        monthYearValueFactory = new IntegerSpinnerValueFactory(1972, 3000);
        monthYearValueFactory.valueProperty().addListener(obs -> control.setMonthYear(monthYearValueFactory.getValue()));
        control.monthYearProperty().addListener(obs -> {
            if (control.getMonthYear() != null) {
                monthYearValueFactory.setValue(control.getMonthYear());
            }
        });

        monthYearSpinner = new Spinner<>();
        monthYearSpinner.getEditor().setPrefColumnCount(6);
        monthYearSpinner.setValueFactory(monthYearValueFactory);
        monthYearSpinner.managedProperty().bind(monthYearSpinner.visibleProperty());

        afterUnitsValueFactory = new IntegerSpinnerValueFactory(1, 500);
        afterUnitsValueFactory.valueProperty().addListener(obs -> control.setAfterUnits(afterUnitsValueFactory.getValue()));
        control.afterUnitsProperty().addListener(obs -> {
            if (control.getAfterUnits() != null) {
                afterUnitsValueFactory.setValue(control.getAfterUnits());
            }
        });
        afterUnitsSpinner = new Spinner<>();
        afterUnitsSpinner.getEditor().setPrefColumnCount(4);
        afterUnitsSpinner.setValueFactory(afterUnitsValueFactory);
        afterUnitsSpinner.managedProperty().bind(afterUnitsSpinner.visibleProperty());
        afterUnitsLabel = new Label();
        afterUnitsLabel.managedProperty().bind(afterUnitsLabel.visibleProperty());

        InvalidationListener listener = obs -> layout();
        control.viewTypeProperty().addListener(listener);
        control.valueProperty().addListener(listener);

        HBox container = new HBox(5, valuesComboBox, datePicker, weekNumberSpinner, monthYearSpinner, afterUnitsSpinner, afterUnitsLabel);
        container.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(valuesComboBox, Priority.ALWAYS);
        HBox.setHgrow(datePicker, Priority.SOMETIMES);

        getChildren().add(container);
        layout();
    }

    private void layout() {
        datePicker.setVisible(false);
        weekNumberSpinner.setVisible(false);
        monthYearSpinner.setVisible(false);
        afterUnitsSpinner.setVisible(false);
        afterUnitsLabel.setVisible(false);

        if (getSkinnable().getValue() == TimeRangeField.TimeRangeFieldValue.ON_DATE) {
            datePicker.setVisible(true);
        } else if (getSkinnable().getValue() == TimeRangeField.TimeRangeFieldValue.ON_WEEK_NUMBER) {
            weekNumberSpinner.setVisible(true);
            weekValueFactory.setValue(getSkinnable().getOnWeekNumber());
        } else if (getSkinnable().getValue().isMonthValue()) {
            monthYearSpinner.setVisible(true);
            monthYearValueFactory.setValue(getSkinnable().getMonthYear());
        } else if (getSkinnable().getValue() == TimeRangeField.TimeRangeFieldValue.AFTER) {
            afterUnitsLabel.setVisible(true);
            afterUnitsLabel.setText(Messages.getString(getSkinnable().getViewType().getPluralChronoMessageKey()));
            afterUnitsSpinner.setVisible(true);
            afterUnitsValueFactory.setValue(getSkinnable().getAfterUnits());
        }
    }

    private static class TimeRangeFieldValueStringConverter extends StringConverter<TimeRangeField.TimeRangeFieldValue> {

        @Override
        public String toString(TimeRangeField.TimeRangeFieldValue object) {
            return Messages.getString(object.getMessageKey());
        }

        @Override
        public TimeRangeField.TimeRangeFieldValue fromString(String string) {
            if (string != null) {
                for (TimeRangeField.TimeRangeFieldValue type : TimeRangeField.TimeRangeFieldValue.values()) {
                    if (string.equals(Messages.getString(type.getMessageKey()))) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

}
