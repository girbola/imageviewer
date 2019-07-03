package com.girbola.imageviewer.imageviewer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;

public class Model_ImageViewer {

	final private double width = 100;

	private ScrollPane scrollPane;
	private RenderVisibleNode renderVisibleNode;
	private I18NSupport i18nSupport;
	private Configuration configuration = new Configuration();
	private Dialogs dialogs;

	public Model_ImageViewer() {
		loadConfig();
		i18nSupport = new I18NSupport(configuration.getLanguage(), configuration.getCountry());
		//		dialogs = new Dialogs(this);
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

	public Path getConfigPath() {
		Path path = Paths.get("." + File.separator + "config.dat");
		return path;
	}

	public void saveConfig() {
		Path path = getConfigPath();
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

	private void loadConfig() {
		Path path = getConfigPath();
		System.out.println("loading config: " + path);
		if (!Files.exists(path)) {
			System.out.println("Config file doesn't exists: " + path);
			return;
		}
		try {
			JAXBContext jabx = JAXBContext.newInstance(Configuration.class);
			Unmarshaller unmarshaller = jabx.createUnmarshaller();
			Configuration configuration = (Configuration) unmarshaller.unmarshal(path.toFile());
			setConfiguration(configuration);
		} catch (JAXBException e) {
			e.printStackTrace();
			Dialogs.errorAlert(e.getMessage(), this);
			closeProgram();
		}

	}

	public void closeProgram() {
		getRenderVisibleNode().terminateAllBackgroundTasks();
		saveConfig();
		Platform.exit();

	}

}
