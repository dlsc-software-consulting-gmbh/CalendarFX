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

    private final StackPane stackPane;
    private final RecurrencePopup popup;

    public RecurrencePopupSkin(RecurrencePopup popup) {
        super();

        this.popup = popup;

        Button okButton = new Button(Messages.getString("RecurrencePopupSkin.OK"));
        okButton.setDefaultButton(true);
        okButton.setOnAction(evt -> {
            popup.hide();
            popup.fireEvent(
                    new RecurrencePopupEvent(RecurrencePopupEvent.OK_PRESSED));
        });

        Button cancelButton = new Button(Messages.getString("RecurrencePopupSkin.CANCEL"));
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(evt -> {
            popup.hide();
            popup.fireEvent(
                    new RecurrencePopupEvent(RecurrencePopupEvent.CANCEL_PRESSED));
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(cancelButton, okButton);
        buttonBox.getStyleClass().add("button-pane");

        BorderPane contentPane = new BorderPane();
        contentPane.getStyleClass().add("content");
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
