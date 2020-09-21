package com.calendarfx.view.segments.basic;

import com.calendarfx.model.Calendar;
import com.calendarfx.util.ViewHelper;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.segments.base.LabelSegmentBase;
import com.calendarfx.view.segments.traits.StyleAwareEntrySegmentTrait;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.control.Label;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Basic implementation of {@link LabelSegmentBase} for displaying start time of an entry. Automatically refreshes
 * its value when underlying property of an entry is changed.
 */
public class StartTimeSegment extends LabelSegmentBase<LocalTime> implements StyleAwareEntrySegmentTrait {

    private DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    public StartTimeSegment(int visibilityWeight, VPos alignment) {
        super(visibilityWeight, alignment, DEFAULT_ORDER);
    }

    public StartTimeSegment(int visibilityWeight, VPos alignment, int order) {
        super(visibilityWeight, alignment, order);
    }

    @Override
    protected ObservableValue<LocalTime> getValueProperty(EntryViewBase<?> viewEntry) {
        return ViewHelper.getEntry(viewEntry).startTimeProperty();
    }

    @Override
    protected void configureLabel(Label label) {
        label.setManaged(false);
        label.setMouseTransparent(true);
        label.setMinSize(0, 0);
    }

    @Override
    protected String formatValue(LocalTime value) {
        return formatter.format(value);
    }

    @Override
    public void updateStyle(EntryViewBase<?> entryView) {
        final Calendar calendar = ViewHelper.getCalendar(entryView);
        getNode().getStyleClass().setAll("start-time-label", "default-style-entry-time-label", calendar.getStyle() + "-entry-time-label");
    }
}
