package com.mldm.core.persistence;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mldm.core.persistence.model.Fcc;
import com.mldm.core.persistence.model.FccMeta;
import com.mldm.core.utl.Consts;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class DBController {

	private static final Logger logger = LoggerFactory.getLogger(DBController.class);
	
	//public static MongoCollection onlyFreemanTable;
	public static MongoCollection<Document> metaFreemanTable;
	
//	public static void saveOnlyFreeman(String freemancode, int lblClass){
//		
//		Document doc = new Document();
//		doc.append("fcc", freemancode)
//		.append("lblClass", lblClass);
//
//		onlyFreemanTable.insertOne(doc);
//	}
	
	public String saveMetaFreeman(String fileName, String freemanCode, int lblClass, BufferedImage origImg, BufferedImage contourImg){
		
		Document doc = new Document();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(origImg, "png", baos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Binary b = new Binary(baos.toByteArray());
		doc.put("origImg", b);
		
		baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(contourImg, "png", baos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		b = new Binary(baos.toByteArray());
		doc.append("contourImg", b)
		.append("fcc", freemanCode)
		.append("lblClass", lblClass)
		.append("fileName", fileName);
		
		metaFreemanTable.insertOne(doc);
		return doc.get("_id").toString();
	}
	
	public List<Fcc> getAllFCodes(){
		//Document query = new Document("lblClass", new Document("$eq", 2)); // bunu find kısmına koayrsan filtre çalıştırmış olacak
		List<Fcc> fccs = new ArrayList<>();
		FindIterable<Document> tt = metaFreemanTable.find()
									.projection(new Document("fcc", 1).append("lblClass", 1));
		for (Document d : tt) {
			fccs.add(new Fcc(d.get("_id").toString(), d.getString("fcc"), d.getInteger("lblClass", -1)));
		}

		return fccs;
	}

	public List<FccMeta> getAllFMeta(){
		List<FccMeta> fccs = new ArrayList<>();
		FindIterable<Document> tt = metaFreemanTable.find();
		for (Document doc : tt) {
			fccs.add(new FccMeta(doc.getString("fcc"),
 					doc.getInteger("lblClass", -1),
					doc.getString("origImg"), 
					doc.getString("contourImg")).setId(doc.get("_id").toString()));
		}
		
		return fccs;
	}
	
	public long deleteFccMetaByIds(List<String> ids){
		List<ObjectId> oids = new ArrayList<ObjectId>();
		ids.forEach(id -> oids.add(new ObjectId(id)));
		
		return metaFreemanTable.deleteMany(new Document("_id", new Document("$in", oids))).getDeletedCount();
	}
	
	public List<String> getFileNamesByOids(List<String> ids){
		List<ObjectId> oids = new ArrayList<ObjectId>();
		ids.forEach(id -> oids.add(new ObjectId(id)));
		
		List<String> fNames = new ArrayList<>();
		
		Document query = new Document("_id", new Document("$in", oids));
		FindIterable<Document> tt = metaFreemanTable.find(query)
				.projection(new Document("fileName", 1).append("lblClass", 1));
		
		for (Document d : tt) {
			fNames.add(d.getInteger("lblClass") + File.separator +  d.getString("fileName"));
		}
		return fNames;
	}
	
	public Map<String, BufferedImage> getCounterImgsByOid(List<Fcc> fccs){
		List<ObjectId> oids = new ArrayList<ObjectId>();
		fccs.forEach(fcc -> oids.add(new ObjectId(fcc.getId())));
		
		List<String> fNames = new ArrayList<>();
		
		Document query = new Document("_id", new Document("$in", oids));
		FindIterable<Document> tt = metaFreemanTable.find(query)
				.projection(new Document("contourImg", 1));
		Map<String, BufferedImage> map = new HashMap<>();
		for (Document d : tt) {
			try {
				map.put(d.get("_id").toString(), ImageIO.read(new ByteArrayInputStream(((Binary)d.get("contourImg")).getData())));
			} catch (IOException e) {logger.error("Exeption occured on one sample ", e);}
		}
		return map;
	}
}
