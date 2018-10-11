package imageviewer.image.tasks;

import java.nio.file.Path;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Marko Lokka
 */
public class MediaConverters {

	public static Task<Void> handleVideoThumbnail(Path fileName, ImageView imageView, double image_width) {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					Picture pic = FrameGrab.getFrameFromFile(fileName.toFile(), (5));
					Image image = SwingFXUtils.toFXImage(AWTUtil.toBufferedImage(pic), null);
					imageView.setImage(image);
				} catch (Exception ex) {
					System.out.println("Exception ex: " + ex);
					return null;
				}
				return null;
			}

		};
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
