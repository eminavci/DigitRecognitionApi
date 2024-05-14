package com.mldm.core.persistence;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mldm.core.utl.Consts;
import com.mongodb.client.MongoCollection;
//db.createUser({user: "mongodb-mldm2",pwd: "emin12345",roles: [ "readWrite", "dbAdmin"]})
@Service
public class MongoUtil {

	private static final Logger logger = LoggerFactory.getLogger(MongoUtil.class); 
	
	@Autowired
	private MongoClient mongo = null;
	

	public  MongoCollection<Document> getHwdrDataTable(){
		return mongo.getDatabase("hwdr_db").getCollection(Consts.FREEMAN_METADATA_TABLE);
	}

	public  MongoCollection<Document> getTable(String tableName){
		return mongo.getDatabase("hwdr_db").getCollection(tableName);
	}
}
