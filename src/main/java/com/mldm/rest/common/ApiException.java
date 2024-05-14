package com.mldm.rest.common;

import java.util.HashMap;
import java.util.Map;

public class ApiException extends Exception{

	private String errMsg;
	private MTYPE msgType;
	
	public ApiException() {
		super();
		this.errMsg = "General Error";
	}
	
	public ApiException(String errMsg) {
		super(errMsg);
		this.errMsg = errMsg;
		this.msgType = MTYPE.ERROR;
	}
	
	public ApiException(String errMsg, MTYPE msgType) {
		super(errMsg);
		this.errMsg = errMsg;
		this.msgType = msgType;
	}
	
	public ApiException(Throwable th) {
		super(th);
		if(th.getMessage() == null)
			this.errMsg = th+"";
		else
			this.errMsg = th.getMessage();
		this.msgType = MTYPE.ERROR;
	}

	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
	public Map<String, Object> getAsResponse() {
		Map<String, Object> job = new HashMap<>();
		job.put("errMsg", this.errMsg);
		job.put("msgType", this.msgType.getMsgType());
		return job;
	}
	
	public enum MTYPE{
		INFO(1),
		WARN(2),
		ERROR(0);
		
		public int msgType;
		
		private MTYPE(int msgType) {
			this.msgType = msgType;
		}

		public int getMsgType() {
			return msgType;
		}
	}
}
