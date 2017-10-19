/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
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
