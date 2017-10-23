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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * The default day entry view. <br />
 * It displays a title and the start time of the entry.
 */
public class DayEntryViewSkin extends SkinBase<DayEntryView> {

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

        Entry entry = getEntry();

        entry.intervalProperty().addListener(weakUpdateLabelsListener);
        entry.calendarProperty().addListener(weakUpdateStylesListener);
        entry.titleProperty().addListener(weakUpdateLabelsListener);

        getSkinnable().positionProperty().addListener(weakUpdateLabelsListener);
        updateLabels();

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        updateStyles();
    }

    /**
     * @returns The entry.
     */
    protected Entry getEntry() {
        Entry<?> entry = getSkinnable().getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }
        return entry;
    }

    /**
     * This methods updates the styles of the node according to the entry settings.
     */
    protected void updateStyles() {
        DayEntryView view = getSkinnable();
        Entry entry = getEntry();

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
     * The label used to show the start time.
     *
     * @returns The label component.
     */
    protected Label createStartTimeLabel() {
        Label label = new Label();
        label.setMinSize(0, 0);

        return label;
    }

    /**
     * Convert the given time to a string.
     *
     * @returns The formatted time.
     */
    protected String formatTime(LocalTime time) {
        return formatter.format(time);
    }

    /**
     * Convert the given title. This method can be overridden for e.g. translating the title.
     *
     * @returns The formatted title.
     */
    protected String formatTitle(String title) {
        return title;
    }

    /**
     * The label used to show the title.
     *
     * @returns The title component.
     */
    protected Label createTitleLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMinSize(0, 0);

        return label;
    }

    /**
     * This method will be called if the labels need to be updated.
     */
    protected void updateLabels() {
        Entry entry = getEntry();

        startTimeLabel.setText(formatTime(entry.getStartTime()));
        titleLabel.setText(formatTitle(entry.getTitle()));
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
