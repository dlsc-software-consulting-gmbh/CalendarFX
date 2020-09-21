package com.calendarfx.view.segments.traits;

import com.calendarfx.view.EntryViewBase;

/**
 * Trait that allows segment listening to change of properties contained by view entry.
 */
public interface ReactiveEntrySegmentTrait {

    /**
     * Called when segment is accepted to use as part of skin. Allows adding listeners for selected properties
     * of {@link EntryViewBase}, added listeners references should be a type of weak.
     *
     * @param viewEntry view entry
     */
    void observe(EntryViewBase<?> viewEntry);

}
