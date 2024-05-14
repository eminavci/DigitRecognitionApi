package com.mldm.core.utl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
	
	private static final Logger logger = LoggerFactory.getLogger(Util.class);
	
	public static final File desktop = new File(System.getProperty("user.home"), "Desktop");
	public static final File data = new File(desktop, "resources" + File.separator + "data");
	private static final Random r = new Random();
	
	static{
		if(!data.exists()){
			if(!data.getParentFile().exists())
				data.getParentFile().mkdirs();
			data.mkdirs();
		}
	}
	
	public static String saveOrigImg(String lblClass, BufferedImage bImage) throws IOException{
		File f = new File(Util.data + File.separator + lblClass);
		if(!f.exists())
			f.mkdirs();
		File outputfile = File.createTempFile("hrdr_", ".png", f);
		try {
			ImageIO.write(bImage, "png", outputfile);
		} catch (IOException e1) {
			logger.error("File written Error", e1);
		}
		return outputfile.getName();
	}
	
	public static void deleteOrigFiles(List<String> fNames){
		Runnable deleteTask = () -> {
			try {
				for (String fn : fNames)
					if(!new File(Util.data + File.separator + fn).delete())
						logger.error("Deleting file " + fn + " failed");
			} catch (Exception e) {}
		};
		deleteTask.run();
	}
	
	public static void deleteOrigFilesWithFcc(List<FccData> fccs){
		Runnable deleteTask = () -> {
			
			List<String> fNames = new ArrayList<>();
			for (FccData fccData : fccs) {
				String fn = fccData.getInteger(KEY.lblClass)+ File.separator + fccData.getString(KEY.fName);
				if(!new File(Util.data + File.separator + fn).delete())
					logger.error("Deleting file " + fn + " failed");
			}
		};
		deleteTask.run();
	}
	
	public static byte[] decode(String str){
		return Base64.getDecoder().decode(str);
	}
	
	public static String encode(byte[] bytes){
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public static byte[] bufferedImageToByteArray(BufferedImage bufImage) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(bufImage, "jpg", baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		return imageInByte;
	}
	
	public static BufferedImage byteArrayToBufferedImage(byte[] bytes) throws IOException{
		return ImageIO.read(new ByteArrayInputStream(bytes));
	}
	
	public static String encodeBufferedImage(BufferedImage bufImg) throws IOException{
		return encode(bufferedImageToByteArray(bufImg));
	}
	public static int rand(int i){
		return r.nextInt(i);
	}
}
