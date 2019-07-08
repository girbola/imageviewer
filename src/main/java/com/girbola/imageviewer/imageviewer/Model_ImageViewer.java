package com.girbola.imageviewer.imageviewer;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;

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

	public void saveConfig() {
		Path path = configuration.getConfigDataPath();
		try {
			JAXBContext context = JAXBContext.newInstance(Configuration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(getConfiguration(), System.out);
			marshaller.marshal(getConfiguration(), path.toFile());
		} catch (JAXBException e) {
			e.printStackTrace();
			Dialogs.errorAlert(e.getMessage(), this);
			closeProgram();
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
		Platform.exit();

	}

	/**
	 * @return the selectionModel
	 */
	public SelectionModel getSelectionModel() {
		return this.selectionModel;
	}

}
