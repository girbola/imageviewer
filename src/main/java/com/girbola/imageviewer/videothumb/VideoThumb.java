package com.girbola.imageviewer.videothumb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.Demuxer;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.Format;
import org.jcodec.common.JCodecUtil;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import net.coobird.thumbnailator.Thumbnails;

public class VideoThumb {

	public static List<BufferedImage> getList(File file) {
		List<BufferedImage> list = new ArrayList<>();
		double duration = getVideoLenght(file);
		System.out.println("Total duration: " + duration);
		List<Double> listOfTimeLine = grabListOfTimeLine(duration);
		list = grab(file, listOfTimeLine);
		System.out.println("GetList done: " + list.size());
		return list;

	}

	private static List<BufferedImage> grab(File file, List<Double> list) {
		System.out.println("Grab started");
		List<BufferedImage> buff_list = new ArrayList<>();

		FrameGrab grab = null;
		try {
			grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
			grab.seekToSecondSloppy(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				double sec = list.get(i);
				System.out.println("Loooop Sec: " + sec);
				Picture picture = grab.getNativeFrame();
				System.out.println("sec: " + sec + "picture width: " + picture.getWidth() + "x" + "picture heigth: " + picture.getHeight() + " "
						+ picture.getColor());
				BufferedImage buff = AWTUtil.toBufferedImage(picture);
				buff = Thumbnails.of(buff).height(150).keepAspectRatio(true).asBufferedImage();

				System.out.println("buff height: " + buff.getHeight());
				if (buff != null) {
					System.out.println("Buff added");
					buff_list.add(buff);
					grab.seekToSecondSloppy(sec);
				}
				System.out.println("End: " + i);
			}
		} catch (IOException | JCodecException e) {
			e.printStackTrace();
		}
		System.out.println("Buff list returning");
		return buff_list;

	}

	private static List<Double> grabListOfTimeLine(double duration) {
		List<Double> list = new ArrayList<>();
		if (duration < 15) {
			double startPos = 0;
			double ratio = (duration / 5); //
			if (ratio < 1) {
				list.add(startPos);
				return list;
			}
			list.add(startPos);
			for (int i = 0; i < 1; i++) {
				startPos += ratio;
				list.add(startPos);
			}
		} else {
			double startPos = (duration / 10); // = 3. 10% from duration. This could avoid black screen as a start image
			double ratio = ((duration - startPos) / 5); // 
			System.out.println("startPos= " + startPos + " Ratio is: " + ratio);
			list.add(startPos);
			for (int i = 0; i < 4; i++) {
				startPos += ratio;
				list.add(startPos);
			}

		}
		System.out.println("List size at videoThumb is: " + list.size());
		return list;
	}

	public static double getVideoLenght(File file) {
		Format f = null;
		try {
			f = JCodecUtil.detectFormat(file);
			Demuxer d = JCodecUtil.createDemuxer(f, file);
			DemuxerTrack vt = d.getVideoTracks().get(0);
			DemuxerTrackMeta dtm = vt.getMeta();

			return dtm.getTotalDuration();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

}
