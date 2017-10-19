/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.SourceView;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

@SuppressWarnings("javadoc")
public class SourceViewSkin extends SkinBase<SourceView> {

    private VBox vbox;

    private final InvalidationListener updater = obs -> updateView();

    public SourceViewSkin(SourceView view) {
        super(view);

        vbox = new VBox();
        vbox.getStyleClass().add("container"); //$NON-NLS-1$

        getChildren().add(vbox);

        view.getCalendarSources().addListener(updater);
        updateView();
    }

    private void updateView() {
        vbox.getChildren().clear();
        for (CalendarSource source : getSkinnable().getCalendarSources()) {
            source.getCalendars().removeListener(updater);
            source.getCalendars().addListener(updater);

            VBox box = new VBox(8);
            box.getStyleClass().add("single-calendar-group");

            for (Calendar calendar : source.getCalendars()) {
                CheckBox checkBox = new CheckBox();
                checkBox.textProperty().bind(calendar.nameProperty());
                checkBox.getStyleClass().addAll("default-style-visibility-checkbox",//$NON-NLS-1$
                        calendar.getStyle() + "-visibility-checkbox"); //$NON-NLS-1$
                Bindings.bindBidirectional(checkBox.selectedProperty(), getSkinnable().getCalendarVisibilityProperty(calendar));
                box.getChildren().add(checkBox);
            }

            if (getSkinnable().getCalendarSources().size() == 1) {
                vbox.getChildren().add(box);
            } else {
                TitledPane titledPane = new TitledPane();
                titledPane.textProperty().bind(source.nameProperty());
                titledPane.setContent(box);
                vbox.getChildren().add(titledPane);
            }
        }
    }
}
