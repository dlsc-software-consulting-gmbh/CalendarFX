/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.WeekTimeScaleView;

import java.time.LocalDate;

@SuppressWarnings("javadoc")
public class WeekTimeScaleViewSkin extends TimeScaleViewSkin<WeekTimeScaleView> {

	public WeekTimeScaleViewSkin(WeekTimeScaleView scale) {
		super(scale);
	}

	@Override
	protected boolean isShowingTimeMarker() {
		WeekTimeScaleView dayView = getSkinnable();
		DetailedWeekView weekView = dayView.getDetailedWeekView();

		if (weekView != null) {
			LocalDate today = getSkinnable().getToday();

			LocalDate weekStart = weekView.getStartDate();
			LocalDate weekEnd = weekView.getEndDate();

			return !(weekStart.isAfter(today) || weekEnd.isBefore(today));

		}

		return false;
	}
}
