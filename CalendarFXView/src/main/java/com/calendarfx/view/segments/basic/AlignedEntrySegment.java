package com.calendarfx.view.segments.basic;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Segment which allows to position nodes as three groups aligned to: left, center or right. Requires to wrap
 * nodes in {@link AlignedNode} instance, which keeps additional alignment information. Segment does not try
 * to prevent overlapping of contained groups, inside of group nodes are placed next to each other
 * horizontally in order how they were provided.
 */
public class AlignedEntrySegment extends EntrySegment {

    /**
     * Helper class for storing node and keeping information about its alignment inside of the segment.
     */
    public static class AlignedNode {
        final Node node;
        final HPos alignment;

        public AlignedNode(Node aNode, HPos aAlignment) {
            node = aNode;
            alignment = aAlignment;
        }

        public Node getNode() {
            return node;
        }

        public HPos getAlignment() {
            return alignment;
        }
    }

    final Map<HPos, Pane> panes = new HashMap<>();

    public AlignedEntrySegment(int visibilityWeight, VPos alignment, List<AlignedNode> nodes) {
        this(visibilityWeight, alignment, DEFAULT_ORDER, new Pane(), nodes);
    }

    public AlignedEntrySegment(int visibilityWeight, VPos alignment, int order, List<AlignedNode> nodes) {
        this(visibilityWeight, alignment, order, new Pane(), nodes);
    }

    private AlignedEntrySegment(int aVisibilityWeight, VPos alignment, int order, Pane root, List<AlignedNode> nodes) {
        super(aVisibilityWeight, alignment, order, root);

        root.setMouseTransparent(true);

        nodes.forEach(entry -> panes.computeIfAbsent(entry.getAlignment(), this::providePane)
                .getChildren()
                .add(entry.getNode()));

        root.getChildren().addAll(panes.values());
    }

    /**
     * Provides group pane. Could be override to change how nodes are grouped.
     *
     * @param pos horizontal position of the pane
     * @return pane
     */
    protected Pane providePane(HPos pos) {
        return new HBox();
    }

    @Override
    public void prepareLayout(Region region, double contentWidth, double contentHeight) {
        super.prepareLayout(region, contentWidth, contentHeight);

        final Pane l = panes.getOrDefault(HPos.LEFT, null);
        if (l != null) {
            l.relocate(0, 0);
        }

        final Pane c = panes.getOrDefault(HPos.CENTER, null);
        if (c != null) {
            final double w = c.prefWidth(contentHeight);
            c.relocate(region.snapPositionX(contentWidth * 0.5 - w * 0.5),  0);
        }

        final Pane r = panes.getOrDefault(HPos.RIGHT, null);
        if (r != null) {
            final double w = r.prefWidth(contentHeight);
            r.relocate(region.snapPositionX(contentWidth - w), 0);
        }
    }
}
