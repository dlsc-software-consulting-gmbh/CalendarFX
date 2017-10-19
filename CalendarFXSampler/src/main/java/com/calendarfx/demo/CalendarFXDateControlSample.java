/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo;

import static java.util.Objects.requireNonNull;

import org.controlsfx.control.MasterDetailPane;

import com.calendarfx.view.DateControl;
import com.calendarfx.view.DeveloperConsole;

import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public abstract class CalendarFXDateControlSample extends CalendarFXSample {

	@Override
	public final Node getPanel(Stage stage) {
		DateControl dateControl = createControl();
		control = dateControl;
		requireNonNull(control, "missing date control");
		
		DeveloperConsole console = new DeveloperConsole();
		console.setDateControl(dateControl);

		if (isSupportingDeveloperConsole()) {
			MasterDetailPane masterDetailPane = new MasterDetailPane();
			masterDetailPane.setMasterNode(wrap(dateControl));
			masterDetailPane.setDetailSide(Side.BOTTOM);
			masterDetailPane.setDetailNode(console);
			masterDetailPane.setShowDetailNode(true);
			return masterDetailPane;
		}

		return wrap(dateControl);
	}
	
	@Override
	protected Node wrap (Node node) {
		StackPane outerPane = new StackPane();
		outerPane.setStyle("-fx-padding: 20px;");

		StackPane stackPane = new StackPane();
		stackPane.setStyle(
				"-fx-background-color: white; -fx-border-color: gray; -fx-border-width: .25px; -fx-padding: 20px;");
		outerPane.getChildren().add(stackPane);

		StackPane.setAlignment(node, Pos.CENTER);
		stackPane.getChildren().add(node);

		return outerPane;
	}
	
	protected boolean isSupportingDeveloperConsole() {
		return true;
	}
	
	@Override
	protected abstract DateControl createControl();
	
}
