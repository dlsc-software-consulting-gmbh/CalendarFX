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

import com.calendarfx.view.DayEntryView;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * A specialized scrollpane used for automatic scrolling when the user performs
 * a drag operation close to the edges of the pane.
 */
public class AutoScrollPane extends ScrollPane {

    final double proximity = 20;

    /**
     * Constructs a new scrollpane.
     */
    public AutoScrollPane() {
        this(null);
    }

    /**
     * Constructs a new scrollpane for the given content node.
     *
     * @param content the content node
     */
    public AutoScrollPane(Node content) {
        super(content);

        // regular drag, e.g. of an entry view
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::autoscrollIfNeeded);
        addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> stopAutoScrollIfNeeded());

        // drag and drop from the outside
        addEventFilter(MouseEvent.DRAG_DETECTED, this::startDrag);
        addEventFilter(DragEvent.DRAG_OVER, this::autoscrollIfNeeded);
        addEventFilter(DragEvent.DRAG_EXITED, evt -> stopAutoScrollIfNeeded());
        addEventFilter(DragEvent.DRAG_DROPPED, evt -> stopAutoScrollIfNeeded());
        addEventFilter(DragEvent.DRAG_DONE, evt -> stopAutoScrollIfNeeded());
    }

    private void startDrag(MouseEvent evt) {
        EventTarget target = evt.getTarget();
        if (isScrollBar(target) || !isOnEntry(target)) {
            return;
        }
        Dragboard db = startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();

        /*
         * We have to add some content, otherwise drag over will not be called.
         */
        content.putString("dummy");
        db.setContent(content);
    }

    private boolean isOnEntry(EventTarget target) {
        if (target == null || !(target instanceof Node)) {
            return false;
        }

        Node node = (Node) target;
        if (node instanceof DayEntryView) {
            return true;
        }

        return isOnEntry(node.getParent());
    }

    private boolean isScrollBar(EventTarget target) {
        if (target instanceof Node) {
            return isScrollBar((Node) target);
        }

        return false;
    }

    private boolean isScrollBar(Node node) {
        boolean result = false;
        if (node instanceof ScrollBar) {
            result = true;
        } else if (node.getParent() != null) {
            return isScrollBar(node.getParent());
        }

        return result;
    }

    private void autoscrollIfNeeded(DragEvent evt) {
        evt.acceptTransferModes(TransferMode.ANY);

        if (getBoundsInLocal().getWidth() < 1) {
            if (getBoundsInLocal().getWidth() < 1) {
                stopAutoScrollIfNeeded();
                return;
            }
        }

        double yOffset = 0;

        // y offset

        double delta = evt.getSceneY() - localToScene(0, 0).getY();
        if (delta < proximity) {
            yOffset = -(proximity - delta);
        }

        delta = localToScene(0, 0).getY() + getHeight() - evt.getSceneY();
        if (delta < proximity) {
            yOffset = proximity - delta;
        }

        if (yOffset != 0) {
            autoscroll(yOffset);
        } else {
            stopAutoScrollIfNeeded();
        }
    }

    private void autoscrollIfNeeded(MouseEvent evt) {
        if (getBoundsInLocal().getWidth() < 1) {
            if (getBoundsInLocal().getWidth() < 1) {
                stopAutoScrollIfNeeded();
                return;
            }
        }

        double yOffset = 0;

        // y offset

        double delta = evt.getSceneY() - localToScene(0, 0).getY();
        if (delta < 0) {
            yOffset = Math.max(delta / 2, -10);
        }

        delta = localToScene(0, 0).getY() + getHeight() - evt.getSceneY();
        if (delta < 0) {
            yOffset = Math.min(-delta / 2, 10);
        }

        if (yOffset != 0) {
            autoscroll(yOffset);
        } else {
            stopAutoScrollIfNeeded();
        }
    }

    class ScrollThread extends Thread {
        private boolean running = true;
        private double yOffset;

        public ScrollThread() {
            super("Autoscrolling List View");
            setDaemon(true);
        }

        @Override
        public void run() {

            /*
             * Some initial delay, especially useful when dragging something in
             * from the outside.
             */

            try {
                Thread.sleep(300);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            while (running) {

                Platform.runLater(this::scrollY);

                try {
                    sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void scrollY() {
            double percent = yOffset / getHeight();
            setVvalue(getVvalue() + percent);
        }

        public void stopRunning() {
            this.running = false;
        }

        public void setDelta(double yOffset) {
            this.yOffset = yOffset;
        }
    }

    private ScrollThread scrollThread;

    private void autoscroll(double yOffset) {
        if (scrollThread == null) {
            scrollThread = new ScrollThread();
            scrollThread.start();
        }

        scrollThread.setDelta(yOffset);
    }

    private void stopAutoScrollIfNeeded() {
        if (scrollThread != null) {
            scrollThread.stopRunning();
            scrollThread = null;
        }
    }
}