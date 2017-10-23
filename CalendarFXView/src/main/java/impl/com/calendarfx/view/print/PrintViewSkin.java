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
import com.calendarfx.view.print.PreviewPane;
import com.calendarfx.view.print.PrintView;
import com.calendarfx.view.print.SettingsView;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

public class PrintViewSkin extends SkinBase<PrintView> {

    public PrintViewSkin(PrintView control) {
        super(control);

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("container");
        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();

        row1.setVgrow(Priority.ALWAYS);
        row2.setVgrow(Priority.NEVER);

        col1.setHgrow(Priority.ALWAYS);
        col2.setHgrow(Priority.NEVER);
        col3.setHgrow(Priority.NEVER);

        row1.setFillHeight(true);
        row2.setFillHeight(true);

        col1.setFillWidth(true);
        col2.setFillWidth(true);
        col3.setFillWidth(true);

        col1.setMaxWidth(Double.MAX_VALUE);
        col3.setMaxWidth(Region.USE_PREF_SIZE);
        col3.setMinWidth(Region.USE_PREF_SIZE);

        row1.setMaxHeight(Double.MAX_VALUE);
        row2.setMinHeight(Region.USE_PREF_SIZE);

        gridPane.getRowConstraints().setAll(row1, row2);
        gridPane.getColumnConstraints().setAll(col1, col2, col3);

        // preview pane
        PreviewPane previewPane = control.getPreviewPane();
        gridPane.add(previewPane, 0, 0);
        GridPane.setRowSpan(previewPane, 2);

        // settings
        SettingsView settingsView = control.getSettingsView();
        gridPane.add(settingsView, 2, 0);

        // separator
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        GridPane.setRowSpan(separator, 2);
        gridPane.add(separator, 1, 0);

        // button bar
        Button cancelBtn = new Button(Messages.getString("PrintView.CANCEL_BUTTON"));
        cancelBtn.onActionProperty().bind(control.onCancelProperty());

        Button continueBtn = new Button(Messages.getString("PrintView.CONTINUE_BUTTON"));
        continueBtn.onActionProperty().bind(control.onContinueProperty());

        HBox buttonsBar = new HBox();
        buttonsBar.getStyleClass().add("button-bar");
        buttonsBar.getChildren().addAll(cancelBtn, continueBtn);

        gridPane.add(buttonsBar, 2, 1);

        getChildren().add(gridPane);
    }

}
