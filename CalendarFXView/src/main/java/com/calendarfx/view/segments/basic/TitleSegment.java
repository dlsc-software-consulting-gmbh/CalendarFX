package com.calendarfx.view.segments.basic;

import com.calendarfx.model.Calendar;
import com.calendarfx.util.ViewHelper;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.segments.base.LabelSegmentBase;
import com.calendarfx.view.segments.traits.StyleAwareEntrySegmentTrait;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.control.Label;

/**
 * Basic implementation of {@link LabelSegmentBase} for displaying title of an entry. Automatically refreshes its
 * value when underlying property of an entry is changed.
 */
public class TitleSegment extends LabelSegmentBase<String> implements StyleAwareEntrySegmentTrait {

    public TitleSegment(int visibilityWeight, VPos alignment) {
        super(visibilityWeight, alignment, DEFAULT_ORDER);
    }

    public TitleSegment(int visibilityWeight, VPos alignment, int order) {
        super(visibilityWeight, alignment, order);
    }

    @Override
    protected ObservableValue<String> getValueProperty(EntryViewBase<?> viewEntry) {
        return ViewHelper.getEntry(viewEntry).titleProperty();
    }

    @Override
    protected void configureLabel(Label label) {
        label.setManaged(false);
        label.setMouseTransparent(true);
        label.setWrapText(true);
        label.setMinSize(0, 0);
    }

    @Override
    public void updateStyle(EntryViewBase<?> entryView) {
        final Calendar calendar = ViewHelper.getCalendar(entryView);
        getNode().getStyleClass().setAll("title-label", "default-style-entry-title-label", calendar.getStyle() + "-entry-title-label");
    }
}
