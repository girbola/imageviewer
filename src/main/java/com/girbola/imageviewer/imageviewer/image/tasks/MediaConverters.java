package com.girbola.imageviewer.imageviewer.image.tasks;

import java.nio.file.Path;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Marko Lokka
 */
public class MediaConverters {

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
