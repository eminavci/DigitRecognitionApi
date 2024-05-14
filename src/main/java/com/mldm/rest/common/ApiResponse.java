package com.mldm.rest.common;

import java.util.List;
import java.util.Map;
import com.mldm.core.utl.FccData;
import com.mldm.core.utl.KEY;

public class ApiResponse extends FccData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/** data object is added to response as the content
	 * @param status
	 * @param data
	 */
	public ApiResponse(STATUS status, Map<String, Object> data) {
		this.put("status", status.getResStatus());
		this.putAll(data);
	}
	
	public ApiResponse(STATUS status, List<FccData> data) {
		this.put("status", status.getResStatus());
		put(KEY.list, data);
		
	}
	
	/** data objects is added to response as a "list" object
	 * @param data
	 * @param status
	 */
	public ApiResponse(FccData data, STATUS status) {
		this.put("status", status.getResStatus());
		put(KEY.list, data);
		
	}
	
	public ApiResponse() {
		super();
		this.put("status", STATUS.SUCCESS);
	}

	public STATUS getStatus(){
		return STATUS.valueOf(this.getInteger("status"));
	}
	
	public enum STATUS{
		SUCCESS(1),
		ERROR(0);
		public int resStatus;
		
		STATUS(int resStatus) {
			this.resStatus = resStatus;
		}
		public int getResStatus() {
			return resStatus;
		}
		
		public static STATUS valueOf(int status) {
	        for (STATUS s : STATUS.values()) {
	            if (s.resStatus == status) return s;
	        }
			return STATUS.ERROR;   
	    }
	}
	
	public ApiResponse put(String key, Object obj){
		super.put(key, obj);
		return this;
	}
	
	public String getString(String key){
		return this.get(key).toString();
	}
	
	public String getString(String key, String deffault){
		return this.getString(key) == null ? deffault : this.getString(key);
	}
	
	public int getInteger(String key){
		return Integer.valueOf(this.get(key).toString());
	}
	
	public int getInteger(String key, int deffault){
		return this.getInteger(key) == 0 ? deffault : this.getInteger(key);
	}
}
