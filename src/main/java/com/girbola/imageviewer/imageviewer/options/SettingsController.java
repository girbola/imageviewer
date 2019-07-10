package com.girbola.imageviewer.imageviewer.options;

import java.io.File;
import java.nio.file.Paths;

import com.girbola.imageviewer.imageviewer.Dialogs;
import com.girbola.imageviewer.imageviewer.Model_ImageViewer;
import com.sun.jna.NativeLibrary;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

public class SettingsController {

	private Model_ImageViewer model_ImageViewer;
	@FXML
	private TextField vlcPath_tf;
	@FXML
	private Button chooseVLCPath_btn;
	@FXML
	private CheckBox isVLC_chk;
	@FXML
	private HBox vlc_container;

	@FXML
	private void chooseVLCPath_btn_action(ActionEvent event) {
		FileChooser dc = new FileChooser();
		dc.getExtensionFilters().add(new FileChooser.ExtensionFilter("libvlc.dll", "libvlc.dll"));
		System.out.println("Getting vlc path with filechooser");
		File vlcPath_fc = dc.showOpenDialog(chooseVLCPath_btn.getScene().getWindow());

		if (vlcPath_fc != null) {
			try {
				System.setProperty("VLC_PLUGIN_PATH", Paths.get(vlcPath_fc.toString()).getParent() + "plugins");
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), Paths.get(vlcPath_fc.toString()).getParent().toString());
				System.out.println("vlcpath: " + vlcPath_fc);
				model_ImageViewer.getConfiguration().setVlcPath(Paths.get(vlcPath_fc.toString()).getParent().toString());
				vlcPath_tf.setText(model_ImageViewer.getConfiguration().getVlcPath());
				model_ImageViewer.getConfiguration().setVLCSupported(true);
				System.out.println("written to propertyfile: " + vlcPath_fc);
			} catch (Exception e) {
				System.err.println("Error: " + e);
			}
		} else {
			Dialogs.showAlert(model_ImageViewer.getI18nSupport().getBundle().getString("noValidVlcPathChosen"), AlertType.WARNING);
		}
	}

	public void init(Model_ImageViewer model_ImageViewer) {
		Dialogs.sprintf("init settingscontroller");
		this.model_ImageViewer = model_ImageViewer;
		isVLC_chk.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					Dialogs.sprintf("enabled");
					model_ImageViewer.getConfiguration().setVLCSupported(true);
					vlc_container.setDisable(false);
					model_ImageViewer.initVlc();
				} else {
					Dialogs.sprintf("disabled");
					model_ImageViewer.getConfiguration().setVLCSupported(false);
					vlc_container.setDisable(true);
				}
			}
		});
		model_ImageViewer.getConfiguration().isVLCSupported_property().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					isVLC_chk.setSelected(true);
				} else {
					isVLC_chk.setSelected(false);
				}
			}
		});
		vlcPath_tf.textProperty().bindBidirectional(this.model_ImageViewer.getConfiguration().vlcPath_property());

		if (model_ImageViewer.getConfiguration().isVLCSupported()) {
			isVLC_chk.setSelected(true);
		} else {
			isVLC_chk.setSelected(false);
		}
	}

}
