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
import com.calendarfx.view.WeekFieldsView;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import java.time.LocalTime;
import java.time.temporal.WeekFields;

public class CalendarPropertySheet extends PropertySheet {

    public CalendarPropertySheet() {
        this(FXCollections.emptyObservableList());
    }

    public CalendarPropertySheet(ObservableList<Item> items) {
        super(items);
        setPropertyEditorFactory(new Factory());
    }

    class Factory extends DefaultPropertyEditorFactory {

        @Override
        public PropertyEditor<?> call(Item property) {
            PropertyEditor<?> editor = super.call(property);

            if (editor == null) {
                if (property.getType().equals(LocalTime.class)) {
                    return new AbstractPropertyEditor<LocalTime, TimeField>(property, new TimeField()) {
                        @Override
                        protected ObservableValue<LocalTime> getObservableValue() {
                            return getEditor().valueProperty();
                        }

                        @Override
                        public void setValue(LocalTime value) {
                            getEditor().setValue(value);
                        }
                    };
                } else if (property.getType().equals(WeekFields.class)) {
                    return new AbstractPropertyEditor<WeekFields, WeekFieldsView>(property, new WeekFieldsView()) {
                        @Override
                        protected ObservableValue<WeekFields> getObservableValue() {
                            return getEditor().weekFieldsProperty();
                        }

                        @Override
                        public void setValue(WeekFields value) {
                            getEditor().setWeekFields(value);
                        }
                    };
                }
            }

            return editor;
        }
    }
}
