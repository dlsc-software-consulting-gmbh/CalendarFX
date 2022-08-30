package com.calendarfx.view.popover;

import impl.com.calendarfx.view.ZoneIdStringConverter;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.Objects;

public class ZoneIdComparator implements Comparator<ZoneId> {

    private ZoneIdStringConverter converter = new ZoneIdStringConverter();

    public ZoneIdComparator(ZoneIdStringConverter converter) {
        this.converter = Objects.requireNonNull(converter, "converter can not be null");
    }

    public ZoneIdComparator() {
    }

    @Override
    public int compare(ZoneId zone1, ZoneId zone2) {
        String s1 = converter.toString(zone1);
        String s2 = converter.toString(zone2);
        return s1.compareTo(s2);
    }
}
