/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;

/**
 * Common superclass for all controls in the calendar framework.
 */
public abstract class CalendarFXControl extends Control {

	private static String stylesheet;

	/**
	 * Constructs a new control.
	 */
	protected CalendarFXControl() {
    }

	@Override
	public final String getUserAgentStylesheet() {
		if (stylesheet == null) {
			stylesheet = CalendarFXControl.class.getResource("calendar.css") //$NON-NLS-1$
					.toExternalForm();
		}
		return stylesheet;
	}
	
	/**
     * Returns a list of property items that can be shown by the
     * {@link PropertySheet} of ControlsFX.
     *
     * @return the property sheet items
     */
    public ObservableList<Item> getPropertySheetItems() {
    	return FXCollections.observableArrayList();
    }
	
}
