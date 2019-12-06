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

package com.calendarfx.google.service;

import com.calendarfx.google.model.GoogleAccount;
import javafx.scene.control.Alert;

import java.io.IOException;

/**
 * Class that holds the info about the user logged in.
 *
 * Created by gdiaz on 5/05/2017.
 */
public class SecurityService {

    /**
     * Default user account id, used internally by the application for authentication purposes.
     */
    private static final String DEFAULT_ACCOUNT_ID = "Google-Calendar";

    private static SecurityService instance;

    public static SecurityService getInstance() {
        if (instance == null) {
            instance = new SecurityService();
        }
        return instance;
    }

    private GoogleAccount account;

    public GoogleAccount getLoggedAccount() {
        return account;
    }

    public boolean isLoggedIn() {
        return account != null;
    }

    public boolean isAuthorized() {
        return GoogleConnector.getInstance().isAuthorized(SecurityService.DEFAULT_ACCOUNT_ID);
    }

    public boolean authorize(String authorizationCode) {
        try {
            GoogleConnector.getInstance().authorize(SecurityService.DEFAULT_ACCOUNT_ID, authorizationCode);
            return true;
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Unexpected error while authenticating into Google.");
            alert.setContentText(e.getLocalizedMessage());
            alert.show();
            return false;
        }
    }

    public GoogleAccount login() {
        if (isLoggedIn()) {
            logout();
        }

        try {
            account = GoogleConnector.getInstance().getAccountInfo(SecurityService.DEFAULT_ACCOUNT_ID);
            return account;
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Unexpected error while login into Google.");
            alert.setContentText(e.getLocalizedMessage());
            alert.show();
            return null;
        }
    }

    public void logout() {
        account = null;
        try {
            GoogleConnector.getInstance().removeCredential(SecurityService.DEFAULT_ACCOUNT_ID);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Unexpected error removing credentials");
            alert.setContentText(e.getLocalizedMessage());
            alert.show();
        }
    }

}
