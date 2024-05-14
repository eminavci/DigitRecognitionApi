package com.mldm.rest.services;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mldm.core.utl.FccData;
import com.mldm.rest.common.ApiException;

public interface DataService {

	FccData computeFreeman(String base64ImgStr) throws Exception;
	Map<String, Object> saveData(String reducedBase64ImgStr, int lblClass, String fName) throws Exception;
	List<FccData> getAllFccCodes(int isTraining, boolean isPrototype);
	List<FccData> getAllFccData(int isTraining, boolean isPrototype);
	void deleteFccData(String id) throws ApiException;
	FccData findFccDataById(String id);
	List<FccData> findFccDataByIds(List<ObjectId> oids);
	List<FccData> redeuceDataSet() throws Exception;
	List<FccData> getNumberOfSamples();
	
	FccData tuneK(boolean reCompute) throws Exception;
	FccData computeFrequentPatterns(); 
	List<FccData> getFrequentPatterns(int lblClass);
	FccData testAccuracy(boolean reComputer) throws Exception;
	void createTestTrainSet(boolean onlyPrint) throws Exception;
}
