package it.cnr.istc.pst.cognition.sir.app.kr2020;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class FeasibilityExperiment 
{
	private static final String ONTO_MODEL = "etc/ontology/sir_stimulation_v1.2.owl";
	private static final String RULE_MODEL = "etc/ontology/stimulations_v1.0.rules";
	private static final String DATA_FOLDER = "data/exp";
	private static final int PROFILES = 10;								// number of random profiles to generate
	private static final int STIMULATIONS = 5;							// number of maximum stimulation functions for each stimulus
	private static final int[] STIMULI_SETS = new int[] {				// number of stimuli for each evaluated set
		10,
		20,
		30,
		40,
		50,
		60,
		70,
		80,
		90,
		100,
		150,
		200,
		250,
		300,
		350,
		400,
		450,
		500
	};
	
	/**
	 * 
	 */
	protected FeasibilityExperiment() {}
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		// list of generated profiles
		List<String> pFiles = new ArrayList<>();
		// list of generated stimuli sets
		List<String> sFiles = new ArrayList<>();
		
		try
		{
			// setup experiments
			for (int i = 0; i < PROFILES; i++) 
			{
				// set owl model
		        OWLSirModel kb = new OWLSirModel(
		        		ONTO_MODEL,
		        		RULE_MODEL);
		        
		        // create profile generator
				ProfileGenerator gen = new ProfileGenerator(kb);
				// create a random profile profile
				String pFile = gen.create();
				// add to list
	    		pFiles.add(pFile);
			}
			
			
			// create scenarios and evaluate the feasibility of the reasoner
			for (int j = 0; j < STIMULI_SETS.length; j++)
			{
				// set owl model
		        OWLSirModel kb = new OWLSirModel(
		        		ONTO_MODEL,
		        		RULE_MODEL);
		        
		        // create stimuli set generator
		        StimuliGenerator sGen = new StimuliGenerator(kb);
				// create a random set of stimuli  
				String sFile = sGen.create(
						STIMULI_SETS[j], 
						STIMULATIONS);
				// add to list
				sFiles.add(sFile);
			}
			
			
			// clean memory
			System.gc();
			
			// file name 
			String dataFileName = "feasibility_data_" + System.currentTimeMillis() + ".csv";
			// create a new XML file
			File file = new File(DATA_FOLDER + "/" + dataFileName);
			// create writer
			try (BufferedWriter writer =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")))
			{
				// write file header
				writer.write("profile;#stimuli;#impairments;#opportunities;#recomms;#best_recomms;#inf_time;#recomm_time\n");
				writer.flush();

				// set profile counter
				int pCounter = 0;
				// run experiments on all generated pairs <pFile, sFile>
				for (String profileFile : pFiles) 
				{
					// set stimuli counter
					int sCounter = 0;
					for (String stimuliFile : sFiles) 
					{
						System.out.println("Run feasibility test on couple <" + profileFile  +", " +  stimuliFile + ">");
						
						// compute inference time
						long infTime = System.currentTimeMillis();
						// set owl model
				        OWLSirModel kb = new OWLSirModel(
				        		ONTO_MODEL,
				        		RULE_MODEL);
						
				        // create parser of stimuli
			    		StimulationPortfolioXMLParser sParser = new StimulationPortfolioXMLParser(kb);
			    		// parse stimuli file
			    		sParser.parse(stimuliFile);
						
			    		
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
	    						+ "" + STIMULI_SETS[sCounter] + ";"
								+ "" +  impairs.size() + ";"
								+ "" + opportunities.size() + ";"
								+ "" + recoms.get(key).size() + ";"
								+ "" + best.get(key).size() + ";"
								+ "" + infTime + ";"
								+ "" + recTime +"\n");
				    		// write data to file
				    		writer.flush();
				    		
			    			// next run
			    			break;
			    		}
			    		
			    		sCounter++;
					}
					
					pCounter++;
				}	
			}
				
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
}
