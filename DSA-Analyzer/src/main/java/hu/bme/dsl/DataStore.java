package hu.bme.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DataStore {
	
	private Properties props;
	
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
		initProperties();
		client = new MongoClient();
		db = client.getDatabase(props.getProperty("THESIS_DB"));
		documents = db.getCollection(props.getProperty("SIMULATIONS_COLLECTION"));
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
	
	private void initProperties() {
    	ClassLoader loader = Thread.currentThread().getContextClassLoader();
    	props = new Properties();
    	
    	try(InputStream stream = loader.getResourceAsStream("simulation.properties")) {
    		props.load(stream);
    	} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
