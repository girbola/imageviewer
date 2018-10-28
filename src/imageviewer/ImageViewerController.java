package imageviewer;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
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

	private I18NSupport i18n;
	private Task<Void> task;

	@FXML
	private ToggleGroup language;
	@FXML
	RadioMenuItem language_finnish_radio;
	@FXML
	RadioMenuItem language_english_radio;
	@FXML
	RadioMenuItem language_swedish_radio;
	@FXML
	private Button import_btn;
	@FXML
	private TreeView<File> folders_treeView;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private TilePane tilePane;
	@FXML
	private ResourceBundle bundle;

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

	@FXML
	void about_action(ActionEvent event) {

	}

	@FXML
	void language_english_action(ActionEvent event) {
		model_ImageViewer.getConfiguration().setLanguage(Language.ENGLISH.getType().toLowerCase());
		model_ImageViewer.getConfiguration().setCountry(Language.ENGLISH.getType());
		Locale locale = new Locale(model_ImageViewer.getConfiguration().getLanguage(), model_ImageViewer.getConfiguration().getCountry());
		bundle = ResourceBundle.getBundle("bundle/lang", locale);
		model_ImageViewer.getI18nSupport().setBundle(bundle);
		model_ImageViewer.getDialogs().showAlert();
	}

	@FXML
	void language_finnish_action(ActionEvent event) {
		model_ImageViewer.getConfiguration().setLanguage(Language.FINNISH.getType().toLowerCase());
		model_ImageViewer.getConfiguration().setCountry(Language.FINNISH.getType());
		Locale locale = new Locale(model_ImageViewer.getConfiguration().getLanguage(), model_ImageViewer.getConfiguration().getCountry());
		bundle = ResourceBundle.getBundle("bundle/lang", locale);
		model_ImageViewer.getI18nSupport().setBundle(bundle);
		model_ImageViewer.getDialogs().showAlert();
	}

	@FXML
	void language_swedish_action(ActionEvent event) {
		model_ImageViewer.getConfiguration().setLanguage(Language.SWEDISH.getType().toLowerCase());
		model_ImageViewer.getConfiguration().setCountry(Language.SWEDISH.getType());
		Locale locale = new Locale(model_ImageViewer.getConfiguration().getLanguage(), model_ImageViewer.getConfiguration().getCountry());
		bundle = ResourceBundle.getBundle("bundle/lang", locale);
		model_ImageViewer.getI18nSupport().setBundle(bundle);
		model_ImageViewer.getDialogs().showAlert();

	}

	

	@FXML
	void menuItem_File_Close_action(ActionEvent event) {
		model_ImageViewer.getRenderVisibleNode().terminateAllBackgroundTasks();
		// model_ImageViewer.getRenderVisibleNode().getTimeline().stop();
		Stage stage = (Stage) import_btn.getScene().getWindow();

		Platform.exit();
		stage.close();
	}

	public void init(Model_ImageViewer model_ImageViewer) {
		System.out.println("init: " + model_ImageViewer.getI18nSupport().getBundle());
		this.model_ImageViewer = model_ImageViewer;
		model_ImageViewer.setScrollPane(scrollPane);
		model_ImageViewer.init();
		model_ImageViewer.getRenderVisibleNode().registerScrollPaneProperties();
		// model_ImageViewer.getConfiguration().load(file);
		if (model_ImageViewer.getConfiguration().getCountry().equals(Language.ENGLISH.getType())) {
			language_english_radio.setSelected(true);
		} else if (model_ImageViewer.getConfiguration().getCountry().equals(Language.FINNISH.getType())) {
			language_finnish_radio.setSelected(true);
		} else if (model_ImageViewer.getConfiguration().getCountry().equals(Language.SWEDISH.getType())) {
			language_swedish_radio.setSelected(true);
		}
		// model_ImageViewer.getI18nSupport().

		// System.out.println("language is: " +
		// model_ImageViewer.getI18nSupport().getBundle().getLocale().getLanguage());
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
