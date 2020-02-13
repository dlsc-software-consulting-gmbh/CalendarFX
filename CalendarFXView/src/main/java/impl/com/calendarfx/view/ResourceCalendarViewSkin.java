package impl.com.calendarfx.view;

import com.calendarfx.view.DayView;
import com.calendarfx.view.ResourceCalendarView;
import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class ResourceCalendarViewSkin<T> extends DayViewBaseSkin<ResourceCalendarView<T>> {

    private GridPane gridPane = new GridPane();

    public ResourceCalendarViewSkin(ResourceCalendarView view) {
        super(view);

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
        updateView();
    }

    private void updateView() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();

        final int columnCounts = getSkinnable().getResources().size();
        for (int i = 0; i < columnCounts; i++) {
            T resource = getSkinnable().getResources().get(i);

            ColumnConstraints con = new ColumnConstraints();
            con.setHalignment(HPos.CENTER);
            con.setPercentWidth(100.0 / columnCounts);
            con.setFillWidth(true);
            con.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(con);

            DayView dayView = getSkinnable().getDayView(resource);
            Node header = getSkinnable().getHeaderFactory().call(resource);

            GridPane.setFillHeight(dayView, true);
            GridPane.setVgrow(dayView, Priority.ALWAYS);

            if (getSkinnable().isOverlapHeader()) {
                gridPane.add(dayView, i, 0);
                gridPane.add(header, i, 0);
                GridPane.setRowSpan(dayView, 2);
            } else {
                gridPane.add(dayView, i, 1);
                gridPane.add(header, i, 0);
                GridPane.setRowSpan(dayView, 1);
            }
        }
    }
}
