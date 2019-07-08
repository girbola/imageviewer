package com.girbola.imageviewer.imageviewer;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.girbola.imageviewer.common.utils.FileUtils;
import com.girbola.imageviewer.imageviewer.options.SettingsController;
import com.girbola.imageviewer.vlc.VLCPlayerController;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

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
	private MenuItem settings_menuItem;
	@FXML
	private RadioMenuItem language_finnish_radio;
	@FXML
	private RadioMenuItem language_english_radio;
	@FXML
	private RadioMenuItem language_swedish_radio;
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
	private StackPane stackPane;

	@FXML
	private void settings_menuItem_action(ActionEvent event) {
		Parent root = null;
		FXMLLoader loader = null;
		SettingsController settingsController = null;
		try {
			loader = new FXMLLoader(SettingsController.class.getResource("Settings.fxml"));
			loader.setResources(model_ImageViewer.getI18nSupport().getBundle());
			root = loader.load();
			settingsController = (SettingsController) loader.getController();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Scene options = new Scene(root);
		options.getStylesheets().add(ImageViewer.class.getResource("/com/girbola/imageviewer/themes/Options.css").toExternalForm());

		Stage stage = new Stage();
		stage.setScene(options);
		stage.show();
	}

	private void viewImage(Path path) {
		StackPane viewImagePane = new StackPane();
		viewImagePane.setPrefSize(300, 300);
		viewImagePane.setMinSize(300, 300);
		viewImagePane.setMaxSize(300, 300);
		viewImagePane.getStyleClass().add("viewImage");

		ImageView imageView = new ImageView();
		imageView.setMouseTransparent(true);
		Image image = new Image(path.toUri().toString(), 294, 0, true, true, true);
		imageView.setImage(image);

		viewImagePane.getChildren().add(imageView);

		StackPane.setAlignment(imageView, Pos.CENTER);

		Scene scene = new Scene(viewImagePane, 300, 300);
		scene.getStylesheets().add(ImageViewer.class.getResource("/com/girbola/imageviewer/themes/ImageViewer.css").toExternalForm());

		Stage stage = new Stage();
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
		stage.toFront();
	}

	@FXML
	private void import_btn_action(ActionEvent event) {
		Stage stage = (Stage) import_btn.getScene().getWindow();
		DirectoryChooser dc = new DirectoryChooser();
		File file = dc.showDialog(stage);
		if (file != null && file.exists()) {
			validateTask(task);
			task = new DrawPane(file, tilePane, model_ImageViewer);
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					System.out.println("Event: " + event);
					tilePane.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							System.out.println("Target: " + event.getTarget());
							if (event.getTarget() instanceof StackPane) {
								StackPane stackPane = (StackPane) event.getTarget();
								System.out.println("Stackpane found!: " + stackPane.getId());
							}
						}
					});
				}

			});
			task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {

				}
			});

			task.setOnFailed(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {

				}
			});

			new Thread(task).start();
		}
	}

	@FXML
	void about_action(ActionEvent event) {
		model_ImageViewer.getDialogs().showAlert("ImageViewer 0.14\n\n" + "https://github.com/girbola/imageviewer", AlertType.INFORMATION);

	}

	@FXML
	void language_english_action(ActionEvent event) {
		model_ImageViewer.getConfiguration().setLanguage(Language.ENGLISH.getType().toLowerCase());
		model_ImageViewer.getConfiguration().setCountry(Language.ENGLISH.getType());
		Locale locale = new Locale(model_ImageViewer.getConfiguration().getLanguage(), model_ImageViewer.getConfiguration().getCountry());
		bundle = ResourceBundle.getBundle("bundle/lang", locale);
		model_ImageViewer.getI18nSupport().setBundle(bundle);
		model_ImageViewer.getDialogs().showAlert(model_ImageViewer.getI18nSupport().getBundle().getString("affectNextTime"), AlertType.INFORMATION);
	}

	@FXML
	void language_finnish_action(ActionEvent event) {
		model_ImageViewer.getConfiguration().setLanguage(Language.FINNISH.getType().toLowerCase());
		model_ImageViewer.getConfiguration().setCountry(Language.FINNISH.getType());
		Locale locale = new Locale(model_ImageViewer.getConfiguration().getLanguage(), model_ImageViewer.getConfiguration().getCountry());
		bundle = ResourceBundle.getBundle("bundle/lang", locale);
		model_ImageViewer.getI18nSupport().setBundle(bundle);
		model_ImageViewer.getDialogs().showAlert(model_ImageViewer.getI18nSupport().getBundle().getString("affectNextTime"), AlertType.INFORMATION);

	}

	@FXML
	void language_swedish_action(ActionEvent event) {
		model_ImageViewer.getConfiguration().setLanguage(Language.SWEDISH.getType().toLowerCase());
		model_ImageViewer.getConfiguration().setCountry(Language.SWEDISH.getType());
		Locale locale = new Locale(model_ImageViewer.getConfiguration().getLanguage(), model_ImageViewer.getConfiguration().getCountry());
		bundle = ResourceBundle.getBundle("bundle/lang", locale);
		model_ImageViewer.getI18nSupport().setBundle(bundle);
		model_ImageViewer.getDialogs().showAlert(model_ImageViewer.getI18nSupport().getBundle().getString("affectNextTime"), AlertType.INFORMATION);

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
		if (model_ImageViewer.getConfiguration().getCountry().equals(Language.ENGLISH.getType())) {
			language_english_radio.setSelected(true);
		} else if (model_ImageViewer.getConfiguration().getCountry().equals(Language.FINNISH.getType())) {
			language_finnish_radio.setSelected(true);
		} else if (model_ImageViewer.getConfiguration().getCountry().equals(Language.SWEDISH.getType())) {
			language_swedish_radio.setSelected(true);
		}
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
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						System.out.println("Event: " + event);
						tilePane.setOnMouseClicked(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								if (event.getButton().equals(MouseButton.PRIMARY)) {
									if (event.getClickCount() == 1) {
										if (event.getTarget() instanceof StackPane && ((Node) event.getTarget()).getId().equals("imageFrame")) {
											model_ImageViewer.getSelectionModel().add((Node) event.getTarget());
										}
									} else if (event.getClickCount() == 2) {
										System.out.println("Target: " + event.getTarget());
										if (event.getTarget() instanceof StackPane && ((Node) event.getTarget()).getId().equals("imageFrame")) {
											StackPane stackPane = (StackPane) event.getTarget();
											Path path = (Path) stackPane.getUserData();
											if (FileUtils.supportedVideo(path)) {
												System.out.println("Video found!: " + path);
												viewVideo(path);
											} else if (FileUtils.supportedImage(path)) {
												viewImage(path);
											} else if (FileUtils.supportedRaw(path)) {

											} else {
												Dialogs.showAlert("Not supported media format", AlertType.INFORMATION);
											}
										}
									}
								}
							}
						});
					}

				});
				task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {

					}
				});

				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {

					}
				});

				new Thread(task).start();
			}
		});
	}

	private void viewVideo(Path path) {
		try {
			Parent root = null;
			FXMLLoader loader = new FXMLLoader(VLCPlayerController.class.getResource("VLCPlayer.fxml"));
			root = loader.load();
			VLCPlayerController vlcPlayerController = (VLCPlayerController) loader.getController();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(ImageViewer.class.getResource("/com/girbola/imageviewer/themes/ImageViewer.css").toExternalForm());

			Stage stage = new Stage();
			stage.setScene(scene);

			stage.initStyle(StageStyle.UNDECORATED);
			vlcPlayerController.init(path, stage);
			stage.show();

			stage.focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					System.out.println("focus? " + newValue);
					if (!newValue) {
						vlcPlayerController.stop();
						stage.close();
					}
				}
			});
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					vlcPlayerController.stop();
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
		}
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
