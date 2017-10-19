/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.print;

import com.calendarfx.view.print.ZoomPane;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class ZoomPaneSkin extends SkinBase<ZoomPane> {
	
	private static final double INITIAL_SCALE_FACTOR = 0.98;

	private final DoubleProperty scale = new SimpleDoubleProperty();
	private final DoubleProperty scaleX = new SimpleDoubleProperty();
	private final DoubleProperty scaleY = new SimpleDoubleProperty();

	private double mouseX;
	private double mouseY;

	public ZoomPaneSkin(ZoomPane pane) {
		super(pane);
		
		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(pane.widthProperty());
		clip.heightProperty().bind(pane.heightProperty());
		
		pane.setClip(clip);
		pane.contentProperty().addListener((obs, oldValue, newValue) -> updateView(oldValue, newValue));
		pane.zoomProperty().addListener(obs -> getSkinnable().requestLayout());
		
		updateView(null, pane.getContent());
	}

	private void updateView(Region oldValue, Region newValue) {
		if (oldValue != null) {
			oldValue.scaleXProperty().unbind();
			oldValue.scaleYProperty().unbind();
			oldValue.setOnMousePressed(null);
			oldValue.setOnMouseDragged(null);
		}

		Objects.requireNonNull(newValue);
		newValue.setManaged(false);
		newValue.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
			mouseX = evt.getSceneX();
			mouseY = evt.getSceneY();
		});

		newValue.addEventFilter(MouseEvent.MOUSE_DRAGGED, evt -> {
			double deltaX = evt.getSceneX() - mouseX;
			double deltaY = evt.getSceneY() - mouseY;
			
			newValue.relocate(newValue.getLayoutX() + deltaX, newValue.getLayoutY() + deltaY);
			
			mouseX = evt.getSceneX();
			mouseY = evt.getSceneY();
		});

		scale.unbind();
		scale.bind(Bindings.multiply(Bindings.min(scaleX, scaleY), getSkinnable().zoomProperty()));

		scaleX.unbind();
		scaleX.bind(Bindings.min(1, Bindings.divide(Bindings.multiply(INITIAL_SCALE_FACTOR, getSkinnable().widthProperty()), newValue.widthProperty())));

		scaleY.unbind();
		scaleY.bind(Bindings.min(1, Bindings.divide(Bindings.multiply(INITIAL_SCALE_FACTOR, getSkinnable().heightProperty()), newValue.heightProperty())));

		newValue.scaleXProperty().bind(scale);
		newValue.scaleYProperty().bind(scale);

		getChildren().setAll(newValue);
	}
	
	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
		Region content = Objects.requireNonNull(getSkinnable().getContent());

		double w = content.getPrefWidth();
		double h = content.getPrefHeight();

		double mx = contentX + (contentWidth / 2);
		double my = contentY + (contentHeight / 2);

		content.resizeRelocate(mx - w / 2, my - h / 2, w, h);
	}
}
