package com.mldm.rest.controllers;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.mldm.core.classifier.Classifier;
import com.mldm.core.classifier.cnn.KNearestNeigbors;
import com.mldm.core.utl.FccData;
import com.mldm.core.utl.KEY;
import com.mldm.core.utl.Util;
import com.mldm.rest.common.ApiException;
import com.mldm.rest.common.ApiResponse;
import com.mldm.rest.common.ApiResponse.STATUS;
import com.mldm.rest.services.DataService;

@RestController
@RequestMapping(value = "/hwdr-api", produces = "application/json; charset=UTF-8")
public class AppController {

	private Logger logger = LoggerFactory.getLogger(AppController.class);
	
	@Autowired
	DataService dataService;
	
	@RequestMapping(value = "/createtestset")
	public ResponseEntity<ApiResponse> createtestset(@RequestParam(value="onlyPrint", defaultValue = "true") boolean onlyPrint) throws Exception{
		dataService.createTestTrainSet(onlyPrint);
		return new ResponseEntity<ApiResponse>(new ApiResponse(), HttpStatus.OK);
	}
			
	
	@RequestMapping(value = "/testacc")
	public ResponseEntity<ApiResponse> testAccuracy(@RequestParam(value="recompute",required=true) boolean recompute) throws Exception{
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, dataService.testAccuracy(recompute)), HttpStatus.OK);
	}
	
	@RequestMapping(value="/addToTrainingSet",method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<ApiResponse> addToTrainingSet(@RequestBody FccData data) throws Exception{

		String fName = "none";
		try {
			BufferedImage bi = Util.byteArrayToBufferedImage(Util.decode(data.getString(KEY.drawedBase64ImgStr)));
			
			fName = Util.saveOrigImg(data.getInteger("lblClass")+"", bi);
		} catch (Exception e) {logger.error("Could not save the image file : " + e.getMessage());}
		
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, 
				dataService.saveData(data.getString("reducedBase64ImgStr"), data.getInteger("lblClass"), fName)), 
				HttpStatus.OK);
	}
	
	
	@RequestMapping(value="/computeFreeman",method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<ApiResponse> computeFreeman(@RequestBody String base64ImgStr, HttpServletRequest request) throws Exception{

		if(base64ImgStr == null || base64ImgStr.length()<10)
			throw new ApiException("The image encoded format is broken or missing!");
		
		if(base64ImgStr.contains(","))
			base64ImgStr = base64ImgStr.split(",")[1];
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, dataService.computeFreeman(base64ImgStr)), HttpStatus.OK);
    }
	
	@RequestMapping(value="/getAllFccData",method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<ApiResponse> findAllFccData() throws Exception{
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, dataService.getAllFccData(1, false)), HttpStatus.OK);
	}
	
	@RequestMapping(value="/deleteFccData/{id}",method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<ApiResponse> deleteFccData(@PathVariable String id) throws Exception{
		dataService.deleteFccData(id);
		return new ResponseEntity<ApiResponse>(new ApiResponse(), HttpStatus.OK);	
	}
	
	@RequestMapping(value="/classify", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<ApiResponse> classify(@RequestBody FccData data) throws Exception{
		int k = data.getInteger(KEY.K.name());
		if(k > 50)
			throw new ApiException("K" + k  + " is too big!!");
		boolean isPrototype = false;
		if(k == 1)
			isPrototype = true;
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(6); 
		
		ArrayList<String> fcc = (ArrayList<String>) data.get("fccs");
		long startT = System.currentTimeMillis();
		Classifier classifier = new KNearestNeigbors(k, dataService.getAllFccCodes(1, isPrototype));
		classifier.classify(fcc.get(0));
		data = new FccData();
		data.put("exec_time", df.format((System.currentTimeMillis() - startT)/1000f));
		data.put(KEY.strength, classifier.getStrength());
		data.put(KEY.predictedClass, classifier.getPredictedClass());
		List<FccData> nearestFccs = classifier.getFirstK();
		
		List<ObjectId> oids = new ArrayList<ObjectId>();
		nearestFccs.forEach(fccD -> oids.add(new ObjectId(fccD.getString(KEY.id))));
		
		List<FccData> nearestFccs2 = dataService.findFccDataByIds(oids);
		for (int i = 0; i < nearestFccs.size(); i++) {
			for (int j = 0; j < nearestFccs2.size(); j++) {
				if(nearestFccs.get(i).get(KEY.id.name()).equals(nearestFccs2.get(j).get(KEY.id.name()))){
					nearestFccs.get(i).putAll(nearestFccs2.get(j));
					break;
				}
			}
		}
		data.put(KEY.nearestFccs, nearestFccs);
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, data), HttpStatus.OK);
	}
	
	@RequestMapping(value="/condense", method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<ApiResponse> condenseDataSet() throws Exception{
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, dataService.redeuceDataSet()), HttpStatus.OK);
	}
	
	@RequestMapping(value="/numberofsamples", method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<ApiResponse> getNumberOfSamples() throws Exception{
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, dataService.getNumberOfSamples()), HttpStatus.OK);
	}
	
	@RequestMapping(value="/learnk", method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<ApiResponse> learnK(@RequestParam(value="recompute",required=true) boolean recompute) throws Exception{
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, dataService.tuneK(recompute)), HttpStatus.OK);
	}
	
	@RequestMapping(value="/computefrequentpatterns", method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<ApiResponse> computeFrequentPatterns() throws Exception{
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, dataService.computeFrequentPatterns()), HttpStatus.OK);
	}
	
	@RequestMapping(value="/getfrequentpatterns", method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<ApiResponse> getFrequentPatterns(@RequestParam(value="lblClass",required=true) int lblClass) throws Exception{
		return new ResponseEntity<ApiResponse>(new ApiResponse(STATUS.SUCCESS, dataService.getFrequentPatterns(lblClass)), HttpStatus.OK);
	}
	
}















//try
//{
//  //This will decode the String which is encoded by using Base64 class
//  byte[] imageByte=Base64.getDecoder().decode(base64ImgStr);
//  
//  String directory="/home/avci/Desktop/sample.png";
//  
//  FileOutputStream fos = new FileOutputStream(directory);
//  fos.write(imageByte);
//  fos.close();
//  return "success ";
//}
//catch(Exception e)
//{
//  return "error = "+e;
//}
