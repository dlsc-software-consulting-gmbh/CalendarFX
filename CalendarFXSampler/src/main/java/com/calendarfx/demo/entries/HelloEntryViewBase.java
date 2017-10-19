/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo.entries;

import org.controlsfx.control.PropertySheet;

import com.calendarfx.demo.CalendarFXSample;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.EntryViewBase;

import impl.com.calendarfx.view.CalendarPropertySheet;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;

public abstract class HelloEntryViewBase extends CalendarFXSample {

	private static final String ORANGE_BACKGROUND_STYLE = "orange-background";
	private static final String RED_BORDER_STYLE = "red-border";

	protected EntryViewBase<?> entryView;
	protected Entry<?> entry;

	public HelloEntryViewBase() {
		Calendar calendar = new Calendar("Test Calendar");
		entry = new Entry<>("Test Entry");
		entry.setCalendar(calendar);
	}

	@Override
	protected Node createControl() {
		entryView = createEntryView(entry);
		entryView.getStylesheets().add(HelloDayEntryView.class.getResource("entry.css").toExternalForm());
		control = entryView;

		ContextMenu menu = new ContextMenu();

		// red border
		CheckMenuItem redBorder = new CheckMenuItem("Red Border");
		menu.getItems().add(redBorder);
		redBorder.setOnAction(evt -> {
			if (redBorder.isSelected()) {
				entry.getStyleClass().add(RED_BORDER_STYLE);
			} else {
				entry.getStyleClass().remove(RED_BORDER_STYLE);
			}
		});

		// red background
		CheckMenuItem redBackground = new CheckMenuItem("Orange Background");
		menu.getItems().add(redBackground);
		redBackground.setOnAction(evt -> {
			if (redBackground.isSelected()) {
				entry.getStyleClass().add(ORANGE_BACKGROUND_STYLE);
			} else {
				entry.getStyleClass().remove(ORANGE_BACKGROUND_STYLE);
			}
		});

		entryView.setContextMenu(menu);

		return entryView;
	}

	protected abstract EntryViewBase<?> createEntryView(Entry<?> entry);

	@Override
	public Node getControlPanel() {
		PropertySheet sheet = new CalendarPropertySheet(entryView.getPropertySheetItems());
		sheet.getItems().addAll(entry.getPropertySheetItems());
		return sheet;
	}
}
