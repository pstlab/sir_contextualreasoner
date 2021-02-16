package it.cnr.istc.pst.cognition.sir.reasoner;

/**
 * 
 * @author alessandroumbrico
 *
 */
public class InteractionParameter 
{
	private StimulationRecommendation action;
	private String parameterType;
	private String value;
	
	/**
	 * 
	 * @param action
	 * @param paramType
	 * @param value
	 */
	protected InteractionParameter(StimulationRecommendation action, String paramType, String value) {
		this.action = action;
		this.parameterType = paramType;
		this.value = value;
	}
	
	/**
	 * 
	 * @return
	 */
	public StimulationRecommendation getAction() {
		return action;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getParameterType() {
		return this.parameterType;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getValue() {
		return this.value;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return "[InteractionParameter type= " +  this.parameterType +", value= " + this.value + "]";
	}
}

