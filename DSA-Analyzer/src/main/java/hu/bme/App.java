package hu.bme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import hu.bme.analyzer.CollisionAnalyzer;

public class App {
	
	private static Properties props;
	
	public static void main(String[] args) {
		if (args.length > 0) {
			int n = Integer.parseInt(args[0]);
			
			switch (n) {
			case 1:
				analyzeCollisions();
				break;
			case 2:
				List<String> docNamesByStrategies = Arrays.asList(
//						"(mixed|competitive)_1(_[0-2]){2}_fix_u_[2-4]_c_[1-2]_[0-4]",
//						"(mixed|competitive)_1(_[0-2]){2}_decr_u_[2-4]_c_[1-2]_[0-4]",
//						"(mixed|competitive)_1(_[0-2]){2}_max_u_[2-4]_c_[1-2]_[0-4]",
//						"(mixed|competitive)_1(_[0-2]){2}_rnd_u_[2-4]_c_[1-2]_[0-4]"
						"fix_[0-4]",
						"decr_[0-4]",
						"max_[0-4]",
						"rnd_[0-4]"
					);
				analyzeCollisionsByPhasesAndStrategies(docNamesByStrategies);
				break;
			default:
				System.out.println("Parameter usage: ");
				System.out.println("1 - collision analysis depending on user and channel number");
				System.out.println("2 - collision analysis depending on strategy and phases");
			}
		}
	}
	
	public static void initProperties() {
    	ClassLoader loader = Thread.currentThread().getContextClassLoader();
    	props = new Properties();
    	
    	try(InputStream stream = loader.getResourceAsStream("simulation.properties")) {
    		props.load(stream);
    	} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void analyzeCollisions() {

		PrintStream out = null;
		
		try (BufferedReader br = new BufferedReader(new FileReader(new File(props.getProperty("DOCUMENT_NAME_FILE"))))) {
			out = new PrintStream(new File(props.getProperty("SIMULATION_OUTPUT_PATH ")+ "collision_simulation1.txt"));
			
			for (int i = 0; i < 16; i++) {
				StringBuilder sb = new StringBuilder();

				for (int j = 0; j < 6; j++) {
					sb.append(br.readLine() + "\n");
				}
			
				CollisionAnalyzer analyzer = new CollisionAnalyzer(sb.toString(), out);
				analyzer.analyzeCollisionByUserAndChannelNumber();
			}
			
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			out.close();
		}
	}
	
	public static void analyzeCollisionsByPhasesAndStrategies(List<String> docNamesByStrategies) {
		try (PrintStream out = new PrintStream(new File(props.getProperty("SIMULATION_OUTPUT_PATH ") + "longCollision_simulation.txt"))) {
			for (String docName : docNamesByStrategies) {
				CollisionAnalyzer analyzer = new CollisionAnalyzer(out);
				analyzer.analyzeCollisionsByStrategyAndPhase(docName);
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
