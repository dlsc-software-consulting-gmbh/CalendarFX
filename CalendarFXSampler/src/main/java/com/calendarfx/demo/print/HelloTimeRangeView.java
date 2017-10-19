/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.print;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.view.print.TimeRangeView;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class HelloTimeRangeView extends CalendarFXSample {

	@Override
	public String getSampleName() {
		return "Time Range View";
	}
	
	@Override
	public String getSampleDescription() {
		return "Allows to configure the period to be printed via the print dialog";
	}

	@Override
	protected Class<?> getJavaDocClass() {
		return TimeRangeView.class;
	}

	@Override
	protected Node createControl() {
		return new TimeRangeView();
	}
	
	@Override
	protected Node wrap(Node node) {
		TimeRangeView field = (TimeRangeView) node;
		
		Label label = new Label("Range:   " + field.getStartDate() + "    to    " + field.getEndDate());
		label.setMaxHeight(Double.MAX_VALUE);
		field.startDateProperty().addListener(it -> label.setText("Range:   " + field.getStartDate() + "    to    " + field.getEndDate()));
		field.endDateProperty().addListener(it -> label.setText("Range:   " + field.getStartDate() + "    to    " + field.getEndDate()));
		
		VBox box2 = new VBox(20, field, label);
		box2.setFillWidth(false);
		
		StackPane stackPane = new StackPane();
		stackPane.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: .25px; -fx-padding: 20px;");
		stackPane.getChildren().add(box2);
		
		HBox box = new HBox(stackPane);
		box.setStyle("-fx-padding: 100px;");
		box.setAlignment(Pos.CENTER);
		box.setFillHeight(false);
		
		return box;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
