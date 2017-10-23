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

package com.calendarfx.experimental;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;

import java.util.Set;

/**
 * Created by dirk on 18/01/17.
 */
public class IntroPaneSkin extends SkinBase<IntroPane> {

    public IntroPaneSkin(IntroPane pane) {
        super(pane);

        InvalidationListener updateViewListener = (Observable it) -> updateView();
        pane.getTargets().addListener(updateViewListener);
        pane.widthProperty().addListener(updateViewListener);
        pane.heightProperty().addListener(updateViewListener);

        Platform.runLater(() -> updateView());
    }

    private void updateView() {
        getChildren().clear();

        getSkinnable().setVisible(false);
        final Scene scene = getSkinnable().getScene();
        if (scene == null) {
            return;
        }

        for (IntroPane.IntroTarget target : getSkinnable().getTargets()) {
            Set<Node> nodes = target.getParent().lookupAll(target.getId());
            if (nodes != null) {
                nodes.forEach(node -> snapshotNode(scene, node));
            }
        }

        getSkinnable().setVisible(true);
        getSkinnable().requestLayout();
    }

    private void snapshotNode(Scene scene, Node node) {
        SnapshotParameters params = new SnapshotParameters();
        Bounds layoutBounds = node.getLayoutBounds();
        Bounds bounds = node.localToScene(layoutBounds);

        if (!(bounds.getWidth() > 0 && bounds.getHeight() > 0)) {
            return;
        }

        params.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));
        WritableImage writable = new WritableImage((int) bounds.getWidth(), (int) bounds.getHeight());
        writable = scene.getRoot().snapshot(params, writable);

        ImageView imageView = new ImageView(writable);
        imageView.getStyleClass().add("snapshot-image");
        imageView.setManaged(false);
        imageView.setLayoutX(bounds.getMinX());
        imageView.setLayoutY(bounds.getMinY());
        imageView.setFitWidth(bounds.getWidth());
        imageView.setFitHeight(bounds.getHeight());

        Region rect = new Region();
        rect.getStyleClass().add("snapshot-background");
        rect.setLayoutX(bounds.getMinX() - 5);
        rect.setLayoutY(bounds.getMinY() - 5);
        rect.resize(bounds.getWidth() + 10, bounds.getHeight() + 10);
        rect.setManaged(false);

        Line line = new Line();
        line.setStartX(bounds.getMaxX() + 4);
        line.setStartY(bounds.getMaxY() + 4);
        line.setEndX(bounds.getMaxX() + 200);
        line.setEndY(bounds.getMaxY() + 200);
        line.setStroke(imagePattern);
        line.setStrokeWidth(5);
        line.setManaged(false);

        getChildren().addAll(rect, imageView); //, line);
    }

    private final ImagePattern imagePattern = new ImagePattern(new Image(IntroPane.class.getResource("texture.jpg").toExternalForm()));
}
