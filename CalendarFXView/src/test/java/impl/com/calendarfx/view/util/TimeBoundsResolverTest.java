/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package impl.com.calendarfx.view.util;

import com.calendarfx.FxTestSupport;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.DayView;
import com.calendarfx.view.EntryViewBase;
import javafx.scene.control.Skin;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link TimeBoundsResolver}. The resolver has to guarantee that two
 * entry views whose entries overlap in time never occupy the same horizontal
 * space, no matter in which order the views are passed in.
 */
public class TimeBoundsResolverTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 28);

    private static final ZoneId ZONE = ZoneId.of("UTC");

    private final Calendar<String> calendar = new Calendar<>("Test");

    @BeforeClass
    public static void initializeToolkit() {
        FxTestSupport.initializeUserAgentStylesheet();
    }

    @After
    public void tearDown() {
        TimeBoundsResolver.setAdditionalComparator(null);
    }

    // ----------------------------------------------------------------- basics

    @Test
    public void shouldReturnEmptyListForEmptyInput() {

        // given
        List<TestEntryView> views = new ArrayList<>();

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views);

        // then
        assertThat(placements, is(empty()));
    }

    @Test
    public void shouldUseSingleColumnForSingleEntry() {

        // given
        List<TestEntryView> views = views(view("A", 9, 10));

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views);

        // then
        assertThat(placements, hasSize(1));
        assertThat(placements.get(0).getColumnIndex(), is(0));
        assertThat(placements.get(0).getColumnCount(), is(1));
    }

    @Test
    public void shouldUseSingleColumnForNonOverlappingEntries() {

        // given
        List<TestEntryView> views = views(view("A", 9, 10), view("B", 10, 11), view("C", 11, 12));

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views);

        // then
        assertThat(placements, hasSize(3));
        for (Placement placement : placements) {
            assertThat(placement.getColumnIndex(), is(0));
            assertThat(placement.getColumnCount(), is(1));
        }
    }

    @Test
    public void shouldUseTwoColumnsForTwoOverlappingEntries() {

        // given
        List<TestEntryView> views = views(view("A", 9, 11), view("B", 10, 12));

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views);

        // then
        assertThat(placements, hasSize(2));
        assertThat(columnIndexOf(placements, "A"), is(0));
        assertThat(columnIndexOf(placements, "B"), is(1));
        assertThat(columnCountOf(placements, "A"), is(2));
        assertThat(columnCountOf(placements, "B"), is(2));
        assertNoVisualOverlap(placements);
    }

    @Test
    public void shouldUseThreeColumnsForThreeOverlappingEntries() {

        // given
        List<TestEntryView> views = views(view("A", 9, 12), view("B", 10, 13), view("C", 11, 14));

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views);

        // then
        assertThat(placements, hasSize(3));
        assertThat(columnIndexOf(placements, "A"), is(0));
        assertThat(columnIndexOf(placements, "B"), is(1));
        assertThat(columnIndexOf(placements, "C"), is(2));
        for (Placement placement : placements) {
            assertThat(placement.getColumnCount(), is(3));
        }
        assertNoVisualOverlap(placements);
    }

    @Test
    public void shouldReuseColumnWhenTimeSlotIsFree() {

        // given: A and B overlap, C starts after A has ended
        List<TestEntryView> views = views(view("A", 9, 10), view("B", 9, 12), view("C", 10, 11));

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views);

        // then
        assertThat(columnIndexOf(placements, "A"), is(0));
        assertThat(columnIndexOf(placements, "B"), is(1));
        assertThat(columnIndexOf(placements, "C"), is(0));
        assertNoVisualOverlap(placements);
    }

    @Test
    public void shouldCreateSeparateClustersForSeparateTimeBlocks() {

        // given
        List<TestEntryView> views = views(
                view("A", 8, 9), view("B", 8, 9),   // first cluster, two columns
                view("C", 14, 15));                 // second cluster, one column

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views);

        // then
        assertThat(columnCountOf(placements, "A"), is(2));
        assertThat(columnCountOf(placements, "B"), is(2));
        assertThat(columnCountOf(placements, "C"), is(1));
        assertNoVisualOverlap(placements);
    }

    // ---------------------------------------------------------------- sorting

    @Test
    public void shouldSortEntryViewsByStartTime() {

        // given
        TestEntryView a = view("A", 9, 10);
        TestEntryView b = view("B", 10, 11);
        TestEntryView c = view("C", 11, 12);
        List<TestEntryView> views = views(c, a, b);

        // when
        TimeBoundsResolver.resolve(views);

        // then
        assertThat(views, contains(a, b, c));
    }

    @Test
    public void shouldResolveOverlapsIndependentlyOfInputOrder() {

        // given: the "bridge" entry B connects A and C, but is passed in last
        List<TestEntryView> views = views(view("A", 9, 10), view("C", 11, 12), view("B", 9, 12));

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views);

        // then
        assertThat(placements, hasSize(3));
        assertNoVisualOverlap(placements);
    }

    @Test
    public void shouldProduceSameResultForEveryPermutation() {

        // given
        TestEntryView a = view("A", 9, 10);
        TestEntryView b = view("B", 9, 12);
        TestEntryView c = view("C", 11, 12);
        List<Placement> reference = TimeBoundsResolver.resolve(views(a, b, c));
        Map<String, Integer> expectedIndex = columnIndices(reference);
        Map<String, Integer> expectedCount = columnCounts(reference);

        // when / then
        for (List<TestEntryView> permutation : permutations(Arrays.asList(a, b, c))) {
            List<Placement> placements = TimeBoundsResolver.resolve(new ArrayList<>(permutation));
            assertNoVisualOverlap(placements);
            assertThat("column indices differ for " + titles(permutation), columnIndices(placements), is(expectedIndex));
            assertThat("column counts differ for " + titles(permutation), columnCounts(placements), is(expectedCount));
        }
    }

    @Test
    public void shouldResolveComplexScenarioInEveryPermutation() {

        // given
        TestEntryView a = view("A", 8, 9);
        TestEntryView b = view("B", 8, 17);
        TestEntryView c = view("C", 9, 10);
        TestEntryView d = view("D", 9, 11);
        TestEntryView e = view("E", 13, 14);
        TestEntryView f = view("F", 16, 18);

        // when / then
        for (List<TestEntryView> permutation : permutations(Arrays.asList(a, b, c, d, e, f))) {
            List<Placement> placements = TimeBoundsResolver.resolve(new ArrayList<>(permutation));
            assertThat(placements, hasSize(6));
            assertNoVisualOverlap(placements);
        }
    }

    // ------------------------------------------------------------- visibility

    @Test
    public void shouldIgnoreInvisibleEntryViews() {

        // given
        TestEntryView a = view("A", 9, 12);
        TestEntryView b = view("B", 9, 12);
        b.setVisible(false);

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views(a, b));

        // then
        assertThat(placements, hasSize(1));
        assertThat(placements.get(0).getColumnCount(), is(1));
    }

    // --------------------------------------------------------------- full day

    @Test
    public void shouldNotPlaceTimedEntryIntoColumnOfFullDayEntry() {

        // given
        TestEntryView fullDay = fullDayView("A");
        TestEntryView timed = view("B", 9, 10);

        // when
        List<Placement> placements = TimeBoundsResolver.resolve(views(fullDay, timed));

        // then
        assertThat(placements, hasSize(2));
        assertThat(columnCountOf(placements, "A"), is(2));
        assertThat(columnCountOf(placements, "B"), is(2));
        assertThat(columnIndexOf(placements, "A"), is(0));
        assertThat(columnIndexOf(placements, "B"), is(1));
    }

    // -------------------------------------------------- additional comparator

    @Test
    public void shouldUseAdditionalComparatorForEqualTimeBounds() {

        // given
        TestEntryView a = view("A", 9, 10);
        TestEntryView b = view("B", 9, 10);
        List<TestEntryView> views = views(a, b);
        TimeBoundsResolver.setAdditionalComparator(descendingByTitle());

        // when
        TimeBoundsResolver.resolve(views);

        // then
        assertThat(views, contains(b, a));
    }

    @Test
    public void shouldNotLetAdditionalComparatorBreakTimeOrder() {

        // given
        TestEntryView a = view("A", 9, 10);
        TestEntryView b = view("B", 14, 15);
        List<TestEntryView> views = views(b, a);
        TimeBoundsResolver.setAdditionalComparator(descendingByTitle());

        // when
        TimeBoundsResolver.resolve(views);

        // then
        assertThat(views, contains(a, b));
    }

    // ---------------------------------------------------------------- helpers

    private java.util.Comparator<EntryViewBase<?>> descendingByTitle() {
        return (v1, v2) -> v2.getEntry().getTitle().compareTo(v1.getEntry().getTitle());
    }

    /**
     * The invariant the resolver has to fulfill: entries that overlap in time
     * must never share horizontal space.
     */
    private void assertNoVisualOverlap(List<Placement> placements) {
        for (int i = 0; i < placements.size(); i++) {
            for (int j = i + 1; j < placements.size(); j++) {
                Placement p1 = placements.get(i);
                Placement p2 = placements.get(j);

                if (!intersectInTime(p1.getEntryView(), p2.getEntryView())) {
                    continue;
                }

                double x1 = (double) p1.getColumnIndex() / p1.getColumnCount();
                double x2 = x1 + 1d / p1.getColumnCount();
                double y1 = (double) p2.getColumnIndex() / p2.getColumnCount();
                double y2 = y1 + 1d / p2.getColumnCount();

                if (x1 < y2 && y1 < x2) {
                    throw new AssertionError("entries overlap in time and in space: " + p1 + " / " + p2);
                }
            }
        }
    }

    private boolean intersectInTime(EntryViewBase<?> view1, EntryViewBase<?> view2) {
        return startOf(view1).isBefore(endOf(view2)) && startOf(view2).isBefore(endOf(view1));
    }

    private ZonedDateTime startOf(EntryViewBase<?> view) {
        Entry<?> entry = view.getEntry();
        if (entry.isFullDay()) {
            return entry.getStartAsZonedDateTime().with(LocalTime.MIN);
        }
        return entry.getStartAsZonedDateTime();
    }

    private ZonedDateTime endOf(EntryViewBase<?> view) {
        Entry<?> entry = view.getEntry();
        if (entry.isFullDay()) {
            return entry.getEndAsZonedDateTime().with(LocalTime.MAX);
        }
        return entry.getEndAsZonedDateTime();
    }

    private int columnIndexOf(List<Placement> placements, String title) {
        return placementOf(placements, title).getColumnIndex();
    }

    private int columnCountOf(List<Placement> placements, String title) {
        return placementOf(placements, title).getColumnCount();
    }

    private Placement placementOf(List<Placement> placements, String title) {
        return placements.stream()
                .filter(p -> title.equals(p.getEntryView().getEntry().getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no placement found for entry " + title));
    }

    private Map<String, Integer> columnIndices(List<Placement> placements) {
        Map<String, Integer> result = new LinkedHashMap<>();
        placements.forEach(p -> result.put(p.getEntryView().getEntry().getTitle(), p.getColumnIndex()));
        return result;
    }

    private Map<String, Integer> columnCounts(List<Placement> placements) {
        Map<String, Integer> result = new LinkedHashMap<>();
        placements.forEach(p -> result.put(p.getEntryView().getEntry().getTitle(), p.getColumnCount()));
        return result;
    }

    private List<String> titles(List<TestEntryView> views) {
        List<String> result = new ArrayList<>();
        views.forEach(v -> result.add(v.getEntry().getTitle()));
        return result;
    }

    private <E> List<List<E>> permutations(List<E> input) {
        List<List<E>> result = new ArrayList<>();
        permute(new ArrayList<>(input), new ArrayList<>(), result);
        return result;
    }

    private <E> void permute(List<E> remaining, List<E> current, List<List<E>> result) {
        if (remaining.isEmpty()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < remaining.size(); i++) {
            E element = remaining.remove(i);
            current.add(element);
            permute(remaining, current, result);
            current.remove(current.size() - 1);
            remaining.add(i, element);
        }
    }

    private List<TestEntryView> views(TestEntryView... views) {
        return new ArrayList<>(Arrays.asList(views));
    }

    private TestEntryView view(String title, int startHour, int endHour) {
        Entry<String> entry = new Entry<>(title);
        entry.setZoneId(ZONE);
        entry.setInterval(new Interval(DATE, LocalTime.of(startHour, 0), DATE, LocalTime.of(endHour, 0), ZONE));
        entry.setCalendar(calendar);
        return new TestEntryView(entry);
    }

    private TestEntryView fullDayView(String title) {
        Entry<String> entry = new Entry<>(title);
        entry.setZoneId(ZONE);
        entry.setInterval(new Interval(DATE, LocalTime.MIN, DATE, LocalTime.MIN, ZONE));
        entry.setFullDay(true);
        entry.setCalendar(calendar);
        return new TestEntryView(entry);
    }

    private static final class TestEntryView extends EntryViewBase<DayView> {

        TestEntryView(Entry<?> entry) {
            super(entry);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return null;
        }

        @Override
        public String toString() {
            return String.valueOf(getEntry().getTitle());
        }
    }
}
