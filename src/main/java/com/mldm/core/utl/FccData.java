package com.mldm.core.utl;

import java.util.HashMap;
import java.util.Map;

public class FccData extends HashMap<String, Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public FccData() {
		super();
	}
	
	public FccData(Map<String, Object> map) {
		super(map);
	}
	
	public Object put(KEY key, Object value) {
		return super.put(key.toString(), value);
	}

	public String getString(String key){
		return get(key).toString();
	}
	
	public String getString(String key, String deffault){
		return this.getString(key) == null ? deffault : this.getString(key);
	}
	
	public int getInteger(String key){
		return Integer.valueOf(get(key).toString());
	}
	
	public int getInteger(String key, int deffault){
		return this.getInteger(key) == 0 ? deffault : this.getInteger(key);
	}
	
	public float getFloat(String key){
		return  Float.valueOf(get(key).toString());
	}
	
	public float getFloat(String key, float deffault){
		return this.getFloat(key) == 0 ? deffault : this.getFloat(key);
	}
	
	public String getString(KEY key){
		return get(key.toString()).toString();
	}
	
	public String getString(KEY key, String deffault){
		return this.getString(key) == null ? deffault : this.getString(key);
	}
	
	public int getInteger(KEY key){
		return Integer.valueOf(get(key.toString()).toString());
	}
	
	public int getInteger(KEY key, int deffault){
		return this.getInteger(key) == 0 ? deffault : this.getInteger(key);
	}

	@Override
	public String toString() {
		String s = "";
		for (Map.Entry<String, Object> map : this.entrySet()) {
			s += "[" + map.getKey() + " : " + map.getValue() + "]\n";
		}
		return s;
	}
	
}
