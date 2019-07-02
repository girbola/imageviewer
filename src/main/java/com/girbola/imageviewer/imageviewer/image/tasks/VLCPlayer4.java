package com.girbola.imageviewer.imageviewer.image.tasks;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public abstract class VLCPlayer4 {

	/**
	 * Lightweight JavaFX canvas, the video is rendered here.
	 */
	private final Canvas canvas;

	private PixelWriter pixelWriter;


	private final WritablePixelFormat<ByteBuffer> pixelFormat;


	private final MediaPlayerFactory mediaPlayerFactory;
	private WritableImage img;

	private final EmbeddedMediaPlayer mediaPlayer;
	private Stage stage;
	
	public VLCPlayer4(File file) {
		canvas = new Canvas();

		pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
		pixelFormat = PixelFormat.getByteBgraInstance();

		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

		mediaPlayer.videoSurface().set(new JavaFxVideoSurface());

		mediaPlayer.controls().setRepeat(true);

		mediaPlayer.media().play(file.toString());

	}

	private class JavaFxVideoSurface extends CallbackVideoSurface {

		JavaFxVideoSurface() {
			super(new JavaFxBufferFormatCallback(), new JavaFxRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
		}

	}

	private class JavaFxBufferFormatCallback implements BufferFormatCallback {
		@Override
		public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
			VLCPlayer4.this.img = new WritableImage(sourceWidth, sourceHeight);
			VLCPlayer4.this.pixelWriter = img.getPixelWriter();

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					stage.setWidth(sourceWidth);
					stage.setHeight(sourceHeight);
				}
			});
			return new RV32BufferFormat(sourceWidth, sourceHeight);
		}
	}

	// Semaphore used to prevent the pixel writer from being updated in one thread while it is being rendered by a
	// different thread
	private final Semaphore semaphore = new Semaphore(1);

	// This is correct as far as it goes, but we need to use one of the timers to get smooth rendering (the timer is
	// handled by the demo sub-classes)
	private class JavaFxRenderCallback implements RenderCallback {
		@Override
		public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
			try {
				semaphore.acquire();
				pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, nativeBuffers[0],
						bufferFormat.getPitches()[0]);
				semaphore.release();
			} catch (InterruptedException e) {
			}
		}
	}

	protected final void renderFrame() {
		GraphicsContext g = canvas.getGraphicsContext2D();

		double width = canvas.getWidth();
		double height = canvas.getHeight();

		g.setFill(new Color(0, 0, 0, 1));
		g.fillRect(0, 0, width, height);

		if (img != null) {
			double imageWidth = img.getWidth();
			double imageHeight = img.getHeight();

			double sx = width / imageWidth;
			double sy = height / imageHeight;

			double sf = Math.min(sx, sy);

			double scaledW = imageWidth * sf;
			double scaledH = imageHeight * sf;

			Affine ax = g.getTransform();

			g.translate((width - scaledW) / 2, (height - scaledH) / 2);

			if (sf != 1.0) {
				g.scale(sf, sf);
			}

			try {
				semaphore.acquire();
				g.drawImage(img, 0, 0);
				semaphore.release();
			} catch (InterruptedException e) {
			}

			g.setTransform(ax);
		}
	}

	/**
	 *
	 */
	protected abstract void startTimer();

	/**
	 *
	 */
	protected abstract void stopTimer();

}
