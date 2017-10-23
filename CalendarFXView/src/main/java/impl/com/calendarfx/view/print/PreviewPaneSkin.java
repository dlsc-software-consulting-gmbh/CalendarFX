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
import com.calendarfx.view.print.PrintablePage;
import com.calendarfx.view.print.ZoomPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class PreviewPaneSkin extends SkinBase<PreviewPane> {

    public PreviewPaneSkin(PreviewPane control) {
        super(control);

        Slider slider = new Slider();
        slider.setMin(ZoomPane.MIN_ZOOM_VALUE);
        slider.setMax(ZoomPane.MAX_ZOOM_VALUE);
        slider.valueProperty().bindBidirectional(control.getZoomPane().zoomProperty());

        BorderPane center = new BorderPane();
        center.setCenter(control.getZoomPane());
        center.getStyleClass().add("center");

        PrintablePage page = control.getPrintablePage();

        FontAwesomeIconView backIcon = new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_LEFT);
        FontAwesomeIconView forwardIcon = new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_RIGHT);

        Button backBtn = new Button();
        backBtn.setGraphic(backIcon);
        backBtn.setOnAction(evt -> page.back());
        backBtn.disableProperty().bind(Bindings.equal(1, page.pageNumberProperty()));

        Button nextBtn = new Button();
        nextBtn.setGraphic(forwardIcon);
        nextBtn.setOnAction(evt -> page.next());
        nextBtn.disableProperty().bind(Bindings.equal(page.pageNumberProperty(), page.totalPagesProperty()));

        Label pagesLbl = new Label();
        pagesLbl.textProperty().bind(Bindings.createStringBinding(() -> page.getPageNumber() + "/" + page.getTotalPages(), page.pageNumberProperty(), page.totalPagesProperty()));

        HBox bottom = new HBox(new Label(Messages.getString("PreviewPaneSkin.ZOOM_LABEL")), slider, backBtn, pagesLbl, nextBtn);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.getStyleClass().add("footer");

        BorderPane container = new BorderPane();
        container.getStyleClass().add("container");
        container.setCenter(center);
        container.setBottom(bottom);

        getChildren().add(container);
    }

}

