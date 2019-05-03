package com.girbola.imageviewer.imageviewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.girbola.imageviewer.common.utils.FileUtils;
import com.girbola.imageviewer.imageviewer.image.tasks.MediaConverters;
import com.girbola.imageviewer.imageviewer.image.tasks.RawFile;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;

/**
 *
 * @author Marko Lokka
 */
public class RenderVisibleNode {

    private ExecutorService executorService;
    private Model_ImageViewer model_ImageViewer;

    private ScrollPane scrollPane;
    private Timeline timeline;
    private boolean start;

    private Map<ImageView, Path> map = new HashMap<>();

    public RenderVisibleNode(ScrollPane aScrollPane, Model_ImageViewer model_ImageViewer) {
	this.scrollPane = aScrollPane;
	this.model_ImageViewer = model_ImageViewer;
	executorService = Executors.newSingleThreadExecutor(r -> {
	    Thread t = new Thread(r);
	    t.setName("multi thread");
	    t.setDaemon(true);
	    return t;
	});
    }

    private void renderVisibleNodes() throws NullPointerException {
	if (executorService != null) {
	    if (!executorService.isShutdown() || executorService.isTerminated()) {
		timeline.stop();
		executorService.shutdownNow();
	    }
	}
	checkVisible(scrollPane);

	if (!map.isEmpty()) {
	    initExec();
	    List<Task<Void>> needToConvert_Image_list = new ArrayList<>();
	    List<Task<Void>> needToConvert_Video_list = new ArrayList<>();
	    List<Task<Void>> needToConvert_Raw_list = new ArrayList<>();

	    for (Entry<ImageView, Path> entry : map.entrySet()) {
		ImageView iv = entry.getKey();
		Path file = entry.getValue();

		if (iv != null) {
		    if (Files.exists(file)) {
			if (iv.getImage() == null) {
			    if (FileUtils.supportedVideo(file)) {
				Task<Void> convertVideo = MediaConverters.handleVideoThumbnail(file, iv,
					(model_ImageViewer.getWidth() - 2));
				needToConvert_Video_list.add(convertVideo);
			    } else if (FileUtils.supportedImage(file)) {
				Task<Void> imageThumb = MediaConverters.handleImageThumbnail(file, iv,
					(model_ImageViewer.getWidth() - 2));
				needToConvert_Image_list.add(imageThumb);
			    } else if (FileUtils.supportedRaw(file)) {
				Task<Void> imageThumb = new RawFile(file.toFile(), iv,
					(model_ImageViewer.getWidth() - 2));
				needToConvert_Raw_list.add(imageThumb);
			    }
			}
		    }
		}
	    }
	    for (Task<Void> image_Task : needToConvert_Image_list) {
		executorService.submit(image_Task);
	    }
	    for (Task<Void> raw_Task : needToConvert_Raw_list) {
		executorService.submit(raw_Task);
	    }
	    for (Task<Void> video_Task : needToConvert_Video_list) {
		// System.out.println("Video");
		executorService.submit(video_Task);
	    }
	    executorService.shutdown();
	} else {
	    System.out.println("Visible node were empty");
	}
    }

    private void checkVisible(ScrollPane scrollPane) {
	map.clear();
	Bounds paneBounds = scrollPane.localToScene(scrollPane.getBoundsInParent());

	Node mainNode = scrollPane.getContent();
	if (mainNode instanceof TilePane) {
	    for (Node n : ((TilePane) mainNode).getChildren()) {
		Bounds nodeBounds = n.localToScene(n.getBoundsInLocal());
		if (paneBounds.intersects(nodeBounds)) {
		    if (n instanceof Pane && n.getId().equals("imageFrame")) {
			ImageView iv = (ImageView) n.lookup("#imageView");
			if (iv != null && iv.getImage() == null) {
			    map.put(iv, (Path) n.getUserData());
			}
		    }
		}
	    }
	}
    }

    public void registerScrollPaneProperties() {
	start = true;
	scrollPane.vvalueProperty().addListener((obs) -> {

	    if (timeline != null) {
		timeline.stop();
	    }
	    timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(200), ae -> renderVisibleNodes()));
	    timeline.play();
	    map.clear();
	});
	scrollPane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
	    @Override
	    public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {

		if (timeline != null) {
		    timeline.stop();
		}
		timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(200), ae -> renderVisibleNodes()));
		timeline.play();
		map.clear();
	    }
	});
	scrollPane.setVvalue(-100);
	scrollPane.setVvalue(0);

    }

    public void terminateAllBackgroundTasks() {
	if (executorService != null) {
	    executorService.shutdownNow();
	}
	if (timeline != null) {
	    timeline.stop();
	}
	try {
	    executorService.awaitTermination(1, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

    }

    public ExecutorService getExecutorService() {
	return executorService;
    }

    public void initExec() {
	System.out.println("initexec");
	if (!executorService.isTerminated() || !executorService.isShutdown()) {
	    executorService.shutdownNow();
	}

	executorService = Executors.newSingleThreadExecutor(r -> {
	    // exec_multi = Executors.newCachedThreadPool(r -> {
	    Thread t = new Thread(r);
	    t.setName("single thread");
	    t.setDaemon(true);
	    return t;
	});
	timeline.play();

    }

    public boolean hasStarted() {
	return start;
    }

    public void setStart(boolean start) {
	this.start = start;
    }

    public Timeline getTimeline() {
	return timeline;
    }

}
