package com.calendarfx.view.segments;

import com.calendarfx.model.Entry;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.segments.base.EntrySegmentBase;
import com.calendarfx.view.segments.traits.SegmentedEntryViewTrait;
import impl.com.calendarfx.view.segments.SegmentedEntryViewSkin;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.scene.control.Skin;

/**
 * Implementation of {@link EntryViewBase} that uses {@link SegmentedEntryViewSkin} as default layout and
 * provides data for it. Alternative implementation for {@link DayEntryView}, which gives more control on what
 * is visible when there is not enough space to display all visual elements of an calendar entry.
 */
public class SegmentedEntryView<T extends DateControl> extends EntryViewBase<T> implements SegmentedEntryViewTrait {

    private final ReadOnlyListWrapper<EntrySegmentBase> segments = new ReadOnlyListWrapper<>(this, "segments");

    /**
     * Constructs a new view for the given entry.
     *
     * @param entry the calendar entry
     */
    public SegmentedEntryView(Entry<?> entry) {
        super(entry);
    }

    @Override
    public ReadOnlyListWrapper<EntrySegmentBase> segmentsProperty() {
        return segments;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SegmentedEntryViewSkin<>(this);
    }

}
