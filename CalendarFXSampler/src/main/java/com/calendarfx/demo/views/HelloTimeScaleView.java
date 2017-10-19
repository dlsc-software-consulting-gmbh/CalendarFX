/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.views;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.TimeScaleView;
import impl.com.calendarfx.view.DayViewScrollPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloTimeScaleView extends CalendarFXSample {

	@Override
	public String getSampleName() {
		return "Time Scale";
	}

	@Override
	protected Node createControl() {
		return null;
	}

	@Override
	public Node getPanel(Stage stage) {
		TimeScaleView view = new TimeScaleView();
		final DayViewScrollPane scrollPane = new DayViewScrollPane(view, new ScrollBar());
		scrollPane.setPrefHeight(2000);
		return wrap(scrollPane);
	}

	@Override
	public Node wrap(Node node) {

		HBox box = new HBox();
		box.setStyle("-fx-padding: 100px;");
		box.setAlignment(Pos.CENTER);
		box.setFillHeight(false);

		StackPane stackPane = new StackPane();
		stackPane.setStyle(
				"-fx-background-color: white; -fx-border-color: gray; -fx-border-width: .25px; -fx-padding: 0 20 0 20;");
		box.getChildren().add(stackPane);

		stackPane.getChildren().add(node);
		stackPane.setEffect(new Reflection());

		return box;
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return TimeScaleView.class;
	}

	@Override
	public String getSampleDescription() {
		return "The scale shows the time of day vertically.";
	}

	public static void main(String[] args) {
		launch(args);
	}

}
