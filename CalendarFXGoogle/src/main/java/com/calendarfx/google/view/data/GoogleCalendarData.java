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

package com.calendarfx.google.view.data;

import com.calendarfx.google.model.GoogleEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class storing data about the entries loaded from google for one single calendar.
 *
 * Created by gdiaz on 27/02/2017.
 */
public final class GoogleCalendarData {

    /**
     * Set of Ids already loaded.
     */
    private final Set<String> loadedEntryIds = new HashSet<>();

    /**
     * Set of searchText already loaded.
     */
    private final Set<String> loadedSearchText = new HashSet<>();

    /**
     * Set of slices already loaded for this calendar.
     */
    private final Set<Slice> loadedSlices = new TreeSet<>();

    /**
     * Set of slices being loaded in background.
     */
    private final Set<Slice> inProgressSlices = new TreeSet<>();

    /**
     * Takes the list of slices and removes those already loaded.
     *
     * @param slices the slices to be processed.
     * @return A new list containing the unloaded slices.
     */
    public List<Slice> getUnloadedSlices(List<Slice> slices) {
        List<Slice> unloadedSlices = new ArrayList<>(slices);
        unloadedSlices.removeAll(loadedSlices);
        unloadedSlices.removeAll(inProgressSlices);
        return unloadedSlices;
    }

    /**
     * Adds the given list of slices to the ones being loaded.  This produces the slices are not classified as not loaded but in progress loading.
     *
     * @param slices The list of slices to be added.
     */
    public void addInProgressSlices(List<Slice> slices) {
        inProgressSlices.addAll(slices);
    }

    /**
     * Adds the given slice to the list of loaded ones.  This also removes it from the list of being loaded ones, in case it was added.
     *
     * @param slice The slice to be added.
     */
    public void addLoadedSlice(Slice slice) {
        loadedSlices.add(slice);
        inProgressSlices.remove(slice);
    }

    /**
     * Checks whether an entry was already added or not.
     *
     * @param entry The entry to be loaded.
     * @return True or false.
     */
    public boolean isLoadedEntry(GoogleEntry entry) {
        return loadedEntryIds.contains(entry.getId());
    }

    /**
     * Puts the entry in the loaded ones.
     *
     * @param entry The entry loaded.
     */
    public void addLoadedEntry(GoogleEntry entry) {
        loadedEntryIds.add(entry.getId());
    }

    /**
     * Checks whether the search text was already loaded or not.
     *
     * @param searchText The search text.
     * @return True or false.
     */
    public boolean isLoadedSearchText(String searchText) {
        return loadedSearchText.contains(searchText);
    }

    /**
     * Puts the text in the loaded ones.
     *
     * @param searchText The text to be added.
     */
    public void addLoadedSearchText(String searchText) {
        loadedSearchText.add(searchText);
    }

    /**
     * Clears the data stored about the calendar.
     */
    public void clear() {
        loadedSlices.clear();
        loadedSearchText.clear();
        loadedEntryIds.clear();
    }

}
