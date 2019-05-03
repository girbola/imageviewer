package com.girbola.imageviewer.imageviewer;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18NSupport {

    private ResourceBundle bundle;
    private Locale locale;

    public I18NSupport(String lang, String country) {
	System.out.println("lang: " + lang + " country: " + country);
	locale = new Locale(lang, country);
	bundle = ResourceBundle.getBundle("com/girbola/imageviewer/bundle/lang", locale);
    }

    public ResourceBundle getBundle() {
	return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
	this.bundle = bundle;

    }
}
