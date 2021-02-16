package it.cnr.istc.pst.cognition.sir.reasoner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirModel;
import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirNameSpace;

/**
 * 
 * @author alessandroumbrico
 *
 */
public class RecommenderSystem 
{
	private OWLSirModel kb;					// the semantic model (KG)
	private Resource[] qualities;			// mapping functioning qualities to matrix index
	private Resource[] stimuli;				// mapping stimuli to matrix index
	private Resource[] profiles;			// mapping profiles to matrix index
	private int[][] incMatrix;				// stimulus incident matrix
	private int[][] proMatrix;				// user profiles matrix
	
	private boolean debug;					// debug flag
	
	/**
	 * 
	 * @param model
	 */
	public RecommenderSystem(OWLSirModel model) 
	{
		try
		{
			// set the model
			this.kb = model;
			// setup the recommender system
			this.evaluate();
			// set debug flag
			this.debug = false;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	/**
	 * 
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * 
	 * Prepare the recommendation system
	 * 
	 * This method should be call every time an updated on the KB is detected, so it could be 
	 * useful to integrate asynchronous notifications through Observer design pattern
	 * 
	 * 
	 * @throws Exception
	 */
	public void evaluate() 
			throws Exception
	{
		// get the taxonomy of functioning qualities 
		Map<Resource, List<Resource>> taxonomy = this.kb.getTaxonomyOfFunctioningQualities();
		// get taxonomy leaves
		Set<Resource> set = this.getTaxonomyLeaves(taxonomy);
		// set array of qualities
		this.qualities = new Resource[set.size()];
		// set position counter
		int index = 0;
		// create an index of leaves
		for (Resource res : set) {
			// add functioning quality to the array
			this.qualities[index] = res;
			index++;
		}
		
		
		// get stimuli 
		set = this.kb.getStimuli();
		// set array of stimuli
		this.stimuli = new Resource[set.size()];
		// set position counter
		index = 0;
		for (Resource res : set) {
			// add stimulus to the array
			this.stimuli[index] = res;
			index++;
		}
		
		// set incident matrix 
		this.incMatrix = new int[this.stimuli.length][this.qualities.length];
		// check stimulation capabilities
		for (int i = 0; i < this.incMatrix.length; i++) 
		{
			// get current stimulus
			Resource stimulus = this.stimuli[i];
			// check qualities
			for (int j = 0; j < this.incMatrix[i].length; j++) 
			{
				// get current functioning quality
				Resource quality = this.qualities[j];
				
				// check the knowledge base
				List<Statement> capabilities = this.kb.getStatements(
						stimulus.getURI(), 
						OWLSirNameSpace.SIR.getNs()+ "hasEffectOn", 
						quality.getURI());
				// check if not empty
				if (!capabilities.isEmpty()) {
					// set incident matrix
					this.incMatrix[i][j] = 1;
				}
			}
		}
		
		
		// get registered profiles
		set = this.kb.getProfiles();
		// set array of profiles
		this.profiles = new Resource[set.size()];
		// set position counter
		index = 0;
		// create an index of leaves
		for (Resource res : set) {
			// add functioning quality to the array
			this.profiles[index] = res;
			index++;
		}
		
		
		
		
		// set patient profile vector
		this.proMatrix = new int[this.qualities.length][this.profiles.length];
		for (int i = 0; i < this.proMatrix.length; i++)
		{
			// get quality
			Resource quality = this.qualities[i];
			for (int j = 0; j < this.proMatrix[i].length; j++) 
			{
				// get current profile
				Resource profile = this.profiles[j];
				// get measurement outcome
				int value = this.kb.getMeasurementOutcome(profile, quality);
				// set value
				this.proMatrix[i][j] = value;
			}
			
		}
		
		// check debug flag
		if (this.debug) 
		{
			// print computed incident matrix
			System.out.println("Incident Matrix A(n,m)");
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
			for (int i = 0; i < this.incMatrix.length; i++) {
				System.out.print("\t");
				for (int j = 0; j < this.incMatrix[i].length; j++) {
					System.out.print(" " + this.incMatrix[i][j] + " ");
				}
				
				System.out.println();
			}
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
			
			// print computed incident matrix
			System.out.println("Profile Matrix V(n,k)");
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
			for (int i = 0; i < this.proMatrix.length; i++) {
				System.out.print("\t");
				for (int j = 0; j < this.proMatrix[i].length; j++) {
					System.out.print(" " + this.proMatrix[i][j] + " ");
				}
				
				System.out.println();
			}
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, List<StimulationRecommendation>> compute()
	{
		// map of recommendations
		Map<String, List<StimulationRecommendation>> recoms = new HashMap<>();
		
		// compute ranking vector
		int ranking[][] = new int[this.stimuli.length][this.profiles.length];
		// compute the values of the ranking vector
		for (int i = 0; i < this.stimuli.length; i++)
		{
			// get current stimulus
			Resource stimulus = this.stimuli[i];
			
			
			// compute stimulus correlation with patients' profiles
			for (int j = 0; j < this.profiles.length; j++) 
			{
				// get current profile 
				Resource profile = this.profiles[j];
				// check results
				if (!recoms.containsKey(profile.getURI())) {
					// set entry
					recoms.put(profile.getURI(), new ArrayList<>());
				}
				
				// set ranking value 
				int rank = 0;
				// compute stimulus ranking value with respect to the profile of the current patient
				for (int k = 0; k < this.qualities.length; k++)
				{
					// get stimulus correlation with the quality 
					rank += (this.incMatrix[i][k] * this.proMatrix[k][j]);
				}
				
				// set ranking value
				ranking[i][j] = rank;
				// do not consider stimuli with ranking value equals to 0 
				if (rank > 0) 
				{
					try
					{
						// extract stimulation information
						
						// get stimulus name
						String stimulusName = stimulus.getProperty(this.kb.getProperty(OWLSirNameSpace.SIR.getNs() + "hasName")).getString();
						// get type
						String stimulusType = stimulus.getProperty(this.kb.getProperty(OWLSirNameSpace.RDF.getNs() + "type")).getObject().asResource().getURI();
						
						// create recommendation
						StimulationRecommendation rec = new StimulationRecommendation(
								profile.getURI(), 
								stimulus.getURI(), 
								stimulusName, 
								stimulusType, 
								rank);
						
						// check inferred interaction preferences
						List<Statement> slist = this.kb.getStatements(
								rec.getStimulationId(),
								OWLSirNameSpace.SIR.getNs() + "hasInteractionParameter",
								null);
						
						// check parameters
						for (Statement s : slist) 
						{
							// get statement object
							Resource region = s.getResource();
							
							// get parameter type statement
							Statement st = region.getProperty(this.kb.getProperty(OWLSirNameSpace.RDF.getNs() + "type"));
							// get value statement
							Statement sv = region.getProperty(this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "hasRegionDataValue"));
							
							// add parameter to the recommendation
							rec.addInteractionParameter(
									rec, 
									st.getResource().getLocalName(), 
									sv.getString());
						}
						
							
						// add an element to recommendations
						recoms.get(profile.getURI()).add(rec);
						
					}
					catch (Exception ex) {
						// skip stimulation
						System.err.println("Error while extracting information about stimuls : <" + stimulus.getURI() + ">\n\t- error message: " + ex.getMessage());
						
						// list all properties concerning the individual
						Iterator<Statement> it = stimulus.listProperties();
						while (it.hasNext()) {
							System.err.println("\t- it: " + it.next());
						}
					}
				}
			}
		}
		
		// sort computed rankings recommendations
		for (String profile : recoms.keySet()) {
			Collections.sort(recoms.get(profile));
		}
		
		// check debug flag
		if (this.debug)
		{
			// print computed incident matrix
			System.out.println("Ranking Matrix R(m,k)");
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
			for (int i = 0; i < ranking.length; i++) {
				System.out.print("\t");
				for (int j = 0; j < ranking[i].length; j++) {
					System.out.print(" " + ranking[i][j] + " ");
				}
				
				System.out.println();
			}
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
			
			
			
			// print recommendations
			for (String profile : recoms.keySet()) {
				System.out.println("Recommendations for profile " + profile);
				for (StimulationRecommendation r : recoms.get(profile)) {
					System.out.println(r);
				}
			}
		}
		
		// get sorted list
		return recoms;
	}
	
	
	/**
	 * 
	 * Compute recommendations for a specific user profile
	 * 
	 * @param profile
	 * @return
	 */
	public List<StimulationRecommendation> compute(Resource profile)
	{
		// list of recommendations
		List<StimulationRecommendation> recoms = new ArrayList<>();
		
		// get profile index
		int pIndex = -1;
		for (int i = 0; i < this.profiles.length; i++) {
			if (this.profiles[i].equals(profile)) {
				pIndex = i; 
				break;
			}
		}
		
		// check if index found
		if (pIndex > 0)
		{
			// compute ranking vector
			int ranking[] = new int[this.stimuli.length];
			// compute the values of the ranking vector
			for (int i = 0; i < this.stimuli.length; i++)
			{
				// get current stimulus
				Resource stimulus = this.stimuli[i];
				// set ranking value 
				int rank = 0;
				// compute stimulus ranking value with respect to the profile of the current patient
				for (int k = 0; k < this.qualities.length; k++)
				{
					// get stimulus correlation with the quality 
					rank += (this.incMatrix[i][k] * this.proMatrix[k][pIndex]);
				}
				
				
				// set ranking value
				ranking[i] = rank;
				// do not consider stimuli with ranking value equals to 0 
				if (rank > 0) 
				{
					try
					{
						// extract stimulation information
						
						// get stimulus name
						String stimulusName = stimulus.getProperty(this.kb.getProperty(OWLSirNameSpace.SIR.getNs() + "hasName")).getString();
						// get type
						// get type
						String stimulusType = stimulus.getProperty(this.kb.getProperty(OWLSirNameSpace.RDF.getNs() + "type")).getObject().asResource().getURI();
						// create recommendation
						StimulationRecommendation rec = new StimulationRecommendation(
								profile.getURI(), 
								stimulus.getURI(), 
								stimulusName, 
								stimulusType, 
								rank);
						
						// check inferred interaction preferences
						List<Statement> slist = this.kb.getStatements(
								rec.getStimulationId(),
								OWLSirNameSpace.SIR.getNs() + "hasInteractionParameter",
								null);
						
						// check parameters
						for (Statement s : slist) 
						{
							// get statement object
							Resource region = s.getResource();
							
							// get parameter type statement
							Statement st = region.getProperty(this.kb.getProperty(OWLSirNameSpace.RDF.getNs() + "type"));
							// get value statement
							Statement sv = region.getProperty(this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "hasRegionDataValue"));
							
							// add parameter to the recommendation
							rec.addInteractionParameter(
									rec, 
									st.getResource().getLocalName(), 
									sv.getString());
						}
						
							
						// add an element to recommendations
						recoms.add(rec);
						
					}
					catch (Exception ex) {
						// skip stimulation
						System.err.println("Error while extracting information about stimuls : <" + stimulus.getURI() + ">\n\t- error message: " + ex.getMessage());
						
						// list all properties concerning the individual
						Iterator<Statement> it = stimulus.listProperties();
						while (it.hasNext()) {
							System.err.println("\t- it: " + it.next());
						}
					}
				}
			}
			
			// sort rankings
			Collections.sort(recoms);
			
			// check debug flag
			if (debug)
			{
				// print computed incident matrix
				System.out.println("Ranking Matrix R(m,k)");
				System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
				for (int i = 0; i < ranking.length; i++) {
					System.out.print("\t" + ranking[i] + " ");
				}
				System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
	
				// print recommendations
				System.out.println("Recommendations for profile " + profile);
				for (StimulationRecommendation r : recoms) {
					System.out.println(r);
				}
			}
		}
		
		// get sorted list
		return recoms;
	}
	
	
	/**
	 * 
	 * Compute the best recommendations found for a specific user profile
	 * 
	 * @param profile
	 * @return
	 */
	public List<StimulationRecommendation> best(Resource profile)
	{
		// list of recommendations
		List<StimulationRecommendation> recoms = new ArrayList<>();
		// max ranking value 
		double max = Double.MIN_VALUE + 1;
		
		// get profile index
		int pIndex = -1;
		for (int i = 0; i < this.profiles.length; i++) {
			if (this.profiles[i].equals(profile)) {
				pIndex = i; 
				break;
			}
		}
		
		// check if index found
		if (pIndex >= 0)
		{
			// compute ranking vector
			int ranking[] = new int[this.stimuli.length];
			// compute the values of the ranking vector
			for (int i = 0; i < this.stimuli.length; i++)
			{
				// set ranking value 
				int rank = 0;
				// compute stimulus ranking value with respect to the profile of the current patient
				for (int k = 0; k < this.qualities.length; k++)
				{
					// get stimulus correlation with the quality 
					rank += (this.incMatrix[i][k] * this.proMatrix[k][pIndex]);
				}
				
				
				// set ranking value
				ranking[i] = rank;
				// update max
				max = Math.max(max, rank);
			}
			
			// compute ranking threshold
			double threshold = max / 2;
			 // check computed ranks
			for (int i = 0; i < this.stimuli.length; i++) 
			{
				// get stimulus
				Resource stimulus = this.stimuli[i];
				// check ranking value
				int rank = ranking[i];
				// do not consider stimuli under the threshold
				if (rank >= threshold) 
				{
					try
					{
						// extract stimulation information
						
						// get stimulus name
						String stimulusName = stimulus.getProperty(this.kb.getProperty(OWLSirNameSpace.SIR.getNs() + "hasName")).getString();
						// get type
						String stimulusType = stimulus.getProperty(this.kb.getProperty(OWLSirNameSpace.RDF.getNs() + "type")).getObject().asResource().getURI();
						
						// create recommendation
						StimulationRecommendation rec = new StimulationRecommendation(
								profile.getURI(), 
								stimulus.getURI(), 
								stimulusName, 
								stimulusType, 
								rank);
						
						// check inferred interaction preferences
						List<Statement> slist = this.kb.getStatements(
								rec.getStimulationId(),
								OWLSirNameSpace.SIR.getNs() + "hasInteractionParameter",
								null);
						
						// check parameters
						for (Statement s : slist) 
						{
							// get statement object
							Resource region = s.getResource();
							
							// get parameter type statement
							Statement st = region.getProperty(this.kb.getProperty(OWLSirNameSpace.RDF.getNs() + "type"));
							// get value statement
							Statement sv = region.getProperty(this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "hasRegionDataValue"));
							
							// add parameter to the recommendation
							rec.addInteractionParameter(
									rec, 
									st.getResource().getLocalName(), 
									sv.getString());
						}
						
							
						// add an element to recommendations
						recoms.add(rec);
						
					}
					catch (Exception ex) {
						// skip stimulation
						System.err.println("Error while extracting information about stimuls : <" + stimulus.getURI() + ">\n\t- errro message: " + ex.getMessage());
						
						// list all properties concerning the individual
						Iterator<Statement> it = stimulus.listProperties();
						while (it.hasNext()) {
							System.err.println("\t- it: " + it.next());
						}
					}
				}
			}
			
			// sort rankings
			Collections.sort(recoms);
			
			// check debug flag
			if (this.debug)
			{
				// print computed incident matrix
				System.out.println("Ranking Matrix R(m,k)");
				System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
				for (int i = 0; i < ranking.length; i++) {
					System.out.print("\t" + ranking[i] + " ");
				}
				
				System.out.println("------------------------------------------------------------------------------------------------------------------------------------");

				// print recommendations
				System.out.println("Recommendations for profile " + profile);
				for (StimulationRecommendation r : recoms) {
					System.out.println(r);
				}
			}
		}
		
		// get sorted list
		return recoms;
	}
	
	
	
	
	/**
	 * 
	 * Compute the best recommendations for all profiles
	 * 
	 * @return
	 */
	public Map<String, List<StimulationRecommendation>> best()
	{
		// map of recommendations
		Map<String, List<StimulationRecommendation>> recoms = new HashMap<>();
		
		// get profiles
		for (int i = 0; i < this.profiles.length; i++)
		{
			// get current profile
			Resource profile = this.profiles[i];
			// compute profile best recommendations
			List<StimulationRecommendation> list =  this.best(profile);
			// add to results
			recoms.put(profile.getURI(), list);
		}
		
		// get sorted list
		return recoms;
	}
	
	
	
	/**
	 * 
	 * @param taxonomy
	 * @return
	 */
	private Set<Resource> getTaxonomyLeaves(Map<Resource, List<Resource>> taxonomy) 
	{
		// result set
		Set<Resource> leaves = new HashSet<Resource>();
		// extract leaves
		for (Resource res : taxonomy.keySet()) {
			// check if leaf
			if (taxonomy.get(res).isEmpty()) {
				leaves.add(res);
			}
		}
		
		// get result set
		return leaves;
	}
}
