/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.print;

import com.calendarfx.view.Messages;
import com.calendarfx.view.print.TimeRangeView;

import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class TimeRangeViewSkin extends SkinBase<TimeRangeView> {

	public TimeRangeViewSkin(TimeRangeView control) {
		super(control);

		Label overviewLabel = new Label();
		overviewLabel.textProperty()
				.bind(Bindings
						.createStringBinding(
								() -> control.getUnitsToPrint() == 0 ? ""
										: Messages
												.getString("TimeRangeViewSkin.PERIOD_LABEL", control.getUnitsToPrint(),
														Messages.getString(
																control.getViewType().getPluralChronoMessageKey())),
								control.unitsToPrintProperty(), control.viewTypeProperty()));

		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("container");
		gridPane.add(new Label(Messages.getString("TimeRangeViewSkin.START_LABEL")), 0, 0);
		gridPane.add(control.getStartField(), 1, 0);
		gridPane.add(new Label(Messages.getString("TimeRangeViewSkin.END_LABEL")), 0, 1);
		gridPane.add(control.getEndField(), 1, 1);
		gridPane.add(overviewLabel, 1, 2);

		ColumnConstraints col1 = new ColumnConstraints();
		ColumnConstraints col2 = new ColumnConstraints();
		gridPane.getColumnConstraints().addAll(col1, col2);

		GridPane.setHalignment(control.getStartField(), HPos.LEFT);
		GridPane.setHalignment(control.getEndField(), HPos.LEFT);

		GridPane.setFillWidth(control.getStartField(), true);
		GridPane.setFillWidth(control.getEndField(), true);

		getChildren().add(gridPane);
	}

}
