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

package com.calendarfx.google.view.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Class representing a period for loading events from google.
 *
 * @author Gabriel Diaz, 20.03.2015.
 */
public final class Slice implements Comparable {

    private final LocalDate start;
    private final LocalDate end;

    private Slice(LocalDate start, LocalDate end) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Slice other = (Slice) obj;
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        } else if (!start.equals(other.start)) {
            return false;
        }

        if (end == null) {
            if (other.end != null) {
                return false;
            }
        } else if (!end.equals(other.end)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Slice [" + getStart() + " | " + getEnd() + "]";
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Slice) {
            Slice other = (Slice) o;
            if (start == null) {
                return other.start == null ? 0 : -1;
            }
            return start.compareTo(other.start);
        }
        return 1;
    }

    /**
     * Splits the given period into multiple slices of one month long.
     *
     * @param start the start of the period.
     * @param end   the end of the period.
     * @return The list of slices result of the splitting.
     */
    public static List<Slice> split(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Preconditions.checkArgument(!start.isAfter(end));

        List<Slice> slices = Lists.newArrayList();

        LocalDate startOfMonth = start.withDayOfMonth(1);
        LocalDate endOfMonth = YearMonth.from(end).atEndOfMonth();

        do {
            slices.add(new Slice(startOfMonth, YearMonth.from(startOfMonth).atEndOfMonth()));
            startOfMonth = startOfMonth.plus(1, ChronoUnit.MONTHS);
        }
        while (startOfMonth.isBefore(endOfMonth) || startOfMonth.isEqual(endOfMonth));

        return slices;
    }

}
