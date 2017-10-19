/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.print;

import com.calendarfx.view.print.SettingsView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SettingsViewSkin extends SkinBase<SettingsView> {

    public SettingsViewSkin(SettingsView control) {
        super(control);

        VBox container = new VBox();
        container.getStyleClass().add("container");

        ScrollPane scrollPane = new ScrollPane(control.getSourceView());
        scrollPane.setPrefViewportHeight(180);

        container.getChildren().addAll(
                new SectionTitle("Paper"),
                control.getPaperView(),
                new SectionTitle("Time Range"),
                control.getTimeRangeView(),
                new SectionTitle("Calendars"),
                scrollPane,
                new SectionTitle("Options"),
                control.getOptionsView());

        getChildren().add(container);
    }

    private static class SectionTitle extends HBox {

        public SectionTitle(String name) {
            getStyleClass().add("section-title");

            Label titleLabel = new Label(name);
            Separator separator = new Separator();
            separator.setPadding(new Insets(5, 0, 0, 0));

            HBox.setHgrow(separator, Priority.ALWAYS);
            HBox.setHgrow(titleLabel, Priority.NEVER);

            setSpacing(10);
            setAlignment(Pos.CENTER);

            getChildren().addAll(titleLabel, separator);
        }
    }
}
