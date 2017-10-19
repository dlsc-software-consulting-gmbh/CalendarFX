/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.Observable;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleGroup;
import javafx.scene.shape.Rectangle;

import com.calendarfx.model.Calendar;
import com.calendarfx.view.CalendarSelector;
import com.calendarfx.view.CalendarView;

@SuppressWarnings("javadoc")
public class CalendarSelectorSkin extends SkinBase<CalendarSelector> {

	private MenuButton button;
	private Rectangle buttonIcon;

	public CalendarSelectorSkin(CalendarSelector selector) {
		super(selector);

		buttonIcon = new Rectangle(12, 12);

		button = new MenuButton();
		button.setGraphic(buttonIcon);
		button.getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm()); //$NON-NLS-1$

		getChildren().add(button);

		selector.calendarProperty().addListener(it -> updateButton());

		selector.getCalendars().addListener(
				(Observable evt) -> updateMenuItems());

		updateMenuItems();
		updateButton();
	}

	private void updateButton() {
		Calendar calendar = getSkinnable().getCalendar();
		if (calendar != null) {
			buttonIcon.getStyleClass().setAll(calendar.getStyle() + "-icon"); //$NON-NLS-1$
		} else {
			buttonIcon.getStyleClass().clear();
		}
	}

	private void updateMenuItems() {
		ToggleGroup group = new ToggleGroup();
		List<MenuItem> items = new ArrayList<>();
		for (Calendar calendar : getSkinnable().getCalendars()) {
			RadioMenuItem item = new RadioMenuItem(calendar.getName());
			Rectangle icon = new Rectangle(10, 10);
			icon.setArcHeight(2);
			icon.setArcWidth(2);
			icon.getStyleClass().add(calendar.getStyle() + "-icon"); //$NON-NLS-1$
			item.setGraphic(icon);
			item.setDisable(calendar.isReadOnly());
			item.setOnAction(evt -> getSkinnable().setCalendar(calendar));
			group.getToggles().add(item);
			items.add(item);
			if (calendar.equals(getSkinnable().getCalendar())) {
				item.setSelected(true);
			}
		}

		button.getItems().setAll(items);
	}
}
