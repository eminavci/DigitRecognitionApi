package com.mldm.core.persistence.model;

import java.io.Serializable;

public class Fcc implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String fcc;
	private int lblClass;
	
	private int howFar;
	
	public Fcc(String id, String fcc, int lblClass) {
		super();
		this.id = id;
		this.fcc = fcc;
		this.lblClass = lblClass;
	}


	public Fcc(String fcc, int lblClass) {
		super();
		this.fcc = fcc;
		this.lblClass = lblClass;
	}
	

	public String getFcc() {
		return fcc;
	}
	public void setFcc(String fcc) {
		this.fcc = fcc;
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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}


	@Override
	public String toString() {
		return "Fcc [id=" + id + ", lblClass=" + lblClass + "]";
	}
}
