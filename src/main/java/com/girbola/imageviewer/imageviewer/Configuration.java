package com.girbola.imageviewer.imageviewer;

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
	private SimpleBooleanProperty isVLCSupported = new SimpleBooleanProperty(false);

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
	 * @return the isVLCSupported
	 */
	public SimpleBooleanProperty isVLCSupported_property() {
		return isVLCSupported;
	}

	/**
	 * @return the isVLCSupported
	 */
	public boolean getIsVLCSupported() {
		return this.isVLCSupported.get();
	}

	/**
	 * 
	 * @param isVLCSupported
	 */
	public void setVLCSupported(boolean isVLCSupported) {
		this.isVLCSupported.set(isVLCSupported);
	}

}
