package it.cnr.istc.pst.cognition.sir.semantic.owl.jena;

/**
 * 
 * @author alessandro
 *
 */
public enum OWLSirNameSpace 
{
	/**
	 * 
	 */
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
	
	/**
	 * 
	 */
	RDFS("http://www.w3.org/2000/01/rdf-schema#"),
	
	/**
	 * 
	 */
	DUL("http://www.loa-cnr.it/ontologies/DUL.owl#"),
	
	/**
	 * 
	 */
	SSN("http://purl.oclc.org/NET/ssnx/ssn#"),

	/**
	 * 
	 */
	SIR("http://pst.istc.cnr.it/ontologies/2020/01/sir#"),
	
	/**
	 * 
	 */
	SIR_TAXONOMY_OF_STIMULATION_FUNCTIONS_ROOT(SIR + "StimulationFunction"),
	
	/**
	 * 
	 */
	SIR_TAXONOMY_OF_FUNCTIONING_QUALITIES_ROOT(SIR + "FunctioningQuality");;
	
	private String ns;
	
	/**
	 * 
	 * @param ns
	 */
	private OWLSirNameSpace(String ns) {
		this.ns = ns;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getNs() {
		return ns;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return this.ns;
	}
}
