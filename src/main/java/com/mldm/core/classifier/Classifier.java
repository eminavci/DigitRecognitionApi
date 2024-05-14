package com.mldm.core.classifier;

import java.util.List;
import java.util.Map;

import com.mldm.core.utl.FccData;

public interface Classifier {

	void classify(String freemanCode);
	List<FccData> getFirstK();
	double getStrength();
	int getPredictedClass();
	Map<Integer, Integer> getNN();
	
}
