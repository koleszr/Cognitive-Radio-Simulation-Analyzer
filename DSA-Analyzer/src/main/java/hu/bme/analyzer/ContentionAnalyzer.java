package hu.bme.analyzer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

import hu.bme.dsl.DataStore;

public class ContentionAnalyzer {
	
	private DataStore ds;
	
	private double limit;
	
	public ContentionAnalyzer(double limit) {
		ds = new DataStore();
		this.limit = limit;
	}
	
	public void analyze() {
		MongoCollection<Document> simulations = ds.getDocuments();
		
		Document project = new Document("$project", 
				new Document("radios.competingUsers", 1)
				.append("radios.contentions", 1)
				);
		
		Document unwind = new Document("$unwind", "$radios");
		
		AggregateIterable<Document> iter = simulations.aggregate(Arrays.asList(project, unwind));
		
		final Map<String, Double> totalContention = new HashMap<>();
		totalContention.put("1", 0.0);
		totalContention.put("2", 0.0);
		totalContention.put("3", 0.0);
		
		final Map<String, Integer> total = new HashMap<>();
		total.put("1", 0);
		total.put("2", 0);
		total.put("3", 0);
		
		iter.forEach(new Block<Document>() {

			@SuppressWarnings("unchecked")
			@Override
			public void apply(Document doc) {
				List<List<Integer>> competingUsers = doc.get("radios", Document.class).get("competingUsers", List.class);
				List<List<Double>> contentions = doc.get("radios", Document.class).get("contentions", List.class);
				
				for (int i = 0; i < competingUsers.size(); i++) {
					List<Integer> users = competingUsers.get(i);
					List<Double> cs = contentions.get(i);
					
					for (int j = 0; j < users.size(); j++) {
						if (cs.get(j) > limit && users.get(j) > 0) {
							totalContention.put(String.valueOf(users.get(j)), totalContention.get(String.valueOf(users.get(j))) + cs.get(j));
							total.put(String.valueOf(users.get(j)), Math.incrementExact(total.get(String.valueOf(users.get(j)))));
						}
					}
				}
			}
		});
		
		System.out.println("Total contention: ");
		System.out.println(totalContention);
		
		System.out.println("Total: ");
		System.out.println(total);
		
		System.out.println("Estimation with 1 competing users: " + totalContention.get("1") / total.get("1"));
		System.out.println("Estimation with 2 competing users: " + totalContention.get("2") / total.get("2"));
		System.out.println("Estimation with 3 competing users: " + totalContention.get("3") / total.get("3"));
	}

}
