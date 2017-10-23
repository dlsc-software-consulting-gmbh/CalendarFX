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
import com.calendarfx.view.Messages;
import com.calendarfx.view.MonthEntryView;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Circle;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;

import static java.time.format.FormatStyle.SHORT;

@SuppressWarnings("javadoc")
public class MonthEntryViewSkin extends SkinBase<MonthEntryView> {

    private Label titleLabel;
    private Label timeLabel;
    private Circle colorDot;

    public MonthEntryViewSkin(MonthEntryView view) {
        super(view);

        Entry<?> entry = view.getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        Calendar calendar = entry.getCalendar();

        colorDot = new Circle();
        colorDot.setRadius(2.5);
        colorDot.setMouseTransparent(true);

        titleLabel = new Label();
        titleLabel.setGraphic(colorDot);
        titleLabel.setMinSize(0, 0);
        titleLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        titleLabel.setMouseTransparent(true);

        timeLabel = new Label();
        timeLabel.setMouseTransparent(true);
        timeLabel.setMaxHeight(Double.MAX_VALUE);
        timeLabel.setMouseTransparent(true);

        // update in these cases
        entry.titleProperty().addListener(weakUpdateViewListener);
        entry.fullDayProperty().addListener(weakUpdateViewListener);
        entry.intervalProperty().addListener(weakUpdateViewListener);
        entry.calendarProperty().addListener(weakUpdateViewListener);

        view.positionProperty().addListener(weakUpdateViewListener);

        if (calendar != null) {
            calendar.styleProperty().addListener(weakUpdateViewListener);
        }

        getChildren().addAll(titleLabel, timeLabel);

        updateView();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double pw = 0;

        if (contentWidth > 120) {
            pw = timeLabel.prefWidth(-1);
            timeLabel.resizeRelocate(snapPosition(contentX + contentWidth - pw), snapPosition(contentY), snapSize(pw), snapSize(contentHeight));
            titleLabel.resizeRelocate(snapPosition(contentX), snapPosition(contentY), snapSize(contentWidth - pw), snapSize(contentHeight));
            timeLabel.setVisible(true);
        } else {
            titleLabel.resizeRelocate(snapPosition(contentX), snapPosition(contentY), snapSize(contentWidth - pw), snapSize(contentHeight));
            timeLabel.setVisible(false);
        }
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Math.max(titleLabel.prefHeight(-1), timeLabel.prefHeight(-1)) + topInset + bottomInset;
    }

    private InvalidationListener updateViewListener = it -> updateView();

    private WeakInvalidationListener weakUpdateViewListener = new WeakInvalidationListener(updateViewListener);

    private void updateView() {
        final MonthEntryView view = getSkinnable();
        Entry<?> entry = view.getEntry();
        final Calendar calendar = entry.getCalendar();

        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        final ObservableList<String> styleClass = view.getStyleClass();

        styleClass.setAll("month-entry-view", "default-style-entry-small"); //$NON-NLS-1$
        if (calendar != null) {
            styleClass.add(calendar.getStyle() + "-entry-small"); //$NON-NLS-1$
        }

        if (entry.isFullDay() || entry.isMultiDay()) {
            styleClass.add("default-style-entry-small-full-day"); //$NON-NLS-1$
            if (calendar != null) {
                styleClass.add(calendar.getStyle() + "-entry-small-full-day"); //$NON-NLS-1$
            }
        }

        // color dot visibility
        colorDot.setVisible(!entry.isFullDay() && !entry.isMultiDay());

        if (calendar != null) {
            // color dot style
            colorDot.getStyleClass().setAll("default-style-icon-small", //$NON-NLS-1$
                    calendar.getStyle() + "-icon-small"); //$NON-NLS-1$

            // title style
            titleLabel.getStyleClass().setAll(
                    "default-style-entry-small-title-label", //$NON-NLS-1$
                    calendar.getStyle() + "-entry-small-title-label"); //$NON-NLS-1$

            // time label style
            timeLabel.getStyleClass().setAll(
                    "default-style-entry-small-time-label", //$NON-NLS-1$
                    calendar.getStyle() + "-entry-small-time-label"); //$NON-NLS-1$

            // title style
            if (entry.isMultiDay() || entry.isFullDay()) {
                titleLabel.getStyleClass().addAll(
                        "default-style-entry-small-title-label-full-day", //$NON-NLS-1$
                        calendar.getStyle()
                                + "-entry-small-title-label-full-day"); //$NON-NLS-1$
                timeLabel.getStyleClass().addAll(
                        "default-style-entry-small-time-label-full-day", //$NON-NLS-1$
                        calendar.getStyle()
                                + "-entry-small-time-label-full-day"); //$NON-NLS-1$
            }
        } else {
            /*
             * Calendar might be null when the entry is a "dummy" entry.
             */

            // color dot style
            colorDot.getStyleClass().setAll("default-style-icon-small"); //$NON-NLS-1$

            // title style
            titleLabel.getStyleClass()
                    .setAll("default-style-entry-small-title-label"); //$NON-NLS-1$

            // time label style
            timeLabel.getStyleClass()
                    .setAll("default-style-entry-small-time-label"); //$NON-NLS-1$

            // title style
            if (entry.isMultiDay() || entry.isFullDay()) {
                titleLabel.getStyleClass().addAll(
                        "default-style-entry-small-title-label-full-day"); //$NON-NLS-1$
                timeLabel.getStyleClass().addAll(
                        "default-style-entry-small-time-label-full-day"); //$NON-NLS-1$
            }
        }

        if (entry.isFullDay()) {
            timeLabel.setText(""); //$NON-NLS-1$
        }

        switch (view.getPosition()) {
            case FIRST:
            case ONLY:
                titleLabel.setText(entry.getTitle());
                if (!(entry.isFullDay() || entry.isMultiDay())) {
                    titleLabel.setGraphic(colorDot);
                }
                break;
            default:
                /*
                 * Blank string is important for layout purposes (title and time
                 * might have different font sizes
                 */
                titleLabel.setText(" "); //$NON-NLS-1$
                titleLabel.setGraphic(null);
                break;
        }

        styleClass.add("default-style-entry-small-" + view.getPosition().toString().toLowerCase());

        // time label text
        if (!entry.isFullDay()) {
            if (entry.isMultiDay()) {
                switch (view.getPosition()) {
                    case LAST:
                        timeLabel.setText(MessageFormat.format(
                                Messages.getString("MonthEntryViewSkin.ENDS_AT"), //$NON-NLS-1$
                                DateTimeFormatter.ofLocalizedTime(SHORT)
                                        .format(entry.getEndTime())));
                        break;
                    case FIRST:
                        /*
                         * Only show it if the view is the first and represents the
                         * start date of the entry. Views can be on first but
                         * represent a date between start and end date of the entry
                         * (e.g. when not shown in the first week of the entry time
                         * interval).
                         */
                        if (view.getStartDate()
                                .equals(entry.getStartDate())) {
                            timeLabel.setText(
                                    DateTimeFormatter.ofLocalizedTime(SHORT)
                                            .format(entry.getStartTime()));
                        }
                        break;
                    default:
                        timeLabel.setText(""); //$NON-NLS-1$
                        break;
                }
            } else {
                timeLabel.setText(DateTimeFormatter.ofLocalizedTime(SHORT).format(entry.getStartTime()));
            }
        }
    }
}
