package imageviewer;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18NSupport {

	private ResourceBundle bundle;
	private Locale locale;

	// private String country = "EN";
	// private String lang = "en";

	public I18NSupport(String lang, String country) {
		locale = new Locale(lang, country);
		bundle = ResourceBundle.getBundle("bundle/lang", locale);
	}

	public ResourceBundle getBundle() {
		return bundle;
	}

	public void setBundle(ResourceBundle bundle) {
		this.bundle = bundle;

	}
}

