package imageviewer.image.tasks;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;

import imageviewer.RenderVisibleNode;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Marko Lokka
 */
public class RawFile extends Task<Void> {

	private File file;
	private ImageView imageView;
	private double image_Width;

	public RawFile(File aFile, ImageView aImageView, double aWidth) {
		this.file = aFile;
		this.imageView = aImageView;
		this.image_Width = aWidth;
	}

	@Override
	protected Void call() throws Exception {

		Metadata metaData = null;
		try {
			metaData = readMetaData(file.toPath());
		} catch (Exception e) {
			System.out.println("Cannot read raw file format: " + file);
			metaData = null;
		}
		if (isCancelled()) {
			return null;
		}
		ExifThumbnailDirectory directory = metaData.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
		if (directory != null) {
			try {
				int offset = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET);
				int length = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);
				System.out.println("offset: " + offset + " lenght: " + length);
				if (offset != 0) {
					byte[] slice = readBytes(file.toPath(), offset, length);
					if (slice == null) {
						System.out.println("Cannot readAllByte to data->");
						cancel();
						return null;
					}

					if (isCancelled()) {
						slice = null;
						return null;
					}
					ByteArrayInputStream in = new ByteArrayInputStream(slice);
					BufferedImage bufferedImage = null;
					try {
						bufferedImage = ImageIO.read(in);
					} catch (IOException ex) {
						Logger.getLogger(MediaConverters.class.getName()).log(Level.SEVERE, null, ex);
					}

					imageView.setImage(null);
					imageView.setFitWidth(image_Width);
					imageView.setFitHeight(image_Width);
					imageView.setPreserveRatio(true);

					if (isCancelled()) {
						slice = null;
						return null;
					}
					try {
						Image image = SwingFXUtils.toFXImage(bufferedImage, null);
						imageView.setImage(image);

					} catch (Exception e) {

						System.out.println("Error loading image: " + e.getMessage());
						Image image = new Image(file.toString(), image_Width, 0, true, true, true);
						imageView.setImage(image);
					}

				}
			} catch (Exception e) {
				System.out.println("Cannot read offset and length");
			}
		}
		return null;
	}

	public static byte[] readBytes(Path file, int start, int size) throws IOException {
		long fileSize = Files.size(file);

		if (start < 0) {
			throw new IllegalArgumentException("The start may not be negative!");
		}

		if (size < 0) {
			throw new IllegalArgumentException("The size may not be negative!");
		}

		if (start + size > fileSize) {
			throw new IllegalArgumentException("Interval exceeds file size!");
		}

		byte[] readBytes = new byte[size];

		try (InputStream inputStream = new FileInputStream(file.toFile())) {
			long actuallySkipped = inputStream.skip(start);

			if (start != actuallySkipped) {
				throw new IllegalStateException("Error while skipping bytes ahead!");
			}

			int bytesReadCount = inputStream.read(readBytes, 0, size);
			if (bytesReadCount != size) {
				throw new IllegalStateException("Not enough bytes have been read!");
			}
		}

		return readBytes;
	}

	private Metadata readMetaData(Path path) {
		Metadata metaData = null;
		try {
			metaData = ImageMetadataReader.readMetadata(path.toFile());
		} catch (Exception e) {
			return null;
		}
		return metaData;
	}
}
