package com.girbola.imageviewer.imageviewer;

import com.sun.jna.NativeLibrary;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;
import uk.co.caprica.vlcj.support.Info;

/**
 * ImageView is a simple program for viewing images.
 * 
 * External libraries used in this program:
 *
 * Metadata-extractor 2.11.0 https://github.com/drewnoakes/metadata-extractor
 * 
 * JCodec 0.2.3 https://github.com/jcodec/jcodec
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

	public static final boolean DEBUG = true;
	private Model_ImageViewer model_ImageViewer;

	@Override
	public void start(Stage stage) throws Exception {

		model_ImageViewer = new Model_ImageViewer();

		Parent root = null;
		FXMLLoader loader = null;
		ImageViewerController imageViewerController = null;

		try {
			loader = new FXMLLoader(ImageViewer.class.getResource("ImageViewer.fxml"));
			loader.setResources(model_ImageViewer.getI18nSupport().getBundle());
			root = loader.load();
			imageViewerController = (ImageViewerController) loader.getController();

		} catch (Exception e) {
			e.printStackTrace();
		}

		stage.setTitle("ImageViewer");
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				model_ImageViewer.closeProgram();
			}

		});
		imageViewerController.init(model_ImageViewer);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(ImageViewer.class.getResource("/com/girbola/imageviewer/themes/ImageViewer.css").toExternalForm());
		stage.setScene(scene);
		stage.showingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					if (model_ImageViewer.getConfiguration().isVLCSupported()) {
						Dialogs.sprintf("Initalizing VLC");
						model_ImageViewer.initVlc();
					}
				}
			}
		});
		stage.show();

	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
