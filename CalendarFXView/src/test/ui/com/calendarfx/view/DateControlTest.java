/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of CalendarFX.
 */

package com.calendarfx.view;

import com.calendarfx.model.Entry;
import com.calendarfx.util.CalendarFX;
import javafx.scene.Parent;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateControlTest extends GuiTest {

    private DayView dayView;

    @Override
    protected Parent getRootNode() {
        CalendarFX.setLicenseKey(
                "LIC=DLSCDemos;VEN=DLSC;VER=1;PRO=STANDARD;RUN=no;CTR=1;SignCode=3F;Signature=302D0214047E13E511BF6104D2C543D02EA7EE789BF681CC02150084738BC8911A25ECBD081EA0ABD4E24D12BD5171");
        return dayView = new DayView();
    }

    @Test
    public void shouldSelect() {

        // given
        Entry<String> source = new Entry<>();

        String recurrenceId = ZonedDateTime.now().toString();

        Entry<String> recurrence1 = new Entry<>();
        recurrence1.setId(source.getId());
        recurrence1.getProperties().put("com.calendarfx.recurrence.id",
                recurrenceId);
        recurrence1.getProperties().put("com.calendarfx.recurrence.source",
                source);

        Entry<String> recurrence2 = new Entry<>();
        recurrence2.setId(source.getId());
        recurrence2.getProperties().put("com.calendarfx.recurrence.id",
                recurrenceId);
        recurrence2.getProperties().put("com.calendarfx.recurrence.source",
                source);

        // when
        dayView.select(recurrence1);

        // then
        assertThat(dayView.getSelections().contains(recurrence2), is(true));
    }
}
