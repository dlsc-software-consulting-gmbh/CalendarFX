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

package com.calendarfx.google.service;

import com.calendarfx.google.converter.BeanConverter;
import com.calendarfx.google.converter.CalendarListEntryToGoogleCalendarConverter;
import com.calendarfx.google.converter.EventToGoogleEntryConverter;
import com.calendarfx.google.converter.GoogleCalendarToCalendarConverter;
import com.calendarfx.google.converter.GoogleCalendarToCalendarListEntryConverter;
import com.calendarfx.google.converter.GoogleEntryToEventConverter;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntry;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that provides convertion from one object to another based on the class.
 *
 * Created by gdiaz on 22/02/2017.
 */
public class BeanConverterService {

    private static BeanConverterService instance;

    public static BeanConverterService getInstance() {
        if (instance == null) {
            instance = new BeanConverterService();
        }
        return instance;
    }

    private final Map<Pair<Class<?>, Class<?>>, BeanConverter<?, ?>> converters = new HashMap<>();

    private BeanConverterService() {
        converters.put(Pair.of(GoogleEntry.class, Event.class), new GoogleEntryToEventConverter());
        converters.put(Pair.of(Event.class, GoogleEntry.class), new EventToGoogleEntryConverter());
        converters.put(Pair.of(GoogleCalendar.class, CalendarListEntry.class), new GoogleCalendarToCalendarListEntryConverter());
        converters.put(Pair.of(CalendarListEntry.class, GoogleCalendar.class), new CalendarListEntryToGoogleCalendarConverter());
        converters.put(Pair.of(GoogleCalendar.class, Calendar.class), new GoogleCalendarToCalendarConverter());
    }

    <S, T> T convert(S source, Class<T> targetClass) {
        if (canConvert(source.getClass(), targetClass)) {
            BeanConverter<S, T> converter = getConverter((Class<S>) source.getClass(), targetClass);
            return converter.convert(source);
        }
        throw new UnsupportedOperationException("The object " + source + " cannot be converted to " + targetClass);
    }

    private <S, T> boolean canConvert(Class<S> sourceClass, Class<T> targetClass) {
        return getConverter(sourceClass, targetClass) != null;
    }

    private <S, T> BeanConverter<S, T> getConverter(Class<S> sourceClass, Class<T> targetClass) {
        return (BeanConverter<S, T>) converters.get(Pair.of(sourceClass, targetClass));
    }

    private static class Pair<S, T> {

        private S source;
        private T target;

        Pair(S source, T target) {
            this.source = source;
            this.target = target;
        }

        public S getSource() {
            return source;
        }

        public void setSource(S source) {
            this.source = source;
        }

        public T getTarget() {
            return target;
        }

        public void setTarget(T target) {
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (source != null ? !source.equals(pair.source) : pair.source != null) return false;
            return target != null ? target.equals(pair.target) : pair.target == null;
        }

        @Override
        public int hashCode() {
            int result = source != null ? source.hashCode() : 0;
            result = 31 * result + (target != null ? target.hashCode() : 0);
            return result;
        }

        public static <S, T> Pair<S, T> of(S source, T target) {
            return new Pair<>(source, target);
        }

    }

}
