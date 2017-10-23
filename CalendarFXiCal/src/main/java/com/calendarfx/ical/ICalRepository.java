/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.calendarfx.ical;

import com.calendarfx.ical.model.ICalCalendar;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gdiaz on 7/01/2017.
 */
public final class ICalRepository {

    private static final String SETTINGS_DIR = "/.store/calendarfx/";

    private static final String SETTINGS_FILE = "iCalCalendars";

    private static final Map<WebCalendarData, ICalCalendar> webCalendars = new LinkedHashMap<>();

    public static CalendarSource familyCalendars = new CalendarSource("Family");

    public static CalendarSource communityCalendars = new CalendarSource("Others");

    public static DoubleProperty workDoneProperty = new SimpleDoubleProperty();

    public static DoubleProperty totalWorkProperty = new SimpleDoubleProperty();

    public static StringProperty messageProperty = new SimpleStringProperty();

    public static void loadSources() throws IOException, ParserException {

        loadWebSource();

        if (familyCalendars.getCalendars().isEmpty()) {
            totalWorkProperty.set(10);
//            createWebCalendar("https://www.nasa.gov/templateimages/redesign/calendar/iCal/nasa_calendar.ics", "NASA", Calendar.Style.STYLE1, communityCalendars);
            workDoneProperty.set(1);
            createWebCalendar("https://cantonbecker.com/astronomy-calendar/astrocal.ics", "Moon / Astro", Calendar.Style.STYLE2, communityCalendars);
            workDoneProperty.set(2);
            createWebCalendar("http://ical.mac.com/ical/US32Holidays.ics", "US Holidays", Calendar.Style.STYLE3, communityCalendars);
            workDoneProperty.set(3);
            createWebCalendar("https://www.google.com/calendar/ical/6g08e17mnjao5k7ddftfvq5gs8%40group.calendar.google.com/public/basic.ics", "FC Liverpool", Calendar.Style.STYLE5, communityCalendars);
            workDoneProperty.set(4);
            createWebCalendar("https://www.google.com/calendar/ical/ohg8jr90apq8k0vili2fbs17to%40group.calendar.google.com/public/basic.ics", "Real Madrid", Calendar.Style.STYLE4, communityCalendars);
            workDoneProperty.set(5);
            createWebCalendar("https://calendar.google.com/calendar/ical/flexcalendarfxdemo%40gmail.com/private-43f02ea9664382e80e6fbe0a541511ee/basic.ics", "Standard", Calendar.Style.STYLE1, familyCalendars);
            workDoneProperty.set(6);
            createWebCalendar("https://calendar.google.com/calendar/ical/75bjnbr2qr5qgetav71tug2sec%40group.calendar.google.com/private-c6c6a59d97aa2806fe28cfbdb2e2957b/basic.ics", "Home", Calendar.Style.STYLE2, familyCalendars);
            workDoneProperty.set(7);
            createWebCalendar("https://calendar.google.com/calendar/ical/5rj1uvaobtosjqoqqkpdlj01gg%40group.calendar.google.com/private-4dc56992aed93526cbab07da6cd4b69b/basic.ics", "School", Calendar.Style.STYLE3, familyCalendars);
            workDoneProperty.set(8);
            createWebCalendar("https://calendar.google.com/calendar/ical/0itqq6d7pukf1tapbll3lbad5c%40group.calendar.google.com/private-7fb78a4b949cede8228d791faba9061e/basic.ics", "Sports", Calendar.Style.STYLE4, familyCalendars);
            workDoneProperty.set(9);
            createWebCalendar("https://calendar.google.com/calendar/ical/u6em5saa8omkamh68bl7fikclo%40group.calendar.google.com/private-bce22b1e9b43b632c7edfad45d677b59/basic.ics", "Work", Calendar.Style.STYLE5, familyCalendars);
            workDoneProperty.set(10);
        }
    }

    public static void loadWebSource() {
        try (FileInputStream fin = new FileInputStream(new File(System.getProperty("user.home") + SETTINGS_DIR, SETTINGS_FILE));
             ObjectInputStream ois = new ObjectInputStream(fin)) {

            List<WebCalendarData> webCalendars = (List<WebCalendarData>) ois.readObject();

            if (webCalendars != null) {
                totalWorkProperty.set(webCalendars.size());
                double progress = 0;
                for (WebCalendarData data : webCalendars) {
                    putWebCalendar(data, data.isFamily() ? familyCalendars : communityCalendars);
                    progress++;
                    workDoneProperty.set(progress);
                }
            }
        } catch (FileNotFoundException ex) {
            // we can ignore this, this will happen first time we start the app
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CalendarSource getCommunityCalendarSource() {
        return communityCalendars;
    }

    public static boolean existsWebCalendar(String url) {
        return getWebCalendar(url) != null;
    }

    public static ICalCalendar getWebCalendar(String url) {
        for (WebCalendarData webCalendar : webCalendars.keySet()) {
            if (webCalendar.getUrl().equals(url)) {
                return webCalendars.get(webCalendar);
            }
        }
        return null;
    }

    public static ICalCalendar createWebCalendar(String url, String name, Calendar.Style style, CalendarSource source) throws IOException, ParserException {
        if (url == null || url.isEmpty() || name == null || name.isEmpty() || style == null) {
            return null;
        }

        WebCalendarData data = new WebCalendarData(url, name, style, source == familyCalendars);

        ICalCalendar cal = putWebCalendar(data, source);

        List<WebCalendarData> webCalendarDatas = new ArrayList<>();
        webCalendarDatas.addAll(webCalendars.keySet());
        if (!webCalendarDatas.isEmpty()) {
            final File directory = new File(System.getProperty("user.home") + SETTINGS_DIR);
            boolean directoryExists = true;
            if (!directory.exists()) {
                directoryExists = directory.mkdirs();
            }

            if (directoryExists) {
                final File file = new File(directory, SETTINGS_FILE);

                boolean fileExists = true;
                if (!file.exists()) {
                    fileExists = file.createNewFile();
                }

                if (fileExists) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                        oos.writeObject(webCalendarDatas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return cal;
    }

    private static ICalCalendar putWebCalendar(WebCalendarData data, CalendarSource source) throws IOException, ParserException {
        ICalCalendar cal = getWebCalendar(data.getUrl());
        if (cal == null) {

            messageProperty.set("Calendar: " + data.getName());

            URL urlObj = new URL(data.getUrl().replace("webcal", "https"));

            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);

            CalendarBuilder builder = new CalendarBuilder();
            InputStream inputStream = conn.getInputStream();

            net.fortuna.ical4j.model.Calendar calendar = builder.build(inputStream);

            cal = new ICalCalendar(data.getName(), calendar);
            cal.setStyle(data.getStyle());

            webCalendars.put(data, cal);

            final ICalCalendar fcal = cal;
            Platform.runLater(() -> {
                        source.getCalendars().add(fcal);
                    }
            );
        }

        return cal;
    }

    private static class WebCalendarData implements Serializable {

        private final String url;
        private final String name;
        private final Calendar.Style style;
        private final boolean family;

        public WebCalendarData(String url, String name, Calendar.Style style, boolean family) {
            this.url = url;
            this.name = name;
            this.style = style;
            this.family = family;
        }

        public boolean isFamily() {
            return family;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public Calendar.Style getStyle() {
            return style;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WebCalendarData that = (WebCalendarData) o;

            return url != null ? url.equals(that.url) : that.url == null;
        }

        @Override
        public int hashCode() {
            return url != null ? url.hashCode() : 0;
        }
    }

}
