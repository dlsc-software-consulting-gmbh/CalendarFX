/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
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
                break;
            case GRID:
                yearView.setManaged(true);
                yearView.setVisible(true);
                sheetView.setManaged(false);
                sheetView.setVisible(false);
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
        StackPane stackPane = new StackPane();

        this.sheetView = getSkinnable().getMonthSheetView();
        this.sheetView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.yearView = getSkinnable().getYearView();
        this.yearView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        stackPane.getChildren().addAll(yearView, sheetView);

        return stackPane;
    }
}
