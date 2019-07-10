package com.girbola.imageviewer.imageviewer;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.sun.jna.NativeLibrary;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;
import uk.co.caprica.vlcj.support.Info;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;
import uk.co.caprica.vlcj.support.version.Version;

public class Model_ImageViewer {

	final private double width = 200;

	private ScrollPane scrollPane;
	private RenderVisibleNode renderVisibleNode;
	private I18NSupport i18nSupport;
	private Configuration configuration = new Configuration();
	private Dialogs dialogs;
	private SelectionModel selectionModel = new SelectionModel();

	public Model_ImageViewer() {
		boolean loaded = loadConfig();
		if (!loaded) {
			Dialogs.sprintf("loaded were false. Creating required program path(s)");
			boolean created = createProgramPaths();
			if (!created) {
				Dialogs.sprintf("Could not able to create directories. Exiting...");
				Platform.exit();
			}
		}
		i18nSupport = new I18NSupport(configuration.getLanguage(), configuration.getCountry());
	}

	private boolean createProgramPaths() {
		Path appDataPath = configuration.getAppDataPath();
		try {
			Files.createDirectories(appDataPath);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public Dialogs getDialogs() {
		return dialogs;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public I18NSupport getI18nSupport() {
		return i18nSupport;
	}

	public void init() {
		renderVisibleNode = new RenderVisibleNode(scrollPane, this);
	}

	public void setScrollPane(ScrollPane scrollPane) {
		this.scrollPane = scrollPane;
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

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;

	}

	//	public Path getConfigPath() {
	//		Path path = Paths.get(configuration.getAppDataPath().toString() + File.separator + "config.dat");
	//		return path;
	//	}

	public boolean saveConfig() {
		Path path = configuration.getConfigDataPath();
		try {
			JAXBContext context = JAXBContext.newInstance(Configuration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(getConfiguration(), System.out);
			marshaller.marshal(getConfiguration(), path.toFile());
			return true;
		} catch (JAXBException e) {
			e.printStackTrace();
			Dialogs.errorAlert(e.getMessage(), this);
			closeProgram();
			return false;
		}

	}

	private boolean loadConfig() {
		Path path = configuration.getConfigDataPath();
		System.out.println("loading config: " + path);
		if (!Files.exists(path)) {
			System.out.println("Config file doesn't exists: " + path);
			return false;
		}
		try {
			JAXBContext jabx = JAXBContext.newInstance(Configuration.class);
			Unmarshaller unmarshaller = jabx.createUnmarshaller();
			Configuration configuration = (Configuration) unmarshaller.unmarshal(path.toFile());
			setConfiguration(configuration);
			return true;
		} catch (JAXBException e) {
			e.printStackTrace();
			return false;
			//			Dialogs.errorAlert(e.getMessage(), this);
			//			closeProgram();
		}

	}

	public void closeProgram() {
		getRenderVisibleNode().terminateAllBackgroundTasks();
		saveConfig();
		//		Platform.exit();
	}

	/**
	 * @return the selectionModel
	 */
	public SelectionModel getSelectionModel() {
		return this.selectionModel;
	}

	private void getLib() {
		Dialogs.sprintf("getLib started");
		LibVlcVersion lbl = new LibVlcVersion();
		if (lbl.getRequiredVersion().atLeast(lbl.getVersion())) {
			Dialogs.sprintf("" + i18nSupport.getBundle().getString("vlcPlayerVersionIsOld") + " ver: " + lbl.getVersion() + " req: "
					+ lbl.getRequiredVersion());
			Dialogs.showAlert(i18nSupport.getBundle().getString("vlcPlayerVersionIsOld"), AlertType.WARNING);
			Dialogs.sprintf("getRequiredVersion " + lbl.getRequiredVersion());
			configuration.setVLCSupported(false);
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
		NativeLibrary.addSearchPath("libvlc", "C:\\Program Files\\VideoLAN\\VLC");

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

		boolean found = new NativeDiscovery().discover();
		if (found) {
			System.out.println("found? " + found + " discoveredPath: " + dis.discoveredPath());
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

		System.out.printf("vlcj             : %s%n", info.vlcjVersion() != null ? info.vlcjVersion() : "<version not available>");
		System.out.printf("os               : %s%n", (info.os()));
		System.out.printf("java             : %s%n", (info.javaVersion()));
		System.out.printf("java.home        : %s%n", (info.javaHome()));
		System.out.printf("jna.library.path : %s%n", (info.jnaLibraryPath()));
		System.out.printf("java.library.path: %s%n", (info.javaLibraryPath()));
		System.out.printf("PATH             : %s%n", (info.path()));
		System.out.printf("VLC_PLUGIN_PATH  : %s%n", (info.pluginPath()));

		if (RuntimeUtil.isNix()) {
			System.out.printf(" LD_LIBRARY_PATH  : %s%n", (info.ldLibraryPath()));
		} else if (RuntimeUtil.isMac()) {
			System.out.printf("DYLD_LIBRARY_PATH          : %s%n", (info.dyldLibraryPath()));
			System.out.printf("DYLD_FALLBACK_LIBRARY_PATH : %s%n", (info.dyldFallbackLibraryPath()));
		}
	}

}
