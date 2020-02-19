package impl.com.calendarfx.view;

import com.calendarfx.model.Marker;
import com.calendarfx.view.DayView;
import com.calendarfx.view.ResourceCalendarView;
import com.calendarfx.view.TimeScaleView;
import com.calendarfx.view.VirtualGrid;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.PlusMinusSlider;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class ResourceCalendarViewSkin<T> extends DayViewBaseSkin<ResourceCalendarView<T>> {

    private GridPane gridPane = new GridPane();

    private CustomGridPane innerGridPane = new CustomGridPane();

    private TimeScaleView timeScaleView = new TimeScaleView();

    private PlusMinusSlider slider;

    public ResourceCalendarViewSkin(ResourceCalendarView view) {
        super(view);

        timeScaleView.setScrollingEnabled(true);
        view.bind(timeScaleView, true);

        gridPane.getStyleClass().add("resource-calendar-container");

        getChildren().add(gridPane);

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();

        row1.setVgrow(Priority.NEVER);
        row2.setVgrow(Priority.ALWAYS);

        gridPane.getRowConstraints().setAll(row1, row2);

        final InvalidationListener updateGridPaneListener = it -> updateView();

        view.dayViewMapProperty().addListener(updateGridPaneListener);
        view.showScrollBarProperty().addListener(updateGridPaneListener);
        view.markersProperty().addListener(updateGridPaneListener);

        updateView();
    }

    private void updateView() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();

        ColumnConstraints con = new ColumnConstraints();
        con.setPrefWidth(Region.USE_COMPUTED_SIZE);
        con.setFillWidth(true);

        gridPane.getColumnConstraints().add(con);

        gridPane.add(timeScaleView, 0, 1);

        final int columnCounts = getSkinnable().getResources().size();

        Region header = new Region();
        header.getStyleClass().add("header-background");
        gridPane.add(header, 0, 0);
        GridPane.setColumnSpan(header, columnCounts + 2);

        for (int i = 0; i < columnCounts; i++) {
            con = new ColumnConstraints();
            con.setHalignment(HPos.CENTER);
            con.setFillWidth(true);
            con.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(con);

            T resource = getSkinnable().getResources().get(i);

            Node columnHeader = getSkinnable().getHeaderFactory().call(resource);

            gridPane.add(columnHeader, i + 1, 0);
        }

        gridPane.add(innerGridPane, 1, 1);
        GridPane.setColumnSpan(innerGridPane, columnCounts);
        GridPane.setFillWidth(innerGridPane, true);

        innerGridPane.updateView();

        if (getSkinnable().isShowScrollBar()) {
            // slider column
            con = new ColumnConstraints();
            con.setPrefWidth(Region.USE_COMPUTED_SIZE);
            con.setFillWidth(true);
            gridPane.getColumnConstraints().add(con);

            slider = new PlusMinusSlider();
            slider.setOrientation(Orientation.VERTICAL);
            gridPane.add(slider, columnCounts + 2, 1);
            slider.setOnValueChanged(evt -> {
                // exponential function to increase scrolling speed when reaching ends of slider
                final double base = slider.getValue();
                final double pow = Math.signum(slider.getValue()) * Math.pow(base, 2);
                final double pixel = pow * -100;
                getSkinnable().setScrollTime(getSkinnable().getZonedDateTimeAt(0, pixel));
            });
        }
    }

    public class CustomGridPane extends GridPane {

        private MarkerLine draggedMarkerLine;

        private InvalidationListener markerListener = it -> Platform.runLater(() -> getSkinnable().layout());

        private WeakInvalidationListener weakMarkerListener = new WeakInvalidationListener(markerListener);

        private ObservableMap<Marker, MarkerLine> markerLineMap = FXCollections.observableMap(new HashMap<>());

        private double startY;

        public CustomGridPane() {
            addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
                startY = evt.getScreenY();
                if (evt.getTarget() instanceof MarkerLine) {
                    draggedMarkerLine = (MarkerLine) evt.getTarget();
                    draggedMarkerLine.setCursor(Cursor.CLOSED_HAND);
                }
            });

            addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
                if (draggedMarkerLine != null) {
                    draggedMarkerLine.setCursor(Cursor.HAND);
                    final double y = draggedMarkerLine.getLayoutY();
                    adjustLineLocation(draggedMarkerLine, y);
                    draggedMarkerLine = null;
                }
            });

            addEventFilter(MouseEvent.MOUSE_DRAGGED, evt -> {
                if (draggedMarkerLine != null) {
                    double y = evt.getScreenY();
                    double delta = startY - y;
                    double newLocation = draggedMarkerLine.getLayoutY() - delta;
                    draggedMarkerLine.setLayoutY(newLocation);
                    startY = y;
                }
            });

            ListChangeListener<Marker> l = change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(marker -> addMarkerLine(marker));
                    } else if (change.wasRemoved()) {
                        change.getRemoved().forEach(marker -> {
                            marker.timeProperty().removeListener(weakMarkerListener);
                            getChildren().remove(markerLineMap.get(marker));
                        });
                    }
                }
            };

            getSkinnable().markersProperty().addListener(l);

            final ObservableList<Marker> markers = getSkinnable().getMarkers();
            markers.forEach(marker -> addMarkerLine(marker));

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(widthProperty());
            clip.heightProperty().bind(heightProperty());
            setClip(clip);
        }

        private void adjustLineLocation(MarkerLine markerLine, double y) {
            ZonedDateTime dropTime = getSkinnable().getZonedDateTimeAt(0, y);
            final VirtualGrid virtualGrid = getSkinnable().getVirtualGrid();
            if (virtualGrid != null) {

                ZonedDateTime timeA = virtualGrid.adjustTime(dropTime, true, getSkinnable().getFirstDayOfWeek());
                ZonedDateTime timeB = virtualGrid.adjustTime(dropTime, false, getSkinnable().getFirstDayOfWeek());

                final long secondsA = Math.abs(timeA.until(dropTime, ChronoUnit.SECONDS));
                final long secondsB = Math.abs(timeB.until(dropTime, ChronoUnit.SECONDS));

                // "jump / snap" to the closes grid time
                if (secondsA < secondsB) {
                    dropTime = timeA;
                } else {
                    dropTime = timeB;
                }
            }

            markerLine.getMarker().setTime(dropTime);
            final double dropLocationY = getSkinnable().getLocation(dropTime);
            markerLine.setLayoutY(dropLocationY - markerLine.prefHeight(-1));
        }

        private void addMarkerLine(Marker marker) {
            marker.timeProperty().addListener(weakMarkerListener);
            MarkerLine markerLine = new MarkerLine(marker);
            markerLineMap.put(marker, markerLine);
            markerLine.setManaged(false);
            getChildren().add(markerLine);
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            markerLineMap.values().forEach(line -> {
                final Marker marker = line.getMarker();

                /*
                 * Very important not to lay out the currently dragged marker line.
                 */
                if (line != draggedMarkerLine) {
                    final ZonedDateTime time = marker.getTime();
                    final double location = getSkinnable().getLocation(time);
                    MarkerLine markerLine = markerLineMap.get(marker);
                    double ph = markerLine.prefHeight(-1);

                    markerLine.toFront();

                    double x = getInsets().getLeft();
                    double w = getWidth() - getInsets().getLeft() - getInsets().getRight();

                    markerLine.resizeRelocate(x, snapPositionY(location - ph / 2), snapSizeX(w), snapSizeY(ph));
                }
            });
        }

        private void updateView() {
            getChildren().removeIf(node -> !(node instanceof MarkerLine));
            getColumnConstraints().clear();

            final int columnCounts = getSkinnable().getResources().size();

            for (int i = 0; i < columnCounts; i++) {
                T resource = getSkinnable().getResources().get(i);

                ColumnConstraints con = new ColumnConstraints();
                con.setHalignment(HPos.CENTER);
                con.setFillWidth(true);
                con.setHgrow(Priority.ALWAYS);
                innerGridPane.getColumnConstraints().add(con);

                DayView dayView = getSkinnable().getDayView(resource);

                GridPane.setFillHeight(dayView, true);
                GridPane.setVgrow(dayView, Priority.ALWAYS);

                innerGridPane.add(dayView, i, 1);
                GridPane.setRowSpan(dayView, 1);
            }
        }
    }

    private static class MarkerLine extends StackPane {

        private final Marker marker;

        public MarkerLine(Marker marker) {
            this.marker = marker;

            marker.styleClassProperty().addListener((Observable it) -> updateStyleClass());
            updateStyleClass();

            setCursor(Cursor.HAND);

            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(marker.titleProperty());
            Tooltip.install(this, tooltip);
        }

        private void updateStyleClass() {
            getStyleClass().setAll("marker-line");
            getStyleClass().addAll(marker.getStyleClass());
        }

        public Marker getMarker() {
            return marker;
        }
    }
}
