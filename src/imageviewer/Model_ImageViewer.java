package imageviewer;

import javafx.scene.control.ScrollPane;

public class Model_ImageViewer {

	final private double width = 100;

	private ScrollPane scrollPane;
	private RenderVisibleNode renderVisibleNode;

	public Model_ImageViewer(ScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		renderVisibleNode = new RenderVisibleNode(scrollPane, this);
	}

	public double getWidth() {
		return width;
	}

	public RenderVisibleNode getRenderVisibleNode() {
		return renderVisibleNode;
	}

	public ScrollPane getScrollPane() {
		return scrollPane;
	}
}
