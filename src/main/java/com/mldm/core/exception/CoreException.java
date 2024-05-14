package com.mldm.core.exception;

import com.mldm.rest.common.ApiException.MTYPE;

public class CoreException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MTYPE msgType;
	public String msg;
	public boolean isShowMsg;
	
	
	 
	
	public CoreException(MTYPE msgType, String msg) {
		super(msg);
		this.msgType = msgType;
		this.msg = msg;
		this.isShowMsg = true;
	}
	
	public CoreException(Exception ex){
		super(ex);
		this.msgType = MTYPE.ERROR;
		this.msg = ex.toString();
		this.isShowMsg = true;
	}

	public MTYPE getMsgType() {
		return msgType;
	}

	public String getMsg() {
		return msg;
	}

	public boolean isShowMsg() {
		return isShowMsg;
	}

	@Override
	public String toString() {
		return msg;
	}

}
