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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author Marko Lokka
 */
public class MediaConverters {

	@Deprecated
	public static Task<List<BufferedImage>> handleVideoThumbnail_(Path fileName, ImageView imageView, double image_width) {
		Task<List<BufferedImage>> task = new Task<List<BufferedImage>>() {
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

		};
		System.out.println("Video is gonna be: ");
		return task;
	}

	public static Task<Void> handleImageThumbnail(Path fileName, ImageView imageView, double image_width) {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Image image = new Image(fileName.toUri().toString(), image_width, 0, true, true, false);
				imageView.setImage(image);
				return null;
			}
		};
		return task;
	}

}
