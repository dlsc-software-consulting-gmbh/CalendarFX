package impl.com.calendarfx.view;

import com.calendarfx.view.DayView;
import com.calendarfx.view.ResourceCalendarView;
import com.calendarfx.view.TimeScaleView;
import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import org.controlsfx.control.PlusMinusSlider;

public class ResourceCalendarViewSkin<T> extends DayViewBaseSkin<ResourceCalendarView<T>> {

    private GridPane gridPane = new GridPane();

    private TimeScaleView timeScaleView = new TimeScaleView();

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

        final InvalidationListener updateViewListener = it -> updateView();

        view.dayViewMapProperty().addListener(updateViewListener);
        view.overlapHeaderProperty().addListener(updateViewListener);
        view.showScrollBarProperty().addListener(updateViewListener);

        updateView();
    }

    private void updateView() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();

        final int columnCounts = getSkinnable().getResources().size();

        ColumnConstraints con = new ColumnConstraints();
        con.setPrefWidth(Region.USE_COMPUTED_SIZE);
        con.setFillWidth(true);

        gridPane.getColumnConstraints().add(con);

        if (getSkinnable().isOverlapHeader()) {
            gridPane.add(timeScaleView, 0, 0);
            GridPane.setRowSpan(timeScaleView, 2);
        } else {
            gridPane.add(timeScaleView, 0, 1);
            GridPane.setRowSpan(timeScaleView, 1);
        }

        for (int i = 1; i <= columnCounts; i++) {
            T resource = getSkinnable().getResources().get(i - 1);

            con = new ColumnConstraints();
            con.setHalignment(HPos.CENTER);
            con.setFillWidth(true);
            con.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(con);

            DayView dayView = getSkinnable().getDayView(resource);

            GridPane.setFillHeight(dayView, true);
            GridPane.setVgrow(dayView, Priority.ALWAYS);

            if (getSkinnable().isOverlapHeader()) {
                gridPane.add(dayView, i, 0);
                GridPane.setRowSpan(dayView, 2);
            } else {
                gridPane.add(dayView, i, 1);
                GridPane.setRowSpan(dayView, 1);
            }
        }

        Region header = new Region();
        header.getStyleClass().add("header-background");
        gridPane.add(header, 0, 0);
        GridPane.setColumnSpan(header, columnCounts + 2);

        for (int i = 1; i <= columnCounts; i++) {
            T resource = getSkinnable().getResources().get(i - 1);

            Node columnHeader = getSkinnable().getHeaderFactory().call(resource);

            if (getSkinnable().isOverlapHeader()) {
                gridPane.add(columnHeader, i, 0);
            } else {
                gridPane.add(columnHeader, i, 0);
            }
        }

        if (getSkinnable().isShowScrollBar()) {
            // slider column
            con = new ColumnConstraints();
            con.setPrefWidth(Region.USE_COMPUTED_SIZE);
            con.setFillWidth(true);
            gridPane.getColumnConstraints().add(con);

            PlusMinusSlider slider = new PlusMinusSlider();
            slider.setOrientation(Orientation.VERTICAL);
            gridPane.add(slider, columnCounts + 1, 1);
            slider.setOnValueChanged(evt -> {
                // exponential function to increase scrolling speed when reaching ends of slider
                final double base = slider.getValue();
                final double pow = Math.signum(slider.getValue()) * Math.pow(base, 2);
                final double pixel = pow * -100;
                getSkinnable().setScrollTime(getSkinnable().getZonedDateTimeAt(0, pixel));
            });
        }
    }
}
