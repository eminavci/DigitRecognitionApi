package com.mldm.core.classifier.cnn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.mldm.core.classifier.Classifier;
import com.mldm.core.exception.CoreException;
import com.mldm.core.metric.Distance;
import com.mldm.core.metric.EditDistance;
import com.mldm.core.utl.FccData;
import com.mldm.core.utl.KEY;
import com.mldm.rest.common.ApiException.MTYPE;

public class KNearestNeigbors implements Classifier{

	private List<FccData> fccs;
	private int k;
	private Distance distance;
	private Map<Integer, Integer> classess = new HashMap<>();
	
	public KNearestNeigbors(int k, List<FccData> fccs) throws CoreException {
		if(fccs == null || fccs.size() < k)
			throw new CoreException(MTYPE.ERROR, "Training Set is empty or \n there are less samples than " + k);
		this.k = k;
		this.fccs = fccs;
		this.distance = new EditDistance();
	}
	
	public enum deneme{
		cano, kano;
	}
	
	@Override
	public void classify(String freemanCode) {

		fccs.forEach(fcc -> {
			fcc.put("howFar", (int)distance.distance(freemanCode, fcc.get("fcc").toString()));
		});
		
		fccs.sort((Map<String, Object> o1, Map<String, Object> o2) -> Integer.valueOf(o1.get("howFar").toString()) - Integer.valueOf(o2.get("howFar").toString()));
		
		//fccs.forEach(fcc -> System.out.println(fcc.getLblClass() + " " + fcc.getHowFar()));

		Iterator<FccData> it = getFirstK().iterator();		
		while (it.hasNext()) {
			FccData f = it.next();
			if(classess.containsKey(f.getInteger(KEY.lblClass))){
				classess.put(f.getInteger(KEY.lblClass), classess.get(f.getInteger(KEY.lblClass)) + 1);
			} else {
				classess.put(f.getInteger(KEY.lblClass), 1);
			}
		}
	}

	@Override
	public List<FccData> getFirstK() {
		return this.fccs.subList(0, k);
	}

	@Override
	public double getStrength() {

		return (classess.get(getPredictedClass())/(double)k)*100.0;
	}

	@Override
	public int getPredictedClass() {
		int strength = 0;
		int prClass=-1;
		for (Map.Entry<Integer,Integer> entry : classess.entrySet()) {
			if(entry.getValue() > strength){
				prClass  = entry.getKey();
				strength = entry.getValue();
			}
		}
		return prClass;
	}

	@Override
	public Map<Integer, Integer> getNN() {
		return this.classess;
	}


	
}
