package com.mldm.rest.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.mldm.core.classifier.Classifier;
import com.mldm.core.classifier.cnn.CondenseNearestNeighbor;
import com.mldm.core.classifier.cnn.KNearestNeigbors;
import com.mldm.core.fccode.DrawingCharProcess;
import com.mldm.core.fccode.FreemanChainCode;
import com.mldm.core.patterns.FrequentSubset;
import com.mldm.core.persistence.MongoUtil;
import com.mldm.core.utl.Consts;
import com.mldm.core.utl.FccData;
import com.mldm.core.utl.KEY;
import com.mldm.core.utl.Util;
import com.mldm.rest.common.ApiException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Service
public class DataServiceImpl implements DataService{

	Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);

	@Autowired
	MongoUtil mongodb;

	@Override
	public FccData computeFreeman(String base64ImgStr) throws Exception {
		FccData map = new FccData();
	    try {
			DrawingCharProcess drProc = new DrawingCharProcess(Util.decode(base64ImgStr));
			drProc.compute();

			List<Map<String, Object>> listFcc = new ArrayList<>();
			for (FreemanChainCode fcc : drProc.getDetectedComponenets()) {
				FccData mapin = new FccData();
				mapin.put(KEY.fcc.name(), fcc.getFreemanCode().toString());
				mapin.put(KEY.drawedBase64ImgStr.name(), Util.encodeBufferedImage(fcc.getOrigImg()));
				mapin.put(KEY.counteredBase64ImgStr.name(), Util.encodeBufferedImage(fcc.getChainImg()));

				listFcc.add(mapin);
			}
			map.put(KEY.fccs.name(), listFcc);
			map.put(KEY.counteredBase64ImgStr.name(), drProc.getCounteredBase64ImgStr());
			map.put(KEY.drawedBase64ImgStr.name(), Util.encodeBufferedImage(drProc.getDrawedImg()));
			//map.put("reducedDrawedImg", Util.encodeBufferedImage(drProc.getReduceDrawedImgSize()));
		} catch (Exception e) {
			throw new ApiException(e);
		}
	    return map;
	}

	@Override
	public Map<String, Object> saveData(String reducedBase64ImgStr, int lblClass, String fName) throws Exception {
		MongoCollection<Document> col = mongodb.getHwdrDataTable();
		Document doc = new Document();

		try {
			DrawingCharProcess drProc = new DrawingCharProcess(Util.decode(reducedBase64ImgStr));
			drProc.compute();

			if(drProc.getDetectedComponenets().size() > 1)
				throw new ApiException("Currently Only 1 segmented Draw can be accepted");

			doc.put(KEY.lblClass.name(), lblClass);
			doc.put(KEY.counteredBase64ImgStr.name(), drProc.getCounteredBase64ImgStr());
			doc.put(KEY.drawedBase64ImgStr.name(), Util.encodeBufferedImage(drProc.getDrawedImg()));
			doc.put(KEY.fName.name(), fName);
			doc.put(KEY.fcc.name(), drProc.getDetectedComponenets().get(0).getFreemanCode().toString());//;
			doc.put(KEY.training.name(), 1);
			doc.put(KEY.isPrototype.name(), 0);
			col.insertOne(doc);

		} catch (Exception e) {
			throw new ApiException(e);
		}
		return new HashMap<>();
	}

	/* isTraining = 1 only training data
	 * isTraining = 0 only test data
	 * isTraining = -1 whole data (train + test)
	 * @see com.mldm.rest.services.DataService#getAllFccCodes(int)
	 */
	@Override
	public List<FccData> getAllFccCodes(int isTraining, boolean isPrototype) {
		MongoCollection<Document> col = mongodb.getHwdrDataTable();
		List<FccData> fccs = new ArrayList<>();
		Document qDoc = new Document();
		if(isTraining == 0 || isTraining == 1)
			qDoc.append(KEY.training.name(), isTraining);
		if(isPrototype)
			qDoc.append(KEY.isPrototype.name(), 1);

		FindIterable<Document> tt = col.find(qDoc).projection(new Document(KEY.fcc.name(), 1).append(KEY.lblClass.name(), 1).append(KEY.fName.name(), 1));
		for (Document doc : tt) {
			FccData fd = new FccData();
			fd.put(KEY.id.name(), doc.get("_id").toString());
			doc.remove("_id");
			fd.putAll(doc);
			fccs.add(fd);
		}
		return fccs;
	}

	@Override
	public List<FccData> getAllFccData(int isTraining, boolean isPrototype) {
		MongoCollection<Document> col = mongodb.getHwdrDataTable();
		List<FccData> fccs = new ArrayList<>();
		Document qDoc = new Document();
		if(isTraining == 0 || isTraining == 1)
			qDoc.append(KEY.training.name(), isTraining);
		if(isPrototype)
			qDoc.append(KEY.isPrototype.name(), 1);

		FindIterable<Document> tt = col.find(qDoc);

		for (Document doc : tt) {
			FccData fd = new FccData();
			fd.put(KEY.id, doc.get("_id").toString());
			doc.remove("_id");
			fd.putAll(doc);
			fccs.add(fd);
		}
		return fccs;
	}

	@Override
	public void deleteFccData(String id) throws ApiException {
		MongoCollection<Document> col = mongodb.getHwdrDataTable();
		Document doc = col.findOneAndDelete(new Document("_id", new ObjectId(id)));
		if(doc == null)
			throw new ApiException("Entry asked to be deleted could not be found!");
		try {
			Util.deleteOrigFiles(Arrays.asList(doc.getInteger(KEY.lblClass.name())+ File.separator + doc.getString(KEY.fName.name())));
		} catch (Exception e) {logger.error("Filename" + doc.getString(KEY.fName) + " could not be deleted!");}
	}

	@Override
	public FccData findFccDataById(String id) {
		MongoCollection<Document> col = mongodb.getHwdrDataTable();
		Document doc = col.find(new Document("_id", new ObjectId(id))).first();

		doc.append(KEY.id.name(), doc.get("_id").toString());
		doc.remove("_id");
		return new FccData(doc);
	}

	@Override
	public List<FccData> findFccDataByIds(List<ObjectId> oids) {
		MongoCollection<Document> col = mongodb.getHwdrDataTable();
		List<FccData> fccs = new ArrayList<>();

		Document query = new Document("_id", new Document("$in", oids));
		FindIterable<Document> tt = col.find(query).projection(new Document(KEY.counteredBase64ImgStr.name(), 1));

		for (Document doc : tt) {
			FccData fd = new FccData();
			fd.put(KEY.id, doc.get("_id").toString());
			doc.remove("_id");
			fd.putAll(doc);
			fccs.add(fd);
		}
		return fccs;
	}

	@Override
	public List<FccData> redeuceDataSet() throws ApiException {
		logger.debug("redeuceDataSet started...");
		MongoCollection<Document> col = mongodb.getHwdrDataTable();

		CondenseNearestNeighbor cnn = new CondenseNearestNeighbor();
		List<FccData> fccs = getAllFccCodes(1, false);
		Collections.shuffle(fccs);
		List<FccData> outlierDeleteds = cnn.reduceDataSet(fccs);
		logger.debug(outlierDeleteds.size() + " samples were deleted as outlier");
		fccs.removeAll(outlierDeleteds);

		List<FccData> absorbedDeleteds = cnn.condenseDataSet(fccs);
		logger.debug(absorbedDeleteds.size() + " samples were deleted as absorbed");

		List<FccData> deleteds = new ArrayList<>(outlierDeleteds);
		deleteds.addAll(absorbedDeleteds);

		List<ObjectId> oids = new ArrayList<ObjectId>();
		deleteds.forEach(fccD -> oids.add(new ObjectId(fccD.getString(KEY.id))));
		logger.info("DELETEDS SIZE : " + oids.size());
//		col.deleteMany(new Document("_id", new Document("$in", oids)));
//		Util.deleteOrigFilesWithFcc(deleteds);

		col.updateMany(new Document(), new Document("$set", new Document("isPrototype", 0)));

		Document query = new Document("_id", new Document("$nin", oids));
		query.append(KEY.training.name(), 1);

		col.updateMany(query, new Document("$set", new Document("isPrototype", 1)));

		return deleteds;
	}

	@Override
	public List<FccData> getNumberOfSamples() {
		MongoCollection<Document> col = mongodb.getHwdrDataTable();

		Document groupField = new Document("_id", "$lblClass");
		groupField.append("count", new Document("$sum", 1));

		Document group = new Document("$group", groupField);

		// use this to sort them
		Document sortFields = new Document("_id", 1);// -1 koyarsan tersi sıralıyor
		Document sort = new Document("$sort", sortFields );

		AggregateIterable<Document> tt = col.aggregate(Arrays.asList(group, sort));
		List<FccData> fc = new ArrayList<>();
		for (Document doc : tt) {
			FccData d = new FccData();
			d.put("label", doc.get("_id")+"");
			d.put("value", doc.getInteger("count"));
			fc.add(d);

		}

		return fc;
	}

	/* Before tuning Best K, I reduce (remove outliers) from raining set
	 * (non-Javadoc)
	 * @see com.mldm.rest.services.DataService#tuneK(boolean)
	 */
	@Override
	public FccData tuneK(boolean reCompute) throws Exception {

		MongoCollection<Document> col = mongodb.getTable(Consts.K_HISTORY_TABLE);
		FccData resData = new FccData();

		if(!reCompute){
			Document doc = col.find().first();
			if(doc != null){
				doc.remove("_id");
				resData.putAll(doc);
				return resData;
			}
		}

		List<FccData> fccs = getAllFccCodes(1, false);
		if(fccs.size() < 15)
			return resData;
		long startT = System.currentTimeMillis();
		FccData data = new FccData();
		//List<FccData> data = new ArrayList<>();
		int nbFold = 10;
		int kRange = 10;

		for (int i = 1; i <= kRange; i++){
			data.put(i+"", 0.0);
			data.put(i+"a", 0.0);
		}

		CondenseNearestNeighbor cnn = new CondenseNearestNeighbor();
		Collections.shuffle(fccs);
		/*List<FccData> outlierDeleteds = cnn.reduceDataSet(fccs);
		logger.debug(outlierDeleteds.size() + " samples were deleted as outlier before k-fold validation");
		fccs.removeAll(outlierDeleteds);*/

		int hm = fccs.size()/nbFold;
		float min = 10000000f, min2 = 10000000f;
		float max = 0f, max2 = 0f;
		for (int i = 0; i < nbFold; i++) {
			int fromIndex = i*hm;
			int toIndex = i == nbFold - 1 ? (i+1)*hm + fccs.size()%nbFold : (i+1)*hm;

			List<FccData> validationSet = new ArrayList<>();
			List<FccData> trainSet = new ArrayList<>();

			validationSet.addAll(fccs.subList(fromIndex, toIndex));
			trainSet.addAll(fccs.subList(0, fromIndex));
			trainSet.addAll(fccs.subList(toIndex, fccs.size()));

			for (int k = 1; k <= kRange; k++) {
				float errCount = 0;
				float errCount2 = 0;
				for (FccData fccData : validationSet) {
					Classifier knn = new KNearestNeigbors(k, trainSet);
					knn.classify(fccData.getString(KEY.fcc.name()));
					// TODO maybe better to use prediction accuracy: strength
					if(fccData.getInteger(KEY.lblClass.name()) != knn.getPredictedClass()){
						errCount = errCount + 1f;
						Map<Integer, Integer> nns = knn.getNN();
						int corPredCount = 0;
						try {corPredCount = nns.get(fccData.getInteger(KEY.lblClass.name()));} catch (Exception e) {}
						errCount2 = (k-corPredCount)/Float.valueOf(k);
						//errCount2 = (float) (errCount2 + (1- (knn.getStrength()/100.0)));
					}
				}

				data.put(k+"a", Float.parseFloat(data.get(k+"a").toString()) + (errCount2/(float)nbFold));
				data.put(k+"", Float.parseFloat(data.get(k+"").toString()) + (errCount/(float)nbFold));
			}
		}
		// For normalization, better plot
		for (Map.Entry<String, Object> map : data.entrySet()) {
			Float vval = Float.valueOf(map.getValue().toString());
			if(!map.getKey().contains("a"))
				if(vval < min)
					min = vval;
				if(vval > max)
					max = vval;
			else
				if(vval < min2)
					min2 = vval;
				if(vval > max2)
					max2 = vval;
		}
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(6);
		String bestK = "-1";
		float bestCost = 100000000;
		List<FccData> kHistory = new ArrayList<>();
		for (Map.Entry<String, Object> map : data.entrySet()) {
			if(!map.getKey().contains("a")){
				float errVal = Float.valueOf(map.getValue().toString())/max;

				if(errVal < bestCost){
					bestCost = errVal;
					bestK = map.getKey();
				}
				FccData fc = new FccData();

				fc.put("label", map.getKey());
				fc.put("value", Float.valueOf(df.format(errVal)));
				fc.put("value2", Float.valueOf(df.format(Float.valueOf(data.get(map.getKey()+"a").toString())/max2)));
				kHistory.add(fc);
			}
		}

		resData.put("khistory", kHistory);
		resData.put(KEY.K, Integer.valueOf(bestK));
		resData.put(KEY.error, String.format("%.2f", bestCost));
		resData.put("runTime", (System.currentTimeMillis() - startT)/1000.0);
		resData.put("nbFold", nbFold);
		resData.put("nbSamples", fccs.size());

		col.findOneAndDelete(col.find().first());
		col.insertOne(new Document(resData));

		return resData;
	}

	@Override
	public FccData computeFrequentPatterns(){
		long startTime = System.currentTimeMillis();
		MongoCollection<Document> col = mongodb.getHwdrDataTable();
		MongoCollection<Document> col2 = mongodb.getTable(Consts.FREQUENT_PATTERN_TABLE);
		File fout = new File("/home/avci/Desktop/out7_new.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fout);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		col2.deleteMany(new Document());// empty prevously camputed patterns
		for (int i = 0; i < 10; i++) {
			FindIterable<Document> tt = col.find(new Document("lblClass", i)).projection(new Document(KEY.fcc.name(), 1).append(KEY.lblClass.name(), 1));

			List<String> fccs = new ArrayList<>();
			for (Document doc : tt) {
				String fcc = doc.getString(KEY.fcc.name());
				fccs.add(fcc);

			}
			FrequentSubset fpg = new FrequentSubset(fccs, 7, 10,i);
			fpg.computeFrequentSubsequents(fpg.getRoot());
			fpg.optimizeFreqSubsRes();
			fpg.calculateImg();
			List<FccData> fdg = fpg.getFrequentList();
			for (FccData fccData : fdg) {
				Document docInsert = new Document(fccData);
				col2.insertOne(docInsert);
			}
		}

		FccData res = new FccData();
		res.put("exec_time", (System.currentTimeMillis() - startTime)/1000.0);
		logger.info("EXECUTION TIME : " + (System.currentTimeMillis() - startTime)/1000.0);
		return res;
	}

	@Override
	public List<FccData> getFrequentPatterns(int lblClass) {
		MongoCollection<Document> col = mongodb.getTable(Consts.FREQUENT_PATTERN_TABLE);
		List<FccData> patterns = new ArrayList<>();
		FindIterable<Document> tt = col.find(new Document(KEY.lblClass.name(), lblClass));
		for (Document doc : tt) {
			FccData fd = new FccData();
			fd.put(KEY.id, doc.get("_id").toString());
			doc.remove("_id");
			fd.putAll(doc);
			patterns.add(fd);
		}
		return patterns;
	}

	/**
	 *
	 * @param reCompute
	 * type, type=0 without condence&reduce, type=1 for after condence&reduce
	 * @return
	 * @throws Exception
	 */
	@Override
	public FccData testAccuracy(boolean reCompute) throws Exception{
		Gson gson = new Gson();
		MongoCollection<Document> col = mongodb.getTable(Consts.TEST_ACCURACY);

		if(!reCompute){
			Document doc = col.find().first();
			if(doc != null){
				doc.remove("_id");
				return new FccData(doc);
			}
		}
		List<FccData> testResultList = new ArrayList<>();
		createTestTrainSet(false);
		for (int type = 0; type < 2; type++) {
			int k = 5;
			if(type == 0)
				k = tuneK(false).getInteger(KEY.K.name());

			List<FccData> fccs = getAllFccCodes(0, false);
			List<FccData> trainFccs;

			if(type == 1)
				trainFccs = getAllFccCodes(1, true);
			else
				trainFccs = getAllFccCodes(1, false);
			logger.info("TRAIN SET SIZE : " + trainFccs.size() + " TESTSIZE : " + fccs.size());

			long startT = System.currentTimeMillis();
			int corClassifieds = 0;
			float[][] matrix = new float[11][13];
			for (int i = 1; i < matrix.length; i++) {
				matrix[0][i] = i-1;
			}
			for (int i = 1; i < matrix.length; i++) {
				matrix[i][0] = i-1;
			}
			Classifier classifier = new KNearestNeigbors(k, trainFccs);
			for (final ListIterator<FccData> i = fccs.listIterator(); i.hasNext();) {
				FccData fccData = i.next();
				classifier.classify(fccData.getString(KEY.fcc.name()));
				int predClass = classifier.getPredictedClass();
				if(predClass == fccData.getInteger(KEY.lblClass))
					corClassifieds++;
				fccData.put(KEY.predictedClass.name(), predClass);
				fccData.remove(KEY.fcc.name());
				fccData.remove(KEY.fName.name());

				matrix[fccData.getInteger(KEY.lblClass.name())+1][predClass+1] = matrix[fccData.getInteger(KEY.lblClass.name())+1][predClass+1] + 1;
			}
			// compute precision and recall
			float totalPrec = 0, totalRecall = 0;
			for (int i = 1; i < matrix.length; i++) {
				float totalActualLabel = 0, totalPredLabel = 0;

				for (int j = 1; j < matrix.length; j++)
					totalActualLabel += matrix[i][j];

				for (int j = 1; j < matrix.length; j++)
					totalPredLabel += matrix[j][i];

				float truePos = matrix[i][i];
				matrix[i][11] = Float.valueOf(String.format("%.2f", (truePos / totalActualLabel)*100f)); // RECALL
				matrix[i][12] = Float.valueOf(String.format("%.2f", (truePos / totalPredLabel)*100f)); // Precision
				if(Float.isNaN(matrix[i][11]))
					matrix[i][11] = 0f;
				if(Float.isNaN(matrix[i][12]))
					matrix[i][12] = 0f;
				totalRecall += matrix[i][11];
				totalPrec += matrix[i][12];
			}

			totalPrec /= 10;
			totalRecall /= 10;

			float accuracy = (corClassifieds/Float.valueOf(fccs.size()))*100f;
			logger.info("ACCURACCY : " + accuracy);

			FccData resData = new FccData();
			resData.put(KEY.accuracy.name(), String.format("%.4f", accuracy));
			resData.put("avg_precision", String.format("%.2f", totalPrec));
			resData.put("avg_recall", String.format("%.2f", totalRecall));
			resData.put("testsize", fccs.size());
			resData.put("matrix", gson.toJson(matrix));
			resData.put(KEY.K.name(), k);
			resData.put("exectime", String.format("%.2f", Float.valueOf((System.currentTimeMillis()-startT)/1000f)));
			resData.put("desc", (type == 0) ? "Without Reduced-Condensed" : "With Reduced-Condensed");
			resData.put("trainsize", trainFccs.size());
			testResultList.add(resData);
		}
		FccData res = new FccData();
		res.put("accuracyresult", testResultList);
		col.findOneAndDelete(col.find().first());
		col.insertOne(new Document(res));
		return res;
	}

	@Override
	public void createTestTrainSet(boolean onlyPrint){
		logger.debug("createTestTrainSet starting!!!");
		MongoCollection<Document> col = mongodb.getHwdrDataTable();

		if(!onlyPrint){
			logger.info("Recomputing test and train datasets");
			List<FccData> fccs = getAllFccData(-1, false);
			Collections.shuffle(fccs);
			int untill = Math.floorDiv(fccs.size(), 4); // take %25 as training

			for (int i = 0; i < fccs.size(); i++) {
				Document set = new Document("$set", new Document(KEY.training.name(), (i<untill)? 0 : 1));

				col.updateOne(new Document("_id", new ObjectId(fccs.get(i).getString(KEY.id.name()))), set);
			}
		}

		logger.debug(" HEPSI " + getAllFccCodes(-1, false).size() );
		logger.debug(" TRAIN " + getAllFccCodes(1, false).size() );
		logger.debug(" TEST " + getAllFccCodes(0, false).size() );


	}

}
