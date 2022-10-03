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

package com.calendarfx.view.popover;

import com.calendarfx.model.Entry;
import com.calendarfx.util.LoggingDomain;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Pane that shows in a map the location of the entry.
 */
public class EntryMapView extends EntryPopOverPane {

    private final Entry<?> entry;

    private final ImageLoader imageLoader = new ImageLoader();

    private final InvalidationListener invalidationListener = it -> imageLoader.restart();

    private final WeakInvalidationListener weakInvalidationListener = new WeakInvalidationListener(invalidationListener);

    public EntryMapView(Entry<?> entry) {
        this.entry = Objects.requireNonNull(entry);

        StackPane mapViewPane = new StackPane();
        mapViewPane.setPrefSize(340, 240);
        mapViewPane.setPadding(new Insets(1));
        mapViewPane.setStyle("-fx-border-color: gray; -fx-border-radius: 4px;");
        mapViewPane.backgroundProperty().bind(Bindings.createObjectBinding(() -> new Background(new BackgroundImage(imageLoader.getValue(), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, false, true))), imageLoader.valueProperty()));

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
            mapViewPane.setCursor(Cursor.HAND);
            mapViewPane.setOnMouseClicked(evt -> {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URL("https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(entry.getLocation(), StandardCharsets.UTF_8)).toURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(mapViewPane.widthProperty());
        clip.heightProperty().bind(mapViewPane.heightProperty());
        clip.setArcHeight(4);
        clip.setArcWidth(4);
        mapViewPane.setClip(clip);

        getChildren().add(mapViewPane);

        entry.locationProperty().addListener(weakInvalidationListener);

        visibleProperty().bind(entry.locationProperty().isNotNull().and(googleApiKeyProperty().isNotNull()));
        managedProperty().bind(entry.locationProperty().isNotNull().and(googleApiKeyProperty().isNotNull()));

        imageLoader.restart();
    }

    private class ImageLoader extends Service<Image> {
        public ImageLoader() {
        }

        @Override
        protected Task createTask() {
            return new ImageTask(entry.getLocation());
        }
    }

    private class ImageTask extends Task<Image> {

        private final String location;

        public ImageTask(String location) {
            this.location = location;
        }

        @Override
        protected Image call() throws Exception {
            Thread.sleep(500);

            if (isCancelled() || getGoogleApiKey().isBlank() || location == null || location.trim().equals("")) {
                return null;
            }

            return loadMapImage(location);
        }

    }

    public Image loadMapImage(String address) {
        if (address != null) {
            try {
                String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
                String key = getGoogleApiKey();
                String url = "https://maps.googleapis.com/maps/api/staticmap?center=" + encodedAddress + "&markers=color:0xE94B35%7C" + encodedAddress + "&scale=2&zoom=16&size=340x240&key=" + key;
                LoggingDomain.VIEW.fine("google map call (static image): " + url);
                Image image = new Image(url);
                return image;
            } catch (Exception ex) {
                LoggingDomain.VIEW.log(Level.SEVERE, "error when trying to load google maps static image", ex);
            }
        }

        return null;
    }

    private final StringProperty googleApiKey = new SimpleStringProperty(this, "googleApiKey", System.getProperty("calendarfx.google.api.key"));

    public final String getGoogleApiKey() {
        return googleApiKey.get();
    }

    public final StringProperty googleApiKeyProperty() {
        return googleApiKey;
    }

    public final void setGoogleApiKey(String googleApiKey) {
        this.googleApiKey.set(googleApiKey);
    }
}
