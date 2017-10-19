/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.AllDayEntryView;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;

@SuppressWarnings("javadoc")
public class AllDayEntryViewSkin extends SkinBase<AllDayEntryView> {

    private Label titleLabel;
    private BorderPane borderPane;

    public AllDayEntryViewSkin(AllDayEntryView view) {
        super(view);

        titleLabel = new Label();
        titleLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        titleLabel.setMouseTransparent(true);
        titleLabel.setManaged(false);

        Entry<?> entry = view.getEntry();

        entry.calendarProperty().addListener(weakUpdateViewListener);
        entry.fullDayProperty().addListener(weakUpdateViewListener);
        entry.titleProperty().addListener(weakUpdateViewListener);
        entry.intervalProperty().addListener(weakUpdateViewListener);

        updateView();

        getChildren().addAll(titleLabel);
    }

    private InvalidationListener updateViewListener = it -> updateView();

    private WeakInvalidationListener weakUpdateViewListener = new WeakInvalidationListener(updateViewListener);

    private void updateView() {
        final AllDayEntryView view = getSkinnable();

        Entry<?> entry = view.getEntry();
        Calendar calendar = entry.getCalendar();

        if (calendar == null) {
            return;
        }

        view.getStyleClass().setAll(
                "default-style-entry-small",
                calendar.getStyle() + "-entry-small",
                "default-style-entry-small-full-day",
                calendar.getStyle() + "-entry-small-full-day");

        // title style
        titleLabel.getStyleClass().setAll(
                "default-style-entry-small-title-label",
                calendar.getStyle() + "-entry-small-title-label",
                "default-style-entry-small-title-label-full-day",
                calendar.getStyle() + "-entry-small-title-label-full-day");

        titleLabel.setText(entry.getTitle());

        view.getStyleClass().add("default-style-entry-small-only");
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        titleLabel.resizeRelocate(snapPosition(contentX), snapPosition(contentY), snapSize(contentWidth), snapSize(contentHeight));
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return titleLabel.prefHeight(-1) + topInset + bottomInset;
    }
}
