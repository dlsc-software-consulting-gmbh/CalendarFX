package com.calendarfx.view.segments.base;

import impl.com.calendarfx.view.segments.SegmentedEntryViewSkin;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;

/**
 * Base class defining common interface for creating entry segments used by {@link SegmentedEntryViewSkin}.
 * Beside typical {@link Node}, it contains meta-information about importance of contained node. Can be used
 * by the implementation of {@link Skin} to control visibility of contained nodes.
 */
public abstract class EntrySegmentBase
{
    // default value of order field used when order is not explicitly defined
    public static final int DEFAULT_ORDER = 0;

    private final int visibilityWeight;
    private final VPos alignment;
    private final int order;

    public EntrySegmentBase(int visibilityWeight, VPos alignment, int order) {
        this.visibilityWeight = visibilityWeight;
        this.alignment = alignment;
        this.order = order;
    }

    /**
     * Returns contained node.
     *
     * @return node
     */
    public abstract Node getNode();

    /**
     * Requests a try of adjusting contents layout to fit requested bounds.
     * @param region region
     * @param contentWidth maximum usable width
     * @param contentHeight maximum usable height
     */
    public abstract void prepareLayout(final Region region, final double contentWidth, final double contentHeight);

    /**
     * Returns visibility weight, segments with higher visibility weight will have higher priority to stay visible
     * when there is not enough space to display all segments.
     *
     * @return visibility weight
     */
    public int getVisibilityWeight()
    {
        return visibilityWeight;
    }

    /**
     * Tells in which of three sections of the entry should the segment be positioned: top, center or bottom/baseline.
     *
     * @return vertical alignment of the segment
     */
    public VPos getAlignment()
    {
        return alignment;
    }

    /**
     * Order value is used for sorting segments, segments with lower order value will be measured and
     * positioned before segments with higher value. Order works the same for all possible alignments -
     * segments with lower order are positioned higher than ones with lower value.
     *
     * @return order
     */
    public int getOrder() {
        return order;
    }
}
