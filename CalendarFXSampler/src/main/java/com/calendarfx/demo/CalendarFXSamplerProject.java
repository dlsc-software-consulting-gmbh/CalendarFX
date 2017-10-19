/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.demo;

import fxsampler.FXSamplerProject;
import fxsampler.model.WelcomePage;

public class CalendarFXSamplerProject implements FXSamplerProject {

	@Override
	public String getProjectName() {
		return "CalendarFX";
	}

	@Override
	public String getSampleBasePackage() {
		return "com.calendarfx.demo";
	}

	@Override
	public WelcomePage getWelcomePage() {
		return new CalendarFXSamplerWelcome();
	}
}
