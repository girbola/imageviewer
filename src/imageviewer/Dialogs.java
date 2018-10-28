package imageviewer;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Alert.AlertType;

public class Dialogs {
	private Model_ImageViewer model_ImageViewer;

	public Dialogs(Model_ImageViewer model_ImageViewer) {
		this.model_ImageViewer = model_ImageViewer;
	}

	public void showAlert() {
		Alert alert = new Alert(AlertType.INFORMATION);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(ImageViewer.class.getResource("/themes/ImageViewer.css").toExternalForm());
		dialogPane.getStyleClass().add("alertDiag");
		dialogPane.setHeaderText(null);
		dialogPane.setContentText(model_ImageViewer.getI18nSupport().getBundle().getString("affectNextTime"));
		alert.showAndWait();
	}
}
