package com.girbola.imageviewer.videothumb;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.girbola.imageviewer.imageviewer.ImageViewer;
import com.sun.jna.Memory;

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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

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
public class VLCPlayer implements Runnable {

	private Stage stage;

	private Path file;

	private ImageView imageView;

	private DirectMediaPlayerComponent mediaPlayerComponent;

	private WritableImage writableImage;

	private HBox playerHolder;

	private Button forward;
	private Button pause;
	private Button play;
	private Button rewind;
	private Button stop;
	private Slider slider;
	private VLCPlayer vlc;

	private WritablePixelFormat<ByteBuffer> pixelFormat;

	private FloatProperty videoSourceRatioProperty;

	private boolean playerCancelled = false;
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	private final double picRatio = 1.2;
	private SimpleDateFormat hhmmss_dots = new SimpleDateFormat("HH:mm:ss");
	private Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

	private Scene scene;

	public VLCPlayer(Stage stage, Path file) throws Exception {
		this.stage = stage;
		this.file = file;
	}

	public void initPlayer() {
		Stage playVideo_stage = new Stage();

		BorderPane player_root_bp = new BorderPane();

		scene = new Scene(player_root_bp, visualBounds.getWidth(), visualBounds.getHeight());
		scene.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("scene new width is: " + newValue);
			}
		});
		scene.getStylesheets().add(ImageViewer.class.getResource("/com/girbola/imageviewer/themes/ImageViewer.css").toExternalForm());
		//scene.getStylesheets().add(com.girbola.imageviewer.imageviewer.ImageViewer.)
		mediaPlayerComponent = new CanvasPlayerComponent();
		playVideo_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				playerCancelled = true;
				mediaPlayerComponent.getMediaPlayer().stop();
				mediaPlayerComponent.getMediaPlayer().release();
				System.out.println("VLC STAGE CLOSED");

			}
		});
		playerHolder = new HBox();
		playerHolder.setAlignment(Pos.CENTER);
		playerHolder.prefWidthProperty().bind(scene.widthProperty());
		// playerHolder.prefHeightProperty().bind(scene.heightProperty());
		// VBox.setVgrow(playerHolder, Priority.ALWAYS);
		HBox.setHgrow(playerHolder, Priority.ALWAYS);
		// BorderPane.setAlignment(playerHolder, Pos.CENTER);
		// BorderPane.setAlignment(player_root_bp, Pos.CENTER);
		playerHolder.getStyleClass().add("playerHolder");
		// playerHolder.setStyle("-fx-background-color: black;");

		slider = new Slider();
		slider.setMin(0);
		slider.setMax(100);
		slider.setValue(0);
		slider.setOrientation(Orientation.HORIZONTAL);

		slider.setOnMousePressed((MouseEvent event) -> {
			mediaPlayerComponent.getMediaPlayer().pause();
			System.out.println("1 slider.getValue(); = " + slider.getValue());
		});
		slider.setOnMouseReleased((MouseEvent event) -> {
			Double val = slider.getValue();
			float sliderValue = val.floatValue() / 100f;
			if (sliderValue > 0.99f) {
				sliderValue = 0.99f;
				System.out.println("sliderValue were out of range but now fixed to0.99f" + sliderValue);
			}

			mediaPlayerComponent.getMediaPlayer().setPosition(sliderValue);
			mediaPlayerComponent.getMediaPlayer().play();
		});

		ProgressBar pb = new ProgressBar(0);

		slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
			pb.setProgress((Double) new_val / 100);
		});

		slider.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			pb.setMinWidth((Double) newValue);
			pb.setMaxWidth((Double) newValue);
			pb.setPrefWidth((Double) newValue);
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

		player_root_bp.setCenter(playerHolder);
		player_root_bp.setBottom(verticalBox);
		// player_root_bp.getChildren().addAll(playerHolder, verticalBox);
		videoSourceRatioProperty = new SimpleFloatProperty(0.4f);
		pixelFormat = PixelFormat.getByteBgraPreInstance();
		initializeImageView();

		playVideo_stage.setScene(scene);

		mediaPlayerComponent.getMediaPlayer().prepareMedia(file.toString());
		mediaPlayerComponent.getMediaPlayer().start();
		mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MPE(slider));
		playVideo_stage.show();
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
			mediaPlayerComponent.getMediaPlayer().stop();
		});
		play.setOnAction((ActionEvent event) -> {
			mediaPlayerComponent.getMediaPlayer().play();
		});
		pause.setOnAction((ActionEvent event) -> {
			mediaPlayerComponent.getMediaPlayer().pause();
		});
		forward.setOnAction((ActionEvent event) -> {
			System.out.println("slider get value = " + (float) ((slider.getValue() / 100f) + 0.1f));
			mediaPlayerComponent.getMediaPlayer().setPosition((float) (slider.getValue() + 0.1f));
		});

		rewind.setOnAction((ActionEvent event) -> {
			System.out.println("slider get value = " + slider.getValue());
			mediaPlayerComponent.getMediaPlayer().setPosition((float) (slider.getValue() - 0.1f));
		});
	}

	private void fitImageViewSize_(float width, float height) {
		// Platform.runLater(() -> {

		float fitHeight = videoSourceRatioProperty.get() * width;
		if (fitHeight <= 0) {
			System.out.println("ERROR with fitHeight: " + fitHeight);
			return;
		}
		System.out.println("testing fitheight: " + fitHeight + " height: " + height + " width: " + width + " videoSourceRatioProperty.get(): "
				+ videoSourceRatioProperty.get());
		if (fitHeight > height) {
			if (fitHeight > 640) {
				height = 640;
			}
			System.out.println("fitHeight is > height: " + fitHeight);
			imageView.setFitHeight(height);
			double fitWidth = height / videoSourceRatioProperty.get();
			if (fitWidth > 640) {
				fitWidth = 640;
			}
			System.out.println("fitWidth: " + fitWidth);
			imageView.setFitWidth(fitWidth);
			imageView.setX((width - fitWidth) / 2);
			imageView.setY(0);

		} else if (fitHeight < height) {
			System.out.println("fitHeight is else: " + +fitHeight);
			imageView.setFitWidth(width);
			imageView.setFitHeight(fitHeight);
			imageView.setY((height - fitHeight) / 2);
			imageView.setX(0);

		} else {

		}
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

	private class CanvasPlayerComponent extends DirectMediaPlayerComponent {

		public CanvasPlayerComponent() {
			super(new CanvasBufferFormatCallback());
		}

		PixelWriter pixelWriter = null;

		private PixelWriter getPW() {
			if (pixelWriter == null) {
				pixelWriter = writableImage.getPixelWriter();
			}
			return pixelWriter;
		}

		@Override
		public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
			if (writableImage == null) {
				return;
			}

			Memory nativeBuffer = mediaPlayer.lock()[0];

			try {
				ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
				getPW().setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
			} finally {
				mediaPlayer.unlock();
			}

		}
	}

	private class CanvasBufferFormatCallback implements BufferFormatCallback {

		@Override
		public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
			Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
			Platform.runLater(() -> videoSourceRatioProperty.set((float) sourceHeight / (float) sourceWidth));
			return new RV32BufferFormat((int) visualBounds.getWidth(), (int) visualBounds.getHeight());
		}
	}

	public class MPE implements MediaPlayerEventListener {

		private Slider slider;

		MPE(Slider aSlider) {
			this.slider = aSlider;
		}

		@Override
		public void mediaChanged(MediaPlayer mp, libvlc_media_t l, String string) {

		}

		@Override
		public void opening(MediaPlayer mp) {

		}

		@Override
		public void buffering(MediaPlayer mp, float f) {

		}

		@Override
		public void playing(MediaPlayer mp) {

		}

		@Override
		public void paused(MediaPlayer mp) {
		}

		@Override
		public void stopped(MediaPlayer mp) {

		}

		@Override
		public void forward(MediaPlayer mp) {

		}

		@Override
		public void backward(MediaPlayer mp) {

		}

		@Override
		public void finished(MediaPlayer mp) {

		}

		@Override
		public void timeChanged(MediaPlayer mp, long l) {
			System.out.println("Time now is: " + hhmmss_dots.format(mediaPlayerComponent.getMediaPlayer().getTime()) + " Long is: " + l);
		}

		@Override
		public void positionChanged(MediaPlayer mp, float f) {
			slider.setValue(Double.valueOf("" + (f * 100f)));
		}

		@Override
		public void seekableChanged(MediaPlayer mp, int i) {

		}

		@Override
		public void pausableChanged(MediaPlayer mp, int i) {

		}

		@Override
		public void titleChanged(MediaPlayer mp, int i) {

		}

		@Override
		public void snapshotTaken(MediaPlayer mp, String string) {

		}

		@Override
		public void lengthChanged(MediaPlayer mp, long l) {

		}

		@Override
		public void videoOutput(MediaPlayer mp, int i) {

		}

		@Override
		public void scrambledChanged(MediaPlayer mp, int i) {

		}

		@Override
		public void elementaryStreamAdded(MediaPlayer mp, int i, int i1) {

		}

		@Override
		public void elementaryStreamDeleted(MediaPlayer mp, int i, int i1) {

		}

		@Override
		public void elementaryStreamSelected(MediaPlayer mp, int i, int i1) {

		}

		@Override
		public void error(MediaPlayer mp) {

		}

		@Override
		public void mediaMetaChanged(MediaPlayer mp, int i) {

		}

		@Override
		public void mediaSubItemAdded(MediaPlayer mp, libvlc_media_t l) {

		}

		@Override
		public void mediaDurationChanged(MediaPlayer mp, long l) {

		}

		@Override
		public void mediaParsedChanged(MediaPlayer mp, int i) {

		}

		@Override
		public void mediaFreed(MediaPlayer mp) {

		}

		@Override
		public void mediaStateChanged(MediaPlayer mp, int i) {

		}

		@Override
		public void mediaSubItemTreeAdded(MediaPlayer mp, libvlc_media_t l) {

		}

		@Override
		public void newMedia(MediaPlayer mp) {

		}

		@Override
		public void subItemPlayed(MediaPlayer mp, int i) {

		}

		@Override
		public void subItemFinished(MediaPlayer mp, int i) {

		}

		@Override
		public void endOfSubItems(MediaPlayer mp) {

		}

		@Override
		public void corked(MediaPlayer mp, boolean bln) {
		}

		@Override
		public void muted(MediaPlayer mp, boolean bln) {
		}

		@Override
		public void volumeChanged(MediaPlayer mp, float f) {
		}

		@Override
		public void audioDeviceChanged(MediaPlayer mp, String string) {
		}

		@Override
		public void chapterChanged(MediaPlayer mp, int i) {
		}

		@Override
		public void mediaPlayerReady(MediaPlayer mediaPlayer) {
			throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																			// choose Tools | Templates.
		}

		@Override
		public void mediaParsedStatus(MediaPlayer mediaPlayer, int newStatus) {
			throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																			// choose Tools | Templates.
		}

	}
}
