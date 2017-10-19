/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view.page;

import com.calendarfx.view.ButtonBar;
import com.calendarfx.view.Messages;
import com.calendarfx.view.page.PageBase;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;

public abstract class PageBaseSkin<C extends PageBase> extends SkinBase<C> {

	private Text dateText;
	private BorderPane headerPane;
	private BorderPane borderPane;

	public PageBaseSkin(C page) {
		super(page);

		// Navigation

		Button backButton = new Button("<");
		Button forwardButton = new Button(">");
		Button todayButton = new Button(Messages.getString("PageBaseSkin.TODAY"));

		backButton.getStyleClass().add("previous-date-button");
		forwardButton.getStyleClass().add("next-date-button");

		backButton.setOnAction(evt -> page.goBack());
		forwardButton.setOnAction(evt -> page.goForward());
		todayButton.setOnAction(evt -> page.goToday());

		ButtonBar navigationButton = new ButtonBar(backButton, todayButton, forwardButton);
		navigationButton.getStyleClass().add("navigation-button-bar"); //$NON-NLS-1$
		navigationButton.visibleProperty().bind(page.showNavigationProperty());

		// Date label
		this.dateText = new Text("Date"); //$NON-NLS-1$
		this.dateText.getStyleClass().add("date-text"); //$NON-NLS-1$
		this.dateText.visibleProperty().bind(page.showDateProperty());
		page.dateProperty().addListener(evt -> updateDateText());

		BorderPane.setMargin(navigationButton, new Insets(10));
		BorderPane.setMargin(dateText, new Insets(10));

		headerPane = new BorderPane();
		headerPane.getStyleClass().add("header");
		headerPane.setLeft(navigationButton);
		headerPane.setRight(dateText);

		Node content = createContent();
		content.getStyleClass().add("content"); //$NON-NLS-1$
		content.sceneProperty().addListener(it -> {
			if (content.getScene() != null) {
				content.applyCss();
			}
		});

		borderPane = new BorderPane();
		borderPane.getStyleClass().add("container");
		borderPane.setCenter(content);

		getChildren().add(borderPane);

		updateDateText();
		updateHeaderVisibility();

		page.showDateProperty().addListener(it -> updateHeaderVisibility());
		page.showNavigationProperty().addListener(it -> updateHeaderVisibility());
	}

	private void updateHeaderVisibility() {
		if (getSkinnable().isShowDateHeader() || getSkinnable().isShowNavigation()) {
			borderPane.setTop(headerPane);
		} else {
			borderPane.setTop(null);
		}
	}

	private void updateDateText() {
		DateTimeFormatter formatter = getSkinnable().getDateTimeFormatter();
		String text = formatter.format(getSkinnable().getDate());
		dateText.setText(text);
	}

	protected abstract Node createContent();
}
