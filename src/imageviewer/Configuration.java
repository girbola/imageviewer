package imageviewer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
public class Configuration {
	private String language;
	private String country;

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

}
