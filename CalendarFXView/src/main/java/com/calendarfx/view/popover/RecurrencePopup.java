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

package com.calendarfx.view.popover;

import com.calendarfx.view.CalendarView;
import com.calendarfx.view.RecurrenceView;
import impl.com.calendarfx.view.popover.RecurrencePopupSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;

public class RecurrencePopup extends PopupControl {

    private static final String DEFAULT_STYLE = "recurrence-popup"; //$NON-NLS-1$

    private RecurrenceView recurrenceView;

    private StackPane root;

    public RecurrencePopup() {
        getStyleClass().add(DEFAULT_STYLE);

        root = new StackPane();
        root.getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());

        recurrenceView = new RecurrenceView();
        recurrenceView.setShowSummary(false);

        Bindings.bindContentBidirectional(root.getStyleClass(), getStyleClass());

        setAutoFix(true);
        setAutoHide(true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RecurrencePopupSkin(this);
    }

    public final StackPane getRoot() {
        return root;
    }

    public final RecurrenceView getRecurrenceView() {
        return recurrenceView;
    }

    private class RecurrencePopupEventHandlerProperty
            extends SimpleObjectProperty<EventHandler<RecurrencePopupEvent>> {

        private EventType<RecurrencePopupEvent> eventType;

        public RecurrencePopupEventHandlerProperty(final String name,
                                                   final EventType<RecurrencePopupEvent> eventType) {
            super(RecurrencePopup.this, name);
            this.eventType = eventType;
        }

        @Override
        protected void invalidated() {
            setEventHandler(eventType, get());
        }
    }

    /*
     * OK pressed event support.
     */

    private RecurrencePopupEventHandlerProperty onOkPressed;

    public final ObjectProperty<EventHandler<RecurrencePopupEvent>> onOkPressedProperty() {
        if (onOkPressed == null) {
            onOkPressed = new RecurrencePopupEventHandlerProperty("onOkPressed", //$NON-NLS-1$
                    RecurrencePopupEvent.OK_PRESSED);
        }

        return onOkPressed;
    }

    public final void setOnOkPressed(EventHandler<RecurrencePopupEvent> value) {
        onOkPressedProperty().set(value);
    }

    public final EventHandler<RecurrencePopupEvent> getOnOkPressed() {
        return onOkPressed == null ? null : onOkPressedProperty().get();
    }

    /*
     * Cancel pressed event support.
     */

    private RecurrencePopupEventHandlerProperty onCancelPressed;

    public final ObjectProperty<EventHandler<RecurrencePopupEvent>> onCancelPressedProperty() {
        if (onCancelPressed == null) {
            onCancelPressed = new RecurrencePopupEventHandlerProperty(
                    "onCancelPressed", RecurrencePopupEvent.CANCEL_PRESSED); //$NON-NLS-1$
        }

        return onCancelPressed;
    }

    public final void setOnCancelPressed(
            EventHandler<RecurrencePopupEvent> value) {
        onCancelPressedProperty().set(value);
    }

    public final EventHandler<RecurrencePopupEvent> getOnCancelPressed() {
        return onCancelPressed == null ? null : onCancelPressedProperty().get();
    }

    public static class RecurrencePopupEvent extends Event {

        public static final EventType<RecurrencePopupEvent> RECURRENCE_POPUP_CLOSED = new EventType<>(
                Event.ANY, "RECURRENCE_POPUP_CLOSED"); //$NON-NLS-1$

        public static final EventType<RecurrencePopupEvent> OK_PRESSED = new EventType<>(
                RecurrencePopupEvent.RECURRENCE_POPUP_CLOSED, "OK_PRESSED"); //$NON-NLS-1$

        public static final EventType<RecurrencePopupEvent> CANCEL_PRESSED = new EventType<>(
                RecurrencePopupEvent.RECURRENCE_POPUP_CLOSED, "CANCEL_PRESSED"); //$NON-NLS-1$

        public RecurrencePopupEvent(EventType<? extends Event> eventType) {
            super(eventType);
        }
    }

}
