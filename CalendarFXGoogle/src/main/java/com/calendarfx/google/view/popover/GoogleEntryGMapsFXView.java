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

package com.calendarfx.google.view.popover;

import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.view.popover.EntryPopOverPane;
import com.google.code.geocoder.model.GeocoderGeometry;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

/**
 * Pane that shows in a map the location of the entry.
 *
 * Created by gdiaz on 13/01/2017.
 */
public class GoogleEntryGMapsFXView extends EntryPopOverPane implements MapComponentInitializedListener {

    private final GoogleEntry entry;
    private final GoogleMapView mapView = new GoogleMapView();
    private final StackPane mapViewWrapper = new StackPane();
    private final LocationFXService service = new LocationFXService();

    GoogleEntryGMapsFXView(GoogleEntry entry) {
        this.entry = entry;
        this.entry.locationProperty().addListener(obs -> updateLocation());

        this.mapView.addMapInializedListener(this);
        this.mapView.getStyleClass().add("map");
        this.mapView.setMouseTransparent(true);

        this.mapViewWrapper.getChildren().add(mapView);
        this.mapViewWrapper.setVisible(false);
        this.mapViewWrapper.managedProperty().bind(this.mapView.visibleProperty());
        this.mapViewWrapper.setPrefSize(300, 300);

        StackPane stackPane = new StackPane();
        stackPane.getStyleClass().add("map-view-wrapper");
        stackPane.getChildren().add(mapViewWrapper);
        getChildren().add(stackPane);
    }

    @Override
    public void mapInitialized() {
        Platform.runLater(this::updateLocation);
    }

    private GoogleMap createMap() {
        MapOptions options = new MapOptions();
        options.zoom(15)
                .overviewMapControl(false)
                .mapTypeControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .mapType(MapTypeIdEnum.ROADMAP);
        return mapView.createMap(options, false);
    }

    private void updateLocation() {
        service.restart();
    }

    private class LocationFXService extends Service<GeocoderGeometry> {

        @Override
        protected Task<GeocoderGeometry> createTask() {
            return new Task<GeocoderGeometry>() {
                @Override
                protected GeocoderGeometry call() throws Exception {
                    Thread.sleep(1000);
                    return GoogleConnector.getInstance().getGeocoderService().locationToCoordinate(entry.getLocation());
                }
            };
        }

        @Override
        public void start() {
            super.start();
            mapViewWrapper.setVisible(false);
        }

        @Override
        protected void failed() {
            super.failed();
            mapViewWrapper.setVisible(false);
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            GeocoderGeometry geometry = getValue();

            if (geometry != null) {
                LatLong position = new LatLong(geometry.getLocation().getLat().doubleValue(), geometry.getLocation().getLng().doubleValue());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(position);
                markerOptions.title(entry.getTitle());
                Marker marker = new Marker(markerOptions);
                GoogleMap map = createMap();
                map.addMarker(marker);
                map.setCenter(position);
                map.panTo(position);
                mapViewWrapper.setVisible(true);
                Platform.runLater(() -> Platform.runLater(() -> map.setCenter(position)));
            }
        }
    }

}
