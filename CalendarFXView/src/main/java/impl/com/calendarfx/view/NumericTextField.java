/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import javafx.scene.control.TextField;

public final class NumericTextField extends TextField {

	public NumericTextField() {
		focusedProperty().addListener(it -> {
			if (isFocused()) {
				selectAll();
			}
		});
	}

	@Override
	public void replaceText(int start, int end, String s) {
		super.replaceText(start, end, s.replaceAll("[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void replaceSelection(String s) {
		super.replaceSelection(s.replaceAll("[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
