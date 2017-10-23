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

package impl.com.calendarfx.google.view;

import com.calendarfx.google.model.GoogleAccount;
import com.calendarfx.google.model.GoogleCalendar;
import com.calendarfx.google.model.GoogleEntry;
import com.calendarfx.google.service.GoogleConnector;
import com.calendarfx.google.service.SecurityService;
import com.calendarfx.google.view.GoogleCalendarAppView;
import com.calendarfx.google.view.popover.GoogleEntryPopOverContentPane;
import com.calendarfx.google.view.task.InsertCalendarTask;
import com.calendarfx.google.view.task.InsertEntryTask;
import com.calendarfx.google.view.task.LoadAllCalendarsTask;
import com.calendarfx.google.view.thread.CalendarViewTimeUpdateThread;
import com.calendarfx.google.view.thread.GoogleAutoRefreshThread;
import com.calendarfx.google.view.thread.GoogleNotificationPopupThread;
import com.calendarfx.google.view.thread.GoogleTaskExecutor;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.LoadEvent;
import com.calendarfx.view.AllDayView;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.DeveloperConsole;
import com.calendarfx.view.VirtualGrid;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.StatusBar;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

/**
 * Skin class for the {@link GoogleCalendarAppView} control, which allows to display
 * the Google Login pane.
 *
 * @author Gabriel Diaz, 14.02.2015.
 */
public class GoogleCalendarAppViewSkin extends SkinBase<GoogleCalendarAppView> {

    private final WebView loginView;
    private final BorderPane calendarPane;
    private final CalendarView calendarView;

    private final CookieManager cookieManager;
    private final GoogleCalendarDataManager dataManager;
    private final GoogleSyncManager syncManager;
    private final GoogleCalendarSearchTextManager searchProvider;

