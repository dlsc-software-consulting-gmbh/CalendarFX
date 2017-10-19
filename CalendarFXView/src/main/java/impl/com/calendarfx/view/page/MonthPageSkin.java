/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.page;

import com.calendarfx.view.MonthView;
import com.calendarfx.view.page.MonthPage;

import javafx.scene.Node;

public class MonthPageSkin extends PageBaseSkin<MonthPage>  {

	public MonthPageSkin(MonthPage view) {
		super(view);
	}

	@Override
	protected Node createContent() {
		MonthPage monthPage = getSkinnable();
		MonthView monthView = monthPage.getMonthView();
		monthView.setMinSize(0, 0);
		monthPage.bind(monthView, true);
		return monthView;
	}
}
