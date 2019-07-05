package com.girbola.imageviewer.imageviewer;

import javafx.application.Application;
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

//		stage.setWidth(763);
//		stage.setMinWidth(763);
//		stage.setMinHeight(685);
//		stage.setMaxHeight(685);
//		stage.setHeight(685);
//		stage.setResizable(false);
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
		initVlc();

		//		Object loadLibrary = null;
		//
		//		try {
		//			loadLibrary = Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		//			model_ImageViewer.getConfiguration().setVLCSupported(true);
		//			System.out.println("vlc supported!!");
		//		} catch (UnsatisfiedLinkError ex) {
		//			System.out.println("VLC won't work! " + ex);
		//			//			setVlcSupport(false);
		//			model_ImageViewer.getConfiguration().setVLCSupported(false);
		//		

	}

	public boolean discovery() {
		NativeDiscovery dis = new NativeDiscovery() {

			@Override
			protected void onFound(String path, NativeDiscoveryStrategy strategy) {
				System.out.println("Found; " + " path: " + path + " strategy: " + strategy);
			}

			@Override
			protected void onNotFound() {
				System.out.println("Native not found");
			}

		};
		boolean found = new NativeDiscovery().discover();
		System.out.println("found? " + found);

		return false;
	}

	private void initVlc() {
discovery();
		/*	try {
		System.setProperty("VLC_PLUGIN_PATH", Paths.get("C:\\Program Files\\VideoLAN\\VLC").getParent() + File.separator + "plugins");
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), Paths.get("C:\\Program Files\\VideoLAN\\VLC").getParent().toString());
		boolean found = new NativeDiscovery().discover();
		System.out.println("foudn? " + found);
			
				System.out.println("Path add: " + Paths.get("C:\\Program Files\\VideoLAN\\VLC").getParent().toString());
				System.out.println(Paths.get("C:\\Program Files\\VideoLAN\\VLC").getParent() + "plugins");
				//			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), System.getProperty("C:/Program Files/VideoLAN/VLC/plugins"));
				//			System.setProperty("VLC_PLUGIN_PATH", System.getProperty("C:/Program Files/VideoLAN/VLC"));
			} catch (Exception e) {
				e.printStackTrace();
			}*/
		Info info = Info.getInstance();

		System.out.printf("vlcj             : %s%n", info.vlcjVersion() != null ? info.vlcjVersion() : "<version not available>");
		System.out.printf("os               : %s%n", (info.os()));
		System.out.printf("java             : %s%n", (info.javaVersion()));
		System.out.printf("java.home        : %s%n", (info.javaHome()));
		System.out.printf("jna.library.path : %s%n", (info.jnaLibraryPath()));
		System.out.printf("java.library.path: %s%n", (info.javaLibraryPath()));
		System.out.printf("PATH             : %s%n", (info.path()));
		System.out.printf("VLC_PLUGIN_PATH  : %s%n", (info.pluginPath()));

		if (RuntimeUtil.isNix()) {
			System.out.printf("LD_LIBRARY_PATH  : %s%n", (info.ldLibraryPath()));
		} else if (RuntimeUtil.isMac()) {
			System.out.printf("DYLD_LIBRARY_PATH          : %s%n", (info.dyldLibraryPath()));
			System.out.printf("DYLD_FALLBACK_LIBRARY_PATH : %s%n", (info.dyldFallbackLibraryPath()));
		}

		//	        if (null != NATIVE_LIBRARY_SEARCH_PATH) {
		//	            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), NATIVE_LIBRARY_SEARCH_PATH);
		//	        }
		//
		//	        System.setProperty("jna.dump_memory", DUMP_NATIVE_MEMORY);

	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
