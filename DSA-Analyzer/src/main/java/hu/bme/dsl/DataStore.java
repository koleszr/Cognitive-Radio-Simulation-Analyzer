package hu.bme.dsl;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DataStore {
	
	private static final String THESIS_DB = "thesis";
	private static final String SIMULATIONS_COLLECTION = "longSimulations";
	
	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<Document> documents;
	
	public DataStore() {
		init();
	}
	
	/**
	 * Initializes database connection to thesis
	 * database and to simulations collection.
	 */
	public void init() {
		client = new MongoClient();
		db = client.getDatabase(THESIS_DB);
		documents = db.getCollection(SIMULATIONS_COLLECTION);
	}

	public MongoClient getClient() {
		return client;
	}

	public void setClient(MongoClient client) {
		this.client = client;
	}

	public MongoDatabase getDb() {
		return db;
	}

	public void setDb(MongoDatabase db) {
		this.db = db;
	}

	public MongoCollection<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(MongoCollection<Document> documents) {
		this.documents = documents;
	}
}
