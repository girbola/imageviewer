package com.girbola.imageviewer.imageviewer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

@XmlRootElement(name = "config")
public class Configuration {

	/*
	 * Language.ENGLISH.getType().toLowerCase()
	 */
	private String language = Language.ENGLISH.getType().toLowerCase();
	private String country = Language.ENGLISH.getType();
	private SimpleStringProperty vlcPath = new SimpleStringProperty("");
	private SimpleBooleanProperty vlcSupported = new SimpleBooleanProperty(false);
	private final Path appDataPath = Paths.get(System.getenv("APPDATA") + File.separator + "imageviewer");
	private final Path configDataPath = Paths.get(appDataPath.toString() + File.separator + "config.dat");

	public String getCountry() {
		return country;
	}

	@XmlElement
	public void setCountry(String country) {
		this.country = country;
	}

	public String getLanguage() {
		return language;
	}

	@XmlElement
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the vlcPath
	 */
	public SimpleStringProperty vlcPath_property() {
		return this.vlcPath;
	}

	/**
	 * @return the vlcPath
	 */
	public String getVlcPath() {
		return this.vlcPath.get();
	}

	/**
	 * @param vlcPath the vlcPath to set
	 */
	public void setVlcPath(String vlcPath) {
		this.vlcPath.set(vlcPath);
	}

	/**
	 * @return the vlcSupported
	 */
	public SimpleBooleanProperty isVLCSupported_property() {
		return vlcSupported;
	}

	/**
	 * @return the vlcSupported
	 */
	public boolean isVLCSupported() {
		return this.vlcSupported.get();
	}

	/**
	 * 
	 * @param vlcSupported
	 */
	public void setVLCSupported(boolean isVLCSupported) {
		this.vlcSupported.set(isVLCSupported);
	}

	/**
	 * @return the appDataPath
	 */
	public final Path getAppDataPath() {
		return appDataPath;
	}

	/**
	 * @return the configDataPath
	 */
	public final Path getConfigDataPath() {
		return configDataPath;
	}

}
