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
import com.calendarfx.view.MonthView;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Circle;

import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("javadoc")
public class MonthEntryViewSkin extends SkinBase<MonthEntryView> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    protected Label titleLabel;
    protected Label timeLabel;
    protected Circle colorDot;

    public MonthEntryViewSkin(MonthEntryView view) {
        super(view);

        Entry<?> entry = view.getEntry();
        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        Calendar calendar = entry.getCalendar();

        colorDot = new Circle();
        colorDot.setRadius(5);
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
    protected void layoutChildren(double contentX, double contentY,
            double contentWidth, double contentHeight) {
        double pw = 0;

        if (contentWidth > 120) {
            pw = timeLabel.prefWidth(-1);
            timeLabel.resizeRelocate(snapPositionX(contentX + contentWidth - pw), snapPositionY(contentY), snapSizeX(pw), snapSizeY(contentHeight));
            titleLabel.resizeRelocate(snapPositionX(contentX), snapPositionY(contentY), snapSizeX(contentWidth - pw), snapSizeY(contentHeight));
            timeLabel.setVisible(true);
        } else {
            titleLabel.resizeRelocate(snapPositionX(contentX), snapPositionY(contentY), snapSizeX(contentWidth - pw), snapSizeY(contentHeight));
            timeLabel.setVisible(false);
        }
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Math.max(titleLabel.prefHeight(-1), timeLabel.prefHeight(-1)) + topInset + bottomInset;
    }

    private final InvalidationListener updateViewListener = it -> updateView();

    private final WeakInvalidationListener weakUpdateViewListener = new WeakInvalidationListener(updateViewListener);

    protected void updateView() {
        System.out.println("UPDATE VIEW");
        final MonthEntryView view = getSkinnable();
        Entry<?> entry = view.getEntry();
        final Calendar calendar = entry.getCalendar();

        if (entry.isRecurrence()) {
            entry = entry.getRecurrenceSourceEntry();
        }

        final ObservableList<String> styleClass = view.getStyleClass();

        styleClass.setAll("month-entry-view", "default-style-entry-small");
        if (calendar != null) {
            styleClass.add(calendar.getStyle() + "-entry-small");
        }

        if (entry.isFullDay() || entry.isMultiDay()) {
            styleClass.add("default-style-entry-small-full-day");
            if (calendar != null) {
                styleClass.add(calendar.getStyle() + "-entry-small-full-day");
            }
        }

        // color dot visibility
        colorDot.setVisible(!entry.isFullDay() && !entry.isMultiDay());

        if (calendar != null) {
            // color dot style
            colorDot.getStyleClass().setAll("default-style-icon-small", calendar.getStyle() + "-icon-small");

            // title style
            titleLabel.getStyleClass().setAll("default-style-entry-small-title-label", calendar.getStyle() + "-entry-small-title-label");

            // time label style
            timeLabel.getStyleClass().setAll("default-style-entry-small-time-label", calendar.getStyle() + "-entry-small-time-label");

            // title style
            if (entry.isMultiDay() || entry.isFullDay()) {
                titleLabel.getStyleClass().addAll("default-style-entry-small-title-label-full-day", calendar.getStyle() + "-entry-small-title-label-full-day");
                timeLabel.getStyleClass().addAll("default-style-entry-small-time-label-full-day", calendar.getStyle() + "-entry-small-time-label-full-day");
            }
        } else {
            // Calendar might be null when the entry is a "dummy" entry.
            // color dot style
            colorDot.getStyleClass().setAll("default-style-icon-small");

            // title style
            titleLabel.getStyleClass().setAll("default-style-entry-small-title-label");

            // time label style
            timeLabel.getStyleClass().setAll("default-style-entry-small-time-label");

            // title style
            if (entry.isMultiDay() || entry.isFullDay()) {
                titleLabel.getStyleClass().addAll("default-style-entry-small-title-label-full-day");
                timeLabel.getStyleClass().addAll("default-style-entry-small-time-label-full-day");
            }
        }

        if (entry.isFullDay()) {
            timeLabel.setText("");
        }

        switch (view.getPosition()) {
        case FIRST:
        case ONLY:
            //titleLabel.setText(entry.getTitle());
            if (!(entry.isFullDay() || entry.isMultiDay())) {
                titleLabel.setGraphic(colorDot);
            }
            break;
        default:
            // Blank string is important for layout purposes (title and time
            // might have different font sizes
            titleLabel.setText(" ");
            titleLabel.setGraphic(null);
            break;
        }

        styleClass.add("default-style-entry-small-" + view.getPosition().toString().toLowerCase());

        // time label text
        if (!entry.isFullDay()) {
            DateTimeFormatter dateTimeFormatter = getDateTimeFormatter();
            MonthView dateControl = getSkinnable().getDateControl();
            ZoneId entryZoneId = entry.getZoneId();
            ZoneId dateControlZoneId = dateControl.getZoneId();

            /*
            if (entry.isMultiDay()) {
                switch (view.getPosition()) {
                case LAST:
                    if (!Objects.equals(entryZoneId, dateControlZoneId)) {
                        timeLabel.setText(MessageFormat.format(Messages.getString("MonthEntryViewSkin.ENDS_AT"), dateTimeFormatter.format(entry.getEndAsZonedDateTime().withZoneSameInstant(dateControlZoneId).toLocalTime()) + " (" + entryZoneId.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")"));
                    } else {
                        timeLabel.setText(MessageFormat.format(Messages.getString("MonthEntryViewSkin.ENDS_AT"), dateTimeFormatter.format(entry.getEndTime())));
                    }
                    break;
                case FIRST:*/
                    /*
                     * Only show it if the view is the first and represents the
                     * start date of the entry. Views can be on first but
                     * represent a date between start and end date of the entry
                     * (e.g. when not shown in the first week of the entry time
                     * interval).
                     */
               /*     if (view.getStartDate().equals(entry.getStartDate())) {
                        if (!Objects.equals(entryZoneId, dateControlZoneId)) {
                            timeLabel.setText(dateTimeFormatter.format(entry.getStartAsZonedDateTime().withZoneSameInstant(dateControlZoneId).toLocalTime()) + " (" + entryZoneId.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")");
                        } else {
                            timeLabel.setText(dateTimeFormatter.format(entry.getStartTime()));
                        }
                    }
                    break;
                default:
                    timeLabel.setText("");
                    break;
                }
            } else {
                if (!Objects.equals(entryZoneId, dateControlZoneId)) {
                    timeLabel.setText(dateTimeFormatter.format(entry.getStartAsZonedDateTime().withZoneSameInstant(dateControlZoneId).toLocalTime()) + " (" + entryZoneId.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")");
                } else {
                    timeLabel.setText(dateTimeFormatter.format(entry.getStartTime()));
                }
            }*/
        }

        ((MonthViewSkin.MonthDayEntriesPane)(getNode().getParent())).updateFlag();
        styleClass.addAll(entry.getStyleClass());

    }

    /**
     * Sets the Date Time Format on the Label that shows the time.
     * 
     * @return the date time formatter.
     */
    protected DateTimeFormatter getDateTimeFormatter() {
        return formatter;
    }
}
