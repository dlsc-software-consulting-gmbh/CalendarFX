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
import javafx.scene.control.Labeled;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Rectangle;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * The default day entry renderer. <br />
 * It renders a title and the start time of the entry.
 */
public class DayEntryViewSkin extends SkinBase<DayEntryView> {

    private Entry<?> entry;
    private DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    private Labeled startTimeLabel;
    private Labeled titleLabel;

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

        setupUpdateListeners(weakUpdateLabelsListener, weakUpdateStylesListener);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        updateStyles();
    }

    /**
     * This method registers the given listeners so that the node will be notified about changes of e.g. the entry
     * which require updates on the UI.
     */
    protected void setupUpdateListeners(InvalidationListener updateLabelsListener, InvalidationListener updateStylesListener) {
        entry.intervalProperty().addListener(updateLabelsListener);
        entry.calendarProperty().addListener(updateStylesListener);

        getSkinnable().positionProperty().addListener(updateLabelsListener);
    }

    /**
     * This methods updates the styles of the node according to the entry settings.
     */
    protected void updateStyles() {
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

    /**
     * The label used to render the start time
     */
    protected Labeled createStartTimeLabel() {
        Label label = new Label();
        label.setMinSize(0, 0);

        Entry<?> entry = getSkinnable().getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        label.setText(formatTime(entry.getStartTime()));

        return label;
    }

    /**
     * Convert the given time to a string
     */
    protected String formatTime(LocalTime time) {
        return formatter.format(time);
    }

    /**
     * The label used to render the title
     */
    protected Labeled createTitleLabel() {
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

    /**
     * This method will be called if the labels needs to be updated
     */
    protected void updateLabels() {
        Entry<?> entry = getSkinnable().getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        startTimeLabel.setText(formatTime(entry.getStartTime()));
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
