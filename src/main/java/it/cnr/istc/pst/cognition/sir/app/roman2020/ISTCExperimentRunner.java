package it.cnr.istc.pst.cognition.sir.app.roman2020;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import it.cnr.istc.pst.cognition.sir.parser.xml.CognitiveProfileXMLParser;
import it.cnr.istc.pst.cognition.sir.parser.xml.StimulationPortfolioXMLParser;
import it.cnr.istc.pst.cognition.sir.reasoner.RecommenderSystem;
import it.cnr.istc.pst.cognition.sir.reasoner.StimulationRecommendation;
import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirModel;
import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirNameSpace;

/**
 * 
 * @author alessandroumbrico
 *
 */
public class ISTCExperimentRunner 
{
	private static final String DATA_FOLDER = "data/exp";
	private static final String ACTIONS_FILE = "etc/roman2020/istc/stimulation_actions.xml";
	private static final String[] PROFILES = new String[] {
			"etc/roman2020/istc/profile1.xml",
			"etc/roman2020/istc/profile2.xml",
			"etc/roman2020/istc/profile3.xml",
	};
	
	/**
	 * 
	 */
	protected ISTCExperimentRunner() {}
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		// result map
		Map<String, Map<String, Double>> profile2stimulus2rank = new HashMap<>();
		Map<String, Map<String, String>> profile2stimulus2param = new HashMap<>();
		
