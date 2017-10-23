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
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.code.geocoder.Geocoder;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Utility class used to connect to google services.
 *
 * @author Gabriel Diaz, 16.12.2014
 */
public final class GoogleConnector {

    /** Application name for identification purposes. */
    private static final String APPLICATION_NAME = "flexcalendarfxapp";

    /** Directory used to store the user credentials. */
    private static final File CREDENTIALS_DIRECTORY = new File(System.getProperty("user.home"), ".store/flexcalendarfx");

    /** Default scopes used by the Google calendar application. */
    private static final List<String> SCOPES = Lists.newArrayList(CalendarScopes.CALENDAR, Oauth2Scopes.USERINFO_PROFILE);

    /** The instance of this singleton class. */
    private static GoogleConnector instance;

    /** JSON implementation. */
    private final JsonFactory jsonFactory;

    /** HTTP transport used for transferring data. */
    private final HttpTransport httpTransport;

    /** Secrets of the FX calendar application. */
    private final GoogleClientSecrets secrets;

    /**  Factory to create the data store object. */
    private DataStoreFactory dataStoreFactory;

    /** Flow to get the access token. */
    private AuthorizationCodeFlow flow;

    /** Service that allows to manipulate Google Calendars. */
    private GoogleCalendarService calendarService;

    /** Service that allows to use Google Geolocalization. */
    private GoogleGeocoderService geoService;

    /**
     * Instances the google connector configuring all required resources.
     */
    private GoogleConnector() throws IOException, GeneralSecurityException {
        super();
        // 1. JSON library
        jsonFactory = JacksonFactory.getDefaultInstance();

        // 2. Configure HTTP transport
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 3. Load the credentials
        secrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(GoogleConnector.class.getResourceAsStream("client-secrets.json")));

        // 4. Configure the authentication flow
        dataStoreFactory = new FileDataStoreFactory(CREDENTIALS_DIRECTORY);

        // 5. Create flow
        imp_buildAuthorizationFlow();
    }

    /**
     * On demand instance creator method used to get the single instance of this
     * google authenticator class.
     *
     * @return The single instance of this class, if the instance does not exist,
     *         this is immediately created.
     */
    public static GoogleConnector getInstance() {
        if (instance == null) {
            try {
                instance = new GoogleConnector();
            } catch (Exception e) {
                throw new RuntimeException("The GoogleConnector could not be instanced!", e);
            }
        }
        return instance;
    }

    /**
     * Starts the authorization process but using the provided authorization
     * code, so this will not attempt to get it from the user.
     *
     * @param accountId The account to be authorized.
     * @param authorizationCode The code used as key of authorization.
     * @throws IOException If storing the code fails.
     */
    synchronized void authorize(String accountId, String authorizationCode) throws IOException {
        impl_storeCredential(accountId, authorizationCode);
    }

    /**
     * Deletes the stored credentials for the given account id. This means the
     * next time the user must authorize the app to access his calendars.
     *
     * @param accountId
     *            The identifier of the account.
     */
    synchronized void removeCredential(String accountId) throws IOException {
        DataStore<StoredCredential> sc = StoredCredential.getDefaultDataStore(dataStoreFactory);
        sc.delete(accountId);
        calendarService = null;
        geoService = null;
    }

    /**
     * Checks if the given account id has already been authorized and the
     * user granted access to his calendars info.
     *
     * @param accountId
     *            The identifier of the account used internally by the application.
     * @return {@code true} if the account has already been set up, otherwise
     *         {@code false}.
     */
    boolean isAuthorized(String accountId) {
        try {
            DataStore<StoredCredential> sc = StoredCredential.getDefaultDataStore(dataStoreFactory);
            return sc.containsKey(accountId);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Generates a valid URL to let the user authorize access his Google
     * calendar information.
     *
     * @return The valid web URL.
     */
    public String getAuthorizationURL() {
        return flow.newAuthorizationUrl().setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).build();
    }

    /**
     * Gets the service to access geo localization methods; using this service does not require authentication.
     *
     * @return The service created, this is on demand created.
     */
    public synchronized GoogleGeocoderService getGeocoderService() {
        if (geoService == null) {
            geoService = new GoogleGeocoderService(new Geocoder());
        }
        return geoService;
    }

    /**
     * Instances a new calendar service for the given google account user name.
     * This requires previous authorization to get the service, so if the user
     * has not granted access to his data, this method will start the
     * authorization process automatically; this attempts to open the login google page in the
     * default browser.
     *
     * @param accountId
     *            The google account.
     * @return The calendar service, this can be null if the account cannot be
     *         authenticated.
     * @throws IOException If the account has not been authenticated.
     */
    public synchronized GoogleCalendarService getCalendarService(String accountId) throws IOException {
        if (calendarService == null) {
            Credential credential = impl_getStoredCredential(accountId);
            if (credential == null) {
                throw new UnsupportedOperationException("The account has not been authorized yet!");
            }
            calendarService = new GoogleCalendarService(impl_createService(credential));
        }
        return calendarService;
    }

    /**
     * Requests the user info for the given account. This requires previous
     * authorization from the user, so this might start the process.
     *
     * @param accountId
     *            The id of the account to get the user info.
     * @return The user info bean.
     * @throws IOException If the account cannot be accessed.
     */
    GoogleAccount getAccountInfo(String accountId) throws IOException {
        Credential credential = impl_getStoredCredential(accountId);
        if (credential == null) {
            throw new UnsupportedOperationException("The account has not been authorized yet!");
        }
        Userinfoplus info = impl_requestUserInfo(credential);
        GoogleAccount account = new GoogleAccount();
        account.setId(accountId);
        account.setName(info.getName());
        return account;
    }

    // ::::::::::::::::::    PRIVATE STUFF    :::::::::::::::::::::

    private Calendar impl_createService(Credential credentials) {
        return new Calendar.Builder(httpTransport, jsonFactory, credentials).setApplicationName(APPLICATION_NAME).build();
    }

    private Credential impl_getStoredCredential(String accountId) throws IOException {
        return flow.loadCredential(accountId);
    }

    private void impl_storeCredential(String accountId, String authorizationCode) throws IOException {
        TokenResponse response = flow.newTokenRequest(authorizationCode).setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).execute();
        flow.createAndStoreCredential(response, accountId);
    }

    private Userinfoplus impl_requestUserInfo(Credential credentials) throws IOException {
        Oauth2 userInfoService = new Oauth2.Builder(httpTransport, jsonFactory, credentials).setApplicationName(APPLICATION_NAME).build();
        return userInfoService.userinfo().get().execute();
    }

    private void imp_buildAuthorizationFlow() throws IOException {
        flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, secrets, SCOPES).setDataStoreFactory(dataStoreFactory).build();
    }

}
