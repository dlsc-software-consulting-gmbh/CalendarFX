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
import com.google.ical.compat.jodatime.LocalDateIterator;
import com.google.ical.compat.jodatime.LocalDateIteratorFactory;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.RRule;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;
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

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

@SuppressWarnings("javadoc")
public class RecurrenceViewSkin extends SkinBase<RecurrenceView> {

    private ComboBox<Frequency> frequencyBox = new ComboBox<>();
    private Spinner<Integer> repeatCountSpinner = new Spinner<>();
    private Label repeatCountGranularity = new Label();
    private Label startOnDateLabel = new Label();
    private RadioButton endsNeverButton = new RadioButton(Messages.getString("RecurrenceViewSkin.NEVER")); //$NON-NLS-1$
    private RadioButton endsAfterButton = new RadioButton(Messages.getString("RecurrenceViewSkin.AFTER")); //$NON-NLS-1$
    private Spinner<Integer> endsAfterCounterSpinner = new Spinner<>();
    private Label endsAfterOccurencesLabel = new Label(Messages.getString("RecurrenceViewSkin.OCCURENCES")); //$NON-NLS-1$
    private RadioButton endsOnButton = new RadioButton(Messages.getString("RecurrenceViewSkin.ON")); //$NON-NLS-1$
    private DatePicker endsOnDatePicker = new DatePicker(LocalDate.now());
    private Label summaryLabel = new Label();
    private RadioButton repeatByDayOfTheMonth = new RadioButton(Messages.getString("RecurrenceViewSkin.DAY_OF_MONTH")); //$NON-NLS-1$
    private RadioButton repeatByDayOfTheWeek = new RadioButton(Messages.getString("RecurrenceViewSkin.DAY_OF_WEEK")); //$NON-NLS-1$
    private ToggleButton weekDayMondayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_MONDAY")); //$NON-NLS-1$
    private ToggleButton weekDayTuesdayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_TUESDAY")); //$NON-NLS-1$
    private ToggleButton weekDayWednesdayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_WEDNESDAY")); //$NON-NLS-1$
    private ToggleButton weekDayThursdayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_THURSDAY")); //$NON-NLS-1$
    private ToggleButton weekDayFridayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_FRIDAY")); //$NON-NLS-1$
    private ToggleButton weekDaySaturdayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_SATURDAY")); //$NON-NLS-1$
    private ToggleButton weekDaySundayButton = new ToggleButton(Messages.getString("RecurrenceViewSkin.SHORT_SUNDAY")); //$NON-NLS-1$
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
                        return Messages.getString("RecurrenceViewSkin.DAILY"); //$NON-NLS-1$
                    case MONTHLY:
                        return Messages.getString("RecurrenceViewSkin.MONTHLY"); //$NON-NLS-1$
                    case WEEKLY:
                        return Messages.getString("RecurrenceViewSkin.WEEKLY"); //$NON-NLS-1$
                    case YEARLY:
                        return Messages.getString("RecurrenceViewSkin.YEARLY"); //$NON-NLS-1$
                    default:
                        return ""; //$NON-NLS-1$
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
        repeatCountBox.getStyleClass().add("repeat-count-box"); //$NON-NLS-1$
        repeatCountBox.getChildren().setAll(repeatCountSpinner, repeatCountGranularity);

        /*
         * Weekday box.
         */
        weekdayBox = new HBox();
        weekdayBox.getStyleClass().add("weekday-box"); //$NON-NLS-1$
        weekdayBox.getChildren().setAll(weekDayMondayButton,
                weekDayTuesdayButton, weekDayWednesdayButton,
                weekDayThursdayButton, weekDayFridayButton,
                weekDaySaturdayButton, weekDaySundayButton);

        /*
         * Repeat by box.
         */
        repeatByBox = new HBox();
        repeatByBox.getStyleClass().add("repeat-by-box"); //$NON-NLS-1$
        repeatByBox.getChildren().setAll(repeatByDayOfTheMonth, repeatByDayOfTheWeek);

        /*
         * Ends after box.
         */
        endsAfterBox = new HBox();
        endsAfterBox.getStyleClass().add("ends-after-box"); //$NON-NLS-1$
        endsAfterBox.getChildren().setAll(endsAfterButton, endsAfterCounterSpinner, endsAfterOccurencesLabel);

        /*
         * Ends on box.
         */
        endsOnBox = new HBox();
        endsOnBox.getStyleClass().add("ends-on-box"); //$NON-NLS-1$
        endsOnBox.getChildren().setAll(endsOnButton, endsOnDatePicker);

