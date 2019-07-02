package com.girbola.imageviewer.imageviewer.image.tasks;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

import com.girbola.imageviewer.imageviewer.VideoPreview;
import com.girbola.imageviewer.videothumb.VideoThumb;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class VideoThumbMaker extends Task<List<BufferedImage>> {

	private Path fileName;
	private ImageView imageView;
	private double image_width;
	private Timeline timeLine;

	public VideoThumbMaker(Path fileName, ImageView imageView, double image_width) {
		this.fileName = fileName;
		this.imageView = imageView;
		this.image_width = image_width;
	}

	@Override
	protected List<BufferedImage> call() throws Exception {
		List<BufferedImage> list = null;
		try {
			list = VideoThumb.getList(fileName.toFile());
		} catch (Exception ex) {
			System.out.println("Exception ex: " + ex);
			return null;
		}
		System.out.println("list size is: " + list.size());
		return list;
	}

	@Override
	protected void succeeded() {
		List<BufferedImage> list = null;
		try {
			list = get();
		} catch (Exception e) {
			super.cancel();
			System.err.println("buffered failed: " + e);
			return;
		}

		StackPane pane = (StackPane) imageView.getParent();
		pane.setStyle("-fx-background-color: white;");
		if (list.isEmpty()) {
			System.err.println("video thumblist were empty. returning: " + fileName);
			pane.getChildren().add(new Label("Video. NP"));
			return;
		} else {
			Label label = new Label("Video");
			label.setMouseTransparent(true);
			pane.getChildren().add(label);

		}
		VideoPreview bvp = new VideoPreview(list, imageView);
		imageView.setImage(bvp.getImage(0));
		pane.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.println("m e");
				timeLine = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
					Image image = SwingFXUtils.toFXImage(bvp.showNextImage(), null);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							imageView.setImage(image);
						}
					});
				}));
				timeLine.setCycleCount(10);
				timeLine.play();
			}
		});
		pane.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (timeLine != null) {
					timeLine.stop();
				}
			}
		});
		System.out.println("List size is gonna be: " + list.size());

	}

	/* (non-Javadoc)
	 * @see javafx.concurrent.Task#cancelled()
	 */
	@Override
	protected void cancelled() {
		// TODO Auto-generated method stub
		super.cancelled();
	}

	/* (non-Javadoc)
	 * @see javafx.concurrent.Task#failed()
	 */
	@Override
	protected void failed() {
		// TODO Auto-generated method stub
		super.failed();
	}

}
