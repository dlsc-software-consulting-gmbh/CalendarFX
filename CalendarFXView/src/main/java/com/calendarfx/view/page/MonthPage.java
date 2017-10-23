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

package com.calendarfx.view.page;

import com.calendarfx.view.Messages;
import com.calendarfx.view.MonthView;
import com.calendarfx.view.print.ViewType;
import impl.com.calendarfx.view.page.MonthPageSkin;
import javafx.scene.control.Skin;

import java.time.format.DateTimeFormatter;

/**
 * A composite view focused on displaying calendar information for a single
 * month. The view consists of the page "chrome" inherited from the superclass
 * and a {@link MonthView}.
 * <p/>
 * <center><img width="100%" src="doc-files/month-page.png"></center>
 */
public class MonthPage extends PageBase {

    private MonthView monthView;

    /**
     * Constructs a new month page.
     */
    public MonthPage() {
        super();

        getStyleClass().add("month-page"); //$NON-NLS-1$

        this.monthView = new MonthView();

        setDateTimeFormatter(DateTimeFormatter.ofPattern(Messages.getString("MonthPage.DATE_FORMAT"))); //$NON-NLS-1$
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MonthPageSkin(this);
    }

    /**
     * Returns the week view child control. Most of the visualization in this
     * page is done by this view. The page only adds its chrome.
     *
     * @return the week view
     */
    public final MonthView getMonthView() {
        return monthView;
    }

    @Override
    public final void goForward() {
        setDate(getDate().plusMonths(1).withDayOfMonth(1));
    }

    @Override
    public final void goBack() {
        setDate(getDate().minusMonths(1).withDayOfMonth(1));
    }

    @Override
    public final ViewType getPrintViewType() {
        return ViewType.MONTH_VIEW;
    }

}
