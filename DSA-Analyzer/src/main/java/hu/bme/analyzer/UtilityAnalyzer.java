package hu.bme.analyzer;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

import hu.bme.dsl.DataStore;

public class UtilityAnalyzer {
	
	private DataStore ds;
	
	private PrintStream out;
	
	public UtilityAnalyzer(PrintStream out) {
		ds = new DataStore();
		this.out = out;
	}
	
	public void analyze(String docName) {
		MongoCollection<Document> simulations = ds.getDocuments();
		
		Document match = new Document("$match", 
				new Document("name", 
						new Document("$regex", docName)
						)
				);
		
		Document unwind = new Document("$unwind", "$radios");

		Document project = new Document("$project", 
				new Document("radio", "$radios.radio")
				.append("utilities", "$radios.utilities")
				.append("accessDecisions", "$radios.accessDecisions")
				.append("_id", 0)
				);
		
		AggregateIterable<Document> iter = simulations.aggregate(
					Arrays.asList(
							match,
							unwind,
							project
						)
				);
		
		Map<Integer, Double> utilSum = new HashMap<>();
		Map<Integer, Integer> utilNum = new HashMap<>();
				
		
		iter.forEach(new Block<Document>() {

			@Override
			public void apply(Document doc) {
				int accessDecision = (int) doc.get("accessDecisions", List.class).get(0);
				double primaryUtility = (double) doc.get("utilities", List.class).get(accessDecision);
				int radio = doc.getInteger("radio");
				
				if (utilSum.containsKey(radio) && utilNum.containsKey(radio)) {
					utilSum.put(radio, utilSum.get(radio) + primaryUtility);
					utilNum.put(radio, Math.incrementExact(utilNum.get(radio)));
				}
				else {
					utilSum.put(radio, primaryUtility);
					utilNum.put(radio, 1);
				}
			}
			
		});
		
		out.println(docName);
		out.println(utilSum);
		out.println(utilNum);
		
		for (Entry<Integer, Double> e : utilSum.entrySet()) {
			out.println((e.getValue() / utilNum.get(e.getKey())));
		}
		
		out.println();
	}

}
