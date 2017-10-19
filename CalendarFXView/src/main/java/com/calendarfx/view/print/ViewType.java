/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view.print;

import javafx.print.PageOrientation;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

/**
 * An enumerator listing the different views that are supported
 * by the print preview functionality.
 */
public enum ViewType {

	DAY_VIEW {
		@Override
		public String getMessageKey() {
			return "PrintViewType.DAY_VIEW";
		}
		
		@Override
		public String getPluralChronoMessageKey() {
			return "PrintViewType.DAY_PLURAL_CHRONO";
		}
		
		@Override
		public PageOrientation getPageOrientation() {
			return PageOrientation.PORTRAIT;
		}
		
		@Override
		public DateTimeFormatter getDateTimeFormatter() {
			return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
		}
		
		@Override
		public ChronoUnit getChronoUnit() {
			return ChronoUnit.DAYS;
		}
		
	},

	WEEK_VIEW {
		@Override
		public String getMessageKey() {
			return "PrintViewType.WEEK_VIEW";
		}
		
		@Override
		public String getPluralChronoMessageKey() {
			return "PrintViewType.WEEK_PLURAL_CHRONO";
		}
		
		@Override
		public PageOrientation getPageOrientation() {
			return PageOrientation.LANDSCAPE;
		}
		
		@Override
		public DateTimeFormatter getDateTimeFormatter() {
			return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
		}
		
		@Override
		public ChronoUnit getChronoUnit() {
			return ChronoUnit.WEEKS;
		}
	},

	MONTH_VIEW {
		@Override
		public String getMessageKey() {
			return "PrintViewType.MONTH_VIEW";
		}
		
		@Override
		public String getPluralChronoMessageKey() {
			return "PrintViewType.MONTH_PLURAL_CHRONO";
		}
		
		@Override
		public PageOrientation getPageOrientation() {
			return PageOrientation.LANDSCAPE;
		}
		
		@Override
		public DateTimeFormatter getDateTimeFormatter() {
			return DateTimeFormatter.ofPattern("MMMM yyyy");
		}
		
		@Override
		public ChronoUnit getChronoUnit() {
			return ChronoUnit.MONTHS;
		}
	};
	
	public abstract String getMessageKey ();
	
	public abstract String getPluralChronoMessageKey ();
	
	public abstract PageOrientation getPageOrientation ();
	
	public abstract DateTimeFormatter getDateTimeFormatter();
	
	public abstract ChronoUnit getChronoUnit ();
	
}
