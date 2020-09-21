package com.calendarfx.view.segments.traits;

import com.calendarfx.view.EntryViewBase;
import impl.com.calendarfx.view.segments.SegmentedEntryViewSkin;

/**
 * Trait that allows segment updating its style properties when requested by {@link SegmentedEntryViewSkin}.
 */
public interface StyleAwareEntrySegmentTrait {

    /**
     * Called when views style change is detected. Should handle updating segments style.
     *
     * @param entryView entry view
     */
    void updateStyle(EntryViewBase<?> entryView);

}
