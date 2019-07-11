package com.girbola.imageviewer.imageviewer;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;

public class Dialogs {
	//	private Model_ImageViewer model_ImageViewer;

	//	public Dialogs(Model_ImageViewer model_ImageViewer) {
	//		this.model_ImageViewer = model_ImageViewer;
	//	}

	public static void showAlert(String message, AlertType alertType) {
		Alert alert = new Alert(alertType);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(ImageViewer.class.getResource("/com/girbola/imageviewer/themes/ImageViewer.css").toExternalForm());
		dialogPane.getStyleClass().add("alertDiag");
		dialogPane.setHeaderText(null);
		dialogPane.setContentText(message);
		alert.showAndWait();
	}

	public static void errorAlert(String message, Model_ImageViewer model_ImageViewer) {
		Alert alert = new Alert(AlertType.ERROR);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(ImageViewer.class.getResource("/com/girbola/imageviewer/themes/ImageViewer.css").toExternalForm());
		dialogPane.getStyleClass().add("alertDiag");
		dialogPane.setHeaderText(null);
		TextArea textArea = new TextArea(message);
		dialogPane.setContent(textArea);
		dialogPane.setContentText(model_ImageViewer.getI18nSupport().getBundle().getString("affectNextTime"));
		alert.showAndWait();
	}

	public static void sprintf(String string) {
		if (ImageViewer.DEBUG) {
			System.out.println(string);
		}
	}

	public static void printf(String string, Object object) {
		System.out.printf(string, object);
	}
}
