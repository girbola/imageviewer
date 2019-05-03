package com.girbola.imageviewer.common.utils;

/*
 @(#)Copyright:  Copyright (c) 2012-2018 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 */
import java.nio.file.Path;

public class FileUtils {

	
	private final static String[] SUPPORTED_VIDEO_FORMATS = { "3gp", "avi", "mov", "mp4", "mpg", "mkv" };
	private final static String[] SUPPORTED_IMAGE_FORMATS = { "png", "jpg", "jpeg", "gif", "bmp", "tiff", "tif" };
	private final static String[] SUPPORTED_RAW_FORMATS = { "cr2", "nef" };

	/* FILE FORMATS START */
	/**
	 * Checks if file supports image, raw or video formats
	 *
	 * @param path
	 * @return
	 */
	public static boolean supportedMediaFormat(Path path) {
		for (String s : SUPPORTED_VIDEO_FORMATS) {
			if (path.getFileName().toString().toLowerCase().endsWith(s.toLowerCase())) {
				return true;
			}
		}
		for (String s : SUPPORTED_IMAGE_FORMATS) {
			if (path.getFileName().toString().toLowerCase().endsWith(s.toLowerCase())) {
				return true;
			}
		}

		for (String s : SUPPORTED_RAW_FORMATS) {
			if (path.getFileName().toString().toLowerCase().endsWith(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean supportedImage(Path path) {
		for (String s : SUPPORTED_IMAGE_FORMATS) {
			if (path.getFileName().toString().toLowerCase().endsWith(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean supportedRaw(Path path) {
		for (String s : SUPPORTED_RAW_FORMATS) {
			if (path.getFileName().toString().toLowerCase().endsWith(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean supportedVideo(Path path) {
		for (String s : SUPPORTED_VIDEO_FORMATS) {
			if (path.getFileName().toString().toLowerCase().endsWith(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