        grid = new GridPane();
        grid.getStyleClass().add("container"); //$NON-NLS-1$

        /*
         * Columns
         */
        ColumnConstraints labelsColumn = new ColumnConstraints();
        ColumnConstraints fieldsColumn = new ColumnConstraints();

        labelsColumn.setHalignment(HPos.RIGHT);
        labelsColumn.setPrefWidth(USE_COMPUTED_SIZE);

        grid.getColumnConstraints().setAll(labelsColumn, fieldsColumn);

        frequencyLabel = new Label(Messages.getString("RecurrenceViewSkin.FREQUENCY")); //$NON-NLS-1$
        repeatCountLabel = new Label(Messages.getString("RecurrenceViewSkin.REPEAT_EVERY")); //$NON-NLS-1$
        labelRepeatOn = new Label(Messages.getString("RecurrenceViewSkin.REPEAT_ON")); //$NON-NLS-1$
        labelRepeatBy = new Label(Messages.getString("RecurrenceViewSkin.REPEAT_BY")); //$NON-NLS-1$
        startsOnLabel = new Label(Messages.getString("RecurrenceViewSkin.STARTS_ON")); //$NON-NLS-1$
        endsOnLabel = new Label(Messages.getString("RecurrenceViewSkin.ENDS")); //$NON-NLS-1$
        summaryLabel = new Label(Messages.getString("RecurrenceViewSkin.SUMMARY")); //$NON-NLS-1$

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
            RRule rrule = new RRule(rule);
            switch (rrule.getFreq()) {
                case DAILY:
                    frequencyBox.setValue(Frequency.DAILY);
                    repeatCountGranularity.setText(Messages.getString("RecurrenceViewSkin.DAYS")); //$NON-NLS-1$
                    break;
                case WEEKLY:
                    frequencyBox.setValue(Frequency.WEEKLY);
                    repeatCountGranularity.setText(Messages.getString("RecurrenceViewSkin.32")); //$NON-NLS-1$
                    break;
                case MONTHLY:
                    frequencyBox.setValue(Frequency.MONTHLY);
                    repeatCountGranularity.setText(Messages.getString("RecurrenceViewSkin.MONTHS")); //$NON-NLS-1$
                    break;
                case YEARLY:
                    frequencyBox.setValue(Frequency.YEARLY);
                    repeatCountGranularity.setText(Messages.getString("RecurrenceViewSkin.YEARS")); //$NON-NLS-1$
                    break;
                case SECONDLY:
                case HOURLY:
                case MINUTELY:
                    throw new IllegalArgumentException(
                            "unsupported frequency: " + rrule.getFreq()); //$NON-NLS-1$
                default:
                    throw new IllegalArgumentException(
                            "unknown frequency: " + rrule.getFreq()); //$NON-NLS-1$

            }

            DateValue until = rrule.getUntil();
            if (until != null) {
                endsOnButton.setSelected(true);
                endsOnDatePicker.setValue(
                        LocalDate.of(until.year(), until.month(), until.day()));
            } else if (rrule.getCount() > 0) {
                endsAfterButton.setSelected(true);
            } else {
                endsNeverButton.setSelected(true);
            }

            if (rrule.getByMonthDay().length > 0) {
                repeatByDayOfTheMonth.setSelected(true);
            } else {
                repeatByDayOfTheWeek.setSelected(true);
            }

            repeatCountSpinnerValueFactory.setValue(rrule.getInterval());
            endsAfterCounterSpinnerValueFactory.setValue(rrule.getCount());

            List<WeekdayNum> days = rrule.getByDay();

            weekDayMondayButton.setSelected(isSelected(Weekday.MO, days));
            weekDayTuesdayButton.setSelected(isSelected(Weekday.TU, days));
            weekDayWednesdayButton.setSelected(isSelected(Weekday.WE, days));
            weekDayThursdayButton.setSelected(isSelected(Weekday.TH, days));
            weekDayFridayButton.setSelected(isSelected(Weekday.FR, days));
            weekDaySaturdayButton.setSelected(isSelected(Weekday.SA, days));
            weekDaySundayButton.setSelected(isSelected(Weekday.SU, days));

