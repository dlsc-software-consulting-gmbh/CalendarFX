package impl.com.calendarfx.view;

import com.calendarfx.view.ButtonBar;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;

public class NavigateDateView extends ButtonBar {

    private final Button backButton = new Button("<");
    private final Button todayButton = new Button("Today");
    private final Button forwardButton = new Button(">");

    public NavigateDateView() {
        super(FXCollections.observableArrayList());

        backButton.getStyleClass().add("previous-date-button");
        todayButton.getStyleClass().add("today-button");
        forwardButton.getStyleClass().add("next-date-button");

        backButton.disableProperty().bind(onBackward.isNull());
        todayButton.disableProperty().bind(onToday.isNull());
        forwardButton.disableProperty().bind(onForward.isNull());

        backButton.setOnAction(evt -> getOnBackward().run());
        forwardButton.setOnAction(evt -> getOnForward().run());
        todayButton.setOnAction(evt -> getOnToday().run());

        getButtons().setAll(backButton, todayButton, forwardButton);
        getStyleClass().add("navigate-date-view");
    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getTodayButton() {
        return todayButton;
    }

    public Button getForwardButton() {
        return forwardButton;
    }

    private final ObjectProperty<Runnable> onForward = new SimpleObjectProperty<>(this, "onForward");

    public Runnable getOnForward() {
        return onForward.get();
    }

    public ObjectProperty<Runnable> onForwardProperty() {
        return onForward;
    }

    public void setOnForward(Runnable onForward) {
        this.onForward.set(onForward);
    }

    private final ObjectProperty<Runnable> onToday = new SimpleObjectProperty<>(this, "onToday");

    public Runnable getOnToday() {
        return onToday.get();
    }

    public ObjectProperty<Runnable> onTodayProperty() {
        return onToday;
    }

    public void setOnToday(Runnable onToday) {
        this.onToday.set(onToday);
    }

    private final ObjectProperty<Runnable> onBackward = new SimpleObjectProperty<>(this, "onBackward");

    public Runnable getOnBackward() {
        return onBackward.get();
    }

    public ObjectProperty<Runnable> onBackwardProperty() {
        return onBackward;
    }

    public void setOnBackward(Runnable onBackward) {
        this.onBackward.set(onBackward);
    }
}
