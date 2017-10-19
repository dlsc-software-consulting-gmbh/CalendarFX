package com.calendarfx.google.view.thread;

import com.calendarfx.google.view.log.LogItem;
import com.calendarfx.google.view.task.GoogleTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import impl.com.calendarfx.view.util.Util;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executor class that allows to have some background tasks executed.
 *
 * Created by gdiaz on 22/02/2017.
 */
public final class GoogleTaskExecutor {

	private static GoogleTaskExecutor instance;

	public static GoogleTaskExecutor getInstance () {
		if (instance == null) {
			instance = new GoogleTaskExecutor();
		}
		return instance;
	}

	private final IntegerProperty pendingTasks = new SimpleIntegerProperty(this, "pendingTasks", 0);

	private final ExecutorService executor = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder()
			.setDaemon(true)
			.setNameFormat("Google-Calendar-Executor-%d")
			.setPriority(Thread.MIN_PRIORITY)
			.build());

	private GoogleTaskExecutor() {
		super();
		progress.bind(Bindings.divide(
				Bindings.when(pendingTasks.isEqualTo(0)).then(0).otherwise(1),
				Bindings.when(pendingTasks.isEqualTo(0)).then(Double.MAX_VALUE).otherwise(pendingTasks))
		);
	}

	private final ObservableList<LogItem> log = FXCollections.observableArrayList();

	public ObservableList<LogItem> getLog() {
		return log;
	}

	public void clearLog () {
		log.clear();
	}

	private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", 0);

	public final ReadOnlyDoubleProperty progressProperty () {
		return progress.getReadOnlyProperty();
	}

	public final void execute (GoogleTask<?> task) {
		updatePendingTasks(task);
		executor.submit(task);
	}

	public final void executeImmediate (GoogleTask<?> task) {
		updatePendingTasks(task);
		task.run();
	}

	private void updatePendingTasks (GoogleTask<?> task) {
		Util.runInFXThread(() -> {
			pendingTasks.set(pendingTasks.get() + 1);
			EventHandler<WorkerStateEvent> handler = evt -> pendingTasks.set(pendingTasks.get() - 1);
			task.setOnFailed(handler);
			task.setOnSucceeded(handler);
			log.add(0, task.getLogItem());
		});
	}

}