            summary.setText(Util.convertRFC2445ToText(rule,
                    getSkinnable().getStartDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        startOnDateLabel
                .setText(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                        .format(getSkinnable().getStartDate()));
    }

    private boolean isSelected(Weekday day, List<WeekdayNum> days) {
        for (WeekdayNum num : days) {
            if (num.wday.equals(day)) {
                return true;
            }
        }

        return false;
    }

    private void updateRule() {
        RRule rule = new RRule();
        switch (frequencyBox.getValue()) {
            case DAILY:
                rule.setFreq(com.google.ical.values.Frequency.DAILY);
                break;
            case MONTHLY:
                rule.setFreq(com.google.ical.values.Frequency.MONTHLY);
                break;
            case WEEKLY:
                rule.setFreq(com.google.ical.values.Frequency.WEEKLY);
                break;
            case YEARLY:
                rule.setFreq(com.google.ical.values.Frequency.YEARLY);
                break;
            default:
                break;
        }

        int interval = repeatCountSpinner.getValue();
        if (interval > 1) {
            rule.setInterval(interval);
        } else {
            rule.setInterval(0);
        }

        if (endsOnButton.isSelected()) {
            LocalDate date = endsOnDatePicker.getValue();
            rule.setUntil(new DateValueImpl(date.getYear(),
                    date.getMonthValue(), date.getDayOfMonth()));
        }

        if (endsAfterButton.isSelected()) {
            rule.setCount(endsAfterCounterSpinner.getValue());
        }

        if (rule.getFreq().equals(com.google.ical.values.Frequency.MONTHLY)) {
            if (repeatByDayOfTheMonth.isSelected()) {
                rule.setByMonthDay(new int[]{
                        getSkinnable().getStartDate().getDayOfMonth()});
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

                List<WeekdayNum> weekdays = new ArrayList<>();
                switch (zonedDateTime.getDayOfWeek()) {
                    case FRIDAY:
                        weekdays.add(new WeekdayNum(hits, Weekday.FR));
                        break;
                    case MONDAY:
                        weekdays.add(new WeekdayNum(hits, Weekday.MO));
                        break;
                    case SATURDAY:
                        weekdays.add(new WeekdayNum(hits, Weekday.SA));
                        break;
                    case SUNDAY:
                        weekdays.add(new WeekdayNum(hits, Weekday.SU));
                        break;
                    case THURSDAY:
                        weekdays.add(new WeekdayNum(hits, Weekday.TH));
                        break;
                    case TUESDAY:
                        weekdays.add(new WeekdayNum(hits, Weekday.TU));
                        break;
                    case WEDNESDAY:
                        weekdays.add(new WeekdayNum(hits, Weekday.WE));
                        break;
                    default:
                        break;
                }

                rule.setByDay(weekdays);
            }
        }

        if (rule.getFreq().equals(com.google.ical.values.Frequency.WEEKLY)) {

            /*
             * Weekdays MO, TU, .... SU
             */
            List<WeekdayNum> weekdays = new ArrayList<>();

            maybeAddWeekday(weekdays, new WeekdayNum(0, Weekday.MO),
                    weekDayMondayButton);
            maybeAddWeekday(weekdays, new WeekdayNum(0, Weekday.TU),
                    weekDayTuesdayButton);
            maybeAddWeekday(weekdays, new WeekdayNum(0, Weekday.WE),
                    weekDayWednesdayButton);
            maybeAddWeekday(weekdays, new WeekdayNum(0, Weekday.TH),
                    weekDayThursdayButton);
            maybeAddWeekday(weekdays, new WeekdayNum(0, Weekday.FR),
                    weekDayFridayButton);
            maybeAddWeekday(weekdays, new WeekdayNum(0, Weekday.SA),
                    weekDaySaturdayButton);
            maybeAddWeekday(weekdays, new WeekdayNum(0, Weekday.SU),
                    weekDaySundayButton);

            rule.setByDay(weekdays);
        }

        getSkinnable().setRecurrenceRule(rule.toIcal());

        if (LoggingDomain.RECURRENCE.isLoggable(Level.FINE)) {
            LoggingDomain.RECURRENCE.fine(
                    "test dumping 10 recurrences starting with today's date"); //$NON-NLS-1$

            try {
                LocalDateIterator iterator = LocalDateIteratorFactory
                        .createLocalDateIterator(rule.toIcal(),
                                new org.joda.time.LocalDate(2015, 8, 18), true);

                int counter = 0;
                while (iterator.hasNext()) {
                    org.joda.time.LocalDate repeatingDate = iterator.next();
                    LoggingDomain.RECURRENCE.fine(repeatingDate.toString());
                    counter++;
                    if (counter == 10) {
                        break;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void maybeAddWeekday(List<WeekdayNum> weekdays,
                                 WeekdayNum weekdayNum, ToggleButton button) {
        if (button.isSelected()) {
            weekdays.add(weekdayNum);
        }
    }
}
