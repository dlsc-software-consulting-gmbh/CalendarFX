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

package com.calendarfx.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DateSelectionModel {

    private LocalDate lastSelected;
    private DateSelector selector;
    private final ObservableList<LocalDate> selectedDates = FXCollections.observableArrayList();
    private final ObservableList<LocalDate> unmodifiable = FXCollections.unmodifiableObservableList(selectedDates);
    private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(this, "selectionMode");

    public DateSelectionModel() {
        super();
        setSelectionMode(SelectionMode.MULTIPLE_DATES);
    }

    public final void select(LocalDate date) {
        selector.select(date);
    }

    public final void selectRange(LocalDate start, LocalDate end) {
        selector.selectRange(start, end);
    }

    public final void selectUntil(LocalDate date) {
        selector.selectUntil(date);
    }

    public final void deselect(LocalDate date) {
        selector.deselect(date);
    }

    public final void clearAndSelect(LocalDate date) {
        selector.clearAndSelect(date);
    }

    public final void clear() {
        selector.clear();
    }

    public final boolean isSelected(LocalDate date) {
        return selectedDates.contains(date);
    }

    public final boolean isEmpty() {
        return selectedDates.isEmpty();
    }

    public final ObservableList<LocalDate> getSelectedDates() {
        return unmodifiable;
    }

    public final LocalDate getLastSelected() {
        return lastSelected;
    }

    public final ObjectProperty<SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    public final SelectionMode getSelectionMode() {
        return selectionModeProperty().get();
    }

    public final void setSelectionMode(SelectionMode selectionMode) {
        SelectionMode oldMode = getSelectionMode();
        selectionModeProperty().set(Objects.requireNonNull(selectionMode));
        if (oldMode != selectionMode) {
            selector = DateSelector.createInstance(this);
        }
    }

    public enum SelectionMode {

        SINGLE_DATE,

        SINGLE_DATE_RANGE,

        MULTIPLE_DATES

    }

    private static abstract class DateSelector {

        protected final DateSelectionModel model;

        public DateSelector(DateSelectionModel model) {
            this.model = model;
        }

        public abstract void select(LocalDate date);

        public abstract void selectRange(LocalDate start, LocalDate end);

        public abstract void selectUntil(LocalDate date);

        public void clear() {
            model.selectedDates.clear();
            model.lastSelected = null;
        }

        public void clearAndSelect(LocalDate date) {
            if (date != null) {
                model.selectedDates.setAll(date);
                model.lastSelected = date;
            } else {
                clear();
            }
        }

        public void deselect(LocalDate date) {
            model.selectedDates.remove(date);
            if (model.lastSelected != null && model.lastSelected.equals(date)) {
                model.lastSelected = null;
            }
        }

        public static DateSelector createInstance(DateSelectionModel model) {
            switch (model.getSelectionMode()) {
                case SINGLE_DATE:
                    return new SingleDateSelector(model);

                case SINGLE_DATE_RANGE:
                    return new SingleDateRangeSelector(model);

                case MULTIPLE_DATES:
                    return new MultipleDatesSelector(model);

                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    private static class SingleDateSelector extends DateSelector {

        public SingleDateSelector(DateSelectionModel model) {
            super(model);
            if (model.selectedDates.size() > 1) {
                if (model.lastSelected != null) {
                    select(model.lastSelected);
                } else {
                    select(model.selectedDates.get(0));
                }
            }
        }

        @Override
        public void select(LocalDate date) {
            if (date != null) {
                model.selectedDates.setAll(date);
                model.lastSelected = date;
            } else {
                model.clear();
            }
        }

        @Override
        public void selectRange(LocalDate start, LocalDate end) {
            if (start == null && end == null) {
                model.clear();
            } else if (end != null) {
                select(end);
            } else {
                select(start);
            }
        }

        @Override
        public void selectUntil(LocalDate date) {
            select(date);
        }

    }

    private static final class SingleDateRangeSelector extends DateSelector {

        private LocalDate rangeStart;
        private LocalDate rangeEnd;

        public SingleDateRangeSelector(DateSelectionModel model) {
            super(model);

            List<LocalDate> selection = new ArrayList<>();
            selection.addAll(model.selectedDates);

            if (!selection.isEmpty()) {
                Collections.sort(selection);

                boolean valid = true;
                for (int i = 0; i < selection.size() - 1; i++) {
                    if (i == selection.size() - 1) {
                        break;
                    }

                    LocalDate pivot = selection.get(i);
                    LocalDate next = selection.get(i + 1);

                    if (!next.equals(pivot.plusDays(1))) {
                        valid = false;
                        break;
                    }
                }

                if (!valid) {
                    clear();
                } else {
                    rangeStart = selection.get(0);
                    rangeEnd = selection.get(selection.size() - 1);
                    model.lastSelected = rangeEnd;
                }
            }
        }

        @Override
        public void select(LocalDate date) {
            if (date != null) {
                if (rangeStart == null) {
                    model.selectedDates.setAll(date);
                    model.lastSelected = date;
                    rangeStart = date;
                    rangeEnd = date;
                } else {
                    if (date.equals(rangeStart.plusDays(-1)) || date.equals(rangeEnd.plusDays(1))) {
                        model.selectedDates.add(date);
                        model.lastSelected = date;

                        if (date.isAfter(rangeEnd)) {
                            rangeEnd = date;
                        }

                        if (date.isBefore(rangeStart)) {
                            rangeStart = date;
                        }
                    } else {
                        rangeStart = null;
                        rangeEnd = null;
                        select(date);
                    }
                }
            }
        }

        @Override
        public void selectRange(LocalDate start, LocalDate end) {
            if (start != null && end != null && !start.isAfter(end)) {
                if (model.isEmpty() || isIntersecting(start, end)) {
                    List<LocalDate> toSelect = new ArrayList<>();
                    while (start.isBefore(end) || start.isEqual(end)) {
                        if (!model.selectedDates.contains(start)) {
                            toSelect.add(start);
                        }
                        start = start.plusDays(1);
                    }

                    if (!toSelect.isEmpty()) {
                        LocalDate first = toSelect.get(0);
                        LocalDate last = toSelect.get(toSelect.size() - 1);
                        model.selectedDates.addAll(toSelect);
                        model.lastSelected = last;

                        if (rangeStart == null || rangeStart.isAfter(first)) {
                            rangeStart = first;
                        }

                        if (rangeEnd == null || rangeEnd.isBefore(last)) {
                            rangeEnd = last;
                        }
                    }
                } else if (start.equals(rangeEnd.plusDays(1)) || end.equals(rangeStart.plusDays(-1))) {
                    List<LocalDate> toSelect = new ArrayList<>();
                    while (start.isBefore(end) || start.isEqual(end)) {
                        toSelect.add(start);
                        start = start.plusDays(1);
                    }

                    LocalDate first = toSelect.get(0);
                    LocalDate last = toSelect.get(toSelect.size() - 1);
                    model.selectedDates.addAll(toSelect);
                    model.lastSelected = last;

                    if (rangeStart == null || rangeStart.isAfter(first)) {
                        rangeStart = first;
                    }

                    if (rangeEnd == null || rangeEnd.isBefore(last)) {
                        rangeEnd = last;
                    }
                } else {
                    List<LocalDate> toSelect = new ArrayList<>();
                    while (start.isBefore(end) || start.isEqual(end)) {
                        toSelect.add(start);
                        start = start.plusDays(1);
                    }

                    LocalDate first = toSelect.get(0);
                    LocalDate last = toSelect.get(toSelect.size() - 1);
                    model.selectedDates.setAll(toSelect);
                    model.lastSelected = last;

                    if (rangeStart == null || rangeStart.isAfter(first)) {
                        rangeStart = first;
                    }

                    if (rangeEnd == null || rangeEnd.isBefore(last)) {
                        rangeEnd = last;
                    }
                }
            }
        }

        @Override
        public void selectUntil(LocalDate date) {
            if (date != null && model.lastSelected != null) {
                LocalDate start = model.lastSelected.isBefore(date) ? model.lastSelected : date;
                LocalDate end = model.lastSelected.isBefore(date) ? date : model.lastSelected;
                selectRange(start, end);
                model.lastSelected = date;
            }
        }

        @Override
        public void clear() {
            super.clear();
            rangeStart = null;
            rangeEnd = null;
        }

        private boolean isIntersecting(LocalDate start, LocalDate end) {
            if ((start.isBefore(rangeStart) || start.isEqual(rangeStart)) && (end.isAfter(rangeEnd) || end.isEqual(rangeEnd))) {
                return true;
            }

            if ((start.isEqual(rangeStart) || start.isAfter(rangeStart)) && (start.isBefore(rangeEnd) || start.isEqual(rangeEnd))) {
                return true;
            }

            return (end.isEqual(rangeStart) || end.isAfter(rangeStart)) && (end.isBefore(rangeEnd) || end.isEqual(rangeEnd));

        }

        @Override
        public void clearAndSelect(LocalDate date) {
            super.clearAndSelect(date);
            rangeStart = date;
            rangeEnd = date;
        }

    }

    private static final class MultipleDatesSelector extends DateSelector {

        public MultipleDatesSelector(DateSelectionModel model) {
            super(model);
        }

        @Override
        public void select(LocalDate date) {
            if (date != null) {
                if (!model.selectedDates.contains(date)) {
                    model.selectedDates.add(date);
                }
                model.lastSelected = date;
            }
        }

        @Override
        public void selectRange(LocalDate start, LocalDate end) {
            if (start == null || end == null || start.isAfter(end)) {
                return;
            }

            List<LocalDate> toSelect = new ArrayList<>();
            while (start.isBefore(end) || start.equals(end)) {
                if (!model.selectedDates.contains(start)) {
                    toSelect.add(start);
                }
                start = start.plusDays(1);
            }

            model.selectedDates.addAll(toSelect);
            model.lastSelected = toSelect.get(toSelect.size() - 1);
        }

        @Override
        public void selectUntil(LocalDate date) {
            if (date != null && model.lastSelected != null) {
                LocalDate start = model.lastSelected.isBefore(date) ? model.lastSelected : date;
                LocalDate end = model.lastSelected.isBefore(date) ? date : model.lastSelected;
                selectRange(start, end);
            }
        }

    }

}
