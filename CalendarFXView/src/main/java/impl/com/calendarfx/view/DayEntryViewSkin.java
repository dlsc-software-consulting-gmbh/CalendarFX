/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DraggedEntry;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Rectangle;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DayEntryViewSkin extends SkinBase<DayEntryView> {

    private Entry<?> entry;
    private DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    private Label startTimeLabel;
    private Label titleLabel;

    private final InvalidationListener updateStylesListener = it -> updateStyles();
    private final WeakInvalidationListener weakUpdateStylesListener = new WeakInvalidationListener(updateStylesListener);

    private final InvalidationListener updateLabelsListener = it -> updateLabels();
    private final WeakInvalidationListener weakUpdateLabelsListener = new WeakInvalidationListener(updateLabelsListener);

    public DayEntryViewSkin(DayEntryView view) {
        super(view);

        startTimeLabel = createStartTimeLabel();
        startTimeLabel.setManaged(false);
        startTimeLabel.setMouseTransparent(true);

        titleLabel = createTitleLabel();
        titleLabel.setManaged(false);
        titleLabel.setMouseTransparent(true);

        getChildren().addAll(startTimeLabel, titleLabel);

        entry = view.getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        entry.intervalProperty().addListener(weakUpdateLabelsListener);
        entry.calendarProperty().addListener(weakUpdateStylesListener);

        view.positionProperty().addListener(weakUpdateLabelsListener);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        updateStyles();
    }

    private void updateStyles() {
        DayEntryView view = getSkinnable();

        Calendar calendar = entry.getCalendar();
        if (entry instanceof DraggedEntry) {
            calendar = ((DraggedEntry) entry).getOriginalCalendar();
        }

        // when the entry gets removed from its calendar then the calendar can be null
        if (calendar == null) {
            return;
        }

        view.getStyleClass().setAll("default-style-entry", calendar.getStyle() + "-entry");

        if (entry.isRecurrence()) {
            view.getStyleClass().add("recurrence"); //$NON-NLS-1$
        }

        startTimeLabel.getStyleClass().setAll("start-time-label", "default-style-entry-time-label",
                calendar.getStyle() + "-entry-time-label");
        titleLabel.getStyleClass().setAll("title-label", "default-style-entry-title-label",
                calendar.getStyle() + "-entry-title-label");
    }

    private Label createStartTimeLabel() {
        Label label = new Label();
        label.setMinSize(0, 0);

        Entry<?> entry = getSkinnable().getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        label.setText(formatter.format(entry.getStartTime()));

        return label;
    }

    private Label createTitleLabel() {
        Entry<?> entry = getSkinnable().getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        Label label = new Label();
        label.setWrapText(true);
        label.setMinSize(0, 0);
        label.textProperty().bind(entry.titleProperty());

        return label;
    }

    private void updateLabels() {
        Entry<?> entry = getSkinnable().getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        startTimeLabel.setText(formatter.format(entry.getStartTime()));
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        // title label
        double titleHeight = titleLabel.prefHeight(contentWidth);

        // it is guaranteed that we have enough height to display the title (see "computeMinHeight")
        titleLabel.resizeRelocate(snapPosition(contentX), snapPosition(contentY), snapSize(contentWidth), snapSize(titleHeight));

        // start time label
        double timeLabelHeight = startTimeLabel.prefHeight(contentWidth);
        if (contentHeight - titleHeight > timeLabelHeight) {
            startTimeLabel.setVisible(true);
            startTimeLabel.resizeRelocate(snapPosition(contentX), snapPosition(contentY + titleHeight), snapSize(contentWidth), snapSize(timeLabelHeight));
        } else {
            startTimeLabel.setVisible(false);
        }
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (titleLabel != null) {
            // For this pref height calculation we do not consider the available width because
            // we only want to show a single line of text anyways.
            return titleLabel.prefHeight(-1) + topInset + bottomInset;
        }

        return super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
    }
}
