/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import javafx.scene.control.Control;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface LoadDataSettingsProvider {

	String getLoaderName();

	LocalDate getLoadStartDate();

	LocalDate getLoadEndDate();

	ZoneId getZoneId();

	List<CalendarSource> getCalendarSources();

	Control getControl();

	boolean isCalendarVisible(Calendar calendar);
}
