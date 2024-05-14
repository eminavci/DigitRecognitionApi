package com.mldm.core.classifier.cnn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mldm.core.classifier.Classifier;
import com.mldm.core.utl.FccData;
import com.mldm.core.utl.KEY;
import com.mldm.core.utl.Util;
import com.mldm.rest.common.ApiException;

/**
 * Condense and remove outliers from training set
 * Datapoints in the training set are divided into three types
 * 1. Outliers: points which would not be recognised as the correct type if added to the database later
 * 2. Prototypes: the minimum set of points required in the training set for all the other non-outlier points to be correctly recognised
 * 3. Absorbed points: points which are not outliers, and would be correctly recognised based just on the set of prototype points
 * 
 * At the end, our training set will be consisted of only Prototypes
 * @author avci
 *
 */
public class CondenseNearestNeighbor {
	
	private static Logger logger = LoggerFactory.getLogger(CondenseNearestNeighbor.class);
	public List<FccData> prototypes = new ArrayList<>();
	/** here we only remove outliers.
	 * @param fccs
	 * @return
	 * @throws ApiException
	 */
	public List<FccData> reduceDataSet(List<FccData> fccs)  throws ApiException{
		if(fccs.size() < 100)
			throw new ApiException("To condense training set, there must be at least 100 samples!");
		
		List<FccData> deletedsFinal = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			List<FccData> set1 = new ArrayList<>(fccs.subList(0, fccs.size()/2));
			List<FccData> set2 = new ArrayList<>(fccs.subList(fccs.size()/2, fccs.size()));
			Iterator<FccData> iter1 = set1.iterator();
			Iterator<FccData> iter2 = set2.iterator();
			List<FccData> deleteds = new ArrayList<>();
			
			while(iter1.hasNext() || iter2.hasNext()){
				try {
					if(iter1.hasNext()){
						FccData fcc = iter1.next();
						List<FccData> trainingSet = new ArrayList<>(set2);
						Classifier knn = new KNearestNeigbors(1, trainingSet);
						knn.classify(fcc.getString(KEY.fcc.name()));
						if(knn.getPredictedClass() != fcc.getInteger(KEY.lblClass.name())){
							deleteds.add(fcc);
							iter1.remove();
						}
					}
					
					if(iter2.hasNext()){
						FccData fcc = iter2.next();
						List<FccData> trainingSet = new ArrayList<>(set1);
						Classifier knn = new KNearestNeigbors(1, trainingSet);
						knn.classify(fcc.getString(KEY.fcc.name()));
						if(knn.getPredictedClass() != fcc.getInteger(KEY.lblClass.name())){
							deleteds.add(fcc);
							iter2.remove();
						}
					}	
				} catch (Exception e) {
					logger.error("Reduce error : " + e);
					e.printStackTrace();
				}
			}
			deletedsFinal.addAll(deleteds);
		}
		return deletedsFinal;
	}

	/** it returns the deleted items
	 * @param fccs
	 * @return
	 * @throws ApiException
	 */
	public List<FccData> condenseDataSet(List<FccData> fccs) throws ApiException{
		if(fccs.size() < 100)
			throw new ApiException("To condense training set, there must be at least 100 samples!");
		List<FccData> absorbed = new ArrayList<>();
		
		prototypes.add(fccs.get(Util.rand(fccs.size())));
		for (Iterator<FccData> it = fccs.iterator(); it.hasNext();) {
			FccData fccd = it.next();
			try {
				Classifier knn = new KNearestNeigbors(1, prototypes);
				knn.classify(fccd.getString(KEY.fcc.name()));
				if(knn.getPredictedClass() == fccd.getInteger(KEY.lblClass.name()))
					absorbed.add(fccd);
				else
					prototypes.add(fccd);
				it.remove();
			} catch (Exception e) {
				logger.error("Condence error : " + e);
			}
			
		}
		return absorbed;
	}
}
