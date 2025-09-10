package com.calendarfx.app;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;

public class CalendarAppAtlantaFX extends CalendarApp {

    public static void main(String[] args) {
        System.setProperty("atlantafx", "true");
        setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        launch(args);
    }
}
