package com.girbola.imageviewer.imageviewer;

import com.sun.jna.Native;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

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
			e.getMessage();
			System.out.println("TRACE START");
			e.printStackTrace();
		}

		stage.setWidth(763);
		stage.setMinWidth(763);
		stage.setMinHeight(685);
		stage.setMaxHeight(685);
		stage.setHeight(685);
		stage.setResizable(false);
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
		stage.show();
		Object loadLibrary = null;

		try {
			loadLibrary = Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
			model_ImageViewer.getConfiguration().setVLCSupported(true);
			System.out.println("vlc supported!!");
		} catch (UnsatisfiedLinkError ex) {
			System.out.println("VLC won't work! " + ex);
			//			setVlcSupport(false);
			model_ImageViewer.getConfiguration().setVLCSupported(false);
		}

	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
