package com.girbola.imageviewer.imageviewer;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VideoPreview {
	private List<BufferedImage> buff_list;
	private ImageView iv;
	private AtomicInteger index = new AtomicInteger(0);

	public VideoPreview(List<BufferedImage> buff_list, ImageView iv) {
		System.out.println("videopreview loading");
		this.buff_list = buff_list;
		this.iv = iv;
		for (int i = 0; i < buff_list.size(); i++) {
			BufferedImage buff = buff_list.get(i);
			System.out.println("width: " + buff);
		}
	}

	public BufferedImage showNextImage() {
		index.getAndIncrement();
		System.out.println("showNextImage: " + index.get() + " buff size: "+buff_list.size());
		if (index.get() > (buff_list.size() - 1)) {
			index.set(0);
		}
		System.out.println("index is: " + index.get());
		BufferedImage buff = buff_list.get(index.get());
		return buff;
	}

	public Image getImage(int index) {
		Image image = SwingFXUtils.toFXImage(buff_list.get(index), null);
		return image;
	}

}
