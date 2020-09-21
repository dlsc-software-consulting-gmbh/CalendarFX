package com.calendarfx.view.segments;

import com.calendarfx.model.Entry;
import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.segments.base.EntrySegmentBase;
import com.calendarfx.view.segments.basic.AlignedEntrySegment;
import com.calendarfx.view.segments.basic.AlignedEntrySegment.AlignedNode;
import com.calendarfx.view.segments.traits.SegmentedEntryViewTrait;
import impl.com.calendarfx.view.segments.SegmentedDayEntryViewSkin;
import impl.com.calendarfx.view.segments.SegmentedEntryViewSkin;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extended "bridge" implementation of {@link DayEntryView} that allows to use the visibility control functionality of
 * {@link SegmentedEntryViewSkin}.
 */
public class SegmentedDayEntryView extends DayEntryView implements SegmentedEntryViewTrait {

    public static final int DEFAULT_POSITIONAL_SEGMENTS_VISIBILITY_WEIGHT = SegmentedEntryViewSkin.DEFAULT_VISIBILITY_WEIGHT_THRESHOLD - 1;

    private final ReadOnlyListWrapper<EntrySegmentBase> segments = new ReadOnlyListWrapper<>(this, "segments");

    private List<EntrySegmentBase> positionalSegments;

    /**
     * Constructs a new entry view for the given calendar entry.
     *
     * @param entry the entry for which the view will be created
     */
    public SegmentedDayEntryView(Entry<?> entry) {
        super(entry);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        assignNodesToSegments();
        return new SegmentedDayEntryViewSkin(this);
    }

    protected void assignNodesToSegments()
    {
        if (positionalSegments != null) {
            positionalSegments.forEach(this::removeSegment);
        }
        positionalSegments = asPositionalSegments(getNodes());
        positionalSegments.forEach(this::addSegment);
    }

    private List<EntrySegmentBase> asPositionalSegments(Map<Pos, List<Node>> positionalNodes )
    {
        return groupByVPos(positionalNodes).entrySet()
            .stream()
            .map(this::asPositionalSegment)
            .collect(Collectors.toList());
    }

    private EntrySegmentBase asPositionalSegment(Map.Entry<VPos, List<AlignedNode>> group) {
        return new AlignedEntrySegment(getPositionalSegmentsVisibilityWeight(), group.getKey(), EntrySegmentBase.DEFAULT_ORDER, group.getValue());
    }

    public int getPositionalSegmentsVisibilityWeight() {
        return DEFAULT_POSITIONAL_SEGMENTS_VISIBILITY_WEIGHT;
    }

    private Map<VPos, List<AlignedNode>> groupByVPos(Map<Pos, List<Node>> aPositionalNodes) {
        final Map<VPos, List<AlignedNode>> groupedNodes = new HashMap<>();
        for (Map.Entry<Pos, List<Node>> entry : aPositionalNodes.entrySet()) {
            final Pos pos = entry.getKey();
            for (Node node : entry.getValue()) {
                groupedNodes.computeIfAbsent(pos.getVpos(), key -> new ArrayList<>()).add(new AlignedNode(node, pos.getHpos()));
            }
        }
        return groupedNodes;
    }

    @Override
    public ReadOnlyListWrapper<EntrySegmentBase> segmentsProperty() {
        return segments;
    }

}
