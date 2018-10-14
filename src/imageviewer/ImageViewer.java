package imageviewer;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * ImageView is a simple program for viewing images.
 * 
 * External libraries used in this program:
 *
 * Metadata-extractor 2.11.0
 * https://github.com/drewnoakes/metadata-extractor
 * 
 * JCodec 0.2.3
 * https://github.com/jcodec/jcodec
 * 
 * 
 * Supported file formats for creating thumbnails are: Image = BMP, GIF, JPG,
 * JPEG, PNG, TIF, TIFF Raw = CR2, NEF Video = 3GP, AVI, MKV, MOV, MP4, MPG
 * 
 * Video player has been exported from this project.
 *
 * Short instructions ================== -Select folders on the left side
 * -Double-click opens images preview in new window.
 * 
 * 
 * @(#)Author: Marko Lokka
 * 
 */

public class ImageViewer extends Application {

	private ResourceBundle bundle;
	private Locale locale;

	final public static String country = "EN";
	final public static String lang = "en";

	@Override
	public void start(Stage stage) throws Exception {
		locale = new Locale(lang, country);
		bundle = ResourceBundle.getBundle("bundle/lang", locale);

		FXMLLoader loader = new FXMLLoader(ImageViewer.class.getResource("ImageViewer.fxml"), bundle);
		Parent root = loader.load();
		ImageViewerController imageViewerController = (ImageViewerController) loader.getController();

		stage.setWidth(763);
		stage.setMinWidth(763);
		stage.setMaxWidth(763);
		stage.setMinHeight(685);
		stage.setMaxHeight(685);
		stage.setHeight(685);
		stage.initStyle(StageStyle.UTILITY);
		stage.setResizable(false);
		stage.setTitle("ImageViewer");
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				imageViewerController.getModel_ImageViewer().getRenderVisibleNode().terminateAllBackgroundTasks();
			Platform.exit();
			}
		});

		Scene scene = new Scene(root);
		scene.getStylesheets().add(ImageViewer.class.getResource("/themes/ImageViewer.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
		imageViewerController.init();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
