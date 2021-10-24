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

package impl.com.calendarfx.view;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.util.LoggingDomain;
import com.calendarfx.view.AgendaView;
import com.calendarfx.view.AgendaView.AgendaEntry;
import com.calendarfx.view.Messages;

import impl.com.calendarfx.view.util.Util;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

public class AgendaViewSkin extends DateControlSkin<AgendaView>
        implements LoadDataSettingsProvider {

    private static final String AGENDA_VIEW_PLACEHOLDER_LABEL = "placeholder-label";

    private final ListView<AgendaEntry> listView;

    private final DataLoader dataLoader = new DataLoader(this);

    private final Label statusLabel;

    public AgendaViewSkin(AgendaView view) {
        super(view);

        listView = view.getListView();
        listView.setMinWidth(1);
        listView.setFixedCellSize(-1);
        listView.setSelectionModel(Util.createEmptySelectionModel());
        listView.getStyleClass().add("agenda-view-list");

        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.visibleProperty().bind(view.showStatusLabelProperty());
        statusLabel.managedProperty().bind(statusLabel.visibleProperty());

        Label placeholderLabel = new Label(
                Messages.getString("AgendaViewSkin.NO_ENTRIES"));
        placeholderLabel.getStyleClass().add(AGENDA_VIEW_PLACEHOLDER_LABEL);
        listView.setPlaceholder(placeholderLabel);

        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("container");
        getChildren().add(borderPane);

        borderPane.setCenter(listView);
        borderPane.setTop(statusLabel);

        InvalidationListener reloadListener = it -> updateList(
                "a view property has changed, property = " + it.toString());
        view.lookAheadPeriodInDaysProperty().addListener(reloadListener);
        view.lookBackPeriodInDaysProperty().addListener(reloadListener);
        view.enableHyperlinksProperty().addListener(reloadListener);
        view.getCalendars().addListener(reloadListener);

        updateList("initial loading");

        listenToCalendars();

        view.getCalendars()
                .addListener((Observable observable) -> listenToCalendars());

        view.dateProperty().addListener(reloadListener);
    }

    private final InvalidationListener calendarVisibilityChanged = it -> updateList(
            "calendar visibility changed");

    private final WeakInvalidationListener weakCalendarVisibilityChanged = new WeakInvalidationListener(
            calendarVisibilityChanged);

    private void listenToCalendars() {
        for (Calendar c : getSkinnable().getCalendars()) {
            getSkinnable().getCalendarVisibilityProperty(c)
                    .addListener(weakCalendarVisibilityChanged);
        }
    }

    @Override
    protected void calendarChanged(Calendar calendar) {
        updateList("calendar changed");
    }

    @Override
    protected void entryIntervalChanged(CalendarEvent evt) {
        updateList(evt, "entry interval changed, entry = " + evt.getEntry());
    }

    @Override
    protected void entryRecurrenceRuleChanged(CalendarEvent evt) {
        updateList(evt,
                "entry recurrence rule changed, entry = " + evt.getEntry());
    }

    @Override
    protected void entryFullDayChanged(CalendarEvent evt) {
        updateList(evt,
                "entry full day changed changed, entry = " + evt.getEntry());
    }

    @Override
    protected void entryCalendarChanged(CalendarEvent evt) {
        updateList(evt, "entry calendar changed, entry = " + evt.getEntry());
    }

    @Override
    protected void refreshData() {
        updateList("data refresh");
    }

    private void updateList(final CalendarEvent evt, String reason) {
        Entry<?> entry = evt.getEntry();

        // TODO: this can be optimized more to only update when really needed
        if (isRelevant(entry)) {
            updateList(reason);
        }
    }

    private void updateList(String reason) {
        if (LoggingDomain.VIEW.isLoggable(Level.FINE)) {
            LoggingDomain.VIEW.fine(
                    "updating list inside agenda view, reason = " + reason);
        }

        Map<LocalDate, List<Entry<?>>> dataMap = new HashMap<>();
        dataLoader.loadEntries(dataMap);
        List<AgendaEntry> listEntries = new ArrayList<>();
        for (LocalDate date : dataMap.keySet()) {
            AgendaEntry listViewEntry = new AgendaEntry(date);
            for (Entry<?> entry : dataMap.get(date)) {
                listViewEntry.getEntries().add(entry);
            }
            listEntries.add(listViewEntry);
        }

        Collections.sort(listEntries);
        listView.getItems().setAll(listEntries);

        String startTime = getSkinnable().getDateTimeFormatter()
                .format(getLoadStartDate());
        String endTime = getSkinnable().getDateTimeFormatter()
                .format(getLoadEndDate());

        statusLabel.setText(MessageFormat.format(
                Messages.getString("AgendaViewSkin.AGENDA_TIME_RANGE"),
                startTime, endTime));
    }

    @Override
    public String getLoaderName() {
        return "Agenda View";
    }

    @Override
    public LocalDate getLoadStartDate() {
        return getSkinnable().getDate()
                .minusDays(getSkinnable().getLookBackPeriodInDays());
    }

    @Override
    public LocalDate getLoadEndDate() {
        return getSkinnable().getDate()
                .plusDays(getSkinnable().getLookAheadPeriodInDays());
    }

    @Override
    public ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    @Override
    public List<CalendarSource> getCalendarSources() {
        return getSkinnable().getCalendarSources();
    }

    @Override
    public Control getControl() {
        return getSkinnable();
    }

    @Override
    public boolean isCalendarVisible(Calendar calendar) {
        return getSkinnable().isCalendarVisible(calendar);
    }

}
