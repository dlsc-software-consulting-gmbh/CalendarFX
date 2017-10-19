/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package com.calendarfx.view.popover;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;

public class PopOverContentPane extends BorderPane {

	public PopOverContentPane() {
		super();

		topProperty().bind(headerProperty());

		Accordion accordion = new Accordion();
		accordion.getStyleClass().add("popover-accordion"); //$NON-NLS-1$
		setCenter(accordion);

		Bindings.bindContentBidirectional(getPanes(), accordion.getPanes());
		Bindings.bindBidirectional(expandedPaneProperty(),
				accordion.expandedPaneProperty());

		bottomProperty().bind(footerProperty());

		headerProperty().addListener((value, oldNode, newNode) -> {
			if (newNode != null) {
				String style = "popover-header"; //$NON-NLS-1$
				if (!newNode.getStyleClass().contains(style)) {
					newNode.getStyleClass().add(style);
				}
			}
		});

		footerProperty().addListener((value, oldNode, newNode) -> {
			if (newNode != null) {
				String style = "popover-footer"; //$NON-NLS-1$
				if (!newNode.getStyleClass().contains(style)) {
					newNode.getStyleClass().add(style);
				}
			}
		});
	}

	// header support

	private final ObjectProperty<Node> header = new SimpleObjectProperty<>(
            this, "header"); //$NON-NLS-1$

	public final ObjectProperty<Node> headerProperty() {
		return header;
	}

	public final Node getHeader() {
		return headerProperty().get();
	}

	public final void setHeader(Node node) {
		headerProperty().set(node);
	}

	// footer support

	private final ObjectProperty<Node> footer = new SimpleObjectProperty<>(
            this, "footer"); //$NON-NLS-1$

	public final ObjectProperty<Node> footerProperty() {
		return footer;
	}

	public final Node getFooter() {
		return footerProperty().get();
	}

	public final void setFooter(Node node) {
		footerProperty().set(node);
	}

	// panes

	private final ObservableList<TitledPane> panes = FXCollections
			.observableArrayList();

	public final ObservableList<TitledPane> getPanes() {
		return panes;
	}

	// Expanded pane support

	private final ObjectProperty<TitledPane> expandedPane = new SimpleObjectProperty<>(
            this, "expandedPane"); //$NON-NLS-1$

	public final ObjectProperty<TitledPane> expandedPaneProperty() {
		return expandedPane;
	}

	public final void setExpandedPane(TitledPane titledPane) {
		expandedPaneProperty().set(titledPane);
	}

	public final TitledPane getExpandedPane() {
		return expandedPane.get();
	}
}
