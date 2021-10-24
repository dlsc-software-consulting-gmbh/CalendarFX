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

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.Messages;
import com.calendarfx.view.RequestEvent;
import com.calendarfx.view.SearchResultView;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@SuppressWarnings("javadoc")
public class SearchResultViewSkin extends SkinBase<SearchResultView> {

    private final ListView<Entry<?>> listView;

    public SearchResultViewSkin(SearchResultView view) {
        super(view);

        Label placeholderLabel = new Label();
        placeholderLabel.getStyleClass().add("placeholder-label");

        listView = new ListView<>();
        listView.setItems(view.getSearchResults());
        listView.setCellFactory(new SearchResultCellFactory());
        listView.setPlaceholder(placeholderLabel);
        listView.getSelectionModel().selectedItemProperty()
                .addListener(it -> view.getProperties().put(
                        "selected.search.result",
                        listView.getSelectionModel().getSelectedItem()));
        getChildren().add(listView);
    }

    public static class SearchResultCellFactory implements Callback<ListView<Entry<?>>, ListCell<Entry<?>>> {
        @Override
        public ListCell<Entry<?>> call(ListView<Entry<?>> param) {
            return new SearchResultListViewCell();
        }
    }

    public static class SearchResultListViewCell extends ListCell<Entry<?>> {

        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        private final Circle colorCircle;
        private final Label titleLabel;
        private final Label dateLabel;
        private final Label timeLabel;
        private final BorderPane borderPane;

        public SearchResultListViewCell() {
            setPrefWidth(0);

            getStyleClass().add("search-result-cell");

            colorCircle = new Circle();
            colorCircle.setRadius(3.5);

            titleLabel = new Label();
            titleLabel.setMinWidth(0);
            titleLabel.setGraphic(colorCircle);
            titleLabel.getStyleClass().add("title-label");

            dateLabel = new Label();
            dateLabel.setMinWidth(0);
            dateLabel.getStyleClass().add("date-label");

            timeLabel = new Label();
            timeLabel.setMinWidth(0);
            timeLabel.getStyleClass().add("time-label");

            BorderPane dateTimePane = new BorderPane();
            dateTimePane.getStyleClass().add("date-time-pane");
            dateTimePane.setLeft(dateLabel);
            dateTimePane.setRight(timeLabel);

            borderPane = new BorderPane();
            borderPane.getStyleClass().add("container");
            borderPane.setTop(titleLabel);
            borderPane.setBottom(dateTimePane);

            setGraphic(borderPane);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2) {
                    Entry<?> entry = getItem();
                    if (entry != null) {
                        fireEvent(new RequestEvent(this, this, entry));
                    }
                }
            });
        }

        @Override
        protected void updateItem(Entry<?> entry, boolean empty) {
            super.updateItem(entry, empty);
            if (entry != null) {
                Calendar calendar = entry.getCalendar();

                borderPane.setVisible(true);
                colorCircle.getStyleClass().add(calendar.getStyle() + "-icon");

                titleLabel.setText(entry.getTitle());
                titleLabel.setVisible(true);
                timeLabel.setText(getTimeText(entry));
                dateLabel.setText(dateFormatter.format(entry.getStartDate()));
            } else {
                borderPane.setVisible(false);
            }
        }

        private String getTimeText(Entry<?> entry) {
            if (entry.isFullDay()) {
                return "all-day";
            }

            LocalDate startDate = entry.getStartDate();
            LocalDate endDate = entry.getEndDate();

            String text;
            if (startDate.equals(endDate)) {
                text = MessageFormat.format(Messages.getString("SearchResultViewSkin.FROM_UNTIL"),
                        timeFormatter.format(entry.getStartTime()),
                        timeFormatter.format(entry.getEndTime()));
            } else {
                text = MessageFormat.format(Messages.getString("SearchResultViewSkin.FROM_UNTIL_WITH_DATE"),
                        timeFormatter.format(entry.getStartTime()),
                        dateFormatter.format(entry.getStartDate()),
                        timeFormatter.format(entry.getEndTime()),
                        dateFormatter.format(entry.getEndDate()));
            }

            return text;
        }
    }
}
