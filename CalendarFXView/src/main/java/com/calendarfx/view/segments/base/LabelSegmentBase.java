package com.calendarfx.view.segments.base;

import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.segments.basic.EntrySegment;
import com.calendarfx.view.segments.traits.ReactiveEntrySegmentTrait;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.VPos;
import javafx.scene.control.Label;

/**
 * Base class for creating segment that contains single label, reacts on value changes of provided property of view entry.
 *
 * @param <T> type of value contained by listened property
 */
public abstract class LabelSegmentBase<T> extends EntrySegment implements ReactiveEntrySegmentTrait {

    private final ChangeListener<T> updateListener = this::update;
    private final WeakChangeListener<T> weakUpdateListener = new WeakChangeListener<>(updateListener);

    public LabelSegmentBase(int visibilityWeight, VPos alignment, int order) {
        this(visibilityWeight, alignment, order, new Label());
    }

    protected LabelSegmentBase(int visibilityWeight, VPos alignment, int order, Label label) {
        super(visibilityWeight, alignment, order, label);
        configureLabel(label);
    }

    @Override
    public void observe(EntryViewBase<?> viewEntry) {
        final ObservableValue<T> property = getValueProperty(viewEntry);
        if (property != null) {
            property.addListener(weakUpdateListener);
            setValue(property.getValue());
        }
    }

    /**
     * Provides property which is listened for value changes.
     *
     * @param viewEntry view entry
     * @return property which is listened for value changes
     */
    protected abstract ObservableValue<T> getValueProperty(EntryViewBase<?> viewEntry);

    /**
     * Configures contained label.
     *
     * @param label label
     */
    protected void configureLabel(Label label) {}

    /**
     * Formats value before is set as a text in contained label.
     *
     * @param value value
     * @return formatted value as string
     */
    protected String formatValue(T value) {
        return value.toString();
    }

    protected void setValue(T value) {
        ((Label)getNode()).setText(formatValue(value));
    }

    private void update(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        setValue(newValue);
    }
}
