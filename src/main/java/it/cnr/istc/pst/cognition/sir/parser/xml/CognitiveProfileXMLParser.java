package it.cnr.istc.pst.cognition.sir.parser.xml;

import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.jena.rdf.model.Resource;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirModel;
import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirNameSpace;

/**
 * 
 * @author alessandroumbrico
 *
 */
public class CognitiveProfileXMLParser 
{
	private OWLSirModel kb;
	
	/**
	 * 
	 * @param kb
	 */
	public CognitiveProfileXMLParser(OWLSirModel kb) {
		this.kb = kb;
	}
	
	/**
	 * 
	 * @param profileXMlFilePath
	 * @throws Exception
	 */
	public void parse(String profileXMlFilePath) 
			throws Exception
	{
		// parse the document file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		// parse XML document
		Document document = builder.parse(new FileInputStream(profileXMlFilePath));
		
		
		// prepare XPath expression
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xp = xpf.newXPath();
		
		// get information about cognitive profile 
		XPathExpression expression = xp.compile("//element[@type= '" + OWLSirNameSpace.SIR.getNs() + "CognitiveProfile']");
		NodeList profiles = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
		// read all profiles
		for (int pIndex = 0; pIndex < profiles.getLength(); pIndex++)
		{
			// get profile node
			Node profile = profiles.item(pIndex);
			// check attributes
			Attr profileType = (Attr) profile.getAttributes().getNamedItem("type");
			// get patient profile 
			expression = xp.compile("//element[@type= '" + profileType.getValue().trim()+ "']"
					+ "//element[@type= '" +  OWLSirNameSpace.SIR.getNs() + "Patient']");
			
			
			NodeList elements = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			// create patient individual
			Resource iPatient = this.kb.createIndividual(OWLSirNameSpace.SIR.getNs() + "Patient");
			// create profile individual  
			Resource iProfile = this.kb.createIndividual(OWLSirNameSpace.SIR.getNs() + "CognitiveProfile");
			// associate patient to profile
			this.kb.assertProperty(
					iProfile.getURI(), 
					OWLSirNameSpace.SIR.getNs() + "isProfileOf", 
					iPatient.getURI());
			
			// get measures
			expression = xp.compile("//element[@type= '" + profileType.getValue().trim()+ "']"
					+ "//element[@type= '" + OWLSirNameSpace.SIR.getNs() + "Measurement']");
			// get all profile measurements
			elements = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
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
				
				// the first element is the type of measured quality function
				Node nFunction = els.item(0);
				Attr fdType = (Attr) nFunction.getAttributes().getNamedItem("type"); 
				// the second element is the outcome of the measure
				Node outcome = els.item(1);
				Attr regionType = (Attr) outcome.getAttributes().getNamedItem("type");
				// create individual measurement
				Resource iMeasure = this.kb.createIndividual(OWLSirNameSpace.SIR.getNs() + "Measurement");
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
		}
	}
}
