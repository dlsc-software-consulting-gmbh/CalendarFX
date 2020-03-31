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

import com.calendarfx.util.LoggingDomain;
import com.calendarfx.util.Util;
import com.calendarfx.view.Messages;
import com.calendarfx.view.RecurrenceView;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.WeekDay.Day;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.logging.Level;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

@SuppressWarnings("javadoc")
public class RecurrenceViewSkin extends SkinBase<RecurrenceView> {

    private ComboBox<Frequency> frequencyBox = new ComboBox<>();
    private Spinner<Integer> repeatCountSpinner = new Spinner<>();
    private Label repeatCountGranularity = new Label();
    private Label startOnDateLabel = new Label();
    private RadioButton endsNeverButton = new RadioButton(Messages.getString("RecurrenceViewSkin.NEVER"));
    private RadioButton endsAfterButton = new RadioButton(Messages.getString("RecurrenceViewSkin.AFTER"));
    private Spinner<Integer> endsAfterCounterSpinner = new Spinner<>();
    private Label endsAfterOccurencesLabel = new Label(Messages.getString("RecurrenceViewSkin.OCCURENCES"));
    private RadioButton endsOnButton = new RadioButton(Messages.getString("RecurrenceViewSkin.ON"));
    private DatePicker endsOnDatePicker = new DatePicker(LocalDate.now());
    private Label summaryLabel = new Label();
    private RadioButton repeatByDayOfTheMonth = new RadioButton(Messages.getString("RecurrenceViewSkin.DAY_OF_MONTH"));
    private RadioButton repeatByDayOfTheWeek = new RadioButton(Messages.getString("RecurrenceViewSkin.DAY_OF_WEEK"));
    private ToggleButton weekDayMondayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_MONDAY"));
    private ToggleButton weekDayTuesdayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_TUESDAY"));
    private ToggleButton weekDayWednesdayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_WEDNESDAY"));
    private ToggleButton weekDayThursdayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_THURSDAY"));
    private ToggleButton weekDayFridayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_FRIDAY"));
    private ToggleButton weekDaySaturdayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_SATURDAY"));
    private ToggleButton weekDaySundayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_SUNDAY"));
    private HBox weekdayBox;
    private HBox repeatByBox;
    private HBox repeatCountBox;
    private Label labelRepeatOn;
    private Label summary = new Label();
    private Label labelRepeatBy;
    private Label frequencyLabel;
    private Label repeatCountLabel;
    private Label startsOnLabel;
    private Label endsOnLabel;
    private HBox endsAfterBox;
    private HBox endsOnBox;
    private GridPane grid;
    private IntegerSpinnerValueFactory repeatCountSpinnerValueFactory;
    private IntegerSpinnerValueFactory endsAfterCounterSpinnerValueFactory;

    enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY,
    }

    public RecurrenceViewSkin(RecurrenceView view) {
        super(view);

        /*
         * Inits
         */

        summary.setMaxWidth(250);
        summary.setWrapText(true);

        frequencyBox.getItems().setAll(Frequency.values());
        frequencyBox.setValue(Frequency.DAILY);
        frequencyBox.setConverter(new StringConverter<Frequency>() {

            @Override
            public String toString(Frequency frequency) {
                switch (frequency) {
                    case DAILY:
                        return Messages.getString("RecurrenceViewSkin.DAILY");
                    case MONTHLY:
                        return Messages.getString("RecurrenceViewSkin.MONTHLY");
                    case WEEKLY:
                        return Messages.getString("RecurrenceViewSkin.WEEKLY");
                    case YEARLY:
                        return Messages.getString("RecurrenceViewSkin.YEARLY");
                    default:
                        return "";
                }
            }

            @Override
            public Frequency fromString(String txt) {
                return null;
            }
        });

        repeatCountSpinnerValueFactory = new IntegerSpinnerValueFactory(1, 100);

        repeatCountSpinner.setValueFactory(repeatCountSpinnerValueFactory);
        repeatCountSpinner.setEditable(true);
        repeatCountSpinner.getEditor().setPrefColumnCount(5);

        endsAfterCounterSpinnerValueFactory = new IntegerSpinnerValueFactory(1, 1000);
        endsAfterCounterSpinner.setValueFactory(endsAfterCounterSpinnerValueFactory);
        endsAfterCounterSpinner.setEditable(true);
        endsAfterCounterSpinner.getEditor().setPrefColumnCount(5);

        endsAfterCounterSpinner.disableProperty().bind(Bindings.not(endsAfterButton.selectedProperty()));
        endsAfterOccurencesLabel.disableProperty().bind(Bindings.not(endsAfterButton.selectedProperty()));
        endsOnDatePicker.disableProperty().bind(Bindings.not(endsOnButton.selectedProperty()));

        endsOnDatePicker.getEditor().setPrefColumnCount(14);

        /*
         * Button groups.
         */
        ToggleGroup endsByGroup = new ToggleGroup();
        endsByGroup.getToggles().setAll(endsAfterButton, endsNeverButton, endsOnButton);

        ToggleGroup repeatByGroup = new ToggleGroup();
        repeatByGroup.getToggles().setAll(repeatByDayOfTheMonth, repeatByDayOfTheWeek);

        ToggleGroup endsGroup = new ToggleGroup();
        endsGroup.getToggles().setAll(endsOnButton, endsAfterButton, endsNeverButton);

        /*
         * Repeat count box.
         */
        repeatCountBox = new HBox();
        repeatCountBox.getStyleClass().add("repeat-count-box");
        repeatCountBox.getChildren().setAll(repeatCountSpinner, repeatCountGranularity);

        /*
         * Weekday box.
         */
        weekdayBox = new HBox();
        weekdayBox.getStyleClass().add("weekday-box");
        weekdayBox.getChildren().setAll(weekDayMondayButton,
                weekDayTuesdayButton, weekDayWednesdayButton,
                weekDayThursdayButton, weekDayFridayButton,
                weekDaySaturdayButton, weekDaySundayButton);

        /*
         * Repeat by box.
         */
        repeatByBox = new HBox();
        repeatByBox.getStyleClass().add("repeat-by-box");
        repeatByBox.getChildren().setAll(repeatByDayOfTheMonth, repeatByDayOfTheWeek);

        /*
         * Ends after box.
         */
        endsAfterBox = new HBox();
        endsAfterBox.getStyleClass().add("ends-after-box");
        endsAfterBox.getChildren().setAll(endsAfterButton, endsAfterCounterSpinner, endsAfterOccurencesLabel);

        /*
         * Ends on box.
         */
        endsOnBox = new HBox();
        endsOnBox.getStyleClass().add("ends-on-box");
        endsOnBox.getChildren().setAll(endsOnButton, endsOnDatePicker);

        grid = new GridPane();
        grid.getStyleClass().add("container");

        /*
         * Columns
         */
        ColumnConstraints labelsColumn = new ColumnConstraints();
        ColumnConstraints fieldsColumn = new ColumnConstraints();

        labelsColumn.setHalignment(HPos.RIGHT);
        labelsColumn.setPrefWidth(USE_COMPUTED_SIZE);

        grid.getColumnConstraints().setAll(labelsColumn, fieldsColumn);

        frequencyLabel = new Label(Messages.getString("RecurrenceViewSkin.FREQUENCY"));
        repeatCountLabel = new Label(Messages.getString("RecurrenceViewSkin.REPEAT_EVERY"));
        labelRepeatOn = new Label(Messages.getString("RecurrenceViewSkin.REPEAT_ON"));
        labelRepeatBy = new Label(Messages.getString("RecurrenceViewSkin.REPEAT_BY"));
        startsOnLabel = new Label(Messages.getString("RecurrenceViewSkin.STARTS_ON"));
        endsOnLabel = new Label(Messages.getString("RecurrenceViewSkin.ENDS"));
        summaryLabel = new Label(Messages.getString("RecurrenceViewSkin.SUMMARY"));

        grid.add(frequencyLabel, 0, 0);
        grid.add(frequencyBox, 1, 0);
        grid.add(repeatCountLabel, 0, 1);
        grid.add(repeatCountBox, 1, 1);

        buildGrid();

        frequencyBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(frequencyBox, true);

        getChildren().add(grid);

        InvalidationListener updateListener = it -> updateRule();

        frequencyBox.valueProperty().addListener(updateListener);
        frequencyBox.valueProperty().addListener(it -> buildGrid());
        repeatCountSpinner.valueProperty().addListener(updateListener);

        weekDayMondayButton.selectedProperty().addListener(updateListener);
        weekDayTuesdayButton.selectedProperty().addListener(updateListener);
        weekDayWednesdayButton.selectedProperty().addListener(updateListener);
        weekDayThursdayButton.selectedProperty().addListener(updateListener);
        weekDayFridayButton.selectedProperty().addListener(updateListener);
        weekDaySaturdayButton.selectedProperty().addListener(updateListener);
        weekDaySundayButton.selectedProperty().addListener(updateListener);
        repeatByDayOfTheMonth.selectedProperty().addListener(updateListener);
        repeatByDayOfTheWeek.selectedProperty().addListener(updateListener);
        endsNeverButton.selectedProperty().addListener(updateListener);
        endsAfterButton.selectedProperty().addListener(updateListener);
        endsAfterCounterSpinner.valueProperty().addListener(updateListener);
        endsOnButton.selectedProperty().addListener(updateListener);
        endsOnDatePicker.valueProperty().addListener(updateListener);

        getSkinnable().showSummaryProperty().addListener(it -> buildGrid());

        view.recurrenceRuleProperty().addListener(it -> updateView());

        updateView();
    }

    private void buildGrid() {
        grid.getChildren().removeIf(node -> !(node == frequencyLabel || node == frequencyBox
                || node == repeatCountLabel || node == repeatCountBox));

        int row = 2;

        if (frequencyBox.getValue().equals(Frequency.WEEKLY)) {
            grid.add(labelRepeatOn, 0, row);
            grid.add(weekdayBox, 1, row);
            row++;
        }

        if (frequencyBox.getValue().equals(Frequency.MONTHLY)) {
            grid.add(labelRepeatBy, 0, row);
            grid.add(repeatByBox, 1, row);
            row++;
        }

        grid.add(startsOnLabel, 0, row);
        grid.add(startOnDateLabel, 1, row);
        row++;

        grid.add(endsOnLabel, 0, row);
        grid.add(endsNeverButton, 1, row);
        row++;

        grid.add(endsAfterBox, 1, row);
        row++;

        grid.add(endsOnBox, 1, row);
        row++;

        if (getSkinnable().isShowSummary()) {
            grid.add(summaryLabel, 0, row);
            grid.add(summary, 1, row);
        }
    }

    private void updateView() {
        try {
            String rule = getSkinnable().getRecurrenceRule();
            if (rule == null) {
                return;
            }
            Recur<LocalDate> rrule = new Recur<>(rule.replaceFirst("^RRULE:", ""));
            switch (rrule.getFrequency()) {
                case DAILY:
                    frequencyBox.setValue(Frequency.DAILY);
                    repeatCountGranularity.setText(Messages.getString("RecurrenceViewSkin.DAYS"));
                    break;
                case WEEKLY:
                    frequencyBox.setValue(Frequency.WEEKLY);
                    repeatCountGranularity.setText(Messages.getString("RecurrenceViewSkin.32"));
                    break;
                case MONTHLY:
                    frequencyBox.setValue(Frequency.MONTHLY);
                    repeatCountGranularity.setText(Messages.getString("RecurrenceViewSkin.MONTHS"));
                    break;
                case YEARLY:
                    frequencyBox.setValue(Frequency.YEARLY);
                    repeatCountGranularity.setText(Messages.getString("RecurrenceViewSkin.YEARS"));
                    break;
                case SECONDLY:
                case HOURLY:
                case MINUTELY:
                    throw new IllegalArgumentException(
                            "unsupported frequency: " + rrule.getFrequency());
                default:
                    throw new IllegalArgumentException(
                            "unknown frequency: " + rrule.getFrequency());

            }

            LocalDate until = rrule.getUntil();
            if (until != null) {
                endsOnButton.setSelected(true);
                endsOnDatePicker.setValue(until);
            } else if (rrule.getCount() > 0) {
                endsAfterButton.setSelected(true);
            } else {
                endsNeverButton.setSelected(true);
            }

            if (!rrule.getMonthDayList().isEmpty()) {
                repeatByDayOfTheMonth.setSelected(true);
            } else {
                repeatByDayOfTheWeek.setSelected(true);
            }

            repeatCountSpinnerValueFactory.setValue(rrule.getInterval());
            endsAfterCounterSpinnerValueFactory.setValue(rrule.getCount());

            WeekDayList days = rrule.getDayList();

            weekDayMondayButton.setSelected(isSelected(Day.MO, days));
            weekDayTuesdayButton.setSelected(isSelected(Day.TU, days));
            weekDayWednesdayButton.setSelected(isSelected(Day.WE, days));
            weekDayThursdayButton.setSelected(isSelected(Day.TH, days));
            weekDayFridayButton.setSelected(isSelected(Day.FR, days));
            weekDaySaturdayButton.setSelected(isSelected(Day.SA, days));
            weekDaySundayButton.setSelected(isSelected(Day.SU, days));

            summary.setText(Util.convertRFC2445ToText(rule,
                    getSkinnable().getStartDate()));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            e.printStackTrace();
        }

        startOnDateLabel
                .setText(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                        .format(getSkinnable().getStartDate()));
    }

    private boolean isSelected(WeekDay.Day day, WeekDayList days) {
        for (WeekDay num : days) {
            if (num.getDay().equals(day)) {
                return true;
            }
        }

        return false;
    }

    private void updateRule() {
        Recur.Builder<LocalDate> rBuilder = new Recur.Builder<>();
        switch (frequencyBox.getValue()) {
            case DAILY:
                rBuilder.frequency(net.fortuna.ical4j.model.Recur.Frequency.DAILY);
                break;
            case MONTHLY:
                rBuilder.frequency(net.fortuna.ical4j.model.Recur.Frequency.MONTHLY);
                break;
            case WEEKLY:
                rBuilder.frequency(net.fortuna.ical4j.model.Recur.Frequency.WEEKLY);
                break;
            case YEARLY:
                rBuilder.frequency(net.fortuna.ical4j.model.Recur.Frequency.YEARLY);
                break;
            default:
                break;
        }

        int interval = repeatCountSpinner.getValue();
        if (interval > 1) {
            rBuilder.interval(interval);
        } else {
            rBuilder.interval(0);
        }

        if (endsOnButton.isSelected()) {
            LocalDate date = endsOnDatePicker.getValue();
            rBuilder.until(date);
        }

        if (endsAfterButton.isSelected()) {
            rBuilder.count(endsAfterCounterSpinner.getValue());
        }

        if (frequencyBox.getValue() == Frequency.MONTHLY) {
            if (repeatByDayOfTheMonth.isSelected()) {
                int value = getSkinnable().getStartDate().getDayOfMonth();
                rBuilder.monthList(new NumberList(value, value, false));
            } else {
                LocalDate localDate = getSkinnable().getStartDate();

                // TODO: use zone id of context (entry, calendar)
                ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate,
                        LocalTime.now(), ZoneId.systemDefault());
                int hits = 1;
                ZonedDateTime current = zonedDateTime.withDayOfMonth(1);
                do {
                    if (current.getDayOfWeek()
                            .equals(zonedDateTime.getDayOfWeek())) {
                        hits++;
                    }
                    current = current.plusDays(1);
                } while (current.toLocalDate().isBefore(localDate));

                WeekDayList weekdays = new WeekDayList();
                switch (zonedDateTime.getDayOfWeek()) {
                    case FRIDAY:
                        weekdays.add(new WeekDay(WeekDay.FR, hits));
                        break;
                    case MONDAY:
                        weekdays.add(new WeekDay(WeekDay.MO, hits));
                        break;
                    case SATURDAY:
                        weekdays.add(new WeekDay(WeekDay.SA, hits));
                        break;
                    case SUNDAY:
                        weekdays.add(new WeekDay(WeekDay.SU, hits));
                        break;
                    case THURSDAY:
                        weekdays.add(new WeekDay(WeekDay.TH, hits));
                        break;
                    case TUESDAY:
                        weekdays.add(new WeekDay(WeekDay.TU, hits));
                        break;
                    case WEDNESDAY:
                        weekdays.add(new WeekDay(WeekDay.WE, hits));
                        break;
                    default:
                        break;
                }

                rBuilder.dayList(weekdays);
            }
        }

        if (frequencyBox.getValue() == Frequency.WEEKLY) {

            /*
             * Weekdays MO, TU, .... SU
             */
            WeekDayList weekdays = new WeekDayList();

            maybeAddWeekday(weekdays, WeekDay.MO,
                    weekDayMondayButton);
            maybeAddWeekday(weekdays, WeekDay.TU,
                    weekDayTuesdayButton);
            maybeAddWeekday(weekdays, WeekDay.WE,
                    weekDayWednesdayButton);
            maybeAddWeekday(weekdays, WeekDay.TH,
                    weekDayThursdayButton);
            maybeAddWeekday(weekdays, WeekDay.FR,
                    weekDayFridayButton);
            maybeAddWeekday(weekdays, WeekDay.SA,
                    weekDaySaturdayButton);
            maybeAddWeekday(weekdays, WeekDay.SU,
                    weekDaySundayButton);

            rBuilder.dayList(weekdays);
        }

        Recur<LocalDate> rule = rBuilder.build();
        getSkinnable().setRecurrenceRule(rule.toString());

        if (LoggingDomain.RECURRENCE.isLoggable(Level.FINE)) {
            LoggingDomain.RECURRENCE.fine(
                    "test dumping 10 recurrences starting with today's date");

            LocalDate today = LocalDate.of(2015, 8, 18);
            List<LocalDate> dates = rule.getDates(today, today, LocalDate.MAX, 10);

            for (LocalDate repeatingDate : dates) {
                LoggingDomain.RECURRENCE.fine(repeatingDate.toString());
            }
        }
    }

    private void maybeAddWeekday(WeekDayList weekdays,
                                 WeekDay weekdayNum, ToggleButton button) {
        if (button.isSelected()) {
            weekdays.add(weekdayNum);
        }
    }
}
