package com.calendarfx.view.segments.basic;

import com.calendarfx.view.segments.base.EntrySegmentBase;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * Most basic but complete implementation of segments base class. Can be used for displaying single nodes
 * or groups of nodes contained in provided pane.
 */
public class EntrySegment extends EntrySegmentBase {

    private final Node node;

    public EntrySegment(int visibilityWeight, VPos alignment, Node node) {
        super(visibilityWeight, alignment, DEFAULT_ORDER);
        this.node = node;
    }

    public EntrySegment(int visibilityWeight, VPos alignment, int order, Node node) {
        super(visibilityWeight, alignment, order);
        this.node = node;
    }

    @Override
    public Node getNode()
    {
        return node;
    }

    @Override
    public void prepareLayout(Region region, double contentWidth, double contentHeight) {
        double nodeHeight = node.prefHeight(contentWidth);
        node.resize(region.snapSizeX(contentWidth), region.snapSizeY(nodeHeight));
    }
}
