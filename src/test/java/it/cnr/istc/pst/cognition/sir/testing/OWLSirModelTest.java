package it.cnr.istc.pst.cognition.sir.testing;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.cnr.istc.pst.cognition.sir.app.kr2020.ProfileGenerator;
import it.cnr.istc.pst.cognition.sir.app.kr2020.StimuliGenerator;
import it.cnr.istc.pst.cognition.sir.parser.xml.CognitiveProfileXMLParser;
import it.cnr.istc.pst.cognition.sir.parser.xml.StimulationPortfolioXMLParser;
import it.cnr.istc.pst.cognition.sir.reasoner.RecommenderSystem;
import it.cnr.istc.pst.cognition.sir.reasoner.StimulationRecommendation;
import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirModel;
import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirNameSpace;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;



/**
 * 
 * Unit test for simple App.
 */
public class OWLSirModelTest extends TestCase
{
	private OWLSirModel kb; 
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public OWLSirModelTest( String testName )
    {
        super( testName );
        // set owl model
        this.kb = new OWLSirModel(
        		"etc/ontology/sir_stimulation_v1.2.owl",
        		"etc/ontology/sir_stimulations_v1.0.rules");
        
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
    	// setup test suite
        return new TestSuite( OWLSirModelTest.class );
    }

    
    /**
     * T1 - check correct access to the ontology and setup of the KB
     */
    public void testModelSetup()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());
        
        // get an iterator
        Iterator<Statement> it = this.kb.iterator();
        assertNotNull(it);
    }
    
    /**
     * T2 - check the taxonomy of stimulation functions 
     */
    public void testCheckTaxonomyOfStimulationFunctions()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());

        // get the taxonomy 
        Map<Resource, List<Resource>> taxonomy = new HashMap<Resource, List<Resource>>();
        
        // retrieve the root of the taxonomy
        Resource root = this.kb.getResource(OWLSirNameSpace.SIR.getNs() + "StimulationFunction");
        assertNotNull(root);
        // add the root into the search fringe
        List<Resource> fringe = new ArrayList<Resource>();
        fringe.add(root);
        
        // list of taxonomy elements to expand
        try
        {
        	while (!fringe.isEmpty())
        	{
        		// remove the next element from the fringe
        		Resource parent = fringe.remove(0);
        		assertNotNull(parent);
        		System.out.println("Current taxonomy node: " + parent.getURI());
        		
        		// update the graph
        		if (!taxonomy.containsKey(parent)) {
        			taxonomy.put(parent, new ArrayList<Resource>());
        		}
        		
        		// extract taxonomy children
    	        List<Statement> list = this.kb.getStatements(
    	        		OWLSirNameSpace.RDFS.getNs() + "subClassOf",
    	        		parent.getURI());
    	        
    	        // check the list
    	        assertNotNull(list);
    	        for (Statement s : list) 
    	        {
    	        	// skip reflexive relationships
    	        	if (!s.getSubject().equals(s.getResource()))
    	        	{
	    	        	// get child resource
	    	        	Resource child = s.getSubject();
	    	        	assertNotNull(child);
	    	        	System.out.println("\tFound child: " + child.getURI());
	    	        	
	    	        	// update the taxonomy graph
	    	        	taxonomy.get(parent).add(child);
	    	        	// add the child to the fringe
	    	        	fringe.add(child);
    	        	}
    	        }
        	}
        	
        	// check the taxonomy
	        assertNotNull(taxonomy);
	        assertFalse(taxonomy.isEmpty());
	        
	        // get the set of taxonomy leaves
	        Set<Resource> leaves = new HashSet<Resource>();
	        for (Resource node : taxonomy.keySet()) {
	        	// check children
	        	if (taxonomy.get(node).isEmpty()) {
	        		// print leaf
	        		System.out.println("Taxonomy leaf: " + node.getURI());
	        		// add leaf
	        		leaves.add(node);
	        	}
	        }
	        
	        assertFalse(leaves.isEmpty());
	        assertTrue(leaves.size() > 10);
        }
        catch (Exception ex) {
        	System.err.println(ex.getMessage());
        	assertFalse(true);
        }
        
    }
    
    
    /**
     * T3 - check the taxonomy of functioning qualities 
     */
    public void testCheckTaxonomyOfFunctioningQualities()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());

        try
        {
            // get the taxonomy 
            Map<Resource, List<Resource>> taxonomy = 
            		this.kb.getTaxonomyOfFunctioningQualities();
        	
        	// check the taxonomy
	        assertNotNull(taxonomy);
	        assertFalse(taxonomy.isEmpty());
	        
	        // get the set of taxonomy leaves
	        Set<Resource> leaves = new HashSet<Resource>();
	        for (Resource node : taxonomy.keySet()) {
	        	// check children
	        	if (taxonomy.get(node).isEmpty()) {
	        		// print leaf
	        		System.out.println("Taxonomy leaf: " + node.getURI());
	        		// add leaf
	        		leaves.add(node);
	        	}
	        }
	        
	        assertFalse(leaves.isEmpty());
	        assertTrue(leaves.size() > 30);
        }
        catch (Exception ex) {
        	System.err.println(ex.getMessage());
        	assertFalse(true);
        }
        
    }

    /**
     * T4 - store into the KB a patient profile
     */
    public void testReadPatientProfileExample()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());
        
    	try
		{
			// parse the document file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			// parse XML document
			Document document = builder.parse(new FileInputStream(
					"etc/test/profile.xml"));
			
			
			// prepare XPath expression
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xp = xpf.newXPath();
			
			// get information about cognitive profile 
			XPathExpression expression = xp.compile("//element[@type= '" + OWLSirNameSpace.SIR.getNs() + "CognitiveProfile']");
			NodeList elements = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			assertNotNull(elements);
			assertTrue(elements.getLength() == 1);
			
			// get profile node
			Node profile = elements.item(0);
			assertNotNull(profile);
			// get id 
			Attr id = (Attr) profile.getAttributes().getNamedItem("id");
			Attr profileType = (Attr) profile.getAttributes().getNamedItem("type");
			assertNotNull(id);
			assertTrue(id.getValue().equals("p1"));
			System.out.println("Example patient profile [id = \"" + id.getValue() +"\"]");
			
			// get patient profile 
			expression = xp.compile("//element[@type= '" + profileType.getValue().trim()+ "']"
					+ "//element[@type= '" +  OWLSirNameSpace.SIR.getNs() + "Patient']");
			elements = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			assertNotNull(elements);
			assertTrue(elements.getLength() == 1);
			
			// get patient node
			Node patient = elements.item(0);
			assertNotNull(patient);
			System.out.println("Example patient name [name = \"" + patient.getTextContent().trim() + "\"]");
			assertTrue(patient.getTextContent().trim().equals("Example Patient"));
			
			
			// create patient individual
			Resource iPatient = this.kb.createIndividual(OWLSirNameSpace.SIR.getNs() + "Patient");
			assertNotNull(iPatient);
			System.out.println("New individual : " + iPatient);
			// create profile individual  
			Resource iProfile = this.kb.createIndividual(OWLSirNameSpace.SIR.getNs() + "CognitiveProfile");
			assertNotNull(iProfile);
			System.out.println("New individual : " + iProfile.getURI());
			// associate patient to profile
			this.kb.assertProperty(
					iProfile.getURI(), 
					OWLSirNameSpace.SIR.getNs() + "isProfileOf", 
					iPatient.getURI());
			
			// retrieve statement
			List<Statement> list = this.kb.getStatements(OWLSirNameSpace.SIR.getNs() + "isProfileOf");
			assertNotNull(list);
			assertTrue(list.size() == 1);
			
			
			// get measures
			expression = xp.compile("//element[@type= '" + profileType.getValue().trim()+ "']"
					+ "//element[@type= '" + OWLSirNameSpace.SIR.getNs() + "Measurement']");
			// get all profile measurements
			elements = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			assertNotNull(elements);
			assertTrue(elements.getLength() == 7);
			
			// assert measurements
			for (int index = 0; index < elements.getLength(); index++) 
			{
				// get measurement node
				Node mNode = elements.item(index);
				// get measure id
				Attr mId = (Attr) mNode.getAttributes().getNamedItem("id");
				Attr mType = (Attr) mNode.getAttributes().getNamedItem("type");
				
				
				
				// get measured outcomes
				XPathExpression e = xp.compile("//element[@type= '" + profileType.getValue().trim()+ "']"
						+ "//element[@type= '" + mType.getValue() + "' and @id= '" + mId.getValue() + "']"
						+ "//element");
				// get all profile measurements
				NodeList els = (NodeList) e.evaluate(document, XPathConstants.NODESET);
				assertNotNull(els);
				assertTrue(els.getLength() == 2);
				
				// the first element is the type of measured quality function
				Node nFunction = els.item(0);
				Attr fdType = (Attr) nFunction.getAttributes().getNamedItem("type"); 
				
				// the second element is the outcome of the measure
				Node outcome = els.item(1);
				Attr regionType = (Attr) outcome.getAttributes().getNamedItem("type");
				
				// print data
				System.out.println(">> Measurement ["
						+ "id= " + mId.getValue() + ", "
						+ "value= " + Integer.parseInt(outcome.getTextContent().trim()) + ","
						+ "quality= " + fdType.getValue() + ","
						+ "regionType = " + regionType.getValue() + "]");
				
				// create individual measurement
				Resource iMeasure = this.kb.createIndividual(OWLSirNameSpace.SIR.getNs() + "Measurement");
				assertNotNull(iMeasure);
				System.out.println("New individual : " + iMeasure);
				// associate the measure to the profile
				this.kb.assertProperty(
						iProfile.getURI(), 
						OWLSirNameSpace.SIR.getNs() + "hasPart", 
						iMeasure.getURI());
				
				// assert properties concerning the measurement
				this.kb.assertProperty(
						iMeasure.getURI(), 
						OWLSirNameSpace.DUL.getNs() + "hasConstituent", 
						iPatient.getURI());
				
				this.kb.assertProperty(
						iMeasure.getURI(), 
						OWLSirNameSpace.DUL.getNs() + "isRelatedToDescription", 
						iProfile.getURI());
				
				this.kb.assertProperty(
						iMeasure.getURI(), 
						OWLSirNameSpace.SIR.getNs() + "measures", 
						fdType.getValue());
				
				// create region individual to store the measured value
				Resource iRegion = this.kb.createIndividual(regionType.getValue());
				
				this.kb.assertProperty(
						iMeasure.getURI(), 
						OWLSirNameSpace.SIR.getNs() + "measurementOutcome", 
						iRegion.getURI());
				
				// assert data property
				this.kb.assertDataProperty(
						iRegion.getURI(), 
						OWLSirNameSpace.SIR.getNs() + "hasICFScore", 
						Integer.parseInt(outcome.getTextContent().trim()));
			}
			
			
			// check asserted properties
			list = this.kb.getStatements(
					iProfile.getURI(),
					OWLSirNameSpace.SIR.getNs() + "hasPart",
					null);
			
			// check data
			assertNotNull(list);
			assertTrue(list.size() == 7);
			
			
			// check asserted properties
			list = this.kb.getStatements(
					OWLSirNameSpace.DUL.getNs() + "hasConstituent",
					iPatient.getURI());
			
			// check data
			assertNotNull(list);
			assertTrue(list.size() == 7);
			
			// check asserted properties
			list = this.kb.getStatements(OWLSirNameSpace.SIR.getNs() + "hasICFScore");
			// check data
			assertNotNull(list);
			assertTrue(list.size() == 7);
			for (Statement s : list) {
				System.out.println(s);
			}
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
			assertTrue(false);
		}
    	
    }


    /**
     * T5 - store into the KB information about stimuli
     */
    public void testReadPortfolioExample()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());
        
    	try
		{
			// parse the document file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			// parse XML document
			Document document = builder.parse(new FileInputStream(
					"etc/test/stimuli.xml"));
			
			
			// prepare XPath expression
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xp = xpf.newXPath();
			
			// get information about cognitive profile 
			XPathExpression expression = xp.compile("//element[@type= '" + OWLSirNameSpace.SIR.getNs() + "Stimulus']");
			NodeList elements = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			assertNotNull(elements);
			assertTrue(elements.getLength() == 5);
			
			// assert measurements
			for (int index = 0; index < elements.getLength(); index++) 
			{
				// get measurement node
				Node sNode = elements.item(index);
				// get measure id
				Attr sId = (Attr) sNode.getAttributes().getNamedItem("id");
				Attr sType = (Attr) sNode.getAttributes().getNamedItem("type");
				
				// print data
				System.out.println(">> Stimulus ["
						+ "id= " + sId.getValue() + ", "
						+ "type= " + sType.getValue() + "]");
				
				// create stimulus individual  
				Resource iStimulus = this.kb.createIndividual(sType.getValue());
				
				// get supported functioning qualities outcomes
				XPathExpression e = xp.compile("//element[@type= '" + sType.getValue().trim()+ "' and @id='" + sId.getValue() + "']"
						+ "//element");
				// get all supported qualities
				NodeList els = (NodeList) e.evaluate(document, XPathConstants.NODESET);
				assertNotNull(els);
				assertTrue(els.getLength() > 0);
				
				for (int jndex = 0; jndex < els.getLength(); jndex++)
				{
					// get supported functioning quality
					Node fNode = els.item(jndex);
					// get functioning quality
					Attr quality = (Attr) fNode.getAttributes().getNamedItem("type");
					
					// assert property
					this.kb.assertProperty(
							iStimulus.getURI(), 
							OWLSirNameSpace.DUL.getNs() + "hasConstituent", 
							quality.getValue());
				}
			}
			
			
			// check asserted individuals
			List<Resource> list = this.kb.getIndividuals(OWLSirNameSpace.SIR.getNs() + "Stimulus");
			// check data
			assertNotNull(list);
			assertTrue(list.size() == 5);
			for (Resource r : list) 
			{
				System.out.println(r);
				// get property
				Property p = this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "hasConstituent");
				// check resource associated properties
				Iterator<Statement> it = r.listProperties(p);
				while (it.hasNext()) {
					System.out.println("\t" + it.next());
				}
			}
			
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
			assertTrue(false);
		}
    }

    /**
     * T6 - evaluate inference of patient's status
     */
    public void testInferenceOfImpairments()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());
        
    	try
		{
    		// create profile parser
    		CognitiveProfileXMLParser parser = new CognitiveProfileXMLParser(this.kb);
    		// parse profile 
    		parser.parse("etc/test/profile.xml");
    		
    		// check inferred knowledge 
    		List<Statement> list = this.kb.getStatements(
    				OWLSirNameSpace.RDF.getNs() + "type", 
    				OWLSirNameSpace.SIR.getNs() + "CognitiveImpairment");
    		// check found statements
    		assertNotNull(list);
    		assertTrue(list.size() > 0);
    		// print statements
    		for (Statement s : list) 
    		{
    			// statement
    			System.out.println(s);
    			
    			// get property 
    			Property p = this.kb.getProperty(OWLSirNameSpace.SIR.getNs() + "concernsQuality");
    			// get patient
    			Iterator<Statement> it = s.getSubject().listProperties(p);
    			assertTrue(it.hasNext());
    			// get next
    			Resource quality = it.next().getResource();
    			assertFalse(it.hasNext());
    			System.out.println("\tQuality: " + quality.getURI());
    			
    			
    			// get property 
    			p = this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "satisfies");
    			// get patient
    			it = s.getSubject().listProperties(p);
    			assertTrue(it.hasNext());
    			// get next
    			Resource measure = it.next().getResource();
    			assertFalse(it.hasNext());
    			System.out.println("\tMeasure: " + measure.getURI());
    			
    		}
    		
		}
    	catch (Exception ex) {
    		System.err.println(ex.getMessage());
    		assertTrue(false);
    	}
    }
    
    /**
     * T7 - evaluate inference of stimulation opportunities
     */
    public void testInferenceOfStimulationOpportunities()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());
        
    	try
		{
    		// create parser of stimuli
    		StimulationPortfolioXMLParser sParser = new StimulationPortfolioXMLParser(this.kb);
    		// parse description of stimuli
    		sParser.parse("etc/test/stimuli.xml");
    		
    		// create profile parser
    		CognitiveProfileXMLParser cPerser = new CognitiveProfileXMLParser(this.kb);
    		// parse profile 
    		cPerser.parse("etc/test/profile.xml");
    		
    		
    		
    		// check inferred knowledge 
    		List<Statement> list = this.kb.getStatements(
    				OWLSirNameSpace.RDF.getNs() + "type", 
    				OWLSirNameSpace.SIR.getNs() + "StimulationOpportunity");
    		// check found statements
    		assertNotNull(list);
    		assertTrue(list.size() > 0);
    		// print statements
    		for (Statement s : list) 
    		{
    			// get property 
    			Property p = this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "classifies");
    			// get patient
    			Iterator<Statement> it = s.getSubject().listProperties(p);
    			assertTrue(it.hasNext());
    			System.out.println(it.next());
    			assertTrue(it.hasNext());
    			System.out.println(it.next());
    		}
    		
		}
    	catch (Exception ex) {
    		System.err.println(ex.getMessage());
    		assertTrue(false);
    	}
    }


    /**
     * T8 - evaluate inference on randomly generated profile
     */
    public void testInferenceOfImpairmentsOnRandomProfile()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());
        
    	try
		{
			// create profile generator
			ProfileGenerator gen = new ProfileGenerator(this.kb);
			// create a random profile profile
			String file = gen.create();
    		
    		// create profile parser
    		CognitiveProfileXMLParser parser = new CognitiveProfileXMLParser(this.kb);
    		// parse profile 
    		parser.parse(file);
    		
    		// check inferred knowledge 
    		List<Statement> list = this.kb.getStatements(
    				OWLSirNameSpace.RDF.getNs() + "type", 
    				OWLSirNameSpace.SIR.getNs() + "CognitiveImpairment");
    		// check found statements
    		assertNotNull(list);
    		// print statements
    		for (Statement s : list) 
    		{
    			// statement
    			System.out.println(s);
    			
    			// get property 
    			Property p = this.kb.getProperty(OWLSirNameSpace.SIR.getNs() + "concernsQuality");
    			// get patient
    			Iterator<Statement> it = s.getSubject().listProperties(p);
    			assertTrue(it.hasNext());
    			// get next
    			Resource quality = it.next().getResource();
    			assertFalse(it.hasNext());
    			System.out.println("\tQuality: " + quality.getURI());
    			
    			
    			// get property 
    			p = this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "satisfies");
    			// get patient
    			it = s.getSubject().listProperties(p);
    			assertTrue(it.hasNext());
    			// get next
    			Resource measure = it.next().getResource();
    			assertFalse(it.hasNext());
    			System.out.println("\tMeasure: " + measure.getURI());
    			
    		}
    		
		}
    	catch (Exception ex) {
    		System.err.println(ex.getMessage());
    		assertTrue(false);
    	}
    }
    
    
    
    /**
     * T9 - evaluate inference of stimulation opportunities on randomly generated scenario
     */
    public void testInferenceStimulationOpportunityOnRandomScenario()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());
        
    	try
		{
			StimuliGenerator sGen = new StimuliGenerator(this.kb);
			// create a random set of (N) stimuli
			String sFile = sGen.create(50, 5);
    		
			// create parser of stimuli
    		StimulationPortfolioXMLParser sParser = new StimulationPortfolioXMLParser(this.kb);
    		// parse description of stimuli
    		sParser.parse(sFile);
    		
    		
    		// create profile generator
			ProfileGenerator gen = new ProfileGenerator(this.kb);
			// create a random profile profile
			String pFile = gen.create();
    					
    		// create profile parser
    		CognitiveProfileXMLParser cPerser = new CognitiveProfileXMLParser(this.kb);
    		// parse profile 
    		cPerser.parse(pFile);
    		
    		// check inferred knowledge 
    		List<Statement> list = this.kb.getStatements(
    				OWLSirNameSpace.RDF.getNs() + "type", 
    				OWLSirNameSpace.SIR.getNs() + "StimulationOpportunity");
    		// check found statements
    		assertNotNull(list);
    		// print statements
    		for (Statement s : list) 
    		{
    			// get property 
    			Property p = this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "classifies");
    			// get patient
    			Iterator<Statement> it = s.getSubject().listProperties(p);
    			assertTrue(it.hasNext());
    			System.out.println(it.next());
    			assertTrue(it.hasNext());
    			System.out.println(it.next());
    		}
		}
    	catch (Exception ex) {
    		System.err.println(ex.getMessage());
    		assertTrue(false);
    	}
    }



    /**
     * T10 - evaluate recommendation capabilities on a randomly generated scenario
     */
    public void testRecommendationsOnRandomScenario()
    {
    	// check 
        assertNotNull(this.kb);
        // check validity
        assertTrue(this.kb.validate());
        
    	try
		{
			StimuliGenerator sGen = new StimuliGenerator(this.kb);
			// create a random set of (N) stimuli  
			String sFile = sGen.create(100, 5);
    		
			// create parser of stimuli
    		StimulationPortfolioXMLParser sParser = new StimulationPortfolioXMLParser(this.kb);
    		// parse description of stimuli
    		sParser.parse(sFile);
    		
    		
    		// create profile generator
			ProfileGenerator gen = new ProfileGenerator(this.kb);
			// create a random profile profile
			String pFile = gen.create();
    					
    		// create profile parser
    		CognitiveProfileXMLParser cPerser = new CognitiveProfileXMLParser(this.kb);
    		// parse profile 
    		cPerser.parse(pFile);
    		
    		// check inferred knowledge 
    		List<Statement> list = this.kb.getStatements(
    				OWLSirNameSpace.RDF.getNs() + "type", 
    				OWLSirNameSpace.SIR.getNs() + "StimulationOpportunity");
    		// check found statements
    		assertNotNull(list);
    		// print statements
    		for (Statement s : list) 
    		{
    			// get property 
    			Property p = this.kb.getProperty(OWLSirNameSpace.DUL.getNs() + "classifies");
    			// get patient
    			Iterator<Statement> it = s.getSubject().listProperties(p);
    			assertTrue(it.hasNext());
    			System.out.println(it.next());
    			assertTrue(it.hasNext());
    			System.out.println(it.next());
    		}
    		
    		
    		// create recommendation system
    		RecommenderSystem rs = new RecommenderSystem(this.kb);
    		// compute recommendations
    		Map<String, List<StimulationRecommendation>> recs = rs.compute();
    		assertNotNull(recs);
    		assertTrue(recs.size() == 1);
    		assertTrue(recs.values().size() == 1);
    		
    		// compute best recommendations
    		Map<String, List<StimulationRecommendation>> best = rs.best();
    		assertNotNull(best);
    		assertTrue(best.size() == 1);
    		assertTrue(best.values().size() == 1);
    		
    		for (String key : recs.keySet()) {
    			// compare the sizes of the two lists
    			System.out.println("#recms = " + recs.get(key).size() + " : #best = " + best.get(key).size());
    			assertTrue(best.get(key).size() <= recs.get(key).size());
    		}
		}
    	catch (Exception ex) {
    		System.err.println(ex.getMessage());
    		assertTrue(false);
    	}
    }
}
