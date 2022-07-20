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

import com.calendarfx.view.Messages;
import com.calendarfx.view.print.TimeRangeField;

import com.calendarfx.view.print.TimeRangeField.TimeRangeFieldValue;
import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Spinner;
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

        ComboBox<TimeRangeFieldValue> valuesComboBox = new ComboBox<>();
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

        afterUnitsLabel = new Label();
        afterUnitsLabel.managedProperty().bind(afterUnitsLabel.visibleProperty());
        afterUnitsValueFactory = new IntegerSpinnerValueFactory(1, 500);
        afterUnitsValueFactory.valueProperty().addListener(obs -> control.setAfterUnits(afterUnitsValueFactory.getValue()));
        control.afterUnitsProperty().addListener(obs -> {
            if (control.getAfterUnits() != null) {
                afterUnitsValueFactory.setValue(control.getAfterUnits());
                afterUnitsLabel.setText(control.getAfterUnits().equals(1)
                        ? Messages.getString(getSkinnable().getViewType()
                                .getSingularChronoMessageKey())
                        : Messages.getString(getSkinnable().getViewType()
                                .getPluralChronoMessageKey()));
            }
        });
        afterUnitsSpinner = new Spinner<>();
        afterUnitsSpinner.getEditor().setPrefColumnCount(4);
        afterUnitsSpinner.setValueFactory(afterUnitsValueFactory);
        afterUnitsSpinner.managedProperty().bind(afterUnitsSpinner.visibleProperty());

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

        if (getSkinnable().getValue() == TimeRangeFieldValue.ON_DATE) {
            datePicker.setVisible(true);
        } else if (getSkinnable().getValue() == TimeRangeFieldValue.ON_WEEK_NUMBER) {
            weekNumberSpinner.setVisible(true);
            weekValueFactory.setValue(getSkinnable().getOnWeekNumber());
        } else if (getSkinnable().getValue().isMonthValue()) {
            monthYearSpinner.setVisible(true);
            monthYearValueFactory.setValue(getSkinnable().getMonthYear());
        } else if (getSkinnable().getValue() == TimeRangeFieldValue.AFTER) {
            afterUnitsLabel.setVisible(true);
            afterUnitsLabel.setText(Messages.getString(getSkinnable().getViewType().getSingularChronoMessageKey()));
            afterUnitsSpinner.setVisible(true);
            afterUnitsValueFactory.setValue(getSkinnable().getAfterUnits());
        }
    }

    private static class TimeRangeFieldValueStringConverter extends StringConverter<TimeRangeFieldValue> {

        @Override
        public String toString(TimeRangeFieldValue value) {
            if (value != null) {
                return Messages.getString(value.getMessageKey());
            }
            return "";
        }

        @Override
        public TimeRangeFieldValue fromString(String string) {
            if (string != null) {
                for (TimeRangeFieldValue type : TimeRangeFieldValue.values()) {
                    if (string.equals(Messages.getString(type.getMessageKey()))) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

}
