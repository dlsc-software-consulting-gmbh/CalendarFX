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

import com.calendarfx.view.TimeScaleView;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class TimeScaleViewSkin<T extends TimeScaleView> extends
        DayViewBaseSkin<T> {

    private List<Label> labels = new ArrayList<>();

    private Label currentTimeLabel;

    private final DateTimeFormatter formatter = DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT);

    public TimeScaleViewSkin(T view) {
        super(view);

        LocalTime time = LocalTime.of(1, 0);

        for (int i = 1; i < 24; i++) {
            Label label = new Label(time.format(formatter));
            label.setManaged(false);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER_RIGHT);
            label.getStyleClass().add("time-label"); //$NON-NLS-1$
            label.setTextOverrun(OverrunStyle.CLIP);
            labels.add(label);
            getChildren().add(label);
            time = time.plusHours(1);
        }

        currentTimeLabel = new Label();
        currentTimeLabel.getStyleClass().add("current-time-label"); //$NON-NLS-1$
        currentTimeLabel.setManaged(false);
        currentTimeLabel.setMaxWidth(Double.MAX_VALUE);
        currentTimeLabel.setAlignment(Pos.CENTER_RIGHT);
        currentTimeLabel.setOpacity(0);
        currentTimeLabel.setTextOverrun(OverrunStyle.CLIP);
        currentTimeLabel.visibleProperty().bind(view.enableCurrentTimeMarkerProperty());

        getChildren().add(currentTimeLabel);

        updateCurrentTimeMarkerVisibility();
        view.showCurrentTimeMarkerProperty().addListener(
                it -> updateCurrentTimeMarkerVisibility());
        setupCurrentTimeMarkerSupport();
        updateShowMarkers();
    }

    private void updateCurrentTimeMarkerVisibility() {
        double opacity = getSkinnable().isShowCurrentTimeMarker() ? 1 : 0;

        FadeTransition lineTransition = new FadeTransition(
                Duration.millis(600), currentTimeLabel);
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
        InvalidationListener listener = evt -> updateShowMarkers();
        view.dateProperty().addListener(listener);
        view.todayProperty().addListener(listener);
    }

    private void updateShowMarkers() {
        T view = getSkinnable();
        view.getProperties().put("show.current.time.marker", //$NON-NLS-1$
                isShowingTimeMarker());
    }

    protected boolean isShowingTimeMarker() {
        return getSkinnable().getDate().equals(getSkinnable().getToday());
    }

    @Override
    protected void layoutChildren(double contentX, double contentY,
                                  double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        int labelCount = labels.size();

        // now label
        LocalTime now = getSkinnable().getTime();
        currentTimeLabel.setText(now.format(formatter));
        placeLabel(currentTimeLabel, now, contentX, contentY, contentWidth,
                contentHeight);

        // hour labels
        LocalTime startTime = getSkinnable().getStartTime();
        LocalTime endTime = getSkinnable().getEndTime();

        for (int hour = 0; hour < labelCount; hour++) {
            LocalTime time = LocalTime.of(hour + 1, 0);
            Label label = labels.get(hour);

            label.getStyleClass().removeAll("early-hour-label", //$NON-NLS-1$
                    "late-hour-label"); //$NON-NLS-1$

            placeLabel(label, time, contentX, contentY, contentWidth,
                    contentHeight);

            Bounds localToParent1 = currentTimeLabel
                    .localToParent(currentTimeLabel.getLayoutBounds());
            Bounds localToParent2 = label
                    .localToParent(label.getLayoutBounds());

            if (currentTimeLabel.isVisible()
                    && getSkinnable().isShowCurrentTimeMarker()
                    && localToParent1.intersects(localToParent2)) {
                label.setVisible(false);
            } else {
                label.setVisible(true);
            }

            if (time.isBefore(startTime)) {
                if (!label.getStyleClass().contains("early-hour-label")) { //$NON-NLS-1$
                    label.getStyleClass().add("early-hour-label"); //$NON-NLS-1$
                }
            }
            if (time.isAfter(endTime)) {
                if (!label.getStyleClass().contains("late-hour-label")) { //$NON-NLS-1$
                    label.getStyleClass().add("late-hour-label"); //$NON-NLS-1$
                }
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

    private void placeLabel(Label label, LocalTime time, double contentX,
                            double contentY, double contentWidth, double contentHeight) {

        double prefHeight = label.prefHeight(contentWidth);

        double y = contentY + ViewHelper.getTimeLocation(getSkinnable(), time, true);

        /*
         * Min and max calculations to ensure text is completely visible at the
         * top and the bottom.
         */
        y = Math.min(contentHeight - label.getFont().getSize(),
                Math.max(0, ((int) (y - prefHeight / 2)) + .5));

        label.resizeRelocate(snapPosition(contentX), snapPosition(y), snapSize(contentWidth), snapSize(prefHeight));
    }

    @Override
    protected double computePrefWidth(double height, double topInset,
                                      double rightInset, double bottomInset, double leftInset) {

        double width = 0;

        for (Label label : labels) {
            width = Math.max(width, label.prefWidth(-1));
        }

        return width + leftInset + rightInset;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }
}
