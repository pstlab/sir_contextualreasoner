package it.cnr.istc.pst.cognition.sir.parser.xml;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

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
public class StimulationPortfolioXMLParser 
{
	private OWLSirModel kb;
	private List<Resource> resources;
	
	/**
	 * 
	 * @param kb
	 */
	public StimulationPortfolioXMLParser(OWLSirModel kb) {
		this.kb = kb;
		this.resources = new ArrayList<>();
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Resource> getResources() {
		return new ArrayList<>(this.resources);
	}
	
	/**
	 * 
	 * @param portfolioXMlFilePath
	 * @throws Exception
	 */
	public void parse(String portfolioXMlFilePath) 
			throws Exception
	{
		// parse the document file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		// parse XML document
		Document document = builder.parse(new FileInputStream(portfolioXMlFilePath));
		
		
		// prepare XPath expression
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xp = xpf.newXPath();
		
		// get information about cognitive profile 
		XPathExpression expression = xp.compile("//element[@type= '" + OWLSirNameSpace.SIR.getNs() + "Stimulus']");
		NodeList elements = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
		
		// assert measurements
		for (int index = 0; index < elements.getLength(); index++) 
		{
			// get measurement node
			Node sNode = elements.item(index);
			// get measure id
			Attr sId = (Attr) sNode.getAttributes().getNamedItem("id");
			Attr sType = (Attr) sNode.getAttributes().getNamedItem("type");
			Attr sName = (Attr) sNode.getAttributes().getNamedItem("name");
			
			
			// create stimulus individual  
			Resource iStimulus = this.kb.createIndividual(sType.getValue());
			// add stimulus
			this.resources.add(iStimulus);
			// check if null
			if (sName != null) {
				// assert data property
				this.kb.assertDataProperty(
						iStimulus.getURI(), 
						OWLSirNameSpace.SIR.getNs() + "hasName", 
						sName.getValue());
			}
			
			// get supported functioning qualities outcomes
			XPathExpression e = xp.compile("//element[@type= '" + sType.getValue().trim()+ "' and @id='" + sId.getValue() + "']"
					+ "//element");
			// get all supported qualities
			NodeList els = (NodeList) e.evaluate(document, XPathConstants.NODESET);
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
		
		// get the list
//		return list;
	}
}
