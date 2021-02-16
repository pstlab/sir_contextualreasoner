package it.cnr.istc.pst.cognition.sir.app.kr2020;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
public class StimuliGenerator 
{
	private static final AtomicInteger ID = new AtomicInteger(0);
	private static final String STAT_FILE = "data/gen/stimuli_data.csv";
	private String folder;
	private OWLSirModel kb;
	
	// functioning quality index
	private List<String> index;
	
	/**
	 * 
	 * @param model
	 */
	public StimuliGenerator(OWLSirModel model) {
		this("etc/stimuli", model);
	}
	
	/**
	 * 
	 * @param folder
	 * @param model
	 */
	public StimuliGenerator(String folder, OWLSirModel model) 
	{
		try
		{
			// set fields
			this.folder = folder;
			this.kb = model;
			
			// set index
			this.index = new ArrayList<String>();
			// get taxonomy of stimulation functions
			Map<Resource, List<Resource>> taxonomy = this.kb.getTaxonomyOfStimulationFunctions();
			// index leaves of the taxonomy
			for (Resource res : taxonomy.keySet()) {
				// check if leaf
				if (taxonomy.get(res).isEmpty()) {
					// add an entry to the index
					if (!this.index.contains(res.getURI())) {
						this.index.add(res.getURI());
					}
				}
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	/**
	 * 
	 * Create a set of N stimuli support a maximum number of C functions
	 * 
	 * 
	 * @param n
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public String create(int n, int c) 
			throws Exception
	{
		// create profile ID
		long id = ID.getAndIncrement();
		// set XML file
		String xml = "<portfolio id=\"portfolio" + id + "\">\n";

		// count the number of stimuli
		int counter = 0;
		// count the number of stimulation functions
//		int stimFunctions = 0;
		Map<String, Integer> stimFunctions = new HashMap<>();
		// create a random number generator
		Random rand = new Random(System.currentTimeMillis());
		// add stimuli 
		for (int i = 0; i < n; i++)
		{
			// open measurement tag
			xml += "\t<element id=\"s" + counter + "\" type=\"" +  OWLSirNameSpace.SIR.getNs() + "Stimulus\">\n";
			
			// randomly decide the number of supported functions
			int funcs = rand.nextInt(c) + 1;
			// list of selected functions
			List<String> selected = new ArrayList<String>();
			while (funcs > 0) 
			{
				// get a random index of known stimulation functions
				int fIndex = rand.nextInt(this.index.size());
				// get stimulation function URI
				String functionURI = this.index.get(fIndex);
				// check if already selected
				if (!selected.contains(functionURI))
				{
					// add function among selected
					selected.add(functionURI);
					// check stimulation function counter
					if (!stimFunctions.containsKey(functionURI)) {
						// initialize
						stimFunctions.put(functionURI, new Integer(0));
					}
					
					// increment the number of stimulation functions
					int val = stimFunctions.get(functionURI).intValue();
					val++;
					// update 
					stimFunctions.put(functionURI, val);
					
					xml += "\t\t<element type=\"" + functionURI+ "\" />\n";
					
					// decrement the number of missing functions
					funcs--;
				}
				else {
					// skip
				}
			}
			
			// close tag
			xml += "\t</element>\n\n";
			counter++;
		}
		
		// close XML file 
		xml += "</portfolio>\n";
		
		
		// file name 
		String fileName = "stimuli_set_" + id + ".xml";
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
			// fileName;#stimuli;#stimulation_functions;#supported_functions
			fwriter.write(fileName + ";" + counter +  ";" + this.index.size() + ";" + stimFunctions.keySet().size() + "\n");
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
	 * Create M sets of stimuli each of which composed by N stimuli that support a maximum number of C functions
	 * 
	 * @param m
	 * @param n
	 * @param c
	 * @throws Exception
	 */
	public void create(int m, int n, int c) 
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
			fwriter.write("stimuli_set;#stimuli;#stimulation_functions;#supported_functions\n");
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
		for (int i = 0; i < m; i++) {
			// create a random profile instance
			String file = this.create(n, c);
			System.out.println("Generated stimulation portfolio file : " + file);
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
			StimuliGenerator gen = new StimuliGenerator(model);
			// create profile
			gen.create(10, 80, 5);
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
}
