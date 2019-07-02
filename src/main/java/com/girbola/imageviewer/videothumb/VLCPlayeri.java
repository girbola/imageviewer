package com.girbola.imageviewer.videothumb;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;

import com.girbola.imageviewer.imageviewer.ImageViewer;

import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

/**
 * Example showing how to dynamically resize video.
 * <p>
 * Originally contributed by Vladislav Kisel,
 * https://github.com/caprica/vlcj-javafx/pull/9, incorporated with minor
 * changes.
 * <p>
 * The idea is to first determine the maximum size available (i.e. the screen
 * size) and request that LibVLC send video frames in that size. We then scale
 * *down* from the maximum size to fit the current window size, without any
 * change in the native video buffer format.
 * <p>
 * So LibVLC will always be sending video frames in the maximum possible size.
 * <p>
 * This is a reasonable compromise.
 * <p>
 * For comparison, to achieve dynamic resizing by having LibVLC send video
 * frames at a constantly changing size would require a constantly changing
 * buffer format - and this would require you to get the current play-back
 * position, stop the media player, play the media, set the new buffer format
 * for the new size, and then restore the previous play-back position. Such an
 * approach is clearly problematic.
 */
public class VLCPlayeri implements Runnable {

	private Stage playVideo_stage;

	private Path file;

	private ImageView imageView;
	/**
	 * Lightweight JavaFX canvas, the video is rendered here.
	 */
	private Canvas canvas;

	private PixelWriter pixelWriter;

	private WritablePixelFormat<ByteBuffer> pixelFormat = null;

	private MediaPlayerFactory mediaPlayerFactory = null;
	private WritableImage img;

	private EmbeddedMediaPlayer mediaPlayer = null;

	private WritableImage writableImage;

	private HBox playerHolder;

	private Button forward;
	private Button pause;
	private Button play;
	private Button rewind;
	private Button stop;
	private Slider slider;

	private FloatProperty videoSourceRatioProperty;

	private boolean playerCancelled = false;
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	private final double picRatio = 1.2;
	private SimpleDateFormat hhmmss_dots = new SimpleDateFormat("HH:mm:ss");
	private Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

	private Scene scene;

	public VLCPlayeri(Path file) {
		this.file = file;
	}

