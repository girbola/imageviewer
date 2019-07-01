package com.girbola.imageviewer.imageviewer.options;

import java.io.File;
import java.nio.file.Paths;

import com.girbola.imageviewer.imageviewer.Dialogs;
import com.girbola.imageviewer.imageviewer.ImageViewer;
import com.girbola.imageviewer.imageviewer.Model_ImageViewer;
import com.sun.jna.NativeLibrary;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class SettingsController {

	private Model_ImageViewer model_ImageViewer;
	@FXML
	private TextField vlcPath_tf;
	@FXML
	private Button chooseVLCPath_btn;

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
			Dialogs dialog = new Dialogs(model_ImageViewer);
			dialog.showAlert(model_ImageViewer.getI18nSupport().getBundle().getString("noValidVlcPathChosen"), AlertType.WARNING);
		}
	}

	public void init(Model_ImageViewer model_ImageViewer) {
		this.model_ImageViewer = model_ImageViewer;
		vlcPath_tf.textProperty().bindBidirectional(this.model_ImageViewer.getConfiguration().vlcPath_property());

	}

}
