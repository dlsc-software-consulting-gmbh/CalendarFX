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

package impl.com.calendarfx.view.print;

import com.calendarfx.view.Messages;
import com.calendarfx.view.print.PaperView;
import com.calendarfx.view.print.ViewType;

import impl.com.calendarfx.view.NumericTextField;
import javafx.print.Paper;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class PaperViewSkin extends SkinBase<PaperView> {

    private final GridPane gridPane;

    private GridPane marginsGridPane;

    public PaperViewSkin(PaperView control) {
        super(control);

        ComboBox<ViewType> viewTypeComboBox = new ComboBox<>();
        viewTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        viewTypeComboBox.getItems().setAll(ViewType.values());
        viewTypeComboBox.valueProperty().bindBidirectional(control.viewTypeProperty());
        viewTypeComboBox.setConverter(new StringConverter<ViewType>() {
            @Override
            public String toString(ViewType object) {
                return Messages.getString(object.getMessageKey());
            }

            @Override
            public ViewType fromString(String string) {
                if (string != null) {
                    for (ViewType type : ViewType.values()) {
                        if (string.equals(Messages.getString(type.getMessageKey()))) {
                            return type;
                        }
                    }
                }
                return null;
            }
        });

        ComboBox<Paper> paperComboBox = new ComboBox<>();
        paperComboBox.setMaxWidth(Double.MAX_VALUE);
        paperComboBox.setItems(control.getAvailablePapers());
        paperComboBox.valueProperty().bindBidirectional(control.paperProperty());
        paperComboBox.setConverter(new StringConverter<Paper>() {
            @Override
            public String toString(Paper object) {
                return object.getName();
            }

            @Override
            public Paper fromString(String string) {
                if (string != null) {
                    for (Paper paper : control.getAvailablePapers()) {
                        if (string.equals(paper.getName())) {
                            return paper;
                        }
                    }
                }
                return null;
            }
        });

        ComboBox<PaperView.MarginType> marginTypeComboBox = new ComboBox<>();
        marginTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        marginTypeComboBox.getItems().setAll(PaperView.MarginType.values());
        marginTypeComboBox.valueProperty().bindBidirectional(control.marginTypeProperty());
        marginTypeComboBox.setConverter(new StringConverter<PaperView.MarginType>() {
            @Override
            public String toString(PaperView.MarginType type) {
                switch (type) {
                    case CUSTOM:
                        return Messages.getString("Margin.CUSTOM");
                    case DEFAULT:
                        return Messages.getString("Margin.DEFAULT");
                    case MINIMUM:
                        return Messages.getString("Margin.MINIMUM");
                    default:
                        return "Unknown margin type";
                }
            }

            @Override
            public PaperView.MarginType fromString(String string) {
                /*
                 * No need to implement this as the user can not "type" the value.
                 */
                return null;
            }
        });

        gridPane = new GridPane();
        gridPane.getStyleClass().add("container");
        gridPane.add(new Label(Messages.getString("PaperViewSkin.VIEW_TYPE_LABEL")), 0, 0);
        gridPane.add(viewTypeComboBox, 1, 0);
        gridPane.add(new Label(Messages.getString("PaperViewSkin.PAPER_LABEL")), 0, 1);
        gridPane.add(paperComboBox, 1, 1);
        
        if (control.isShowMargin()){
            gridPane.add(new Label(Messages.getString("PaperViewSkin.MARGIN_LABEL")), 0, 2);
            gridPane.add(marginTypeComboBox, 1, 2);
        }

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        gridPane.getColumnConstraints().addAll(col1, col2);

        GridPane.setFillWidth(paperComboBox, true);
        GridPane.setFillWidth(viewTypeComboBox, true);

        getChildren().add(gridPane);

        control.marginTypeProperty().addListener(it -> updateVisibility());
        updateVisibility();
    }

    private void updateVisibility() {
        if (getSkinnable().getMarginType().equals(PaperView.MarginType.CUSTOM)) {
            if (marginsGridPane == null) {
                /*
                 * lazy initialization of the margin fields.
                 */
                createMarginFields();
            }
            gridPane.add(marginsGridPane, 1, 3);
        } else {
            if (marginsGridPane != null) {
                gridPane.getChildren().remove(marginsGridPane);
            }
        }
    }

    private void createMarginFields() {
        NumericTextField topField = new NumericTextField();
        NumericTextField rightField = new NumericTextField();
        NumericTextField bottomField = new NumericTextField();
        NumericTextField leftField = new NumericTextField();

        StringConverter<Number> converter = new NumberStringConverter();

        leftField.textProperty().bindBidirectional(getSkinnable().leftMarginProperty(), converter);
        rightField.textProperty().bindBidirectional(getSkinnable().rightMarginProperty(), converter);
        topField.textProperty().bindBidirectional(getSkinnable().topMarginProperty(), converter);
        bottomField.textProperty().bindBidirectional(getSkinnable().bottomMarginProperty(), converter);

        marginsGridPane = new GridPane();
        marginsGridPane.getStyleClass().add("custom-fields");
        marginsGridPane.add(new Label(Messages.getString("MarginSelector.TOP")), 0, 0);
        marginsGridPane.add(topField, 1, 0);
        marginsGridPane.add(new Label(Messages.getString("MarginSelector.RIGHT")), 2, 0);
        marginsGridPane.add(rightField, 3, 0);
        marginsGridPane.add(new Label(Messages.getString("MarginSelector.BOTTOM")), 0, 1);
        marginsGridPane.add(bottomField, 1, 1);
        marginsGridPane.add(new Label(Messages.getString("MarginSelector.LEFT")), 2, 1);
        marginsGridPane.add(leftField, 3, 1);
    }
}
