package it.cnr.istc.pst.cognition.sir.semantic.owl.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import it.cnr.istc.pst.cognition.semantic.owl.jena.OWLModel;

/**
 * Hello world!
 *
 */
public class OWLSirModel extends OWLModel 
{
	/**
	 * 
	 * @param ontologyFile
	 * @param inferenceRuleFile
	 */
	public OWLSirModel(String ontologyFile, String inferenceRuleFile) {
		super(ontologyFile,	inferenceRuleFile, OWLSirNameSpace.SIR.getNs());
	}
	
	/**
	 * 
	 * @param model
	 */
	public void setup(Model model) {
		// setup the encapsulated semantic model
		super.setup(model, OWLSirNameSpace.SIR.getNs());
	}
	
	/**
	 * 
	 * @return
	 */
	public Model getModel() {
		// get the encapsulated semantic model
		return super.getModel(OWLSirNameSpace.SIR.getNs());
	}
	
	/**
	 * 
	 * @param rootURI
	 * @return
	 * @throws Exception
	 */
	private Map<Resource, List<Resource>> getTaxonomyFromRoot(String rootURI) 
			throws Exception
	{
		// get the taxonomy 
        Map<Resource, List<Resource>> taxonomy = new HashMap<Resource, List<Resource>>();
        // retrieve the root of the taxonomy
        Resource root = this.getResource(
        		rootURI);
        
        // add the root into the search fringe
        List<Resource> fringe = new ArrayList<Resource>();
        fringe.add(root);
        
    	while (!fringe.isEmpty())
    	{
    		// remove the next element from the fringe
    		Resource parent = fringe.remove(0);
    		// update the graph
    		if (!taxonomy.containsKey(parent)) {
    			taxonomy.put(parent, new ArrayList<Resource>());
    		}
    		
    		// extract taxonomy children
	        List<Statement> list = this.getStatements(
	        		OWLSirNameSpace.RDFS.getNs() + "subClassOf",
	        		parent.getURI());
	        
	        // check the list
	        for (Statement s : list) 
	        {
	        	// skip reflexive relationships
	        	if (!s.getSubject().equals(s.getResource()))
	        	{
    	        	// get child resource
    	        	Resource child = s.getSubject();
    	        	// update the taxonomy graph
    	        	taxonomy.get(parent).add(child);
    	        	// add the child to the fringe
    	        	fringe.add(child);
	        	}
	        }
    	}
    	
    	// get taxonomy
    	return taxonomy;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<Resource, List<Resource>> getTaxonomyOfStimulationFunctions() 
			throws Exception
	{
		// get taxonomy of stimulation functions
		return this.getTaxonomyFromRoot(
				OWLSirNameSpace.SIR_TAXONOMY_OF_STIMULATION_FUNCTIONS_ROOT.getNs());
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<Resource, List<Resource>> getTaxonomyOfFunctioningQualities() 
			throws Exception
	{
		// get taxonomy of stimulation functions
		return this.getTaxonomyFromRoot(
				OWLSirNameSpace.SIR_TAXONOMY_OF_FUNCTIONING_QUALITIES_ROOT.getNs());
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Set<Resource> getStimuli() 
			throws Exception
	{
		// get statements
		List<Resource> list = this.getIndividuals(OWLSirNameSpace.SIR.getNs() + "Stimulus");
		// get result set
		return new HashSet<Resource>(list);
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Set<Resource> getProfiles() 
			throws Exception
	{
		// get statements
		List<Resource> list = this.getIndividuals(OWLSirNameSpace.SIR.getNs() + "CognitiveProfile");
		// get result set
		return new HashSet<Resource>(list);
	}
	
	/**
	 * 
	 * @param profile
	 * @param quality
	 * @return
	 */
	public int getMeasurementOutcome(Resource profile, Resource quality)
	{
		// set null value
		int value = 0;
		// get all measurement of a profile
		Iterator<Statement> it = profile.listProperties(
				this.infModel.getProperty(
						OWLSirNameSpace.SIR.getNs() + "hasPart"));
		// flag
		boolean found = false;
		// check statements
		while (it.hasNext() && !found)
		{
			// get statement
			Statement s1 = it.next();
			// get measure
			Resource measure = s1.getResource();
			// check measured quality
			Iterator<Statement> it1 = measure.listProperties(
					this.infModel.getProperty(
							OWLSirNameSpace.SIR.getNs() + "measures"));
			
			// check measured qualities
			while (it1.hasNext() && !found) 
			{
				// get next
				Statement s2 = it1.next();
				// compare measured quality with the expected one
				if (quality.getURI().equals(s2.getResource().getURI())) 
				{
					// get the measured value 
					Iterator<Statement> it2 = measure.listProperties(
							this.infModel.getProperty(
									OWLSirNameSpace.SIR.getNs() + "measurementOutcome"));
					// check outcomes (one expected for each measure) 
					if (it2.hasNext()) {
						// get statement
						Statement s3 = it2.next();
						// get measure outcome
						Iterator<Statement> it3 = s3.getResource().listProperties(
								this.infModel.getProperty(
										OWLSirNameSpace.SIR.getNs() + "hasICFScore"));
						
						// get the outcome (one expected)
						if (it3.hasNext()) {
							// get statement
							Statement s4 = it3.next();
							// get value
							value = s4.getInt();
							// set flag
							found = true;
						}
					}
				}
			}
		}
		
		// get the value
		return value;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Resource getResourceById(Long id) 
			throws Exception {
		// get a resource within the domain name space
		return super.getResourceById(id, OWLSirNameSpace.SIR.getNs());
	}
}
