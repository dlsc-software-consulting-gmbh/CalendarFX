package impl.com.calendarfx.view.segments;

import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayView;
import com.calendarfx.view.segments.SegmentedDayEntryView;
import com.calendarfx.view.segments.base.EntrySegmentBase;
import com.calendarfx.view.segments.basic.TitleSegment;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;

import java.util.Comparator;

/**
 * Extended "bridge" implementation of {@link SegmentedEntryViewSkin} for use with {@link SegmentedDayEntryView}.
 */
public class SegmentedDayEntryViewSkin extends SegmentedEntryViewSkin<DayView> {

    private static final Comparator<EntrySegmentBase> PRIORITIZE_TITLE_SEGMENT_COMPARATOR =
            Comparator.comparingInt(segment -> (segment instanceof TitleSegment ? 0 : 1));

    private final InvalidationListener minHeightEqualToTitleListener =(Observable o) -> updateSegments();
    private final WeakInvalidationListener minHeightEqualToTitleWeakListener = new WeakInvalidationListener(minHeightEqualToTitleListener);

    public SegmentedDayEntryViewSkin(SegmentedDayEntryView view) {
        super(view);
        view.minHeightEqualToTitleHeightProperty().addListener(minHeightEqualToTitleWeakListener);
    }

    /**
     * If we declare that the minimum height is equal to height required by title segments (here we assume we
     * could have more than one), we need to give them maximum priority during measurement phase. Otherwise
     * when entry has minimal height, segments with higher visibility weight could take place of
     * title segments.
     */
    @Override
    protected Comparator<EntrySegmentBase> getVisibilityPriorityComparator() {
        final Comparator<EntrySegmentBase> defaultComparator = super.getVisibilityPriorityComparator();
        if (isMinHeightEqualToTitleHeight()) {
            return (segmentA, segmentB) -> {
                final int result = PRIORITIZE_TITLE_SEGMENT_COMPARATOR.compare(segmentA, segmentB);
                return result != 0 ? result : defaultComparator.compare(segmentA, segmentB);
            };
        }
        return defaultComparator;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (isMinHeightEqualToTitleHeight()) {
            return computeTotalTitleSegmentsHeight() + topInset + bottomInset;
        }
        return super.computeMinHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    private double computeTotalTitleSegmentsHeight() {
        return getSegments().stream()
                .filter(TitleSegment.class::isInstance)
                .map(segment -> segment.getNode().prefHeight(-1))
                .reduce(0.0, Double::sum);
    }

    private boolean isMinHeightEqualToTitleHeight() {
        return ((DayEntryView)getSkinnable()).isMinHeightEqualToTitleHeight();
    }
}
