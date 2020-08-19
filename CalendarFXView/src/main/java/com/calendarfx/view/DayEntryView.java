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

import com.calendarfx.model.Entry;
import impl.com.calendarfx.view.DayEntryViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.List;

/**
 * A view representing an entry inside the {@link DayView} control. Instances of
 * this type are created by the {@link DayView} itelf via a pluggable factory.
 * The image below shows the default apperance of this view.
 *
 * <img src="doc-files/day-entry-view.png" alt="Day Entry View">
 *
 * @see DayView#entryViewFactoryProperty()
 */
public class DayEntryView extends EntryViewBase<DayView> {

    /**
     * Constructs a new entry view for the given calendar entry.
     *
     * @param entry the entry for which the view will be created
     */
    public DayEntryView(Entry<?> entry) {
        super(entry);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DayEntryViewSkin(this);
    }

    private final ReadOnlyMapWrapper<Pos, List<Node>> nodes = new ReadOnlyMapWrapper<>(this, "nodes");

    /**
     * A day entry view can be decorated with symbols / nodes. These nodes are stored
     * in a hash map where the position of the nodes is the key.
     *
     * @return the map of nodes
     */
    public final ObservableMap<Pos, List<Node>> getNodes() {
        return nodes.getReadOnlyProperty();
    }

    /**
     * Returns the hashmap used to store nodes used for decorating the entry view.
     *
     * @return the nodes map
     */
    public final ReadOnlyMapWrapper<Pos, List<Node>> nodesProperty() {
        return nodes;
    }

    /**
     * Removes all nodes from all positions.
     */
    public void clearNodes() {
        if (nodes.get() != null) {
            nodes.get().clear();
        }
    }

    /**
     * Adds a node to the given position to the entry view.
     *
     * @param pos the position for the node
     * @param node the node itself
     */
    public void addNode(Pos pos, Node node) {
        if (nodes.get() == null) {
             nodes.set(FXCollections.observableHashMap());
        }

        // force map invalidation event by completely replacing the list instead of just
        // adding the new node to the existing list
        final List<Node> nodes = this.nodes.computeIfAbsent(pos, p -> new ArrayList<>());
        final List<Node> newNodes = new ArrayList<>(nodes);
        newNodes.add(node);

        this.nodes.put(pos, newNodes);
    }

    /**
     * Removes the given node from the entry view.
     *
     * @param node the node to remove
     */
    public void removeNode(Node node) {
        if (nodes.get() != null) {
            nodes.values().forEach(nodesList -> nodesList.remove(node));
        }
    }

    // MIN HEIGHT / TITLE HEIGHT

    private final BooleanProperty minHeightEqualToTitleHeight = new SimpleBooleanProperty(this, "minHeightEqualToTitleHeight", true);

    /**
     * Controls whether the day entry view will at least always have the height of the title label.
     *
     * @return true if the entry has a minimum height that guarantees the visibility of the title label
     */
    public final BooleanProperty minHeightEqualToTitleHeightProperty() {
        return minHeightEqualToTitleHeight;
    }

    public final boolean isMinHeightEqualToTitleHeight() {
        return minHeightEqualToTitleHeight.get();
    }

    public final void setMinHeightEqualToTitleHeight(boolean minHeightEqualToTitleHeight) {
        this.minHeightEqualToTitleHeight.set(minHeightEqualToTitleHeight);
    }
}
