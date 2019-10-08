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

package impl.com.calendarfx.view.page;

import com.calendarfx.view.MonthSheetView;
import com.calendarfx.view.YearView;
import com.calendarfx.view.page.YearPage;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

@SuppressWarnings("javadoc")
public class YearPageSkin extends PageBaseSkin<YearPage> {

    private YearView yearView;
    private MonthSheetView sheetView;
    private StackPane stackPane;

    public YearPageSkin(YearPage view) {
        super(view);

        view.addEventFilter(ScrollEvent.SCROLL, this::handleScroll);
        view.displayModeProperty().addListener(it -> updateVisibility());

        updateVisibility();
    }

    private void updateVisibility() {
        switch (getSkinnable().getDisplayMode()) {
            case COLUMNS:
                yearView.setManaged(false);
                yearView.setVisible(false);
                sheetView.setManaged(true);
                sheetView.setVisible(true);
                if(!stackPane.getChildren().contains(sheetView)) {
                    stackPane.getChildren().add(sheetView);
                }
                break;
            case GRID:
                yearView.setManaged(true);
                yearView.setVisible(true);
                sheetView.setManaged(false);
                sheetView.setVisible(false);
                if(!stackPane.getChildren().contains(yearView)) {
                    stackPane.getChildren().add(yearView);
                }
                break;
        }
    }

    private void handleScroll(ScrollEvent evt) {
        YearPage yearPage = getSkinnable();
        double delta = evt.getDeltaX();
        if (delta == 0) {
            return;
        }
        if (delta < 0) {
            yearPage.goForward();
        } else if (delta > 0) {
            yearPage.goBack();
        }
    }

    @Override
    protected Node createContent() {
        stackPane = new StackPane();

        this.sheetView = getSkinnable().getMonthSheetView();
        this.sheetView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.yearView = getSkinnable().getYearView();
        this.yearView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        return stackPane;
    }
}
