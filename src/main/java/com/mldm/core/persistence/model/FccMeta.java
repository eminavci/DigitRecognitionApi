package com.mldm.core.persistence.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;

public class FccMeta implements Serializable{

	private String id;
	private String fcc;
	private String drawedImg;
	private String contourImg;// this is smaller 
	private int lblClass;
	private int howFar;
	private FccMeta() {}
	
	public FccMeta(String fcc, int lblClass, byte[] drawedImg, byte[] contourImg) throws IOException {
		super();
		this.fcc = fcc;
		setDrawedImg(drawedImg);
		setContourImg(contourImg);
		this.lblClass = lblClass;
	}
	
	public FccMeta(String fcc, int lblClass, String drawedImg, String contourImg) {
		super();
		this.lblClass = lblClass;
		this.fcc = fcc;
		this.drawedImg = drawedImg;
		this.contourImg = contourImg;
	}
	
	public FccMeta(String id, String fcc, String drawedImg, String contourImg) {
		super();
		this.id = id;
		this.fcc = fcc;
		this.drawedImg = drawedImg;
		this.contourImg = contourImg;
	}
	
	public FccMeta(String fcc, String drawedImg, String contourImg) {
		super();
		this.fcc = fcc;
		this.drawedImg = drawedImg;
		this.contourImg = contourImg;
	}
	
	public String getId() {
		return id;
	}
	public FccMeta setId(String id) {
		this.id = id;
		return this;
	}
	public String getFcc() {
		return fcc;
	}
	public void setFcc(String fcc) {
		this.fcc = fcc;
	}
	public String getDrawedImg() {
		return drawedImg;
	}
	public int getLblClass() {
		return lblClass;
	}
	public void setLblClass(int lblClass) {
		this.lblClass = lblClass;
	}
	public int getHowFar() {
		return howFar;
	}
	public void setHowFar(int howFar) {
		this.howFar = howFar;
	}

//	public Binary getDrawedImgBinary() throws IOException {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		ImageIO.write(getDrawedImg(), "png", baos);
//		return new Binary(baos.toByteArray());
//	}
	
	public void setDrawedImg(String drawedImg) {
		this.drawedImg = drawedImg;
	}
	public void setDrawedImg(byte[] drawedImg) throws IOException {
		this.drawedImg = Base64.getEncoder().encodeToString(drawedImg);
	}
	public String getContourImg() {
		return contourImg;
	}
	
//	public Binary getContourImgBinary() throws IOException {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		ImageIO.write(getContourImg(), "png", baos);
//		return new Binary(baos.toByteArray());
//	}
	
	public void setContourImg(String contourImg) {
		this.contourImg = contourImg;
	}
	
	public void setContourImg(byte[] contourImg) throws IOException {
		this.contourImg = Base64.getEncoder().encodeToString(contourImg);
	}
}
