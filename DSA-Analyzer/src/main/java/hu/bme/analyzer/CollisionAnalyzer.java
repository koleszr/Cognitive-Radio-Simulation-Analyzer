package hu.bme.analyzer;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

import hu.bme.dsl.DataStore;

public class CollisionAnalyzer {

	private DataStore ds;
	
	private Scanner scanner;
	
	private PrintStream out;
	
	public CollisionAnalyzer(String documentNames, PrintStream out) {
		scanner = new Scanner(documentNames);
		ds = new DataStore();
		this.out = out;
	}
	
	public CollisionAnalyzer(PrintStream out) {
		ds = new DataStore();
		this.out = out;
	}

	public DataStore getDs() {
		return ds;
	}

	public void setDs(DataStore ds) {
		this.ds = ds;
	}

	public Scanner getScanner() {
		return scanner;
	}

	public void setScanner(Scanner scanner) {
		this.scanner = scanner;
	}
	
	/**
	 * Calculates the average collision number depending
	 * on the user and maximal channel number.
	 * 
	 * Aggregation steps: 
	 * <ol>
	 * <li>match: name -> scanner</li>
	 * <li>unwind: collisions</li>
	 * <li>match: collisions.name</li>
	 * <li>project: collisions</li>
	 * <li>group: average of collisions.number for each collisions.name</li>
	 * <li>Sum the collisions</li>
	 * </ol>
	 * 
	 */
	public void analyzeCollisionByUserAndChannelNumber() {
		MongoCollection<Document> simulations = ds.getDocuments();
		
		for (int i = 0; i < 6; i++) {
			String docName = scanner.nextLine();
			out.println(docName);
			System.out.println(docName);
			
			Document nameMatch = new Document("$match", 
					new Document("name", 
							new Document("$regex", docName)
							)
					);
			
			Document collisionsUnwind = new Document("$unwind", "$collisions");
			
			Document phaseMatch = new Document("$match", 
					new Document("collisions.name", 
							new Document("$regex", "[A-Z]+_PHASE_(?:[0-9]{1,3}_){0,1}0$")
							)
					);
			
			Document collisionsProject = new Document("$project", 
					new Document("collisions", 1)
					.append("radios", 1)
					.append("strategySpaceSize", 1)
					);
			
			Document collisionsGroup = new Document("$group", 
					new Document("_id", 
						new Document("name", "$collisions.name")
						.append("radios", "$radios")
						.append("size", "$strategySpaceSize")
						)
					.append("total", new Document("$avg", "$collisions.number"))
					);
			
			Document collisionsTotal = new Document("$group",
					new Document("_id", 
						new Document("radios", "$_id.radios")
						.append("size", "$_id.size")
						)
					.append("totalCollisions", 
						new Document("$sum", "$total"))
					);
			
			AggregateIterable<Document> iter = simulations.aggregate(
					Arrays.asList(
						nameMatch, 
						collisionsUnwind, 
						phaseMatch, 
						collisionsProject, 
						collisionsGroup, 
						collisionsTotal
					)
				);
			
			iter.forEach(new Block<Document>() {

				@Override
				public void apply(Document doc) {
					out.println("Collisions: " + doc.getDouble("totalCollisions"));
				}
			});
			
		}
		
		ds.getClient().close();
		out.println("End of analysis!");
		out.println();
	}
	
	public void analyzeCollisionsByStrategyAndPhase(String docName) {
		out.println("Start analysis for documents: " + docName);
		System.out.println("Start analysis for documents: " + docName);
		
		MongoCollection<Document> simulations = ds.getDocuments();
		
		Document documentMatch = new Document("$match",
				new Document("name", 
						new Document("$regex", docName)
						)
				); 
		
		Document collisionsUnwind = new Document("$unwind", "$collisions");
		
		Document collisionsMatch = new Document("$match", 
				new Document("collisions.name",
						new Document("$regex", "[A-Z]+_PHASE_(?:[0-9]{1,3}_){0,1}0$")
						)
				);
		
		Document collisionsProjection = new Document("$project", 
					new Document("collisions", 1)
				);
		
		Document collisionsTotal = new Document("$group", 
				new Document("_id", "$collisions.name")
				.append("total", 
						new Document("$avg", "$collisions.number")
						)
				);
		
		Document sortResults = new Document("$sort", 
				new Document("_id", 1)
				);
		
		AggregateIterable<Document> iter = simulations.aggregate(
				Arrays.asList(
					documentMatch, 
					collisionsUnwind, 
					collisionsMatch, 
					collisionsProjection, 
					collisionsTotal,
					sortResults
				)
			);
		
		iter.forEach(new Block<Document>() {

			@Override
			public void apply(Document doc) {
				out.println(doc.getDouble("total"));
			}
		});
		
		ds.getClient().close();
		out.println("End of analysis!");
		out.println();
		
		System.out.println("End of analysis!");
	}
}
