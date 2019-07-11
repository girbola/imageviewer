package com.girbola.imageviewer.imageviewer;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.jna.NativeLibrary;

import javafx.scene.control.Alert.AlertType;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;
import uk.co.caprica.vlcj.support.Info;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;
import uk.co.caprica.vlcj.support.version.Version;

public class VLCJDiscovery {

	private Model_ImageViewer model_ImageViewer;

	public VLCJDiscovery(Model_ImageViewer model_ImageViewer) {
		this.model_ImageViewer = model_ImageViewer;
	}

	private void getLib() {
		Dialogs.sprintf("getLib started");
		LibVlcVersion lbl = new LibVlcVersion();
		if (lbl.getRequiredVersion().atLeast(lbl.getVersion())) {
			Dialogs.sprintf("" + model_ImageViewer.getI18nSupport().getBundle().getString("vlcPlayerVersionIsOld") + " ver: " + lbl.getVersion()
					+ " req: " + lbl.getRequiredVersion());
			Dialogs.showAlert(model_ImageViewer.getI18nSupport().getBundle().getString("vlcPlayerVersionIsOld"), AlertType.WARNING);
			Dialogs.sprintf("getRequiredVersion " + lbl.getRequiredVersion());
			model_ImageViewer.getConfiguration().setVLCSupported(false);
		}
		try {
			Version version = lbl.getVersion();
			Dialogs.sprintf("Version is: " + version.toString());
		} catch (Exception e) {
			Dialogs.sprintf("Can not find Version class of vlcj");
		}
	}

	public boolean discovery() {
		Dialogs.sprintf("disvocery started");
		if (Files.exists(Paths.get(model_ImageViewer.getConfiguration().getVlcPath()))) {
			NativeLibrary.addSearchPath("libvlc", model_ImageViewer.getConfiguration().getVlcPath());
		}
		NativeDiscovery dis = new NativeDiscovery() {

			@Override
			protected void onFound(String path, NativeDiscoveryStrategy strategy) {
				System.out.println("Found; " + " path: " + path + " strategy: " + strategy);
			}

			@Override
			protected void onNotFound() {
				System.out.println("Native not found");
			}
		};
		boolean found = dis.discover(); //new NativeDiscovery().discover();
		if (found) {
			System.out.println("found? " + found + " discoveredPath: " + dis.discoveredPath());
			model_ImageViewer.getConfiguration().setVlcPath(dis.discoveredPath());
			getLib();
			return true;
		}

		return false;
	}

	public void initVlc() {
		boolean found = discovery();
		if (found) {
			Dialogs.sprintf("Found");
		} else {
			Dialogs.sprintf("Not Found");
		}
		Info info = Info.getInstance();

		Dialogs.printf("vlcj             : %s%n", info.vlcjVersion() != null ? info.vlcjVersion() : "<version not available>");
		Dialogs.printf("os               : %s%n", (info.os()));
		Dialogs.printf("java             : %s%n", (info.javaVersion()));
		Dialogs.printf("java.home        : %s%n", (info.javaHome()));
		Dialogs.printf("jna.library.path : %s%n", (info.jnaLibraryPath()));
		Dialogs.printf("java.library.path: %s%n", (info.javaLibraryPath()));
		Dialogs.printf("PATH             : %s%n", (info.path()));
		Dialogs.printf("VLC_PLUGIN_PATH  : %s%n", (info.pluginPath()));

		if (RuntimeUtil.isNix()) {
			Dialogs.printf(" LD_LIBRARY_PATH  : %s%n", (info.ldLibraryPath()));
		} else if (RuntimeUtil.isMac()) {
			Dialogs.printf("DYLD_LIBRARY_PATH          : %s%n", (info.dyldLibraryPath()));
			Dialogs.printf("DYLD_FALLBACK_LIBRARY_PATH : %s%n", (info.dyldFallbackLibraryPath()));
		}
	}

}
