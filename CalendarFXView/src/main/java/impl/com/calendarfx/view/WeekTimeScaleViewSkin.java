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

import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.WeekTimeScaleView;

import java.time.LocalDate;

@SuppressWarnings("javadoc")
public class WeekTimeScaleViewSkin extends TimeScaleViewSkin<WeekTimeScaleView> {

    public WeekTimeScaleViewSkin(WeekTimeScaleView scale) {
        super(scale);
    }

    @Override
    protected boolean isShowingTimeMarker() {
        WeekTimeScaleView dayView = getSkinnable();
        DetailedWeekView weekView = dayView.getDetailedWeekView();

        if (weekView != null) {
            LocalDate today = getSkinnable().getToday();

            LocalDate weekStart = weekView.getStartDate();
            LocalDate weekEnd = weekView.getEndDate();

            return !(weekStart.isAfter(today) || weekEnd.isBefore(today));

        }

        return false;
    }
}
