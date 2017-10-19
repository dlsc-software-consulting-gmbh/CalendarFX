/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.popover;

import com.calendarfx.view.Messages;
import com.calendarfx.view.popover.RecurrencePopup;
import com.calendarfx.view.popover.RecurrencePopup.RecurrencePopupEvent;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

@SuppressWarnings("javadoc")
public class RecurrencePopupSkin implements Skin<RecurrencePopup> {

    private StackPane stackPane;
    private RecurrencePopup popup;

    public RecurrencePopupSkin(RecurrencePopup popup) {
        super();

        this.popup = popup;

        Button okButton = new Button(Messages.getString("RecurrencePopupSkin.OK")); //$NON-NLS-1$
        okButton.setDefaultButton(true);
        okButton.setOnAction(evt -> {
            popup.hide();
            popup.fireEvent(
                    new RecurrencePopupEvent(RecurrencePopupEvent.OK_PRESSED));
        });

        Button cancelButton = new Button(Messages.getString("RecurrencePopupSkin.CANCEL")); //$NON-NLS-1$
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(evt -> {
            popup.hide();
            popup.fireEvent(
                    new RecurrencePopupEvent(RecurrencePopupEvent.CANCEL_PRESSED));
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(cancelButton, okButton);
        buttonBox.getStyleClass().add("button-pane"); //$NON-NLS-1$

        BorderPane contentPane = new BorderPane();
        contentPane.getStyleClass().add("content"); //$NON-NLS-1$
        contentPane.setCenter(popup.getRecurrenceView());
        contentPane.setBottom(buttonBox);

        stackPane = popup.getRoot();
        stackPane.getChildren().add(contentPane);
    }

    @Override
    public RecurrencePopup getSkinnable() {
        return popup;
    }

    @Override
    public Node getNode() {
        return stackPane;
    }

    @Override
    public void dispose() {
    }
}
