/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.page;

import com.calendarfx.view.DetailedWeekView;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;

import com.calendarfx.view.page.WeekPage;

@SuppressWarnings("javadoc")
public class WeekPageSkin extends PageBaseSkin<WeekPage> {

	public WeekPageSkin(WeekPage view) {
		super(view);
	}

	@Override
	protected Node createContent() {
		WeekPage weekPage = getSkinnable();
		DetailedWeekView detailedWeekView = weekPage.getDetailedWeekView();

		weekPage.bind(detailedWeekView, true);

		Bindings.bindBidirectional(detailedWeekView.startTimeProperty(), weekPage.startTimeProperty());
		Bindings.bindBidirectional(detailedWeekView.endTimeProperty(), weekPage.endTimeProperty());

		return detailedWeekView;
	}
}
