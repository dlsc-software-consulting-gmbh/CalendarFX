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

package impl.com.calendarfx.view;

import com.calendarfx.view.TimeField;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalTime;

@SuppressWarnings("javadoc")
public class TimeFieldSkin extends SkinBase<TimeField> {

    private final NumericTextField hourField;
    private final NumericTextField minuteField;

    public TimeFieldSkin(TimeField field) {
        super(field);

        StringConverter<String> valueConverter = new StringConverter<>() {

            @Override
            public String fromString(String text) {
                if (text.length() == 0) {
                    return "00";
                } else if (text.length() == 1) {
                    return "0" + text;
                }

                return text;
            }

            @Override
            public String toString(String text) {
                return text;
            }
        };

        TextFormatter<String> hourFormatter = new TextFormatter<>(valueConverter, "0");
        TextFormatter<String> minuteFormatter = new TextFormatter<>(valueConverter, "0");

        hourField = new NumericTextField(23);
        hourField.setOnKeyPressed(new RollingHandler(hourField, 23));
        hourField.setTextFormatter(hourFormatter);

        minuteField = new NumericTextField(59);
        minuteField.setOnKeyPressed(new RollingHandler(minuteField, 59));
        minuteField.setTextFormatter(minuteFormatter);

        Label separator = new Label(":");
        separator.setMaxHeight(Double.MAX_VALUE);

        HBox box = new HBox();
        box.getStyleClass().add("box");
        box.setFillHeight(true);
        box.getChildren().addAll(hourField, separator, minuteField);

        getChildren().add(box);

        field.valueProperty().addListener(it -> {
            if (!updatingValue) {
                updateFields();
            }
        });

        updateFields();

        // Install the listener after setting the values, avoid unnecessary
        // notifications
        InvalidationListener updateValueListener = it -> {
            if (!updatingTextFields) {
                updateValue();
            }
        };

        hourField.textProperty().addListener(updateValueListener);
        minuteField.textProperty().addListener(updateValueListener);
    }

    private boolean updatingTextFields;

    private void updateFields() {
        updatingTextFields = true;

        try {
            TimeField timeField = getSkinnable();
            LocalTime localTime = timeField.getValue();
            if (localTime != null) {
                hourField.setText(Integer.toString(localTime.getHour()));
                minuteField.setText(Integer.toString(localTime.getMinute()));
            } else {
                hourField.setText("");
                minuteField.setText("");
            }
        } finally {
            updatingTextFields = false;
        }
    }

    private boolean updatingValue;

    private void updateValue() {
        updatingValue = true;
        try {
            int hour = 0;
            int minute = 0;
            try {
                hour = Math.max(0, Math.min(23, Integer.parseInt(hourField.getText())));
                minute = Math.max(0, Math.min(59, Integer.parseInt(minuteField.getText())));
            } catch (NumberFormatException ex) {
                // do nothing
            }

            /*
             * LocalTime is immutable, hence we have to create new instances over
             * and over again, which causes property change events. So we have to
             * have this check here to ensure that the value has really changed. And
             * we only care about hours and minutes, so we are not using
             * LocalTime.equals().
             */
            LocalTime oldTime = getSkinnable().getValue();
            LocalTime newTime = LocalTime.of(hour, minute);

            if (oldTime != null && newTime != null) {
                if (!(oldTime.getHour() == newTime.getHour() && oldTime.getMinute() == newTime.getMinute())) {
                    getSkinnable().setValue(newTime);
                }
            } else if (newTime == null) {
                getSkinnable().setValue(null);

            }
        } finally {
            updatingValue = false;
        }
    }

    public class RollingHandler implements EventHandler<KeyEvent> {

        private final int max;
        private final TextField field;

        public RollingHandler(TextField field, int max) {
            this.field = field;
            this.max = max;
        }

        @Override
        public void handle(KeyEvent event) {
            switch (event.getCode()) {
                case UP:
                    increment();
                    break;
                case DOWN:
                    decrement();
                    break;
                default:
                    break;
            }
        }

        private void increment() {
            int value = 0;
            try {
                value = Integer.parseInt(field.getText());
            } catch (NumberFormatException ex) {
                // do nothing
            }
            if (value < max) {
                value++;
            } else if (getSkinnable().isRollOver()) {
                value = 0;
            }
            if (value < 10) {
                field.setText("0" + value);
            } else {
                field.setText(Integer.toString(value));
            }
        }

        private void decrement() {
            int value = 0;
            try {
                value = Integer.parseInt(field.getText());
            } catch (NumberFormatException ex) {
                // do nothing
            }
            if (value > 0) {
                value--;
            } else if (getSkinnable().isRollOver()) {
                value = max;
            }

            if (value < 10) {
                field.setText("0" + value);
            } else {
                field.setText(Integer.toString(value));
            }
        }
    }

    public class NumericTextField extends TextField {

        public NumericTextField(int max) {
            setPrefColumnCount(2);
            focusedProperty().addListener(it -> {
                if (isFocused()) {
                    selectAll();
                }
            });
        }

        @Override
        public void replaceText(int start, int end, String s) {
            super.replaceText(start, end, s.replaceAll("[^0-9]", ""));
        }

        @Override
        public void replaceSelection(String s) {
            super.replaceSelection(s.replaceAll("[^0-9]", ""));
        }
    }
}
