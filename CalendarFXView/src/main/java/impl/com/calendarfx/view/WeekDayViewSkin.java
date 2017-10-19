/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.view.WeekDayView;
import com.calendarfx.view.WeekView;

import java.time.LocalDate;

public class WeekDayViewSkin extends DayViewSkin<WeekDayView> {

	public WeekDayViewSkin(WeekDayView view) {
		super(view);
	}

	@Override
	protected boolean isShowingTimeMarker() {
		WeekDayView dayView = getSkinnable();
		WeekView weekView = dayView.getWeekView();

		if (weekView != null) {
			LocalDate today = getSkinnable().getToday();

			LocalDate weekStart = weekView.getStartDate();
			LocalDate weekEnd = weekView.getEndDate();

			return !(weekStart.isAfter(today) || weekEnd.isBefore(today));

		}

		return false;
	}


	@Override
	protected boolean isShowingTimeTodayMarker() {
		WeekDayView dayView = getSkinnable();
		return dayView.getDate().equals(dayView.getToday());
	}
}
