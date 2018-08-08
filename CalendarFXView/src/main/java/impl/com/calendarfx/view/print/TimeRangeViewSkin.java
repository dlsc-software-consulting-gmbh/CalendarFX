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
import com.calendarfx.view.print.TimeRangeView;

import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class TimeRangeViewSkin extends SkinBase<TimeRangeView> {

    public TimeRangeViewSkin(TimeRangeView control) {
        super(control);

        Label overviewLabel = new Label();
        overviewLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            if (control.getUnitsToPrint() == 0) {
                return "";
            }
            return Messages.getString(
                    control.getUnitsToPrint() == 1
                            ? "TimeRangeViewSkin.PERIOD_LABEL_SINGULAR"
                            : "TimeRangeViewSkin.PERIOD_LABEL_PLURAL",
                    control.getUnitsToPrint(),
                    control.getUnitsToPrint() == 1
                            ? Messages.getString(control.getViewType()
                                    .getSingularChronoMessageKey())
                            : Messages.getString(control.getViewType()
                                    .getPluralChronoMessageKey()));
        }, control.unitsToPrintProperty(), control.viewTypeProperty()));

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("container");
        gridPane.add(
                new Label(Messages.getString("TimeRangeViewSkin.START_LABEL")),
                0, 0);
        gridPane.add(control.getStartField(), 1, 0);
        gridPane.add(
                new Label(Messages.getString("TimeRangeViewSkin.END_LABEL")), 0,
                1);
        gridPane.add(control.getEndField(), 1, 1);
        gridPane.add(overviewLabel, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        gridPane.getColumnConstraints().addAll(col1, col2);

        GridPane.setHalignment(control.getStartField(), HPos.LEFT);
        GridPane.setHalignment(control.getEndField(), HPos.LEFT);

        GridPane.setFillWidth(control.getStartField(), true);
        GridPane.setFillWidth(control.getEndField(), true);

        getChildren().add(gridPane);
    }

}
