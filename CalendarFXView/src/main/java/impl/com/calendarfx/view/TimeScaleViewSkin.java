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

import com.calendarfx.util.ViewHelper;
import com.calendarfx.view.TimeScaleView;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TimeScaleViewSkin<T extends TimeScaleView> extends DayViewBaseSkin<T> {

    private static final String EARLY_HOUR_LABEL = "early-hour-label";
    private static final String LATE_HOUR_LABEL = "late-hour-label";

    private final List<Label> timeLabels = new ArrayList<>();

    private final Label currentTimeLabel;

    public TimeScaleViewSkin(T view) {
        super(view);

        currentTimeLabel = new Label();
        currentTimeLabel.getStyleClass().add("current-time-label");
        currentTimeLabel.setManaged(false);
        currentTimeLabel.setMaxWidth(Double.MAX_VALUE);
        currentTimeLabel.setAlignment(Pos.CENTER_RIGHT);
        currentTimeLabel.setOpacity(0);
        currentTimeLabel.setTextOverrun(OverrunStyle.CLIP);
        currentTimeLabel.visibleProperty().bind(view.enableCurrentTimeMarkerProperty());

        //().add(currentTimeLabel);

        updateCurrentTimeMarkerVisibility();
        view.showCurrentTimeMarkerProperty().addListener(it -> updateCurrentTimeMarkerVisibility());
        setupCurrentTimeMarkerSupport();
        updateShowMarkers();

        view.scrollingEnabledProperty().addListener(it -> {
            timeLabels.clear();
            getChildren().clear();
            view.requestLayout();
        });

        view.scrollTimeProperty().addListener(it -> {
            if (view.isScrollingEnabled()) {
                view.requestLayout();
            }
        });

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);
    }

    private Label createTimeLabel() {
        Label label = new Label();
        label.setManaged(false);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setTextOverrun(OverrunStyle.CLIP);
        timeLabels.add(label);
        //getChildren().add(label);
        return label;
    }

    private void updateCurrentTimeMarkerVisibility() {
        double opacity = getSkinnable().isShowCurrentTimeMarker() ? 1 : 0;

        FadeTransition lineTransition = new FadeTransition(Duration.millis(600), currentTimeLabel);
        lineTransition.setToValue(opacity);
        lineTransition.play();

        /*
         * Need to re-layout as regular time labels might be invisible because
         * they were intersecting with the current time label.
         */
        getSkinnable().requestLayout();
    }

    protected void setupCurrentTimeMarkerSupport() {
        T view = getSkinnable();
        // do not use an invalidation listener as that will also fire for same dates
        ChangeListener listener = (obs, oldValue, newValue) -> updateShowMarkers();
        view.dateProperty().addListener(listener);
        view.todayProperty().addListener(listener);
    }

    private void updateShowMarkers() {
        T view = getSkinnable();
        view.getProperties().put("show.current.time.marker", isShowingTimeMarker());
    }

    protected boolean isShowingTimeMarker() {
        return getSkinnable().getDate().equals(getSkinnable().getToday());
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        if (getSkinnable().isScrollingEnabled()) {
            layoutChildrenInfiniteScrolling(contentX, contentY, contentWidth, contentHeight);
        } else {
            layoutChildrenStatic(contentX, contentY, contentWidth, contentHeight);
        }
    }

    private void layoutChildrenInfiniteScrolling(double contentX, double contentY, double contentWidth, double contentHeight) {
        final T view = getSkinnable();
        final ZonedDateTime scrollTime = view.getScrollTime();

        Instant time = scrollTime.withHour(0).toInstant().truncatedTo(ChronoUnit.HOURS);

        double y = view.getLocation(time);

        int index = 0;

        Bounds lastBoundsUsed = null;

        do {
            final LocalTime localTime = LocalTime.ofInstant(time, view.getZoneId());
            boolean midnight = localTime.equals(LocalTime.MIN);

            Label label;

            if (index < timeLabels.size()) {
                label = timeLabels.get(index);
            } else {
                label = createTimeLabel();
            }

            if (midnight) {
                label.getStyleClass().setAll("label", "date-label");
            } else {
                label.getStyleClass().setAll("label", "time-label");
            }

            double prefHeight = label.prefHeight(contentWidth);

            if (midnight) {
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(time, view.getZoneId());
                label.setText(dateTime.toLocalDate().format(view.getDateFormatter()));
                label.setStyle(view.getDateStyleProvider().apply(dateTime.toLocalDateTime()));
            } else {
                ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(time, view.getZoneId());
                label.setText(zonedDateTime.toLocalTime().format(view.getTimeFormatter()));
                label.setStyle(view.getTimeStyleProvider().apply(zonedDateTime.toLocalDateTime()));
            }

            final BoundingBox layoutBounds = new BoundingBox(snapPositionX(contentX), snapPositionY(y - prefHeight / 2), snapSizeX(contentWidth), snapSizeY(prefHeight));

            boolean labelVisible = lastBoundsUsed == null || !layoutBounds.intersects(lastBoundsUsed);

            if (!labelVisible) {

                if (midnight) {
                    labelVisible = true;
                }

            }
            label.setVisible(labelVisible);

            if (labelVisible) {
                label.resizeRelocate(layoutBounds.getMinX(), layoutBounds.getMinY(), layoutBounds.getWidth(), layoutBounds.getHeight());
                lastBoundsUsed = layoutBounds;
            }


            index++;

            time = time.plus(60, ChronoUnit.MINUTES);
            y = view.getLocation(time);

        } while (y < contentY + contentHeight);

        for (int i = index; i < timeLabels.size(); i++) {
            timeLabels.get(i).setVisible(false);
        }
    }

    private void layoutChildrenStatic(double contentX, double contentY, double contentWidth, double contentHeight) {
        // now label
        LocalTime now = getSkinnable().getTime();
        currentTimeLabel.setText(now.format(getSkinnable().getTimeFormatter()));
        placeLabel(currentTimeLabel, now, contentX, contentY, contentWidth, contentHeight);

        // hour labels
        LocalTime startTime = getSkinnable().getStartTime();
        LocalTime endTime = getSkinnable().getEndTime();

        for (int hour = 0; hour < 23; hour++) {
            LocalTime time = LocalTime.of(hour + 1, 0);
            Label label;

            if (hour < timeLabels.size()) {
                label = timeLabels.get(hour);
            } else {
                label = createTimeLabel();
                label.getStyleClass().add("time-label");
            }

            label.getStyleClass().removeAll(EARLY_HOUR_LABEL, LATE_HOUR_LABEL);
            label.setStyle(getSkinnable().getTimeStyleProvider().apply(time.atDate(getSkinnable().getDate())));

            placeLabel(label, time, contentX, contentY, contentWidth, contentHeight);

            Bounds localToParent1 = currentTimeLabel.localToParent(currentTimeLabel.getLayoutBounds());
            Bounds localToParent2 = label.localToParent(label.getLayoutBounds());

            label.setVisible(!currentTimeLabel.isVisible() || !getSkinnable().isShowCurrentTimeMarker() || !localToParent1.intersects(localToParent2));

            if (time.isBefore(startTime) && !label.getStyleClass().contains(EARLY_HOUR_LABEL)) {
                label.getStyleClass().add(EARLY_HOUR_LABEL);
            }
            if (time.isAfter(endTime) && !label.getStyleClass().contains(LATE_HOUR_LABEL)) {
                label.getStyleClass().add(LATE_HOUR_LABEL);
            }

            if (label.isVisible()) {
                switch (getSkinnable().getEarlyLateHoursStrategy()) {
                    case HIDE:
                    case SHOW_COMPRESSED:
                        if (time.isBefore(startTime) || time.isAfter(endTime)) {
                            label.setVisible(false);
                        }
                        break;
                    case SHOW:
                        label.setVisible(true);
                        break;
                    default:
                        break;

                }
            }
        }

        currentTimeLabel.toFront();
    }

    private void placeLabel(Label label, LocalTime time, double contentX, double contentY, double contentWidth, double contentHeight) {

        double prefHeight = label.prefHeight(contentWidth);

        double y = contentY + ViewHelper.getTimeLocation(getSkinnable(), time, true);

        /*
         * Min and max calculations to ensure text is completetextAlignment = nullly visible at the
         * top and the bottom.
         */
        y = Math.min(contentHeight - label.getFont().getSize(), Math.max(0, ((int) (y - prefHeight / 2)) + .5));

        label.setText(time.format(getSkinnable().getTimeFormatter()));
        label.resizeRelocate(snapPositionX(contentX), snapPositionY(y), snapSizeX(contentWidth), snapSizeY(prefHeight));
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double width = 0;

        for (Label label : timeLabels) {
            width = Math.max(width, label.prefWidth(-1));
        }

        return width + leftInset + rightInset;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }
}
