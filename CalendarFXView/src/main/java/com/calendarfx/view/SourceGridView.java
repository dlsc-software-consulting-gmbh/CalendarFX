/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import impl.com.calendarfx.view.SourceGridViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Skin;
import org.controlsfx.control.PropertySheet.Item;

import java.util.Optional;

public class SourceGridView extends CalendarFXControl {

	private static final int DEFAULT_MAXIMUM_ROWS_PER_COLUMN = 5;
	
	@Override
	protected Skin<?> createDefaultSkin() {
		return new SourceGridViewSkin(this);
	}

	public final void bind(DateControl dateControl) {
		Bindings.bindContentBidirectional(calendarSources, dateControl.getCalendarSources());
		Bindings.bindContentBidirectional(calendarVisibilityMap, dateControl.getCalendarVisibilityMap());
	}

	public final void unbind(DateControl dateControl) {
		Bindings.unbindContentBidirectional(calendarSources, dateControl.getCalendarSources());
		Bindings.unbindContentBidirectional(calendarVisibilityMap, dateControl.getCalendarVisibilityMap());
	}

	private final ObservableList<CalendarSource> calendarSources = FXCollections.observableArrayList();

	public final ObservableList<CalendarSource> getCalendarSources() {
		return calendarSources;
	}

	private final ObservableMap<Calendar, BooleanProperty> calendarVisibilityMap = FXCollections.observableHashMap();

	public final ObservableMap<Calendar, BooleanProperty> getCalendarVisibilityMap() {
		return calendarVisibilityMap;
	}

	public final BooleanProperty getCalendarVisibilityProperty(Calendar calendar) {
		return calendarVisibilityMap.computeIfAbsent(calendar, cal -> new SimpleBooleanProperty(SourceGridView.this, "visible", true));
	}

	public final boolean isCalendarVisible(Calendar calendar) {
		BooleanProperty prop = getCalendarVisibilityProperty(calendar);
		return prop.get();
	}

	public final void setCalendarVisibility(Calendar calendar, boolean visible) {
		BooleanProperty prop = getCalendarVisibilityProperty(calendar);
		prop.set(visible);
	}

	private final IntegerProperty maximumRowsPerColumn = new SimpleIntegerProperty(this, "maximumRowsPerColumn", DEFAULT_MAXIMUM_ROWS_PER_COLUMN) {
		@Override
		public void set(int newValue) {
			if (newValue < 1) {
				throw new RuntimeException("Invalid Number! " + newValue);
			}
			super.set(newValue);
		}
	};

	public final IntegerProperty maximumRowsPerColumnProperty() {
		return maximumRowsPerColumn;
	}

	public final int getMaximumRowsPerColumn() {
		return maximumRowsPerColumnProperty().get();
	}
	

	public final void setMaximumRowsPerColumn(final int maximumRowsPerColumn) {
		maximumRowsPerColumnProperty().set(maximumRowsPerColumn);
	}
	
	private static final String SOURCE_GRID_VIEW_CATEGORY = "Source Grid View";
	
	@Override
	public ObservableList<Item> getPropertySheetItems() {
		ObservableList<Item> items = super.getPropertySheetItems();
		
		items.add(new Item() {
			@Override
			public void setValue(Object value) {
				setMaximumRowsPerColumn((int) value);
			}
			
			@Override
			public Object getValue() {
				return getMaximumRowsPerColumn();
			}
			
			@Override
			public Class<?> getType() {
				return Integer.class;
			}
			
			@Override
			public Optional<ObservableValue<?>> getObservableValue() {
				return Optional.of(maximumRowsPerColumnProperty());
			}
			
			@Override
			public String getName() {
				return "Maximum Rows Per Column";
			}
			
			@Override
			public String getDescription() {
				return "The maximum number of rows per column";
			}
			
			@Override
			public String getCategory() {
				return SOURCE_GRID_VIEW_CATEGORY;
			}
		});
		
		return items;
	}

}
