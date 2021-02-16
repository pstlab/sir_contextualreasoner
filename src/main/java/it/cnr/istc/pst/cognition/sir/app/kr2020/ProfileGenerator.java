package it.cnr.istc.pst.cognition.sir.app.kr2020;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.rdf.model.Resource;

import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirModel;
import it.cnr.istc.pst.cognition.sir.semantic.owl.jena.OWLSirNameSpace;

/**
 * 
 * @author alessandroumbrico
 *
 */
public class ProfileGenerator 
{
	private static final AtomicInteger ID = new AtomicInteger(0);
	private static final String STAT_FILE = "data/gen/profiles_data.csv";
	private String folder;
	private OWLSirModel kb;
	
	// functioning quality index
	private Map<String, Resource> index;
	
	/**
	 * 
	 * @param model
	 */
	public ProfileGenerator(OWLSirModel model) {
		this("etc/profiles", model);
	}
	
	/**
	 * 
	 * @param folder
	 * @param model
	 */
	public ProfileGenerator(String folder, OWLSirModel model) 
	{
		try
		{
			// set fields
			this.folder = folder;
			this.kb = model;
			
			// set index
			this.index = new HashMap<String, Resource>();
			// get taxonomy of functioning qualities
			Map<Resource, List<Resource>> taxonomy = this.kb.getTaxonomyOfFunctioningQualities();
			// index leaves of the taxonomy
			for (Resource res : taxonomy.keySet()) {
				// check if leaf
				if (taxonomy.get(res).isEmpty()) {
					// add an entry to the index
					this.index.put(res.getURI(), res);
				}
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	/**

	 * Create an ICF-based profile 
	 * 
	 * @return
	 * @throws Exception
	 */
	public String create() 
			throws Exception
	{
		// create profile ID
		long id = ID.getAndIncrement();
		// set XML file
		String xml = "<assistance>\n";
		
		// open profile tag
		xml += "\t<element id=\"p" + id +"\" type=\"" + OWLSirNameSpace.SIR.getNs() + "CognitiveProfile\">\n";
		
		// add patient tag
		xml += "\t\t<element type=\"" + OWLSirNameSpace.SIR.getNs() + "Patient\">\n";
		xml += "\t\t\tProfileId" + id + "\n";
		xml += "\t\t</element>\n\n";
		
		// count the number of impairments
		int impairments = 0;
		// measurement counter
		int counter = 0;
		// create a random number generator
		Random rand = new Random(System.currentTimeMillis());
		// add a measurement for each indexed leaf
		for (Resource leaf : this.index.values())
		{
			// get a random value
			int value = rand.nextInt(7);
			// open measurement tag
			xml += "\t\t<element id=\"m" + counter + "\" type=\"" +  OWLSirNameSpace.SIR.getNs() + "Measurement\">\n";
			
			xml += "\t\t\t<element type=\"" + leaf.getURI() + "\" />\n";
			
			xml += "\t\t\t<element type=\"" + OWLSirNameSpace.SIR.getNs() + "FunctioningQualityRegion\">\n";
			xml += "\t\t\t\t" + value + "\n";
			xml += "\t\t\t</element>\n";
			
			// close tag
			xml += "\t\t</element>\n\n";
			counter++;
			
			// check value
			if (value > 0 && value < 6) {
				impairments++;
			}
		}
		
		
		// close profile tag
		xml += "\t</element>\n";
		
		// close XML file 
		xml += "</assistance>\n";
		
		
		// file name 
		String fileName = "profile_" + id + ".xml";
		// create a new XML file
		File profileFile = new File(this.folder + "/" + fileName);
		// create writer
		BufferedWriter writer = null; 
		try
		{	
			// create writer
			writer = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(profileFile), "UTF-8"));
			// write file
			writer.write(xml);
			writer.flush();
		}
		catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		
		// create statistic data
		File stat = new File(STAT_FILE);
		// create writer in append mode
		FileWriter fwriter = null;
		try
		{ 
			// create writer
			fwriter = new FileWriter(stat, true);
			// fileName;#measures;#qualities;#impairments
			fwriter.write(fileName + ";" + counter +  ";" + this.index.values().size() + ";" + impairments + "\n");
			fwriter.flush();
		}
		catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		finally {
			if (fwriter != null) {
				fwriter.close();
			}
		}
		
		// get generated profile file path
		return this.folder + "/" + fileName;
	}
	
	
	/**
	 * 
	 * Create N instances of ICF-based profile 
	 * 
	 * @throws Exception
	 */
	public void create(int n) 
			throws Exception
	{
		// create statistic data
		File stat = new File(STAT_FILE);
		if (stat.exists()) {
			// remove file 
			stat.delete();
		}
		
		// create writer in append mode
		FileWriter fwriter = null;
		try
		{ 
			// create writer
			fwriter = new FileWriter(stat, true);
			// write CSV file header
			fwriter.write("profile;#measures;#qualities;#impairments\n");
			fwriter.flush();
		}
		catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		finally {
			if (fwriter != null) {
				fwriter.close();
			}
		}
		
		// generate profiles
		for (int i = 0; i < n; i++) {
			// create a random profile instance
			String file = this.create();
			System.out.println("Generated profile file : " + file);
		}
	}
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			// create model 
			OWLSirModel model = new OWLSirModel(
			        		"etc/ontology/sir.owl",
			        		"etc/ontology/stimulations_v1.0.rules");
			
			// create profile generator
			ProfileGenerator gen = new ProfileGenerator(model);
			// create profile
			gen.create(10);
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
}
