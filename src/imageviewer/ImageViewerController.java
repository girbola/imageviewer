package imageviewer;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.TilePane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 *
 * @author Marko Lokka
 */
public class ImageViewerController {

	private Model_ImageViewer model_ImageViewer;

	private Task<Void> task;

	@FXML
	private Button import_btn;
	@FXML
	private TreeView<File> folders_treeView;
	@FXML
	private ScrollPane scrollPane;

	private TilePane tilePane;

	@FXML
	private void import_btn_action(ActionEvent event) {
		Stage stage = (Stage) import_btn.getScene().getWindow();
		DirectoryChooser dc = new DirectoryChooser();
		File file = dc.showDialog(stage);
		if (file != null && file.exists()) {
			validateTask(task);
			task = new DrawPane(file, tilePane, model_ImageViewer);
			new Thread(task).start();
		}
	}

	public void init() {
		model_ImageViewer = new Model_ImageViewer(scrollPane);

		File file = new File(System.getProperty("user.home") + File.separator + "Pictures");
		if (file.exists()) {
			Task<TreeItem<File>> task = new FileTreeView(file);
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					try {
						TreeItem<File> treeItem = task.get();
						treeItem.setExpanded(true);
						folders_treeView.setRoot(treeItem);

						model_ImageViewer.getRenderVisibleNode().registerScrollPaneProperties();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			});
			task.setOnFailed(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					System.out.println("failed");
				}
			});
			task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					System.out.println("cancelled");
				}
			});
			new Thread(task).start();
		}
		folders_treeView.setCellFactory(treeView -> new TreeCell<File>() {
			@Override
			public void updateItem(File path, boolean empty) {
				super.updateItem(path, empty);
				if (empty) {
					setText(null);
				} else {
					if (treeView.getRoot().getValue().equals(path)) {
						setText(path.toString());
					} else {
						setText(path.getName().toString());
					}
				}
			}
		});
		folders_treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<File>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<File>> observable, TreeItem<File> oldValue, TreeItem<File> newValue) {
				task = new DrawPane(newValue.getValue(), tilePane, model_ImageViewer);
				new Thread(task).start();
			}
		});
	}

	private void validateTask(Task<Void> task) {
		if (task == null) {
			System.out.println("validateTask - Task were null");
			return;
		} else {
			if (task.isCancelled()) {
				System.out.println("validateTask - Task were cancelled");
				task.cancel();
				model_ImageViewer.getRenderVisibleNode().getExecutorService().shutdownNow();
				try {
					model_ImageViewer.getRenderVisibleNode().getExecutorService().awaitTermination(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Model_ImageViewer getModel_ImageViewer() {
		return model_ImageViewer;
	}

}