	public void initPlayer() {
		playVideo_stage = new Stage();

		BorderPane player_root_bp = new BorderPane();

		scene = new Scene(player_root_bp, visualBounds.getWidth(), visualBounds.getHeight());
		scene.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("scene new width is: " + newValue);
			}
		});
		scene.getStylesheets().add(ImageViewer.class.getResource("/com/girbola/imageviewer/themes/ImageViewer.css").toExternalForm());

		canvas = new Canvas();

		playVideo_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				playerCancelled = true;
				mediaPlayer.controls().stop();
				mediaPlayer.release();
				System.out.println("VLC STAGE CLOSED");

			}
		});

		playerHolder = new HBox();
		playerHolder.setAlignment(Pos.CENTER);
		playerHolder.prefWidthProperty().bind(scene.widthProperty());
		HBox.setHgrow(playerHolder, Priority.ALWAYS);
		playerHolder.getStyleClass().add("playerHolder");
		playerHolder.getChildren().add(canvas);
		playerHolder.setStyle("-fx-background-color: red;");

		slider = new Slider();
		slider.setMin(0);
		slider.setMax(100);
		slider.setValue(0);
		slider.setOrientation(Orientation.HORIZONTAL);

		ProgressBar pb = new ProgressBar(0);

		slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
			Platform.runLater(() -> {
				pb.setProgress((Double) new_val / 100);
			});
		});

		slider.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			Platform.runLater(() -> {
				pb.setMinWidth((Double) newValue);
				pb.setMaxWidth((Double) newValue);
				pb.setPrefWidth((Double) newValue);

			});
		});

		StackPane vlcSliderProgressBar_sp = new StackPane();

		StackPane.setAlignment(pb, Pos.CENTER);
		StackPane.setAlignment(slider, Pos.CENTER);
		vlcSliderProgressBar_sp.setPadding(new Insets(0, 30, 0, 30));

		vlcSliderProgressBar_sp.setAlignment(Pos.CENTER);
		pb.getStyleClass().add("progressBar");

		HBox vlcSliderProgressBar_hbox = new HBox();
		vlcSliderProgressBar_sp.getChildren().addAll(pb, slider);

		vlcSliderProgressBar_hbox.getChildren().add(vlcSliderProgressBar_sp);

		HBox.setHgrow(vlcSliderProgressBar_sp, Priority.ALWAYS);
		vlcSliderProgressBar_hbox.setAlignment(Pos.CENTER);

		HBox buttons_hbox = new HBox();
		buttons_hbox.getStyleClass().add("buttons");
		HBox.setHgrow(buttons_hbox, Priority.ALWAYS);

		forward = new Button("Forward");
		pause = new Button("Pause");
		play = new Button("Play");
		rewind = new Button("Back");
		stop = new Button("Stop");

		buttons_hbox.getChildren().addAll(rewind, stop, play, pause, forward);

		VBox verticalBox = new VBox(5);
		verticalBox.getChildren().addAll(vlcSliderProgressBar_hbox, buttons_hbox);
		player_root_bp.setStyle("-fx-background-color: yellow;");
		player_root_bp.setCenter(canvas);
		player_root_bp.setBottom(verticalBox);

		videoSourceRatioProperty = new SimpleFloatProperty(0.4f);

		playVideo_stage.setScene(scene);

		playVideo_stage.show();
		try {
			pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
			pixelFormat = PixelFormat.getByteBgraInstance();

			mediaPlayerFactory = new MediaPlayerFactory();
			mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
			slider.setOnMousePressed((MouseEvent event) -> {
				mediaPlayer.controls().pause();
				System.out.println("1 slider.getValue(); = " + slider.getValue());
			});
			slider.setOnMouseReleased((MouseEvent event) -> {
				Double val = slider.getValue();
				float sliderValue = val.floatValue() / 100f;
				if (sliderValue > 0.99f) {
					sliderValue = 0.99f;
					System.out.println("sliderValue were out of range but now fixed to0.99f" + sliderValue);
				}

				mediaPlayer.controls().setPosition(sliderValue);
				mediaPlayer.controls().play();
			});
			mediaPlayer.events().addMediaPlayerEventListener(new MPE(slider));
			mediaPlayer.videoSurface().set(new JavaFxVideoSurface());
			mediaPlayer.media().prepare(file.toString());
			mediaPlayer.media().play(file.toString());
		} catch (Exception e) {

		}
	}

	private class MPE implements MediaPlayerEventListener {

		private Slider slider;

		MPE(Slider aSlider) {
			this.slider = aSlider;
		}

		@Override
		public void timeChanged(MediaPlayer mp, long l) {
			//System.out.println("Time now is: " + hhmmss_dots.format(mediaPlayer.status().time()) + " Long is: " + l);
		}

		@Override
		public void positionChanged(MediaPlayer mp, float f) {
			slider.setValue(Double.valueOf("" + (f * 100f)));
		}

		@Override
		public void mediaChanged(MediaPlayer mediaPlayer, MediaRef media) {

		}

		@Override
		public void opening(MediaPlayer mediaPlayer) {

		}

		@Override
		public void buffering(MediaPlayer mediaPlayer, float newCache) {

		}

		@Override
		public void playing(MediaPlayer mediaPlayer) {

		}

		@Override
		public void paused(MediaPlayer mediaPlayer) {

		}

		@Override
		public void stopped(MediaPlayer mediaPlayer) {

		}

		@Override
		public void forward(MediaPlayer mediaPlayer) {

		}

		@Override
		public void backward(MediaPlayer mediaPlayer) {

		}

		@Override
		public void finished(MediaPlayer mediaPlayer) {

		}

		@Override
		public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {

		}

		@Override
		public void pausableChanged(MediaPlayer mediaPlayer, int newPausable) {

		}

		@Override
		public void titleChanged(MediaPlayer mediaPlayer, int newTitle) {

		}

		@Override
		public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {

		}

		@Override
		public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {

		}

		@Override
		public void videoOutput(MediaPlayer mediaPlayer, int newCount) {

		}

		@Override
		public void scrambledChanged(MediaPlayer mediaPlayer, int newScrambled) {

		}

		@Override
		public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType type, int id) {

		}

		@Override
		public void elementaryStreamDeleted(MediaPlayer mediaPlayer, TrackType type, int id) {

		}

		@Override
		public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType type, int id) {

		}

		@Override
		public void corked(MediaPlayer mediaPlayer, boolean corked) {

		}

		@Override
		public void muted(MediaPlayer mediaPlayer, boolean muted) {

		}

		@Override
		public void volumeChanged(MediaPlayer mediaPlayer, float volume) {

		}

		@Override
		public void audioDeviceChanged(MediaPlayer mediaPlayer, String audioDevice) {

		}

		@Override
		public void chapterChanged(MediaPlayer mediaPlayer, int newChapter) {

		}

		@Override
		public void error(MediaPlayer mediaPlayer) {

		}

		@Override
		public void mediaPlayerReady(MediaPlayer mediaPlayer) {

		}

	}

	private void initializeImageView() {

		writableImage = new WritableImage((int) visualBounds.getWidth(), (int) visualBounds.getHeight());
		// writableImage.progressProperty().addListener(new ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> observable, Number
		// oldValue, Number newValue) {
		// System.out.println("writableimage changed: " + newValue);
		// }
		// });
		imageView = new ImageView(writableImage);
		// imageView.setFitWidth(800);
		// imageView.setFitHeight(640);
		// AnchorPane.setLeftAnchor(imageView, -1.0);
		// AnchorPane.setRightAnchor(imageView, -1.0);
		// AnchorPane.setTopAnchor(imageView, -1.0);
		// AnchorPane.setBottomAnchor(imageView, -1.0);

		playerHolder.getChildren().add(imageView);
		playerHolder.widthProperty().addListener((observable, oldValue, newValue) -> {
			fitImageViewSize(newValue.floatValue(), (float) playerHolder.getHeight());
		});

		playerHolder.heightProperty().addListener((observable, oldValue, newValue) -> {
			fitImageViewSize((float) playerHolder.getWidth(), newValue.floatValue());
		});

		videoSourceRatioProperty.addListener((observable, oldValue, newValue) -> {
			fitImageViewSize((float) playerHolder.getWidth(), (float) playerHolder.getHeight());
		});
		stop.setOnAction((ActionEvent event) -> {
			mediaPlayer.controls().stop();
		});
		play.setOnAction((ActionEvent event) -> {
			mediaPlayer.controls().play();
		});
		pause.setOnAction((ActionEvent event) -> {
			mediaPlayer.controls().pause();
		});
		forward.setOnAction((ActionEvent event) -> {
			System.out.println("slider get value = " + (float) ((slider.getValue() / 100f) + 0.1f));
			mediaPlayer.controls().setPosition((float) (slider.getValue() + 0.1f));
		});

		rewind.setOnAction((ActionEvent event) -> {
			System.out.println("slider get value = " + slider.getValue());
			mediaPlayer.controls().setPosition((float) (slider.getValue() - 0.1f));
		});
	}

	private void fitImageViewSize(float width, float height) {
		Platform.runLater(() -> {
			float fitHeight = videoSourceRatioProperty.get() * width;

			if (fitHeight > height) {
				imageView.setFitHeight(height / picRatio);
				double fitWidth = height / videoSourceRatioProperty.get();
				imageView.setFitWidth(fitWidth / picRatio);
				imageView.setX(((width / picRatio) - (fitWidth / picRatio)) / 2);
				imageView.setY(0);
			} else {
				imageView.setFitWidth(width * picRatio);
				imageView.setFitHeight(fitHeight * picRatio);
				imageView.setY(((height / picRatio) - (fitHeight / picRatio)) / 2);
				imageView.setX(0);
			}
		});
	}

	@Override
	public void run() {
		initPlayer();
	}

	private class JavaFxVideoSurface extends CallbackVideoSurface {

		JavaFxVideoSurface() {
			super(new JavaFxBufferFormatCallback(), new JavaFxRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
		}

	}

	private class JavaFxBufferFormatCallback implements BufferFormatCallback {
		@Override
		public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
			VLCPlayeri.this.img = new WritableImage(sourceWidth, sourceHeight);
			VLCPlayeri.this.pixelWriter = img.getPixelWriter();

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					playVideo_stage.setWidth(sourceWidth);
					playVideo_stage.setHeight(sourceHeight);
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

}
