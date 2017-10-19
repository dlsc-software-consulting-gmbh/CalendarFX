/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view.page;

import java.time.format.DateTimeFormatter;

import com.calendarfx.view.Messages;
import com.calendarfx.view.MonthView;
import com.calendarfx.view.print.ViewType;

import impl.com.calendarfx.view.page.MonthPageSkin;
import javafx.scene.control.Skin;

/**
 * A composite view focused on displaying calendar information for a single
 * month. The view consists of the page "chrome" inherited from the superclass
 * and a {@link MonthView}.
 * <p/>
 * <center><img width="100%" src="doc-files/month-page.png"></center>
 */
public class MonthPage extends PageBase {

	private MonthView monthView;

	/**
	 * Constructs a new month page.
	 */
	public MonthPage() {
		super();

		getStyleClass().add("month-page"); //$NON-NLS-1$

		this.monthView = new MonthView();

		setDateTimeFormatter(DateTimeFormatter.ofPattern(Messages.getString("MonthPage.DATE_FORMAT"))); //$NON-NLS-1$
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new MonthPageSkin(this);
	}

	/**
	 * Returns the week view child control. Most of the visualization in this
	 * page is done by this view. The page only adds its chrome.
	 *
	 * @return the week view
	 */
	public final MonthView getMonthView() {
		return monthView;
	}

	@Override
	public final void goForward() {
		setDate(getDate().plusMonths(1).withDayOfMonth(1));
	}

	@Override
	public final void goBack() {
		setDate(getDate().minusMonths(1).withDayOfMonth(1));
	}
	
	@Override
	public final ViewType getPrintViewType() {
		return ViewType.MONTH_VIEW;
	}
	
}
