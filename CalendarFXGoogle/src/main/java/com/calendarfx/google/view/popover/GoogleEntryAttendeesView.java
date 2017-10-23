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

package com.calendarfx.google.view.popover;

import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.view.popover.EntryPopOverPane;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.common.collect.Lists;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED;

/**
 * Pane used to edit the attendees of a Google Entry.
 *
 * @author Gabriel Diaz, 21.02.2015.
 */
public class GoogleEntryAttendeesView extends EntryPopOverPane {

    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");

    private GoogleEntry entry;
    private TextField txtEmail;
    private Button btAdd;

    public GoogleEntryAttendeesView(GoogleEntry entry) {
        super();

        this.entry = requireNonNull(entry);

        txtEmail = new TextField();
        txtEmail.getStyleClass().add("email-field");
        txtEmail.textProperty().addListener(obs -> enableButton());
        txtEmail.setOnAction(evt -> createAttendee());

        btAdd = new Button("Add");
        btAdd.setOnAction(evt -> createAttendee());

        enableButton();

        Label lblTitle = new Label("Attendees can:");
        lblTitle.getStyleClass().add("title");

        CheckBox chkEdit = new CheckBox("modify event");
        chkEdit.selectedProperty().bindBidirectional(entry.attendeesCanModifyProperty());
        chkEdit.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        CheckBox chkInvite = new CheckBox("invite others");
        chkInvite.selectedProperty().bindBidirectional(entry.attendeesCanInviteOthersProperty());
        chkInvite.disableProperty().bind(Bindings.or(entry.getCalendar().readOnlyProperty(), entry.attendeesCanModifyProperty()));

        CheckBox chkSeeOthers = new CheckBox("see attendees list");
        chkSeeOthers.selectedProperty().bindBidirectional(entry.attendeesCanSeeOthersProperty());
        chkSeeOthers.disableProperty().bind(Bindings.or(entry.getCalendar().readOnlyProperty(), entry.attendeesCanModifyProperty()));

        HBox checksParent = new HBox(chkEdit, chkInvite, chkSeeOthers);
        checksParent.getStyleClass().add("checks-parent");

        HBox.setHgrow(txtEmail, Priority.ALWAYS);
        HBox.setHgrow(btAdd, Priority.NEVER);

        HBox.setHgrow(chkEdit, Priority.ALWAYS);
        HBox.setHgrow(chkInvite, Priority.ALWAYS);
        HBox.setHgrow(chkSeeOthers, Priority.ALWAYS);

        HBox top = new HBox(txtEmail, btAdd);
        top.getStyleClass().add("top");
        VBox center = new VBox();
        center.getStyleClass().add("center");
        VBox bottom = new VBox(lblTitle, checksParent);
        bottom.getStyleClass().add("bottom");

        BorderPane container = new BorderPane();
        container.setTop(top);
        container.setCenter(center);
        container.setBottom(bottom);

        getChildren().add(container);
        getStyleClass().add("attendees-view");

        entry.getAttendees().addListener((Observable obs) -> buildItems(center, entry.getAttendees()));
        buildItems(center, entry.getAttendees());
    }

    private void createAttendee() {
        createAttendee(txtEmail.getText());
        txtEmail.setText("");
        enableButton();
        getScene().getWindow().sizeToScene();
    }

    private void enableButton() {
        txtEmail.pseudoClassStateChanged(INVALID, false);

        if (entry.getCalendar().isReadOnly()) {
            btAdd.setDisable(true);
            return;
        }

        if (txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) {
            btAdd.setDisable(true);
            return;
        }

        if (!isValidEmail(txtEmail.getText())) {
            btAdd.setDisable(true);
            txtEmail.pseudoClassStateChanged(INVALID, true);
            return;
        }

        for (EventAttendee attendee : entry.getAttendees()) {
            if (attendee.getEmail().equals(txtEmail.getText())) {
                btAdd.setDisable(true);
                return;
            }
        }

        btAdd.setDisable(false);
    }

    private boolean isValidEmail(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void buildItems(VBox parent, List<EventAttendee> attendees) {
        List<GoogleEntryAttendeeItem> attendeesNode = Lists.newArrayList();
        for (EventAttendee attendee : attendees) {
            attendeesNode.add(new GoogleEntryAttendeeItem(attendee));
        }
        parent.getChildren().setAll(attendeesNode);
    }

    private void createAttendee(String email) {
        EventAttendee attendee = new EventAttendee();
        attendee.setEmail(email);
        attendee.setDisplayName(email);
        entry.getAttendees().add(attendee);
    }

    private void removeAttendee(EventAttendee attendee) {
        entry.getAttendees().remove(attendee);
    }

    /**
     * Custom control to display an attendee of the Google Entry.
     *
     * @author Gabriel Diaz, 21.02.2015.
     */
    private class GoogleEntryAttendeeItem extends HBox {

        private Label optionalIcon;
        private Label statusIcon;
        private Label name;
        private Label removeButton;
        private EventAttendee attendee;

        public GoogleEntryAttendeeItem(EventAttendee attendee) {
            this.attendee = Objects.requireNonNull(attendee);

            optionalIcon = new Label();
            optionalIcon.setOnMouseClicked(evt -> setOptional(!isOptional()));
            optionalIcon.getStyleClass().add("button-icon");
            optionalIcon.setTooltip(new Tooltip());

            statusIcon = new Label();

            name = new Label();
            name.setMaxWidth(Double.MAX_VALUE);

            setOptional(Boolean.TRUE.equals(attendee.getOptional()));
            optionalProperty().addListener(obs -> updateIcon());
            updateIcon();

            removeButton = new Label();
            removeButton.setGraphic(new FontAwesome().create(FontAwesome.Glyph.TRASH_ALT));
            removeButton.getStyleClass().add("button-icon");
            removeButton.setOnMouseClicked(evt -> removeAttendee(attendee));

            HBox.setHgrow(optionalIcon, Priority.NEVER);
            HBox.setHgrow(name, Priority.ALWAYS);
            HBox.setHgrow(removeButton, Priority.NEVER);

            getStyleClass().add("attendee-item");
            getChildren().addAll(optionalIcon, statusIcon, name, removeButton);

            ContextMenu menu = new ContextMenu();
            MenuItem optionalItem = new MenuItem("Mark as optional");
            optionalItem.setOnAction(evt -> setOptional(true));
            MenuItem requiredItem = new MenuItem("Mark as required");
            requiredItem.setOnAction(evt -> setOptional(false));
            MenuItem removeItem = new MenuItem("Remove attendee");
            removeItem.setOnAction(evt -> removeAttendee(attendee));
            menu.getItems().addAll(optionalItem, requiredItem, new SeparatorMenuItem(), removeItem);

            addEventHandler(CONTEXT_MENU_REQUESTED, evt -> menu.show(this, evt.getScreenX(), evt.getScreenY()));
        }

        private void updateIcon() {
            FontAwesome fontAwesome = new FontAwesome();
            Glyph img = fontAwesome.create(FontAwesome.Glyph.MALE);
            img.setOpacity(isOptional() ? 0.4 : 1.0);
            optionalIcon.setGraphic(img);
            optionalIcon.getTooltip().setText(isOptional() ? "optional" : "required");
            name.setText(attendee.getEmail() + (isOptional() ? " (optional)" : ""));
        }

        private final BooleanProperty optional = new SimpleBooleanProperty(this, "optional");

        public final BooleanProperty optionalProperty() {
            return optional;
        }

        public final boolean isOptional() {
            return optionalProperty().get();
        }

        public final void setOptional(boolean optional) {
            optionalProperty().set(optional);
        }

    }

}
