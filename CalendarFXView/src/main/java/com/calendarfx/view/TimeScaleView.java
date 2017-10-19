/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import impl.com.calendarfx.view.TimeScaleViewSkin;
import javafx.scene.control.Skin;

/**
 * A control used for displaying a vertical time scale.
 * <p/>
 * <center><img src="doc-files/time-scale-view.png"></center>
 * <p/>
 */
public class TimeScaleView extends DayViewBase {

	/**
	 * Constructs a new scale view.
	 */
	public TimeScaleView() {
		getStyleClass().add("time-scale"); //$NON-NLS-1$
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new TimeScaleViewSkin<>(this);
	}
}
