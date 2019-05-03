package com.girbola.imageviewer.imageviewer;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;

import com.girbola.imageviewer.common.utils.FileUtils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

/**
 *
 * @author Marko Lokka
 */
public class DrawPane extends Task<Void> {

	private Model_ImageViewer model_ImageViewer;
	private File file;
	private TilePane tilePane;

	public DrawPane(File aFile, TilePane aTilePane, Model_ImageViewer aModel_ImageViewer) {
		this.file = aFile;
		this.tilePane = aTilePane;
		this.model_ImageViewer = aModel_ImageViewer;
	}

	@Override
	protected Void call() throws Exception {
		System.out.println("Drawpane started call");
		if (model_ImageViewer.getRenderVisibleNode().hasStarted()) {
			model_ImageViewer.getRenderVisibleNode().terminateAllBackgroundTasks();
			System.out.println("Stopped");
		}
		if (!tilePane.getChildren().isEmpty()) {
			Platform.runLater(new Runnable() {
				public void run() {
					tilePane.getChildren().clear();
				}
			});
		}
		File[] folder = new File(file.toString()).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory();
			}
		});

		if (folder.length == 0) {
			model_ImageViewer.getRenderVisibleNode().initExec();
			System.out.println("Folder is empty");
			return null;
		}
		for (File file : folder) {
			if (FileUtils.supportedMediaFormat(file.toPath())) {
				StackPane pane = createPane(file.toPath(), model_ImageViewer.getWidth());
				ImageView imageView = createImageView((model_ImageViewer.getWidth() - 2));
				Image image = null;
				imageView.setImage(image);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						pane.getChildren().add(imageView);
						tilePane.getChildren().add(pane);
					}
				});
			}
		}
		model_ImageViewer.getRenderVisibleNode().getTimeline().play();

		return null;
	}

	private ImageView createImageView(double width) {
		ImageView imageView = new ImageView();
		imageView.setId("imageView");
		imageView.setFitHeight(width - 4);
		imageView.setFitWidth(width - 4);
		imageView.setPreserveRatio(true);
		imageView.setMouseTransparent(true);
		return imageView;
	}

	private StackPane createPane(Path path, double width) {
		StackPane imageFrame = new StackPane();
		imageFrame.setUserData(path);
		imageFrame.setId("imageFrame");
		imageFrame.getStyleClass().add("imageFrame");
		imageFrame.setPrefSize(width, width);
		imageFrame.setMinSize(width, width);
		imageFrame.setMaxSize(width, width);
		imageFrame.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() == 2) {
					viewImage(path);
				}
			}

			private void viewImage(Path path) {
				StackPane viewImagePane = new StackPane();
				viewImagePane.setPrefSize(300, 300);
				viewImagePane.setMinSize(300, 300);
				viewImagePane.setMaxSize(300, 300);
				viewImagePane.getStyleClass().add("viewImage");

				ImageView imageView = new ImageView();
				Image image = new Image(path.toUri().toString(), 294, 0, true, true, true);
				imageView.setImage(image);

				viewImagePane.getChildren().add(imageView);

				StackPane.setAlignment(imageView, Pos.CENTER);

				Scene scene = new Scene(viewImagePane, 300, 300);
				scene.getStylesheets().add(ImageViewer.class.getResource("/themes/ImageViewer.css").toExternalForm());

				Stage stage = new Stage();
				stage.setResizable(false);
				stage.setScene(scene);
				stage.show();
				stage.toFront();
			}
		});
		return imageFrame;
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		model_ImageViewer.getRenderVisibleNode().terminateAllBackgroundTasks();
	}

	@Override
	protected void failed() {
		super.failed();
		model_ImageViewer.getRenderVisibleNode().terminateAllBackgroundTasks();
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		System.out.println("task succeeded");
	}
}
