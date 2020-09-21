package com.calendarfx.view.segments.traits;

import com.calendarfx.view.EntryViewBase;
import impl.com.calendarfx.view.segments.SegmentedEntryViewSkin;
import com.calendarfx.view.segments.base.EntrySegmentBase;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

/**
 * Trait that allows usage of {@link SegmentedEntryViewSkin} by any implementation of the {@link EntryViewBase}.
 */
public interface SegmentedEntryViewTrait {

    /**
     * Provides property which keeps collection of segments.
     *
     * @return segments storing property
     */
    ReadOnlyListWrapper<EntrySegmentBase> segmentsProperty();

    default ObservableList<EntrySegmentBase> getSegments() {
        return segmentsProperty().getReadOnlyProperty();
    }

    default void clearSegments() {
        final ReadOnlyListWrapper<EntrySegmentBase> segments = segmentsProperty();
        if (segments.get() != null) {
            segments.get().clear();
        }
    }

    default void addSegment(EntrySegmentBase segment) {
        final ReadOnlyListWrapper<EntrySegmentBase> segments = segmentsProperty();
        if (segments.get() == null) {
            segments.set(FXCollections.observableList(new ArrayList<>()));
        }
        segments.get().add(segment);
    }

    default void removeSegment(EntrySegmentBase segment) {
        final ReadOnlyListWrapper<EntrySegmentBase> segments = segmentsProperty();
        if (segments.get() != null) {
            segments.get().remove(segment);
        }
    }

}
