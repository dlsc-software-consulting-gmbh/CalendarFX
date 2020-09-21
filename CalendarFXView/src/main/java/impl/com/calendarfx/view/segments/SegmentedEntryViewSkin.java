package impl.com.calendarfx.view.segments;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.util.ViewHelper;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.EntryViewBase;
import com.calendarfx.view.segments.base.EntrySegmentBase;
import com.calendarfx.view.segments.basic.StartTimeSegment;
import com.calendarfx.view.segments.basic.TitleSegment;
import com.calendarfx.view.segments.traits.ReactiveEntrySegmentTrait;
import com.calendarfx.view.segments.traits.SegmentedEntryViewTrait;
import com.calendarfx.view.segments.traits.StyleAwareEntrySegmentTrait;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of {@link SkinBase} that uses {@link EntrySegmentBase} as basic layout element. Segments
 * gives more control over what should be visible where there is not enough space to show all contained visual
 * elements ({@link Node}). This implementation aligns segments from top to bottom with taking into the
 * account theirs importance (visibility weight), vertical alignment and order. By default most influential
 * property of segment is visibility weight, but in extending classes this could be adjusted
 * by overriding {@link #getVisibilityPriorityComparator()}.
 */
public class SegmentedEntryViewSkin<T extends DateControl> extends SkinBase<EntryViewBase<T>>
{
    /**
     * Helper internal class to store measurement of segments result, used later during segments positioning phase.
     */
    private static class MeasurementResult
    {
        double topRangeStart;
        double centerRangeStart;
        double bottomRangeStart;

        public MeasurementResult(double topRangeStart, double centerRangeStart, double bottomRangeStart) {
            this.topRangeStart = topRangeStart;
            this.centerRangeStart = centerRangeStart;
            this.bottomRangeStart = bottomRangeStart;
        }

        public double getTopRangeStart() {
            return topRangeStart;
        }

        public double getCenterRangeStart() {
            return centerRangeStart;
        }

        public double getBottomRangeStart() {
            return bottomRangeStart;
        }
    }

    /**
     * Segments with value of visibility weight equal or above the threshold will be counted towards minimum
     * height computation.
     */
    public static final int DEFAULT_VISIBILITY_WEIGHT_THRESHOLD = 10;

    private final InvalidationListener updateStylesListener = it -> updateStyles();
    private final WeakInvalidationListener weakUpdateStylesListener = new WeakInvalidationListener(updateStylesListener);

    private List<EntrySegmentBase> segments;
    private MeasurementResult lastMeasurement;

    public SegmentedEntryViewSkin(EntryViewBase<T> view)
    {
        super(view);

        applyClipping(view);

        enableSegmentAutoUpdates(view);
        updateSegments();

        enableStyleAutoUpdates(view);
        updateStyles();
    }

    /**
     * Connects listener to update skin segments every time underlying views segments collection is modified.
     * Listener is added only for views with certain trait: {@link SegmentedEntryViewTrait} which defines the
     * interface how the segments should be provided to skin implementation and managed.
     *
     * @param view entry view
     */
    protected final void enableSegmentAutoUpdates(EntryViewBase<T> view) {
        if (view instanceof SegmentedEntryViewTrait) {
            ((SegmentedEntryViewTrait) view).segmentsProperty().addListener((Observable it) -> updateSegments());
        }
    }

    /**
     * Collects provided segments from view and creates default segments defined by the skin. Updates children
     * of this skin with provided by segments nodes. If segment has {@link ReactiveEntrySegmentTrait} it calls
     * its observe method, so it can add its listeners.
     */
    protected void updateSegments() {
        final EntryViewBase<T> view = getSkinnable();

        segments = new ArrayList<>();
        segments.addAll(createDefaultSegments(view));
        segments.addAll(getAdditionalViewSegments(view));

        getChildren().clear();
        segments.stream()
                .map(EntrySegmentBase::getNode)
                .forEach(getChildren()::add);

        segments.stream()
                .filter(ReactiveEntrySegmentTrait.class::isInstance)
                .map(ReactiveEntrySegmentTrait.class::cast)
                .forEach(segment -> segment.observe(getSkinnable()));
    }

    private void applyClipping(EntryViewBase<T> view) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);
    }

    /**
     * Creates default collection of segments.
     *
     * @param entryView entry view for which segments should be created
     * @return list of default segments
     */
    protected List<EntrySegmentBase> createDefaultSegments(EntryViewBase<T> entryView) {
        ArrayList<EntrySegmentBase> segments = new ArrayList<>();
        segments.add(new TitleSegment(DEFAULT_VISIBILITY_WEIGHT_THRESHOLD, VPos.TOP));
        segments.add(new StartTimeSegment(DEFAULT_VISIBILITY_WEIGHT_THRESHOLD - 1, VPos.TOP));
        return segments;
    }

    /**
     * Collects additional segments provided by the entry view.
     *
     * @param view entry view
     * @return list of addition segments provided by the entry view
     */
    protected List<EntrySegmentBase> getAdditionalViewSegments(EntryViewBase<T> view) {
        if (view instanceof SegmentedEntryViewTrait) {
            return ((SegmentedEntryViewTrait) view).getSegments();
        }
        return Collections.emptyList();
    }

    protected final void enableStyleAutoUpdates(EntryViewBase<T> aView) {
        ViewHelper.getEntry(aView).calendarProperty().addListener(weakUpdateStylesListener);
    }

    /**
     * This methods updates the styles of the node according to the entry
     * settings.
     */
    protected void updateStyles()
    {
        final EntryViewBase<T> view = getSkinnable();
        final Entry<?> entry = ViewHelper.getEntry(view);
        final Calendar calendar = ViewHelper.getCalendar(view);

        if (calendar == null) {
            return;
        }

        view.getStyleClass().setAll("default-style-entry", calendar.getStyle() + "-entry");

        if (entry.isRecurrence()) {
            view.getStyleClass().add("recurrence");
        }

        view.getStyleClass().addAll(entry.getStyleClass());

        updateSegmentsStyle();
    }

    private void updateSegmentsStyle() {
        segments.stream()
                .filter(StyleAwareEntrySegmentTrait.class::isInstance)
                .map(StyleAwareEntrySegmentTrait.class::cast)
                .forEach(segment -> segment.updateStyle(getSkinnable()));
    }

    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        final List<EntrySegmentBase> sortedByOrder = new ArrayList<>(segments);
        sortedByOrder.sort(Comparator.comparingInt(EntrySegmentBase::getOrder));

        // measurement phase
        measureSegments(sortedByOrder, x, y, width, height);

        // positioning phase
        positionSegments(sortedByOrder, x, y, width, height);
    }

    /**
     * Measures space required by contained segments, if not enough space is available segments with lower
     * importance (lower visibility weight and higher order value) are set to be invisible.
     *
     * After first not fitting segment is found the rest is ignored (theirs visibility set to hidden).
     *
     * @param orderedSegments vertically sorted (top to bottom) segments
     * @param x contents x
     * @param y contents y
     * @param width contents width
     * @param height contents height
     */
    protected void measureSegments(List<EntrySegmentBase> orderedSegments, double x, double y, double width, double height) {
        double usedSpaceTotal = 0.0;
        double usedSpaceTop = 0.0;
        double usedSpaceBottom = 0.0;

        List<EntrySegmentBase> sortedByWeight = new ArrayList<>(orderedSegments);
        sortedByWeight.sort(getVisibilityPriorityComparator());

        boolean isAllSpaceReserved = false;

        for (EntrySegmentBase segment : sortedByWeight) {
            final Node node = segment.getNode();

            if (isAllSpaceReserved) {
                node.setVisible(false);
                continue;
            }

            segment.prepareLayout(getSkinnable(), width, height);

            final double preferredHeight = node.prefHeight(width);
            if (usedSpaceTotal + preferredHeight <= height) {
                usedSpaceTotal += preferredHeight;

                switch (segment.getAlignment()) {
                    case TOP:
                        usedSpaceTop += preferredHeight;
                        break;
                    case BOTTOM:
                    case BASELINE:
                        usedSpaceBottom += preferredHeight;
                        break;
                    default:
                }

                markSegmentAsUsable(segment);
            } else {
                markSegmentAsIgnored(segment);
                isAllSpaceReserved = true;
            }
        }

        double optimisticCenterRangeStart = y + (height * 0.5) - (usedSpaceTotal - (usedSpaceTop + usedSpaceBottom)) * 0.5;

        final MeasurementResult result = new MeasurementResult(
                0.0,
                Math.max(optimisticCenterRangeStart, usedSpaceTop),
                height - usedSpaceBottom);
        setLastMeasurement(result);
    }

    /**
     * Provides comparator used during measurement phase for deciding priority of segments which should stay
     * visible. By default priority should be defined by visibility weight, but this could be adjusted in
     * extending classes.
     *
     * @return comparator
     */
    protected Comparator<EntrySegmentBase> getVisibilityPriorityComparator() {
        return Comparator.comparingInt(EntrySegmentBase::getVisibilityWeight).reversed();
    }

    /**
     * Positions segments using lastly measured space usage {@link MeasurementResult}. Segments set to be
     * invisible during measurement phase will be not positioned.
     *
     * @param orderedSegments vertically sorted (top to bottom) segments
     * @param x contents x
     * @param y contents y
     * @param width contents width
     * @param height contents height
     */
    protected void positionSegments(List<EntrySegmentBase> orderedSegments, double x, double y, double width, double height) {
        MeasurementResult measurement = getLastMeasurement();

        double topPointer = measurement.getTopRangeStart();
        double centerPointer = measurement.getCenterRangeStart();
        double bottomPointer = measurement.getBottomRangeStart();

        for (EntrySegmentBase segment : orderedSegments)
        {
            final Node node = segment.getNode();
            if (isSegmentMarkedAsUsable(segment))
            {
                final double nodeHeight = node.prefHeight(width);

                final double computedY;
                switch (segment.getAlignment()) {
                    case TOP:
                        computedY = y + topPointer;
                        topPointer += nodeHeight;
                        break;
                    case CENTER:
                        computedY = y + centerPointer;
                        centerPointer += nodeHeight;
                        break;
                    default:
                    case BOTTOM:
                    case BASELINE:
                        computedY = y + bottomPointer;
                        bottomPointer += nodeHeight;
                        break;
                }

                node.resizeRelocate(snapPositionX(x), snapPositionY(computedY), snapSizeX(width), snapSizeY(nodeHeight));
            }
        }
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return segments.stream()
                .filter(segment -> segment.getVisibilityWeight() >= DEFAULT_VISIBILITY_WEIGHT_THRESHOLD)
                .map(EntrySegmentBase::getNode)
                .map(node -> node.prefHeight(-1))
                .reduce(0.0, Double::sum) + topInset + bottomInset;
    }

    /**
     * Called during measurement phase. Marks segment as usable during positioning stage by setting visibility of segments node to true.
     *
     * @param segment segment
     */
    protected void markSegmentAsUsable(EntrySegmentBase segment) {
        segment.getNode().setVisible(true);
    }

    /**
     * Called during measurement phase. Marks segment as ignored by setting visibility of segments node to false.
     *
     * @param segment segment
     */
    protected void markSegmentAsIgnored(EntrySegmentBase segment) {
        segment.getNode().setVisible(false);
    }

    /**
     * Called during positioning phase. Returns true if segment was marked as usable during measurement phase.
     *
     * @param segment segment
     * @return true if segment should be used during positioning phase
     */
    protected boolean isSegmentMarkedAsUsable(EntrySegmentBase segment) {
        return segment.getNode().isVisible();
    }

    /**
     * Gives access to read-only list of segments for extending classes.
     *
     * @return read-only list of segments
     */
    protected final List<EntrySegmentBase> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    private MeasurementResult getLastMeasurement() {
        return lastMeasurement;
    }

    private void setLastMeasurement(MeasurementResult measurement) {
        lastMeasurement = measurement;
    }
}
