package it.cnr.istc.pst.cognition.sir.reasoner;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author alessandroumbrico
 *
 */
public class StimulationRecommendation implements Comparable<StimulationRecommendation> 
{
	private String profileId;							// profile Id
	private String stimulationId; 						// stimulation ID
	private String stimulationName;
	private String stimulationType;						// belonging onto class
	private double rank;									// computed ranking value
	private List<InteractionParameter> parameters;		// interaction parameters
	
	/**
	 * 
	 * @param profile
	 * @param stimulus
	 * @param rank
	 */
	protected StimulationRecommendation(String profileId, String id, String name, String type, double rank) {
		this.profileId = profileId;
		this.stimulationId = id;
		this.stimulationName = name;
		this.stimulationType = type;
		this.rank = rank;
		this.parameters = new ArrayList<>();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getProfileId() {
		return this.profileId;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getStimulationName() {
		return stimulationName;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getStimulationType() {
		return stimulationType;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getRank() {
		return rank;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getStimulationId() {
		return stimulationId;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<InteractionParameter> getInteractionParameters() {
		return new ArrayList<>(this.parameters);
	}
	
	/**
	 * 
	 * @param action
	 * @param param
	 * @param value
	 */
	public void addInteractionParameter(StimulationRecommendation action, String param, String value) {
		this.parameters.add(new InteractionParameter(action, param, value));
	}
	
	/**
	 * 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stimulationId == null) ? 0 : stimulationId.hashCode());
		result = prime * result + ((stimulationName == null) ? 0 : stimulationName.hashCode());
		result = prime * result + ((stimulationType == null) ? 0 : stimulationType.hashCode());
		return result;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StimulationRecommendation other = (StimulationRecommendation) obj;
		if (stimulationId == null) {
			if (other.stimulationId != null)
				return false;
		} else if (!stimulationId.equals(other.stimulationId))
			return false;
		if (stimulationName == null) {
			if (other.stimulationName != null)
				return false;
		} else if (!stimulationName.equals(other.stimulationName))
			return false;
		if (stimulationType == null) {
			if (other.stimulationType != null)
				return false;
		} else if (!stimulationType.equals(other.stimulationType))
			return false;
		return true;
	}

	/**
	 * 
	 */
	@Override
	public int compareTo(StimulationRecommendation o) {
		// get ranking value
		return this.rank > o.rank ? -1 : this.rank < o.rank ? 1 : 0; 
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return "[StimulusRecommendation rank= " + this.rank + ", stimuls= " + this.stimulationName + ", type=" + this.stimulationType +"]";
	}
}
