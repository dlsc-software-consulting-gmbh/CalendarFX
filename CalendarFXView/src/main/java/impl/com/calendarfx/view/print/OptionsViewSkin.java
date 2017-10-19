/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.print;

import com.calendarfx.view.Messages;
import com.calendarfx.view.print.OptionsView;
import com.calendarfx.view.print.ViewType;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class OptionsViewSkin extends SkinBase<OptionsView> {

    private final VBox container;

    private final CheckBox allDayEventsChk;
    private final CheckBox detailsChk;
    private final CheckBox timedEventsChk;
    private final CheckBox miniCalendarChk;
    private final CheckBox calendarKeysChk;
    private final CheckBox swimlaneLayoutChk;

    public OptionsViewSkin(OptionsView control) {
        super(control);

        control.viewTypeProperty().addListener(obs -> layout());

        container = new VBox(5);

        allDayEventsChk = new CheckBox(Messages.getString("OptionsViewSkin.ALL_DAY_EVENTS_LABEL"));
        allDayEventsChk.selectedProperty().bindBidirectional(control.showAllDayEntriesProperty());

        detailsChk = new CheckBox(Messages.getString("OptionsViewSkin.DETAILS_LABEL"));
        detailsChk.selectedProperty().bindBidirectional(control.showEntryDetailsProperty());

        timedEventsChk = new CheckBox(Messages.getString("OptionsViewSkin.TIMED_EVENTS_LABEL"));
        timedEventsChk.selectedProperty().bindBidirectional(control.showTimedEntriesProperty());

        miniCalendarChk = new CheckBox(Messages.getString("OptionsViewSkin.MINI_CALENDAR_LABEL"));
        miniCalendarChk.selectedProperty().bindBidirectional(control.showMiniCalendarsProperty());

        calendarKeysChk = new CheckBox(Messages.getString("OptionsViewSkin.CALENDAR_KEYS_LABEL"));
        calendarKeysChk.selectedProperty().bindBidirectional(control.showCalendarKeysProperty());

        swimlaneLayoutChk = new CheckBox(Messages.getString("OptionsViewSkin.SWIMLANE_LAYOUT_LABEL"));
        swimlaneLayoutChk.selectedProperty().bindBidirectional(control.showSwimlaneLayoutProperty());

        layout();
        getChildren().add(container);
    }

    private void layout() {
        List<Node> children = new ArrayList<>();

        if (getSkinnable().getViewType() == ViewType.DAY_VIEW) {
            children.add(allDayEventsChk);
            children.add(detailsChk);
            children.add(miniCalendarChk);
            children.add(calendarKeysChk);
            children.add(swimlaneLayoutChk);
        } else if (getSkinnable().getViewType() == ViewType.WEEK_VIEW) {
            children.add(allDayEventsChk);
            children.add(miniCalendarChk);
            children.add(calendarKeysChk);
            children.add(swimlaneLayoutChk);
        } else if (getSkinnable().getViewType() == ViewType.MONTH_VIEW) {
            children.add(allDayEventsChk);
            children.add(timedEventsChk);
            children.add(miniCalendarChk);
            children.add(calendarKeysChk);
        }

        container.getChildren().setAll(children);
    }

}