    public GoogleCalendarAppViewSkin(GoogleCalendarAppView control) {
        super(control);

        calendarView = control.getCalendarView();
        dataManager = new GoogleCalendarDataManager();
        cookieManager = new CookieManager();
        syncManager = new GoogleSyncManager();
        searchProvider = new GoogleCalendarSearchTextManager(dataManager);

        CalendarViewTimeUpdateThread timeUpdateThread = new CalendarViewTimeUpdateThread(calendarView);
        GoogleAutoRefreshThread autoRefreshThread = new GoogleAutoRefreshThread(dataManager);
        GoogleNotificationPopupThread notificationThread = new GoogleNotificationPopupThread(calendarView);

        loginView = new WebView();
        loginView.setVisible(false);
        loginView.getEngine().titleProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.contains("Success code=")) {
                String code = newValue.split("code=")[1];
                if (SecurityService.getInstance().authorize(code)) {
                    login();
                }
            }
        });

        Bindings.bindContentBidirectional(control.getLogPane().getItems(), GoogleTaskExecutor.getInstance().getLog());

        StatusBar statusBar = new StatusBar();
        statusBar.textProperty().bind(Bindings.when(GoogleTaskExecutor.getInstance().progressProperty().isEqualTo(0)).then("").otherwise("Loading..."));
        statusBar.progressProperty().bind(GoogleTaskExecutor.getInstance().progressProperty());

        calendarView.addEventFilter(LoadEvent.LOAD, dataManager);
        calendarView.setEntryFactory(new GoogleEntryCreateCallback());
        calendarView.setCalendarSourceFactory(new GoogleCalendarCreateCallback(control.getScene().getWindow()));
        calendarView.setEntryDetailsPopOverContentCallback(new GoogleEntryPopOverContentProvider());

        calendarPane = new BorderPane();
        calendarPane.setTop(createMenuBar(autoRefreshThread));
        calendarPane.setCenter(calendarView);
        calendarPane.setBottom(statusBar);

        DeveloperConsole developerConsole = calendarView.getDeveloperConsole();
        if (developerConsole != null) {
            developerConsole.getTabPane().getTabs().add(new Tab("Google", control.getLogPane()));
        }

        getChildren().add(new StackPane(loginView, calendarPane));
        CookieHandler.setDefault(cookieManager);

        timeUpdateThread.start();
        autoRefreshThread.start();
        notificationThread.start();

        attemptAutoLogin();
    }

    private MenuBar createMenuBar(GoogleAutoRefreshThread autoRefreshThread) {
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(evt -> logout());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(KeyCombination.keyCombination("shortcut+q"));
        exitItem.setOnAction(evt -> Platform.exit());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(logoutItem);
        fileMenu.getItems().add(exitItem);

        MenuItem refreshItem = new MenuItem("Refresh");
        refreshItem.setOnAction(evt -> autoRefreshThread.restart());
        refreshItem.setAccelerator(KeyCombination.keyCombination("F5"));

        ToggleGroup intervalGroup = new ToggleGroup();
        Menu autoRefreshItem = new Menu("Auto Refresh");

        for (GoogleAutoRefreshThread.RefreshInterval interval : GoogleAutoRefreshThread.RefreshInterval.values()) {
            RadioMenuItem intervalItem = new RadioMenuItem(interval.getName());
            intervalItem.setOnAction(evt -> autoRefreshThread.setDelay(interval.getTime()));
            intervalItem.setToggleGroup(intervalGroup);
            intervalItem.setSelected(interval.getTime() == autoRefreshThread.getDelay());
            autoRefreshItem.getItems().add(intervalItem);
        }

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(refreshItem, autoRefreshItem);

        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(viewMenu);

        return menuBar;
    }

    private void attemptAutoLogin() {
        if (SecurityService.getInstance().isAuthorized()) {
            login();
        } else {
            showLoginView();
        }
    }

    private void login() {
        GoogleAccount account = SecurityService.getInstance().login();
        if (account != null) {
            calendarView.getCalendarSources().setAll(account);
            account.addCalendarListeners(dataManager, searchProvider, syncManager);
            GoogleTaskExecutor.getInstance().execute(new LoadAllCalendarsTask(account));
            showCalendarPane();
        } else {
            showLoginView();
        }
    }

    private void logout() {
        if (SecurityService.getInstance().isLoggedIn()) {
            GoogleAccount account = SecurityService.getInstance().getLoggedAccount();
            calendarView.getCalendarSources().clear();
            dataManager.clearData();
            account.removeCalendarListeners(dataManager, searchProvider, syncManager);
            GoogleTaskExecutor.getInstance().clearLog();
            SecurityService.getInstance().logout();
        }
        showLoginView();
    }

    private void showLoginView() {
        cookieManager.getCookieStore().removeAll();
        loginView.getEngine().load(GoogleConnector.getInstance().getAuthorizationURL());
        loginView.setVisible(true);
        calendarPane.setVisible(false);
    }

    private void showCalendarPane() {
        cookieManager.getCookieStore().removeAll();
        loginView.getEngine().load(GoogleConnector.getInstance().getAuthorizationURL());
        loginView.setVisible(false);
        calendarPane.setVisible(true);
    }

    /**
     * Factory for calendars.
     *
     * Created by gdiaz on 5/05/2017.
     */
    private static class GoogleCalendarCreateCallback implements
            Callback<DateControl.CreateCalendarSourceParameter, CalendarSource>,
            Consumer<GoogleCalendarCreateView.CalendarViewBean> {

        private final Window owner;

        GoogleCalendarCreateCallback(Window owner) {
            this.owner = owner;
        }

        @Override
        public CalendarSource call(DateControl.CreateCalendarSourceParameter param) {
            if (SecurityService.getInstance().isLoggedIn()) {
                GoogleCalendarCreateView view = new GoogleCalendarCreateView(this);
                view.show(owner);
            }
            return null;
        }

        @Override
        public void accept(GoogleCalendarCreateView.CalendarViewBean bean) {
            GoogleAccount account = SecurityService.getInstance().getLoggedAccount();
            GoogleCalendar calendar = account.createCalendar(bean.getName(), bean.getStyle());
            GoogleTaskExecutor.getInstance().execute(new InsertCalendarTask(calendar, account));
        }
    }

    /**
     * Provider of the google entry pop over content.
     *
     * Created by gdiaz on 5/05/2017.
     */
    private static class GoogleEntryPopOverContentProvider implements Callback<DateControl.EntryDetailsPopOverContentParameter, Node> {
        @Override
        public Node call(DateControl.EntryDetailsPopOverContentParameter param) {
            PopOver popOver = param.getPopOver();
            GoogleEntry entry = (GoogleEntry) param.getEntry();

            InvalidationListener listener = obs -> {
                if (entry.isFullDay() && !popOver.isDetached()) {
                    popOver.setDetached(true);
                }
            };

            entry.fullDayProperty().addListener(listener);
            popOver.setOnHidden(evt -> entry.fullDayProperty().removeListener(listener));

            return new GoogleEntryPopOverContentPane(entry, param.getDateControl().getCalendars());
        }
    }

    /**
     * Factory for google entries.
     *
     * Created by gdiaz on 5/05/2017.
     */
    private static class GoogleEntryCreateCallback implements Callback<DateControl.CreateEntryParameter, Entry<?>> {

        @Override
        public Entry<?> call(DateControl.CreateEntryParameter param) {
            if (SecurityService.getInstance().isLoggedIn()) {
                GoogleAccount account = SecurityService.getInstance().getLoggedAccount();
                GoogleCalendar primaryCalendar = account.getPrimaryCalendar();

                if (primaryCalendar != null) {
                    DateControl control = param.getDateControl();
                    VirtualGrid grid = control.getVirtualGrid();
                    ZonedDateTime start = param.getZonedDateTime();
                    DayOfWeek firstDayOfWeek = control.getFirstDayOfWeek();
                    ZonedDateTime lowerTime = grid.adjustTime(start, false, firstDayOfWeek);
                    ZonedDateTime upperTime = grid.adjustTime(start, true, firstDayOfWeek);

                    if (Duration.between(start, lowerTime).abs().minus(Duration.between(start, upperTime).abs()).isNegative()) {
                        start = lowerTime;
                    }

                    GoogleEntry entry = primaryCalendar.createEntry(start, control instanceof AllDayView);
                    GoogleTaskExecutor.getInstance().execute(new InsertEntryTask(entry, primaryCalendar, account));
                }
            }

            return null;
        }
    }

}