		try
		{
			// file name 
			String dataFileName = "roma2020_feasibility_data_" + System.currentTimeMillis() + ".csv";
			// create a new XML file
			File file = new File(DATA_FOLDER + "/" + dataFileName);
			// create writer
			try (BufferedWriter writer =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")))
			{
				// write file header
				writer.write("profile;#impairments;#opportunities;#recomms;#best_recomms;#inf_time;#recomm_time\n");
				writer.flush();

				// set profile counter
				int pCounter = 1;
				// run experiments on all generated pairs <pFile, sFile>
				for (String profileFile : PROFILES) 
				{
					// run reasoner on profile
					System.out.println("Run test on <" + profileFile + ">");
					// set result map
					profile2stimulus2rank.put("profile" + pCounter, new HashMap<>());
					profile2stimulus2param.put("profile" + pCounter, new HashMap<>());
					
					// compute inference time
					long infTime = System.currentTimeMillis();
					// set owl model
			        OWLSirModel kb = new OWLSirModel(
			        		"etc/ontology/sir_stimulation_v1.2.owl",
			        		"etc/ontology/sir_stimulations_v1.2.rules");
					
			        // create parser of stimuli
		    		StimulationPortfolioXMLParser sParser = new StimulationPortfolioXMLParser(kb);
		    		// parse stimuli file
		    		sParser.parse(ACTIONS_FILE);
		    		for (Resource r : sParser.getResources()) 
		    		{
		    			// get the name of the stimulation
		    			Statement s = r.getProperty(kb.getProperty(OWLSirNameSpace.SIR.getNs() + "hasName"));
		    			// get value 
		    			String sName = s.getString();
		    			// add entry to the result
		    			profile2stimulus2rank.get("profile" + pCounter).put(sName, 0.0);
		    			
		    		}
		    		
		    		// create profile parser
		    		CognitiveProfileXMLParser cPerser = new CognitiveProfileXMLParser(kb);
		    		// parse profile 
		    		cPerser.parse(profileFile);
		    		
		    		
		    		// compute the number of inferred impairments
		    		List<Statement> impairs = kb.getStatements(
		    				OWLSirNameSpace.RDF.getNs() + "type", 
		    				OWLSirNameSpace.SIR.getNs() + "CognitiveImpairment");
		    		
		    		// compute the number of inferred opportunities
		    		List<Statement> opportunities = kb.getStatements(
		    				OWLSirNameSpace.RDF.getNs() + "type", 
		    				OWLSirNameSpace.SIR.getNs() + "StimulationOpportunity");
		    		
		    		
		    		// compute reasoning time
		    		long recTime = System.currentTimeMillis();
		    		// set recommender system
		    		RecommenderSystem rs = new RecommenderSystem(kb);
		    		// compute recommendations
		    		Map<String, List<StimulationRecommendation>> recoms = rs.compute();
		    		// compute best recommendations
		    		Map<String, List<StimulationRecommendation>> best = rs.best();
		    		
		    		// get recommendations data - only the first is necessary
		    		for (String key : recoms.keySet()) 
		    		{
		    			// compute inference time
			    		infTime = System.currentTimeMillis() - infTime;
			    		// compute 
			    		recTime = System.currentTimeMillis() - recTime;
			    		// update data
			    		writer.write(
		    				"profile_" + pCounter + ";"
							+ "" +  impairs.size() + ";"
							+ "" + opportunities.size() + ";"
							+ "" + recoms.get(key).size() + ";"
							+ "" + best.get(key).size() + ";"
							+ "" + infTime + ";"
							+ "" + recTime +"\n");
			    		// write data to file
			    		writer.flush();
			    		
		    			System.out.println();
			    		System.out.println("Check inferred interaction parameters for all relevant stimulation actions...\n");
		    			// update result cache
			    		for (StimulationRecommendation rec : recoms.get(key)) 
			    		{
//			    			// get stimulus
//			    			Resource s = rec.getStimulus();
//			    			// get name 
//			    			String name = s.getProperty(kb.getProperty(OWLSirNameSpace.SIR.getNs() + "hasName")).getString();
			    			
			    			// update entry
			    			profile2stimulus2rank.get("profile" + pCounter).put(rec.getStimulationName(), rec.getRank());
			    			
			    			
			    			// check stimulus inferred interaction parameters
			    			List<Statement> ss = kb.getStatements(
			    					rec.getStimulationId(),
			    					OWLSirNameSpace.SIR.getNs() + "hasInteractionParameter",
			    					null);
			    			
			    			System.out.println("\tInteraction parameters for : <" + rec.getStimulationId() + ":" + rec.getStimulationName() +">");
			    			// print inferred interaction qualities
			    			for (Statement si : ss) 
			    			{
			    				// get object
			    				Resource region = si.getResource();
			    				
			    				// get parameter type 
			    				Statement rt = region.getProperty(kb.getProperty(OWLSirNameSpace.RDF.getNs() + "type"));
			    				// get value
			    				Statement rv = region.getProperty(kb.getProperty(OWLSirNameSpace.DUL.getNs() + "hasRegionDataValue"));
			    				// get interaction quality
			    				Statement rq = region.getProperty(kb.getProperty(OWLSirNameSpace.DUL.getNs() + "isRegionFor"));
			    				
			    				System.out.println("\t\t" + rq.getResource().getLocalName() + " = " + rt.getResource().getLocalName() + ":" + rv.getString());
			    				
			    				// add entry result
			    				profile2stimulus2param.get("profile" + pCounter).put(
			    						rt.getResource().getLocalName(), rv.getString());
			    			}
			    			System.out.println();
			    		}
		    		}
		    		
					pCounter++;
				}	
			}
			
			System.out.println();
			
			// show resulting data
			for (String profile : profile2stimulus2rank.keySet()) 
			{
				for (String stimulus : profile2stimulus2rank.get(profile).keySet()) {
					// print resulting cache
	    			System.out.println(" " + profile + " > " + stimulus + " > " + profile2stimulus2rank.get(profile).get(stimulus));
				}
				
				System.out.println("............................................................");
				
    			// check interaction parameters
    			Map<String, String> params = profile2stimulus2param.get(profile);
    			for (String param : params.keySet()) {
	    			// print interaction parameters
	    			System.out.println(" " + profile + " > " + param + " : " + profile2stimulus2param.get(profile).get(param));
    			}
				
				System.out.println("---------------------------------------------------");
			}
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
}
