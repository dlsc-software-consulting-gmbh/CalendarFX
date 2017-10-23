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

package com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import impl.com.calendarfx.view.SearchResultViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Skin;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.calendarfx.util.LoggingDomain.SEARCH;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.FINE;

/**
 * A view used for searching entries in the calendars and for displaying the
 * results.
 * <p/>
 * <center><img src="doc-files/search-result-view.png"></center>
 * <p/>
 * To perform a search the application simply needs to change the value of the
 * text property of this view. The easiest way to do this is to bind the
 * property to the text property of a textfield.
 *
 * @see Calendar#findEntries(String)
 * @see Entry#matches(String)
 */
public class SearchResultView extends CalendarFXControl {

    private static final String DEFAULT_STYLE_CLASS = "search-result-view";

    private static final String SELECTED_ENTRY = "selected.search.result"; //$NON-NLS-1$

    private final SearchService searchService;

    /**
     * Constructs a new view.
     */
    public SearchResultView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        searchService = new SearchService();
        searchService.setOnSucceeded(evt -> updateSearchResults());

        searchTextProperty().addListener(it -> {
            if (SEARCH.isLoggable(FINE)) {
                SEARCH.fine("restarting search service"); //$NON-NLS-1$
            }

            searchService.restart();
        });

        /*
         * Listens to changes to the properties map. Each control has a
         * properties map associated with it. We are using the map to pass
         * values from the skin to the control. This allows the skin to update
         * read-only properties.
         */
        MapChangeListener<? super Object, ? super Object> listener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals(SELECTED_ENTRY)) {
                    Entry<?> entry = (Entry<?>) change.getValueAdded();
                    selectedEntry.set(entry);
                    getProperties().remove(SELECTED_ENTRY);
                }
            }
        };

        getProperties().addListener(listener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SearchResultViewSkin(this);
    }

    private final ObservableList<Entry<?>> searchResults = FXCollections.observableArrayList();

    /**
     * The list containing the search results.
     *
     * @return the search results
     */
    public final ObservableList<Entry<?>> getSearchResults() {
        return searchResults;
    }

    private void updateSearchResults() {
        List<Entry<?>> searchResult = searchService.getValue();
        getSearchResults().setAll(searchResult);
    }

    private final ObservableList<CalendarSource> calendarSources = FXCollections
            .observableArrayList();

    /**
     * The list of calendar sources where the view will perform the search.
     *
     * @return the calendar sources
     */
    public final ObservableList<CalendarSource> getCalendarSources() {
        return calendarSources;
    }

    private final ReadOnlyObjectWrapper<Entry<?>> selectedEntry = new ReadOnlyObjectWrapper<>(
            this, "selectedEntry"); //$NON-NLS-1$

    /**
     * Stores the currently selected entry / search result.
     *
     * @return the selected result
     */
    public final ReadOnlyObjectProperty<Entry<?>> selectedEntryProperty() {
        return this.selectedEntry.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #selectedEntryProperty()}.
     *
     * @return the selected entry / search result
     */
    public final Entry<?> getSelectedEntry() {
        return this.selectedEntryProperty().get();
    }

    private final ObjectProperty<ZoneId> zoneId = new SimpleObjectProperty<>(
            this, "zoneId", ZoneId.systemDefault()); //$NON-NLS-1$

    /**
     * Provides the current time zone. The zone is required for searches as the
     * view will invoke {@link Calendar#findEntries(String)} where the
     * time zone is a required parameter.
     *
     * @return the time zone
     */
    public final ObjectProperty<ZoneId> zoneIdProperty() {
        return zoneId;
    }

    /**
     * Sets the value of {@link #zoneIdProperty()}.
     *
     * @param zoneId
     *            the time zone
     */
    public final void setZoneId(ZoneId zoneId) {
        requireNonNull(zoneId);
        zoneIdProperty().set(zoneId);
    }

    /**
     * Returns the value of {@link #zoneIdProperty()}.
     *
     * @return the time zone
     */
    public final ZoneId getZoneId() {
        return zoneIdProperty().get();
    }

    private final StringProperty searchText = new SimpleStringProperty(this,
            "searchText"); //$NON-NLS-1$

    /**
     * The text used to perform the search in the calendars.
     *
     * @see Calendar#findEntries(String)
     * @see Entry#matches(String)
     *
     * @return the search term
     */
    public final StringProperty searchTextProperty() {
        return searchText;
    }

    /**
     * Returns the value of {@link #searchTextProperty()}.
     *
     * @return the search text
     */
    public final String getSearchText() {
        return searchTextProperty().get();
    }

    /**
     * Sets the value of {@link #searchTextProperty()}.
     *
     * @param text
     *            the search text
     */
    public final void setSearchText(String text) {
        searchTextProperty().set(text);
    }

    private class SearchService extends Service<List<Entry<?>>> {

        public SearchService() {
        }

        @Override
        protected Task<List<Entry<?>>> createTask() {
            return new SearchTask();
        }

        class SearchTask extends Task<List<Entry<?>>> {

            @Override
            protected List<Entry<?>> call() throws Exception {
                if (!isCancelled()) {

                    String searchText = getSearchText();

                    if (SEARCH.isLoggable(FINE)) {
                        SEARCH.fine("search text: " + searchText); //$NON-NLS-1$
                    }

                    if (searchText == null || searchText.trim().isEmpty()) {
                        return Collections.emptyList();
                    }

                    /*
                     * Let's sleep a little bit, so we don't run a query after
                     * every key press event.
                     */
                    Thread.sleep(200);

                    if (SEARCH.isLoggable(FINE)) {
                        SEARCH.fine("performing search after delay"); //$NON-NLS-1$
                    }

                    if (!isCancelled()) {

                        List<Entry<?>> totalResult = new ArrayList<>();

                        for (CalendarSource source : getCalendarSources()) {

                            if (SEARCH.isLoggable(FINE)) {
                                SEARCH.fine("searching in source " //$NON-NLS-1$
                                        + source.getName());
                            }

                            for (Calendar calendar : source.getCalendars()) {

                                if (SEARCH.isLoggable(FINE)) {
                                    SEARCH.fine("searching in calendar " //$NON-NLS-1$
                                            + calendar.getName());
                                }

                                if (!isCancelled()) {
                                    try {
                                        List<? extends Entry<?>> result = calendar
                                                .findEntries(searchText);
                                        if (result != null) {
                                            for (Entry<?> entry : result) {
                                                totalResult.add(entry);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        if (SEARCH.isLoggable(FINE)) {
                            if (isCancelled()) {
                                SEARCH.fine("search was canceled"); //$NON-NLS-1$
                            }
                        }

                        if (SEARCH.isLoggable(FINE)) {
                            SEARCH.fine(
                                    "found " + totalResult.size() + " entries"); //$NON-NLS-1$ //$NON-NLS-2$
                        }

                        return totalResult;
                    }
                }

                if (SEARCH.isLoggable(FINE)) {
                    SEARCH.fine("returning empty search result"); //$NON-NLS-1$
                }

                return Collections.emptyList();
            }
        }
    }
}
