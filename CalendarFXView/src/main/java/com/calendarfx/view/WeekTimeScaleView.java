/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import impl.com.calendarfx.view.WeekTimeScaleViewSkin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Skin;

/**
 * A specialization of the regular {@link TimeScaleView} to support a reference to the
 * {@link WeekView} where this scale is being used.
 */
public class WeekTimeScaleView extends TimeScaleView {

	/**
	 * Constructs a new scale view.
	 */
	public WeekTimeScaleView() {
		MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
			if (change.wasAdded()) {
				if (change.getKey().equals("week.view")) { //$NON-NLS-1$
					detailedWeekView.set((DetailedWeekView) change.getValueAdded());
				}
			}
		};

		getProperties().addListener(propertiesListener);
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new WeekTimeScaleViewSkin(this);
	}

	private final ReadOnlyObjectWrapper<DetailedWeekView> detailedWeekView = new ReadOnlyObjectWrapper<>(this, "detailedWeekView"); //$NON-NLS-1$

	/**
	 * The week view where this scale is being used.
	 *
	 * @return the week view
	 */
	public final ReadOnlyObjectProperty<DetailedWeekView> detailedWeekViewProperty() {
		return detailedWeekView.getReadOnlyProperty();
	}

	/**
	 * Returns the value of {@link #detailedWeekViewProperty()}.
	 *
	 * @return the week view
	 */
	public final DetailedWeekView getDetailedWeekView() {
		return detailedWeekView.get();
	}
}
